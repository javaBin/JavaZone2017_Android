package no.javazone.androidapp.v1.debug;

import android.content.Context;

public interface DebugAction {
    void run(Context context, Callback callback);
    String getLabel();

    public interface Callback {
        void done(boolean success, String message);
    }
}