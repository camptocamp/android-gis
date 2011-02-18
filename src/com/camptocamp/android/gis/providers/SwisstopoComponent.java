package com.camptocamp.android.gis.providers;

import com.camptocamp.android.gis.MapComponent;
import com.camptocamp.android.gis.control.ZoomIndicator;
import com.camptocamp.android.gis.layer.LocationMarker;
import com.nutiteq.components.WgsPoint;

public class SwisstopoComponent extends MapComponent {

    // private static final String TAG = Map.D + "SwisstopoComponent";

    protected static final int ZOOM = 14;

    public SwisstopoComponent(WgsPoint middlePoint, int width, int height, int zoom) {
        super(middlePoint, width, height, (zoom != -1 ? zoom : ZOOM));
        setZoomLevelIndicator(new ZoomIndicator(SwisstopoMap.MIN_ZOOM, SwisstopoMap.MAX_ZOOM));
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
        double ratio = ((SwisstopoMap) displayedMap).getResolution(z1)
                / ((SwisstopoMap) displayedMap).getResolution(middlePoint.getZoom());

        createZoomBufferAndUpdateScreen(Math.log(ratio) / Math.log(2), true, false);

        // Set precision radius if gps tracking is active
        if (locationSource != null) {
            ((LocationMarker) locationSource.getLocationMarker()).setRadius();
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
        double ratio = ((SwisstopoMap) displayedMap).getResolution(middlePoint.getZoom())
                / ((SwisstopoMap) displayedMap).getResolution(z1);

        createZoomBufferAndUpdateScreen(Math.log(ratio) / Math.log(2), true, true);

        // Set precision radius if gps tracking is active
        if (locationSource != null) {
            ((LocationMarker) locationSource.getLocationMarker()).setRadius();
        }
    }

    @Override
    public void setZoom(final int newZoom) {
        final int currentZoom = middlePoint.getZoom();
        if (currentZoom == newZoom) {
            return;
        }
        cleanMapBuffer();

        double ratio = ((SwisstopoMap) displayedMap).getResolution(newZoom)
                / ((SwisstopoMap) displayedMap).getResolution(currentZoom);
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
        return ((SwisstopoMap) displayedMap).getResolution(middlePoint.getZoom());
    }
}
