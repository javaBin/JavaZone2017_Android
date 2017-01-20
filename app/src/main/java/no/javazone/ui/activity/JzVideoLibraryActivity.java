package no.javazone.ui.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import no.javazone.R;
import no.javazone.navigation.NavigationModel;
import no.javazone.ui.activity.base.BaseActivity;
import no.javazone.ui.fragment.JzVideoLibraryFragment;
import no.javazone.util.AnalyticsHelper;

public class JzVideoLibraryActivity extends BaseActivity {
    private static final String SCREEN_LABEL = "JavaZone Video Library";
    private Fragment mFragment;
    public static final int ACTIVITY_RESULT = 90;
    public static final String SEARCH_PARAM = "SEARCH_PARAM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.jz_video_library_act);

        // ANALYTICS SCREEN: View the video library screen
        // Contains: Nothing (Page name is a constant)
        AnalyticsHelper.sendScreenView(SCREEN_LABEL);
    }

    @Override
    protected NavigationModel.NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationModel.NavigationItemEnum.VIDEO_LIBRARY;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_RESULT:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();

                    ((JzVideoLibraryFragment)mFragment).doWebViewCall(res.getString(SEARCH_PARAM));
                }
                break;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(((JzVideoLibraryFragment)mFragment).webViewKeyDown(keyCode)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
