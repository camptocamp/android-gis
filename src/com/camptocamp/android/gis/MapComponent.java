package com.camptocamp.android.gis;

import android.os.Handler;
import android.os.Message;
import android.view.animation.DecelerateInterpolator;

import com.camptocamp.android.gis.layer.LocationMarker;
import com.nutiteq.BasicMapComponent;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.WgsBoundingBox;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.location.LocationSource;
import com.nutiteq.maps.OpenStreetMap;

public class MapComponent extends BasicMapComponent {

    // private static final String TAG = Map.D + "MyMapComponent";
    private static final String KEY = "182be0c5cdcd5072bb1864cdee4d3d6e4c593f89365962.70956542";
    private static final String VDR = "Camptocamp SA";
    private static final int MOVE = 0;
    private static final long DELAY = 24; // ms
    private static final long DOUBLETAP_DELTA = 500; // ms
    private static final int DOUBLETAP_RADIUS = 50; // px
    private static final int DOUBLETAP_PAN = 2; // px
    private static final int ZOOM = 7;
    private static final double C = 40076592d; // m

    private WgsBoundingBox maxExtent = null;
    private final int[] lastpanx = new int[2];
    private final int[] lastpany = new int[2];

    private double ycos = 0;
    private int lastposx = 0;
    private int lastposy = 0;
    private long lasttouch;

    public MapComponent(WgsPoint middlePoint, int width, int height, int zoom) {
        super(KEY, VDR, BaseMap.APP, width, height, middlePoint, (zoom != -1 ? zoom : ZOOM));
        // FIXME: must set according to min/max zoom levels
        // setZoomLevelIndicator(new ZoomIndicator());
        ycos = Math.cos(middlePoint.getLat());
    }

    public MapComponent(WgsBoundingBox bbox, WgsPoint middlePoint, int width, int height, int zoom) {
        super(KEY, VDR, BaseMap.APP, width, height, middlePoint, (zoom != -1 ? zoom : ZOOM));

        // setZoomLevelIndicator(new ZoomIndicator());
        ycos = Math.cos(middlePoint.getLat());
        maxExtent = bbox;
    }

    @Override
    public void panMap(int panX, int panY) {
        // Save last two pan value
        lastpanx[1] = lastpanx[0];
        lastpany[1] = lastpany[0];
        lastpanx[0] = panX;
        lastpany[0] = panY;

        final int zoom = getZoom();
        if (maxExtent != null) {
            // Don't pan outside max extent
            WgsBoundingBox bbox = getBoundingBox();
            MapPos currentmin = displayedMap.wgsToMapPos(bbox.getWgsMin().toInternalWgs(), zoom);
            MapPos currentmax = displayedMap.wgsToMapPos(bbox.getWgsMax().toInternalWgs(), zoom);
            MapPos min = displayedMap.wgsToMapPos(maxExtent.getWgsMin().toInternalWgs(), zoom);
            MapPos max = displayedMap.wgsToMapPos(maxExtent.getWgsMax().toInternalWgs(), zoom);

            if (currentmin.getX() + panX <= min.getX() ^ currentmax.getX() + panX >= max.getX()) {
                panX = 0;
            }
            if (currentmin.getY() + panY >= min.getY() ^ currentmax.getY() + panY <= max.getY()) {
                panY = 0;
            }
        }
        else {
            // Don't pan outside of map size
            MapPos pos = getInternalMiddlePoint();
            int quartx = getWidth() / 4;
            int quarty = getHeight() / 4;
            if ((pos.getX() > displayedMap.getMapWidth(zoom) - quartx && panX > 0)
                    || (pos.getX() < quartx && panX < 0)) {
                panX = 0;
            }
            if ((pos.getY() > displayedMap.getMapHeight(zoom) - quarty && panY > 0)
                    || (pos.getY() < quarty && panY < 0)) {
                panY = 0;
            }
        }

        super.panMap(panX, panY);
        ycos = Math.cos(getMiddlePoint().getLat());
    }

    @Override
    public void zoomIn() {
        super.zoomIn();

        // Set precision radius if gps tracking is active
        if (locationSource != null) {
            ((LocationMarker) locationSource.getLocationMarker()).setRadius();
        }
    }

    @Override
    public void zoomOut() {
        super.zoomOut();

        // Set precision radius if gps tracking is active
        if (locationSource != null) {
            ((LocationMarker) locationSource.getLocationMarker()).setRadius();
        }
    }

    @Override
    public void pointerPressed(final int x, final int y) {
        super.pointerPressed(x, y);
        resetEasing();
    }

    private void resetEasing() {
        mHandler.removeMessages(MOVE);
        posx = 0;
        posy = 0;
        current = 0f;
        lastpanx[0] = 0;
        lastpanx[1] = 0;
        lastpany[0] = 0;
        lastpany[1] = 0;
    }

    public void pointerReleasedManual(final int x, final int y) {
        super.pointerReleased(x, y);
    }

    @Override
    public void pointerReleased(final int x, final int y) {
        super.pointerReleased(x, y);

        int panx = lastpanx[0] + lastpanx[1];
        int pany = lastpany[0] + lastpany[1];

        // Double Tap ZoomIn
        long now = System.currentTimeMillis();
        if (now - lasttouch <= DOUBLETAP_DELTA && Math.abs(lastposx - x) < DOUBLETAP_RADIUS
                && Math.abs(lastposy - y) < DOUBLETAP_RADIUS && Math.abs(panx) < DOUBLETAP_PAN
                && Math.abs(pany) < DOUBLETAP_PAN) {
            panMap(x - (getWidth() / 2), y - (getHeight() / 2));
            zoomIn();
        }
        // Initiate Easing
        else if (panx != 0 || pany != 0) {
            mHandler.sendMessageDelayed(Message.obtain(mHandler, MOVE, panx, pany), DELAY);
        }
        lasttouch = now;
        lastposx = x;
        lastposy = y;
    }

    public LocationSource getLocationSource() {
        return locationSource;
    }

    // Easing Handler and variables
    DecelerateInterpolator di = new DecelerateInterpolator(1.0f);
    int posx = 0;
    int posy = 0;
    float ip = 0f;
    float current = 0f;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (current <= 1.0f) {
                ip = di.getInterpolation(current);
                int x = (int) Math.floor(((ip * msg.arg1) - posx));
                int y = (int) Math.floor(((ip * msg.arg2) - posy));
                panMap(x, y);
                posx += x;
                posy += y;
                current += 0.1;
                mHandler.sendMessageDelayed(Message.obtain(mHandler, MOVE, msg.arg1, msg.arg2),
                        DELAY);
            }
        }
    };

    public double getMetersPerPixel() {
        // For OSM
        if (getMap() instanceof OpenStreetMap) {
            // Dummy calculation: 0.596 * Math.pow(2, (18 - getZoom()));
            // http://wiki.openstreetmap.org/wiki/Height_and_width_of_a_map#Pure_Math_Method
            return -(C * ycos / Math.pow(2, getZoom() + 8));
        }
        return 0;
    }
}
