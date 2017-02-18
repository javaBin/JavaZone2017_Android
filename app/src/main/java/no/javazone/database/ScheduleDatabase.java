package no.javazone.database;

import android.app.SearchManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.google.common.collect.Tables;

import no.javazone.sync.ConferenceDataHandler;
import no.javazone.sync.SyncHelper;

import static no.javazone.database.ScheduleContract.*;
import static no.javazone.util.LogUtils.LOGD;
import static no.javazone.util.LogUtils.LOGI;
import static no.javazone.util.LogUtils.LOGW;
import static no.javazone.util.LogUtils.makeLogTag;

public class ScheduleDatabase extends SQLiteOpenHelper {
    private static final String TAG = makeLogTag(ScheduleDatabase.class);

    private static final String DATABASE_NAME = "schedule.db";
    private static final int VER_2017_RELEASE_A = 213;
    private static final int VER_2017_RELEASE_B = 214;

    private static final int CUR_DATABASE_VERSION = VER_2017_RELEASE_B;

    private final Context mContext;

    interface DatabaseTables {
        String BLOCKS = "blocks";
        String TAGS = "tags";
        String ROOMS = "rooms";
        String CARDS = "cards";
        String SESSIONS = "sessions";
        String SPEAKERS = "speakers";
        String SESSIONS_TAGS = "sessions_tags";
        String SESSIONS_SPEAKERS = "sessions_speakers";
        String MAPMARKERS = "mapmarkers";
        String MAPTILES = "mapoverlays";
        String FEEDBACK = "feedback";

        String SESSIONS_SEARCH = "sessions_search";

        String SEARCH_SUGGEST = "search_suggest";

        String SESSIONS_JOIN_BLOCKS_ROOMS = "sessions "
                + "LEFT OUTER JOIN blocks ON sessions.block_id=blocks.block_id "
                + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id";

        String SESSIONS_JOIN_ROOMS_TAGS = "sessions "
                + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id "
                + "LEFT OUTER JOIN sessions_tags ON sessions.session_id=sessions_tags.session_id";

        String SESSIONS_JOIN_ROOMS_TAGS_FEEDBACK_MYSCHEDULE = "sessions "
                + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id "
                + "LEFT OUTER JOIN sessions_tags ON sessions.session_id=sessions_tags.session_id "
                + "LEFT OUTER JOIN feedback ON sessions.session_id=feedback.session_id";

        String SESSIONS_JOIN_ROOMS = "sessions "
                + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id";

        String SESSIONS_SPEAKERS_JOIN_SPEAKERS = "sessions_speakers "
                + "LEFT OUTER JOIN speakers ON sessions_speakers.speaker_id=speakers.speaker_id";

        String SESSIONS_TAGS_JOIN_TAGS = "sessions_tags "
                + "LEFT OUTER JOIN tags ON sessions_tags.tag_id=tags.tag_id";

        String SESSIONS_SPEAKERS_JOIN_SESSIONS_ROOMS = "sessions_speakers "
                + "LEFT OUTER JOIN sessions ON sessions_speakers.session_id=sessions.session_id "
                + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id";

        String SESSIONS_SEARCH_JOIN_SESSIONS_ROOMS = "sessions_search "
                + "LEFT OUTER JOIN sessions ON sessions_search.session_id=sessions.session_id "
                + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id";
    }

    private interface Triggers {
        // Deletes from dependent tables when corresponding sessions are deleted.
        String SESSIONS_TAGS_DELETE = "sessions_tags_delete";
        String SESSIONS_SPEAKERS_DELETE = "sessions_speakers_delete";
        String SESSIONS_MY_SCHEDULE_DELETE = "sessions_myschedule_delete";
        String SESSIONS_FEEDBACK_DELETE = "sessions_feedback_delete";
    }

    public interface SessionsSpeakers {
        String SESSION_ID = "session_id";
        String SPEAKER_ID = "speaker_id";
    }

    public interface SessionsTags {
        String SESSION_ID = "session_id";
        String TAG_ID = "tag_id";
    }

    interface SessionsSearchColumns {
        String SESSION_ID = "session_id";
        String BODY = "body";
    }

    /**
     * Fully-qualified field names.
     */
    private interface Qualified {
        String SESSIONS_SEARCH = DatabaseTables.SESSIONS_SEARCH + "(" + SessionsSearchColumns.SESSION_ID
                + "," + SessionsSearchColumns.BODY + ")";

        String SESSIONS_TAGS_SESSION_ID = DatabaseTables.SESSIONS_TAGS + "."
                + SessionsTags.SESSION_ID;

        String SESSIONS_SPEAKERS_SESSION_ID = DatabaseTables.SESSIONS_SPEAKERS + "."
                + SessionsSpeakers.SESSION_ID;

