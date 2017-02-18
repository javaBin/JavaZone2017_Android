package no.javazone.sync;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.text.format.Time;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.javazone.Config;
import no.javazone.R;
import no.javazone.archframework.model.domain.Session;
import no.javazone.archframework.model.domain.Speaker;
import no.javazone.archframework.model.domain.Tag;
import no.javazone.archframework.model.dto.JZLabel;
import no.javazone.archframework.model.dto.JZPreciseDate;
import no.javazone.database.ScheduleContract;
import no.javazone.database.ScheduleContractHelper;
import no.javazone.database.ScheduleDatabase;
import no.javazone.schedule.JsonHandler;
import no.javazone.util.Constants;
import no.javazone.util.ParserUtils;
import no.javazone.util.TimeUtils;

import static no.javazone.util.LogUtils.LOGD;
import static no.javazone.util.LogUtils.LOGE;
import static no.javazone.util.LogUtils.LOGI;
import static no.javazone.util.LogUtils.LOGW;
import static no.javazone.util.LogUtils.makeLogTag;

public class JZConferenceDataHandler extends JsonHandler {
    private static final String TAG = makeLogTag(SyncHelper.class);
    private HashMap<String, Session> mSessions = new HashMap<String, Session>();
    private HashMap<String, Tag> mTagMap = null;
    private HashMap<String, Speaker> mSpeakerMap = null;
    private int mDefaultSessionColor;


    private static final String EVENT_TYPE_KEYNOTE = "keynote";
    private static final String EVENT_TYPE_CODELAB = "codelab";

    private static final int PARSE_FLAG_FORCE_SCHEDULE_REMOVE = 1;
    private static final int PARSE_FLAG_FORCE_SCHEDULE_ADD = 2;

    private static final Time sTime = new Time();
    private boolean mLocal;
    private boolean mThrowIfNoAuthToken;

    public JZConferenceDataHandler(Context context, boolean local, boolean throwIfNoAuthToken) {
        super(context);
        mDefaultSessionColor = mContext.getResources().getColor(R.color.default_session_color);
        mLocal = local;
        mThrowIfNoAuthToken = throwIfNoAuthToken;
    }

