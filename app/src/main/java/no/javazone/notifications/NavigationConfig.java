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

package no.javazone.notifications;

import java.util.ArrayList;
import java.util.List;

import no.javazone.notifications.NavigationModel.NavigationItemEnum;

/**
 * Configuration file for items to show in the {@link AppNavigationView}. This is used by the {@link
 * NavigationModel}.
 */
public class NavigationConfig {

    private final static NavigationModel.NavigationItemEnum[] COMMON_ITEMS_AFTER_CUSTOM =
            new NavigationItemEnum[]{NavigationModel.NavigationItemEnum.VIDEO_LIBRARY,
                    NavigationModel.NavigationItemEnum.SETTINGS, NavigationModel.NavigationItemEnum.ABOUT
            };

    public final static NavigationModel.NavigationItemEnum[] NAVIGATION_ITEMS_LOGGEDIN_ATTENDING =
            concatenateItems(new NavigationItemEnum[]{NavigationItemEnum.MY_SCHEDULE,
                            NavigationItemEnum.IO_LIVE, NavigationItemEnum.EXPLORE,
                            NavigationItemEnum.MAP},
                    COMMON_ITEMS_AFTER_CUSTOM);

    public final static NavigationItemEnum[] NAVIGATION_ITEMS_LOGGEDIN_REMOTE =
            concatenateItems(new NavigationItemEnum[]{NavigationItemEnum.MY_SCHEDULE,
                            NavigationItemEnum.IO_LIVE, NavigationItemEnum.EXPLORE},
                    COMMON_ITEMS_AFTER_CUSTOM);


    public final static NavigationItemEnum[] NAVIGATION_ITEMS_LOGGEDOUT_ATTENDING =
            concatenateItems(new NavigationItemEnum[]{NavigationItemEnum.SIGN_IN,
                    NavigationItemEnum.IO_LIVE, NavigationItemEnum.EXPLORE,
                    NavigationItemEnum.MAP}, COMMON_ITEMS_AFTER_CUSTOM);


    public final static NavigationItemEnum[] NAVIGATION_ITEMS_LOGGEDOUT_REMOTE =
            concatenateItems(new NavigationItemEnum[]{NavigationItemEnum.SIGN_IN,
                            NavigationItemEnum.IO_LIVE, NavigationItemEnum.EXPLORE},
                    COMMON_ITEMS_AFTER_CUSTOM);

    private static NavigationItemEnum[] concatenateItems(NavigationItemEnum[] first,
            NavigationItemEnum[] second) {
        NavigationItemEnum[] items = new NavigationItemEnum[first.length + second.length];
        for (int i = 0; i < first.length; i++) {
            items[i] = first[i];
        }
        for (int i = 0; i < second.length; i++) {
            items[first.length + i] = second[i];
        }
        return items;
    }

    public static NavigationItemEnum[] appendItem(NavigationItemEnum[] first,
            NavigationItemEnum second) {
        return concatenateItems(first, new NavigationItemEnum[]{second});
    }

    public static NavigationItemEnum[] filterOutItemsDisabledInBuildConfig(
            NavigationItemEnum[] items) {
        List<NavigationItemEnum> enabledItems = new ArrayList<NavigationItemEnum>();
        for (int i = 0; i < items.length; i++) {
            boolean includeItem = true;
            switch (items[i]) {
                case MY_SCHEDULE:
                    includeItem = true;
                    break;
                case IO_LIVE:
                    includeItem = false;
                    break;
                case EXPLORE:
                    includeItem = true;
                    break;
                case MAP:
                    includeItem = true;
                    break;
                case VIDEO_LIBRARY:
                    includeItem = true;
                    break;
                case SETTINGS:
                    includeItem = true;
                    break;
                case ABOUT:
                    includeItem = true;
                    break;
                case DEBUG:
                    includeItem = false;
                    break;
            }

            if (includeItem) {
                enabledItems.add(items[i]);
            }
        }
        return enabledItems.toArray(new NavigationItemEnum[enabledItems.size()]);
    }

}
