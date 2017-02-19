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

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.net.Uri;

import no.javazone.androidapp.v1.appwidget.ScheduleWidgetProvider;
import no.javazone.androidapp.v1.database.ScheduleContract;

import static no.javazone.androidapp.v1.util.LogUtils.LOGD;
import static no.javazone.androidapp.v1.util.LogUtils.makeLogTag;

/**
 * Helper class for dealing with common actions to take on a session.
 */
public class SessionsHelper {

    private static final String TAG = makeLogTag(SessionsHelper.class);

    private final Activity mActivity;

    public SessionsHelper(Activity activity) {
        mActivity = activity;
    }

    public void setSessionStarred(Uri sessionUri, boolean starred, String title) {
        LOGD(TAG, "setSessionStarred uri=" + sessionUri + " starred=" +
                starred + " title=" + title);
        sessionUri = ScheduleContract.addCallerIsSyncAdapterParameter(sessionUri);
        String sessionId = ScheduleContract.Sessions.getSessionId(sessionUri);
        final ContentValues values = new ContentValues();
        values.put(ScheduleContract.Sessions.SESSION_STARRED, starred?1:0);
        AsyncQueryHandler handler =
                new AsyncQueryHandler(mActivity.getContentResolver()) {
                };
        handler.startUpdate(-1, null, sessionUri, values,null, null);


        // ANALYTICS EVENT: Add or remove a session from the schedule
        // Contains: Session title, whether it was added or removed (starred or unstarred)
        AnalyticsHelper.sendEvent(
                "Session", starred ? "Starred" : "Unstarred", title);

        // Because change listener is set to null during initialization, these
        // won't fire on pageview.
        mActivity.sendBroadcast(ScheduleWidgetProvider.getRefreshBroadcastIntent(mActivity, false));

        // Request an immediate user data sync to reflect the starred user sessions in the cloud
        //SyncHelper.requestManualSync(AccountUtils.getActiveAccount(mActivity), true);
    }
}
