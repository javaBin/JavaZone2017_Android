package no.javazone.androidapp.v1.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.crash.FirebaseCrash;

import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.navigation.NavigationModel;
import no.javazone.androidapp.v1.ui.activity.base.BaseActivity;
import no.javazone.androidapp.v1.util.AnalyticsHelper;

import static no.javazone.androidapp.v1.util.LogUtils.makeLogTag;

public class ExploreActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {
    private static final String TAG = makeLogTag(ExploreActivity.class);
    private static final String SCREEN_LABEL = "JavaZone 2017";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent launchIntent = getIntent();

        if (launchIntent != null && (!Intent.ACTION_MAIN.equals(launchIntent.getAction())
                || !launchIntent.hasCategory(Intent.CATEGORY_LAUNCHER))) {
            overridePendingTransition(0, 0);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explore_io_act);
        setTitle(R.string.title_explore);

        // ANALYTICS SCREEN: View the Explore screen
        // Contains: Nothing (Page name is a constant)
        AnalyticsHelper.sendScreenView(SCREEN_LABEL);
    }

    @Override
    protected NavigationModel.NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationModel.NavigationItemEnum.EXPLORE;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Add the search button to the toolbar.
        Toolbar toolbar = getToolbar();
        toolbar.inflateMenu(R.menu.explore_io_menu);
        toolbar.setOnMenuItemClickListener(this);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_search:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
        }
        return false;
    }

}
