package com.benoitquenaudon.tvfoot.red.app.domain.matches

import com.benoitquenaudon.tvfoot.red.app.data.entity.FilterTeam
import com.benoitquenaudon.tvfoot.red.app.data.entity.Tag
import com.benoitquenaudon.tvfoot.red.app.domain.matches.displayable.HeaderRowDisplayable
import com.benoitquenaudon.tvfoot.red.app.domain.matches.displayable.LoadingRowDisplayable
import com.benoitquenaudon.tvfoot.red.app.domain.matches.displayable.MatchRowDisplayable
import com.benoitquenaudon.tvfoot.red.app.domain.matches.displayable.MatchesItemDisplayable
import com.benoitquenaudon.tvfoot.red.app.domain.matches.filters.FiltersItemDisplayable.TeamSearchResultDisplayable
import com.benoitquenaudon.tvfoot.red.app.mvi.MviViewState
import com.benoitquenaudon.tvfoot.red.util.TagName
import com.benoitquenaudon.tvfoot.red.util.TagTargets
import com.benoitquenaudon.tvfoot.red.util.TeamCode
import java.util.ArrayList

data class MatchesViewState(
    val matches: List<MatchRowDisplayable> = emptyList(),
    val error: Throwable? = null,
    val nextPageLoading: Boolean = false,
    val refreshLoading: Boolean = false,
    val teamMatchesLoading: Boolean = false,
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
    val tagsLoading: Boolean = false,
    val tagsError: Throwable? = null,
    val tags: List<Tag> = emptyList(),
    var filteredCompetitions: Map<TagName, TagTargets> = emptyMap(),
    var filteredBroadcasters: Map<TagName, TagTargets> = emptyMap(),
    val searchInput: String = "",
    val searchingTeam: Boolean = false,
    val searchedTeams: List<TeamSearchResultDisplayable> = emptyList(),
    val teams: List<FilterTeam> = emptyList(),
    val filteredTeams: List<TeamCode> = emptyList()
) : MviViewState {
  fun matchesItemDisplayables(
      nextPageLoading: Boolean,
      loadingSpecificMatches: Boolean,
      filteredBroadcasters: Map<TagName, TagTargets>,
      filteredCompetitions: Map<TagName, TagTargets>,
      filteredTeams: List<TeamCode>
  ): List<MatchesItemDisplayable> {

    val headers = ArrayList<String>()
    val items = ArrayList<MatchesItemDisplayable>()

    val filteredCompetitionTargets = filteredCompetitions.values.flatten().toSet()
    val filteredBroadcasterTargets = filteredBroadcasters.values.flatten().toSet()
    val filteredTeamsCodes = filteredTeams.toSet()
    for (match in matches) {
      if (filteredCompetitionTargets.isNotEmpty() && filteredCompetitionTargets.intersect(
          match.tags).isEmpty()) {
        continue
      }
      if (filteredBroadcasterTargets.isNotEmpty() &&
          filteredBroadcasterTargets.intersect(
              match.broadcasters.map { it.broadcasterCode }).isEmpty()) {
        continue
      }
      if (filteredTeamsCodes.isNotEmpty() &&
          filteredTeamsCodes.intersect(
              setOf(match.homeTeam.code, match.awayTeam.code)).isEmpty()) {
        continue
      }

      if (!headers.contains(match.headerKey)) {
        headers.add(match.headerKey)
        items.add(HeaderRowDisplayable.create(match.headerKey))
      }
      items.add(match)
    }
    if (!items.isEmpty() && (nextPageLoading || loadingSpecificMatches)) {
      items.add(LoadingRowDisplayable)
    }
    return items
  }

  override fun toString(): String {
    return "MatchesViewState(nextPageLoading=$nextPageLoading, refreshLoading=$refreshLoading, teamMatchesLoading=$teamMatchesLoading, currentPage=$currentPage, hasMore=$hasMore, tagsLoading=$tagsLoading, searchInput='$searchInput', searchingTeam=$searchingTeam)"
  }


  companion object {
    fun idle(): MatchesViewState = MatchesViewState()
  }
}
