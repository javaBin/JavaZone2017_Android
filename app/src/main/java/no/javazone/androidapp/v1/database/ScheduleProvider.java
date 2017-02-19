package no.javazone.androidapp.v1.database;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.javazone.androidapp.v1.Config;
import no.javazone.androidapp.v1.appwidget.ScheduleWidgetProvider;
import no.javazone.androidapp.v1.util.AccountUtils;
import no.javazone.androidapp.v1.util.SelectionBuilder;
import no.javazone.androidapp.v1.util.SettingsUtils;

import static no.javazone.androidapp.v1.database.ScheduleContract.*;
import static no.javazone.androidapp.v1.database.ScheduleDatabase.*;
import static no.javazone.androidapp.v1.util.LogUtils.LOGD;
import static no.javazone.androidapp.v1.util.LogUtils.LOGE;
import static no.javazone.androidapp.v1.util.LogUtils.LOGV;
import static no.javazone.androidapp.v1.util.LogUtils.makeLogTag;

public class ScheduleProvider extends ContentProvider {
    private static final String TAG = makeLogTag(ScheduleProvider.class);

    private ScheduleDatabase mOpenHelper;

    private ScheduleProviderUriMatcher mUriMatcher;

    /**
     * Providing important state information to be included in bug reports.
     *
     * !!! Remember !!! Any important data logged to {@code writer} shouldn't contain personally
     * identifiable information as it can be seen in bugreports.
     */
    @Override
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        Context context = getContext();
        try {
            // Calling append in multiple calls is typically better than creating net new strings to
            // pass to method invocations.
            writer.print("Last sync attempted: ");
            writer.println(new java.util.Date(SettingsUtils.getLastSyncAttemptedTime(context)));
            writer.print("Last sync successful: ");
            writer.println(new java.util.Date(SettingsUtils.getLastSyncSucceededTime(context)));
            writer.print("Current sync interval: ");
            writer.println(SettingsUtils.getCurSyncInterval(context));
            writer.print("Is an account active: ");
            writer.println(AccountUtils.hasActiveAccount(context));
            boolean canGetAuthToken = !TextUtils.isEmpty(AccountUtils.getAuthToken(context));
            writer.print("Can an auth token be retrieved: ");
            writer.println(canGetAuthToken);

        } catch (Exception exception) {
            writer.append("Exception while dumping state: ");
            exception.printStackTrace(writer);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ScheduleDatabase(getContext());
        mUriMatcher = new ScheduleProviderUriMatcher();
        return true;
    }

