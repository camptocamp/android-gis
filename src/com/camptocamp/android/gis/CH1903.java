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
 */
public abstract class CH1903 extends BaseMap implements Projection {

	public CH1903(final Copyright copyright, final int tileSize, final int minZoom, final int maxZoom) {
		super(copyright, tileSize, minZoom, maxZoom);
	}

	public CH1903(final String copyright, final int tileSize, final int minZoom, final int maxZoom) {
		super(copyright, tileSize, minZoom, maxZoom);
	}

	public Point fromWgs(final Point from) {
		Log.v("fromWgs", "x1=" + from.getX() + ", y1=" + from.getY());
		final double lat_aux = (from.getX() - 169028.66) / 10000;
		final double long_aux = (from.getY() - 26782.5) / 10000;
		double x = 200147.07 + (308807.95 * long_aux) + (3745.25 * Math.pow(lat_aux, 2))
				+ (76.63 * Math.pow(long_aux, 2)) - (194.56 * Math.pow(lat_aux, 2) * long_aux)
				+ (119.79 * Math.pow(long_aux, 3));
		double y = 600072.37 + (211455.93 * lat_aux) - (10938.51 * lat_aux * long_aux)
				- (0.36 * lat_aux * Math.pow(long_aux, 2)) - (44.54 * Math.pow(lat_aux, 3));
		Log.v("fromWgs", "x2=" + x + ", y2=" + y);
		return new Point((int) x, (int) y);
	}

	public Point toWgs(final Point to) {
		Log.v("toWgs", "x=" + to.getX() + ", y=" + to.getY());
		return to;
	}

	public Point mapPosToWgs(MapPos pos) {
		Log.v("mapPosToWgs", "x=" + pos.getX());
		return null;
	}

	public MapPos wgsToMapPos(Point wgs, int zoom) {
		return null;
	}

}