    @Override
    public void process(JsonElement element) {
        LOGI(TAG, "Updating sessions data");
        final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();

        Session[] sessions = new Gson().fromJson(element, Session[].class);
        boolean retainLocallyStarredSessions = true; //mLocal;

        Set<String> starredSessionIds = new HashSet<String>();
        // Collect the list of current starred sessions
        Cursor starredSessionsCursor = mContext.getContentResolver().query(
                ScheduleContract.Sessions.CONTENT_STARRED_URI,
                new String[]{ScheduleContract.Sessions.SESSION_ID},
                null, null, null);
        while (starredSessionsCursor.moveToNext()) {
            starredSessionIds.add(starredSessionsCursor.getString(0));
            LOGD(TAG, "session that has been starred was added here!");
        }
        starredSessionsCursor.close();

        // Clear out existing sessions
        batch.add(ContentProviderOperation
                .newDelete(ScheduleContract.addCallerIsSyncAdapterParameter(
                        ScheduleContract.Sessions.CONTENT_URI))
                .build());

        // Maintain a list of created block IDs
        Set<String> blockIds = new HashSet<String>();

        for (Session session : sessions) {
            int flags = 0;
            boolean starred = starredSessionIds.contains(session.id);
            if (retainLocallyStarredSessions) {
                flags = (starredSessionIds.contains(session.id)
                        ? PARSE_FLAG_FORCE_SCHEDULE_ADD
                        : PARSE_FLAG_FORCE_SCHEDULE_REMOVE);
            }

            String sessionId = session.id;
            if (TextUtils.isEmpty(sessionId)) {
                LOGW(TAG, "Found session with empty ID in API response.");
                continue;
            }

            // TODO

            String sessionTitle = session.title;
            //populateStartEndTime(session);
            // parseSpeakers(session, batch);
           // populateRoom(session);


            long sessionStartTime = 0;
            long sessionEndTime = 0;      //TODO handle sessions without timeslot

            long originalSessionEndTime = 1;
            long originalSessionStartTime = 1;

            if (session.startTimestamp != null && session.endTimestamp != null) {
                JZPreciseDate startDate = new JZPreciseDate(session.startTimestamp);
                JZPreciseDate endDate = new JZPreciseDate(session.endTimestamp);
                originalSessionStartTime = startDate.millis();
                originalSessionEndTime = endDate.millis();

                sessionStartTime = startDate.millis();//parseTime(event.start_date, event.start_time);
                sessionEndTime = endDate.millis();//event.end_date, event.end_time);
            }

            if (Constants.LIGHTNINGTALK.equals(session.format)) {
                sessionStartTime = snapStartTime(sessionStartTime);
                sessionEndTime = snapEndTime(sessionEndTime);

                if ((sessionEndTime - sessionStartTime) > 1000 * 60 * 61) {
                    sessionEndTime = sessionStartTime + 1000 * 60 * 60;
                }
            }

            // TODO
            if (!Constants.WORKSHOP.equals(session.format)) {
                int color = mDefaultSessionColor;

                // Insert session info
                final ContentProviderOperation.Builder builder = ContentProviderOperation
                        .newInsert(ScheduleContract
                                .addCallerIsSyncAdapterParameter(ScheduleContract.Sessions.CONTENT_URI))
                        .withValue(ScheduleContract.SyncColumns.UPDATED, System.currentTimeMillis())
                        .withValue(ScheduleContract.Sessions.SESSION_ID, sessionId)
                        .withValue(ScheduleContract.Sessions.SESSION_LEVEL, null)            // Not available
                        .withValue(ScheduleContract.Sessions.SESSION_TITLE, sessionTitle)
                        .withValue(ScheduleContract.Sessions.SESSION_ABSTRACT, session.description)
                        .withValue(ScheduleContract.Sessions.SESSION_START, originalSessionStartTime)
                        .withValue(ScheduleContract.Sessions.SESSION_END, originalSessionEndTime)
                        .withValue(ScheduleContract.Sessions.SESSION_TAGS, session.makeTagsList())
                        // .withValue(ScheduleContract.Sessions.SESSION_SPEAKER_NAMES, speakerNames)
                        .withValue(ScheduleContract.Sessions.SESSION_KEYWORDS, null)             // Not available
                        .withValue(ScheduleContract.Sessions.SESSION_STARRED, starred)
                        .withValue(ScheduleContract.Sessions.ROOM_ID, ParserUtils.sanitizeId(session.room))
                        .withValue(ScheduleContract.Sessions.SESSION_COLOR, color);

                batch.add(builder.build());

                // Replace all session speakers
                final Uri sessionSpeakersUri = ScheduleContract.Sessions.buildSpeakersDirUri(sessionId);
                batch.add(ContentProviderOperation
                        .newDelete(ScheduleContract
                                .addCallerIsSyncAdapterParameter(sessionSpeakersUri))
                        .build());
                if (session.speakers != null) {
                    for (String speakerId : session.speakers) {
                        batch.add(ContentProviderOperation.newInsert(sessionSpeakersUri)
                                .withValue(ScheduleDatabase.SessionsSpeakers.SESSION_ID, sessionId)
                                .withValue(ScheduleDatabase.SessionsSpeakers.SPEAKER_ID, speakerId).build());
                    }
                }

                final Uri tagUri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                        ScheduleContract.Tags.CONTENT_URI);
                if (session.makeTagsList() != null) {
                    String[] tagIds = session.makeTagsList().split(",");
                    for (String tag : tagIds) {
                        batch.add(ContentProviderOperation.newInsert(tagUri)
                                .withValue(ScheduleContract.Tags.TAG_CATEGORY,
                                        tag.startsWith("topic:") ? "TOPIC" :
                                                tag.startsWith("type:")? "TYPE"
                                                        : "THEME")
                                .withValue(ScheduleContract.Tags.TAG_NAME, tag).build());
                    }
                }
            }
        }

        // finally, push the changes into the Content Provider
        LOGI(TAG, "Applying " + batch.size() + " content provider operations.");
        try {
            int operations = batch.size();
            if (operations > 0) {
                mContext.getContentResolver().applyBatch(ScheduleContract.CONTENT_AUTHORITY, batch);
            }
            LOGD(TAG, "Successfully applied " + operations + " content provider operations.");

        } catch (RemoteException ex) {
            LOGE(TAG, "RemoteException while applying content provider operations.");
            throw new RuntimeException("Error executing content provider batch operation", ex);
        } catch (OperationApplicationException ex) {
            LOGE(TAG, "OperationApplicationException while applying content provider operations.");
            throw new RuntimeException("Error executing content provider batch operation", ex);
        }

        LOGD(TAG, "Notifying changes on all top-level paths on Content Resolver.");
        ContentResolver resolver = mContext.getContentResolver();
        for (String path : ScheduleContract.TOP_LEVEL_PATHS) {
            Uri uri = ScheduleContract.BASE_CONTENT_URI.buildUpon().appendPath(path).build();
            resolver.notifyChange(uri, null);
        }

