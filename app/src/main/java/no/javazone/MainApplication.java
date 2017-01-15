package no.javazone;

import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.firebase.client.Firebase;
import com.google.android.gms.security.ProviderInstaller;

import no.javazone.settings.SettingsUtils;
import no.javazone.util.AnalyticsHelper;
import no.javazone.util.TimeUtils;

import static no.javazone.util.LogUtils.LOGE;
import static no.javazone.util.LogUtils.LOGW;
import static no.javazone.util.LogUtils.makeLogTag;

public class MainApplication extends MultiDexApplication {

    private static final String TAG = makeLogTag(MainApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();
        TimeUtils.setAppStartTime(getApplicationContext(), System.currentTimeMillis());

        // Initialize the Firebase library with an Android context.
        Firebase.setAndroidContext(this);

        AnalyticsHelper.prepareAnalytics(getApplicationContext());
        SettingsUtils.markDeclinedWifiSetup(getApplicationContext(), false);

        // Ensure an updated security provider is installed into the system when a new one is
        // available via Google Play services.
        try {
            ProviderInstaller.installIfNeededAsync(getApplicationContext(),
                    new ProviderInstaller.ProviderInstallListener() {
                        @Override
                        public void onProviderInstalled() {
                            LOGW(TAG, "New security provider installed.");
                        }

                        @Override
                        public void onProviderInstallFailed(int errorCode, Intent intent) {
                            LOGE(TAG, "New security provider install failed.");
                            // No notification shown there is no user intervention needed.
                        }
                    });
        } catch (Exception ignorable) {
            LOGE(TAG, "Unknown issue trying to install a new security provider.", ignorable);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
