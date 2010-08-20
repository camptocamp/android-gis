package com.camptocamp.android.gis;

import android.util.Log;

public class Swisstopo extends Tilecache {

    private final String TAG = Map.D + "Swisstopo";
    // Swiss grid min point (SW)
    // private final Point min = new Point(5970000, 45830000); // 5.9700,
    // 45.8300;
    // private final Point min = new WgsPoint(5.9700, 45.8300).toInternalWgs();
    private int shiftx = 0;
    private int shifty = 0;

    public Swisstopo(String baseUrl, String format, int tileSize, int minZoom, int maxZoom,
            String copyright, int initialZoom) {
        super(baseUrl, format, tileSize, minZoom, maxZoom, copyright, initialZoom);
        // getShift(initialZoom);
    }

    // @Override
    // public MapPos zoom(MapPos middlePoint, int zoomSteps) {
    // Log.v(TAG, "width=" + getMapWidth(middlePoint.getZoom()));
    // getShift(middlePoint.getZoom());
    // return super.zoom(middlePoint, zoomSteps);
    // }

    @Override
    public String buildPath(int mapX, int mapY, int zoom) {
        Log.v(TAG, "x=" + mapX + ", y=" + mapY + ", z=" + zoom);
        mapX = mapX - shiftx;
        mapY = mapY - shifty;
        return super.buildPath(mapX, mapY, zoom);
    }

    // private void getShift(int zoom) {
    // MapPos pt = wgsToMapPos(min, zoom);
    // shiftx = getMapWidth(zoom) - pt.getX();
    // shifty = getMapHeight(zoom) - pt.getY();
    // Log.v(TAG, "shiftx=" + shiftx);
    // Log.v(TAG, "shiftx=" + shifty);
    // }
}
