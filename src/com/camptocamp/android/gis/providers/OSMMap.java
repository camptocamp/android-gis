package com.camptocamp.android.gis.providers;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.camptocamp.android.gis.BaseMap;
import com.camptocamp.android.gis.MapComponent;
import com.camptocamp.android.gis.R;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.location.LocationSource;

public class OSMMap extends BaseMap {

    private static final String TAG = D + "OSMMap";
    private static final double LAT = 46.858423; // X: 190'000
    private static final double LNG = 8.225458; // Y: 660'000
    private static final int MENU_MAP_OSM = 0;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Search Bar
        findViewById(R.id.search_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchRequested();
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

    @Override
    public boolean onSearchRequested() {
        startSearch(mSearchQuery, false, null, false);
        return true;
    }

    protected void selectMap(int provider_id) {
        LocationSource loc = null;
        mProvider = provider_id;
        int zoom = -1;
        WgsPoint pt = new WgsPoint(LNG, LAT);
        // Reset mapComponent
        if (mMapComponent != null) {
            // Save zoom and point
            zoom = mMapComponent.getZoom();
            pt = mMapComponent.getMiddlePoint();
            // Save location provider
            if (mTrackingPosition) {
                loc = mMapComponent.getLocationSource();
            }
            mMapComponent.stopMapping();
            mMapComponent = null;
        }
        // Select map
        final Object savedMapComponent = getLastNonConfigurationInstance();
        if (savedMapComponent == null) {
            switch (provider_id) {
                case MENU_MAP_OSM:
                default:
                    mMapComponent = new MapComponent(pt, mWidth, mHeight, zoom);
                    setMapComponent(new OpenStreetMap());
                    break;
            }
            if (loc != null) {
                mMapComponent.setLocationSource(loc);
            }
        }
        else {
            android.util.Log.v(TAG, "using savedMapComponent");
            mMapComponent = (MapComponent) savedMapComponent;
        }
        setMapView();
    }
}
