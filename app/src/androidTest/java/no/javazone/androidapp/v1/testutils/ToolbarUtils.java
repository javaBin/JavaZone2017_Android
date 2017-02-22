package no.javazone.androidapp.v1.testutils;


import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;

import no.javazone.androidapp.v1.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

/**
 * Helper methods for testing toolbar
 */
public class ToolbarUtils {

    public static void checkToolbarIsCompletelyDisplayed() {
        onView(withId(R.id.toolbar)).check(matches(isCompletelyDisplayed()));
    }

    public static void checkToolbarHidesAfterSwipingRecyclerViewUp(int viewResource) {
        ViewInteraction view = onView(withId(R.id.toolbar));

        // Swiping up should hide the toolbar.
        onView(withId(viewResource))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeUp()));

        // Check if the toolbar is hidden.
        view.check(matches(not(isDisplayed())));
    }

    public static void checkToolbarCollapsesAfterSwipingRecyclerViewUp(int viewResource) {
        // TODO
        ViewInteraction view = onView(withId(R.id.toolbar));

        // Swiping up should hide the toolbar.
        onView(withId(viewResource))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeUp()));

        // Check if the toolbar is hidden.
        view.check(matches(not(isDisplayed())));
    }
}
