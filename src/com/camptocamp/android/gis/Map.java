package com.camptocamp.android.gis;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MapView;
import com.nutiteq.cache.MemoryCache;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.controls.AndroidKeysHandler;
import com.nutiteq.log.AndroidLogger;
import com.nutiteq.log.Log;
import com.nutiteq.maps.GeoMap;
import com.nutiteq.ui.ThreadDrivenPanning;

public class Map extends Activity {

    private MapView mapView;
    private BasicMapComponent mapComponent;
    private boolean onRetainCalled;
    private ZoomControls zoomControls;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onRetainCalled = false;

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        final Object savedMapComponent = getLastNonConfigurationInstance();
        if (savedMapComponent == null) {
            mapComponent = new BasicMapComponent("abcdtrial", "Nutiteq", "Android Mapper", 1, 1,
                    new WgsPoint(0, 0), 0);

            GeoMap map = new Tilecache(getString(R.string.base_url), 256, 0, 11, ".jpeg",
                    "Camptocamp SA");

            mapComponent.setMap(map);

            final MemoryCache memoryCache = new MemoryCache(1024 * 1024);
            mapComponent.setNetworkCache(memoryCache);
            mapComponent.setPanningStrategy(new ThreadDrivenPanning());
            mapComponent.setControlKeysHandler(new AndroidKeysHandler());

            mapComponent.startMapping();
            mapComponent.setTouchClickTolerance(BasicMapComponent.FINGER_CLICK_TOLERANCE);
        } else {
            mapComponent = (BasicMapComponent) savedMapComponent;
        }

        Log.setLogger(new AndroidLogger("NutiteqMapper"));
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