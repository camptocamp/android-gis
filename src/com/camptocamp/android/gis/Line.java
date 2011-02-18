package com.camptocamp.android.gis;

import com.nutiteq.components.LineStyle;
import com.nutiteq.components.WgsPoint;

public class Line extends com.nutiteq.components.Line {

    public long time;

    public Line(WgsPoint[] points) {
        super(points, new LineStyle(LineStyle.DEFAULT_COLOR, LineStyle.DEFAULT_WIDTH), null, false);
        time = System.currentTimeMillis();
    }

}
