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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import no.javazone.androidapp.v1.BuildConfig;
import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.archframework.model.Model;
import no.javazone.androidapp.v1.archframework.view.UserActionEnum;
import no.javazone.androidapp.v1.database.QueryEnum;
import no.javazone.androidapp.v1.debug.DebugActivity;
import no.javazone.androidapp.v1.ui.activity.SettingsActivity;
import no.javazone.androidapp.v1.ui.activity.AboutActivity;
import no.javazone.androidapp.v1.ui.activity.ExploreActivity;
import no.javazone.androidapp.v1.ui.activity.JzVideoLibraryActivity;
import no.javazone.androidapp.v1.ui.activity.MapActivity;
import no.javazone.androidapp.v1.ui.activity.MyScheduleActivity;

import static no.javazone.androidapp.v1.navigation.NavigationConfig.NAVIGATION_ATTENDING;

/**
 * Determines which items to show in the {@link AppNavigationView}.
 */
public class NavigationModel implements Model<NavigationModel.NavigationQueryEnum, NavigationModel.NavigationUserActionEnum> {

    private Context mContext;

    private NavigationItemEnum[] mItems;

    public NavigationModel(Context context) {
        mContext = context;
    }

    public NavigationItemEnum[] getItems() {
        return mItems;
    }

    @Override
    public NavigationQueryEnum[] getQueries() {
        return NavigationQueryEnum.values();
    }

    @Override
    public NavigationUserActionEnum[] getUserActions() {
        return NavigationUserActionEnum.values();
    }

    @Override
    public void deliverUserAction(final NavigationUserActionEnum action,
                                  @Nullable final Bundle args, final UserActionCallback callback) {
        switch (action) {
            case RELOAD_ITEMS:
                mItems = null;
                populateNavigationItems();
                callback.onModelUpdated(this, action);
                break;
        }
    }

    @Override
    public void requestData(final NavigationQueryEnum query,
            final DataQueryCallback callback) {
        switch (query) {
            case LOAD_ITEMS:
                if (mItems != null) {
                    callback.onModelUpdated(this, query);
                } else {
                    populateNavigationItems();
                    callback.onModelUpdated(this, query);
                }
                break;
        }
    }

    private void populateNavigationItems() {
        NavigationItemEnum[] items =  NAVIGATION_ATTENDING;
        boolean debug = BuildConfig.DEBUG;

        if (debug) {
            items = NavigationConfig.appendItem(items, NavigationItemEnum.DEBUG);
        }

        mItems = NavigationConfig.filterOutItemsDisabledInBuildConfig(items);
    }

    @Override
    public void cleanUp() {
        mContext = null;
    }

    /**
     * List of all possible navigation items.
     */
    public enum NavigationItemEnum {
        MY_SCHEDULE(R.id.myschedule_nav_item, R.string.navdrawer_item_my_schedule,
                R.drawable.ic_navview_schedule, MyScheduleActivity.class),
        EXPLORE(R.id.explore_nav_item, R.string.navdrawer_item_explore,
                R.drawable.ic_navview_explore, ExploreActivity.class, true),
        MAP(R.id.map_nav_item, R.string.navdrawer_item_map, R.drawable.ic_navview_map, MapActivity.class),
        VIDEO_LIBRARY(R.id.videos_nav_item, R.string.navdrawer_item_video_library,
                R.drawable.ic_navview_video_library, JzVideoLibraryActivity.class),
        SETTINGS(R.id.settings_nav_item, R.string.navdrawer_item_settings, R.drawable.ic_navview_settings,
                SettingsActivity.class),
        ABOUT(R.id.about_nav_item, R.string.description_about, R.drawable.ic_about,
                AboutActivity.class),
        DEBUG(R.id.debug_nav_item, R.string.navdrawer_item_debug, R.drawable.ic_navview_settings,
                DebugActivity.class),
        INVALID(12, 0, 0, null);

        private int id;

        private int titleResource;

        private int iconResource;

        private Class classToLaunch;

        private boolean finishCurrentActivity;

        NavigationItemEnum(int id, int titleResource, int iconResource, Class classToLaunch) {
            this(id, titleResource, iconResource, classToLaunch, false);
        }

        NavigationItemEnum(int id, int titleResource, int iconResource, Class classToLaunch,
                boolean finishCurrentActivity) {
            this.id = id;
            this.titleResource = titleResource;
            this.iconResource = iconResource;
            this.classToLaunch = classToLaunch;
            this.finishCurrentActivity = finishCurrentActivity;
        }

        public int getId() {
            return id;
        }

        public int getTitleResource() {
            return titleResource;
        }

        public int getIconResource() {
            return iconResource;
        }

        public Class getClassToLaunch() {
            return classToLaunch;
        }

        public boolean finishCurrentActivity() {
            return finishCurrentActivity;
        }

        public static NavigationItemEnum getById(int id) {
            NavigationItemEnum[] values = NavigationItemEnum.values();
            for (int i = 0; i < values.length; i++) {
                if (values[i].getId() == id) {
                return values[i];
                }
            }
            return INVALID;
        }

    }

    public enum NavigationQueryEnum implements QueryEnum {
        LOAD_ITEMS(0);

        private int id;

        NavigationQueryEnum(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String[] getProjection() {
            return new String[0];
        }
    }

    public enum NavigationUserActionEnum implements UserActionEnum {
        RELOAD_ITEMS(0);

        private int id;

        NavigationUserActionEnum(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }
}
