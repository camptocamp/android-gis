package com.camptocamp.android.gis;

//http://trac.openlayers.org/browser/trunk/openlayers/lib/OpenLayers/Util.js#L1259

import java.util.HashMap;

import com.nutiteq.components.MapPos;
import com.nutiteq.maps.UnstreamedMap;

public class SwisstopoMap extends CH1903 implements UnstreamedMap {

    // private static final String TAG = Map.D + "SwisstopoMap";
    private static final int MIN_ZOOM = 14;
    private static final int MAX_ZOOM = 26;
    private static final int TILESIZE = 256;
    private static final String EXT = ".jpeg";
    private double y_shift;
    private String baseUrl;
    public int zoom;
    // private final Random rand = new Random();
    // private final int MIN = 5;
    // private final int MAX = 9;

    // Swisstopo data MAX_EXTEND
    protected static double MIN_X = 420000;
    protected static double MAX_X = 900000;
    protected static double MIN_Y = 30000;
    protected static double MAX_Y = 350000;

    // Swisstopo resolutions
    public static final HashMap<Integer, Double> resolutions = new HashMap<Integer, Double>() {
        private static final long serialVersionUID = -1961062331663888031L;
        {
            put(14, 650.0);
            put(15, 500.0);
            put(16, 250.0);
            put(17, 100.0);
            put(18, 50.0);
            put(19, 20.0);
            put(20, 10.0);
            put(21, 5.0);
            put(22, 2.5);
            put(23, 2.0);
            put(24, 1.5);
            put(25, 1.0);
            put(26, 0.5);
        }
    };

    public SwisstopoMap(String baseUrl, String copyright, final int initialZoom) {
        super(copyright, TILESIZE, MIN_ZOOM, MAX_ZOOM, initialZoom);
        this.baseUrl = baseUrl;
        this.zoom = initialZoom;
        setValues();
    }

    public String buildPath(int mapX, int mapY, int zoom) {
        int x = (int) Math.ceil(mapX / TILESIZE);
        int y = (int) Math.ceil((getMapHeight(zoom) - TILESIZE - mapY) / TILESIZE);
        // Log.v(TAG, "x=" + x + ", y=" + y);
        // int r = rand.nextInt(MAX - MIN + 1) + MIN;
        int r = 5;
        return String.format(baseUrl, r, zoom, (int) (x / 1000000), (int) (x / 1000) % 1000,
                (int) (x % 1000), (int) (y / 1000000), (int) (y / 1000) % 1000, (int) (y % 1000),
                EXT);
    }

    public MapPos zoom(final MapPos middlePoint, final int zoomSteps) {
        double xx = PIXtoCHx(middlePoint.getX());
        double yy = PIXtoCHy(middlePoint.getY());
        zoom += zoomSteps;
        setValues();
        return new MapPos((int) Math.round(CHxtoPIX(xx)), (int) Math.round(CHytoPIX(yy)), zoom);
    }

    @Override
    public int getMapWidth(final int zoom) {
        return (int) (Math.ceil((MAX_X - MIN_X) / resolutions.get(zoom) / TILESIZE) * TILESIZE);
    }

    @Override
    public int getMapHeight(final int zoom) {
        // Rounded up to TileSize
        return (int) (Math.ceil(getRealMapHeight(zoom) / TILESIZE) * TILESIZE);
    }

    public double getRealMapHeight(final int zoom) {
        return (MAX_Y - MIN_Y) / resolutions.get(zoom);
    }

    protected double CHxtoPIX(double pt) {
        return (pt - MIN_X) / resolutions.get(zoom);
    }

    protected double CHytoPIX(double pt) {
        return ((MAX_Y - pt) / resolutions.get(zoom)) + y_shift;
    }

    protected double PIXtoCHx(double px) {
        return MIN_X + (px * resolutions.get(zoom));
    }

    protected double PIXtoCHy(double px) {
        return MAX_Y - ((px - y_shift) * resolutions.get(zoom));
    }

    protected void setValues() {
        y_shift = getMapHeight(zoom) - getRealMapHeight(zoom);
        // Log.v(TAG, "ch_pixel_y=" + ch_pixel_y);
        // Log.v(TAG, "y_shift=" + y_shift);
    }

}
