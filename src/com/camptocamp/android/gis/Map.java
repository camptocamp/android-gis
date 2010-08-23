package com.camptocamp.android.gis;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MapView;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.controls.AndroidKeysHandler;
import com.nutiteq.log.AndroidLogger;
import com.nutiteq.log.Log;
import com.nutiteq.ui.ThreadDrivenPanning;

public class Map extends Activity {

    private boolean onRetainCalled;
    private MapView mapView;
    private SwissMapComponent mapComponent;
    private ZoomControls zoomControls;

    public static final String D = "C2C:";
    // private final static String TAG = "Map";
    private final String KEY = "182be0c5cdcd5072bb1864cdee4d3d6e4c593f89365962.70956542";
    private final String VENDOR = "Camptocamp SA";
    private final String APP = "c2c-android-gis";

    private final double lat = 46.517815; // X: 152'210
    private final double lng = 6.562805; // Y: 532'790

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onRetainCalled = false;

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        final Object savedMapComponent = getLastNonConfigurationInstance();
        if (savedMapComponent == null) {

            mapComponent = new SwissMapComponent(KEY, VENDOR, APP, 1, 1, new WgsPoint(lng, lat), 14);
            mapComponent.setMap(new Tilecache(getString(R.string.base_url), ".jpeg", 256, 14, 26,
                    VENDOR, 14));

            // mapComponent.setMap(OpenStreetMap.MAPNIK);
            // mapComponent.setMap(new
            // MyOpenstreetmap("http://tile.openstreetmap.org/", 256, 0, 17));

            // final MemoryCache memoryCache = new MemoryCache(1024 * 1024);
            // mapComponent.setNetworkCache(memoryCache);
            mapComponent.setPanningStrategy(new ThreadDrivenPanning());
            // mapComponent.setPanningStrategy(new EventDrivenPanning());
            mapComponent.setControlKeysHandler(new AndroidKeysHandler());
            mapComponent.startMapping();
            mapComponent.setTouchClickTolerance(BasicMapComponent.FINGER_CLICK_TOLERANCE);
        } else {
            mapComponent = (SwissMapComponent) savedMapComponent;
        }

        Log.setLogger(new AndroidLogger(APP));
        Log.enableAll();

        // Zoom
        mapView = new MapView(this, mapComponent);
        zoomControls = new ZoomControls(this);
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

        final RelativeLayout relativeLayout = new RelativeLayout(this);
        setContentView(relativeLayout);
        final RelativeLayout.LayoutParams mapViewLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        relativeLayout.addView(mapView, mapViewLayoutParams);
        final RelativeLayout.LayoutParams zoomControlsLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        zoomControlsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        zoomControlsLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        relativeLayout.addView(zoomControls, zoomControlsLayoutParams);

        mapView.setClickable(true);
        mapView.setEnabled(true);
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