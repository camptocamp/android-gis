package com.camptocamp.android.gis;

import java.util.HashMap;

import com.nutiteq.maps.MapTileOverlay;

public abstract class C2COverlay implements MapTileOverlay {

    protected HashMap<String, String> layers_all = null;
    protected String layers_selected = null;

}
