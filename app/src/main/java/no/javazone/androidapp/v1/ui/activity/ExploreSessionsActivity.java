package no.javazone.androidapp.v1.ui.activity;

import no.javazone.androidapp.v1.ui.activity.base.BaseActivity;

public class ExploreSessionsActivity extends BaseActivity {
    public static final String EXTRA_FILTER_TAG =
            "no.javazone.androidapp.v1.explore.EXTRA_FILTER_TAG";
    public static final String EXTRA_SHOW_LIVE_STREAM_SESSIONS =
            "no.javazone.androidapp.v1..explore.EXTRA_SHOW_LIVE_STREAM_SESSIONS";

    // The saved instance state filters
    private static final String STATE_FILTER_TAGS =
            "no.javazone.androidapp.v1.explore.STATE_FILTER_TAGS";
    private static final String STATE_CURRENT_URI =
            "no.javazone.androidapp.v1.explore.STATE_CURRENT_URI";

    private static final String SCREEN_LABEL = "ExploreSessions";
}
