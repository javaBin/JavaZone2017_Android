package no.javazone.androidapp.v1.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;

import no.javazone.androidapp.v1.BuildConfig;
import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.database.ScheduleContract;
import no.javazone.androidapp.v1.schedule.JsonHandler;
import no.javazone.androidapp.v1.sync.ConferenceDataHandler;
import no.javazone.androidapp.v1.sync.SyncHelper;
import no.javazone.androidapp.v1.sync.handler.BlocksHandler;
import no.javazone.androidapp.v1.util.LogUtils;
import no.javazone.androidapp.v1.util.SettingsUtils;
import no.javazone.androidapp.v1.util.ToStringConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static no.javazone.androidapp.v1.BuildConfig.SLEEPINGPILL_BASE_ADDRESS_URL;
import static no.javazone.androidapp.v1.util.LogUtils.LOGD;
import static no.javazone.androidapp.v1.util.LogUtils.LOGE;
import static no.javazone.androidapp.v1.util.LogUtils.LOGI;
import static no.javazone.androidapp.v1.util.LogUtils.LOGW;

public class DataBootstrapService extends IntentService {

    private static final String TAG = LogUtils.makeLogTag(DataBootstrapService.class);
    private DataBootstrapApiEndpoint dataBootstrapApiEndpoint = null;

    public static void startDataBootstrapIfNecessary(Context context) {
        if (!SettingsUtils.isDataBootstrapDone(context)) {
            LOGW(TAG, "One-time data bootstrap not done yet. Doing now.");
            context.startService(new Intent(context, DataBootstrapService.class));
        }
    }

    public DataBootstrapService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context appContext = getApplicationContext();

        if (SettingsUtils.isDataBootstrapDone(appContext)) {
            LOGD(TAG, "Data bootstrap already done.");
            return;
        }

            /*
            String bootstrapJson = JsonHandler
                    .parseResource(appContext, R.raw.bootstrap_data); */
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SLEEPINGPILL_BASE_ADDRESS_URL)
                .addConverterFactory(new ToStringConverterFactory())
                .build();

        dataBootstrapApiEndpoint = retrofit.create(DataBootstrapApiEndpoint.class);


        if (BuildConfig.DEBUG) {
            dataBootstrapApiEndpoint.getSessionsDebug().enqueue(retrofitCallBack);
        } else {
            dataBootstrapApiEndpoint.getSessionsRelease().enqueue(retrofitCallBack);
        }
    }

    public Callback<String> retrofitCallBack = new Callback<String>() {
        @Override
        public void onResponse(Call<String> call, Response<String> response) {
            Context appContext = getApplicationContext();
            try {
                LOGD(TAG, "Starting data bootstrap process.");

                ConferenceDataHandler dataHandler = new ConferenceDataHandler(appContext);
                BlocksHandler blockHandler = new BlocksHandler(appContext);
                String blockJson = JsonHandler
                        .parseResource(appContext, R.raw.blocks);
                processDataBody(blockJson, blockHandler);


                dataHandler.applyConferenceData(new String[]{response.body()},
                        BuildConfig.BOOTSTRAP_DATA_TIMESTAMP, false);

                SyncHelper.performPostSyncChores(appContext);

                LOGI(TAG, "End of bootstrap -- successful. Marking bootstrap as done.");
                SettingsUtils.markSyncSucceededNow(appContext);
                SettingsUtils.markDataBootstrapDone(appContext);

                getContentResolver().notifyChange(Uri.parse(ScheduleContract.CONTENT_AUTHORITY),
                        null, false);

            } catch (IOException ex) {
                LOGE(TAG, "*** ERROR DURING BOOTSTRAP! Problem in bootstrap data?", ex);
                LOGE(TAG,
                        "Applying fallback -- marking boostrap as done; sync might fix problem.");
                SettingsUtils.markDataBootstrapDone(appContext);
            } finally {
                SyncHelper.requestManualSync();
            }
        }

        private void processDataBody(String dataBody, JsonHandler handler) throws IOException {
            JsonReader reader = new JsonReader(new StringReader(dataBody));
            JsonParser parser = new JsonParser();
            try {
                reader.setLenient(true);
                reader.beginObject();

                while (reader.hasNext()) {
                    String key = reader.nextName();
                        LOGD(TAG, "Processing key in conference data json: " + key);
                        handler.process(parser.parse(reader));
                }
                reader.endObject();
            } finally {
                reader.close();
            }
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            Context appContext = getApplicationContext();
            LOGE(TAG, "Something failed when retrieving from Sleeping pill", t.getCause());
            LOGE(TAG,
                    "Applying fallback -- marking boostrap as done; sync might fix problem.");
            SettingsUtils.markDataBootstrapDone(appContext);
            SyncHelper.requestManualSync();
        }
    };
}
