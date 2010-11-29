package com.camptocamp.android.gis;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MapView;
import com.nutiteq.cache.Cache;
import com.nutiteq.components.PlaceIcon;
import com.nutiteq.components.WgsBoundingBox;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.controls.AndroidKeysHandler;
import com.nutiteq.location.NutiteqLocationMarker;
import com.nutiteq.location.providers.AndroidGPSProvider;
import com.nutiteq.log.AndroidLogger;
import com.nutiteq.log.Log;
import com.nutiteq.maps.GeoMap;
import com.nutiteq.maps.OpenStreetMap;
import com.nutiteq.ui.EventDrivenPanning;
import com.nutiteq.utils.Utils;

public class Map extends Activity {

    public static final String D = "C2C:";
    public static final String APP = "c2c-android-gis";
    public static final int ZOOM = 14;
    public static final String PKG = "com.camptocamp.android.gis";
    private static final String TAG = D + "Map";

    public static final String ACTION_GOTO = "action_goto";
    public static final String EXTRA_LABEL = "extra_label";
    public static final String EXTRA_MINX = "extra_minx"; // MapPos (px)
    public static final String EXTRA_MINY = "extra_miny";
    public static final String EXTRA_MAXX = "extra_maxx";
    public static final String EXTRA_MAXY = "extra_maxy";

    private int mCurrentMenu = MENU_MAP_ST_PIXEL;
    private static final int MENU_MAP_ST_PIXEL = 0;
    private static final int MENU_MAP_ST_ORTHO = 1;
    private static final int MENU_MAP_OSM = 2;
    // private static final int MENU_MAP_WMS = 3;
    private static final int MENU_PREFS = 4;
    private static final String PLACEHOLDER = "placeholder";

    private List<String> mSelectedLayers = new ArrayList<String>();

    private boolean isTrackingPosition = false;
    private boolean onRetainCalled = false;
    private int mWidth = 1;
    private int mHeight = 1;
    private Context ctxt;
    private RelativeLayout mapLayout;
    private MapView mapView = null;
    private BasicMapComponent mapComponent = null;
    private C2CCaching cache = null;

    private final double lat = 46.517815; // X: 152'210
    private final double lng = 6.562805; // Y: 532'790

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctxt = getApplicationContext();
        Log.setLogger(new AndroidLogger(APP));
        Log.enableAll();
        // Debug.startMethodTracing("Map");

        onRetainCalled = false;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        mapLayout = ((RelativeLayout) findViewById(R.id.map));

        // Width and Height
        Display display = getWindowManager().getDefaultDisplay();
        mWidth = display.getWidth();
        mHeight = display.getHeight();

        // Set default map
        setMapComponent(new SwisstopoComponent(new WgsPoint(lng, lat), mWidth, mHeight, ZOOM),
                new SwisstopoMap(getString(R.string.st_url_pixel),
                        getString(R.string.vendor_swisstopo), ZOOM));
        setMapView();

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
        final AndroidGPSProvider locationSource = new C2CGpsProvider(Map.this);
        locationSource.setLocationMarker(new NutiteqLocationMarker(new PlaceIcon(Utils
                .createImage("/res/drawable/marker.png"), 8, 8), 0, true));
        final ImageButton btn_gps = (ImageButton) findViewById(R.id.position_track);

