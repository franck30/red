package com.benoitquenaudon.tvfoot.red.app.domain.matches.state

import com.benoitquenaudon.tvfoot.red.app.common.LceStatus.FAILURE
import com.benoitquenaudon.tvfoot.red.app.common.LceStatus.IN_FLIGHT
import com.benoitquenaudon.tvfoot.red.app.common.LceStatus.SUCCESS
import com.benoitquenaudon.tvfoot.red.app.common.firebase.BaseRedFirebaseAnalytics
import com.benoitquenaudon.tvfoot.red.app.common.schedulers.BaseSchedulerProvider
import com.benoitquenaudon.tvfoot.red.app.domain.matches.displayable.MatchRowDisplayable
import com.benoitquenaudon.tvfoot.red.app.domain.matches.state.MatchesAction.GetLastStateAction
import com.benoitquenaudon.tvfoot.red.app.domain.matches.state.MatchesAction.LoadNextPageAction
import com.benoitquenaudon.tvfoot.red.app.domain.matches.state.MatchesAction.RefreshAction
import com.benoitquenaudon.tvfoot.red.app.domain.matches.state.MatchesIntent.GetLastState
import com.benoitquenaudon.tvfoot.red.app.domain.matches.state.MatchesIntent.InitialIntent
import com.benoitquenaudon.tvfoot.red.app.domain.matches.state.MatchesIntent.LoadNextPageIntent
import com.benoitquenaudon.tvfoot.red.app.domain.matches.state.MatchesIntent.RefreshIntent
import com.benoitquenaudon.tvfoot.red.app.domain.matches.state.MatchesResult.GetLastStateResult
import com.benoitquenaudon.tvfoot.red.app.domain.matches.state.MatchesResult.LoadNextPageResult
import com.benoitquenaudon.tvfoot.red.app.domain.matches.state.MatchesResult.RefreshResult
import com.benoitquenaudon.tvfoot.red.app.injection.scope.ScreenScope
import com.benoitquenaudon.tvfoot.red.app.mvi.RedStateBinder
import com.benoitquenaudon.tvfoot.red.util.logAction
import com.benoitquenaudon.tvfoot.red.util.logIntent
import com.benoitquenaudon.tvfoot.red.util.logResult
import com.benoitquenaudon.tvfoot.red.util.logState
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList
import javax.inject.Inject

