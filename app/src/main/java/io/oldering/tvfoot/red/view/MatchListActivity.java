package io.oldering.tvfoot.red.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import com.genius.groupie.GroupAdapter;
import com.genius.groupie.Section;
import io.oldering.tvfoot.red.R;
import io.oldering.tvfoot.red.databinding.ActivityMatchListBinding;
import io.oldering.tvfoot.red.flowcontroller.FlowController;
import io.oldering.tvfoot.red.util.rxbus.RxBus;
import io.oldering.tvfoot.red.util.rxbus.event.MatchClickEvent;
import io.oldering.tvfoot.red.util.schedulers.BaseSchedulerProvider;
import io.oldering.tvfoot.red.view.item.DayHeaderItem;
import io.oldering.tvfoot.red.view.item.MatchItem;
import io.oldering.tvfoot.red.viewmodel.DayHeaderViewModel;
import io.oldering.tvfoot.red.viewmodel.MatchListViewModel;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.inject.Inject;
import timber.log.Timber;

public class MatchListActivity extends BaseActivity {
  private final CompositeDisposable disposables = new CompositeDisposable();
  @Inject MatchListViewModel matchListVM;
  @Inject BaseSchedulerProvider schedulerProvider;
  @Inject RxBus rxBus;
  private GroupAdapter matchListGroupAdapter;
  private PublishSubject<Integer> paginatorSubject = PublishSubject.create();
  private boolean requestUnderWay;
  private int pageIndex = 0;
  private ProgressBar progressBar;
  private HashMap<String, Section> sections = new LinkedHashMap<>();

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActivityComponent().inject(this);

    ActivityMatchListBinding dataBinding =
        DataBindingUtil.setContentView(this, R.layout.activity_match_list);
    Toolbar toolbar = dataBinding.matchListFilterToolbar;
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle("");
    }

    progressBar = dataBinding.progressPaging;

    RecyclerView recyclerView = dataBinding.matchListRecyclerView;
    matchListGroupAdapter = new GroupAdapter();
    recyclerView.setAdapter(matchListGroupAdapter);
    recyclerView.addOnScrollListener(
        new InfiniteScrollListener((LinearLayoutManager) recyclerView.getLayoutManager()) {
          @Override public void onLoadMore(int current_page) {
            Timber.d("onLoadMore %d, %d", current_page, pageIndex);
            if (requestUnderWay) return;

            paginatorSubject.onNext(++pageIndex);
          }
        });

    FloatingActionButton fab = dataBinding.matchListFilterFab;
    fab.setOnClickListener(
        view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null)
            .show());
  }

  @Override protected void onStart() {
    super.onStart();
    bind();

    paginatorSubject.onNext(pageIndex);
  }

  private void bind() {
    Disposable matchDisposable = paginatorSubject.doOnNext(integer -> {
      requestUnderWay = true;
      Timber.d("Loading page : %d", pageIndex);
      progressBar.setVisibility(View.VISIBLE);
    })
        .map(matchListVM::getFilter)
        .concatMap(matchListVM::getMatches)
        .observeOn(schedulerProvider.ui())
        .map(MatchListActivity.this::addItem)
        .doOnNext(irrelevant -> {
          requestUnderWay = false;
          progressBar.setVisibility(View.INVISIBLE);
        })
        .subscribe();

    Disposable matchClick = rxBus.toObservable().subscribe(event -> {
      if (event instanceof MatchClickEvent) {
        MatchClickEvent matchClickEvent = (MatchClickEvent) event;
        FlowController.launchMatchDetailActivity(MatchListActivity.this,
            matchClickEvent.getMatchVM());
      }
    });

    disposables.add(matchDisposable);
    disposables.add(matchClick);
  }

  @Override protected void onStop() {
    super.onStop();
    unbind();
  }

  private void unbind() {
    disposables.clear();
  }

  private Irrelevant addItem(MatchItem matchItem) {
    String key = matchItem.getKey();
    Timber.d("the key is %s", key);
    if (!sections.containsKey(key)) {
      Section section = new Section(new DayHeaderItem(DayHeaderViewModel.create(key)));
      sections.put(key, section);
      matchListGroupAdapter.add(section);
    }

    sections.get(key).add(matchItem);

    return Irrelevant.INSTANCE;
  }

  private enum Irrelevant {INSTANCE}
}