        // Hack to get GPS Status (calls LocationListener.onProviderDisabled)
        // locationSource.start();
        // locationSource.quit();

        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTrackingPosition) {
                    Toast.makeText(Map.this, R.string.toast_gps_stop, Toast.LENGTH_SHORT).show();
                    locationSource.quit();
                } else {
                    Toast.makeText(Map.this, R.string.toast_gps_start, Toast.LENGTH_SHORT).show();
                    mapComponent.setLocationSource(locationSource);
                }
                isTrackingPosition = !isTrackingPosition;
            }
        });

        // Switch overlay
        final ImageButton btn_overlay = (ImageButton) findViewById(R.id.switch_overlay_test);
        btn_overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Layers only for Swisstopo maps (FIXME: Be generic)
                if (mCurrentMenu == MENU_MAP_ST_PIXEL || mCurrentMenu == MENU_MAP_ST_ORTHO) {
                    setOverlay(new SwisstopoOverlay(getString(R.string.overlay_swisstopo_data)));
                } else if (mCurrentMenu == MENU_MAP_OSM) {
                    setOverlay(new OsmOverlay(getString(R.string.osm_overlay_contours)));
                } else {
                    Toast.makeText(Map.this, R.string.toast_no_layer, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Search Bar
        findViewById(R.id.search_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchRequested();
            }
        });

        handleIntent();

        // Markers
        // mapComponent.addPlace(new Place(1, "PSE - EPFL", Utils
        // .createImage("/res/drawable/marker.png"), new WgsPoint(6.562794,
        // 46.517705)));
        // 6.562794, 46.517705

        // Debug.stopMethodTracing();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        // Goto Place
        if (ACTION_GOTO.equals(intent.getAction())) {

            ((TextView) findViewById(R.id.search_query))
                    .setText(intent.getStringExtra(EXTRA_LABEL));

            // Get positions in pixels
            double minx = intent.getDoubleExtra(EXTRA_MINX, 0);
            double miny = intent.getDoubleExtra(EXTRA_MINY, 0);
            double maxx = intent.getDoubleExtra(EXTRA_MAXX, 0);
            double maxy = intent.getDoubleExtra(EXTRA_MAXY, 0);

            // Positionning the map according to bbox
            GeoMap map = mapComponent.getMap();
            mapComponent.setZoom(map.getMinZoom());
            WgsPoint min = new WgsPoint(minx, miny);
            WgsPoint max = new WgsPoint(maxx, maxy);
            mapComponent.setBoundingBox(new WgsBoundingBox(min, max));

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSelectedLayers.clear();
        if (mapView != null) {
            mapView.clean();
            mapView = null;
        }
        if (!onRetainCalled) {
            android.util.Log.v(TAG, "onDestroy(): clean mapComponent");
            if (mapComponent != null) {
                mapComponent.stopMapping();
                mapComponent = null;
            }
            cleanCaches();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        onRetainCalled = true;
        return mapComponent;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        menu.add(0, MENU_MAP_ST_PIXEL, 0, R.string.menu_swisstopo_pixel).setIcon(
                android.R.drawable.ic_menu_mapmode);
        menu.add(0, MENU_MAP_ST_ORTHO, 1, R.string.menu_swisstopo_ortho).setIcon(
                android.R.drawable.ic_menu_mapmode);
        menu.add(1, MENU_MAP_OSM, 2, R.string.menu_osm).setIcon(android.R.drawable.ic_menu_mapmode);
        // menu.add(2, MENU_MAP_WMS, 3, R.string.menu_wms_example).setIcon(
        // android.R.drawable.ic_menu_mapmode);
        menu.add(2, MENU_PREFS, 4, R.string.menu_prefs).setIcon(
                android.R.drawable.ic_menu_preferences);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        mCurrentMenu = item.getItemId();
        int zoom = ZOOM;
        WgsPoint pt = new WgsPoint(lng, lat);
        if (mapComponent != null && mCurrentMenu != MENU_PREFS) {
            zoom = mapComponent.getZoom();
            pt = mapComponent.getMiddlePoint();
            mapComponent.stopMapping();
            mapComponent = null;
            cleanCaches();
        }
        switch (mCurrentMenu) {
        case MENU_MAP_ST_PIXEL:
            setMapComponent(new SwisstopoComponent(pt, mWidth, mHeight, zoom), new SwisstopoMap(
                    getString(R.string.st_url_pixel), getString(R.string.vendor_swisstopo), zoom));
            break;
        case MENU_MAP_ST_ORTHO:
            setMapComponent(new SwisstopoComponent(pt, mWidth, mHeight, zoom), new SwisstopoMap(
                    getString(R.string.st_url_ortho), getString(R.string.vendor_swisstopo), zoom));
            break;
        case MENU_MAP_OSM:
            setMapComponent(new C2CMapComponent(pt, mWidth, mHeight, zoom), OpenStreetMap.MAPNIK);
            break;
        // case MENU_MAP_WMS:
        // SimpleWMSMap wms = new SimpleWMSMap(
        // "http://iceds.ge.ucl.ac.uk/cgi-bin/icedswms?VERSION=1.1.1&SRS=EPSG:4326",
        // 256,
        // 0, 18, "bluemarble,cities,countries", "image/jpeg", "default",
        // "GetMap",
        // getString(R.string.vendor_wms));
        // wms.setWidthHeightRatio(2.0);
        // setMapComponent(new C2CMapComponent(pt, mWidth, mHeight, 3), wms);
        // break;

        case MENU_PREFS:
            startActivity(new Intent(Map.this, Prefs.class));
            return true;
        default:
            setMapComponent(new SwisstopoComponent(pt, mWidth, mHeight, ZOOM), new SwisstopoMap(
                    getString(R.string.st_url_pixel), getString(R.string.vendor_swisstopo), zoom));
        }
        setMapView();
        return true;
    }

    private void setMapComponent(final BasicMapComponent bmc, final GeoMap gm) {
        final Object savedMapComponent = getLastNonConfigurationInstance();
        if (savedMapComponent == null) {
            bmc.setMap(gm);
            cleanCaches();
            cache = new C2CCaching(ctxt);
            bmc.setNetworkCache(cache);
            // bmc.setImageProcessor(new NightModeImageProcessor());
            // bmc.setPanningStrategy(new ThreadDrivenPanning());
            bmc.setPanningStrategy(new EventDrivenPanning());
            bmc.setControlKeysHandler(new AndroidKeysHandler());
            bmc.startMapping();
            bmc.setTouchClickTolerance(BasicMapComponent.FINGER_CLICK_TOLERANCE);
            mapComponent = bmc;
        } else {
            mapComponent = (BasicMapComponent) savedMapComponent;
        }
    }

    private void setMapView() {
        if (mapView != null) {
            mapView.clean();
            mapLayout.removeView(mapView);
            mapView = null;
        }
        mapView = new MapView(ctxt, mapComponent);
        mapLayout.addView(mapView);
        mapView.setClickable(true);
        mapView.setEnabled(true);
    }

    private void setOverlay(final C2COverlay overlay) {
        if (overlay.layers_all != null) {
            int len = overlay.layers_all.size();
            final String[] layers_keys = (String[]) overlay.layers_all.keySet().toArray(
                    new String[len]);
            // Replace with string resource
            Resources r = getResources();
            String[] layers_names = new String[len];
            for (int i = 0; i < len; i++) {
                try {
                    layers_names[i] = getString(r.getIdentifier(layers_keys[i], "string", PKG));
                } catch (Exception e) {
                    android.util.Log.e(TAG, e.getMessage());
                }
            }

            // Get overlays status
            boolean[] layers_states = new boolean[len];
            for (int i = 0; i < len; i++) {
                layers_states[i] = mSelectedLayers.contains(overlay.layers_all.get(layers_keys[i]));
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(Map.this);
            dialog.setTitle(R.string.dialog_layer_title);
            dialog.setMultiChoiceItems(layers_names, layers_states,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (isChecked) {
                                mSelectedLayers.add(overlay.layers_all.get(layers_keys[which]));
                            } else {
                                mSelectedLayers.remove(overlay.layers_all.get(layers_keys[which]));
                            }
                        }
                    });
            dialog.setNeutralButton(R.string.btn_apply, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GeoMap gm = mapComponent.getMap();
                    if (mSelectedLayers.size() > 0) {
                        overlay.layers_selected = TextUtils.join(",", mSelectedLayers.toArray());
                        gm.addTileOverlay(overlay);
                    } else {
                        gm.addTileOverlay(null);
                    }
                    mapComponent.refreshTileOverlay();
                }
            });
            dialog.show();
        } else {
            GeoMap gm = mapComponent.getMap();
            if (mSelectedLayers.size() == 0) {
                mSelectedLayers.add(PLACEHOLDER);
                Toast.makeText(Map.this, R.string.toast_overlay_added, Toast.LENGTH_SHORT).show();
                gm.addTileOverlay(overlay);
            } else {
                mSelectedLayers.remove(PLACEHOLDER);
                gm.addTileOverlay(null);
                Toast.makeText(Map.this, R.string.toast_overlay_removed, Toast.LENGTH_SHORT).show();
            }
            mapComponent.refreshTileOverlay();
        }
    }

    private void cleanCaches() {
        if (cache != null) {
            final Cache[] cl = cache.getCacheLevels();
            for (int i = 0; i < cl.length; i++) {
                cl[i].deinitialize();
            }
            cache = null;
        }
    }

    /**
     * GPS custom provider
     */
    private static class C2CGpsProvider extends AndroidGPSProvider {

        private WeakReference<Map> mActivity;

        public C2CGpsProvider(Map a) {
            super((LocationManager) a.getSystemService(Context.LOCATION_SERVICE), 1000L);
            mActivity = new WeakReference<Map>(a);
        }

        @Override
        public void onProviderDisabled(final String provider) {
            final Map a = mActivity.get();
            if (a != null) {
                a.isTrackingPosition = false;
                a.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                Toast.makeText(a, R.string.toast_gps_disabled, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
