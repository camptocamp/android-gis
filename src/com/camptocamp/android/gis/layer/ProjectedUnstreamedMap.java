package com.camptocamp.android.gis.layer;

import com.nutiteq.components.MapPos;
import com.nutiteq.components.Point;
import com.nutiteq.maps.BaseMap;
import com.nutiteq.maps.UnstreamedMap;
import com.nutiteq.maps.projections.Projection;
import com.nutiteq.ui.Copyright;

public abstract class ProjectedUnstreamedMap extends BaseMap implements Projection, UnstreamedMap {
	
	protected Projection projection;
	
	public ProjectedUnstreamedMap(final Copyright copyright, final int tileSize, final int minZoom, final int maxZoom, Projection projection) {
		super(copyright, tileSize, minZoom, maxZoom);
		this.projection = projection;
	}

	@Override
	public Point mapPosToWgs(MapPos pos) {
		return this.projection.mapPosToWgs(pos);
	}

	@Override
	public MapPos wgsToMapPos(Point wgs, int zoom) {
		return this.projection.wgsToMapPos(wgs, zoom);
	}
}
