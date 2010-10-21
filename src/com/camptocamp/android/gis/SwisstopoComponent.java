package com.camptocamp.android.gis;

import com.nutiteq.components.WgsPoint;

public class SwisstopoComponent extends MyMapComponent {

    // private static final String TAG = Map.D + "SwisstopoComponent";

    public SwisstopoComponent(WgsPoint middlePoint, int zoom) {
        super(middlePoint, zoom);
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
        double ratio = SwisstopoMap.resolutions.get(z1)
                / SwisstopoMap.resolutions.get(middlePoint.getZoom());

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
        double ratio = SwisstopoMap.resolutions.get(middlePoint.getZoom())
                / SwisstopoMap.resolutions.get(z1);

        createZoomBufferAndUpdateScreen(Math.log(ratio) / Math.log(2), true, true);
    }
}
