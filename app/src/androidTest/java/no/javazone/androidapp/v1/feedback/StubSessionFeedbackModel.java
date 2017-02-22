package no.javazone.androidapp.v1.feedback;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.HashMap;

import no.javazone.androidapp.v1.archframework.model.SessionFeedbackModel;
import no.javazone.androidapp.v1.database.QueryEnum;
import no.javazone.androidapp.v1.testutils.StubModelHelper;


public class StubSessionFeedbackModel extends SessionFeedbackModel {

    private HashMap<QueryEnum, Cursor> mFakeData = new HashMap<QueryEnum, Cursor>();

    public StubSessionFeedbackModel(Uri uri, Context context, Cursor sessionCursor,
                                    FeedbackHelper feedbackHelper) {
        super(null, uri, context, feedbackHelper);
        mFakeData.put(SessionFeedbackQueryEnum.SESSION, sessionCursor);
    }

    @Override
    public void requestData(final @NonNull SessionFeedbackQueryEnum query,
                            final @NonNull DataQueryCallback callback) {
        new StubModelHelper<SessionFeedbackQueryEnum>()
                .overrideLoaderManager(query, callback, mFakeData, mDataQueryCallbacks, this);
    }
}
