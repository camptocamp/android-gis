package com.camptocamp.android.gis;

import com.nutiteq.components.MapTile;
import com.nutiteq.maps.MapTileOverlay;

public class SwisstopoOverlay implements MapTileOverlay {
    // private static final String TAG = Map.D + "TestOverlay";
    private static final int TILESIZE = 256;
    private String baseUrl;
    private String layers;

    public SwisstopoOverlay(final String baseUrl, final String layers) {
        this.baseUrl = baseUrl;
        this.layers = layers;
    }

    @Override
    public String getOverlayTileUrl(MapTile tile) {
        SwisstopoMap map = (SwisstopoMap) tile.getMap();
        int tx = tile.getX();
        int ty = tile.getY() + TILESIZE;
        double x1 = map.PIXtoCHx(tx);
        double y1 = map.PIXtoCHy(ty);
        double x2 = map.PIXtoCHx(tx + TILESIZE);
        double y2 = map.PIXtoCHy(ty + TILESIZE);
        return String.format(baseUrl, layers, x1, y1, x2, y2);
    }

}
