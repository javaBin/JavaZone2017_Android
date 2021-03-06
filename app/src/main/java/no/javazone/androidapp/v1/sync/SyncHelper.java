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

package no.javazone.androidapp.v1.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.RequestLogger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import no.javazone.androidapp.v1.BuildConfig;
import no.javazone.androidapp.v1.Config;
import no.javazone.androidapp.v1.database.ScheduleContract;
import no.javazone.androidapp.v1.archframework.model.domain.Account;
import no.javazone.androidapp.v1.service.DataBootstrapService;
import no.javazone.androidapp.v1.util.SettingsUtils;
import no.javazone.androidapp.v1.util.TimeUtils;

import static no.javazone.androidapp.v1.util.LogUtils.LOGD;
import static no.javazone.androidapp.v1.util.LogUtils.LOGE;
import static no.javazone.androidapp.v1.util.LogUtils.LOGI;
import static no.javazone.androidapp.v1.util.LogUtils.LOGW;
import static no.javazone.androidapp.v1.util.LogUtils.makeLogTag;

/**
 * A helper class for dealing with conference data synchronization. All operations occur on the
 * thread they're called from, so it's best to wrap calls in an {@link android.os.AsyncTask}, or
 * better yet, a {@link android.app.Service}.
 */
public class SyncHelper {

    private static final String TAG = makeLogTag(SyncHelper.class);

    private Context mContext;

    private ConferenceDataHandler mConferenceDataHandler;

    private BasicHttpClient mHttpClient;

    /**
     *
     * @param context Can be Application, Activity or Service context.
     */
    public SyncHelper(Context context) {
        mContext = context;
        mConferenceDataHandler = new ConferenceDataHandler(mContext);
        mHttpClient = new BasicHttpClient();
        if (!BuildConfig.DEBUG) {
            mHttpClient.setRequestLogger(new MinimalRequestLogger());
        }
    }

    public static void requestManualSync() {
        requestManualSync(false);
    }

    public static void requestManualSync(boolean userDataSyncOnly) {
        LOGD(TAG, "Requesting manual sync for account. userDataSyncOnly=" + userDataSyncOnly);
        android.accounts.Account account = Account.getAccount();
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        if (userDataSyncOnly) {
            b.putBoolean(SyncAdapter.EXTRA_SYNC_USER_DATA_ONLY, true);
        }
        ContentResolver
                .setSyncAutomatically(account, ScheduleContract.CONTENT_AUTHORITY, true);
        ContentResolver.setIsSyncable(account, ScheduleContract.CONTENT_AUTHORITY, 1);

        boolean pending = ContentResolver.isSyncPending(account, ScheduleContract.CONTENT_AUTHORITY);
        if (pending) {
            LOGD(TAG, "Warning: sync is PENDING. Will cancel.");
        }
        boolean active = ContentResolver.isSyncActive(account, ScheduleContract.CONTENT_AUTHORITY);
        if (active) {
            LOGD(TAG, "Warning: sync is ACTIVE. Will cancel.");
        }

        if (pending || active) {
            LOGD(TAG, "Cancelling previously pending/active sync.");
            ContentResolver.cancelSync(account, ScheduleContract.CONTENT_AUTHORITY);
        }

        LOGD(TAG, "Requesting sync now.");
        ContentResolver.requestSync(account, ScheduleContract.CONTENT_AUTHORITY, b);
    }

