package com.camptocamp.android.gis;

import android.util.Log;

import com.nutiteq.components.MapPos;
import com.nutiteq.components.Point;
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

    private static final long FALSE_NORTHING = 1000L;
    private static final long FALSE_EASTING = 1000L;
    private final String TAG = Map.D + "CH1903";
    private final double CH_MIN_X = 76443.1884;
    private final double CH_MAX_X = 299941.7864;
    private final double CH_MIN_Y = 485869.5728;
    private final double CH_MAX_Y = 837076.5648;
    private final double CH_X = CH_MAX_X - CH_MIN_X;
    private final double CH_Y = CH_MAX_Y - CH_MIN_Y;
    private double CH_PX_X;
    private double CH_PX_Y;
    private int zoom;
    private int tileSize;

    public CH1903(final Copyright copyright, final int tileSize, final int minZoom,
            final int maxZoom, final int initialZoom) {
        super(copyright, tileSize, minZoom, maxZoom);
        zoom = initialZoom;
        this.tileSize = tileSize;
        setChMapPixelWidth(initialZoom);
    }

    public CH1903(final String copyright, final int tileSize, final int minZoom, final int maxZoom,
            final int initialZoom) {
        super(copyright, tileSize, minZoom, maxZoom);
        zoom = initialZoom;
        this.tileSize = tileSize;
        setChMapPixelWidth(initialZoom);
    }

    public Point mapPosToWgs(MapPos pos) {
        // Convert from CH1903 to pixel
        int y_aux = (int) PIXtoCHy((double) -pos.getY() - FALSE_EASTING);
        int x_aux = (int) PIXtoCHx((double) pos.getX() - FALSE_NORTHING);
        // int y_aux = pos.getY();
        // int x_aux = pos.getX();

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
        _lat = Math.round(_lat * 100 / 36 * 1000000D);
        _long = Math.round(_long * 100 / 36 * 1000000D);

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
        double x = 200147.07 + 308807.95 * lat_aux + 3745.25 * Math.pow(lng_aux, 2) + 76.63
                * Math.pow(lat_aux, 2) - 194.56 * Math.pow(lng_aux, 2) * lat_aux + 119.79
                * Math.pow(lat_aux, 3);
        double y = 600072.37 + 211455.93 * lng_aux - 10938.51 * lng_aux * lat_aux - 0.36 * lng_aux
                * Math.pow(lat_aux, 2) - 44.54 * Math.pow(lng_aux, 3);

        // Convert from CH1903 to pixel
        x = CHxtoPIX(x);
        y = CHytoPIX(y);

        // Round & Decay
        x = Math.round(x + FALSE_EASTING);
        y = -Math.round(y + FALSE_NORTHING);

        Log.i(TAG + ":wgsToMapPos", "x=" + (int) x + ", y=" + (int) y);
        return new MapPos((int) x, (int) y, zoom);
    }

    private double CHxtoPIX(double pt) {
        Log.v(TAG, pt + " (CHx) -> " + (pt * CH_PX_X / CH_X) + " (PX)");
        return pt * CH_PX_X / CH_X;
    }

    private double CHytoPIX(double pt) {
        Log.v(TAG, pt + " (CHy) -> " + (pt * CH_PX_Y / CH_Y) + " (PX)");
        return pt * CH_PX_Y / CH_Y;
    }

    private double PIXtoCHx(double pt) {
        Log.v(TAG, pt + " (PX) -> " + (pt * CH_X / CH_PX_X) + " (CHx)");
        return pt * CH_X / CH_PX_X;
    }

    private double PIXtoCHy(double pt) {
        Log.v(TAG, pt + " (PX) -> " + (pt * CH_Y / CH_PX_Y) + " (CHy)");
        return pt * CH_Y / CH_PX_Y;
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
        zoom = middlePoint.getZoom();
        setChMapPixelWidth(zoom);
        return super.zoom(middlePoint, zoomSteps);
    }

    private void setChMapPixelWidth(int zoom) {
        switch (zoom) {
        case 14:
            CH_PX_X = 3 * tileSize;
            CH_PX_Y = 2 * tileSize;
            break;
        case 15:
            CH_PX_X = 4 * tileSize;
            CH_PX_Y = 3 * tileSize;
            break;
        case 16:
            CH_PX_X = 8 * tileSize;
            CH_PX_Y = 5 * tileSize;
            break;
        default:
            CH_PX_X = 3 * tileSize;
            CH_PX_Y = 2 * tileSize;
        }
    }
}