@ScreenScope class MatchesStateBinder @Inject constructor(
    private val intentsSubject: PublishSubject<MatchesIntent>,
    private val statesSubject: PublishSubject<MatchesViewState>,
    private val repository: BaseMatchesRepository,
    private val schedulerProvider: BaseSchedulerProvider,
    firebaseAnalytics: BaseRedFirebaseAnalytics
) : RedStateBinder<MatchesIntent, MatchesViewState>(firebaseAnalytics) {

  init {
    compose().subscribe(statesSubject::onNext)
  }

  override fun processIntents(intents: Observable<MatchesIntent>) {
    intents.subscribe(intentsSubject::onNext)
  }

  override fun states(): Observable<MatchesViewState> = statesSubject

  private fun compose(): Observable<MatchesViewState> {
    return intentsSubject
        .doOnNext(this::logIntent)
        .scan(initialIntentFilter)
        .map(this::actionFromIntent)
        .doOnNext(this::logAction)
        .compose<MatchesResult>(actionToResultTransformer)
        .doOnNext(this::logResult)
        .scan(MatchesViewState.idle(), reducer)
        .doOnNext(this::logState)
  }

  private val initialIntentFilter: BiFunction<MatchesIntent, MatchesIntent, MatchesIntent>
    get() = BiFunction { _: MatchesIntent, newIntent: MatchesIntent ->
      // if isReConnection (e.g. after config change)
      // i.e. we are inside the scan, meaning there has already
      // been intent in the past, meaning the InitialIntent cannot
      // be the first => it is a reconnection.
      if (newIntent is InitialIntent) {
        GetLastState
      } else {
        newIntent
      }
    }

  private fun actionFromIntent(intent: MatchesIntent): MatchesAction =
      when (intent) {
        is InitialIntent -> RefreshAction
        is RefreshIntent -> RefreshAction
        is GetLastState -> GetLastStateAction
        is LoadNextPageIntent -> LoadNextPageAction(intent.pageIndex)
      }

  private val refreshTransformer: ObservableTransformer<RefreshAction, RefreshResult>
    get() = ObservableTransformer { actions: Observable<RefreshAction> ->
      actions.flatMap({
        repository.loadPage(0)
            .toObservable()
            .map { RefreshResult.success(it) }
            .onErrorReturn { RefreshResult.failure(it) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith(RefreshResult.inFlight())
      })
    }

  private val loadNextPageTransformer: ObservableTransformer<LoadNextPageAction, LoadNextPageResult>
    get() = ObservableTransformer { actions: Observable<LoadNextPageAction> ->
      actions.flatMap(
          { (pageIndex) ->
            repository.loadPage(pageIndex)
                .toObservable()
                .map { matches -> LoadNextPageResult.success(pageIndex, matches) }
                .onErrorReturn { LoadNextPageResult.failure(it) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .startWith(LoadNextPageResult.inFlight())
          })
    }

  private val getLastStateTransformer: ObservableTransformer<GetLastStateAction, GetLastStateResult>
    get() = ObservableTransformer { actions: Observable<GetLastStateAction> ->
      actions.map({ GetLastStateResult })
    }

  private val actionToResultTransformer: ObservableTransformer<MatchesAction, MatchesResult>
    get() = ObservableTransformer { actions: Observable<MatchesAction> ->
      actions.publish({ shared ->
        Observable.merge<MatchesResult>(
            shared.ofType(RefreshAction::class.java).compose(refreshTransformer),
            shared.ofType(LoadNextPageAction::class.java).compose(loadNextPageTransformer),
            shared.ofType(GetLastStateAction::class.java).compose(getLastStateTransformer))
            .mergeWith(
                // Error for not implemented actions
                shared.filter({ v ->
                  v !is RefreshAction && v !is LoadNextPageAction && v !is GetLastStateAction
                }).flatMap({ w ->
                  Observable.error<MatchesResult>(
                      IllegalArgumentException("Unknown Action type: " + w))
                }))
      })
    }

  companion object {

    private val reducer = BiFunction { previousState: MatchesViewState, matchesResult: MatchesResult ->
      when (matchesResult) {
        is RefreshResult -> {
          when (matchesResult.status) {
            IN_FLIGHT -> previousState.copy(refreshLoading = true, error = null)
            FAILURE -> previousState.copy(refreshLoading = false, error = matchesResult.throwable)
            SUCCESS -> {
              val matches = checkNotNull((matchesResult).matches) { "Matches are null" }

              previousState.copy(
                  refreshLoading = false,
                  error = null,
                  hasMore = !matches.isEmpty(),
                  currentPage = 0,
                  matches = MatchRowDisplayable.fromMatches(matches))
            }
          }
        }
        is GetLastStateResult -> previousState.copy()
        is LoadNextPageResult -> {
          when (matchesResult.status) {
            IN_FLIGHT -> previousState.copy(nextPageLoading = true, error = null)
            FAILURE -> previousState.copy(nextPageLoading = false, error = matchesResult.error)
            SUCCESS -> {
              val newMatches = checkNotNull((matchesResult).matches) { "Matches are null" }

              val matches = ArrayList<MatchRowDisplayable>()
              matches.addAll(previousState.matches)
              matches.addAll(MatchRowDisplayable.fromMatches(newMatches))

              previousState.copy(
                  nextPageLoading = false,
                  error = null,
                  matches = matches,
                  currentPage = (matchesResult).pageIndex,
                  hasMore = !newMatches.isEmpty())
            }
          }
        }
      }
    }
  }
}
