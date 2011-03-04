package com.camptocamp.android.gis.layer;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;

import com.camptocamp.android.gis.MapComponent;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.Placemark;
import com.nutiteq.components.WgsBoundingBox;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.location.NutiteqLocationMarker;

// http://davy-leggieri.developpez.com/tutoriels/android/creation-boussole

public class LocationMarker extends NutiteqLocationMarker {

    private Canvas canvas;
    private Paint paint;
    private Bitmap bitmap = null;
    private float accuracy = 0; // m
    private int radius = 0; // px

    public LocationMarker(Placemark placemarkConnected, Placemark connectionLost,
            int updateInterval, boolean track) {
        super(placemarkConnected, connectionLost, updateInterval, track);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLUE);
        paint.setAlpha(50);
    }

    public LocationMarker(Placemark placemark, int updateInterval, boolean track) {
        super(placemark, placemark, updateInterval, track);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLUE);
        paint.setAlpha(50);
    }

    @Override
    protected void update() {
        if (track) {
            if (radius == 0) {
                mapComponent.setMiddlePoint(lastWgsLocation);
            }
            else {
                final MapPos pt1 = mapComponent.getMapPosition(lastWgsLocation);
                final MapPos pt2 = new MapPos(pt1.getX(), pt1.getY() + radius, mapComponent
                        .getZoom());
                final WgsPoint pt22 = mapComponent.getMap().mapPosToWgs(pt2).toWgsPoint();
                mapComponent.setZoom(mapComponent.getMap().getMinZoom());
                mapComponent.setBoundingBox(new WgsBoundingBox(lastWgsLocation, pt22));
            }
        }
        super.update();
    }

    @Override
    public void paint(final Graphics g, final MapPos mp, final int screenCenterX,
            final int screenCenterY) {
        // Update accuracy circle
        if (radius > 0) {
            final int size = (int) Math.ceil(radius * 2);
            bitmap = Bitmap.createBitmap(size, size, Config.ARGB_4444);
            canvas = new Canvas(bitmap);
            canvas.drawCircle(radius, radius, radius, paint);
            final int x = mapPosition.getX() - mp.getX() + screenCenterX;
            final int y = mapPosition.getY() - mp.getY() + screenCenterY;
            g.drawImage(new Image(bitmap), x, y, Graphics.VCENTER | Graphics.HCENTER);
        }
        super.paint(g, mp, screenCenterX, screenCenterY);
    }

    @Override
    public void quit() {
        super.quit();
        track = false;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
        setRadius();
    }

    public void setRadius() {
        // FIXME: Check (int)
        radius = (int) Math.round(accuracy / ((MapComponent) mapComponent).getMetersPerPixel());
    }

}
