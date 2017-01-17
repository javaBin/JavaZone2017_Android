package no.javazone.archframework.database.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class ScheduleContract {
    public static final String CONTENT_AUTHORITY = "no.javazone";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String CONTENT_TYPE_APP_BASE = "no.javazone.";

    public static final String CONTENT_TYPE_BASE = "vnd.android.cursor.dir/vnd."
            + CONTENT_TYPE_APP_BASE;

    public static final String CONTENT_ITEM_TYPE_BASE = "vnd.android.cursor.item/vnd."
            + CONTENT_TYPE_APP_BASE;


    private static final String PATH_MAP_MARKERS = "mapmarkers";
    private static final String PATH_MAP_FLOOR = "floor";
    private static final String PATH_MAP_TILES = "maptiles";


    interface MapTileColumns {
        String TILE_FLOOR = "map_tile_floor";
        String TILE_FILE = "map_tile_file";
        String TILE_URL = "map_tile_url";
    }


    interface MapMarkerColumns {
        String MARKER_ID = "map_marker_id";
        String MARKER_TYPE = "map_marker_type";
        String MARKER_LATITUDE = "map_marker_latitude";
        String MARKER_LONGITUDE = "map_marker_longitude";
        String MARKER_LABEL = "map_marker_label";
        String MARKER_FLOOR = "map_marker_floor";
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

}
