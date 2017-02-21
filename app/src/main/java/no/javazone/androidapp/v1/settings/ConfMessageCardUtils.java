/*
 * Copyright 2015 Google Inc. All rights reserved.
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

package no.javazone.androidapp.v1.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.List;

import no.javazone.androidapp.v1.BuildConfig;
import no.javazone.androidapp.v1.util.SettingsUtils;
import no.javazone.androidapp.v1.util.TimeUtils;

import static no.javazone.androidapp.v1.util.LogUtils.LOGE;
import static no.javazone.androidapp.v1.util.LogUtils.makeLogTag;

/**
 * Utilities and constants to deal with enabling & showing conference message cards.
 */
public class ConfMessageCardUtils {

    public static final String TAG = makeLogTag(ConfMessageCardUtils.class);

    /**
     * Boolean preference indicating whether to show conference info cards in Explore stream.
     */
    public static final String PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT
            = "pref_answered_conf_message_cards_prompt_" +
            SettingsUtils.CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Enum holding all the different kinds of Conference Message Cards that can appear in Explore.
     * For use with hasDismissedConfMessageCard and markDismissedConfMessageCard.
     */
    public enum ConfMessageCard {
        /**
         * Card asking users to opt-in to session notifications
         */
        SESSION_NOTIFICATIONS("session_notifications", "2016-04-01T00:00:00-07:00", "2016-05-23T00:00:00-07:00", null),

        /**
         * Card allowing developers to provide feedback for the wifi onsite.
         */
        // The app went out in R1 with "wifi_feedback" property for the WiFi preloading feature so we need to set it back only after I/O.
        WIFI_PRELOAD("wifi_feedback", "2017-09-13T09:00:00-07:00", "2017-09-15T16:00:00-07:00", null),

        // The demo card's start and end time are never active because debug builds ignore these times, see below.
        DEMO_MODE("demo_mode",                   "2015-01-01T01:00:00-07:00", "2015-01-01T01:00:00-07:00", "<b>Demo mode</b><br/>You're running a debug build so the conference cards are always active."),
        // The following cards aren't internationalized, but are only shown to onsite attendees.
        KEYNOTE("keynote",                       "2017-09-13T09:45:00-07:00", "2017-09-13T10:00:00-07:00", "<b>Keynote</b><br/>Welcome to Google I/O 2016! The Keynote will start in 15 minutes. Please join us in the Amphitheatre, you won't want to miss this!");

        long mStartTime;
        long mEndTime;
        String mSimpleMessage;
        String mKey;
        ConfMessageCard(String key, String startTime, String endTime, String simpleMessage) {
            mKey = key;
            try {
                mStartTime = TimeUtils.parseTimestamp(startTime).getTime();
            } catch (NullPointerException npe) {
                LOGE(TAG, "Invalid time for key = " + key + " and time = " + startTime);
                mStartTime = Long.MIN_VALUE;
            }
            try {
                mEndTime = TimeUtils.parseTimestamp(endTime).getTime();
            } catch (NullPointerException npe) {
                LOGE(TAG, "Invalid time for key = " + key + " and time = " + endTime);
                mEndTime = Long.MIN_VALUE;
            }
            mSimpleMessage = simpleMessage;

            // Debug builds show all conference cards.
            if (BuildConfig.DEBUG) {
                mStartTime = 0;
                mEndTime = Long.MAX_VALUE;
            }
        }

        public String getDismissedPreferenceKey() {
            return dismiss_prefix + mKey;
        }

        public String getShouldShowPreferenceKey() {
            return should_show_prefix + mKey;
        }

        /**
         * Identify if this card is a simple message card.
         */
        public boolean isSimpleMessageCard() {
            return !TextUtils.isEmpty(mSimpleMessage);
        }

        /**
         * Return a message if this is a simplified conference message card that is meant to only
         * display a message.
         */
        public String getSimpleMessage() {
            return mSimpleMessage;
        }

        public boolean isTimeActive(long millisSinceEpoch) {
            return mStartTime <= millisSinceEpoch && mEndTime >= millisSinceEpoch;
        }

        public static List<ConfMessageCard> getActiveSimpleCards(Context context) {
            ArrayList<ConfMessageCard> activeSimpleCards = new ArrayList<>();
            for (ConfMessageCard card : ConfMessageCard.values()) {
                if (card.isSimpleMessageCard() && card.isTimeActive(TimeUtils.getCurrentTime(context))
                        && !hasDismissedConfMessageCard(context, card)) {
                    activeSimpleCards.add(card);
                }
            }
            return activeSimpleCards;
        }
    }

    private static final String dismiss_prefix = "pref_conf_msg_cards_" +
            SettingsUtils.CONFERENCE_YEAR_PREF_POSTFIX + "_dismissed_";
    private static final String should_show_prefix = "pref_conf_msg_cards_ " +
            SettingsUtils.CONFERENCE_YEAR_PREF_POSTFIX + "_should_show_";

