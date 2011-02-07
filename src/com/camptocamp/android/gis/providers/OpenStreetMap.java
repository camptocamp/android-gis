package com.camptocamp.android.gis.providers;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.nutiteq.ui.Copyright;

public class OpenStreetMap extends com.nutiteq.maps.OpenStreetMap {

    private Image missingTile;

    public OpenStreetMap(String baseUrl, int tileSize, int minZoom, int maxZoom) {
        super(baseUrl, tileSize, minZoom, maxZoom);
    }

    public OpenStreetMap(final Copyright copyright, final String baseUrl, final int tileSize,
            final int minZoom, final int maxZoom) {
        super(copyright, baseUrl, tileSize, minZoom, maxZoom);
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
