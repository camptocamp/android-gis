package com.camptocamp.android.gis;

import com.nutiteq.components.WgsPoint;
import com.nutiteq.location.LocationSource;

public class SwisstopoComponent extends C2CMapComponent {

    // private static final String TAG = Map.D + "SwisstopoComponent";

    public static final int ZOOM = 14;

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
        final LocationSource loc = getLocationSource();
        if (loc != null) {
            ((C2CLocationMarker) loc.getLocationMarker()).setRadius();
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
        final LocationSource loc = getLocationSource();
        if (loc != null) {
            ((C2CLocationMarker) loc.getLocationMarker()).setRadius();
        }
    }

    @Override
    public double getMetersPerPixel() {
        return ((SwisstopoMap) displayedMap).resolutions.get(middlePoint.getZoom());
    }
}
