package no.javazone.util;

/**
 * Created by kkho on 18.01.2017.
 */

public class SessionDetailConstants {
    /**
     * How long before a session "This session starts in N minutes." is displayed.
     */
    public static final long HINT_TIME_BEFORE_SESSION_MIN = 60l;

    /**
     * Every 10 seconds, the time sensitive views of {@link SessionDetailFragment} are updated.
     * Those are related to live streaming, feedback, and information about how soon the session
     * starts.
     */
    public static final int TIME_HINT_UPDATE_INTERVAL = 10000;

    /**
     * How long before the end of a session the user can give feedback.
     */
    public static final long FEEDBACK_MILLIS_BEFORE_SESSION_END_MS = 15 * 60 * 1000l;

    /**
     * How long before the start of a session should livestream be open.
     */
    public static final long LIVESTREAM_BEFORE_SESSION_START_MS = 10 * TimeUtils.MINUTE;
}
