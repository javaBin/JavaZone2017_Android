package no.javazone.androidapp.v1.testutils;

import android.database.Cursor;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.HashMap;

import no.javazone.androidapp.v1.archframework.model.Model;
import no.javazone.androidapp.v1.archframework.model.ModelWithLoaderManager;
import no.javazone.androidapp.v1.database.QueryEnum;

public class StubModelHelper<Q extends QueryEnum> {
    public void overrideLoaderManager(@NonNull final Q query,
                                      @NonNull final Model.DataQueryCallback callback,
                                      final HashMap<QueryEnum, Cursor> fakeData,
                                      HashMap<Q, Model.DataQueryCallback> callbacks,
                                      final ModelWithLoaderManager model) {

        callbacks.put(query, callback);

        Handler h = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (fakeData.containsKey(query)) {
                    model.onLoadFinished(query, fakeData.get(query));
                }
            }
        };

        // Delayed to ensure the UI is ready, because it will fire the callback to update the view
        // very quickly
        h.postDelayed(r, 5);
    }
}