        String SESSIONS_SPEAKERS_SPEAKER_ID = DatabaseTables.SESSIONS_SPEAKERS + "."
                + SessionsSpeakers.SPEAKER_ID;

        String SPEAKERS_SPEAKER_ID = DatabaseTables.SPEAKERS + "." + Speakers.SPEAKER_ID;

        String FEEDBACK_SESSION_ID = DatabaseTables.FEEDBACK + "." + FeedbackColumns.SESSION_ID;
    }

    /**
     * {@code REFERENCES} clauses.
     */
    private interface References {
        String BLOCK_ID = "REFERENCES " + DatabaseTables.BLOCKS + "(" + Blocks.BLOCK_ID + ")";
        String TAG_ID = "REFERENCES " + DatabaseTables.TAGS + "(" + Tags.TAG_ID + ")";
        String ROOM_ID = "REFERENCES " + DatabaseTables.ROOMS + "(" + Rooms.ROOM_ID + ")";
        String SESSION_ID = "REFERENCES " + DatabaseTables.SESSIONS + "(" + Sessions.SESSION_ID + ")";
        String SPEAKER_ID = "REFERENCES " + DatabaseTables.SPEAKERS + "(" + Speakers.SPEAKER_ID + ")";
    }

    public ScheduleDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DatabaseTables.BLOCKS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BlocksColumns.BLOCK_ID + " TEXT NOT NULL,"
                + BlocksColumns.BLOCK_TITLE + " TEXT NOT NULL,"
                + BlocksColumns.BLOCK_START + " INTEGER NOT NULL,"
                + BlocksColumns.BLOCK_END + " INTEGER NOT NULL,"
                + BlocksColumns.BLOCK_TYPE + " TEXT,"
                + BlocksColumns.BLOCK_SUBTITLE + " TEXT,"
                + "UNIQUE (" + BlocksColumns.BLOCK_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + DatabaseTables.TAGS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TagsColumns.TAG_ID + " TEXT NOT NULL,"
                + TagsColumns.TAG_CATEGORY + " TEXT NOT NULL,"
                + TagsColumns.TAG_NAME + " TEXT NOT NULL,"
                + TagsColumns.TAG_ORDER_IN_CATEGORY + " INTEGER,"
                + TagsColumns.TAG_ABSTRACT + " TEXT,"
                + TagsColumns.TAG_COLOR + " TEXT,"
                + TagsColumns.TAG_PHOTO_URL + " TEXT,"
                + "UNIQUE (" + TagsColumns.TAG_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + DatabaseTables.ROOMS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RoomsColumns.ROOM_ID + " TEXT NOT NULL,"
                + RoomsColumns.ROOM_NAME + " TEXT,"
                + RoomsColumns.ROOM_FLOOR + " TEXT,"
                + "UNIQUE (" + RoomsColumns.ROOM_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + DatabaseTables.SESSIONS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + SessionsColumns.SESSION_ID + " TEXT NOT NULL,"
                + Sessions.ROOM_ID + " TEXT " + References.ROOM_ID + ","
                + SessionsColumns.SESSION_START + " INTEGER NOT NULL,"
                + SessionsColumns.SESSION_END + " INTEGER NOT NULL,"
                + SessionsColumns.SESSION_LEVEL + " TEXT,"
                + SessionsColumns.SESSION_TITLE + " TEXT,"
                + SessionsColumns.SESSION_ABSTRACT + " TEXT,"
                + SessionsColumns.SESSION_KEYWORDS + " TEXT,"
                + SessionsColumns.SESSION_VIDEO_URL + " TEXT,"
                + SessionsColumns.SESSION_IMPORT_HASHCODE + " TEXT NOT NULL DEFAULT '',"
                + SessionsColumns.SESSION_TAGS + " TEXT,"
                + SessionsColumns.SESSION_GROUPING_ORDER + " INTEGER,"
                + SessionsColumns.SESSION_SPEAKER_NAMES + " TEXT,"
                + SessionsColumns.SESSION_RELATED_CONTENT + " TEXT,"
                + SessionsColumns.SESSION_STARRED + " INTEGER NOT NULL DEFAULT 0,"
                + "UNIQUE (" + SessionsColumns.SESSION_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + DatabaseTables.SPEAKERS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + SpeakersColumns.SPEAKER_ID + " TEXT NOT NULL,"
                + SpeakersColumns.SPEAKER_NAME + " TEXT,"
                + SpeakersColumns.SPEAKER_IMAGE_URL + " TEXT,"
                + SpeakersColumns.SPEAKER_COMPANY + " TEXT,"
                + SpeakersColumns.SPEAKER_ABSTRACT + " TEXT,"
                + SpeakersColumns.SPEAKER_IMPORT_HASHCODE + " TEXT NOT NULL DEFAULT '',"
                + SpeakersColumns.SPEAKER_URL + " TEXT,"
                + SpeakersColumns.SPEAKER_PLUSONE_URL + " TEXT,"
                + SpeakersColumns.SPEAKER_TWITTER_URL + " TEXT,"
                + "UNIQUE (" + SpeakersColumns.SPEAKER_ID + ") ON CONFLICT REPLACE)");

            db.execSQL("CREATE TABLE " + DatabaseTables.CARDS + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + Cards.ACTION_COLOR + " TEXT, "
                    + Cards.ACTION_TEXT + " TEXT, "
                    + Cards.ACTION_URL + " TEXT, "
                    + Cards.BACKGROUND_COLOR + " TEXT, "
                    + Cards.CARD_ID + " TEXT, "
                    + Cards.DISPLAY_END_DATE + " INTEGER, "
                    + Cards.DISPLAY_START_DATE + " INTEGER, "
                    + Cards.MESSAGE + " TEXT, "
                    + Cards.TEXT_COLOR + " TEXT, "
                    + Cards.TITLE + " TEXT,  "
                    + Cards.ACTION_TYPE + " TEXT,  "
                    + Cards.ACTION_EXTRA + " TEXT, "
                    + "UNIQUE (" + Cards.CARD_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + DatabaseTables.SESSIONS_SPEAKERS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SessionsSpeakers.SESSION_ID + " TEXT NOT NULL " + References.SESSION_ID + ","
                + SessionsSpeakers.SPEAKER_ID + " TEXT NOT NULL " + References.SPEAKER_ID + ","
                + "UNIQUE (" + SessionsSpeakers.SESSION_ID + ","
                + SessionsSpeakers.SPEAKER_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + DatabaseTables.SESSIONS_TAGS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SessionsTags.SESSION_ID + " TEXT NOT NULL " + References.SESSION_ID + ","
                + SessionsTags.TAG_ID + " TEXT NOT NULL " + References.TAG_ID + ","
                + "UNIQUE (" + SessionsTags.SESSION_ID + ","
                + SessionsTags.TAG_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + DatabaseTables.MAPTILES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MapTileColumns.TILE_FLOOR + " INTEGER NOT NULL,"
                + MapTileColumns.TILE_FILE + " TEXT NOT NULL,"
                + MapTileColumns.TILE_URL + " TEXT NOT NULL,"
                + "UNIQUE (" + MapTileColumns.TILE_FLOOR + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + DatabaseTables.FEEDBACK + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + Sessions.SESSION_ID + " TEXT " + References.SESSION_ID + ","
                + FeedbackColumns.SESSION_RATING + " INTEGER NOT NULL,"
                + FeedbackColumns.ANSWER_RELEVANCE + " INTEGER NOT NULL,"
                + FeedbackColumns.ANSWER_CONTENT + " INTEGER NOT NULL,"
                + FeedbackColumns.ANSWER_SPEAKER + " INTEGER NOT NULL,"
                + FeedbackColumns.COMMENTS + " TEXT,"
                + FeedbackColumns.SYNCED + " INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TRIGGER " + Triggers.SESSIONS_FEEDBACK_DELETE + " AFTER DELETE ON "
                + DatabaseTables.SESSIONS + " BEGIN DELETE FROM " + DatabaseTables.FEEDBACK + " "
                + " WHERE " + Qualified.FEEDBACK_SESSION_ID + "=old." + Sessions.SESSION_ID
                + ";" + " END;");

        db.execSQL("CREATE TABLE " + DatabaseTables.MAPMARKERS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MapMarkerColumns.MARKER_ID + " TEXT NOT NULL,"
                + MapMarkerColumns.MARKER_TYPE + " TEXT NOT NULL,"
                + MapMarkerColumns.MARKER_LATITUDE + " DOUBLE NOT NULL,"
                + MapMarkerColumns.MARKER_LONGITUDE + " DOUBLE NOT NULL,"
                + MapMarkerColumns.MARKER_LABEL + " TEXT,"
                + MapMarkerColumns.MARKER_FLOOR + " INTEGER NOT NULL,"
                + "UNIQUE (" + MapMarkerColumns.MARKER_ID + ") ON CONFLICT REPLACE)");

        // Full-text search index. Update using updateSessionSearchIndex method.
        // Use the porter tokenizer for simple stemming, so that "frustration" matches "frustrated."
        db.execSQL("CREATE VIRTUAL TABLE " + DatabaseTables.SESSIONS_SEARCH + " USING fts3("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SessionsSearchColumns.BODY + " TEXT NOT NULL,"
                + SessionsSearchColumns.SESSION_ID
                + " TEXT NOT NULL " + References.SESSION_ID + ","
                + "UNIQUE (" + SessionsSearchColumns.SESSION_ID + ") ON CONFLICT REPLACE,"
                + "tokenize=porter)");

        // Search suggestions
        db.execSQL("CREATE TABLE " + DatabaseTables.SEARCH_SUGGEST + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SearchManager.SUGGEST_COLUMN_TEXT_1 + " TEXT NOT NULL)");

        // Session deletion triggers
        db.execSQL("CREATE TRIGGER " + Triggers.SESSIONS_TAGS_DELETE + " AFTER DELETE ON "
                + DatabaseTables.SESSIONS + " BEGIN DELETE FROM " + DatabaseTables.SESSIONS_TAGS + " "
                + " WHERE " + Qualified.SESSIONS_TAGS_SESSION_ID + "=old." + Sessions.SESSION_ID
                + ";" + " END;");

        db.execSQL("CREATE TRIGGER " + Triggers.SESSIONS_SPEAKERS_DELETE + " AFTER DELETE ON "
                + DatabaseTables.SESSIONS + " BEGIN DELETE FROM " + DatabaseTables.SESSIONS_SPEAKERS + " "
                + " WHERE " + Qualified.SESSIONS_SPEAKERS_SESSION_ID + "=old." + Sessions.SESSION_ID
                + ";" + " END;");
    }

    /**
     * Updates the session search index. This should be done sparingly, as the queries are rather
     * complex.
     */
    static void updateSessionSearchIndex(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + DatabaseTables.SESSIONS_SEARCH);

        db.execSQL("INSERT INTO " + Qualified.SESSIONS_SEARCH
                + " SELECT s." + Sessions.SESSION_ID + ",("

                // Full text body
                + Sessions.SESSION_TITLE + "||'; '||"
                + Sessions.SESSION_ABSTRACT + "||'; '||"
                + "IFNULL(GROUP_CONCAT(t." + Speakers.SPEAKER_NAME + ",' '),'')||'; '||"
                + "'')"

                + " FROM " + DatabaseTables.SESSIONS + " s "
                + " LEFT OUTER JOIN"

                // Subquery resulting in session_id, speaker_id, speaker_name
                + "(SELECT " + Sessions.SESSION_ID + "," + Qualified.SPEAKERS_SPEAKER_ID
                + "," + Speakers.SPEAKER_NAME
                + " FROM " + DatabaseTables.SESSIONS_SPEAKERS
                + " INNER JOIN " + DatabaseTables.SPEAKERS
                + " ON " + Qualified.SESSIONS_SPEAKERS_SPEAKER_ID + "="
                + Qualified.SPEAKERS_SPEAKER_ID
                + ") t"

                // Grand finale
                + " ON s." + Sessions.SESSION_ID + "=t." + Sessions.SESSION_ID
                + " GROUP BY s." + Sessions.SESSION_ID);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOGD(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        // Current DB version. We update this variable as we perform upgrades to reflect
        // the current version we are in.
        int version = oldVersion;

        // Indicates whether the data we currently have should be invalidated as a
        // result of the db upgrade. Default is true (invalidate); if we detect that this
        // is a trivial DB upgrade, we set this to false.
        boolean dataInvalidated = true;
        version = VER_2017_RELEASE_A;

        LOGD(TAG, "After upgrade logic, at version " + version);

        // At this point, we ran out of upgrade logic, so if we are still at the wrong
        // version, we have no choice but to delete everything and create everything again.
        if (version != CUR_DATABASE_VERSION) {
            LOGW(TAG, "Upgrade unsuccessful -- destroying old data during upgrade");

            db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.SESSIONS_TAGS_DELETE);
            db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.SESSIONS_SPEAKERS_DELETE);
            db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.SESSIONS_FEEDBACK_DELETE);
            db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.SESSIONS_MY_SCHEDULE_DELETE);

            db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.BLOCKS);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.ROOMS);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.TAGS);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.SESSIONS);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.SPEAKERS);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.SESSIONS_SPEAKERS);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.SESSIONS_TAGS);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.FEEDBACK);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.SESSIONS_SEARCH);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.SEARCH_SUGGEST);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.MAPMARKERS);
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.MAPTILES);

            onCreate(db);
            version = CUR_DATABASE_VERSION;
        }

        if (dataInvalidated) {
            LOGD(TAG, "Data invalidated; resetting our data timestamp.");
            //ConferenceDataHandler.resetDataTimestamp(mContext);
            LOGI(TAG, "DB upgrade complete. Requesting resync.");
            SyncHelper.requestManualSync();
        }
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
