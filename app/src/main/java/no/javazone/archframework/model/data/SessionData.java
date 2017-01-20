package no.javazone.archframework.model.data;

import android.content.Context;
import android.text.TextUtils;

import java.util.Calendar;
import java.util.TimeZone;

import no.javazone.util.SettingsUtils;
import no.javazone.util.TimeUtils;

public class SessionData {
    private String mSessionName;
    private String mDetails;
    private String mSessionId;
    private Calendar mStartDate;
    private Calendar mEndDate;
    private String mVideoUrl;
    private String mTags;
    private boolean mInSchedule;
    private boolean mIsLive;

    public SessionData() { }

    public SessionData(Context context, String sessionName, String details, String sessionId,
                       long startTime, long endTime,
                       String videoUrl, String tags, boolean inSchedule) {
        updateData(context, sessionName, details, sessionId, startTime, endTime,
                videoUrl, tags, inSchedule);
    }

    public void updateData(Context context, String sessionName, String details, String sessionId,
                           long startTime, long endTime,String videoUrl, String tags, boolean inSchedule) {
        mSessionName = sessionName;
        mDetails = details;
        mSessionId = sessionId;
        TimeZone timeZone = SettingsUtils.getDisplayTimeZone(context);
        mStartDate = Calendar.getInstance();
        mStartDate.setTimeInMillis(startTime);
        mStartDate.setTimeZone(timeZone);
        mEndDate = Calendar.getInstance();
        mEndDate.setTimeInMillis(endTime);
        mEndDate.setTimeZone(timeZone);
        mVideoUrl = videoUrl;
        mTags = tags;
        mInSchedule = inSchedule;
    }


    public boolean IsLive(Context context) {
        if (mStartDate == null || mEndDate == null) {
            return false;
        }
        Calendar now = java.util.Calendar.getInstance();
        now.setTimeInMillis(TimeUtils.getCurrentTime(context));
        return mStartDate.before(now) && mEndDate.after(now);
    }

    public boolean isVideoAvailable() {
        return !TextUtils.isEmpty(mVideoUrl);
    }

    public String getSessionName() {
        return mSessionName;
    }

    public String getDetails() {
        return mDetails;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public void setDetails(String details) { mDetails = details; }

    public Calendar getStartDate() { return mStartDate; }

    public Calendar getEndDate() { return mEndDate; }

    public String getVideoUrl() { return mVideoUrl; }

    public String getTags() { return mTags; }

    public boolean isInSchedule() { return mInSchedule; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SessionData that = (SessionData) o;

        if (mInSchedule != that.mInSchedule) {
            return false;
        }
        if (mSessionName != null ? !mSessionName.equals(that.mSessionName) :
                that.mSessionName != null) {
            return false;
        }
        if (mDetails != null ? !mDetails.equals(that.mDetails) : that.mDetails != null) {
            return false;
        }
        if (mSessionId != null ? !mSessionId.equals(that.mSessionId) : that.mSessionId != null) {
            return false;
        }
        if (mStartDate != null ? !mStartDate.equals(that.mStartDate) : that.mStartDate != null) {
            return false;
        }
        if (mEndDate != null ? !mEndDate.equals(that.mEndDate) : that.mEndDate != null) {
            return false;
        }
        if (mVideoUrl != null ? !mVideoUrl.equals(that.mVideoUrl) :
                that.mVideoUrl != null) {
            return false;
        }
        return mTags != null ? mTags.equals(that.mTags) : that.mTags == null;

    }

    @Override
    public int hashCode() {
        int result = mSessionName != null ? mSessionName.hashCode() : 0;
        result = 31 * result + (mDetails != null ? mDetails.hashCode() : 0);
        result = 31 * result + (mSessionId != null ? mSessionId.hashCode() : 0);
        result = 31 * result + (mStartDate != null ? mStartDate.hashCode() : 0);
        result = 31 * result + (mEndDate != null ? mEndDate.hashCode() : 0);
        result = 31 * result + (mVideoUrl != null ? mVideoUrl.hashCode() : 0);
        result = 31 * result + (mTags != null ? mTags.hashCode() : 0);
        result = 31 * result + (mInSchedule ? 1 : 0);
        return result;
    }
}

