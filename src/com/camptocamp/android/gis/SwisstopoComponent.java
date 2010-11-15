package com.camptocamp.android.gis;

import com.nutiteq.components.WgsPoint;

public class SwisstopoComponent extends C2CMapComponent {

    // private static final String TAG = Map.D + "SwisstopoComponent";

    public SwisstopoComponent(WgsPoint middlePoint, int width, int height, int zoom) {
        super(middlePoint, width, height, zoom);
    }

    @Override
    public void zoomIn() {
        if (middlePoint.getZoom() == displayedMap.getMaxZoom()) {
            return;
        }
        int z1 = middlePoint.getZoom();
        cleanMapBuffer();
        middlePoint = displayedMap.zoom(middlePoint, 1);
        tileMapBounds = displayedMap.getTileMapBounds(middlePoint.getZoom());

        // Zoom buffer according to map resolution
        double ratio = ((SwisstopoMap) displayedMap).resolutions.get(z1)
                / ((SwisstopoMap) displayedMap).resolutions.get(middlePoint.getZoom());

        createZoomBufferAndUpdateScreen(Math.log(ratio) / Math.log(2), true, false);

    }

    @Override
    public void zoomOut() {
        if (middlePoint.getZoom() == displayedMap.getMinZoom()) {
            return;
        }
        int z1 = middlePoint.getZoom();
        cleanMapBuffer();
        middlePoint = displayedMap.zoom(middlePoint, -1);
        tileMapBounds = displayedMap.getTileMapBounds(middlePoint.getZoom());

        // Zoom buffer according to map resolution
        double ratio = ((SwisstopoMap) displayedMap).resolutions.get(middlePoint.getZoom())
                / ((SwisstopoMap) displayedMap).resolutions.get(z1);

        createZoomBufferAndUpdateScreen(Math.log(ratio) / Math.log(2), true, true);
    }
}
