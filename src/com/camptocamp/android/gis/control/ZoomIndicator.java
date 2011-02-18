package com.camptocamp.android.gis.control;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.nutiteq.components.Rectangle;
import com.nutiteq.components.ZoomRange;
import com.nutiteq.log.Log;

public class ZoomIndicator implements com.nutiteq.ui.ZoomIndicator {

    private static final int WIDTH = 17;
    private static final int SLIDER_HEIGHT = 9;
    private static final int PART_HEIGHT = 13;
    private static final int PART_OFFSET = 7;

    private int mZoomRange;
    private int mMaxZoom;
    private Image mVerticalPart;
    private Image mHorizontalPart;
    private Image mSlider;

    public ZoomIndicator(final int min, final int max) {
        try {
            mVerticalPart = Image.createImage("/images/zoom2.png");
            mHorizontalPart = Image.createImage("/images/zoom3.png");
            mSlider = Image.createImage("/images/zoom1.png");
        } catch (final IOException e) {
            Log.printStackTrace(e);
        }
    }

    @Override
    public void paint(Graphics g, int currentZoom, int displayWidth, int displayHeight) {
        final Rectangle clip = new Rectangle(g.getClipX(), g.getClipY(), g.getClipWidth(), g
                .getClipHeight());

        int xpos = displayHeight - (mZoomRange - 1) * (PART_OFFSET + 1) - PART_OFFSET * 3;
        int ypos = displayWidth - SLIDER_HEIGHT;

        for (int i = 0; i < mZoomRange; i++) {
            g.setClip(ypos - WIDTH, 2 + xpos + PART_OFFSET * i + (i > 0 ? 1 : 0), WIDTH,
                    PART_HEIGHT);
            g.drawImage(mHorizontalPart, ypos - 1 - 2, 2 + xpos + 5 + PART_OFFSET * i, Graphics.TOP
                    | Graphics.RIGHT);
            g.drawImage(mVerticalPart, ypos - 6 - 2, 2 + xpos + PART_OFFSET * i, Graphics.TOP
                    | Graphics.RIGHT);
        }

        g.setClip(ypos - 2 - WIDTH, 4 + xpos + PART_OFFSET * (mMaxZoom - currentZoom), WIDTH,
                SLIDER_HEIGHT);
        g.drawImage(mSlider, ypos - 2, 4 + xpos + PART_OFFSET * (mMaxZoom - currentZoom),
                Graphics.TOP | Graphics.RIGHT);

        g.setClip(clip.getX(), clip.getY(), clip.getX(), clip.getY());
    }

    @Override
    public long displayTime() {
        return 0;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void setVisible(boolean visible) {
        // Always visible, TODO from prefs
    }

    @Override
    public void setZoomRange(final ZoomRange zRange) {
        mMaxZoom = zRange.getMaxZoom();
        mZoomRange = mMaxZoom - zRange.getMinZoom() + 1;
    }

}
