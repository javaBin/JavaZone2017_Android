package no.javazone.androidapp.v1.testutils;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.view.View;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;

import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.navigation.NavigationModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertTrue;

public class NavigationUtils {
    public static void showNavigation() {
        onView(withId(R.id.drawer_layout)).perform(open());
    }

    public static void checkScreenTitleIsDisplayed(int stringResource) {
        onView(allOf(withParent(withId(R.id.toolbar)),
                withText(stringResource))).check(matches(isDisplayed()));
    }

    public static void clickOnNavigationItemAndCheckActivityDisplayed(
            int navigationItemStringResource, int expectedTitleResource) {
        NavigationUtils.showNavigation();
        onView(getNavigationItemWithString(navigationItemStringResource)).perform(click());
        NavigationUtils.checkScreenTitleIsDisplayed(expectedTitleResource);
    }

    public static void checkNavigationItemIsDisplayed(int navigationItemStringResource) {
        NavigationUtils.showNavigation();
        onView(getNavigationItemWithString(navigationItemStringResource)).check(matches(isDisplayed()));
    }

    public static void checkNavigationItemIsNotDisplayed(int navigationItemStringResource) {
        NavigationUtils.showNavigation();
        onView(getNavigationItemWithString(navigationItemStringResource)).check(doesNotExist());
    }

    public static void cleanUpActivityStack(ActivityTestRule rule) {
        NavigationUtils.showNavigation();
        onView(getNavigationItemWithString(R.string.navdrawer_item_explore)).perform(click());
        rule.getActivity().finish();
    }

    public static void checkNavigationIsDisplayedWhenClickingMenuIcon() {
        checkNavigationItemIsDisplayed(R.string.navdrawer_item_explore);
    }

    public static void checkNavigationIconIsUp() {
        onView(withContentDescription(R.string.close_and_go_back)).check(matches(isDisplayed()));
    }

    public static void checkNavigationIconIsMenu() {
        onView(withContentDescription(R.string.navdrawer_description_a11y)).check(
                matches(isDisplayed()));
    }

    public static void checkNavigationItemIsSelected(
            NavigationModel.NavigationItemEnum expectedSelectedItem) {
        // Given navigation menu
        NavigationUtils.showNavigation();

        boolean selectedFound = false;

        for (int i = 0; i < NavigationModel.NavigationItemEnum.values().length; i++) {
            NavigationModel.NavigationItemEnum item =
                    NavigationModel.NavigationItemEnum.values()[i];
            try {
                // Check item is displayed
                onView(getNavigationItemWithString(item.getTitleResource())).check(
                        matches(isDisplayed()));

                // If item is shown, check item is not activated, unless it is the requested one
                if (NavigationModel.NavigationItemEnum.values()[i].getId() ==
                        expectedSelectedItem.getId()) {
                    onView(getNavigationItemWithString(item.getTitleResource())).check(
                            matches(isChecked()));
                    selectedFound = true;
                } else {
                    onView(getNavigationItemWithString(item.getTitleResource())).check(
                            matches(not(isChecked())));
                }

            } catch (NoMatchingViewException e) {
                // Not all navigation items in the enum are shown. This test doesn't aim to
                // check that the correct items are shown, but that only the selected one among
                // those shown is shown as selected. Tests in the navigation package check
                // correct items are shown.
            }
        }

        // Sanity check to ensure the tests in the try/catch block weren't all skipped
        assertTrue(selectedFound);
    }

    private static Matcher<View> getNavigationItemWithString(int stringResource) {
        return CoreMatchers.allOf(isAssignableFrom(AppCompatCheckedTextView.class), withText(stringResource));
    }
}