    /**
     * Return true if conference info cards are enabled, false if user has disabled them.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isConfMessageCardsEnabled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(BuildConfig.PREF_CONF_MESSAGES_ENABLED, false);
    }

    /**
     * Set a new value for the conference message cards enabled preference.
     *
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value to be set, setting this to null results in un-setting the value.
     */
    public static void setConfMessageCardsEnabled(final Context context,
                                                  @Nullable Boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (newValue == null) {
            sp.edit().remove(BuildConfig.PREF_CONF_MESSAGES_ENABLED).apply();
        } else {
            sp.edit().putBoolean(BuildConfig.PREF_CONF_MESSAGES_ENABLED, newValue).apply();
        }
    }

    /**
     * Returns true if user already answered the conference info cards prompt, false if they
     * haven't yet.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean hasAnsweredConfMessageCardsPrompt(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT, false);
    }

    /**
     * Mark that the user has answered the conference info cards prompt so app doesn't bother
     * them again.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value to be set, setting this to null results in un-setting the value.
     */
    public static void markAnsweredConfMessageCardsPrompt(final Context context,
                                                          @Nullable Boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (newValue == null) {
            sp.edit().remove(PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT).apply();
        } else {
            sp.edit().putBoolean(PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT, newValue).apply();
        }
    }

    /**
     * Returns true if user has already seen the passed-in message card, false if they haven't yet.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param card    One of the ConfMessageCard enum values
     */
    public static boolean hasDismissedConfMessageCard(final Context context, ConfMessageCard card) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(card.getDismissedPreferenceKey(), false);
    }

    /**
     * Mark that the user has dismissed one of the conference message cards so the app doesn't
     * show that card to them again.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param card    One of the ConfMessageCard enum values
     */
    public static void markDismissedConfMessageCard(final Context context, ConfMessageCard card) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(card.getDismissedPreferenceKey(), true).apply();
    }

    /**
     * Set the dismissal state of one of the conference message cards.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param card    One of the ConfMessageCard enum values
     * @param newValue   True, False, or null to unset.
     */
    public static void setDismissedConfMessageCard(final Context context, ConfMessageCard card,
                                                   Boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (newValue == null) {
            sp.edit().remove(card.getDismissedPreferenceKey()).apply();
        } else {
            sp.edit().putBoolean(card.getDismissedPreferenceKey(), newValue).apply();
        }
    }

    /**
     * Indicates whether the app should show the specified message card in the card feed.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param card    One of the ConfMessageCard enum values
     */
    public static boolean shouldShowConfMessageCard(final Context context, ConfMessageCard card) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(card.getShouldShowPreferenceKey(), true);
    }

    /**
     * Updates the "should show" state of the specified message card. By default, they should not
     * show, but based on external signals, they can be set to be shown.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     * @param card    One of the ConfMessageCard enum values
     * @param newValue Value to set or null to unset.
     */
    public static void markShouldShowConfMessageCard(final Context context, ConfMessageCard card,
                                                     Boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        if (newValue == null) {
            sp.edit().remove(card.getShouldShowPreferenceKey()).apply();
        } else {
            sp.edit().putBoolean(card.getShouldShowPreferenceKey(), newValue).apply();
        }
    }

    /**
     * Register a {@code listener} which is notified when these settings are changed.
     *
     * @param context A context that has the same lifecycle as the listener that will be returned.
     * @param listener Listener to register.
     */
    public static void registerPreferencesChangeListener(final Context context,
            ConferencePrefChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Unregister a {@code listener} which is notified when these settings are changed.
     *
     * @param context A context that has the same lifecycle as the listener that will be returned.
     * @param listener Listener to unregister.
     */
    public static void unregisterPreferencesChangeListener(final Context context,
            ConferencePrefChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Unset the dismissal state of all conference message cards.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static void unsetStateForAllCards(final Context context) {
        for (ConfMessageCard card : ConfMessageCard.values()) {
            setDismissedConfMessageCard(context, card, null /* new state */);
        }
    }

    /**
     * Mark appropriate cards active.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static void enableActiveCards(final Context context) {
        long currentTime = TimeUtils.getCurrentTime(context);
        for (ConfMessageCard card : ConfMessageCard.values()) {
            if (card.isTimeActive(currentTime)) {
                markShouldShowConfMessageCard(context, card, true);
            }
        }
    }

    public static boolean isConfMessageKey(@NonNull String key) {
        return key.startsWith(dismiss_prefix) || key.startsWith(should_show_prefix);
    }

    /**
     * Class that listens for {@link ConfMessageCardUtils} specific preferences and calls
     * onPrefChanged with the specific key and value.
     */
    public static class ConferencePrefChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            if (PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT.equals(key)) {
                onPrefChanged(PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT,
                        sp.getBoolean(PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT, true));
            } else if (BuildConfig.PREF_CONF_MESSAGES_ENABLED.equals(key)) {
                onPrefChanged(BuildConfig.PREF_CONF_MESSAGES_ENABLED,
                        sp.getBoolean(BuildConfig.PREF_CONF_MESSAGES_ENABLED, false));
            } else if (isConfMessageKey(key)) {
                onPrefChanged(key, sp.getBoolean(key, false));
            }
        }

        protected void onPrefChanged(String key, boolean value) {
        }
    }
}
