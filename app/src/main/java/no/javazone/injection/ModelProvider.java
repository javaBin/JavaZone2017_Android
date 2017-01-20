package no.javazone.injection;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.net.Uri;

import no.javazone.archframework.model.ExploreModel;
import no.javazone.archframework.model.Model;
import no.javazone.archframework.model.SessionDetailModel;
import no.javazone.archframework.model.SessionFeedbackModel;
import no.javazone.archframework.model.MyScheduleModel;
import no.javazone.archframework.model.domain.ScheduleHelper;
import no.javazone.feedback.FeedbackHelper;
import no.javazone.util.SessionsHelper;

/**
 * Provides a way to inject stub classes when running integration tests.
 */
public class ModelProvider {

    private static SessionDetailModel stubSessionDetailModel = null;

    private static MyScheduleModel stubMyScheduleModel = null;

    private static SessionFeedbackModel stubSessionFeedbackModel = null;

    private static ExploreModel stubExploreIOModel = null;

    public static SessionDetailModel provideSessionDetailModel(Uri sessionUri, Context context,
                                                               SessionsHelper sessionsHelper, LoaderManager loaderManager) {
        if (stubSessionDetailModel != null) {
            return stubSessionDetailModel;
        } else {
            return new SessionDetailModel(sessionUri, context, sessionsHelper, loaderManager);
        }
    }

    public static MyScheduleModel provideMyScheduleModel(ScheduleHelper scheduleHelper,
            Context context) {
        if (stubMyScheduleModel != null) {
            return stubMyScheduleModel.initStaticDataAndObservers();
        } else {
            return new MyScheduleModel(scheduleHelper, context).initStaticDataAndObservers();
        }
    }

    public static SessionFeedbackModel provideSessionFeedbackModel(Uri sessionUri, Context context,
                                                                   FeedbackHelper feedbackHelper, LoaderManager loaderManager) {
        if (stubSessionFeedbackModel != null) {
            return stubSessionFeedbackModel;
        } else {
            return new SessionFeedbackModel(loaderManager, sessionUri, context, feedbackHelper);
        }
    }

    public static ExploreModel provideExploreModel(Uri sessionsUri, Context context,
                                                       LoaderManager loaderManager) {
        if (stubExploreIOModel != null) {
            return stubExploreIOModel;
        } else {
            return new ExploreModel(context, sessionsUri, loaderManager);
        }
    }

    public static void setStubModel(Model model) {
        if (model instanceof  ExploreModel) {
            stubExploreIOModel = (ExploreModel) model;
        }  if (model instanceof SessionFeedbackModel) {
            stubSessionFeedbackModel = (SessionFeedbackModel) model;
        } else if (model instanceof SessionDetailModel) {
            stubSessionDetailModel = (SessionDetailModel) model;
        } if (model instanceof MyScheduleModel) {
            stubMyScheduleModel = (MyScheduleModel) model;
        }
    }

}
