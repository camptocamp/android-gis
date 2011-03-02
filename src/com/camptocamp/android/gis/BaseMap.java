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

import com.camptocamp.android.gis.control.DirectionsWaiter;
import com.camptocamp.android.gis.layer.Overlay;
import com.camptocamp.android.gis.utils.Caching;
import com.camptocamp.android.gis.utils.ExportGPX;
import com.camptocamp.android.gis.utils.ExportKML;
import com.camptocamp.android.gis.utils.ExportTrace;
import com.camptocamp.android.gis.utils.GpsProvider;
import com.camptocamp.android.gis.utils.Prefs;
import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MapView;
import com.nutiteq.cache.Cache;
import com.nutiteq.components.WgsBoundingBox;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.controls.AndroidKeysHandler;
import com.nutiteq.maps.GeoMap;
import com.nutiteq.net.NutiteqDownloadCounter;
import com.nutiteq.services.YourNavigationDirections;
import com.nutiteq.ui.NutiteqDownloadDisplay;
import com.nutiteq.ui.ThreadDrivenPanning;

//FIXME: Rename to Map, then Map to ~MapOpenStreetMap
public class BaseMap extends Activity {

    public static final String D = "C2C:";
    public static final String PKG = "com.camptocamp.android.gis";
    public static final String APP = "c2c-android-gis";
    private static final String TAG = D + "BaseMap";
    private static final String PLACEHOLDER = "placeholder";

    public static final String ACTION_GOTO = PKG + ".action.GOTO";
    public static final String ACTION_ROUTE = PKG + ".action.ROUTE";
    public static final String ACTION_TOAST = PKG + ".action.TOAST";
    public static final String ACTION_PICK = PKG + ".action.PICK";
    public static final String EXTRA_LABEL = "extra_label";
    public static final String EXTRA_MINLON = "extra_minx";
    public static final String EXTRA_MINLAT = "extra_miny";
    public static final String EXTRA_MAXLON = "extra_maxx";
    public static final String EXTRA_MAXLAT = "extra_maxy";
    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_MSG = "extra_msg";
    public static final String EXTRA_FIELD = "extra_field";
    public static final String EXTRA_COORD = "extra_coord";
    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LON = "extra_lon";

    protected static final int MENU_PREFS = 90;
    protected static final int MENU_RECORD = 91;
    protected static final int MENU_DIRECTION = 92;

    protected SharedPreferences prefs;
    protected String search_query = "";
    protected int mWidth = 1;
    protected int mHeight = 1;
    protected Context ctxt;
    protected RelativeLayout mapLayout;
    protected boolean firstStart = true;
    private MapView mapView = null;
    private List<String> mSelectedLayers;
    private boolean onRetainCalled = false;
    private DirectionsWaiter waiter;

    protected MapComponent mapComponent = null;

    protected boolean isTrackingPosition = false;

    protected int mProvider;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        super.onCreate(savedInstanceState);

        ctxt = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        mSelectedLayers = new ArrayList<String>();
        mapLayout = ((RelativeLayout) findViewById(R.id.map));
        waiter = new DirectionsWaiter(BaseMap.this);
        onRetainCalled = false;

        // com.nutiteq.log.Log.setLogger(new
        // com.nutiteq.log.AndroidLogger(APP));
        // com.nutiteq.log.Log.enableAll();

        // Width and Height
        Display display = getWindowManager().getDefaultDisplay();
        mWidth = display.getWidth();
        mHeight = display.getHeight();

        setDefaultMap();

