package com.camptocamp.android.gis;

import com.nutiteq.components.Line;
import com.nutiteq.components.LineStyle;
import com.nutiteq.components.WgsPoint;

public class C2CLine extends Line {

    public long time;

    public C2CLine(WgsPoint[] points) {
        super(points, new LineStyle(LineStyle.DEFAULT_COLOR, LineStyle.DEFAULT_WIDTH), null, false);
        time = System.currentTimeMillis();
    }

}
