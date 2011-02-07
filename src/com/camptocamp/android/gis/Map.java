package com.camptocamp.android.gis;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
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
import com.nutiteq.components.WgsBoundingBox;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.controls.AndroidKeysHandler;
import com.nutiteq.location.LocationSource;
import com.nutiteq.log.AndroidLogger;
import com.nutiteq.maps.GeoMap;
import com.nutiteq.services.YourNavigationDirections;
import com.nutiteq.ui.NutiteqDownloadDisplay;
import com.nutiteq.ui.ThreadDrivenPanning;

public class Map extends Activity {

    protected static final String D = "C2C:";
    protected static final String APP = "c2c-android-gis";
    protected static final String PKG = "com.camptocamp.android.gis";
    private static final String TAG = D + "Map";
    private static final String PLACEHOLDER = "placeholder";
    private static final String OSM_MAPNIK_URL = "http://tile.openstreetmap.org/";
    private static final double LAT = 46.858423; // X: 190'000
    private static final double LNG = 8.225458; // Y: 660'000

    protected static final String ACTION_GOTO = "action_goto";
    protected static final String ACTION_ROUTE = "action_route";
    protected static final String ACTION_TOAST = "action_toast";
    protected static final String ACTION_PICK = "action_pick";
    protected static final String EXTRA_LABEL = "extra_label";
    protected static final String EXTRA_MINLON = "extra_minx";
    protected static final String EXTRA_MINLAT = "extra_miny";
    protected static final String EXTRA_MAXLON = "extra_maxx";
    protected static final String EXTRA_MAXLAT = "extra_maxy";
    protected static final String EXTRA_TYPE = "extra_type";
    protected static final String EXTRA_MSG = "extra_msg";
    protected static final String EXTRA_FIELD = "extra_field";
    protected static final String EXTRA_COORD = "extra_coord";

    private static final int MENU_MAP_ST_PIXEL = 0;
    private static final int MENU_MAP_ST_ORTHO = 1;
    private static final int MENU_MAP_OSM = 2;
    private static final int MENU_PREFS = 3;
    private static final int MENU_RECORD = 4;
    private static final int MENU_DIRECTION = 5;

    private SharedPreferences prefs;
    private List<String> mSelectedLayers = new ArrayList<String>();
    private boolean onRetainCalled = false;
    private int mWidth = 1;
    private int mHeight = 1;
    private Context ctxt;
    private RelativeLayout mapLayout;
    private MapView mapView = null;
    private String search_query = "";
    private C2CDirectionsWaiter waiter;

    protected C2CMapComponent mapComponent = null;
    protected boolean isTrackingPosition = false;
    private int mProvider;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctxt = getApplicationContext();

        com.nutiteq.log.Log.setLogger(new AndroidLogger(APP));
        com.nutiteq.log.Log.enableAll();

        onRetainCalled = false;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        mapLayout = ((RelativeLayout) findViewById(R.id.map));
        waiter = new C2CDirectionsWaiter(Map.this);

        // Width and Height
        Display display = getWindowManager().getDefaultDisplay();
        mWidth = display.getWidth();
        mHeight = display.getHeight();

        // Set default map
        prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        selectMap(Integer.parseInt(prefs.getString(Prefs.KEY_PROVIDER, Prefs.DEFAULT_PROVIDER)));

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
        final C2CGpsProvider locationSource = new C2CGpsProvider(Map.this);
        final ImageButton btn_gps = (ImageButton) findViewById(R.id.position_track);
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
                if (mProvider == MENU_MAP_ST_PIXEL || mProvider == MENU_MAP_ST_ORTHO) {
                    setOverlay(new SwisstopoOverlay(getString(R.string.overlay_swisstopo_data)));
                } else if (mProvider == MENU_MAP_OSM) {
                    setOverlay(new OsmOverlay(getString(R.string.osm_overlay)));
                } else {
                    Toast.makeText(Map.this, R.string.toast_no_layer, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Search Bar
        findViewById(R.id.search_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch(search_query, false, null, false);
            }
        });

