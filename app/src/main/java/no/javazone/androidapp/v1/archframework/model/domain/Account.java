package no.javazone.androidapp.v1.archframework.model.domain;

import android.app.Activity;

import static android.content.Context.ACCOUNT_SERVICE;
import static no.javazone.androidapp.v1.util.LogUtils.LOGE;
import static no.javazone.androidapp.v1.util.LogUtils.makeLogTag;

public class Account {
    public static final String ACCOUNT_TYPE = "no.javazone.androidapp.v1";
    public static final String ACCOUNT_NAME = "no.javazone.androidapp.v1";


    private static final String TAG = makeLogTag(Account.class);
    private static android.accounts.Account mAccount;

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param activity The application context
     */
    public static android.accounts.Account createSyncAccount(Activity activity) {
        android.accounts.AccountManager accountManager =
                (android.accounts.AccountManager) activity.getSystemService(
                        ACCOUNT_SERVICE);

        // Register account with system
        android.accounts.Account account = getAccount();
        if (accountManager.addAccountExplicitly(account, null, null)) {
            return account;
        } else {
            LOGE(TAG, "Unable to create account");
            return null;
        }
    }

    /** Get the account object for this application.
     *
     * <p>Note that, since this is just used for sync adapter purposes, this object will always
     * be the same.
     *
     * @return account
     */
    public static android.accounts.Account getAccount() {
        if (mAccount == null) {
            mAccount = new android.accounts.Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        }
        return mAccount;
    }

}