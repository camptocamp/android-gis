package com.camptocamp.android.gis;

//http://trac.openlayers.org/browser/trunk/openlayers/lib/OpenLayers/Util.js#L1259

import com.nutiteq.components.MapPos;
import com.nutiteq.components.TileMapBounds;
import com.nutiteq.maps.UnstreamedMap;

public class SwisstopoMap extends CH1903 implements UnstreamedMap {

//    private static final String TAG = Map.D + "SwisstopoMap";
    // private static final int DOTS_PER_INCH = 254;
    // private static final double INCHES_PER_METER = 39.3701;
    // private static final double DOTS_PER_METER = INCHES_PER_METER *
    // DOTS_PER_INCH;
    private static final int MIN_ZOOM = 14;
    private static final int MAX_ZOOM = 24;
    private static final int TILE_SIZE = 256;
    private static final Object FORMAT = ".jpeg";
    private String baseUrl;
    // private final Random rand = new Random();
    // private final int MIN = 5;
    // private final int MAX = 9;

    // Swisstopo data MAX_EXTEND
    protected static double MIN_X = 420000;
    protected static double MAX_X = 900000;
    protected static double MIN_Y = 30000;
    protected static double MAX_Y = 350000;

    public SwisstopoMap(String baseUrl, String copyright, final int initialZoom) {
        super(copyright, TILE_SIZE, MIN_ZOOM, MAX_ZOOM, initialZoom);
        this.baseUrl = baseUrl;
    }

    public String buildPath(int mapX, int mapY, int zoom) {
        int x = (int) Math.ceil(mapX / TILE_SIZE);
        int y = (int) Math.ceil((ch_pixel_y - TILE_SIZE - mapY) / TILE_SIZE);
        // int r = rand.nextInt(MAX - MIN + 1) + MIN;
        // Log.v(TAG, "x=" + x + ", y=" + y);
        int r = 5;
        return String.format(baseUrl, r, zoom, (int) (x / 1000000), (int) (x / 1000) % 1000,
                (int) (x % 1000), (int) (y / 1000000), (int) (y / 1000) % 1000, (int) (y % 1000),
                FORMAT);
    }

    @Override
    public TileMapBounds getTileMapBounds(final int zoom) {
        final MapPos min = new MapPos((int) CHxtoPIX(MIN_X) - 1, (int) CHytoPIX(MAX_Y) - 1, zoom);
        final MapPos max = new MapPos((int) CHxtoPIX(MAX_X) - 1, (int) CHytoPIX(MIN_Y) - 1, zoom);
        return new TileMapBounds(min, max);
    }

    protected void initResolutions() {
        // Tile resolution for each zoom level
        resolutions.put(14, 650.0); // 625D);
        resolutions.put(15, 500.0); // 416.67D);
        resolutions.put(16, 250.0); // 208.33D);
        resolutions.put(17, 100.0);
        resolutions.put(18, 50.0);
        resolutions.put(19, 20.0);
        resolutions.put(20, 10.0);
        resolutions.put(21, 5.0);
        resolutions.put(22, 2.5);
        resolutions.put(23, 2.0);
        resolutions.put(24, 1.5);
        resolutions.put(25, 1.0);
        resolutions.put(26, 0.5);
    }

    protected double CHxtoPIX(double pt) {
        final double px = (pt - MIN_X) / resolutions.get(zoom);
        // Log.v(TAG, pt + " (CHx) -> " + px + " (PX)");
        return px;
    }

    protected double CHytoPIX(double pt) {
        final double px = (MAX_Y - pt) / resolutions.get(zoom);
        // Log.v(TAG, pt + " (CHy) -> " + px + " (PX)");
        return px;
    }

    protected double PIXtoCHx(double px) {
        final double pt = MIN_X + (px * resolutions.get(zoom));
        // Log.v(TAG, px + " (PX) -> " + pt + " (CHx)");
        return pt;
    }

    protected double PIXtoCHy(double px) {
        final double pt = MAX_Y - (px * resolutions.get(zoom));
        // Log.v(TAG, px + " (PX) -> " + pt + " (CHy)");
        return pt;
    }

    protected void setValues(final int zoom, final int tileSize) {
        this.zoom = zoom;
        ch_pixel_y = CHytoPIX(MIN_Y);
    }
}
