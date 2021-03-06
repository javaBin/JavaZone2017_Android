/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.javazone.androidapp.v1.ui.fragment;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.archframework.model.domain.Coordinates;
import no.javazone.androidapp.v1.beacon.EstimoteBeaconManager;
import no.javazone.androidapp.v1.database.ScheduleContract;
import no.javazone.androidapp.v1.archframework.model.domain.MarkerModel;
import no.javazone.androidapp.v1.maps.MarkerLoadingTask;
import no.javazone.androidapp.v1.ui.activity.MapActivity;
import no.javazone.androidapp.v1.util.AnalyticsHelper;
import no.javazone.androidapp.v1.util.MapUtils;
import no.javazone.androidapp.v1.util.NetworkUtils;

import static no.javazone.androidapp.v1.util.LogUtils.LOGD;
import static no.javazone.androidapp.v1.util.LogUtils.makeLogTag;


/**
 * Shows a map of the conference venue.
 */
public class MapFragment extends com.google.android.gms.maps.MapFragment implements
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnIndoorStateChangeListener, GoogleMap.OnMapClickListener, OnMapReadyCallback {

    private static final LatLng OSOLOSPEKTRUM = new LatLng(59.9130, 10.7547);
    private static final int REQUEST_LOCATION = 0;

    private static final String EXTRAS_HIGHLIGHT_ROOM = "EXTRAS_HIGHLIGHT_ROOM";
    private static final String EXTRAS_ACTIVE_FLOOR = "EXTRAS_ACTIVE_FLOOR";

    private EstimoteBeaconManager mEstimoteBeaconManager;

    // Initial camera zoom
    private static final float CAMERA_ZOOM = 18;
    private static final float DETAILED_MAP_ZOOM_THRESHOLD = 17;
    private static final float CAMERA_BEARING = -27;
    private static Marker mCurrentLocationMarker = null;
    private static final int INVALID_FLOOR = Integer.MIN_VALUE;
    private static final int INITIAL_FLOOR_COUNT = 3;
    private static final int OSLOSPEKTRUM_DEFAULT_LEVEL_INDEX = 1;
    private boolean mMyLocationEnabled = false;

    private static final String TAG = makeLogTag(MapFragment.class);

    // Markers stored by id
    private HashMap<String, MarkerModel> mMarkers = new HashMap<>();
    private ArrayList<GroundOverlay> mGroundOverlays = new ArrayList<>();
    // Markers stored by floor
    private SparseArray<ArrayList<Marker>> mFloorMarkerMapping =
            new SparseArray<>(INITIAL_FLOOR_COUNT);

    // Screen DPI
    private float mDPI = 0;

    private IndoorBuilding mOsloSpektrumBuilding = null;

    // currently displayed floor
    private int mFloor = INVALID_FLOOR;

    private Marker mActiveMarker = null;
    private BitmapDescriptor ICON_ACTIVE;
    private ArrayList<BitmapDescriptor> mFloorIcons = new ArrayList<>();

    private boolean mAtOsloSpektrum = false;
    private Marker mOsloSpektrumMarker = null;

    private GoogleMap mMap;
    private Rect mMapInsets = new Rect();

    private String mHighlightedRoomName = null;
    private MarkerModel mHighlightedRoom = null;
    private final static int mNumberFloors = 3;

    private int mInitialFloor = OSLOSPEKTRUM_DEFAULT_LEVEL_INDEX;

    private static final int TOKEN_LOADER_MARKERS = 0x1;
    //For Analytics tracking
    public static final String SCREEN_LABEL = "Map";


    public interface Callbacks {

        void onInfoHide();

        void onInfoShowOsloSpektrum();

        void onInfoShowTitle(String label, int roomType);

        void onInfoShowSessionlist(String roomId, String roomTitle, int roomType);

        void onInfoShowFirstSessionTitle(String roomId, String roomTitle, int roomType);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {

        @Override
        public void onInfoHide() {
        }

        @Override
        public void onInfoShowOsloSpektrum() {
        }

        @Override
        public void onInfoShowTitle(String label, int roomType) {
        }

        @Override
        public void onInfoShowSessionlist(String roomId, String roomTitle, int roomType) {
        }

        @Override
        public void onInfoShowFirstSessionTitle(String roomId, String roomTitle, int roomType) {
        }

    };

    private Callbacks mCallbacks = sDummyCallbacks;


    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public static MapFragment newInstance(String highlightedRoomName) {
        MapFragment fragment = new MapFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRAS_HIGHLIGHT_ROOM, highlightedRoomName);
        fragment.setArguments(arguments);

        return fragment;
    }

    public static MapFragment newInstance(Bundle savedState) {
        MapFragment fragment = new MapFragment();
        fragment.setArguments(savedState);
        return fragment;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActiveMarker != null) {
            // A marker is currently selected, restore its selection.
            outState.putString(EXTRAS_HIGHLIGHT_ROOM, mActiveMarker.getTitle());
            outState.putInt(EXTRAS_ACTIVE_FLOOR, INVALID_FLOOR);
        } else if (mAtOsloSpektrum) {
            // No marker is selected, store the active floor if at Oslo Spektrum.
            outState.putInt(EXTRAS_ACTIVE_FLOOR, mFloor);
            outState.putString(EXTRAS_HIGHLIGHT_ROOM, null);
        }

        LOGD(TAG, "Saved state: " + outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsHelper.sendScreenView(SCREEN_LABEL);
        mDPI = getActivity().getResources().getDisplayMetrics().densityDpi / 160f;

        ICON_ACTIVE = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);


        // Get the arguments and restore the highlighted room or displayed floor.
        Bundle data = getArguments();
        if (data != null) {
            mHighlightedRoomName = data.getString(EXTRAS_HIGHLIGHT_ROOM, null);
            mInitialFloor = data.getInt(EXTRAS_ACTIVE_FLOOR, OSLOSPEKTRUM_DEFAULT_LEVEL_INDEX);
        }

        getMapAsync(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mapView = super.onCreateView(inflater, container, savedInstanceState);
        mEstimoteBeaconManager = new EstimoteBeaconManager(
                getActivity(), this);
        mEstimoteBeaconManager.initializeEstimoteBeaconManager(getActivity());
        setMapInsets(mMapInsets);

        return mapView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mEstimoteBeaconManager.startEstimoteBeaconManager();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!NetworkUtils.isGpsOn(getActivity())) {
            createGpsDialog().show();
        }
        if (!NetworkUtils.isBluetoothOn(getActivity())) {
            createBluetoothDialog().show();
        } else {
            mEstimoteBeaconManager.startMonitorEstimoteBeacons(getActivity());
        }

        if (mFloorIcons.isEmpty()) {
            for (int i = 0; i < mNumberFloors; i++) {
                mFloorIcons.add(BitmapDescriptorFactory
                        .defaultMarker(MapUtils.createFloorColor(i)));
            }
        }
    }

    public void placeMarkerLocationOnCurrentRegion(Coordinates coordinates) {
        LatLng beaconRegionLatLng = new LatLng(coordinates.getLatitude(), coordinates.getLongitude());
        if (mCurrentLocationMarker != null) {
            mCurrentLocationMarker.remove();
            mCurrentLocationMarker = null;
        }

        MarkerOptions markerOptions
                = MapUtils.createCurrentLocationMarker("Your current location", beaconRegionLatLng);
        mCurrentLocationMarker = mMap.addMarker(markerOptions);
    }

    private AlertDialog.Builder createBluetoothDialog() {
        AlertDialog.Builder buildAlertDialog = new AlertDialog.Builder(getActivity(),
                R.style.Dialog_Theme);
        buildAlertDialog.setTitle("Bluetooth disabled");
        buildAlertDialog.setMessage("Please turn on Bluetooth to be able to use indoor maps");
        buildAlertDialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                NetworkUtils.enableBluetooth(getActivity());
                mEstimoteBeaconManager.startMonitorEstimoteBeacons(getActivity());
            }
        });
        buildAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return buildAlertDialog;
    }

    private AlertDialog.Builder createGpsDialog() {
        AlertDialog.Builder buildAlertDialog = new AlertDialog.Builder(getActivity(),
                R.style.Dialog_Theme);
        buildAlertDialog.setTitle("Location disabled");
        buildAlertDialog.setMessage("Please turn on Location to be able to use indoor maps");
        buildAlertDialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                NetworkUtils.enableGPS(getActivity());
                mEstimoteBeaconManager.startMonitorEstimoteBeacons(getActivity());
            }
        });
        buildAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return buildAlertDialog;
    }

    @Override
    public void onPause() {
        super.onPause();
        mEstimoteBeaconManager.stopRanging();
        mEstimoteBeaconManager.destroyEstimoteBeaconManager();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEstimoteBeaconManager.destroyEstimoteBeaconManager();
    }


    /**
     * Toggles the 'my location' button. Note that the location permission <b>must</b> have already
     * been granted when this call is made.
     *
     * @param setEnabled
     */
    public void setMyLocationEnabled(final boolean setEnabled) {
        mMyLocationEnabled = setEnabled;

        if (mMap == null) {
            return;
        }
        //noinspection MissingPermission
        mMap.setMyLocationEnabled(mMyLocationEnabled);
    }


    public void setMapInsets(int left, int top, int right, int bottom) {
        mMapInsets.set(left, top, right, bottom);
        if (mMap != null) {
            mMap.setPadding(mMapInsets.left, mMapInsets.top, mMapInsets.right, mMapInsets.bottom);
        }
    }

    public void setMapInsets(Rect insets) {
        mMapInsets.set(insets.left, insets.top, insets.right, insets.bottom);
        if (mMap != null) {
            mMap.setPadding(mMapInsets.left, mMapInsets.top, mMapInsets.right, mMapInsets.bottom);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mEstimoteBeaconManager.stopEstimoteBeaconManager();
    }


    /**
     * Clears the map and initialises all map variables that hold markers and overlays.
     */
    private void clearMap() {
        if (mMap != null) {
            mMap.clear();
        }

        mMarkers.clear();
        mFloorMarkerMapping.clear();

        mFloor = INVALID_FLOOR;
    }

    private void hideMarkersWhenSwitchingFloors() {
        setFloorElementsVisible(mFloor, false);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setIndoorEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnIndoorStateChangeListener(this);
        mMap.setOnMapClickListener(this);

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (mMap.getCameraPosition().zoom <= DETAILED_MAP_ZOOM_THRESHOLD) {
                    showMarkersForAllFloors(false);
                    mOsloSpektrumMarker.setVisible(true);
                } else {
                    setFloorElementsVisible(mFloor, true);
                    mOsloSpektrumMarker.setVisible(false);
                }
            }
        });

        UiSettings mapUiSettings = mMap.getUiSettings();
        mapUiSettings.setZoomControlsEnabled(false);
        mapUiSettings.setMapToolbarEnabled(false);

        ((MapActivity) getActivity()).attemptEnableMyLocation();

        // load all markers
        LoaderManager lm = getLoaderManager();
        lm.initLoader(TOKEN_LOADER_MARKERS, null, mMarkerLoader).forceLoad();

        setupMap(true);
    }

    public void showMarkersForAllFloors(boolean visible) {
        for (int i = 0; i < mNumberFloors; i++) {
            final ArrayList<Marker> markers = mFloorMarkerMapping.get(i);
            if (markers != null) {
                for (Marker m : markers) {
                    m.setVisible(visible);
                }
            }
        }
    }

    private void setupMap(boolean resetCamera) {
        showMarkersForAllFloors(true);
        mOsloSpektrumMarker = mMap
                .addMarker(MapUtils.createOsloSpektrumMarker(OSOLOSPEKTRUM).visible(false));

        if (resetCamera) {
            centerOnOsloSpektrum(false);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new ClassCastException(
                    "Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;

        activity.getContentResolver().registerContentObserver(
                ScheduleContract.MapMarkers.CONTENT_URI, true, mObserver);
        activity.getContentResolver().registerContentObserver(
                ScheduleContract.MapTiles.CONTENT_URI, true, mObserver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;

        getActivity().getContentResolver().unregisterContentObserver(mObserver);
    }

    private void centerOnOsloSpektrum(boolean animate) {
        CameraUpdate camera = CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder().bearing(CAMERA_BEARING).target(OSOLOSPEKTRUM)
                        .zoom(CAMERA_ZOOM).tilt(0f).build());
        if (animate) {
            mMap.animateCamera(camera);
        } else {
            mMap.moveCamera(camera);
        }
    }

    /**
     * Switches the displayed floor for which elements are displayed.
     * If the map is not initialised yet or no data has been loaded, nothing will be displayed.
     * If an invalid floor is specified and elements are currently on the map, all visible
     * elements will be hidden.
     * If this floor is not active for the indoor building, it is made active.
     *
     * @param floor index of the floor to display. It requires an overlay and at least one Marker
     *              to
     *              be defined for it and it has to be a valid index in the
     *              {@link com.google.android.gms.maps.model.IndoorBuilding} object that
     *              describes Oslo Spektrum.
     */
    private void showFloorElementsIndex(int floor) {
        LOGD(TAG, "Show floor " + floor);

        // Hide previous floor elements if the floor has changed
        if (mFloor != floor) {
            setFloorElementsVisible(mFloor, false);
        }

        mFloor = floor;

        if (mAtOsloSpektrum) {
            // Always hide the Oslo Spektrum marker if a floor is shown
            mOsloSpektrumMarker.setVisible(false);
            setFloorElementsVisible(mFloor, true);
        } else {
            // Show Oslo Spektrum marker if not at Oslo Spektrum or at an invalid floor
            mOsloSpektrumMarker.setVisible(true);
        }
    }

    /**
     * Change the active floor of Oslo Spektrum Center
     * to the given floor index. See {@link #showFloorElementsIndex(int)}.
     *
     * @param floor Index of the floor to show.
     * @see #showFloorElementsIndex(int)
     */
    private void showFloorIndex(int floor) {
        if (isValidFloor(floor) && mAtOsloSpektrum) {

            if (mMap.getFocusedBuilding().getActiveLevelIndex() == floor) {
                // This floor is already active, show its elements
                showFloorElementsIndex(floor);
            } else {
                // This floor is not shown yet, switch to this floor on the map
                mMap.getFocusedBuilding().getLevels().get(floor).activate();
            }

        } else {
            LOGD(TAG, "Can't show floor index " + floor + ".");
        }
    }

    /**
     * Change the visibility of all Markers and TileOverlays for a floor.
     */
    private void setFloorElementsVisible(int floor, boolean visible) {
        if (mFloor == INVALID_FLOOR) {
            showMarkersForAllFloors(true);
            return;
        }

        final ArrayList<Marker> markers = mFloorMarkerMapping.get(floor);
        if (markers != null) {
            for (Marker m : markers) {
                m.setVisible(visible);
            }
        }
    }

    private boolean isValidFloor(int floor) {
        return floor < mOsloSpektrumBuilding.getLevels().size();
    }

    private void enableMapElements() {
        if (mOsloSpektrumBuilding != null && mAtOsloSpektrum) {
            onIndoorLevelActivated(mOsloSpektrumBuilding);
        }
    }

    private void onDefocusOsloSpektrum() {
        // Hide all markers and tile overlays
        deselectActiveMarker();
        showFloorElementsIndex(INVALID_FLOOR);
        mCallbacks.onInfoShowOsloSpektrum();
    }

    private void onFocusHighlightedRoom() {
        // Highlight a room if argument is set and it exists, otherwise show the default floor
        if (mHighlightedRoomName != null && mMarkers.containsKey(mHighlightedRoomName)) {
            highlightRoom(mHighlightedRoomName);
            showMarkersForAllFloors(false);
            setFloorElementsVisible(mHighlightedRoom.floor, true);
            selectActiveMarker(mHighlightedRoom.marker);
            mCallbacks.onInfoShowFirstSessionTitle(mHighlightedRoom.id,
                    mHighlightedRoom.label,
                    mHighlightedRoom.type);
            // Reset highlighted room because it has just been displayed.
            mHighlightedRoomName = null;
        }
    }

    @Override
    public void onIndoorBuildingFocused() {
        IndoorBuilding building = mMap.getFocusedBuilding();

        if (building != null && mOsloSpektrumBuilding == null
                && mMap.getProjection().getVisibleRegion().latLngBounds.contains(OSOLOSPEKTRUM)) {
            // Store the first active building. This will always be Oslo Spektrum
            mOsloSpektrumBuilding = building;
        }

        if (!mAtOsloSpektrum && building != null && building.equals(mOsloSpektrumBuilding)) {
            // Map is focused on Oslo Spektrum Center
            mAtOsloSpektrum = true;
            onFocusHighlightedRoom();
        } else if (mAtOsloSpektrum && mOsloSpektrumBuilding != null && !mOsloSpektrumBuilding.equals(building)) {
            // Map is no longer focused on Oslo Spektrum Center
            mAtOsloSpektrum = false;
            onDefocusOsloSpektrum();
        }
        onIndoorLevelActivated(building);
    }

    @Override
    public void onIndoorLevelActivated(IndoorBuilding indoorBuilding) {
        if (indoorBuilding != null && indoorBuilding.equals(mOsloSpektrumBuilding)) {
            onOsloSpektrumFloorActivated(indoorBuilding.getActiveLevelIndex());
        }
    }

    /**
     * Called when an indoor floor level in the Oslo Spektrum building has been activated.
     * If a room is to be highlighted, the map is centered and its marker is activated.
     */
    private void onOsloSpektrumFloorActivated(int activeLevelIndex) {
        if (mHighlightedRoom != null && mFloor == mHighlightedRoom.floor) {
            // A room highlight is pending. Highlight the marker and display info details.
            onMarkerClick(mHighlightedRoom.marker);
            centerMap(mHighlightedRoom.marker.getPosition());

            // Remove the highlight room flag, because the room has just been highlighted.
            mHighlightedRoom = null;
            mHighlightedRoomName = null;
        } else if (mFloor != activeLevelIndex) {
            // Deselect and hide the info details.
            deselectActiveMarker();
            mCallbacks.onInfoHide();
        }

        // Show map elements for this floor
        showFloorElementsIndex(activeLevelIndex);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        deselectActiveMarker();
        mCallbacks.onInfoHide();
    }

    private void deselectActiveMarker() {
        if (mActiveMarker != null) {
            mActiveMarker.setVisible(false);
        }
    }

    private void selectActiveMarker(Marker marker) {
        if (marker != null) {
            mActiveMarker = mMap.addMarker(MapUtils.createFloorMarkers("selected", mFloor, marker.getPosition()).visible(true));
            mActiveMarker.setIcon(ICON_ACTIVE);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        final String title = marker.getTitle();
        final MarkerModel model = mMarkers.get(title);

        AnalyticsHelper.sendEvent("Map", "markerclick", title);

        deselectActiveMarker();

        if (marker.equals(mCurrentLocationMarker)) {
            mCallbacks.onInfoShowTitle(marker.getTitle(), MarkerModel.TYPE_SESSION);
        }

        // The Oslo Spektrum marker can be compared directly.
        // For all other markers the model needs to be looked up first.
        if (marker.equals(mOsloSpektrumMarker)) {
            // Return camera to Oslo Spektrum
            LOGD(TAG, "Clicked on Oslo Spektrum marker, return to initial display.");
            centerOnOsloSpektrum(true);

        } else if (model != null && MapUtils.hasInfoTitleOnly(model.type)) {
            // Show a basic info window with a title only
            mCallbacks.onInfoShowTitle(model.label, model.type);
            selectActiveMarker(marker);

        } else if (model != null && MapUtils.hasInfoSessionList(model.type)) {
            // Type has sessions to display
            mCallbacks.onInfoShowSessionlist(model.id, model.label, model.type);
            selectActiveMarker(marker);

        } else if (model != null && MapUtils.hasInfoFirstDescriptionOnly(model.type)) {
            // Display the description of the first session only
            mCallbacks.onInfoShowFirstSessionTitle(model.id, model.label, model.type);
            selectActiveMarker(marker);

        } else {
            // Hide the bottom sheet for unknown markers
            mCallbacks.onInfoHide();
        }

        return true;
    }

    private void centerMap(LatLng position) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, CAMERA_ZOOM));
    }

    private void highlightRoom(String roomId) {
        MarkerModel m = mMarkers.get(roomId);
        if (m != null) {
            mHighlightedRoom = m;
        }
    }

    private void onMarkersLoaded(List<MarkerLoadingTask.MarkerEntry> list) {
        if (list != null) {
            for (MarkerLoadingTask.MarkerEntry entry : list) {

                // Skip incomplete entries
                if (entry.options == null || entry.model == null) {
                    break;
                }

                // Add marker to the map
                Marker m = mMap.addMarker(entry.options);
                MarkerModel model = entry.model;
                model.marker = m;

                // Store the marker and its model
                ArrayList<Marker> markerList = mFloorMarkerMapping.get(model.floor);
                if (markerList == null) {
                    // Initialise the list of Markers for this floor
                    markerList = new ArrayList<>();
                    mFloorMarkerMapping.put(model.floor, markerList);
                }
                markerList.add(m);
                mMarkers.put(model.id, model);
            }

            if (mFloor > INVALID_FLOOR) {
                setFloorElementsVisible(mFloor, true);
            }

            onFocusHighlightedRoom();

            if (mHighlightedRoomName == null) {
                ((MapActivity) getActivity()).mFloorAllButton.callOnClick();
            }

            LatLngBounds osloSpektrumLatLngBounds = new LatLngBounds(
                    new LatLng(59.91245775721807, 10.753627974205074),       // South west corner
                    new LatLng(59.91353895983281, 10.755966466270479));      // North east corner

            GroundOverlayOptions osloSpektrumLevel0 = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.oslospektrum_level0))
                    .positionFromBounds(osloSpektrumLatLngBounds);

            GroundOverlayOptions osloSpektrumLevel1 = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.oslospektrum_level1))
                    .positionFromBounds(osloSpektrumLatLngBounds);

            GroundOverlayOptions osloSpektrumLevel2 = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.oslospektrum_level2))
                    .positionFromBounds(osloSpektrumLatLngBounds);

            mGroundOverlays.add(mMap.addGroundOverlay(osloSpektrumLevel0));
            mGroundOverlays.add(mMap.addGroundOverlay(osloSpektrumLevel1));
            mGroundOverlays.add(mMap.addGroundOverlay(osloSpektrumLevel2));

            mGroundOverlays.get(0).setVisible(true);
            for(int i = 1; i < mGroundOverlays.size(); i++) {
                mGroundOverlays.get(i).setVisible(false);
            }
        }

        enableMapElements();
    }

    public void showAllFloors(boolean visible) {
        mFloor = INVALID_FLOOR;
        showMarkersForAllFloors(visible);
    }

    public void showMarkersForSpecificFloor(int floorLevel) {
        if (mFloor == floorLevel) {
            return;
        }

        mFloor = floorLevel;

        if (mMap != null) {
            hideMarkersWhenSwitchingFloors();
        }

        mCallbacks.onInfoHide();
        deselectActiveMarker();
        setFloorElementsVisible(mFloor, true);

        showGroundOverlay(floorLevel);
    }

    public void showGroundOverlay(int floorLevel) {
        for (int i = 0; i < mGroundOverlays.size(); i++) {
            if (i == floorLevel) {
                mGroundOverlays.get(i).setVisible(true);
                continue;
            }

            mGroundOverlays.get(i).setVisible(false);
        }
    }

    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (!isAdded()) {
                return;
            }

            //clear map reload all data
            clearMap();
            setupMap(false);

            // reload data from loaders
            LoaderManager lm = getActivity().getLoaderManager();

            Loader<Cursor> loader =
                    lm.getLoader(TOKEN_LOADER_MARKERS);
            if (loader != null) {
                loader.forceLoad();
            }
        }
    };


    /**
     * LoaderCallbacks for the {@link MarkerLoadingTask} that loads all markers for the map.
     */
    private LoaderCallbacks<List<MarkerLoadingTask.MarkerEntry>> mMarkerLoader
            = new LoaderCallbacks<List<MarkerLoadingTask.MarkerEntry>>() {
        @Override
        public Loader<List<MarkerLoadingTask.MarkerEntry>> onCreateLoader(int id, Bundle args) {
            return new MarkerLoadingTask(getActivity());
        }


        @Override
        public void onLoadFinished(Loader<List<MarkerLoadingTask.MarkerEntry>> loader,
                                   List<MarkerLoadingTask.MarkerEntry> data) {
            onMarkersLoaded(data);
        }

        @Override
        public void onLoaderReset(Loader<List<MarkerLoadingTask.MarkerEntry>> loader) {
        }
    };

}
