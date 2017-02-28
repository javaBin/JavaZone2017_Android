package no.javazone.androidapp.v1.debug;

import android.os.Bundle;

import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.navigation.NavigationModel;
import no.javazone.androidapp.v1.ui.activity.base.BaseActivity;

public class DebugActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_act);
        overridePendingTransition(0, 0);
    }

    @Override
    protected NavigationModel.NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationModel.NavigationItemEnum.DEBUG;
    }
}