        // GPS Location tracking
        final GpsProvider locationSource = new GpsProvider(BaseMap.this);
        final ImageButton btn_gps = (ImageButton) findViewById(R.id.position_track);
        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapComponent != null) {
                    if (isTrackingPosition) {
                        Toast.makeText(BaseMap.this, R.string.toast_gps_stop, Toast.LENGTH_SHORT)
                                .show();
                        locationSource.quit();
                    }
                    else {
                        Toast.makeText(BaseMap.this, R.string.toast_gps_start, Toast.LENGTH_SHORT)
                                .show();
                        mapComponent.setLocationSource(locationSource);
                    }
                    isTrackingPosition = !isTrackingPosition;
                }
            }
        });

        handleIntent();

        // FIXME: KML TESTS
        // KmlUrlReader kml = new KmlUrlReader(
        // "http://www.panoramio.com/panoramio.kml?LANG=en_US.utf8&", true);
        // mapComponent.addKmlService(kml);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanCaches();
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
    public void onLowMemory() {
        Log.i(TAG, "onLowMemory()");
        cleanCaches();
        initCaches();
        if (mapComponent != null) {
            mapComponent.moveMap(mapComponent.getCenterPoint());
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        android.util.Log.v(TAG, "onRetainNonConfigurationInstance");
        onRetainCalled = true;
        return mapComponent;
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
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
        if (itemid == MENU_PREFS) {
            startActivity(new Intent(BaseMap.this, Prefs.class));
        }
        else if (itemid == MENU_RECORD) {
            // Record GPS trace
            final GpsProvider gpsProvider = (GpsProvider) mapComponent.getLocationSource();
            if (gpsProvider != null && gpsProvider.isRecord()) {
                gpsProvider.setRecord(false);
                item.setTitle(R.string.menu_record_start);
                item.setIcon(android.R.drawable.ic_media_play);
                // Save Traces dialog
                final AlertDialog.Builder dialog = new AlertDialog.Builder(BaseMap.this);
                dialog.setMessage(R.string.dialog_save_trace);
                dialog.setPositiveButton(R.string.btn_yes, new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveTrace(gpsProvider);
                    }
                });
                dialog.setNegativeButton(R.string.btn_no, null);
                dialog.show();
            }
            else if (isTrackingPosition) {
                gpsProvider.setRecord(true);
                item.setTitle(R.string.menu_record_stop);
                item.setIcon(android.R.drawable.ic_media_pause);
            }
        }
        else if (itemid == MENU_DIRECTION) {
            startActivity(new Intent(BaseMap.this, Directions.class));
        }
        return true;
    }

    protected void setDefaultMap() {
        // Override me
    }

    protected void setMapComponent(final MapComponent bmc, final GeoMap gm) {
        bmc.setMap(gm);
        bmc.setNetworkCache(new Caching(ctxt));
        // bmc.setImageProcessor(new NightModeImageProcessor());
        bmc.setPanningStrategy(new ThreadDrivenPanning());
        // bmc.setPanningStrategy(new EventDrivenPanning());
        bmc.setControlKeysHandler(new AndroidKeysHandler());
        bmc.setDownloadCounter(new NutiteqDownloadCounter());
        bmc.setDownloadDisplay(new NutiteqDownloadDisplay() {
            @Override
            public void downloadCompleted() {
                downloadComplete();
                super.downloadCompleted();
            }
        });
        bmc.startMapping();
        bmc.setTouchClickTolerance(BasicMapComponent.FINGER_CLICK_TOLERANCE);
        mapComponent = bmc;
    }

    protected void downloadComplete() {
        firstStart = false;
    }

    protected void saveTrace(GpsProvider gpsProvider) {
        if (gpsProvider.getTrace().size() > 0) {
            // Choose format
            int format = Integer.parseInt(prefs.getString(Prefs.KEY_TRACE_FORMAT,
                    Prefs.DEFAULT_TRACE_FORMAT));
            ExportTrace export;
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
            if (export != null && (file = export.export(gpsProvider.getTrace())) != "") {
                Toast.makeText(ctxt, String.format(getString(R.string.toast_trace_saved), file),
                        Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(ctxt, R.string.toast_trace_error, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(ctxt, R.string.toast_trace_empty, Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            onNewIntent(intent);
        }
    };

    protected void handleIntent() {
        final Intent intent = getIntent();
        String action = intent.getAction();

        Log.v(TAG, "intent=" + intent.toString());

        if (ACTION_PICK.equals(action)) {
            // Select place mode
            Toast.makeText(ctxt, R.string.toast_pick_point, Toast.LENGTH_SHORT).show();

            mapComponent.setMapListener(new MapView(ctxt, mapComponent) {
                @Override
                public void mapClicked(WgsPoint p) {
                    mapComponent.setMapListener(mapView);
                    Intent i = new Intent(BaseMap.this, Directions.class);
                    i.putExtra(EXTRA_FIELD, intent.getIntExtra(EXTRA_FIELD, R.id.start));
                    i.putExtra(EXTRA_COORD, p.getLon() + "," + p.getLat());
                    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    i.setAction(ACTION_PICK);
                    startActivity(i);
                }
            });

        }
        else if (ACTION_TOAST.equals(action)) {
            Toast.makeText(ctxt, intent.getStringExtra(EXTRA_MSG), Toast.LENGTH_SHORT).show();

        }
        else if (ACTION_GOTO.equals(action)) {
            // Get label
            if (intent.hasExtra(EXTRA_LABEL)) {
                search_query = intent.getStringExtra(EXTRA_LABEL);
                ((TextView) findViewById(R.id.search_query)).setText(search_query);
            }
            // Get coordinates or bbox
            if (intent.hasExtra(EXTRA_MINLAT)) {
                // Get positions and Zoom
                final double minx = intent.getDoubleExtra(EXTRA_MINLON, 0);
                final double miny = intent.getDoubleExtra(EXTRA_MINLAT, 0);
                final double maxx = intent.getDoubleExtra(EXTRA_MAXLON, 0);
                final double maxy = intent.getDoubleExtra(EXTRA_MAXLAT, 0);
                zoomToBbox(new WgsPoint(minx, miny), new WgsPoint(maxx, maxy));
            }
            else if (intent.hasExtra(EXTRA_LAT) && intent.hasExtra(EXTRA_LON)) {
                // FIXME: There is a problem zooming more than 1 level at once
                mapComponent.zoomIn();
                mapComponent.zoomIn();
                mapComponent.zoomIn();
                mapComponent.zoomIn();
                mapComponent.setMiddlePoint(new WgsPoint(intent.getDoubleExtra(EXTRA_LON, 0),
                        intent.getDoubleExtra(EXTRA_LAT, 0)));
                // Open label
                mapComponent.pointerReleasedManual(mapComponent.getWidth() / 2, mapComponent
                        .getHeight() / 2);
            }

        }
        else if (ACTION_ROUTE.equals(action)) {
            // Modal dialog
            final ProgressDialog dialog = ProgressDialog.show(BaseMap.this,
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
            }
            else if (startx > endx) {
                if (starty > endy) {
                    final WgsPoint tmp = to;
                    to = from;
                    from = tmp;
                }
                else {
                    from = new WgsPoint(endx, starty);
                    to = new WgsPoint(startx, endy);
                }
            }
            zoomToBbox(from, to);
        }
    }

    protected void setMapView() {
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

    protected void setOverlay(final Overlay overlay) {
        if (overlay.getLayersAll() != null) {
            int len = overlay.getLayersAll().size();
            final String[] layers_keys = (String[]) overlay.getLayersAll().keySet().toArray(
                    new String[len]);
            // Replace with string resource
            Resources r = getResources();
            String[] layers_names = new String[len];
            for (int i = 0; i < len; i++) {
                try {
                    layers_names[i] = getString(r.getIdentifier(layers_keys[i], "string", PKG));
                }
                catch (Exception e) {
                    android.util.Log.e(TAG, e.getMessage());
                }
            }

            // Get overlays status
            boolean[] layers_states = new boolean[len];
            for (int i = 0; i < len; i++) {
                layers_states[i] = mSelectedLayers.contains(overlay.getLayersAll().get(
                        layers_keys[i]));
            }
            final AlertDialog.Builder dialog = new AlertDialog.Builder(BaseMap.this);
            dialog.setTitle(R.string.dialog_layer_title);
            dialog.setMultiChoiceItems(layers_names, layers_states,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (isChecked) {
                                mSelectedLayers.add(overlay.getLayersAll().get(layers_keys[which]));
                            }
                            else {
                                mSelectedLayers.remove(overlay.getLayersAll().get(
                                        layers_keys[which]));
                            }
                        }
                    });
            dialog.setNeutralButton(R.string.btn_apply, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GeoMap gm = mapComponent.getMap();
                    if (mSelectedLayers.size() > 0) {
                        overlay.setLayersSelected(TextUtils.join(",", mSelectedLayers.toArray()));
                        gm.addTileOverlay(overlay);
                    }
                    else {
                        gm.addTileOverlay(null);
                    }
                    mapComponent.refreshTileOverlay();
                }
            });
            dialog.show();
        }
        else {
            GeoMap gm = mapComponent.getMap();
            if (mSelectedLayers.size() == 0) {
                mSelectedLayers.add(PLACEHOLDER);
                Toast.makeText(BaseMap.this, R.string.toast_overlay_added, Toast.LENGTH_SHORT)
                        .show();
                gm.addTileOverlay(overlay);
            }
            else {
                mSelectedLayers.remove(PLACEHOLDER);
                gm.addTileOverlay(null);
                Toast.makeText(BaseMap.this, R.string.toast_overlay_removed, Toast.LENGTH_SHORT)
                        .show();
            }
            mapComponent.refreshTileOverlay();
        }
    }

    protected void cleanCaches() {
        if (mapComponent != null) {
            Caching cache = (Caching) mapComponent.getCache();
            if (cache != null) {
                final Cache[] cl = cache.getCacheLevels();
                for (int i = 0; i < cl.length; i++) {
                    cl[i].deinitialize();
                }
                // cache = null;
                System.gc();
            }
        }
    }

    protected void initCaches() {
        if (mapComponent != null) {
            Caching cache = (Caching) mapComponent.getCache();
            if (cache != null) {
                final Cache[] cl = cache.getCacheLevels();
                for (int i = 0; i < cl.length; i++) {
                    cl[i].initialize();
                }
            }
        }
    }

    private void zoomToBbox(WgsPoint min, WgsPoint max) {
        mapComponent.setZoom(mapComponent.getMap().getMinZoom());
        mapComponent.setBoundingBox(new WgsBoundingBox(min, max));
    }

    public MapComponent getMapComponent() {
        return mapComponent;
    }

    public boolean isTrackingPosition() {
        return isTrackingPosition;
    }

    public void setTrackingPosition(boolean isTrackingPosition) {
        this.isTrackingPosition = isTrackingPosition;
    }
}
