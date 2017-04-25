package io.oldering.tvfoot.red.matches.state;

import com.google.auto.value.AutoValue;
import io.oldering.tvfoot.red.matches.displayable.MatchRowDisplayable;

public interface MatchesIntent {
  @AutoValue abstract class InitialIntent implements MatchesIntent {
    public static InitialIntent create() {
      return new AutoValue_MatchesIntent_InitialIntent();
    }
  }

  @AutoValue abstract class GetLastState implements MatchesIntent {
    public static GetLastState create() {
      return new AutoValue_MatchesIntent_GetLastState();
    }
  }

  @AutoValue abstract class LoadNextPageIntent implements MatchesIntent {
    public abstract int pageIndex();

    public static LoadNextPageIntent create(int pageIndex) {
      return new AutoValue_MatchesIntent_LoadNextPageIntent(pageIndex);
    }
  }

  @AutoValue abstract class MatchRowClickIntent implements MatchesIntent {
    public abstract MatchRowDisplayable match();

    public static MatchRowClickIntent create(MatchRowDisplayable match) {
      return new AutoValue_MatchesIntent_MatchRowClickIntent(match);
    }
  }
}
