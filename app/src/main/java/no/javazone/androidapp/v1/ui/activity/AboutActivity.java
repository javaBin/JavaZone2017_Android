package no.javazone.androidapp.v1.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import no.javazone.androidapp.v1.BuildConfig;
import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.navigation.NavigationModel;
import no.javazone.androidapp.v1.ui.activity.base.BaseActivity;
import no.javazone.androidapp.v1.ui.widget.DrawShadowFrameLayout;
import no.javazone.androidapp.v1.util.AboutUtils;
import no.javazone.androidapp.v1.util.UIUtils;

public class AboutActivity extends BaseActivity {
    private View rootView;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.about_licenses:
                    AboutUtils.showOpenSourceLicenses(AboutActivity.this);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        rootView = findViewById(R.id.about_container);

        TextView body = (TextView) rootView.findViewById(R.id.about_main);
        body.setText(Html.fromHtml(getString(R.string.about_main, BuildConfig.VERSION_NAME)));
        rootView.findViewById(R.id.about_licenses).setOnClickListener(mOnClickListener);

        overridePendingTransition(0, 0);
    }


    @Override
    protected NavigationModel.NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationModel.NavigationItemEnum.ABOUT;
    }

    private void setContentTopClearance(int clearance) {
        if (rootView != null) {
            rootView.setPadding(rootView.getPaddingLeft(), clearance,
                    rootView.getPaddingRight(), rootView.getPaddingBottom());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int actionBarSize = UIUtils.calculateActionBarSize(this);
        DrawShadowFrameLayout drawShadowFrameLayout =
                (DrawShadowFrameLayout) findViewById(R.id.main_content);
        if (drawShadowFrameLayout != null) {
            drawShadowFrameLayout.setShadowTopOffset(actionBarSize);
        }
        setContentTopClearance(actionBarSize);
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

}
