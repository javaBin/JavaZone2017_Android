package no.javazone.androidapp.v1.maps;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.javazone.androidapp.v1.navigation.NavigationModel;
import no.javazone.androidapp.v1.testutils.BaseActivityTestRule;
import no.javazone.androidapp.v1.testutils.NavigationUtils;
import no.javazone.androidapp.v1.ui.activity.MapActivity;

@RunWith(AndroidJUnit4.class)
@android.support.test.filters.LargeTest
public class MapActivityTest {
    @Rule
    public BaseActivityTestRule<MapActivity> mActivityRule =
            new BaseActivityTestRule<MapActivity>(
                    MapActivity.class, null);

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
        NavigationUtils.checkNavigationItemIsSelected(NavigationModel.NavigationItemEnum.MAP);
    }
}
