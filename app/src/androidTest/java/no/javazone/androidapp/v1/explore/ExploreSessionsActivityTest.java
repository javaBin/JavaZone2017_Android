package no.javazone.androidapp.v1.explore;


import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.javazone.androidapp.v1.testutils.BaseActivityTestRule;
import no.javazone.androidapp.v1.testutils.NavigationUtils;
import no.javazone.androidapp.v1.ui.activity.ExploreSessionsActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExploreSessionsActivityTest {

    @Rule
    public BaseActivityTestRule<ExploreSessionsActivity> mActivityRule =
            new BaseActivityTestRule<ExploreSessionsActivity>(ExploreSessionsActivity.class, null);

    @Test
    public void navigationIcon_DisplayAsUp() {
        NavigationUtils.checkNavigationIconIsUp(); }
}
