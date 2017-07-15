package com.benoitquenaudon.tvfoot.red.app.domain.matches;

import android.content.Intent;
import android.net.Uri;
import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.benoitquenaudon.tvfoot.red.R;
import com.benoitquenaudon.tvfoot.red.RedApp;
import com.benoitquenaudon.tvfoot.red.app.domain.match.MatchActivity;
import com.benoitquenaudon.tvfoot.red.util.BasicActivityTest;
import com.jakewharton.espresso.OkHttp3IdlingResource;
import okhttp3.OkHttpClient;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@LargeTest @RunWith(AndroidJUnit4.class) public class MatchActivityTest extends BasicActivityTest {

  @Rule public ActivityTestRule<MatchActivity> activityTestRule =
      new ActivityTestRule<MatchActivity>(MatchActivity.class) {
        @Override protected Intent getActivityIntent() {
          return new Intent(Intent.ACTION_VIEW,
              Uri.parse("https://tvfoot/match/league/home/away/1"));
        }
      };

  @Ignore("caca bouda") @Test public void loadFirstPage() {
    RedApp app = (RedApp) activityTestRule.getActivity().getApplication();
    OkHttpClient client = app.getComponent().okHttpClient();
    Espresso.registerIdlingResources(OkHttp3IdlingResource.create("OkHttp", client));

    onView(withId(R.id.match_detail_headline)).check(matches(isDisplayed()));
  }
}
