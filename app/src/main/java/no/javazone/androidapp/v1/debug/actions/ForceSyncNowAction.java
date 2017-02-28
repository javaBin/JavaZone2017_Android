package no.javazone.androidapp.v1.debug.actions;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.AsyncTask;
import android.os.Bundle;

import no.javazone.androidapp.v1.debug.DebugAction;
import no.javazone.androidapp.v1.sync.ConferenceDataHandler;
import no.javazone.androidapp.v1.sync.SyncHelper;
import no.javazone.androidapp.v1.util.AccountUtils;

public class ForceSyncNowAction implements DebugAction {
    @Override
    public void run(final Context context, final Callback callback) {
        ConferenceDataHandler.resetDataTimestamp(context);
        final Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        new AsyncTask<Context, Void, Void>() {
            @Override
            protected Void doInBackground(Context... contexts) {
                Account account = AccountUtils.getActiveAccount(context);
                if (account == null) {
                    callback.done(false, "Cannot sync if there is no active account.");
                } else {
                    new SyncHelper(contexts[0]).performSync(new SyncResult(), bundle);
                }
                return null;
            }
        }.execute(context);
    }

    @Override
    public String getLabel() {
        return "Force data sync now";
    }

}