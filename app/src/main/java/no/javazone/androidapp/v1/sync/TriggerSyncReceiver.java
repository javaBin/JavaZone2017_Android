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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import no.javazone.androidapp.v1.util.AccountUtils;

/**
 * A simple {@link BroadcastReceiver} that triggers a sync. This is used by the GCM code to trigger
 * jittered syncs using {@link android.app.AlarmManager}.
 */
public class TriggerSyncReceiver extends BroadcastReceiver {
    public static final String EXTRA_USER_DATA_SYNC_ONLY = "no.javazone.androidapp.v1.EXTRA_USER_DATA_SYNC_ONLY";

    @Override
    public void onReceive(Context context, Intent intent) {
        String accountName = AccountUtils.getActiveAccountName(context);
        if (TextUtils.isEmpty(accountName)) {
            return;
        }
            if (intent.getBooleanExtra(EXTRA_USER_DATA_SYNC_ONLY, false) ) {
                // this is a request to sync user data only, so do a manual sync right now
                // with the userDataOnly == true.
                SyncHelper.requestManualSync(true);
            }
    }
}
