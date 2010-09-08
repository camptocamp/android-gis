package com.camptocamp.android.gis;

import android.util.Log;

import com.nutiteq.components.MapPos;
import com.nutiteq.components.TileMapBounds;
import com.nutiteq.maps.UnstreamedMap;

public class SwisstopoMap extends CH1903 implements UnstreamedMap {

    private static final String TAG = Map.D + "SwisstopoMap";
    private String baseUrl;
    private String format;
    private int tileSize;
    // private final Random rand = new Random();
    // private final int MIN = 5;
    // private final int MAX = 9;

    // Swisstopo data MAX_EXTEND
    protected static double CH_MIN_X = 420000;
    protected static double CH_MAX_X = 900000;
    protected static double CH_MIN_Y = 30000;
    protected static double CH_MAX_Y = 350000;

    public SwisstopoMap(String baseUrl, String format, int tileSize, int minZoom, int maxZoom,
            String copyright, final int initialZoom) {
        super(copyright, tileSize, minZoom, maxZoom, initialZoom);
        this.baseUrl = baseUrl;
        this.format = format;
        this.tileSize = tileSize;
    }

    public String buildPath(int mapX, int mapY, int zoom) {
        int x = (int) Math.ceil(mapX / tileSize);
        int y = (int) Math.ceil((ch_pixel_y - tileSize - mapY) / tileSize);
        // int r = rand.nextInt(MAX - MIN + 1) + MIN;
        int r = 5;
        return String.format(baseUrl, r, zoom, (int) (x / 1000000), (int) (x / 1000) % 1000,
                (int) (x % 1000), (int) (y / 1000000), (int) (y / 1000) % 1000, (int) (y % 1000),
                format);
    }

    protected void initResolutions() {
        // Tile resolution for each zoom level
        resolutions.put(14, 650D);
        resolutions.put(15, 500D);
        resolutions.put(16, 250D);
        resolutions.put(17, 100D);
        resolutions.put(18, 50D);
        resolutions.put(19, 20D);
        resolutions.put(20, 10D);
        resolutions.put(21, 5D);
        resolutions.put(22, 2.5D);
        resolutions.put(23, 2D);
        resolutions.put(24, 0.5D);
    }

    protected double CHxtoPIX(double pt) {
        final double px = (pt - CH_MIN_X) / resolutions.get(zoom);
        Log.v(TAG, pt + " (CHx) -> " + px + " (PX)");
        return px;
    }

    protected double CHytoPIX(double pt) {
        final double px = (CH_MAX_Y - pt) / resolutions.get(zoom);
        Log.v(TAG, pt + " (CHy) -> " + px + " (PX)");
        return px;
    }

    protected double PIXtoCHx(double px) {
        final double pt = CH_MIN_X + (px * resolutions.get(zoom));
        Log.v(TAG, px + " (PX) -> " + pt + " (CHx)");
        return pt;
    }

    protected double PIXtoCHy(double px) {
        final double pt = CH_MAX_Y - (px * resolutions.get(zoom));
        Log.v(TAG, px + " (PX) -> " + pt + " (CHy)");
        return pt;
    }

    protected void setValues(final int zoom, final int tileSize) {
        this.zoom = zoom;
        ch_pixel_y = CHytoPIX(CH_MIN_Y);
    }

    @Override
    public TileMapBounds getTileMapBounds(final int zoom) {
        final MapPos min = new MapPos((int) CHxtoPIX(CH_MIN_X), (int) CHytoPIX(CH_MAX_Y), zoom);
        final MapPos max = new MapPos((int) CHxtoPIX(CH_MAX_X), (int) CHytoPIX(CH_MIN_Y), zoom);
        return new TileMapBounds(min, max);
    }
}
