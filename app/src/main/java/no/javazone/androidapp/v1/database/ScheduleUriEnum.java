package no.javazone.androidapp.v1.database;

/**
 * The list of {@code Uri}s recognised by the {@code ContentProvider} of the app.
 * <p />
 * It is important to order them in the order that follows {@link android.content.UriMatcher}
 * matching rules: wildcard {@code *} applies to one segment only and it processes matching per
 * segment in a tree manner over the list of {@code Uri} in the order they are added. The first
 * rule means that {@code sessions / *} would not match {@code sessions / id / room}. The second
 * rule is more subtle and means that if Uris are in the  order {@code * / room / counter} and
 * {@code sessions / room / time}, then {@code speaker / room / time} will not match anything,
 * because the {@code UriMatcher} will follow the path of the first  and will fail at the third
 * segment.
 */
public enum ScheduleUriEnum {
    BLOCKS(100, "blocks", ScheduleContract.Blocks.CONTENT_TYPE_ID, false, ScheduleDatabase.DatabaseTables.BLOCKS),
    BLOCKS_BETWEEN(101, "blocks/between/*/*", ScheduleContract.Blocks.CONTENT_TYPE_ID, false, null),
    BLOCKS_ID(102, "blocks/*", ScheduleContract.Blocks.CONTENT_TYPE_ID, true, null),
    BLOCKS_ID_SESSIONS(103, "blocks/*/sessions", ScheduleContract.Blocks.CONTENT_TYPE_ID, true, null),
    BLOCKS_ID_SESSIONS_STARRED(104, "blocks/*/sessions/starred", ScheduleContract.Blocks.CONTENT_TYPE_ID, true, null),
    TAGS(200, "tags", ScheduleContract.Tags.CONTENT_TYPE_ID, false, ScheduleDatabase.DatabaseTables.TAGS),
    TAGS_ID(201, "tags/*", ScheduleContract.Tags.CONTENT_TYPE_ID, false, null),
    ROOMS(300, "rooms", ScheduleContract.Rooms.CONTENT_TYPE_ID, false, ScheduleDatabase.DatabaseTables.ROOMS),
    ROOMS_ID(301, "rooms/*", ScheduleContract.Rooms.CONTENT_TYPE_ID, true, null),
    ROOMS_ID_SESSIONS(302, "rooms/*/sessions", ScheduleContract.Sessions.CONTENT_TYPE_ID, false, null),
    SESSIONS(400, "sessions", ScheduleContract.Sessions.CONTENT_TYPE_ID, false, ScheduleDatabase.DatabaseTables.SESSIONS),
    SESSIONS_MY_SCHEDULE(401, "sessions/my_schedule", ScheduleContract.Sessions.CONTENT_TYPE_ID, false, null),



    SESSIONS_SEARCH(403, "sessions/search/*", ScheduleContract.Sessions.CONTENT_TYPE_ID, false, null),
    SESSIONS_AT(404, "sessions/at/*", ScheduleContract.Sessions.CONTENT_TYPE_ID, false, null),
    SESSIONS_AFTER(411, "sessions/after/*", ScheduleContract.Sessions.CONTENT_TYPE_ID, false, null),
    SESSIONS_ROOM_AFTER(408, "sessions/room/*/after/*", ScheduleContract.Sessions.CONTENT_TYPE_ID, false, null),
    SESSIONS_UNSCHEDULED(409, "sessions/unscheduled/*", ScheduleContract.Sessions.CONTENT_TYPE_ID, false, null),
    SESSIONS_COUNTER(410, "sessions/counter", null, true, null),
    SESSIONS_ID(405, "sessions/*", ScheduleContract.Sessions.CONTENT_TYPE_ID, true, null),
    SESSIONS_ID_SPEAKERS(406, "sessions/*/speakers", ScheduleContract.Speakers.CONTENT_TYPE_ID, false,
            ScheduleDatabase.DatabaseTables.SESSIONS_SPEAKERS),
    SESSIONS_ID_TAGS(407, "sessions/*/tags", ScheduleContract.Tags.CONTENT_TYPE_ID, false, ScheduleDatabase.DatabaseTables.SESSIONS_TAGS),
    SPEAKERS(500, "speakers", ScheduleContract.Speakers.CONTENT_TYPE_ID, false, ScheduleDatabase.DatabaseTables.SPEAKERS),
    SPEAKERS_ID(501, "speakers/*", ScheduleContract.Speakers.CONTENT_TYPE_ID, true, null),
    SPEAKERS_ID_SESSIONS(502, "speakers/*/sessions", ScheduleContract.Sessions.CONTENT_TYPE_ID, false, null),
    SEARCH_SUGGEST(800, "search_suggest_query", null, false, ScheduleDatabase.DatabaseTables.SEARCH_SUGGEST),
    SEARCH_INDEX(801, "search_index", null, false, null),// update only
    MAPMARKERS(900, "mapmarkers", ScheduleContract.MapMarkers.CONTENT_TYPE_ID, false, ScheduleDatabase.DatabaseTables.MAPMARKERS),
    MAPMARKERS_FLOOR(901, "mapmarkers/floor/*", ScheduleContract.MapMarkers.CONTENT_TYPE_ID, false, null),
    MAPMARKERS_ID(902, "mapmarkers/*", ScheduleContract.MapMarkers.CONTENT_TYPE_ID, true, null),
    MAPTILES(1000, "maptiles", ScheduleContract.MapTiles.CONTENT_TYPE_ID, false, ScheduleDatabase.DatabaseTables.MAPTILES),
    MAPTILES_FLOOR(1001, "maptiles/*", ScheduleContract.MapTiles.CONTENT_TYPE_ID, true, null),
    FEEDBACK_ALL(1002, "feedback", ScheduleContract.Feedback.CONTENT_TYPE_ID, false, null),
    FEEDBACK_FOR_SESSION(1003, "feedback/*", ScheduleContract.Feedback.CONTENT_TYPE_ID, true, ScheduleDatabase.DatabaseTables.FEEDBACK),

    SEARCH_TOPICS_SESSIONS(1400, "search_topics_sessions",
                           ScheduleContract.SearchTopicsSessions.CONTENT_TYPE_ID, false, null /*virtual table*/),
    CARDS(1500, "cards", ScheduleContract.Cards.CONTENT_TYPE_ID, false, ScheduleDatabase.DatabaseTables.CARDS);


    public int code;

    public String path;

    public String contentType;

    public String table;

    ScheduleUriEnum(int code, String path, String contentTypeId, boolean item, String table) {
        this.code = code;
        this.path = path;
        this.contentType = item ? ScheduleContract.makeContentItemType(contentTypeId)
                : ScheduleContract.makeContentType(contentTypeId);
        this.table = table;
    }


}
