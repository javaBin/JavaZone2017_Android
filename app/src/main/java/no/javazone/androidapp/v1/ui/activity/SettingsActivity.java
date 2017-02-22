/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.javazone.androidapp.v1.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import no.javazone.androidapp.v1.BuildConfig;
import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.navigation.NavigationModel;
import no.javazone.androidapp.v1.ui.activity.base.BaseActivity;
import no.javazone.androidapp.v1.ui.widget.DrawShadowFrameLayout;
import no.javazone.androidapp.v1.util.SettingsUtils;
import no.javazone.androidapp.v1.util.UIUtils;

/**
 * Activity for customizing app settings.
 */
public class SettingsActivity extends BaseActivity {
    @Override
    protected NavigationModel.NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationModel.NavigationItemEnum.SETTINGS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_act);
        overridePendingTransition(0, 0);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            SettingsUtils.registerOnSharedPreferenceChangeListener(getActivity(), this);
        }

        @Override
        public RecyclerView onCreateRecyclerView(final LayoutInflater inflater,
                                                 final ViewGroup parent,
                                                 final Bundle savedInstanceState) {
            // Override the default list which has horizontal padding. Instead place padding on
            // the preference items for nicer touch feedback.
            final RecyclerView prefList =
                    (RecyclerView) inflater.inflate(R.layout.settings_list, parent, false);
            prefList.setHasFixedSize(true);
            return prefList;
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.settings_prefs);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            SettingsUtils.unregisterOnSharedPreferenceChangeListener(getActivity(), this);
        }

        @Override
        public void onResume() {
            super.onResume();

            // configure the fragment's top clearance to take our overlaid controls (Action Bar
            // and spinner box) into account.
            int actionBarSize = UIUtils.calculateActionBarSize(getActivity());
            DrawShadowFrameLayout drawShadowFrameLayout =
                    (DrawShadowFrameLayout) getActivity().findViewById(R.id.main_content);
            if (drawShadowFrameLayout != null) {
                drawShadowFrameLayout.setShadowTopOffset(actionBarSize);
            }
            setContentTopClearance(actionBarSize);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        }

        private void setContentTopClearance(int clearance) {
            if (getView() != null) {
                getView().setPadding(getView().getPaddingLeft(), clearance,
                        getView().getPaddingRight(), getView().getPaddingBottom());
            }
        }
    }
}
