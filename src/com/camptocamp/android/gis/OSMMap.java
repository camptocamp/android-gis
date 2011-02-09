package com.camptocamp.android.gis;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.camptocamp.android.gis.providers.OpenStreetMap;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.location.LocationSource;

public class OSMMap extends BaseMap {

    private static final String TAG = D + "OSMMap";
    private static final String OSM_MAPNIK_URL = "http://tile.openstreetmap.org/";
    private static final double LAT = 46.858423; // X: 190'000
    private static final double LNG = 8.225458; // Y: 660'000
    private static final int MENU_MAP_OSM = 0;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // Zoom
//        final ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoom);
//        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
//            public void onClick(final View v) {
//                mapComponent.zoomIn();
//            }
//        });
//        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
//            public void onClick(final View v) {
//                mapComponent.zoomOut();
//            }
//        });

//        // Set overlay
//        final ImageButton btn_overlay = (ImageButton) findViewById(R.id.switch_overlay_test);
//        btn_overlay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setOverlay(new OsmOverlay(getString(R.string.osm_overlay)));
//            }
//        });

        // Search Bar
        findViewById(R.id.search_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch(search_query, false, null, false);
            }
        });
    }

    @Override
    protected void setDefaultMap() {
        // selectMap(Integer.parseInt(prefs.getString(Prefs.KEY_PROVIDER,
        // Prefs.DEFAULT_PROVIDER)));
        selectMap(MENU_MAP_OSM);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_MAP_OSM, MENU_MAP_OSM, R.string.menu_osm).setIcon(
                android.R.drawable.ic_menu_mapmode);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        final int itemid = item.getItemId();
        if (itemid <= MENU_MAP_OSM) {
            selectMap(itemid);
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    protected void selectMap(int provider_id) {
        LocationSource loc = null;
        mProvider = provider_id;
        int zoom = -1;
        WgsPoint pt = new WgsPoint(LNG, LAT);
        // Reset mapComponent
        if (mapComponent != null) {
            // Save zoom and point
            zoom = mapComponent.getZoom();
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
            case MENU_MAP_OSM:
            default:
                setMapComponent(new C2CMapComponent(pt, mWidth, mHeight, zoom), new OpenStreetMap(
                        OSM_MAPNIK_URL, OpenStreetMap.TILE_SIZE, OpenStreetMap.MIN_ZOOM, 18));
                break;
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
}
