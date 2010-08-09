package com.camptocamp.android.gis;

import android.util.Log;

import com.nutiteq.components.MapPos;
import com.nutiteq.components.Point;
import com.nutiteq.components.TileMapBounds;
import com.nutiteq.maps.BaseMap;
import com.nutiteq.maps.projections.Projection;
import com.nutiteq.ui.Copyright;

/**
 * Abstract class for doing WGS84 coordinates calculations to map pixels in
 * CH-1903 (Swiss Grid) and back.
 * 
 */
// http://www.swisstopo.admin.ch/internet/swisstopo/en/home/products/software/products/skripts.html
// http://www.swisstopo.admin.ch/internet/swisstopo/en/home/topics/survey/sys/refsys/swiss_grid.html

public abstract class CH1903 extends BaseMap implements Projection {

    private final String TAG = "CH1903";
    private final double MAX_X = 300101;
    private final double MAX_Y = 828614;
    private TileMapBounds bounds = null;

    public CH1903(final Copyright copyright, final int tileSize, final int minZoom,
            final int maxZoom, final int initialZoom) {
        super(copyright, tileSize, minZoom, maxZoom);
        bounds = getTileMapBounds(initialZoom);
    }

    public CH1903(final String copyright, final int tileSize, final int minZoom, final int maxZoom,
            final int initialZoom) {
        super(copyright, tileSize, minZoom, maxZoom);
        bounds = getTileMapBounds(initialZoom);
    }

    public Point mapPosToWgs(MapPos pos) {
        // Convert from CH1903 to pixel
        int y_aux = (int) CHytoPIX((double) pos.getY());
        int x_aux = (int) CHxtoPIX((double) pos.getX());

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
        _lat = (int) Math.round(_lat / 36 * 100);
        _long = (int) Math.round(_long / 36 * 100);

        Log.i("mapPosToWgs", "lat=" + _lat + ", long=" + _long);
        return new Point((int) _lat, (int) _long);
    }

    public MapPos wgsToMapPos(Point wgs, int zoom) {
        // Converts degrees dec to sex
        double _lat = DECtoSEX(wgs.getY() / 1000000);
        double _long = DECtoSEX(wgs.getX() / 1000000);

        // Converts degrees to seconds (sex)
        _lat = DEGtoSEC(_lat);
        _long = DEGtoSEC(_long);

        // Axiliary values (% Bern)
        double lat_aux = (_lat - 169028.66) / 10000;
        double long_aux = (_long - 26782.5) / 10000;

        // Process X/Y
        double x = 200147.07 + 308807.95 * lat_aux + 3745.25 * Math.pow(long_aux, 2) + 76.63
                * Math.pow(lat_aux, 2) - 194.56 * Math.pow(long_aux, 2) * lat_aux + 119.79
                * Math.pow(lat_aux, 3);
        double y = 600072.37 + 211455.93 * long_aux - 10938.51 * long_aux * lat_aux - 0.36
                * long_aux * Math.pow(lat_aux, 2) - 44.54 * Math.pow(long_aux, 3);

        // Maximum CH1903 values
        // if (x < 0) {
        // x = 0;
        // } else if (x > MAX_X) {
        // x = MAX_X;
        // }
        // if (y < 0) {
        // y = 0;
        // } else if (x > MAX_Y) {
        // y = MAX_Y;
        // }

        // Convert from CH1903 to pixel
        x = CHxtoPIX(x);
        y = CHytoPIX(y);

        Log.i("wgsToMapPos", "x=" + (int) x + ", y=" + (int) y);
        return new MapPos((int) x, (int) y, zoom);
    }

    private double CHxtoPIX(double pt) {
        Log.v(TAG, pt + " (CHx) -> " + (pt * MAX_X / bounds.getMaxPoint().getX()) + " (PX)");
        return pt * MAX_X / bounds.getMaxPoint().getX();
    }

    private double CHytoPIX(double pt) {
        Log.v(TAG, pt + " (CHy) -> " + (pt * MAX_Y / bounds.getMaxPoint().getY()) + " (PX)");
        return pt * MAX_Y / bounds.getMaxPoint().getY();
    }

    // Convert DEC angle to SEX DMS
    private double DECtoSEX(double angle) {
        // Extract DMS
        int deg = (int) Math.floor(angle);
        int min = (int) Math.floor((angle - deg) * 60);
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

    @Override
    public MapPos zoom(MapPos middlePoint, int zoomSteps) {
        bounds = getTileMapBounds(middlePoint.getZoom());
        return super.zoom(middlePoint, zoomSteps);
    }

    // Convert SEX DMS angle to DEC
    // private double SEXtoDEC(double angle) {
    // // Extract DMS
    // int deg = (int) angle;
    // int min = (int) ((angle - deg) * 100);
    // double sec = (((angle - deg) * 100) - min) * 100;
    //
    // // Result in degrees sex (dd.mmss)
    // return deg + (sec / 60 + min) / 60;
    // }
}
