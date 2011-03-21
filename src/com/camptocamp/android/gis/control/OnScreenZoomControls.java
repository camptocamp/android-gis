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
    private int posx;
    private int posy;

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
        g.drawImage(btnZoomIn, posx, posy, Graphics.BOTTOM | Graphics.RIGHT);
        g.drawImage(btnZoomOut, posx, posy, Graphics.BOTTOM | Graphics.LEFT);
    }

    public void zoomInPressed(final Graphics g, final int displayWidth, final int displayHeight) {
        posx = displayWidth / 2;
        posy = displayHeight - 5;
        g.setClip(0, 0, displayWidth, displayHeight);
        g.drawImage(btnZoomInPressed, posx, posy, Graphics.BOTTOM | Graphics.RIGHT);
    }

    public void zoomOutPressed(final Graphics g, final int displayWidth, final int displayHeight) {
        // FIXME: Merge with zoomInPressed()
        posx = displayWidth / 2;
        posy = displayHeight - 5;
        g.setClip(0, 0, displayWidth, displayHeight);
        g.drawImage(btnZoomOutPressed, posx, posy, Graphics.BOTTOM | Graphics.LEFT);
    }

    @Override
    public int getControlAction(int x, int y) {
        // TODO: Change image on click
        if (y > posy - btnZoomIn.getHeight() && y < posy) {
            if (x < posx && x > posx - btnZoomIn.getWidth()) {
                return CONTROL_ZOOM_OUT;
            }
            else if (x > posx && x < posx + btnZoomOut.getWidth()) {
                return CONTROL_ZOOM_IN;
            }
        }
        return -1;
    }

}
