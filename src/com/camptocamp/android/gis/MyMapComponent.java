package com.camptocamp.android.gis;

import android.util.Log;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.WgsPoint;

public class MyMapComponent extends BasicMapComponent {

    private static final String KEY = "182be0c5cdcd5072bb1864cdee4d3d6e4c593f89365962.70956542";

    public MyMapComponent(WgsPoint middlePoint, int zoom) {
        super(KEY, Map.VDR, Map.APP, 1, 1, middlePoint, zoom);
    }

    @Override
    public void panMap(int panX, int panY) {
        int zoom = getZoom();
        MapPos pos = getInternalMiddlePoint();

        // Don't pan outside of visible tiles
        if (pos.getX() > displayedMap.getMapWidth(zoom) && panX > 0) {
            panX = 0;
        }
        if (pos.getY() > displayedMap.getMapHeight(zoom) && panY > 0) {
            panY = 0;
        }
        if (pos.getX() < 0 && panX < 0) {
            panX = 0;
        }
        if (pos.getY() < 0 && panY < 0) {
            panY = 0;
        }
        super.panMap(panX, panY);
    }

}
