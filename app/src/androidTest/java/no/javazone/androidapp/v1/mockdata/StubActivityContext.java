package no.javazone.androidapp.v1.mockdata;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

public class StubActivityContext extends ContextWrapper {

    private Activity mActivity;

    public StubActivityContext(Context context) {
        super(context);
    }

    public void setActivityContext(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void startActivity(Intent intent) {
        if (mActivity != null) {
            mActivity.startActivity(intent);
        }
    }
}