        handleIntent();

        // FIXME: KML TESTS
        // KmlUrlReader kml = new KmlUrlReader(
        // "http://www.panoramio.com/panoramio.kml?LANG=en_US.utf8&", true);
        // mapComponent.addKmlService(kml);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            onNewIntent(intent);
        }
    };

    private void handleIntent() {
        final Intent intent = getIntent();
        String action = intent.getAction();

        Log.v(TAG, "intent=" + intent.toString());

        if (ACTION_PICK.equals(action)) {
            // Select place mode
            Toast.makeText(ctxt, "FIXME: Tap your point on the map!", Toast.LENGTH_SHORT);

            mapComponent.setMapListener(new C2CMapView(ctxt, mapComponent) {
                @Override
                public void mapClicked(WgsPoint p) {
                    mapComponent.setMapListener(mapView);
                    Intent i = new Intent(Map.this, C2CDirections.class);
                    i.putExtra(EXTRA_FIELD, intent.getIntExtra(Map.EXTRA_FIELD, R.id.start));
                    i.putExtra(EXTRA_COORD, p.getLon() + "," + p.getLat());
                    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    i.setAction(Map.ACTION_PICK);
                    startActivity(i);
                }
            });

        } else if (ACTION_TOAST.equals(action)) {
            Toast.makeText(ctxt, intent.getStringExtra(EXTRA_MSG), Toast.LENGTH_SHORT).show();

        } else if (ACTION_GOTO.equals(action)) {
            search_query = intent.getStringExtra(EXTRA_LABEL);
            ((TextView) findViewById(R.id.search_query)).setText(search_query);

            // Get positions and Zoom
            final double minx = intent.getDoubleExtra(EXTRA_MINLON, 0);
            final double miny = intent.getDoubleExtra(EXTRA_MINLAT, 0);
            final double maxx = intent.getDoubleExtra(EXTRA_MAXLON, 0);
            final double maxy = intent.getDoubleExtra(EXTRA_MAXLAT, 0);
            zoomToBbox(new WgsPoint(minx, miny), new WgsPoint(maxx, maxy));

        } else if (ACTION_ROUTE.equals(action)) {
            // Modal dialog
            final ProgressDialog dialog = ProgressDialog.show(Map.this,
                    getString(R.string.dialog_route_create_title),
                    getString(R.string.dialog_route_create_messg));
            dialog.setCancelable(true);

            // Get points
            final double startx = intent.getDoubleExtra(EXTRA_MINLON, 0);
            final double starty = intent.getDoubleExtra(EXTRA_MINLAT, 0);
            final double endx = intent.getDoubleExtra(EXTRA_MAXLON, 0);
            final double endy = intent.getDoubleExtra(EXTRA_MAXLAT, 0);
            final String type = intent.getStringExtra(EXTRA_TYPE);
            WgsPoint from = new WgsPoint(startx, starty);
            WgsPoint to = new WgsPoint(endx, endy);

            // Get route and draw it
            YourNavigationDirections yours = new YourNavigationDirections(waiter, from, to, type,
                    YourNavigationDirections.ROUTE_TYPE_FASTEST) {
                @Override
                public void dataRetrieved(final byte[] data) {
                    super.dataRetrieved(data);
                    dialog.dismiss();
                }

                @Override
                public void notifyError() {
                    super.notifyError();
                    dialog.dismiss();
                }
            };
            mapComponent.enqueueDownload(yours, Cache.CACHE_LEVEL_NONE);

            // OpenLSDirections ols = new OpenLSDirections(waiter,
            // OpenLSDirections.NUTITEQ_DEFAULT_SERVICE_URL, "en-US", from, to);
            // mapComponent.enqueueDownload(ols, Cache.CACHE_LEVEL_NONE);

            // Zoom to Route
            if (startx < endx && starty > endy) {
                from = new WgsPoint(startx, endy);
                to = new WgsPoint(endx, starty);
            } else if (startx > endx) {
                if (starty > endy) {
                    final WgsPoint tmp = to;
                    to = from;
                    from = tmp;
                } else {
                    from = new WgsPoint(endx, starty);
                    to = new WgsPoint(startx, endy);
                }
            }
            zoomToBbox(from, to);
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
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Listening for events
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TOAST);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        System.gc();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        android.util.Log.v(TAG, "onRetainNonConfigurationInstance");
        onRetainCalled = true;
        return mapComponent;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        menu.add(0, MENU_MAP_ST_PIXEL, MENU_MAP_ST_PIXEL, R.string.menu_swisstopo_pixel).setIcon(
                android.R.drawable.ic_menu_mapmode);
        menu.add(0, MENU_MAP_ST_ORTHO, MENU_MAP_ST_ORTHO, R.string.menu_swisstopo_ortho).setIcon(
                android.R.drawable.ic_menu_mapmode);
        menu.add(1, MENU_MAP_OSM, MENU_MAP_OSM, R.string.menu_osm).setIcon(
                android.R.drawable.ic_menu_mapmode);
        menu.add(2, MENU_PREFS, MENU_PREFS, R.string.menu_prefs).setIcon(
                android.R.drawable.ic_menu_preferences);
        menu.add(3, MENU_RECORD, MENU_RECORD, R.string.menu_record_start).setIcon(
                android.R.drawable.ic_media_play);
        menu.add(3, MENU_DIRECTION, MENU_DIRECTION, R.string.menu_direction).setIcon(
                android.R.drawable.ic_menu_directions);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        final int itemid = item.getItemId();
        if (itemid <= MENU_MAP_OSM) {
            selectMap(itemid);
        } else if (itemid == MENU_PREFS) {
            startActivity(new Intent(Map.this, Prefs.class));
        } else if (itemid == MENU_RECORD) {
            // Record GPS trace
            final C2CGpsProvider pr = (C2CGpsProvider) mapComponent.getLocationSource();
            if (pr != null && pr.record) {
                pr.setRecord(false);
                item.setTitle(R.string.menu_record_start);
                item.setIcon(android.R.drawable.ic_media_play);
                // Save Traces dialog
                final AlertDialog.Builder dialog = new AlertDialog.Builder(Map.this);
                dialog.setMessage(R.string.dialog_save_trace);
                dialog.setPositiveButton(R.string.btn_yes, new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveTrace(pr);
                    }
                });
                dialog.setNegativeButton(R.string.btn_no, null);
                dialog.show();
            } else if (isTrackingPosition) {
                pr.setRecord(true);
                item.setTitle(R.string.menu_record_stop);
                item.setIcon(android.R.drawable.ic_media_pause);
            }
        } else if (itemid == MENU_DIRECTION) {
            startActivity(new Intent(Map.this, C2CDirections.class));
        }
        return true;
    }

    protected void saveTrace(C2CGpsProvider pr) {
        if (pr.trace.size() > 0) {
            // Choose format
            int format = Integer.parseInt(prefs.getString(Prefs.KEY_TRACE_FORMAT,
                    Prefs.DEFAULT_TRACE_FORMAT));
            C2CExportTrace export;
            switch (format) {
            case 0:
                export = new ExportGPX();
                break;
            case 1:
                export = new ExportKML();
                break;
            default:
                export = null;
            }
            // Export
            String file = "";
            if (export != null && (file = export.export(pr.trace)) != "") {
                Toast.makeText(ctxt, String.format(getString(R.string.toast_trace_saved), file),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ctxt, R.string.toast_trace_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ctxt, R.string.toast_trace_empty, Toast.LENGTH_SHORT).show();
        }
    }

    private void setMapComponent(final C2CMapComponent bmc, final GeoMap gm) {
        cleanCaches();
        bmc.setMap(gm);
        bmc.setNetworkCache(new C2CCaching(ctxt));
        // bmc.setImageProcessor(new NightModeImageProcessor());
        bmc.setPanningStrategy(new ThreadDrivenPanning());
        bmc.setControlKeysHandler(new AndroidKeysHandler());
        bmc.setDownloadCounter(new C2CDownloadCounter());
        bmc.setDownloadDisplay(new NutiteqDownloadDisplay());
        bmc.startMapping();
        bmc.setTouchClickTolerance(BasicMapComponent.FINGER_CLICK_TOLERANCE);
        mapComponent = bmc;
        // return bmc;
    }

    private void selectMap(int provider_id) {
        LocationSource loc = null;
        mProvider = provider_id;
        int zoom = -1;
        WgsPoint pt = new WgsPoint(LNG, LAT);
        // Reset mapComponent
        if (mapComponent != null) {
            // Save zoom
            zoom = mapComponent.getZoom();
            if (mapComponent instanceof SwisstopoComponent && provider_id == MENU_MAP_OSM) {
                zoom -= 7;
            } else if (mapComponent instanceof C2CMapComponent) {
                zoom += 7;
            }
            // Save point
            pt = mapComponent.getMiddlePoint();
            // Save location provider
            if (isTrackingPosition) {
                loc = mapComponent.getLocationSource();
            }

            mapComponent.stopMapping();
            mapComponent = null;
        }
        // Select map
        final Object savedMapComponent = getLastNonConfigurationInstance();
        if (savedMapComponent == null) {
            switch (provider_id) {
            case MENU_MAP_ST_PIXEL:
                setMapComponent(new SwisstopoComponent(pt, mWidth, mHeight, zoom),
                        new SwisstopoMap(getString(R.string.st_url_pixel),
                                getString(R.string.vendor_swisstopo), zoom));
                break;
            case MENU_MAP_ST_ORTHO:
                setMapComponent(new SwisstopoComponent(pt, mWidth, mHeight, zoom),
                        new SwisstopoMap(getString(R.string.st_url_ortho),
                                getString(R.string.vendor_swisstopo), zoom));
                break;
            case MENU_MAP_OSM:
                setMapComponent(new C2CMapComponent(pt, mWidth, mHeight, zoom), new OpenStreetMap(
                        OSM_MAPNIK_URL, OpenStreetMap.TILE_SIZE, OpenStreetMap.MIN_ZOOM, 18));
                break;
            default:
                setMapComponent(new SwisstopoComponent(pt, mWidth, mHeight, zoom),
                        new SwisstopoMap(getString(R.string.st_url_pixel),
                                getString(R.string.vendor_swisstopo), zoom));
            }
            if (loc != null) {
                mapComponent.setLocationSource(loc);
            }
        } else {
            android.util.Log.v(TAG, "using savedMapComponent");
            mapComponent = (C2CMapComponent) savedMapComponent;
        }
        setMapView();
    }

    private void setMapView() {
        if (mapView != null) {
            mapView.clean();
            mapLayout.removeView(mapView);
            mapView = null;
        }
        mapView = new C2CMapView(ctxt, mapComponent);
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
            final AlertDialog.Builder dialog = new AlertDialog.Builder(Map.this);
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
        if (mapComponent != null) {
            C2CCaching cache = (C2CCaching) mapComponent.getCache();
            if (cache != null) {
                final Cache[] cl = cache.getCacheLevels();
                for (int i = 0; i < cl.length; i++) {
                    cl[i].deinitialize();
                }
                cache = null;
                System.gc();
            }
        }
    }

    private void zoomToBbox(WgsPoint min, WgsPoint max) {
        mapComponent.setZoom(mapComponent.getMap().getMinZoom());
        mapComponent.setBoundingBox(new WgsBoundingBox(min, max));
    }
}
