package com.camptocamp.android.gis;

import com.nutiteq.components.MapTile;

public class OsmOverlay extends C2COverlay {

    // private static final String TAG = Map.D + "OsmOverlay";
    private String baseUrl;

    public OsmOverlay(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String getOverlayTileUrl(MapTile tile) {
        final int x = tile.getX() >> 8;
        final int y = tile.getY() >> 8;
        final int zoom = tile.getZoom();

        final StringBuffer url = new StringBuffer(baseUrl);
        url.append(zoom);
        url.append("/");
        url.append(x);
        url.append("/");
        url.append(y);
        url.append(".png");

        return url.toString();
    }

}
