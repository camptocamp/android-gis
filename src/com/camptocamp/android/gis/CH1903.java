package com.camptocamp.android.gis;

import java.util.HashMap;

import android.util.Log;

import com.nutiteq.components.MapPos;
import com.nutiteq.components.Point;
import com.nutiteq.components.TileMapBounds;
import com.nutiteq.maps.BaseMap;
import com.nutiteq.maps.projections.Projection;
import com.nutiteq.ui.Copyright;

/**
 * Abstract class for doing WGS84 coordinates calculations to map pixels in
 * CH-1903 (EPSG:4149, SwissGrid) and back.
 * 
 */
// http://www.swisstopo.admin.ch/internet/swisstopo/en/home/products/software/products/skripts.html
// http://www.swisstopo.admin.ch/internet/swisstopo/en/home/topics/survey/sys/refsys/swiss_grid.html
// http://spatialreference.org/ref/epsg/4149/

public abstract class CH1903 extends BaseMap implements Projection {

    private static final String TAG = Map.D + "CH1903";
    private static final double CH_MIN_X = 420000; // 485869.5728;
    private static final double CH_MAX_X = 900000; // 837076.5648;
    private static final double CH_MIN_Y = 350000; // 299941.7864;
    private static final double CH_MAX_Y = 30000; // 76443.1884;
    private final HashMap<Integer, Double> resolutions = new HashMap<Integer, Double>();
    private int zoom;
    protected double ch_pixel_w;
    protected double ch_pixel_h;

    public CH1903(final Copyright copyright, final int tileSize, final int minZoom,
            final int maxZoom, final int initialZoom) {
        super(copyright, tileSize, minZoom, maxZoom);
        initResolutions();
        setValues(initialZoom, tileSize);
    }

    public CH1903(final String copyright, final int tileSize, final int minZoom, final int maxZoom,
            final int initialZoom) {
        super(copyright, tileSize, minZoom, maxZoom);
        initResolutions();
        setValues(initialZoom, tileSize);
    }

    private void initResolutions() {
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

    public Point mapPosToWgs(MapPos pos) {
        // Convert from CH1903 to pixel
        double x_aux = PIXtoCHx((double) pos.getX());
        double y_aux = PIXtoCHy((double) pos.getY());

        // Converts militar to civil and to unit = 1000km
        // Axiliary values (% Bern)
        x_aux = (x_aux - 600000) / 1000000;
        y_aux = (y_aux - 200000) / 1000000;

        // Process lat/long
        double _lat = 16.9023892 + 3.238272 * y_aux - 0.270978 * Math.pow(x_aux, 2) - 0.002528
                * Math.pow(y_aux, 2) - 0.0447 * Math.pow(x_aux, 2) * y_aux - 0.0140
                * Math.pow(y_aux, 3);
        double _long = 2.6779094 + 4.728982 * x_aux + 0.791484 * x_aux * y_aux + 0.1306 * x_aux
                * Math.pow(y_aux, 2) - 0.0436 * Math.pow(x_aux, 3);

        // Unit 10000'' to 1'' and converts seconds to degrees (dec)
        _lat = _lat * 100 / 36 * 1000000D;
        _long = _long * 100 / 36 * 1000000D;

        // Round up
        _lat = Math.ceil(_lat);
        _long = Math.ceil(_long);

        Log.i(TAG + ":mapPosToWgs", "lat=" + (int) _lat + ", long=" + (int) _long);
        return new Point((int) _long, (int) _lat);
    }

    public MapPos wgsToMapPos(Point wgs, int zoom) {
        // Converts degrees dec to sex
        double _lat = DECtoSEX(wgs.getY() / 1000000D);
        double _long = DECtoSEX(wgs.getX() / 1000000D);

        // Converts degrees to seconds (sex)
        _lat = DEGtoSEC(_lat);
        _long = DEGtoSEC(_long);

        // Axiliary values (% Bern)
        double lat_aux = (_lat - 169028.66) / 10000;
        double lng_aux = (_long - 26782.5) / 10000;

        // Process X/Y
        double x = 600072.37 + 211455.93 * lng_aux - 10938.51 * lng_aux * lat_aux - 0.36 * lng_aux
                * Math.pow(lat_aux, 2) - 44.54 * Math.pow(lng_aux, 3);
        double y = 200147.07 + 308807.95 * lat_aux + 3745.25 * Math.pow(lng_aux, 2) + 76.63
                * Math.pow(lat_aux, 2) - 194.56 * Math.pow(lng_aux, 2) * lat_aux + 119.79
                * Math.pow(lat_aux, 3);

        // Convert from CH1903 to pixel
        x = CHxtoPIX(x);
        y = CHytoPIX(y);

        // Round up
        x = Math.ceil(x);
        y = Math.ceil(y);

        Log.i(TAG + ":wgsToMapPos", "x=" + (int) x + ", y=" + (int) y);
        return new MapPos((int) x, (int) y, zoom);
    }

    // Convert DEC angle to SEX DMS
    private double DECtoSEX(double angle) {
        // Extract DMS
        double deg = Math.floor(angle);
        double min = Math.floor((angle - deg) * 60);
        double sec = (((angle - deg) * 60) - min) * 60;

        // Result in degrees sex (dd.mmss)
        return deg + (double) min / 100 + (double) sec / 10000;
    }

    // Convert Degrees angle to seconds
    private double DEGtoSEC(double angle) {
        // Extract DMS
        double deg = Math.floor(angle);
        double min = Math.floor((angle - deg) * 100);
        double sec = (((angle - deg) * 100) - min) * 100;

        // Result in degrees sex (dd.mmss)
        return sec + (double) min * 60 + (double) deg * 3600;
    }

    private double CHxtoPIX(double pt) {
        final double px = pt / resolutions.get(zoom);
        Log.v(TAG, pt + " (CHx) -> " + px + " (PX)");
        return px;
    }

    private double CHytoPIX(double pt) {
        final double px = -(pt / resolutions.get(zoom));
        Log.v(TAG, pt + " (CHy) -> " + px + " (PX)");
        return px;
    }

    private double PIXtoCHx(double px) {
        final double pt = px * resolutions.get(zoom);
        Log.v(TAG, px + " (PX) -> " + pt + " (CHx)");
        return pt;
    }

    private double PIXtoCHy(double px) {
        final double pt = -px * resolutions.get(zoom);
        Log.v(TAG, px + " (PX) -> " + pt + " (CHy)");
        return pt;
    }

    private void setValues(final int zoom, final int tileSize) {
        this.zoom = zoom;
        // Number of tiles to minimum
        ch_pixel_w = CHxtoPIX(CH_MIN_X);
        ch_pixel_h = -CHytoPIX(CH_MAX_Y);
        Log.v(TAG, "ch_pixel_w=" + ch_pixel_w + ", ch_pixel_h=" + ch_pixel_h);
    }

    @Override
    public MapPos zoom(MapPos middlePoint, int zoomSteps) {
        MapPos pos = super.zoom(middlePoint, zoomSteps);
        setValues(pos.getZoom(), getTileSize());
        return pos;
    }

    @Override
    public TileMapBounds getTileMapBounds(final int zoom) {
        final MapPos min = new MapPos((int) CHxtoPIX(CH_MIN_X), (int) CHytoPIX(CH_MIN_Y), zoom);
        final MapPos max = new MapPos((int) CHxtoPIX(CH_MAX_X), (int) CHytoPIX(CH_MAX_Y), zoom);
        return new TileMapBounds(min, max);
    }
}
