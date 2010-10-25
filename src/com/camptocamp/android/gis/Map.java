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

import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MapView;
import com.nutiteq.cache.MemoryCache;
import com.nutiteq.components.PlaceIcon;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.controls.AndroidKeysHandler;
import com.nutiteq.location.LocationSource;
import com.nutiteq.location.NutiteqLocationMarker;
import com.nutiteq.location.providers.AndroidGPSProvider;
import com.nutiteq.log.AndroidLogger;
import com.nutiteq.log.Log;
import com.nutiteq.maps.GeoMap;
import com.nutiteq.maps.OpenStreetMap;
import com.nutiteq.maps.SimpleWMSMap;
import com.nutiteq.ui.EventDrivenPanning;
import com.nutiteq.utils.Utils;

public class Map extends Activity {

    private boolean isTrackingPosition = false;
    private boolean onRetainCalled;
    private LinearLayout mapLayout;
    private MapView mapView = null;
    private BasicMapComponent mapComponent = null;

    public static final String D = "C2C:";
    private final static String TAG = D + "Map";
    public static final String APP = "c2c-android-gis";
    public static final String VDR = "Swisstopo";
    private static final int ZOOM = 14;
    private static final int MENU_MAP_PIXEL = 0;
    private static final int MENU_MAP_ORTHO = 1;
    private static final int MENU_MAP_OSM = 2;
    private static final int MENU_MAP_WMS = 3;

    private final double lat = 46.517815; // X: 152'210
    private final double lng = 6.562805; // Y: 532'790

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRetainCalled = false;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        mapLayout = ((LinearLayout) findViewById(R.id.map));

        // Set default map
        setMapComponent(new SwisstopoComponent(new WgsPoint(lng, lat), ZOOM), new SwisstopoMap(
                getString(R.string.base_url_pixel), VDR, ZOOM));
        setMapView();

        Log.setLogger(new AndroidLogger(APP));
        // Log.enableAll();

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
        final LocationManager locmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final LocationSource locationSource = new AndroidGPSProvider(locmanager, 1000L);
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
                    // if
                    // (locmanager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    // {
                    // Toast.makeText(Map.this, "GPS is disabled !",
                    // Toast.LENGTH_SHORT).show();
                    // }
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

    @Override
    public Object onRetainNonConfigurationInstance() {
        onRetainCalled = true;
        return mapComponent;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        menu.add(0, MENU_MAP_PIXEL, 0, "SwissTopo Pixel Map");
        menu.add(0, MENU_MAP_ORTHO, 1, "SwissTopo Orthophoto");
        menu.add(0, MENU_MAP_OSM, 2, "OpenStreetMap");
        menu.add(0, MENU_MAP_WMS, 3, "WMS Test");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        int zoom = mapComponent.getZoom();

        switch (item.getItemId()) {
        case MENU_MAP_PIXEL:
            setMapComponent(new SwisstopoComponent(mapComponent.getMiddlePoint(), zoom),
                    new SwisstopoMap(getString(R.string.base_url_pixel), VDR, zoom));
            break;
        case MENU_MAP_ORTHO:
            setMapComponent(new SwisstopoComponent(mapComponent.getMiddlePoint(), zoom),
                    new SwisstopoMap(getString(R.string.base_url_ortho), VDR, zoom));
            break;
        case MENU_MAP_OSM:
            setMapComponent(new C2CMapComponent(mapComponent.getMiddlePoint(), zoom),
                    OpenStreetMap.MAPNIK);
            break;
        case MENU_MAP_WMS:
            SimpleWMSMap wms = new SimpleWMSMap(
                    "http://iceds.ge.ucl.ac.uk/cgi-bin/icedswms?VERSION=1.1.1&SRS=EPSG:4326", 256,
                    0, 18, "bluemarble,cities,countries", "image/jpeg", "default", "GetMap",
                    "© UCL");
            wms.setWidthHeightRatio(2.0);
            setMapComponent(new C2CMapComponent(mapComponent.getMiddlePoint(), 3), wms);
            break;
        default:
            setMapComponent(new SwisstopoComponent(new WgsPoint(lng, lat), ZOOM), new SwisstopoMap(
                    getString(R.string.base_url_pixel), VDR, zoom));
        }
        setMapView();
        return true;
    }

    private void setMapComponent(final BasicMapComponent mc, final GeoMap gm) {
        final Object savedMapComponent = getLastNonConfigurationInstance();
        if (savedMapComponent == null) {
            mc.setMap(gm);
            mc.setNetworkCache(new MemoryCache(1024 * 1024));
            mc.setPanningStrategy(new EventDrivenPanning());
            mc.setControlKeysHandler(new AndroidKeysHandler());
            mc.startMapping();
            mc.setTouchClickTolerance(BasicMapComponent.FINGER_CLICK_TOLERANCE);
            mapComponent = mc;
        } else {
            mapComponent = (BasicMapComponent) savedMapComponent;
        }
    }

    private void setMapView() {
        if (mapView != null) {
            mapLayout.removeAllViews();
        }
        mapView = new MapView(getApplicationContext(), mapComponent);
        mapLayout.addView(mapView);
        mapView.setClickable(true);
        mapView.setEnabled(true);
    }
}