package no.javazone.database;

import android.app.SearchManager;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.text.format.DateUtils;

import java.util.List;

import no.javazone.util.ParserUtils;

public class ScheduleContract {
    public static final String CONTENT_AUTHORITY = "no.javazone";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String CONTENT_TYPE_APP_BASE = "no.javazone.";

    public static final String CONTENT_TYPE_BASE = "vnd.android.cursor.dir/vnd."
            + CONTENT_TYPE_APP_BASE;

    public static final String CONTENT_ITEM_TYPE_BASE = "vnd.android.cursor.item/vnd."
            + CONTENT_TYPE_APP_BASE;


    public interface SyncColumns {
        String UPDATED = "updated";
    }

    interface BlocksColumns {
        String BLOCK_ID = "block_id";
        String BLOCK_TITLE = "block_title";
        String BLOCK_START = "block_start";
        String BLOCK_END = "block_end";
        String BLOCK_TYPE = "block_type";
        String BLOCK_SUBTITLE = "block_subtitle";
    }

    interface TagsColumns {
        String TAG_ID = "tag_id";
        String TAG_CATEGORY = "tag_category";
        String TAG_NAME = "tag_name";
        String TAG_ORDER_IN_CATEGORY = "tag_order_in_category";
        String TAG_COLOR = "tag_color";
        String TAG_ABSTRACT = "tag_abstract";
        String TAG_PHOTO_URL = "tag_photo_url";
    }

    interface RoomsColumns {
        String ROOM_ID = "room_id";
        String ROOM_NAME = "room_name";
        String ROOM_FLOOR = "room_floor";
    }

    interface SessionsColumns {
        String SESSION_ID = "session_id";
        String SESSION_LEVEL = "session_level";
        String SESSION_START = "session_start";
        String SESSION_END = "session_end";
        String SESSION_TITLE = "session_title";
        String SESSION_ABSTRACT = "session_abstract";
        String SESSION_KEYWORDS = "session_keywords";
        String SESSION_VIDEO_URL = "session_video_url";
        String SESSION_TAGS = "session_tags";
        String SESSION_SPEAKER_NAMES = "session_speaker_names";
        String SESSION_GROUPING_ORDER = "session_grouping_order";
        String SESSION_COLOR = "session_color";
        String SESSION_INTERVAL_COUNT = "session_interval_count";
        String SESSION_RELATED_CONTENT = "session_related_content";
        String SESSION_IMPORT_HASHCODE = "session_import_hashcode";
        String SESSION_STARRED = "session_starred";
    }

    interface SpeakersColumns {
        String SPEAKER_ID = "speaker_id";
        String SPEAKER_NAME = "speaker_name";
        String SPEAKER_IMAGE_URL = "speaker_image_url";
        String SPEAKER_COMPANY = "speaker_company";
        String SPEAKER_ABSTRACT = "speaker_abstract";
        String SPEAKER_URL = "speaker_url";
        String SPEAKER_PLUSONE_URL = "plusone_url";
        String SPEAKER_TWITTER_URL = "twitter_url";
        String SPEAKER_IMPORT_HASHCODE = "speaker_import_hashcode";

    }

    interface CardsColumns {
        String CARD_ID = "card_id";
        String TITLE = "title";
        String ACTION_URL = "action_url";
        String DISPLAY_START_DATE = "start_date";
        String DISPLAY_END_DATE = "end_date";
        String MESSAGE = "message";
        String BACKGROUND_COLOR = "bg_color";
        String TEXT_COLOR = "text_color";
        String ACTION_COLOR = "action_color";
        String ACTION_TEXT = "action_text";
        String ACTION_TYPE = "action_type";
        String ACTION_EXTRA = "action_extra";
    }

    interface MapMarkerColumns {
        String MARKER_ID = "map_marker_id";
        String MARKER_TYPE = "map_marker_type";
        String MARKER_LATITUDE = "map_marker_latitude";
        String MARKER_LONGITUDE = "map_marker_longitude";
        String MARKER_LABEL = "map_marker_label";
        String MARKER_FLOOR = "map_marker_floor";
    }

    interface FeedbackColumns {
        String SESSION_ID = "session_id";
        String SESSION_RATING = "feedback_session_rating";
        String ANSWER_RELEVANCE = "feedback_answer_q1";
        String ANSWER_CONTENT = "feedback_answer_q2";
        String ANSWER_SPEAKER = "feedback_answer_q3";
        String COMMENTS = "feedback_comments";
        String SYNCED = "synced";
    }

    interface MapTileColumns {
        String TILE_FLOOR = "map_tile_floor";
        String TILE_FILE = "map_tile_file";
        String TILE_URL = "map_tile_url";
    }

