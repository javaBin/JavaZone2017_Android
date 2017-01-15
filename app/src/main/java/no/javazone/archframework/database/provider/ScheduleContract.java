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

        /**
         * Floor *
         */
        String TILE_FLOOR = "map_tile_floor";
        /**
         * Filename *
         */
        String TILE_FILE = "map_tile_file";
        /**
         * Url *
         */
        String TILE_URL = "map_tile_url";
    }


    interface MapMarkerColumns {

        /**
         * Unique string identifying this marker.
         */
        String MARKER_ID = "map_marker_id";
        /**
         * Type of marker.
         */
        String MARKER_TYPE = "map_marker_type";
        /**
         * Latitudinal position of marker.
         */
        String MARKER_LATITUDE = "map_marker_latitude";
        /**
         * Longitudinal position of marker.
         */
        String MARKER_LONGITUDE = "map_marker_longitude";
        /**
         * Label (title) for this marker.
         */
        String MARKER_LABEL = "map_marker_label";
        /**
         * Building floor this marker is on.
         */
        String MARKER_FLOOR = "map_marker_floor";
    }


    /**
     * TileProvider entries are used to create an overlay provider for the map.
     */
    public static class MapTiles implements MapTileColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MAP_TILES).build();

        public static final String CONTENT_TYPE_ID = "maptiles";

        /**
         * Build {@link Uri} for all overlay zoom entries.
         */
        public static Uri buildUri() {
            return CONTENT_URI;
        }

        /**
         * Build {@link Uri} for requested floor.
         */
        public static Uri buildFloorUri(String floor) {
            return CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(floor)).build();
        }

        /**
         * Read floor from {@link MapMarkers} {@link Uri}.
         */
        public static String getFloorId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /**
     * Markers refer to marked positions on the map.
     */
    public static class MapMarkers implements MapMarkerColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MAP_MARKERS).build();

        public static final String CONTENT_TYPE_ID = "mapmarker";

        /**
         * Build {@link Uri} for requested {@link #MARKER_ID}.
         */
        public static Uri buildMarkerUri(String markerId) {
            return CONTENT_URI.buildUpon().appendPath(markerId).build();
        }

        /**
         * Build {@link Uri} for all markers.
         */
        public static Uri buildMarkerUri() {
            return CONTENT_URI;
        }

        /**
         * Build {@link Uri} for requested {@link #MARKER_ID}.
         */
        public static Uri buildFloorUri(int floor) {
            return CONTENT_URI.buildUpon().appendPath(PATH_MAP_FLOOR)
                    .appendPath("" + floor).build();
        }

        /**
         * Read {@link #MARKER_ID} from {@link MapMarkers} {@link Uri}.
         */
        public static String getMarkerId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * Read FLOOR from {@link MapMarkers} {@link Uri}.
         */
        public static String getMarkerFloor(Uri uri) {
            return uri.getPathSegments().get(2);
        }

    }

}
