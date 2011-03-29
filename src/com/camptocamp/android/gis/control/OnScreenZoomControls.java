package com.camptocamp.android.gis.control;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import android.content.res.Resources;
import android.graphics.BitmapFactory;

import com.camptocamp.android.gis.R;

public class OnScreenZoomControls implements com.nutiteq.controls.OnScreenZoomControls {

    private Image btnZoomIn;
    private Image btnZoomInPressed;
    private Image btnZoomOut;
    private Image btnZoomOutPressed;
    private boolean mZoomOut = false;
    private boolean mZoomIn = false;
    private int posx;
    private int posy;
    public boolean mRelease = false;

    public OnScreenZoomControls(final Resources res) {
        btnZoomIn = new Image(BitmapFactory.decodeResource(res, R.drawable.btn_zoom_down_normal));
        btnZoomInPressed = new Image(BitmapFactory.decodeResource(res,
                R.drawable.btn_zoom_down_pressed));
        btnZoomOut = new Image(BitmapFactory.decodeResource(res, R.drawable.btn_zoom_up_normal));
        btnZoomOutPressed = new Image(BitmapFactory.decodeResource(res,
                R.drawable.btn_zoom_up_pressed));
    }

    public void paint(final Graphics g, final int displayWidth, final int displayHeight) {
        posx = displayWidth / 2;
        posy = displayHeight - 5;
        g.setClip(0, 0, displayWidth, displayHeight);
        if (mZoomIn && !mRelease) {
            g.drawImage(btnZoomInPressed, posx, posy, Graphics.BOTTOM | Graphics.RIGHT);
        }
        else {
            g.drawImage(btnZoomIn, posx, posy, Graphics.BOTTOM | Graphics.RIGHT);
        }
        if (mZoomOut && !mRelease) {
            g.drawImage(btnZoomOutPressed, posx, posy, Graphics.BOTTOM | Graphics.LEFT);
        }
        else {
            g.drawImage(btnZoomOut, posx, posy, Graphics.BOTTOM | Graphics.LEFT);
        }
    }

    @Override
    public int getControlAction(int x, int y) {
        mZoomIn = false;
        mZoomOut = false;
        if (y > posy - btnZoomIn.getHeight() && y < posy) {
            if (x < posx && x > posx - btnZoomIn.getWidth()) {
                mZoomIn = true;
                return CONTROL_ZOOM_IN;
            }
            else if (x > posx && x < posx + btnZoomOut.getWidth()) {
                mZoomOut = true;
                return CONTROL_ZOOM_OUT;
            }
        }
        return -1;
    }

}