    private static final String PATH_BLOCKS = "blocks";

    private static final String PATH_AFTER = "after";

    private static final String PATH_TAGS = "tags";

    private static final String PATH_ROOM = "room";

    private static final String PATH_CARDS = "cards";

    private static final String PATH_UNSCHEDULED = "unscheduled";

    private static final String PATH_ROOMS = "rooms";

    private static final String PATH_SESSIONS = "sessions";

    private static final String PATH_STARRED = "starred";

    private static final String PATH_FEEDBACK = "feedback";

    private static final String PATH_SESSIONS_COUNTER = "counter";

    private static final String PATH_SPEAKERS = "speakers";

    private static final String PATH_MAP_MARKERS = "mapmarkers";

    private static final String PATH_MAP_FLOOR = "floor";

    private static final String PATH_MAP_TILES = "maptiles";

    private static final String PATH_SEARCH = "search";

    private static final String PATH_SEARCH_SUGGEST = "search_suggest_query";

    private static final String PATH_SEARCH_INDEX = "search_index";

    public static final String[] TOP_LEVEL_PATHS = {
            PATH_BLOCKS,
            PATH_TAGS,
            PATH_ROOMS,
            PATH_SESSIONS,
            PATH_FEEDBACK,
            PATH_SPEAKERS,
            PATH_MAP_MARKERS,
            PATH_MAP_FLOOR,
            PATH_MAP_MARKERS,
            PATH_MAP_TILES
    };

    public static final String[] USER_DATA_RELATED_PATHS = {
            PATH_SESSIONS,
    };

    public static String makeContentType(String id) {
        if (id != null) {
            return CONTENT_TYPE_BASE + id;
        } else {
            return null;
        }
    }

    public static String makeContentItemType(String id) {
        if (id != null) {
            return CONTENT_ITEM_TYPE_BASE + id;
        } else {
            return null;
        }
    }

    public static class Blocks implements BlocksColumns, BaseColumns {
        public static final String BLOCK_TYPE_FREE = "free";
        public static final String BLOCK_TYPE_BREAK = "break";
        public static final String BLOCK_TYPE_KEYNOTE = "keynote";

        public static final boolean isValidBlockType(String type) {
            return BLOCK_TYPE_FREE.equals(type) || BLOCK_TYPE_BREAK.equals(type)
                    || BLOCK_TYPE_KEYNOTE.equals(type);
        }

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BLOCKS).build();

        public static final String CONTENT_TYPE_ID = "block";

        public static Uri buildBlockUri(String blockId) {
            return CONTENT_URI.buildUpon().appendPath(blockId).build();
        }

        public static String getBlockId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * Generate a {@link #BLOCK_ID} that will always match the requested
         * {@link Blocks} details.
         *
         * @param startTime the block start time, in milliseconds since Epoch UTC
         * @param endTime   the block end time, in milliseconds since Epoch UTF
         */
        public static String generateBlockId(long startTime, long endTime) {
            startTime /= DateUtils.SECOND_IN_MILLIS;
            endTime /= DateUtils.SECOND_IN_MILLIS;
            return ParserUtils.sanitizeId(startTime + "-" + endTime);
        }
    }