        LOGD(TAG, "Done applying conference data.");
    }

    private static long snapStartTime(final long pSessionStartTime) {

        Date date = new Date(pSessionStartTime);
        int minutes = (date.getHours() - 9) * 60 + (date.getMinutes() - 0);

        int offset = minutes % (60 + 20);
        date.setMinutes(date.getMinutes() - offset);
        return date.getTime();
    }

    private static long snapEndTime(final long pSessionEndTime) {

        Date date = new Date(pSessionEndTime);
        int minutes = (date.getHours() - 9) * 60 + (date.getMinutes() + 0);

        int offset = minutes % (60 + 20);
        date.setMinutes(date.getMinutes() + 60 - offset);
        return date.getTime();
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Sessions.CONTENT_URI);

        // build a map of session to session import hashcode so we know what to update,
        // what to insert, and what to delete
        HashMap<String, String> sessionHashCodes = loadSessionHashCodes();
        boolean incrementalUpdate = (sessionHashCodes != null) && (sessionHashCodes.size() > 0);

        // set of sessions that we want to keep after the sync
        HashSet<String> sessionsToKeep = new HashSet<String>();

        if (incrementalUpdate) {
            LOGD(TAG, "Doing incremental update for sessions.");
        } else {
            LOGD(TAG, "Doing full (non-incremental) update for sessions.");
            list.add(ContentProviderOperation.newDelete(uri).build());
        }

        int updatedSessions = 0;
        for (Session session : mSessions.values()) {
            // Set the session grouping order in the object, so it can be used in hash calculation
            session.groupingOrder = computeTypeOrder(session);

            // compute the incoming session's hashcode to figure out if we need to update
            String hashCode = session.getImportHashCode();
            sessionsToKeep.add(session.id);

            // add session, if necessary
            if (!incrementalUpdate || !sessionHashCodes.containsKey(session.id) ||
                    !sessionHashCodes.get(session.id).equals(hashCode)) {
                ++updatedSessions;
                boolean isNew = !incrementalUpdate || !sessionHashCodes.containsKey(session.id);
                buildSession(isNew, session, list);

                // add relationships to speakers and track
                buildSessionSpeakerMapping(session, list);
                buildTagsMapping(session, list);
            }
        }

        int deletedSessions = 0;
        if (incrementalUpdate) {
            for (String sessionId : sessionHashCodes.keySet()) {
                if (!sessionsToKeep.contains(sessionId)) {
                    buildDeleteOperation(sessionId, list);
                    ++deletedSessions;
                }
            }
        }

        LOGD(TAG, "Sessions: " + (incrementalUpdate ? "INCREMENTAL" : "FULL") + " update. " +
                updatedSessions + " to update, " + deletedSessions + " to delete. New total: " +
                mSessions.size());
    }

    private void buildDeleteOperation(String sessionId, List<ContentProviderOperation> list) {
        Uri sessionUri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Sessions.buildSessionUri(sessionId));
        list.add(ContentProviderOperation.newDelete(sessionUri).build());
    }


    private HashMap<String, String> loadSessionHashCodes() {
        Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Sessions.CONTENT_URI);
        LOGD(TAG, "Loading session hashcodes for session import optimization.");
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(uri, SessionHashcodeQuery.PROJECTION,
                    null, null, null);
            if (cursor == null || cursor.getCount() < 1) {
                LOGW(TAG, "Warning: failed to load session hashcodes. Not optimizing session import.");
                return null;
            }
            HashMap<String, String> hashcodeMap = new HashMap<String, String>();
            if (cursor.moveToFirst()) {
                do {
                    String sessionId = cursor.getString(SessionHashcodeQuery.SESSION_ID);
                    String hashcode = cursor.getString(SessionHashcodeQuery.SESSION_IMPORT_HASHCODE);
                    hashcodeMap.put(sessionId, hashcode == null ? "" : hashcode);
                } while (cursor.moveToNext());
            }
            LOGD(TAG, "Session hashcodes loaded for " + hashcodeMap.size() + " sessions.");
            return hashcodeMap;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    StringBuilder mStringBuilder = new StringBuilder();


    private void buildSession(boolean isInsert,
                              Session session, ArrayList<ContentProviderOperation> list) {
        ContentProviderOperation.Builder builder;
        Uri allSessionsUri = ScheduleContractHelper
                .setUriAsCalledFromSyncAdapter(ScheduleContract.Sessions.CONTENT_URI);
        Uri thisSessionUri = ScheduleContractHelper
                .setUriAsCalledFromSyncAdapter(ScheduleContract.Sessions.buildSessionUri(
                        session.id));

        if (isInsert) {
            builder = ContentProviderOperation.newInsert(allSessionsUri);
        } else {
            builder = ContentProviderOperation.newUpdate(thisSessionUri);
        }

        String speakerNames = "";
        if (mSpeakerMap != null) {
            // build human-readable list of speakers
            mStringBuilder.setLength(0);
            for (int i = 0; i < session.speakers.length; ++i) {
                if (mSpeakerMap.containsKey(session.speakers[i])) {
                    mStringBuilder
                            .append(i == 0 ? "" : i == session.speakers.length - 1 ? " and " : ", ")
                            .append(mSpeakerMap.get(session.speakers[i]).name.trim());
                } else {
                    LOGW(TAG, "Unknown speaker ID " + session.speakers[i] + " in session " + session.id);
                }
            }
            speakerNames = mStringBuilder.toString();
        } else {
            LOGE(TAG, "Can't build speaker names -- speaker map is null.");
        }

        int color = mDefaultSessionColor;
        try {
            if (!TextUtils.isEmpty(session.color)) {
                color = Color.parseColor(session.color);
            }
        } catch (IllegalArgumentException ex) {
            LOGD(TAG, "Ignoring invalid formatted session color: " + session.color);
        }

        builder.withValue(ScheduleContract.SyncColumns.UPDATED, System.currentTimeMillis())
                .withValue(ScheduleContract.Sessions.SESSION_ID, session.id)
                .withValue(ScheduleContract.Sessions.SESSION_LEVEL, null)            // Not available
                .withValue(ScheduleContract.Sessions.SESSION_TITLE, session.title)
                .withValue(ScheduleContract.Sessions.SESSION_ABSTRACT, session.description)
                .withValue(ScheduleContract.Sessions.SESSION_START, TimeUtils.timestampToMillis(session.startTimestamp, 0))
                .withValue(ScheduleContract.Sessions.SESSION_END, TimeUtils.timestampToMillis(session.endTimestamp, 0))
                .withValue(ScheduleContract.Sessions.SESSION_TAGS, session.makeTagsList())
                .withValue(ScheduleContract.Sessions.SESSION_SPEAKER_NAMES, speakerNames)
                .withValue(ScheduleContract.Sessions.SESSION_KEYWORDS, null)             // Not available
                .withValue(ScheduleContract.Sessions.ROOM_ID, session.room)
                .withValue(ScheduleContract.Sessions.SESSION_GROUPING_ORDER, session.groupingOrder)
                .withValue(ScheduleContract.Sessions.SESSION_IMPORT_HASHCODE,
                        session.getImportHashCode())
                .withValue(ScheduleContract.Sessions.SESSION_STARRED, session.starred)
                // Disabled since this isn't being used by this app.
                // .withValue(ScheduleContract.Sessions.SESSION_RELATED_CONTENT, session.relatedContent)
                .withValue(ScheduleContract.Sessions.SESSION_COLOR, color);
        list.add(builder.build());
    }

    private int computeTypeOrder(Session session) {
        int order = Integer.MAX_VALUE;
        int keynoteOrder = -1;
        if (mTagMap == null) {
            throw new IllegalStateException("Attempt to compute type order without tag map.");
        }
        for (String tagId : session.tags) {
            if (Config.Tags.SPECIAL_KEYNOTE.equals(tagId)) {
                return keynoteOrder;
            }
            Tag tag = mTagMap.get(tagId);
            if (tag != null && Config.Tags.SESSION_GROUPING_TAG_CATEGORY.equals(tag.category)) {
                if (tag.order_in_category < order) {
                    order = tag.order_in_category;
                }
            }
        }
        return order;
    }

    private void buildSessionSpeakerMapping(Session session,
                                            ArrayList<ContentProviderOperation> list) {
        final Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Sessions.buildSpeakersDirUri(session.id));

        // delete any existing relationship between this session and speakers
        list.add(ContentProviderOperation.newDelete(uri).build());

        // add relationship records to indicate the speakers for this session
        if (session.speakers != null) {
            for (String speakerId : session.speakers) {
                list.add(ContentProviderOperation.newInsert(uri)
                        .withValue(ScheduleDatabase.SessionsSpeakers.SESSION_ID, session.id)
                        .withValue(ScheduleDatabase.SessionsSpeakers.SPEAKER_ID, speakerId)
                        .build());
            }
        }
    }

    private void buildTagsMapping(Session session, ArrayList<ContentProviderOperation> list) {
        final Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Sessions.buildTagsDirUri(session.id));

        // delete any existing mappings
        list.add(ContentProviderOperation.newDelete(uri).build());

        // add a mapping (a session+tag tuple) for each tag in the session
        for (String tag : session.tags) {
            list.add(ContentProviderOperation.newInsert(uri)
                    .withValue(ScheduleDatabase.SessionsTags.SESSION_ID, session.id)
                    .withValue(ScheduleDatabase.SessionsTags.TAG_ID, tag).build());
        }
    }

    private interface SessionHashcodeQuery {
        String[] PROJECTION = {
                BaseColumns._ID,
                ScheduleContract.Sessions.SESSION_ID,
                ScheduleContract.Sessions.SESSION_IMPORT_HASHCODE
        };
        int _ID = 0;
        int SESSION_ID = 1;
        int SESSION_IMPORT_HASHCODE = 2;
    }

}