    /**
     * Attempts to perform data synchronization. There are 3 types of data: conference, user
     * schedule and user feedback.
     * <p />
     *
     * @param syncResult The sync result object to update with statistics.
     * @param extras Specifies additional information about the sync. This must contain key
     *               {@code SyncAdapter.EXTRA_SYNC_USER_DATA_ONLY} with boolean value
     * @return true if the sync changed the data.
     */
    public boolean performSync(@Nullable SyncResult syncResult, Bundle extras) {
        android.accounts.Account account = Account.getAccount();

        boolean dataChanged = false;

        if (!SettingsUtils.isDataBootstrapDone(mContext)) {
            LOGD(TAG, "Sync aborting (data bootstrap not done yet)");
            // Start the bootstrap process so that the next time sync is called,
            // it is already bootstrapped.
            DataBootstrapService.startDataBootstrapIfNecessary(mContext);
            return false;
        }

        final boolean userDataScheduleOnly = extras
                .getBoolean(SyncAdapter.EXTRA_SYNC_USER_DATA_ONLY, false);

        LOGI(TAG, "Performing sync for account: " + account);
        SettingsUtils.markSyncAttemptedNow(mContext);
        long opStart;
        long syncDuration, choresDuration;

        opStart = System.currentTimeMillis();
        syncDuration = System.currentTimeMillis() - opStart;

        // If data has changed, there are a few chores we have to do.
        opStart = System.currentTimeMillis();
        if (dataChanged) {
            try {
                performPostSyncChores(mContext);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                LOGE(TAG, "Error performing post sync chores.");
            }
        }
        choresDuration = System.currentTimeMillis() - opStart;

        int operations = mConferenceDataHandler.getContentProviderOperationsDone();
        if (syncResult != null && syncResult.stats != null) {
            syncResult.stats.numEntries += operations;
            syncResult.stats.numUpdates += operations;
        }

        LOGI(TAG, "End of sync (" + (dataChanged ? "data changed" : "no data change") + ")");

        updateSyncInterval(mContext);

        return dataChanged;
    }

    public static void performPostSyncChores(final Context context) {
        // Update search index.
        LOGD(TAG, "Updating search index.");
        context.getContentResolver().update(ScheduleContract.SearchIndex.CONTENT_URI,
                new ContentValues(), null, null);
    }


    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void increaseIoExceptions(SyncResult syncResult) {
        if (syncResult != null && syncResult.stats != null) {
            ++syncResult.stats.numIoExceptions;
        }
    }

    public static class AuthException extends RuntimeException {

    }

   private static long calculateRecommendedSyncInterval(final Context context) {
        long now = TimeUtils.getCurrentTime(context);
        long aroundConferenceStart = Config.CONFERENCE_START_MILLIS
                - Config.AUTO_SYNC_AROUND_CONFERENCE_THRESH;
        if (now < aroundConferenceStart) {
            return Config.AUTO_SYNC_INTERVAL_LONG_BEFORE_CONFERENCE;
        } else if (now <= Config.CONFERENCE_END_MILLIS) {
            return Config.AUTO_SYNC_INTERVAL_AROUND_CONFERENCE;
        } else {
            return Config.AUTO_SYNC_INTERVAL_AFTER_CONFERENCE;
        }
    }

    public static void updateSyncInterval(final Context context) {
        android.accounts.Account account = Account.getAccount();
        LOGD(TAG, "Checking sync interval");
        long recommended = calculateRecommendedSyncInterval(context);
        long current = SettingsUtils.getCurSyncInterval(context);
        LOGD(TAG, "Recommended sync interval " + recommended + ", current " + current);
        if (recommended != current) {
            LOGD(TAG,
                    "Setting up sync for account, interval " + recommended + "ms");
            ContentResolver.setIsSyncable(account, ScheduleContract.CONTENT_AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, ScheduleContract.CONTENT_AUTHORITY, true);
            if (recommended <= 0L) { // Disable periodic sync.
                ContentResolver.removePeriodicSync(account, ScheduleContract.CONTENT_AUTHORITY,
                        new Bundle());
            } else {
                ContentResolver.addPeriodicSync(account, ScheduleContract.CONTENT_AUTHORITY,
                        new Bundle(), recommended / 1000L);
            }
            SettingsUtils.setCurSyncInterval(context, recommended);
        } else {
            LOGD(TAG, "No need to update sync interval.");
        }
    }

    public static class MinimalRequestLogger implements RequestLogger {

        @Override
        public boolean isLoggingEnabled() {
            return true;
        }

        @Override
        public void log(final String s) { }

        @Override
        public void logRequest(final HttpURLConnection urlConnection, final Object o)
                throws IOException {
            try {
                URL url = urlConnection.getURL();
                LOGW(TAG, "HTTPRequest to " + url.getHost());
            } catch (Throwable e) {
                LOGI(TAG, "Exception while logging http request.");
            }

        }

        @Override
        public void logResponse(final HttpResponse httpResponse) {
            try {
                URL url = new URL(httpResponse.getUrl());
                LOGW(TAG, "HTTPResponse from " + url.getHost() + " had return status " + httpResponse.getStatus());
            } catch (Throwable e) {
                LOGI(TAG, "Exception while logging http response.");
            }
        }
    }
}