    /**
     * Cards are presented on the Explore I/O screen.
     */
    public static class Cards implements CardsColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CARDS).build();

        public static final String CONTENT_TYPE_ID = "cards";

        /**
         * Build {@link Uri} that references any {@link Cards}.
         */
        public static Uri buildCardsUri() {
            return CONTENT_URI.buildUpon().appendPath(PATH_CARDS).build();
        }

        /** Build {@link Uri} for requested {@link #CARD_ID}. */
        public static Uri buildCardUri(String cardId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_CARDS).appendPath(cardId).build();
        }
    }

    public static class Tags implements TagsColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TAGS).build();

        public static final String CONTENT_TYPE_ID = "tag";

        public static Uri buildTagsUri() {
            return CONTENT_URI;
        }

        public static Uri buildTagUri(String tagId) {
            return CONTENT_URI.buildUpon().appendPath(tagId).build();
        }

        public static String getTagId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Rooms implements RoomsColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROOMS).build();

        public static final String CONTENT_TYPE_ID = "room";

        public static Uri buildRoomUri(String roomId) {
            return CONTENT_URI.buildUpon().appendPath(roomId).build();
        }

        public static Uri buildSessionsDirUri(String roomId) {
            return CONTENT_URI.buildUpon().appendPath(roomId).appendPath(PATH_SESSIONS).build();
        }

        public static String getRoomId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Feedback implements BaseColumns, FeedbackColumns, SyncColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FEEDBACK).build();

        public static final String CONTENT_TYPE_ID = "session_feedback";

        public static Uri buildFeedbackUri(String sessionId) {
            return CONTENT_URI.buildUpon().appendPath(sessionId).build();
        }

        public static String getSessionId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Sessions implements SessionsColumns, RoomsColumns,
            SyncColumns, BaseColumns {

        public static final String QUERY_PARAMETER_TAG_FILTER = "filter";
        public static final String QUERY_PARAMETER_CATEGORIES = "categories";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SESSIONS).build();

        public static final Uri CONTENT_STARRED_URI =
                CONTENT_URI.buildUpon().appendPath(PATH_STARRED).build();

        public static final String CONTENT_TYPE_ID = "session";

        public static final String ROOM_ID = "room_id";

        public static final String SEARCH_SNIPPET = "search_snippet";

        public static final String HAS_GIVEN_FEEDBACK = "has_given_feedback";

        public static final String SORT_BY_TYPE_THEN_TIME = SESSION_GROUPING_ORDER + " ASC,"
                + SESSION_START + " ASC," + SESSION_TITLE + " COLLATE NOCASE ASC";

        public static final String STARTING_AT_TIME_INTERVAL_SELECTION =
                SESSION_START + " >= ? and " + SESSION_START + " <= ?";

        public static final String AT_TIME_SELECTION =
                SESSION_START + " <= ? and " + SESSION_END + " >= ?";

        public static String[] buildAtTimeIntervalArgs(long intervalStart, long intervalEnd) {
            return new String[]{String.valueOf(intervalStart), String.valueOf(intervalEnd)};
        }

        public static String[] buildAtTimeSelectionArgs(long time) {
            final String timeString = String.valueOf(time);
            return new String[]{timeString, timeString};
        }

        public static String[] buildUpcomingSelectionArgs(long minTime) {
            return new String[]{String.valueOf(minTime)};
        }

        public static Uri buildSessionUri(String sessionId) {
            return CONTENT_URI.buildUpon().appendPath(sessionId).build();
        }

        public static Uri buildSpeakersDirUri(String sessionId) {
            return CONTENT_URI.buildUpon().appendPath(sessionId).appendPath(PATH_SPEAKERS).build();
        }

        public static Uri buildTagsDirUri(String sessionId) {
            return CONTENT_URI.buildUpon().appendPath(sessionId).appendPath(PATH_TAGS).build();
        }

        public static Uri buildSearchUri(String query) {
            if (null == query) {
                query = "";
            }
            query = query.replaceAll(" +", " *") + "*";
            return CONTENT_URI.buildUpon()
                    .appendPath(PATH_SEARCH).appendPath(query).build();
        }

        public static boolean isSearchUri(Uri uri) {
            List<String> pathSegments = uri.getPathSegments();
            return pathSegments.size() >= 2 && PATH_SEARCH.equals(pathSegments.get(1));
        }

        public static Uri buildSessionsInRoomAfterUri(String room, long time) {
            return CONTENT_URI.buildUpon().appendPath(PATH_ROOM).appendPath(room)
                    .appendPath(PATH_AFTER)
                    .appendPath(String.valueOf(time)).build();
        }

        public static Uri buildSessionsAfterUri(long time) {
            return CONTENT_URI.buildUpon().appendPath(PATH_AFTER)
                    .appendPath(String.valueOf(time)).build();
        }

        public static Uri buildUnscheduledSessionsInInterval(long start, long end) {
            String interval = start + "-" + end;
            return CONTENT_URI.buildUpon().appendPath(PATH_UNSCHEDULED).appendPath(interval)
                    .build();
        }

        public static boolean isUnscheduledSessionsInInterval(Uri uri) {
            return uri != null && uri.toString().startsWith(
                    CONTENT_URI.buildUpon().appendPath(PATH_UNSCHEDULED).toString());
        }

        public static long[] getInterval(Uri uri) {
            if (uri == null) {
                return null;
            }
            List<String> segments = uri.getPathSegments();
            if (segments.size() == 3 && segments.get(2).indexOf('-') > 0) {
                String[] interval = segments.get(2).split("-");
                return new long[]{Long.parseLong(interval[0]), Long.parseLong(interval[1])};
            }
            return null;
        }

        public static String getRoom(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getAfterForRoom(Uri uri) {
            return uri.getPathSegments().get(4);
        }

        public static String getAfter(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getSessionId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getSearchQuery(Uri uri) {
            List<String> segments = uri.getPathSegments();
            if (2 < segments.size()) {
                return segments.get(2);
            }
            return null;
        }

        public static boolean hasFilterParam(Uri uri) {
            return uri != null && uri.getQueryParameter(QUERY_PARAMETER_TAG_FILTER) != null;
        }

        @Deprecated
        public static Uri buildTagFilterUri(Uri contentUri, String[] requiredTags) {
            return buildCategoryTagFilterUri(contentUri, requiredTags,
                    requiredTags == null ? 0 : requiredTags.length);
        }

        @Deprecated
        public static Uri buildTagFilterUri(String[] requiredTags) {
            return buildTagFilterUri(CONTENT_URI, requiredTags);
        }

        public static Uri buildCategoryTagFilterUri(Uri contentUri, String[] tags, int categories) {
            StringBuilder sb = new StringBuilder();
            for (String tag : tags) {
                if (TextUtils.isEmpty(tag)) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(tag.trim());
            }
            if (sb.length() == 0) {
                return contentUri;
            } else {
                return contentUri.buildUpon()
                        .appendQueryParameter(QUERY_PARAMETER_TAG_FILTER, sb.toString())
                        .appendQueryParameter(QUERY_PARAMETER_CATEGORIES,
                                String.valueOf(categories))
                        .build();
            }
        }

        public static Uri buildCounterByIntervalUri() {
            return CONTENT_URI.buildUpon().appendPath(PATH_SESSIONS_COUNTER).build();
        }
    }

    public static class Speakers implements SpeakersColumns, SyncColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SPEAKERS).build();

        public static final String CONTENT_TYPE_ID = "speaker";

        public static final String DEFAULT_SORT = SpeakersColumns.SPEAKER_NAME
                + " COLLATE NOCASE ASC";

        public static Uri buildSpeakerUri(String speakerId) {
            return CONTENT_URI.buildUpon().appendPath(speakerId).build();
        }

        public static String getSpeakerId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class MapTiles implements MapTileColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MAP_TILES).build();

        public static final String CONTENT_TYPE_ID = "maptiles";

        public static Uri buildUri() {
            return CONTENT_URI;
        }

        public static Uri buildFloorUri(String floor) {
            return CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(floor)).build();
        }

        public static String getFloorId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class MapMarkers implements MapMarkerColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MAP_MARKERS).build();

        public static final String CONTENT_TYPE_ID = "mapmarker";

        public static Uri buildMarkerUri(String markerId) {
            return CONTENT_URI.buildUpon().appendPath(markerId).build();
        }

        public static Uri buildMarkerUri() {
            return CONTENT_URI;
        }

        public static Uri buildFloorUri(int floor) {
            return CONTENT_URI.buildUpon().appendPath(PATH_MAP_FLOOR)
                    .appendPath("" + floor).build();
        }

        public static String getMarkerId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getMarkerFloor(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static class SearchSuggest {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_SUGGEST).build();

        public static final String DEFAULT_SORT = SearchManager.SUGGEST_COLUMN_TEXT_1
                + " COLLATE NOCASE ASC";
    }

    public static class SearchIndex {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_INDEX).build();
    }

    public static class SearchTopicsSessions {
        public static final String PATH_SEARCH_TOPICS_SESSIONS = "search_topics_sessions";

        public static final String CONTENT_TYPE_ID = "search_topics_sessions";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_TOPICS_SESSIONS).build();

        public static final String TOPIC_TAG_SELECTION = Tags.TAG_CATEGORY + "= ? and " +
                Tags.TAG_NAME + " like ?";

        public static final String TOPIC_TAG_SORT = Tags.TAG_NAME + " ASC";

        public static final String[] TOPIC_TAG_PROJECTION = {
                BaseColumns._ID,
                Tags.TAG_ID,
                Tags.TAG_NAME,
        };

        public static final String[] SEARCH_SESSIONS_PROJECTION = {
                BaseColumns._ID,
                ScheduleContract.Sessions.SESSION_ID,
                ScheduleContract.Sessions.SEARCH_SNIPPET
        };

        public static final String[] DEFAULT_PROJECTION = new String[] {
                BaseColumns._ID,
                SearchTopicSessionsColumns.TAG_OR_SESSION_ID,
                SearchTopicSessionsColumns.SEARCH_SNIPPET,
                SearchTopicSessionsColumns.IS_TOPIC_TAG,
        };
    }

    public interface SearchTopicSessionsColumns extends BaseColumns {
        String TAG_OR_SESSION_ID = "tag_or_session_id";
        String SEARCH_SNIPPET = "search_snippet";
        String IS_TOPIC_TAG = "is_topic_tag";
    }

    private ScheduleContract() {
    }

    public static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(
                ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

}
