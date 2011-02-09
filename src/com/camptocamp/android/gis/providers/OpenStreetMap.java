package com.camptocamp.android.gis.providers;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class OpenStreetMap extends com.nutiteq.maps.OpenStreetMap {

    private Image missingTile;
    private static final String OSM_MAPNIK_URL = "http://tile.openstreetmap.org/";
    private static final int MAX_ZOOM = 18;

    public OpenStreetMap(String baseUrl, int tileSize, int minZoom, int maxZoom) {
        super(baseUrl, tileSize, minZoom, maxZoom);
    }

    public OpenStreetMap() {
        super(OSM_MAPNIK_URL, TILE_SIZE, MIN_ZOOM, MAX_ZOOM);
    }

    @Override
    public Image getMissingTileImage() {
        if (missingTile == null) {
            try {
                missingTile = Image.createImage("/images/notile.png");
            } catch (IOException e) {
                missingTile = Image.createImage(getTileSize(), getTileSize());
                final Graphics g = missingTile.getGraphics();
                g.setColor(0xFFFF0000);
                g.fillRect(0, 0, getTileSize(), getTileSize());
            }
        }
        return missingTile;
    }

}
