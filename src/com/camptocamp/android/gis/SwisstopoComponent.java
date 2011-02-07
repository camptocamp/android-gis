package com.camptocamp.android.gis;

import com.nutiteq.components.WgsPoint;

public class SwisstopoComponent extends C2CMapComponent {

    // private static final String TAG = Map.D + "SwisstopoComponent";

    protected static final int ZOOM = 14;

    public SwisstopoComponent(WgsPoint middlePoint, int width, int height, int zoom) {
        super(middlePoint, width, height, (zoom != -1 ? zoom : ZOOM));
        setZoomLevelIndicator(new C2CZoomIndicator(SwisstopoMap.MIN_ZOOM, SwisstopoMap.MAX_ZOOM));
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

        // Set precision radius if gps tracking is active
        if (locationSource != null) {
            ((C2CLocationMarker) locationSource.getLocationMarker()).setRadius();
        }
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

        // Set precision radius if gps tracking is active
        if (locationSource != null) {
            ((C2CLocationMarker) locationSource.getLocationMarker()).setRadius();
        }
    }

    @Override
    public void setZoom(final int newZoom) {
        final int currentZoom = middlePoint.getZoom();
        if (currentZoom == newZoom) {
            return;
        }
        cleanMapBuffer();

        double ratio = ((SwisstopoMap) displayedMap).resolutions.get(newZoom)
                / ((SwisstopoMap) displayedMap).resolutions.get(currentZoom);
        double scale = Math.log(ratio) / Math.log(2);
        int dif = 0;

        if (currentZoom < newZoom && newZoom > displayedMap.getMaxZoom()) {
            dif = displayedMap.getMaxZoom() - currentZoom;
            middlePoint = displayedMap.zoom(middlePoint, dif);
            tileMapBounds = displayedMap.getTileMapBounds(middlePoint.getZoom());
            createZoomBufferAndUpdateScreen(scale, true, true);
        } else if (newZoom < displayedMap.getMinZoom()) {
            dif = displayedMap.getMinZoom() - currentZoom;
            middlePoint = displayedMap.zoom(middlePoint, dif);
            tileMapBounds = displayedMap.getTileMapBounds(middlePoint.getZoom());
            createZoomBufferAndUpdateScreen(scale, true, false);
        }
    }

    @Override
    public double getMetersPerPixel() {
        return ((SwisstopoMap) displayedMap).resolutions.get(middlePoint.getZoom());
    }
}
