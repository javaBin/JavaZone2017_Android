package no.javazone.androidapp.v1.about;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.javazone.androidapp.v1.navigation.NavigationModel;
import no.javazone.androidapp.v1.testutils.BaseActivityTestRule;
import no.javazone.androidapp.v1.testutils.NavigationUtils;
import no.javazone.androidapp.v1.ui.activity.AboutActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AboutActivityTest {
    @Rule
    public BaseActivityTestRule<AboutActivity> mActivityRule =
            new BaseActivityTestRule<AboutActivity>(
                    AboutActivity.class, null);

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
        NavigationUtils.checkNavigationItemIsSelected(NavigationModel.NavigationItemEnum.ABOUT);
    }
}