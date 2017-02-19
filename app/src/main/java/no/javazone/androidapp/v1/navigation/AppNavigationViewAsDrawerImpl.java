/*
 * Copyright (c) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package no.javazone.androidapp.v1.navigation;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.util.ImageLoader;
import no.javazone.androidapp.v1.util.SettingsUtils;
import no.javazone.androidapp.v1.util.UIUtils;

import static no.javazone.androidapp.v1.navigation.NavigationModel.*;
import static no.javazone.androidapp.v1.util.LogUtils.LOGE;
import static no.javazone.androidapp.v1.util.LogUtils.makeLogTag;

/**
 * This is the implementation of {@link AppNavigationView} using a {@link DrawerLayout}. This
 * extends {@link AppNavigationViewAbstractImpl} so only UI specific methods are implemented.
 */
public class AppNavigationViewAsDrawerImpl extends AppNavigationViewAbstractImpl
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = makeLogTag(AppNavigationViewAsDrawerImpl.class);

    // Delay to launch nav drawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;

    // Fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;

    /**
     * Key to track whether the {@link #mAccountSpinner} drop down view is visible when saving the
     * state of this view.
     */
    private static final String ACCOUNT_SPINNER_DROP_DOWN_VISIBLE =
            "account_spinner_drop_down_visible";

    private DrawerLayout mDrawerLayout;

    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private Runnable mDeferredOnDrawerClosedRunnable;

    private NavigationView mNavigationView;

    private Spinner mAccountSpinner;

    private boolean mAccountSpinnerDropDownViewVisible;

    private Handler mHandler;

    private ImageLoader mImageLoader;

    private NavigationDrawerStateListener mNavigationDrawerStateListener;

    public AppNavigationViewAsDrawerImpl(ImageLoader imageLoader,
            NavigationDrawerStateListener listener) {
        mImageLoader = imageLoader;
        mNavigationDrawerStateListener = listener;
    }

    @Override
    public void displayNavigationItems(final NavigationItemEnum[] items) {
        createNavDrawerItems(items);
        setSelectedNavDrawerItem(mSelfItem);
        setupAccountBox();
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        NavigationItemEnum item = NavigationItemEnum.getById(menuItem.getItemId());
        onNavDrawerItemClicked(item);
        return true;
    }

    @Override
    public void setUpView() {
        mHandler = new Handler();

        mDrawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }

        // setup the status bar color to be colorPrimaryDark from the theme
        mDrawerLayout.setStatusBarBackgroundColor(
                UIUtils.getThemeColor(mActivity, R.attr.colorPrimaryDark,
                        R.color.theme_primary_dark));

        mNavigationView = (NavigationView) mActivity.findViewById(R.id.nav_view);

        if (mSelfItem == NavigationItemEnum.INVALID) {
            // do not show a nav drawer
            mDrawerLayout = null;
            return;
        }

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                // run deferred action, if we have one
                if (mDeferredOnDrawerClosedRunnable != null) {
                    mDeferredOnDrawerClosedRunnable.run();
                    mDeferredOnDrawerClosedRunnable = null;
                }
                mNavigationDrawerStateListener.onNavDrawerStateChanged(false, false);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mNavigationDrawerStateListener.onNavDrawerStateChanged(true, false);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                mNavigationDrawerStateListener.onNavDrawerStateChanged(isNavDrawerOpen(),
                        newState != DrawerLayout.STATE_IDLE);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mNavigationDrawerStateListener.onNavDrawerSlide(slideOffset);
            }
        });

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        if (!SettingsUtils.isFirstRunProcessComplete(mActivity)) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            SettingsUtils.markFirstRunProcessesDone(mActivity, true);
        }

        setupAccountBox();
    }

    @Override
    public void showNavigation() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    private void createNavDrawerItems(NavigationItemEnum[] items) {
        if (mNavigationView != null) {
            Menu menu = mNavigationView.getMenu();
            for (int i = 0; i < items.length; i++) {
                MenuItem item = menu.findItem(items[i].getId());
                if (item != null) {
                    item.setVisible(true);
                    item.setIcon(items[i].getIconResource());
                    item.setTitle(items[i].getTitleResource());
                } else {
                    LOGE(TAG, "Menu Item for navigation item with title " +
                            (items[i].getTitleResource() != 0 ? mActivity.getResources().getString(
                                    items[i].getTitleResource()) : "") + "not found");
                }
            }

            mNavigationView.setNavigationItemSelectedListener(this);
        }
    }

    /**
     * Sets up the given navdrawer item's appearance to the selected state. Note: this could also be
     * accomplished (perhaps more cleanly) with state-based layouts.
     */
    private void setSelectedNavDrawerItem(NavigationItemEnum item) {
        if (mNavigationView != null && item != NavigationItemEnum.INVALID) {
            mNavigationView.getMenu().findItem(item.getId()).setChecked(true);
        }
    }

    /**
     * Sets up the account box. The account box is the area at the top of the nav drawer that shows
     * which account the user is logged in as, and lets them switch accounts. It also shows the
     * user's Google+ cover photo as background.
     */
    private void setupAccountBox() {

        final View chosenAccountView = mActivity.findViewById(R.id.chosen_account_view);

        if (chosenAccountView == null) {
            //This activity does not have an account box
            return;
        }
    }

    public boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    private void onNavDrawerItemClicked(final NavigationItemEnum item) {
        if (item == mSelfItem) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (isSpecialItem(item)) {
            itemSelected(item);
        } else {
            // Launch the target Activity after a short delay, to allow the close animation to play
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    itemSelected(item);
                }
            }, NAVDRAWER_LAUNCH_DELAY);

            // Change the active item on the list so the user can see the item changed
            setSelectedNavDrawerItem(item);
            // Fade out the main content
            View mainContent = mActivity.findViewById(R.id.main_content);
            if (mainContent != null) {
                mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
            }
        }

        // The navigation menu is accessible from some screens, via swiping, without a drawer
        // layout, eg MapActivity when accessed from SessionDetailsActivity.
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private boolean isSpecialItem(NavigationItemEnum item) {
        return item == NavigationItemEnum.SETTINGS;
    }

    public void markAccountSpinnerAsNotShowingDropDownView() {
        mAccountSpinnerDropDownViewVisible = false;
    }

    public void onSaveInstanceState(Bundle outState) {
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null &&
                savedInstanceState.containsKey(ACCOUNT_SPINNER_DROP_DOWN_VISIBLE)) {

        }
    }

    public interface NavigationDrawerStateListener {

        void onNavDrawerStateChanged(boolean isOpen, boolean isAnimating);

        void onNavDrawerSlide(float slideOffset);
    }
}
