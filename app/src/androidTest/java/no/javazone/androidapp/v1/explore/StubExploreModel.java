package no.javazone.androidapp.v1.explore;

import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.HashMap;

import no.javazone.androidapp.v1.archframework.model.ExploreModel;
import no.javazone.androidapp.v1.database.QueryEnum;
import no.javazone.androidapp.v1.testutils.StubModelHelper;

public class StubExploreModel extends ExploreModel {
    private HashMap<QueryEnum, Cursor> mFakeData = new HashMap<QueryEnum, Cursor>();


    public StubExploreModel(Context context, Cursor sessionsCursor, Cursor tagsCursor) {
        super(context, null, null);
        mFakeData.put(ExploreQueryEnum.SESSIONS, sessionsCursor);
        mFakeData.put(ExploreQueryEnum.TAGS, tagsCursor);
    }

    @Override
    public void requestData(final @NonNull ExploreModel.ExploreQueryEnum query,
                            final @NonNull DataQueryCallback callback) {
        new StubModelHelper<ExploreQueryEnum>()
                .overrideLoaderManager(query, callback, mFakeData, mDataQueryCallbacks, this);

    }
}
