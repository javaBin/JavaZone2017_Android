package no.javazone.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import no.javazone.R;
import no.javazone.database.ScheduleContract;
import no.javazone.ui.activity.base.BaseActivity;
import no.javazone.util.BeamUtils;
import no.javazone.util.LogUtils;
import no.javazone.util.UIUtils;

import static no.javazone.util.LogUtils.LOGE;

public class SessionDetailActivity extends BaseActivity {
    private static final String TAG = LogUtils.makeLogTag(SessionDetailActivity.class);

    private Handler mHandler = new Handler();

    private Uri mSessionUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UIUtils.tryTranslateHttpIntent(this);
        BeamUtils.tryUpdateIntentFromBeam(this);
        boolean shouldBeFloatingWindow = shouldBeFloatingWindow();
        if (shouldBeFloatingWindow) {
            setupFloatingWindow(R.dimen.session_details_floating_width,
                    R.dimen.session_details_floating_height, 1, 0.4f);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_detail_act);

        setToolbarAsUp(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.finishAfterTransition(SessionDetailActivity.this);
            }
        });
        final Toolbar toolbar = getToolbar();
        // Override the icon if shouldBeFloatingWindow
        if (shouldBeFloatingWindow) {
            toolbar.setNavigationIcon(R.drawable.ic_close);
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Do not display the Activity name in the toolbar
                toolbar.setTitle("");
            }
        });

        if (savedInstanceState == null) {
            Uri sessionUri = getIntent().getData();
            BeamUtils.setBeamSessionUri(this, sessionUri);
        }

        mSessionUri = getIntent().getData();

        if (mSessionUri == null) {
            LOGE(TAG, "SessionDetailActivity started with null session Uri!");
            finish();
            return;
        }
    }

    public Uri getSessionUri() {
        return mSessionUri;
    }

    @Override
    public Intent getParentActivityIntent() {
        return new Intent(this, MyScheduleActivity.class);
    }

    public static void startSessionDetailActivity(final Activity activity,
                                                  final String sessionId) {
        Uri data = ScheduleContract.Sessions.buildSessionUri
                (sessionId);
        Intent sessionDetailIntent = new Intent(activity,
                SessionDetailActivity.class);
        sessionDetailIntent.setData(data);
        activity.startActivity(sessionDetailIntent);
    }
}
