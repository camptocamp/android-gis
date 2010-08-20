package com.camptocamp.android.gis;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.components.WgsPoint;

public class SwissMapComponent extends BasicMapComponent {

    public SwissMapComponent(final String licenseKey, final String vendor, final String appname,
            final int width, final int height, final WgsPoint middlePoint, final int zoom) {
        super(licenseKey, vendor, appname, zoom, zoom, middlePoint, zoom);
    }

}
