package no.javazone.androidapp.v1.explore;

import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.database.ScheduleContract;
import no.javazone.androidapp.v1.mockdata.ExploreMockCursor;
import no.javazone.androidapp.v1.navigation.NavigationModel;
import no.javazone.androidapp.v1.testutils.BaseActivityTestRule;
import no.javazone.androidapp.v1.testutils.NavigationUtils;
import no.javazone.androidapp.v1.testutils.ToolbarUtils;
import no.javazone.androidapp.v1.ui.activity.ExploreActivity;
import no.javazone.androidapp.v1.ui.activity.ExploreSessionsActivity;
import no.javazone.androidapp.v1.ui.activity.SearchActivity;
import no.javazone.androidapp.v1.ui.activity.SessionDetailActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExploreActivityTest {

    @Rule
    public BaseActivityTestRule<ExploreActivity> mActivityRule =
            new BaseActivityTestRule<ExploreActivity>(ExploreActivity.class,
                    new StubExploreModel(
                            InstrumentationRegistry.getTargetContext(),
                            ExploreMockCursor.getCursorForExplore(),
                            ExploreMockCursor.getCursorForTags()));

    @Test
    public void Keynote_IsVisibleWithoutScrolling() {
        onView(withText(ExploreMockCursor.TITLE_KEYNOTE)).check(matches(isDisplayed()));
    }

    @Test
    public void Toolbar_InitiallyDisplayed() {
        ToolbarUtils.checkToolbarIsCompletelyDisplayed();
    }

    @Test
    public void Toolbar_HidesAfterSwipeUp() {
        ToolbarUtils.checkToolbarHidesAfterSwipingRecyclerViewUp(R.id.explore_card_list);
    }

    @Test
    public void SessionClicked_IntentFired() {
        // Given a visible session
        onView(withText(ExploreMockCursor.TITLE_KEYNOTE)).check(matches(isDisplayed()));

        // When clicking on the session
        onView(withText(ExploreMockCursor.TITLE_KEYNOTE)).perform(click());

        // Then the intent to open the session detail activity for that session is fired
        Uri expectedSessionUri =
                ScheduleContract.Sessions.buildSessionUri(ExploreMockCursor.KEYNOTE_ID);
        intended(allOf(
                hasComponent(SessionDetailActivity.class.getName()),
                hasData(expectedSessionUri)));
    }

    @Test
    public void MoreClicked_IntentFired() {
        // Given a visible topic
        onView(withId(R.id.explore_card_list))
                .perform(RecyclerViewActions.scrollTo(
                        hasDescendant(withText(ExploreMockCursor.TRACK_CLOUD_NAME))));

        // When clicking on the "More" button
        onView(allOf(withText(R.string.more_items_button),
                hasSibling(withText(ExploreMockCursor.TRACK_CLOUD_NAME))
        )).perform(click());

        // Then the intent to open the explore sessions activity for that topic is fired
        intended(allOf(
                hasComponent(ExploreSessionsActivity.class.getName()),
                hasExtra(ExploreSessionsActivity.EXTRA_FILTER_TAG,
                        ExploreMockCursor.TRACK_CLOUD)));
    }

    @Test
    public void SearchClicked_IntentFired() {
        // When clicking on the "search" button
        onView(withId(R.id.menu_search)).perform(click());

        // Then the intent to open the search activity is fired
        intended(hasComponent(SearchActivity.class.getName()));
    }

    @Ignore("Not implemented")
    public void trackScrolledHorizontally_SessionsShown() {

    }

    @Test
    public void navigationIcon_DisplaysAsMenu() {
        NavigationUtils.checkNavigationIconIsMenu();
    }

    @Test
    public void navigationIcon_OnClick_NavigationDisplayed() {
        NavigationUtils.checkNavigationIsDisplayedWhenClickingMenuIcon();
    }

    @Test
    public void navigation_WhenShown_CorrectItemIsSelected() {
        NavigationUtils.checkNavigationItemIsSelected(NavigationModel.NavigationItemEnum.EXPLORE);
    }

    @Test
    public void livestreamCard_IsNotVisible() {
        onView(withText(R.string.live_now)).check(doesNotExist());
    }
}