    private void deleteDatabase() {
        // TODO: wait for content provider operations to finish, then tear down
        mOpenHelper.close();
        Context context = getContext();
        ScheduleDatabase.deleteDatabase(context);
        mOpenHelper = new ScheduleDatabase(getContext());
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);
        return matchingUriEnum.contentType;
    }

    /**
     * Returns a tuple of question marks. For example, if {@code count} is 3, returns "(?,?,?)".
     */
    private String makeQuestionMarkTuple(int count) {
        if (count < 1) {
            return "()";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(?");
        for (int i = 1; i < count; i++) {
            stringBuilder.append(",?");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    /**
     * Adds the {@code tagsFilter} query parameter to the given {@code builder}. This query
     * parameter is used by the {@link com.google.samples.apps.iosched.explore.ExploreSessionsActivity}
     * when the user makes a selection containing multiple filters.
     */
    private void addTagsFilter(SelectionBuilder builder, String tagsFilter, String numCategories) {
        // Note: for context, remember that session queries are done on a join of sessions
        // and the sessions_tags relationship table, and are GROUP'ed BY the session ID.
        String[] requiredTags = tagsFilter.split(",");
        if (requiredTags.length == 0) {
            // filtering by 0 tags -- no-op
            return;
        } else if (requiredTags.length == 1) {
            // filtering by only one tag, so a simple WHERE clause suffices
            builder.where(Tags.TAG_ID + "=?", requiredTags[0]);
        } else {
            // Filtering by multiple tags, so we must add a WHERE clause with an IN operator,
            // and add a HAVING statement to exclude groups that fall short of the number
            // of required tags. For example, if requiredTags is { "X", "Y", "Z" }, and a certain
            // session only has tags "X" and "Y", it will be excluded by the HAVING statement.
            int categories = 1;
            if (numCategories != null && TextUtils.isDigitsOnly(numCategories)) {
                try {
                    categories = Integer.parseInt(numCategories);
                    LOGD(TAG, "Categories being used " + categories);
                } catch (Exception ex) {
                    LOGE(TAG, "exception parsing categories ", ex);
                }
            }
            String questionMarkTuple = makeQuestionMarkTuple(requiredTags.length);
            builder.where(Tags.TAG_ID + " IN " + questionMarkTuple, requiredTags);
            builder.having(
                    "COUNT(" + Qualified.SESSIONS_SESSION_ID + ") >= " + categories);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        String tagsFilter = uri.getQueryParameter(Sessions.QUERY_PARAMETER_TAG_FILTER);
        String categories = uri.getQueryParameter(Sessions.QUERY_PARAMETER_CATEGORIES);

        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);

        // Avoid the expensive string concatenation below if not loggable.
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            LOGV(TAG, "uri=" + uri + " code=" + matchingUriEnum.code + " proj=" +
                    Arrays.toString(projection) + " selection=" + selection + " args="
                    + Arrays.toString(selectionArgs) + ")");
        }

        switch (matchingUriEnum) {
            default: {
                // Most cases are handled with simple SelectionBuilder.
                final SelectionBuilder builder = buildExpandedSelection(uri, matchingUriEnum.code);

                // If a special filter was specified, try to apply it.
                if (!TextUtils.isEmpty(tagsFilter) && !TextUtils.isEmpty(categories)) {
                    addTagsFilter(builder, tagsFilter, categories);
                }

                boolean distinct = ScheduleContractHelper.isQueryDistinct(uri);

                Cursor cursor = builder
                        .where(selection, selectionArgs)
                        .query(db, distinct, projection, sortOrder, null);

                Context context = getContext();
                if (null != context) {
                    cursor.setNotificationUri(context.getContentResolver(), uri);
                }
                return cursor;
            }
            case SEARCH_SUGGEST: {
                final SelectionBuilder builder = new SelectionBuilder();

                // Adjust incoming query to become SQL text match.
                selectionArgs[0] = selectionArgs[0] + "%";
                builder.table(DatabaseTables.SEARCH_SUGGEST);
                builder.where(selection, selectionArgs);
                builder.map(SearchManager.SUGGEST_COLUMN_QUERY,
                        SearchManager.SUGGEST_COLUMN_TEXT_1);

                projection = new String[]{
                        BaseColumns._ID,
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_QUERY
                };

                final String limit = uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT);
                return builder.query(db, false, projection, SearchSuggest.DEFAULT_SORT, limit);
            }
            case SEARCH_TOPICS_SESSIONS: {
                if (selectionArgs == null || selectionArgs.length == 0) {
                    return createMergedSearchCursor(null, null);
                }
                String selectionArg = selectionArgs[0] == null ? "" : selectionArgs[0];
                // First we query the Tags table to find any tags that match the given query
                Cursor tags = query(Tags.CONTENT_URI, SearchTopicsSessions.TOPIC_TAG_PROJECTION,
                        SearchTopicsSessions.TOPIC_TAG_SELECTION,
                        new String[]{Config.Tags.CATEGORY_TRACK, selectionArg + "%"},
                        SearchTopicsSessions.TOPIC_TAG_SORT);
                // Then we query the sessions_search table and get a list of sessions that match
                // the given keywords.
                Cursor search = null;
                if (selectionArgs[0] != null) { // dont query if there was no selectionArg.
                    search = query(Sessions.buildSearchUri(selectionArg),
                            SearchTopicsSessions.SEARCH_SESSIONS_PROJECTION,
                            null, null,
                            Sessions.SORT_BY_TYPE_THEN_TIME);
                }
                // Now that we have two cursors, we merge the cursors and return a unified view
                // of the two result sets.
                return createMergedSearchCursor(tags, search);
            }
        }
    }

    /**
     * Create a {@link MatrixCursor} given the tags and search cursors.
     * @param tags Cursor with the projection {@link SearchTopicsSessions#TOPIC_TAG_PROJECTION}.
     * @param search Cursor with the projection
     *              {@link SearchTopicsSessions#SEARCH_SESSIONS_PROJECTION}.
     * @return Returns a MatrixCursor always with {@link SearchTopicsSessions#DEFAULT_PROJECTION}
     */
    private Cursor createMergedSearchCursor(Cursor tags, Cursor search) {
        // How big should our MatrixCursor be?
        int maxCount = (tags == null ? 0 : tags.getCount()) +
                (search == null ? 0 : search.getCount());

        MatrixCursor matrixCursor = new MatrixCursor(
                SearchTopicsSessions.DEFAULT_PROJECTION, maxCount);

        // Iterate over the tags cursor and add rows.
        if (tags != null && tags.moveToFirst()) {
            do {
                matrixCursor.addRow(
                        new Object[]{
                                tags.getLong(0),
                                tags.getString(1), /*tag_id*/
                                "{" + tags.getString(2) + "}", /*search_snippet*/
                                1}); /*is_topic_tag*/
            } while (tags.moveToNext());
        }
        // Iterate over the search cursor and add rows.
        if (search != null && search.moveToFirst()) {
            do {
                matrixCursor.addRow(
                        new Object[]{
                                search.getLong(0),
                                search.getString(1),
                                search.getString(2), /*search_snippet*/
                                0}); /*is_topic_tag*/
            } while (search.moveToNext());
        }
        return matrixCursor;
    }



    /** {@inheritDoc} */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        LOGV(TAG, "insert(uri=" + uri + ", values=" + values.toString()
                + ", account=" + getCurrentAccountName(uri, false) + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);
        if (matchingUriEnum.table != null) {
            try {
                db.insertOrThrow(matchingUriEnum.table, null, values);
                notifyChange(uri);
            } catch (SQLiteConstraintException exception) {
                // Leaving this here as it's handy to to breakpoint on this throw when debugging a
                // bootstrap file issue.
                throw exception;
            }
        }

        switch (matchingUriEnum) {
            case BLOCKS: {
                return Blocks.buildBlockUri(values.getAsString(Blocks.BLOCK_ID));
            }
            case CARDS: {
                return ScheduleContract.Cards.buildCardUri(values.getAsString(
                        ScheduleContract.Cards.CARD_ID));
            }
            case TAGS: {
                return Tags.buildTagUri(values.getAsString(Tags.TAG_ID));
            }
            case ROOMS: {
                return Rooms.buildRoomUri(values.getAsString(Rooms.ROOM_ID));
            }
            case SESSIONS: {
                return Sessions.buildSessionUri(values.getAsString(Sessions.SESSION_ID));
            }
            case SESSIONS_ID_SPEAKERS: {
                return Speakers.buildSpeakerUri(values.getAsString(SessionsSpeakers.SPEAKER_ID));
            }
            case SESSIONS_ID_TAGS: {
                return Tags.buildTagUri(values.getAsString(Tags.TAG_ID));
            }
            case SPEAKERS: {
                return Speakers.buildSpeakerUri(values.getAsString(Speakers.SPEAKER_ID));
            }
            case SEARCH_SUGGEST: {
                return SearchSuggest.CONTENT_URI;
            }
            case MAPMARKERS: {
                return MapMarkers.buildMarkerUri(values.getAsString(MapMarkers.MARKER_ID));
            }
            case MAPTILES: {
                return MapTiles.buildFloorUri(values.getAsString(MapTiles.TILE_FLOOR));
            }
            case FEEDBACK_FOR_SESSION: {
                return Feedback.buildFeedbackUri(values.getAsString(Feedback.SESSION_ID));
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String accountName = getCurrentAccountName(uri, false);
        LOGV(TAG, "update(uri=" + uri + ", values=" + values.toString()
                + ", account=" + accountName + ")");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);
        if (matchingUriEnum == ScheduleUriEnum.SEARCH_INDEX) {
            // update the search index
            updateSessionSearchIndex(db);
            return 1;
        }

        final SelectionBuilder builder = buildSimpleSelection(uri);

        int retVal = builder.where(selection, selectionArgs).update(db, values);
        notifyChange(uri);
        return retVal;
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String accountName = getCurrentAccountName(uri, false);
        LOGV(TAG, "delete(uri=" + uri + ", account=" + accountName + ")");
        if (uri == BASE_CONTENT_URI) {
            // Handle whole database deletes (e.g. when signing out)
            deleteDatabase();
            notifyChange(uri);
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);

        int retVal = builder.where(selection, selectionArgs).delete(db);
        notifyChange(uri);
        return retVal;
    }

    private void notifyChange(Uri uri) {
        if (!ScheduleContractHelper.isUriCalledFromSyncAdapter(uri)) {
            Context context = getContext();
            context.getContentResolver().notifyChange(uri, null);

            // Widgets can't register content observers so we refresh widgets separately.
            context.sendBroadcast(ScheduleWidgetProvider.getRefreshBroadcastIntent(context, false));
        }
    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);
        // The main Uris, corresponding to the root of each type of Uri, do not have any selection
        // criteria so the full table is used. The others apply a selection criteria.
        switch (matchingUriEnum) {
            case BLOCKS:
            case CARDS:
            case TAGS:
            case ROOMS:
            case SESSIONS:
            case SPEAKERS:
            case MAPMARKERS:
            case MAPTILES:
            case SEARCH_SUGGEST:
                return builder.table(matchingUriEnum.table);
            case BLOCKS_ID: {
                final String blockId = Blocks.getBlockId(uri);
                return builder.table(DatabaseTables.BLOCKS)
                        .where(Blocks.BLOCK_ID + "=?", blockId);
            }
            case TAGS_ID: {
                final String tagId = Tags.getTagId(uri);
                return builder.table(DatabaseTables.TAGS)
                        .where(Tags.TAG_ID + "=?", tagId);
            }
            case ROOMS_ID: {
                final String roomId = Rooms.getRoomId(uri);
                return builder.table(DatabaseTables.ROOMS)
                        .where(Rooms.ROOM_ID + "=?", roomId);
            }
            case SESSIONS_ID: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(DatabaseTables.SESSIONS)
                        .where(Sessions.SESSION_ID + "=?", sessionId);
            }
            case SESSIONS_ID_SPEAKERS: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(DatabaseTables.SESSIONS_SPEAKERS)
                        .where(Sessions.SESSION_ID + "=?", sessionId);
            }
            case SESSIONS_ID_TAGS: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(DatabaseTables.SESSIONS_TAGS)
                        .where(Sessions.SESSION_ID + "=?", sessionId);
            }
            case SPEAKERS_ID: {
                final String speakerId = Speakers.getSpeakerId(uri);
                return builder.table(DatabaseTables.SPEAKERS)
                        .where(Speakers.SPEAKER_ID + "=?", speakerId);
            }
            case MAPMARKERS_FLOOR: {
                final String floor = MapMarkers.getMarkerFloor(uri);
                return builder.table(DatabaseTables.MAPMARKERS)
                        .where(MapMarkers.MARKER_FLOOR + "=?", floor);
            }
            case MAPMARKERS_ID: {
                final String markerId = MapMarkers.getMarkerId(uri);
                return builder.table(DatabaseTables.MAPMARKERS)
                        .where(MapMarkers.MARKER_ID + "=?", markerId);
            }
            case MAPTILES_FLOOR: {
                final String floor = MapTiles.getFloorId(uri);
                return builder.table(DatabaseTables.MAPTILES)
                        .where(MapTiles.TILE_FLOOR + "=?", floor);
            }
            case FEEDBACK_FOR_SESSION: {
                final String session_id = Feedback.getSessionId(uri);
                return builder.table(DatabaseTables.FEEDBACK)
                        .where(Feedback.SESSION_ID + "=?", session_id);
            }
            case FEEDBACK_ALL: {
                return builder.table(DatabaseTables.FEEDBACK);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for " + uri);
            }
        }
    }

    private String getCurrentAccountName(Uri uri, boolean sanitize) {
        String accountName = ScheduleContractHelper.getOverrideAccountName(uri);
        if (accountName == null) {
            accountName = AccountUtils.getActiveAccountName(getContext());
        }
        if (sanitize) {
            // sanitize accountName when concatenating (http://xkcd.com/327/)
            accountName = (accountName != null) ? accountName.replace("'", "''") : null;
        }
        return accountName;
    }

    /**
     * Build an advanced {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually only used by {@link #query}, since it
     * performs table joins useful for {@link Cursor} data.
     */
    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchCode(match);
        if (matchingUriEnum == null) {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        switch (matchingUriEnum) {
            case BLOCKS: {
                return builder.table(DatabaseTables.BLOCKS)
                        .map(Blocks.SESSIONS_COUNT, Subquery.BLOCK_SESSIONS_COUNT)
                        .map(Blocks.NUM_STARRED_SESSIONS, Subquery.BLOCK_NUM_STARRED_SESSIONS)
                        .map(Blocks.STARRED_SESSION_ID, Subquery.BLOCK_STARRED_SESSION_ID)
                        .map(Blocks.STARRED_SESSION_TITLE, Subquery.BLOCK_STARRED_SESSION_TITLE)
                        .map(Blocks.STARRED_SESSION_ROOM_NAME,
                                Subquery.BLOCK_STARRED_SESSION_ROOM_NAME)
                        .map(Blocks.STARRED_SESSION_ROOM_ID, Subquery.BLOCK_STARRED_SESSION_ROOM_ID);
            }
            case BLOCKS_BETWEEN: {
                final List<String> segments = uri.getPathSegments();
                final String startTime = segments.get(2);
                final String endTime = segments.get(3);
                return builder.table(DatabaseTables.BLOCKS)
                        .map(Blocks.SESSIONS_COUNT, Subquery.BLOCK_SESSIONS_COUNT)
                        .map(Blocks.NUM_STARRED_SESSIONS, Subquery.BLOCK_NUM_STARRED_SESSIONS)
                        .where(Blocks.BLOCK_START + ">=?", startTime)
                        .where(Blocks.BLOCK_START + "<=?", endTime);
            }
            case BLOCKS_ID: {
                final String blockId = Blocks.getBlockId(uri);
                return builder.table(DatabaseTables.BLOCKS)
                        .map(Blocks.SESSIONS_COUNT, Subquery.BLOCK_SESSIONS_COUNT)
                        .map(Blocks.NUM_STARRED_SESSIONS, Subquery.BLOCK_NUM_STARRED_SESSIONS)
                        .where(Blocks.BLOCK_ID + "=?", blockId);
            }
            case BLOCKS_ID_SESSIONS: {
                final String blockId = Blocks.getBlockId(uri);
                return builder.table(DatabaseTables.SESSIONS_JOIN_BLOCKS_ROOMS)
                        .map(Blocks.SESSIONS_COUNT, Subquery.BLOCK_SESSIONS_COUNT)
                        .map(Blocks.NUM_STARRED_SESSIONS, Subquery.BLOCK_NUM_STARRED_SESSIONS)
                        .mapToTable(Sessions._ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.BLOCK_ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, DatabaseTables.SESSIONS)
                        .where(Qualified.SESSIONS_BLOCK_ID + "=?", blockId);
            }
            case BLOCKS_ID_SESSIONS_STARRED: {
                final String blockId = Blocks.getBlockId(uri);
                return builder.table(DatabaseTables.SESSIONS_JOIN_BLOCKS_ROOMS)
                        .map(Blocks.SESSIONS_COUNT, Subquery.BLOCK_SESSIONS_COUNT)
                        .map(Blocks.NUM_STARRED_SESSIONS, Subquery.BLOCK_NUM_STARRED_SESSIONS)
                        .mapToTable(Sessions._ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.BLOCK_ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, DatabaseTables.SESSIONS)
                        .where(Qualified.SESSIONS_BLOCK_ID + "=?", blockId)
                        .where(Qualified.SESSIONS_STARRED + "=1");
            }
            case CARDS: {
                return builder.table(DatabaseTables.CARDS);
            }
            case TAGS: {
                return builder.table(DatabaseTables.TAGS);
            }
            case TAGS_ID: {
                final String tagId = Tags.getTagId(uri);
                return builder.table(DatabaseTables.TAGS)
                        .where(Tags.TAG_ID + "=?", tagId);
            }
            case ROOMS: {
                return builder.table(DatabaseTables.ROOMS);
            }
            case ROOMS_ID: {
                final String roomId = Rooms.getRoomId(uri);
                return builder.table(DatabaseTables.ROOMS)
                        .where(Rooms.ROOM_ID + "=?", roomId);
            }
            case ROOMS_ID_SESSIONS: {
                final String roomId = Rooms.getRoomId(uri);
                return builder.table(DatabaseTables.SESSIONS_JOIN_ROOMS, getCurrentAccountName(uri, true))
                        .mapToTable(Sessions._ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, DatabaseTables.SESSIONS)
                        .where(Qualified.SESSIONS_ROOM_ID + "=?", roomId)
                        .groupBy(Qualified.SESSIONS_SESSION_ID);
            }
            case SESSIONS: {
                // We query sessions on the joined table of sessions with rooms and tags.
                // Since there may be more than one tag per session, we GROUP BY session ID.
                // The starred sessions ("my schedule") are associated with a user, so we
                // use the current user to select them properly
                return builder
                        .table(DatabaseTables.SESSIONS_JOIN_ROOMS_TAGS, getCurrentAccountName(uri, true))
                        .mapToTable(Sessions._ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, DatabaseTables.SESSIONS)
                        .map(Sessions.SESSION_STARRED, "IFNULL(session_starred, 0)")
                        .groupBy(Qualified.SESSIONS_SESSION_ID);
            }
            case SESSIONS_MY_SCHEDULE: {
                return builder.table(DatabaseTables.SESSIONS_JOIN_ROOMS_TAGS_FEEDBACK_MYSCHEDULE,
                        getCurrentAccountName(uri, true))
                        .mapToTable(Sessions._ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, DatabaseTables.SESSIONS)
                        .map(Sessions.HAS_GIVEN_FEEDBACK, Subquery.SESSION_HAS_GIVEN_FEEDBACK)
                        .map(Sessions.SESSION_STARRED, "IFNULL(session_starred, 0)")
                        .where("( " + Sessions.SESSION_STARRED + "=1")
                        .groupBy(Qualified.SESSIONS_SESSION_ID);
            }
            case SESSIONS_UNSCHEDULED: {
                final long[] interval = Sessions.getInterval(uri);
                return builder.table(DatabaseTables.SESSIONS_JOIN_ROOMS_TAGS_FEEDBACK_MYSCHEDULE,
                        getCurrentAccountName(uri, true))
                        .mapToTable(Sessions._ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, DatabaseTables.SESSIONS)
                        .map(Sessions.SESSION_STARRED, "IFNULL(session_starred, 0)")
                        .where(Sessions.SESSION_STARRED + "=0")
                        .where(Sessions.SESSION_START + ">=?", String.valueOf(interval[0]))
                        .where(Sessions.SESSION_START + "<?", String.valueOf(interval[1]))
                        .groupBy(Qualified.SESSIONS_SESSION_ID);
            }
            case SESSIONS_SEARCH: {
                final String query = Sessions.getSearchQuery(uri);
                return builder.table(DatabaseTables.SESSIONS_SEARCH_JOIN_SESSIONS_ROOMS,
                        getCurrentAccountName(uri, true))
                        .map(Sessions.SEARCH_SNIPPET, Subquery.SESSIONS_SNIPPET)
                        .mapToTable(Sessions._ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, DatabaseTables.SESSIONS)
                        .map(Sessions.SESSION_STARRED, "IFNULL(session_starred, 0)")
                        .where(SessionsSearchColumns.BODY + " MATCH ?", query);
            }
            case SESSIONS_AT: {
                final List<String> segments = uri.getPathSegments();
                final String time = segments.get(2);
                return builder.table(DatabaseTables.SESSIONS_JOIN_ROOMS, getCurrentAccountName(uri, true))
                        .mapToTable(Sessions._ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, DatabaseTables.SESSIONS)
                        .where(Sessions.SESSION_START + "<=?", time)
                        .where(Sessions.SESSION_END + ">=?", time);
            }
            case SESSIONS_ID: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(DatabaseTables.SESSIONS_JOIN_ROOMS, getCurrentAccountName(uri, true))
                        .mapToTable(Sessions._ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, DatabaseTables.SESSIONS)
                        .map(Sessions.SESSION_STARRED, "IFNULL(session_starred, 0)")
                        .where(Qualified.SESSIONS_SESSION_ID + "=?", sessionId);
            }
            case SESSIONS_ID_SPEAKERS: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(DatabaseTables.SESSIONS_SPEAKERS_JOIN_SPEAKERS)
                        .mapToTable(Speakers._ID, DatabaseTables.SPEAKERS)
                        .mapToTable(Speakers.SPEAKER_ID, DatabaseTables.SPEAKERS)
                        .where(Qualified.SESSIONS_SPEAKERS_SESSION_ID + "=?", sessionId);
            }
            case SESSIONS_ID_TAGS: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(DatabaseTables.SESSIONS_TAGS_JOIN_TAGS)
                        .mapToTable(Tags._ID, DatabaseTables.TAGS)
                        .mapToTable(Tags.TAG_ID, DatabaseTables.TAGS)
                        .where(Qualified.SESSIONS_TAGS_SESSION_ID + "=?", sessionId);
            }

            case SESSIONS_ROOM_AFTER: {
                final String room = Sessions.getRoom(uri);
                final String time = Sessions.getAfterForRoom(uri);
                return builder.table(DatabaseTables.SESSIONS_JOIN_ROOMS_TAGS, getCurrentAccountName(uri, true))
                        .mapToTable(Sessions._ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, DatabaseTables.SESSIONS)
                        .where(Qualified.SESSIONS_ROOM_ID + "=?", room)
                        .where("(" + Sessions.SESSION_START + "<= ? AND " + Sessions.SESSION_END +
                                        " >= ?) OR (" + Sessions.SESSION_START + " >= ?)", time,
                                time,
                                time)
                        .map(Sessions.SESSION_STARRED, "IFNULL(session_starred, 0)")
                        .groupBy(Qualified.SESSIONS_SESSION_ID);
            }
            case SESSIONS_AFTER: {
                final String time = Sessions.getAfter(uri);
                return builder.table(DatabaseTables.SESSIONS_JOIN_ROOMS_TAGS, getCurrentAccountName(uri, true))
                        .mapToTable(Sessions._ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, DatabaseTables.SESSIONS)
                        .map(Sessions.SESSION_STARRED, "IFNULL(session_starred, 0)")
                        .where("(" + Sessions.SESSION_START + "<= ? AND " + Sessions.SESSION_END +
                                        " >= ?) OR (" + Sessions.SESSION_START + " >= ?)", time,
                                time, time)
                        .groupBy(Qualified.SESSIONS_SESSION_ID);
            }
            case SPEAKERS: {
                return builder.table(DatabaseTables.SPEAKERS);
            }
            case SPEAKERS_ID: {
                final String speakerId = Speakers.getSpeakerId(uri);
                return builder.table(DatabaseTables.SPEAKERS)
                        .where(Speakers.SPEAKER_ID + "=?", speakerId);
            }
            case SPEAKERS_ID_SESSIONS: {
                final String speakerId = Speakers.getSpeakerId(uri);
                return builder.table(DatabaseTables.SESSIONS_SPEAKERS_JOIN_SESSIONS_ROOMS)
                        .mapToTable(Sessions._ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, DatabaseTables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, DatabaseTables.SESSIONS)
                        .where(Qualified.SESSIONS_SPEAKERS_SPEAKER_ID + "=?", speakerId);
            }
            case MAPMARKERS: {
                return builder.table(DatabaseTables.MAPMARKERS);
            }
            case MAPMARKERS_FLOOR: {
                final String floor = MapMarkers.getMarkerFloor(uri);
                return builder.table(DatabaseTables.MAPMARKERS)
                        .where(MapMarkers.MARKER_FLOOR + "=?", floor);
            }
            case MAPMARKERS_ID: {
                final String roomId = MapMarkers.getMarkerId(uri);
                return builder.table(DatabaseTables.MAPMARKERS)
                        .where(MapMarkers.MARKER_ID + "=?", roomId);
            }
            case MAPTILES: {
                return builder.table(DatabaseTables.MAPTILES);
            }
            case MAPTILES_FLOOR: {
                final String floor = MapTiles.getFloorId(uri);
                return builder.table(DatabaseTables.MAPTILES)
                        .where(MapTiles.TILE_FLOOR + "=?", floor);
            }
            case FEEDBACK_FOR_SESSION: {
                final String sessionId = Feedback.getSessionId(uri);
                return builder.table(DatabaseTables.FEEDBACK)
                        .where(Feedback.SESSION_ID + "=?", sessionId);
            }
            case FEEDBACK_ALL: {
                return builder.table(DatabaseTables.FEEDBACK);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        throw new UnsupportedOperationException("openFile is not supported for " + uri);
    }

    private interface Subquery {
        String SESSION_HAS_GIVEN_FEEDBACK = "(SELECT COUNT(1) FROM "
                + DatabaseTables.FEEDBACK + " WHERE " + Qualified.FEEDBACK_SESSION_ID + "="
                + Qualified.SESSIONS_SESSION_ID + ")";

        String BLOCK_SESSIONS_COUNT = "(SELECT COUNT(" + Qualified.SESSIONS_SESSION_ID + ") FROM "
                + DatabaseTables.SESSIONS + " WHERE " + Qualified.SESSIONS_BLOCK_ID + "="
                + Qualified.BLOCKS_BLOCK_ID + ")";

        String BLOCK_NUM_STARRED_SESSIONS = "(SELECT COUNT(1) FROM "
                + DatabaseTables.SESSIONS + " WHERE " + Qualified.SESSIONS_BLOCK_ID + "="
                + Qualified.BLOCKS_BLOCK_ID + " AND " + Qualified.SESSIONS_STARRED + "=1)";

        String BLOCK_STARRED_SESSION_ID = "(SELECT " + Qualified.SESSIONS_SESSION_ID + " FROM "
                + DatabaseTables.SESSIONS + " WHERE " + Qualified.SESSIONS_BLOCK_ID + "="
                + Qualified.BLOCKS_BLOCK_ID + " AND " + Qualified.SESSIONS_STARRED + "=1 "
                + "ORDER BY " + Qualified.SESSIONS_TITLE + ")";

        String BLOCK_STARRED_SESSION_TITLE = "(SELECT " + Qualified.SESSIONS_TITLE + " FROM "
                + DatabaseTables.SESSIONS + " WHERE " + Qualified.SESSIONS_BLOCK_ID + "="
                + Qualified.BLOCKS_BLOCK_ID + " AND " + Qualified.SESSIONS_STARRED + "=1 "
                + "ORDER BY " + Qualified.SESSIONS_TITLE + ")";

        String BLOCK_STARRED_SESSION_ROOM_NAME = "(SELECT " + Qualified.ROOMS_ROOM_NAME + " FROM "
                + DatabaseTables.SESSIONS_JOIN_ROOMS + " WHERE " + Qualified.SESSIONS_BLOCK_ID + "="
                + Qualified.BLOCKS_BLOCK_ID + " AND " + Qualified.SESSIONS_STARRED + "=1 "
                + "ORDER BY " + Qualified.SESSIONS_TITLE + ")";

        String BLOCK_STARRED_SESSION_ROOM_ID = "(SELECT " + Qualified.ROOMS_ROOM_ID + " FROM "
                + DatabaseTables.SESSIONS_JOIN_ROOMS + " WHERE " + Qualified.SESSIONS_BLOCK_ID + "="
                + Qualified.BLOCKS_BLOCK_ID + " AND " + Qualified.SESSIONS_STARRED + "=1 "
                + "ORDER BY " + Qualified.SESSIONS_TITLE + ")";

        String SESSIONS_SNIPPET = "snippet(" + DatabaseTables.SESSIONS_SEARCH + ",'{','}','\u2026')";
    }

    /**
     * {@link ScheduleContract} fields that are fully qualified with a specific
     * parent {@link Tables}. Used when needed to work around SQL ambiguity.
     */
    private interface Qualified {
        String SESSIONS_STARRED = DatabaseTables.SESSIONS + "." + Sessions.SESSION_STARRED;
        String SESSIONS_BLOCK_ID = DatabaseTables.SESSIONS + "." + Sessions.BLOCK_ID;

        String SESSIONS_SESSION_ID = DatabaseTables.SESSIONS + "." + Sessions.SESSION_ID;
        String SESSIONS_ROOM_ID = DatabaseTables.SESSIONS + "." + Sessions.ROOM_ID;
        String SESSIONS_TAGS_SESSION_ID = DatabaseTables.SESSIONS_TAGS + "."
                + SessionsTags.SESSION_ID;

        String SESSIONS_SPEAKERS_SESSION_ID = DatabaseTables.SESSIONS_SPEAKERS + "."
                + SessionsSpeakers.SESSION_ID;

        String SESSIONS_SPEAKERS_SPEAKER_ID = DatabaseTables.SESSIONS_SPEAKERS + "."
                + SessionsSpeakers.SPEAKER_ID;

        String FEEDBACK_SESSION_ID = DatabaseTables.FEEDBACK + "." + Feedback.SESSION_ID;


        String SESSIONS_TITLE = DatabaseTables.SESSIONS + "." + Sessions.SESSION_TITLE;


        String ROOMS_ROOM_NAME = DatabaseTables.ROOMS + "." + Rooms.ROOM_NAME;
        String ROOMS_ROOM_ID = DatabaseTables.ROOMS + "." + Rooms.ROOM_ID;

        String BLOCKS_BLOCK_ID = DatabaseTables.BLOCKS + "." + Blocks.BLOCK_ID;
    }
}