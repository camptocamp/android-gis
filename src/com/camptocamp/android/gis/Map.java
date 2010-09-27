package com.camptocamp.android.gis;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MapView;
import com.nutiteq.cache.MemoryCache;
import com.nutiteq.components.Place;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.controls.AndroidKeysHandler;
import com.nutiteq.log.AndroidLogger;
import com.nutiteq.log.Log;
import com.nutiteq.ui.EventDrivenPanning;
import com.nutiteq.utils.Utils;

public class Map extends Activity {

    private boolean onRetainCalled;
    private MapView mapView;
    private BasicMapComponent mapComponent;

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

        // Create base map
        final Object savedMapComponent = getLastNonConfigurationInstance();
        if (savedMapComponent == null) {
            mapComponent = new SwisstopoComponent(new WgsPoint(lng, lat), ZOOM);
            mapComponent.setMap(new SwisstopoMap(getString(R.string.base_url_pixel), VDR, ZOOM));
            // final MemoryCache memoryCache = new MemoryCache(1024 * 1024);
            // mapComponent.setNetworkCache(memoryCache);
            mapComponent.setNetworkCache(new MemoryCache(0));
            // mapComponent.setPanningStrategy(new ThreadDrivenPanning());
            mapComponent.setPanningStrategy(new EventDrivenPanning());
            mapComponent.setControlKeysHandler(new AndroidKeysHandler());
            mapComponent.startMapping();
            mapComponent.setTouchClickTolerance(BasicMapComponent.FINGER_CLICK_TOLERANCE);
        } else {
            mapComponent = (BasicMapComponent) savedMapComponent;
        }

        Log.setLogger(new AndroidLogger(APP));
        Log.enableAll();

        final RelativeLayout relativeLayout = new RelativeLayout(this);
        setContentView(relativeLayout);

        // Map View
        mapView = new MapView(this, mapComponent);
        final RelativeLayout.LayoutParams mapViewLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        relativeLayout.addView(mapView, mapViewLayoutParams);
        mapView.setClickable(true);
        mapView.setEnabled(true);

        // Zoom
        ZoomControls zoomControls = new ZoomControls(this);
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
        final RelativeLayout.LayoutParams zoomControlsLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        zoomControlsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        zoomControlsLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        relativeLayout.addView(zoomControls, zoomControlsLayoutParams);

        // GPS Location
        // final LocationSource locationSource = new AndroidGPSProvider(
        // (LocationManager) getSystemService(Context.LOCATION_SERVICE), 1000L);
        // final LocationMarker marker = new NutiteqLocationMarker(new
        // PlaceIcon(Utils
        // .createImage("/res/drawable/marker.png"), 16, 32), 0, false);
        // locationSource.setLocationMarker(marker);
        // mapComponent.setLocationSource(locationSource);

        // Markers
        mapComponent.addPlace(new Place(1, "PSE - EPFL", Utils
                .createImage("/res/drawable/marker.png"), new WgsPoint(6.562794, 46.517705)));
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