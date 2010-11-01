package com.camptocamp.android.gis;

import com.nutiteq.components.MapPos;
import com.nutiteq.components.Point;
import com.nutiteq.components.WgsPoint;
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

    // private static final String TAG = Map.D + "CH1903";
    public static double MIN_X = 485869.5728;
    public static double MAX_X = 837076.5648;
    public static double MIN_Y = 76443.1884;
    public static double MAX_Y = 299941.7864;

    public CH1903(final Copyright copyright, final int tileSize, final int minZoom,
            final int maxZoom, final int initialZoom) {
        super(copyright, tileSize, minZoom, maxZoom);
    }

    public CH1903(final String copyright, final int tileSize, final int minZoom, final int maxZoom,
            final int initialZoom) {
        super(copyright, tileSize, minZoom, maxZoom);
    }

    /**
     * Pixel to CH (in meters) and vice versa. Need to implement meter to pixel
     * calculation according to tiles resolution by zoom level.
     */
    public abstract double CHxtoPIX(double pt);

    public abstract double CHytoPIX(double pt);

    public abstract double PIXtoCHx(double px);

    public abstract double PIXtoCHy(double px);

    public Point mapPosToWgs(MapPos pos) {
        // Convert from CH1903 to pixel
        // (X and Y are inverted in the CH1903 notation)
        double y_aux = PIXtoCHx((double) pos.getX());
        double x_aux = PIXtoCHy((double) pos.getY());

        // Converts militar to civil and to unit = 1000km
        // Axiliary values (% Bern)
        y_aux = (y_aux - 600000) / 1000000;
        x_aux = (x_aux - 200000) / 1000000;

        // Process lat/long
        double _lat = 16.9023892 + 3.238272 * x_aux - 0.270978 * Math.pow(y_aux, 2) - 0.002528
                * Math.pow(x_aux, 2) - 0.0447 * Math.pow(y_aux, 2) * x_aux - 0.0140
                * Math.pow(x_aux, 3);
        double _long = 2.6779094 + 4.728982 * y_aux + 0.791484 * y_aux * x_aux + 0.1306 * y_aux
                * Math.pow(x_aux, 2) - 0.0436 * Math.pow(y_aux, 3);

        // Unit 10000'' to 1'' and converts seconds to degrees (dec)
        _lat = _lat * 100 / 36;
        _long = _long * 100 / 36;

        // Log.i(TAG + ":mapPosToWgs", "lat=" + _lat + ", long=" + _long);
        return new WgsPoint(_long, _lat).toInternalWgs();
    }

    public MapPos wgsToMapPos(Point pt, int zoom) {
        // Converts degrees dec to sex
        WgsPoint wgs = pt.toWgsPoint();
        double _lat = DECtoSEX(wgs.getLat());
        double _long = DECtoSEX(wgs.getLon());

        // Converts degrees to seconds (sex)
        _lat = SEXtoSEC(_lat);
        _long = SEXtoSEC(_long);

        // Axiliary values (% Bern)
        double lat_aux = (_lat - 169028.66) / 10000;
        double lng_aux = (_long - 26782.5) / 10000;

        // Process X/Y
        double y = 600072.37 + 211455.93 * lng_aux - 10938.51 * lng_aux * lat_aux - 0.36 * lng_aux
                * Math.pow(lat_aux, 2) - 44.54 * Math.pow(lng_aux, 3);
        double x = 200147.07 + 308807.95 * lat_aux + 3745.25 * Math.pow(lng_aux, 2) + 76.63
                * Math.pow(lat_aux, 2) - 194.56 * Math.pow(lng_aux, 2) * lat_aux + 119.79
                * Math.pow(lat_aux, 3);

        // Convert from CH1903 to pixel
        // (X and Y are inverted in CH1903 notation)
        int X = (int) Math.ceil(CHxtoPIX(y));
        int Y = (int) Math.ceil(CHytoPIX(x));

        // Log.i(TAG + ":wgsToMapPos", "x=" + X + ", y=" + Y);
        return new MapPos(X, Y, zoom);
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

    // Convert Degrees to seconds
    private double SEXtoSEC(double angle) {
        // Extract DMS
        double deg = Math.floor(angle);
        double min = Math.floor((angle - deg) * 100);
        double sec = (((angle - deg) * 100) - min) * 100;

        // Result in degrees sex (dd.mmss)
        return sec + (double) min * 60 + (double) deg * 3600;
    }
}
