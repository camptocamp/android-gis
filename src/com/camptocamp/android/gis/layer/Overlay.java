package com.camptocamp.android.gis.layer;

import java.util.HashMap;

import com.nutiteq.maps.MapTileOverlay;

public abstract class Overlay implements MapTileOverlay {

    protected HashMap<String, String> layersAll = null;
    protected String layersSelected = null;

    public HashMap<String, String> getLayersAll() {
        return layersAll;
    }

    public void setLayersSelected(String layersSelected) {
        this.layersSelected = layersSelected;
    }

    public String getLayersSelected() {
        return layersSelected;
    }
}
