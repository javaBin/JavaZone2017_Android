package no.javazone.ui.activity.base;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import no.javazone.BuildConfig;
import no.javazone.R;
import no.javazone.archframework.model.domain.Account;
import no.javazone.messaging.MessagingRegistration;
import no.javazone.navigation.AppNavigationViewAsDrawerImpl;
import no.javazone.navigation.NavigationModel;
import no.javazone.ui.widget.MultiSwipeRefreshLayout;
import no.javazone.util.ImageLoader;
import no.javazone.util.LUtils;
import no.javazone.util.RecentTasksStyler;

import static no.javazone.navigation.NavigationModel.*;
import static no.javazone.util.LogUtils.LOGD;
import static no.javazone.util.LogUtils.LOGW;
import static no.javazone.util.LogUtils.makeLogTag;

public abstract class BaseActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        MultiSwipeRefreshLayout.CanChildScrollUpCallback,
        AppNavigationViewAsDrawerImpl.NavigationDrawerStateListener {

    private static final String TAG = makeLogTag(BaseActivity.class);

    // Navigation drawer
    private AppNavigationViewAsDrawerImpl mAppNavigationViewAsDrawer;

    // Toolbar
    private Toolbar mToolbar;

    // Helper methods for L APIs
    private LUtils mLUtils;

    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    // SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Registration with GCM for notifications
    private MessagingRegistration mMessagingRegistration;

    // handle to our sync observer (that notifies us about changes in our sync state)
    private Object mSyncObserverHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecentTasksStyler.styleRecentTasksEntry(this);
/*

        mMessagingRegistration = MessagingRegistrationProvider.provideMessagingRegistration(this);


        if (savedInstanceState == null) {
            mMessagingRegistration.registerDevice();
        } */

        Account.createSyncAccount(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mLUtils = LUtils.getInstance(this);
    }

    private void trySetupSwipeRefresh() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.flat_button_text);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    requestDataRefresh();
                }
            });

            if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
                MultiSwipeRefreshLayout mswrl = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
                mswrl.setCanChildScrollUpCallback(this);
            }
        }
    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses of
     * BaseActivity override this to indicate what nav drawer item corresponds to them Return
     * NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */
    protected NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationItemEnum.INVALID;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getToolbar();
    }

    @Override
    public void onNavDrawerStateChanged(boolean isOpen, boolean isAnimating) {
        // Nothing to do
    }

    @Override
    public void onNavDrawerSlide(float offset) {
    }

    @Override
    public void onBackPressed() {
        if (mAppNavigationViewAsDrawer.isNavDrawerOpen()) {
            mAppNavigationViewAsDrawer.closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key != null && key.equals(BuildConfig.PREF_ATTENDEE_AT_VENUE)) {
            LOGD(TAG, "Attendee at venue preference changed, repopulating nav drawer and menu.");
            if (mAppNavigationViewAsDrawer != null) {
                mAppNavigationViewAsDrawer.updateNavigationItems();
            }
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mAppNavigationViewAsDrawer = new AppNavigationViewAsDrawerImpl(new ImageLoader(this), this);
        mAppNavigationViewAsDrawer.activityReady(this, getSelfNavDrawerItem());

        if (getSelfNavDrawerItem() != NavigationItemEnum.INVALID) {
            setToolbarForNavigation();
        }

        trySetupSwipeRefresh();

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        } else {
            LOGW(TAG, "No view with ID main_content to fade in.");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_refresh:
                requestDataRefresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void requestDataRefresh() {
        ContentResolver contentResolver = getContentResolver();

        LOGD(TAG, "Requesting manual data refresh.");
        // get data here
       // SyncHelper.requestManualSync();
    }

    /**
     * This utility method handles Up navigation intents by searching for a parent activity and
     * navigating there if defined. When using this for an activity make sure to define both the
     * native parentActivity as well as the AppCompat one when supporting API levels less than 16.
     * when the activity has a single parent activity. If the activity doesn't have a single parent
     * activity then don't define one and this method will use back button functionality. If "Up"
     * functionality is still desired for activities without parents then use {@code
     * syntheticParentActivity} to define one dynamically.
     * <p/>
     * Note: Up navigation intents are represented by a back arrow in the top left of the Toolbar in
     * Material Design guidelines.
     *
     * @param currentActivity         Activity in use when navigate Up action occurred.
     * @param syntheticParentActivity Parent activity to use when one is not already configured.
     */
    public static void navigateUpOrBack(Activity currentActivity,
                                        Class<? extends Activity> syntheticParentActivity) {
        // Retrieve parent activity from AndroidManifest.
        Intent intent = NavUtils.getParentActivityIntent(currentActivity);

        // Synthesize the parent activity when a natural one doesn't exist.
        if (intent == null && syntheticParentActivity != null) {
            try {
                intent = NavUtils.getParentActivityIntent(currentActivity, syntheticParentActivity);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (intent == null) {
            // No parent defined in manifest. This indicates the activity may be used by
            // in multiple flows throughout the app and doesn't have a strict parent. In
            // this case the navigation up button should act in the same manner as the
            // back button. This will result in users being forwarded back to other
            // applications if currentActivity was invoked from another application.
            currentActivity.onBackPressed();
        } else {
            if (NavUtils.shouldUpRecreateTask(currentActivity, intent)) {
                // Need to synthesize a backstack since currentActivity was probably invoked by a
                // different app. The preserves the "Up" functionality within the app according to
                // the activity hierarchy defined in AndroidManifest.xml via parentActivity
                // attributes.
                TaskStackBuilder builder = TaskStackBuilder.create(currentActivity);
                builder.addNextIntentWithParentStack(intent);
                builder.startActivities();
            } else {
                // Navigate normally to the manifest defined "Up" activity.
                NavUtils.navigateUpTo(currentActivity, intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Perform one-time bootstrap setup, if needed

        /*DataBootstrapService.startDataBootstrapIfNecessary(this);

        // Check to ensure a Google Account is active for the app. Placing the check here ensures
        // it is run again in the case where a Google Account wasn't present on the device and a
        // picker had to be started.

        // Watch for sync state changes
        //mSyncStatusObserver.onStatusChanged(0);
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver); */
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    /**
     * Converts an intent into a {@link Bundle} suitable for use as fragment arguments.
     */
    public static Bundle intentToFragmentArguments(Intent intent) {
        Bundle arguments = new Bundle();
        if (intent == null) {
            return arguments;
        }

        final Uri data = intent.getData();
        if (data != null) {
            arguments.putParcelable("_uri", data);
        }

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            arguments.putAll(intent.getExtras());
        }

        return arguments;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }


    public Toolbar getToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mToolbar != null) {
                mToolbar.setNavigationContentDescription(getResources().getString(R.string
                        .navdrawer_description_a11y));
                setSupportActionBar(mToolbar);
            }
        }
        return mToolbar;
    }

    private void setToolbarForNavigation() {
        if (mToolbar != null) {
            mToolbar.setNavigationIcon(R.drawable.ic_hamburger);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAppNavigationViewAsDrawer.showNavigation();
                }
            });
        }
    }

    /**
     * @param clickListener The {@link android.view.View.OnClickListener} for the navigation icon of
     *                      the toolbar.
     */
    protected void setToolbarAsUp(View.OnClickListener clickListener) {
        // Initialise the toolbar
        getToolbar();

        mToolbar.setNavigationIcon(R.drawable.ic_up);
        mToolbar.setNavigationContentDescription(R.string.close_and_go_back);
        mToolbar.setNavigationOnClickListener(clickListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    protected void onRefreshingStateChanged(boolean refreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    public LUtils getLUtils() {
        return mLUtils;
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        return false;
    }

    /**
     * Configure this Activity as a floating window, with the given {@code width}, {@code height}
     * and {@code alpha}, and dimming the background with the given {@code dim} value.
     */
    protected void setupFloatingWindow(int width, int height, int alpha, float dim) {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getResources().getDimensionPixelSize(width);
        params.height = getResources().getDimensionPixelSize(height);
        params.alpha = alpha;
        params.dimAmount = dim;
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(params);
    }

    /**
     * Returns true if the theme sets the {@code R.attr.isFloatingWindow} flag to true.
     */
    protected boolean shouldBeFloatingWindow() {
        Resources.Theme theme = getTheme();
        TypedValue floatingWindowFlag = new TypedValue();

        // Check isFloatingWindow flag is defined in theme.
        if (theme == null || !theme
                .resolveAttribute(R.attr.isFloatingWindow, floatingWindowFlag, true)) {
            return false;
        }

        return (floatingWindowFlag.data != 0);
    }

}
