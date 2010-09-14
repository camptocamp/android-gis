package com.camptocamp.android.gis;

//http://trac.openlayers.org/browser/trunk/openlayers/lib/OpenLayers/Util.js#L1259

import com.nutiteq.maps.UnstreamedMap;

public class SwisstopoMap extends CH1903 implements UnstreamedMap {

    @SuppressWarnings("unused")
    private static final String TAG = Map.D + "SwisstopoMap";
    private static final int MIN_ZOOM = 14;
    private static final int MAX_ZOOM = 22;
    private static final int TILESIZE = 256;
    private static final String EXT = ".jpeg";
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
        super(copyright, TILESIZE, MIN_ZOOM, MAX_ZOOM, initialZoom);
        this.baseUrl = baseUrl;
    }

    public String buildPath(int mapX, int mapY, int zoom) {
        int x = (int) Math.ceil(mapX / TILESIZE);
        int y = (int) Math.ceil((ch_pixel_y - TILESIZE - mapY) / TILESIZE);
        // int r = rand.nextInt(MAX - MIN + 1) + MIN;
        int r = 5;
        return String.format(baseUrl, r, zoom, (int) (x / 1000000), (int) (x / 1000) % 1000,
                (int) (x % 1000), (int) (y / 1000000), (int) (y / 1000) % 1000, (int) (y % 1000),
                EXT);
    }

    @Override
    public int getMapHeight(final int zoom) {
        // Rounded up to TileSize
        return (int) (Math.ceil(getRealMapHeight(zoom) / TILESIZE) * TILESIZE);
    }

    @Override
    public int getMapWidth(final int zoom) {
        return (int) (Math.ceil((MAX_X - MIN_X) / resolutions.get(zoom) / TILESIZE) * TILESIZE);
    }

    public double getRealMapHeight(final int zoom) {
        return (MAX_Y - MIN_Y) / resolutions.get(zoom);
    }

    protected void initResolutions() {
        // Tile resolution for each zoom level
        resolutions.put(14, 650.0);
        resolutions.put(15, 500.0);
        resolutions.put(16, 250.0);
        resolutions.put(17, 100.0);
        resolutions.put(18, 50.0);
        resolutions.put(19, 20.0);
        resolutions.put(20, 10.0);
        resolutions.put(21, 5.0);
        resolutions.put(22, 2.5);
        // resolutions.put(23, 2.0);
        // resolutions.put(24, 1.5);
        // resolutions.put(25, 1.0);
        // resolutions.put(26, 0.5);
    }

    protected double CHxtoPIX(double pt) {
        return (pt - MIN_X) / resolutions.get(zoom);
    }

    protected double CHytoPIX(double pt) {
        return ((MAX_Y - pt) / resolutions.get(zoom)) + getMapHeight(zoom) - getRealMapHeight(zoom);
    }

    protected double PIXtoCHx(double px) {
        return MIN_X + (px * resolutions.get(zoom));
    }

    protected double PIXtoCHy(double px) {
        // FIXME: When is this used ?
        px = px - getMapHeight(zoom) + getRealMapHeight(zoom);
        return MAX_Y - (px * resolutions.get(zoom));
    }

    protected void setValues(final int _zoom, final int tileSize) {
        zoom = _zoom;
        ch_pixel_y = CHytoPIX(MIN_Y);
    }
}
