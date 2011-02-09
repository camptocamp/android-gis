package com.camptocamp.android.gis;

import java.util.ArrayList;
import java.util.List;

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

import com.camptocamp.android.gis.utils.ExportGPX;
import com.camptocamp.android.gis.utils.ExportKML;
import com.camptocamp.android.gis.utils.Prefs;
import com.google.android.maps.MapActivity;
import com.nutiteq.BasicMapComponent;
import com.nutiteq.cache.Cache;
import com.nutiteq.components.WgsBoundingBox;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.controls.AndroidKeysHandler;
import com.nutiteq.log.AndroidLogger;
import com.nutiteq.maps.GeoMap;
import com.nutiteq.services.YourNavigationDirections;
import com.nutiteq.ui.NutiteqDownloadDisplay;
import com.nutiteq.ui.ThreadDrivenPanning;

//FIXME: Rename to Map, then Map to ~MapOpenStreetMap
public abstract class BaseMap extends MapActivity {

    public static final String D = "C2C:";
    public static final String PKG = "com.camptocamp.android.gis";
    public static final String APP = "c2c-android-gis";
    private static final String TAG = D + "BaseMap";
    private static final String PLACEHOLDER = "placeholder";

    protected static final String ACTION_GOTO = PKG + ".action.GOTO";
    protected static final String ACTION_ROUTE = PKG + ".action.ROUTE";
    protected static final String ACTION_TOAST = PKG + ".action.TOAST";
    protected static final String ACTION_PICK = PKG + ".action.PICK";
    protected static final String EXTRA_LABEL = "extra_label";
    protected static final String EXTRA_MINLON = "extra_minx";
    protected static final String EXTRA_MINLAT = "extra_miny";
    protected static final String EXTRA_MAXLON = "extra_maxx";
    protected static final String EXTRA_MAXLAT = "extra_maxy";
    protected static final String EXTRA_TYPE = "extra_type";
    protected static final String EXTRA_MSG = "extra_msg";
    protected static final String EXTRA_FIELD = "extra_field";
    protected static final String EXTRA_COORD = "extra_coord";

    protected SharedPreferences prefs;
    protected String search_query = "";
    protected int mWidth = 1;
    protected int mHeight = 1;
    protected Context ctxt;
    protected RelativeLayout mapLayout;
    private C2CMapView mapView = null;
    private List<String> mSelectedLayers = new ArrayList<String>();
    private boolean onRetainCalled = false;
    private C2CDirectionsWaiter waiter;
    private static final int MENU_PREFS = 3;
    private static final int MENU_RECORD = 4;
    private static final int MENU_DIRECTION = 5;

    protected C2CMapComponent mapComponent = null;
    protected boolean isTrackingPosition = false;
    protected int mProvider;

    abstract protected void setDefaultMap();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctxt = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);

        com.nutiteq.log.Log.setLogger(new AndroidLogger(APP));
        com.nutiteq.log.Log.enableAll();

        onRetainCalled = false;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        mapLayout = ((RelativeLayout) findViewById(R.id.map));
        waiter = new C2CDirectionsWaiter(BaseMap.this);

        // Width and Height
        Display display = getWindowManager().getDefaultDisplay();
        mWidth = display.getWidth();
        mHeight = display.getHeight();

        setDefaultMap();

        // GPS Location tracking
        final C2CGpsProvider locationSource = new C2CGpsProvider(BaseMap.this);
        final ImageButton btn_gps = (ImageButton) findViewById(R.id.position_track);
        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapComponent != null) {
                    if (isTrackingPosition) {
                        Toast.makeText(BaseMap.this, R.string.toast_gps_stop, Toast.LENGTH_SHORT)
                                .show();
                        locationSource.quit();
                    } else {
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
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
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
        } else if (itemid == MENU_RECORD) {
            // Record GPS trace
            final C2CGpsProvider pr = (C2CGpsProvider) mapComponent.getLocationSource();
            if (pr != null && pr.record) {
                pr.setRecord(false);
                item.setTitle(R.string.menu_record_start);
                item.setIcon(android.R.drawable.ic_media_play);
                // Save Traces dialog
                final AlertDialog.Builder dialog = new AlertDialog.Builder(BaseMap.this);
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
            startActivity(new Intent(BaseMap.this, C2CDirections.class));
        }
        return true;
    }

    protected void setMapComponent(final C2CMapComponent bmc, final GeoMap gm) {
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
                    Intent i = new Intent(BaseMap.this, C2CDirections.class);
                    i.putExtra(EXTRA_FIELD, intent.getIntExtra(EXTRA_FIELD, R.id.start));
                    i.putExtra(EXTRA_COORD, p.getLon() + "," + p.getLat());
                    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    i.setAction(ACTION_PICK);
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

    protected void setMapView() {
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

    protected void setOverlay(final C2COverlay overlay) {
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
            final AlertDialog.Builder dialog = new AlertDialog.Builder(BaseMap.this);
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
                Toast.makeText(BaseMap.this, R.string.toast_overlay_added, Toast.LENGTH_SHORT)
                        .show();
                gm.addTileOverlay(overlay);
            } else {
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