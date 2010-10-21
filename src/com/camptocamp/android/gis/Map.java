package com.camptocamp.android.gis;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.mgmaps.cache.ScreenCache;
import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MapView;
import com.nutiteq.cache.MemoryCache;
import com.nutiteq.components.PlaceIcon;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.controls.AndroidKeysHandler;
import com.nutiteq.location.LocationSource;
import com.nutiteq.location.NutiteqLocationMarker;
import com.nutiteq.location.providers.AndroidGPSProvider;
import com.nutiteq.location.providers.CellIdLocationProvider;
import com.nutiteq.log.AndroidLogger;
import com.nutiteq.log.Log;
import com.nutiteq.ui.EventDrivenPanning;
import com.nutiteq.utils.Utils;

public class Map extends Activity {

    private boolean isTrackingPosition = false;
    private boolean onRetainCalled;
    private MapView mapView;
    private BasicMapComponent mapComponent;
    private SwisstopoMap geomap;

    public static final String D = "C2C:";
    // private final static String TAG = D + "Map";
    public static final String APP = "c2c-android-gis";
    public static final String VDR = "Camptocamp SA";
    private static final int ZOOM = 14;
    private static final int MENU_MAP_PIXEL = 0;
    private static final int MENU_MAP_ORTHO = 1;

    private final double lat = 46.517815; // X: 152'210
    private final double lng = 6.562805; // Y: 532'790

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRetainCalled = false;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        LinearLayout mapLayout = ((LinearLayout) findViewById(R.id.map));

        // Create base map
        final Object savedMapComponent = getLastNonConfigurationInstance();
        if (savedMapComponent == null) {
            geomap = new SwisstopoMap(getString(R.string.base_url_pixel), VDR, ZOOM);
            mapComponent = new SwisstopoComponent(new WgsPoint(lng, lat), ZOOM);
            mapComponent.setMap(geomap);
            final MemoryCache memoryCache = new MemoryCache(1024 * 1024);
            mapComponent.setNetworkCache(memoryCache);
            // mapComponent.setNetworkCache(new MemoryCache(0));
            // mapComponent.setPanningStrategy(new ThreadDrivenPanning());
            mapComponent.setPanningStrategy(new EventDrivenPanning());
            mapComponent.setControlKeysHandler(new AndroidKeysHandler());
            mapComponent.startMapping();
            mapComponent.setTouchClickTolerance(BasicMapComponent.FINGER_CLICK_TOLERANCE);
        } else {
            mapComponent = (BasicMapComponent) savedMapComponent;
        }

        // Log.setLogger(new AndroidLogger(APP));
        // Log.enableAll();

        // Map View
        mapView = new MapView(this, mapComponent);
        mapLayout.addView(mapView);
        mapView.setClickable(true);
        mapView.setEnabled(true);

        // Zoom
        final ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoom);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                mapComponent.zoomIn();
            }
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                mapComponent.zoomOut();
            }
        });

        // GPS Location tracking
        // FIXME: Implements CellId/Wifi provider and automatic choice of best
        // data available
        final LocationSource locationSource = new AndroidGPSProvider(
                (LocationManager) getSystemService(Context.LOCATION_SERVICE), 1000L);
        locationSource.setLocationMarker(new NutiteqLocationMarker(new PlaceIcon(Utils
                .createImage("/res/drawable/marker.png"), 8, 8), 0, true));
        final ImageButton btn = (ImageButton) findViewById(R.id.position_track);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTrackingPosition) {
                    Toast.makeText(Map.this, "GPS tracking stopped !", Toast.LENGTH_SHORT).show();
                    locationSource.quit();
                    isTrackingPosition = false;
                } else {
                    Toast.makeText(Map.this, "GPS tracking started !", Toast.LENGTH_SHORT).show();
                    mapComponent.setLocationSource(locationSource);
                    isTrackingPosition = true;
                }
            }
        });

        // Markers
        // mapComponent.addPlace(new Place(1, "PSE - EPFL", Utils
        // .createImage("/res/drawable/marker.png"), new WgsPoint(6.562794,
        // 46.517705)));
        // 6.562794, 46.517705
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        menu.add(0, MENU_MAP_PIXEL, 0, "Pixel Map");
        menu.add(0, MENU_MAP_ORTHO, 1, "Orthophoto");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        // FIXME: Doesn't fully work
        int zoom = ((SwisstopoMap) mapComponent.getMap()).zoom;
        switch (item.getItemId()) {
        case MENU_MAP_PIXEL:
            mapComponent.setMap(new SwisstopoMap(getString(R.string.base_url_pixel), VDR, zoom));
            break;
        case MENU_MAP_ORTHO:
            mapComponent.setMap(new SwisstopoMap(getString(R.string.base_url_ortho), VDR, zoom));
            break;
        }
        return true;
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        onRetainCalled = true;
        return mapComponent;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.clean();
            mapView = null;
        }
        if (!onRetainCalled) {
            mapComponent.stopMapping();
            mapComponent = null;
        }
    }
}