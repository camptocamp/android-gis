package com.camptocamp.android.gis;

import android.util.Log;

import com.nutiteq.components.MapPos;
import com.nutiteq.components.Point;
import com.nutiteq.maps.BaseMap;
import com.nutiteq.maps.projections.Projection;
import com.nutiteq.ui.Copyright;

/**
 * Abstract class for doing WGS84 coordinates calculations to map pixels in
 * CH-1903 (Swiss Grid) and back.
 * http://www.swisstopo.admin.ch/internet/swisstopo
 * /en/home/topics/survey/sys/refsys/swiss_grid.html
 */
public abstract class CH1903 extends BaseMap implements Projection {

    public CH1903(final Copyright copyright, final int tileSize, final int minZoom, final int maxZoom) {
        super(copyright, tileSize, minZoom, maxZoom);
    }

    public CH1903(final String copyright, final int tileSize, final int minZoom, final int maxZoom) {
        super(copyright, tileSize, minZoom, maxZoom);
    }

    public Point mapPosToWgs(MapPos pos) {
        // Converts militar to civil and to unit = 1000km
        // Axiliary values (% Bern)
        int y_aux = (pos.getY() - 600000) / 1000000;
        int x_aux = (pos.getX() - 200000) / 1000000;

        // Process lat/long
        int _lat = (int) (16.9023892 + 3.238272 * x_aux - 0.270978 * Math.pow(y_aux, 2) - 0.002528 * Math.pow(x_aux, 2)
                - 0.0447 * Math.pow(y_aux, 2) * x_aux - 0.0140 * Math.pow(x_aux, 3));
        int _long = (int) (2.6779094 + 4.728982 * y_aux + 0.791484 * y_aux * x_aux + 0.1306 * y_aux
                * Math.pow(x_aux, 2) - 0.0436 * Math.pow(y_aux, 3));

        // Unit 10000'' to 1'' and converts seconds to degrees (dec)
        _lat = _lat * 100 / 36;
        _long = _long * 100 / 36;

        Log.i("mapPosToWgs", "x=" + pos.getX() + ", y=" + pos.getY());
        Log.i("mapPosToWgs", "lat=" + _lat + ", long=" + _long);
        return new Point(_lat, _long);
    }

    public MapPos wgsToMapPos(Point wgs, int zoom) {
        // Converts degrees dec to sex
        double _lat = DECtoSEX(wgs.getX());
        double _long = DECtoSEX(wgs.getY());

        // Converts degrees to seconds (sex)
        _lat = DEGtoSEC(_lat);
        _long = DEGtoSEC(_long);

        // Axiliary values (% Bern)
        double lat_aux = (_lat - 169028.66) / 10000;
        double long_aux = (_long - 26782.5) / 10000;

        // Process X/Y
        double x = 200147.07 + 308807.95 * lat_aux + 3745.25 * Math.pow(long_aux, 2) + 76.63 * Math.pow(lat_aux, 2)
                - 194.56 * Math.pow(long_aux, 2) * lat_aux + 119.79 * Math.pow(lat_aux, 3);
        double y = 600072.37 + 211455.93 * long_aux - 10938.51 * long_aux * lat_aux - 0.36 * long_aux
                * Math.pow(lat_aux, 2) - 44.54 * Math.pow(long_aux, 3);

        Log.i("wgsToMapPos", "lat=" + wgs.getX() + ", long=" + wgs.getY());
        Log.i("wgsToMapPos", "x=" + x + ", y=" + y);
        return new MapPos((int) x, (int) y, zoom);
    }

    // Convert DEC angle to SEX DMS
    private double DECtoSEX(double angle) {
        // Extract DMS
        int deg = (int) angle;
        int min = (int) ((angle - deg) * 60);
        double sec = (((angle - deg) * 60) - min) * 60;

        // Result in degrees sex (dd.mmss)
        return deg + min / 100 + sec / 10000;
    }

    // Convert Degrees angle to seconds
    private double DEGtoSEC(double angle) {
        // Extract DMS
        int deg = (int) angle;
        int min = (int) ((angle - deg) * 100);
        double sec = (((angle - deg) * 100) - min) * 100;

        // Result in degrees sex (dd.mmss)
        return sec + min * 60 + deg * 3600;
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
