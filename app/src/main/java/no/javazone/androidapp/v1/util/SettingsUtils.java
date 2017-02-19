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

package no.javazone.androidapp.v1.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


import java.util.TimeZone;

import no.javazone.androidapp.v1.Config;

import static no.javazone.androidapp.v1.util.LogUtils.makeLogTag;

public class SettingsUtils {

    private static final String TAG = makeLogTag(SettingsUtils.class);

    public static final String CONFERENCE_YEAR_PREF_POSTFIX = "_2017";

    public static final String PREF_LOCAL_TIMES = "pref_local_times";

    public static final String PREF_ATTENDEE_AT_VENUE = "pref_attendee_at_venue" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    public static final String PREF_DATA_BOOTSTRAP_DONE = "pref_data_bootstrap_done";

    public static final String PREF_USER_REFUSED_SIGN_IN = "pref_user_refused_sign_in" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    public static final String PREF_DEBUG_BUILD_WARNING_SHOWN = "pref_debug_build_warning_shown";

    public static final String PREF_TOS_ACCEPTED = "pref_tos_accepted" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    private static final String PREF_CONDUCT_ACCEPTED = "pref_conduct_accepted" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    public static final String PREF_DECLINED_WIFI_SETUP = "pref_declined_wifi_setup" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    public static final String PREF_ANSWERED_LOCAL_OR_REMOTE = "pref_answered_local_or_remote" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    public static final String PREF_LAST_SYNC_ATTEMPTED = "pref_last_sync_attempted";

    public static final String PREF_LAST_SYNC_SUCCEEDED = "pref_last_sync_succeeded";

    public static final String PREF_CUR_SYNC_INTERVAL = "pref_cur_sync_interval";

    public static final String PREF_SYNC_CALENDAR = "pref_sync_calendar";

    public static final String PREF_FIRST_START = "pref_first_start" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    public static final String PREF_ANALYTICS_ENABLED = "pref_analytics_enabled";

    public static final String PREF_SHOW_SESSION_REMINDERS = "pref_show_session_reminders" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    public static final String PREF_SHOW_SESSION_FEEDBACK_REMINDERS =
            "pref_show_session_feedback_reminders" + CONFERENCE_YEAR_PREF_POSTFIX;

    public static TimeZone getDisplayTimeZone(Context context) {
        TimeZone defaultTz = TimeZone.getDefault();
        return (isUsingLocalTime(context) && defaultTz != null)
                ? defaultTz : Config.CONFERENCE_TIMEZONE;
    }

    public static boolean isUsingLocalTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_LOCAL_TIMES, false);
    }

    public static boolean isAttendeeAtVenue(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ATTENDEE_AT_VENUE, true);
    }


    public static void markDataBootstrapDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DATA_BOOTSTRAP_DONE, true).apply();
    }

    public static boolean isDataBootstrapDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DATA_BOOTSTRAP_DONE, false);
    }

    public static boolean wasDebugWarningShown(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DEBUG_BUILD_WARNING_SHOWN, false);
    }

    public static void markDebugWarningShown(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DEBUG_BUILD_WARNING_SHOWN, true).apply();
    }

    /**
     * Return true if user has accepted the Code of
     * {@link com.google.samples.apps.iosched.welcome.ConductFragment Conduct}, false if they haven't (yet).
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isConductAccepted(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_CONDUCT_ACCEPTED, false);
    }

    /**
     * Mark {@code newValue whether} the user has accepted the TOS so the app doesn't ask again.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markTosAccepted(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_TOS_ACCEPTED, newValue).apply();
    }

    /**
     * Mark {@code newValue whether} the user has accepted the Code of Conduct so the app doesn't ask again.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markConductAccepted(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_CONDUCT_ACCEPTED, newValue).apply();
    }

    public static boolean hasDeclinedWifiSetup(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DECLINED_WIFI_SETUP, false);
    }

    public static void markDeclinedWifiSetup(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DECLINED_WIFI_SETUP, newValue).apply();
    }

    public static boolean hasAnsweredLocalOrRemote(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ANSWERED_LOCAL_OR_REMOTE, false);
    }

    public static void markAnsweredLocalOrRemote(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_ANSWERED_LOCAL_OR_REMOTE, newValue).apply();
    }

    /**
     * Return true if the first-app-run-activities have already been executed.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isFirstRunProcessComplete(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_FIRST_START, false);
    }

    public static void markFirstRunProcessesDone(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_FIRST_START, newValue).apply();
    }

    /**
     * Return a long representing the last time a sync was attempted (regardless of success).
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static long getLastSyncAttemptedTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_ATTEMPTED, 0L);
    }

    /**
     * Mark a sync was attempted (stores current time as 'last sync attempted' preference).
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void markSyncAttemptedNow(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_LAST_SYNC_ATTEMPTED, UIUtils.getCurrentTime(context)).apply();
    }

    /**
     * Return a long representing the last time a sync succeeded.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static long getLastSyncSucceededTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_SUCCEEDED, 0L);
    }

    /**
     * Mark that a sync succeeded (stores current time as 'last sync succeeded' preference).
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void markSyncSucceededNow(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_LAST_SYNC_SUCCEEDED, UIUtils.getCurrentTime(context)).apply();
    }

    /**
     * Return true if analytics are enabled, false if user has disabled them.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isAnalyticsEnabled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ANALYTICS_ENABLED, true);
    }

    /**
     * Return true if session reminders are enabled, false if user has disabled them.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean shouldShowSessionReminders(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SHOW_SESSION_REMINDERS, true);
    }

    /**
     * Return true if session feedback reminders are enabled, false if user has disabled them.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean shouldShowSessionFeedbackReminders(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SHOW_SESSION_FEEDBACK_REMINDERS, true);
    }

    /**
     * Return a long representing the current data sync interval time.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static long getCurSyncInterval(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_CUR_SYNC_INTERVAL, 0L);
    }

    /**
     * Set a new interval for the data sync time.
     *
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void setCurSyncInterval(final Context context, long newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_CUR_SYNC_INTERVAL, newValue).apply();
    }

    /**
     * Return true if calendar sync is enabled, false if disabled.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean shouldSyncCalendar(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SYNC_CALENDAR, false);
    }

    /**
     * Helper method to register a settings_prefs listener. This method does not automatically handle
     * {@code unregisterOnSharedPreferenceChangeListener() un-registering} the listener at the end
     * of the {@code context} lifecycle.
     *
     * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param listener Listener to register.
     */
    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Helper method to un-register a settings_prefs listener typically registered with
     * {@code registerOnSharedPreferenceChangeListener()}
     *
     * @param context  Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param listener Listener to un-register.
     */
    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
