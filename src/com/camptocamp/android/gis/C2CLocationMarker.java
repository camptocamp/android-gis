package com.camptocamp.android.gis;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;

import com.nutiteq.components.MapPos;
import com.nutiteq.components.Placemark;
import com.nutiteq.location.NutiteqLocationMarker;

// http://davy-leggieri.developpez.com/tutoriels/android/creation-boussole

public class C2CLocationMarker extends NutiteqLocationMarker {

    private Canvas canvas;
    private Paint paint;
    private Bitmap bitmap = null;
    private float accuracy = 0;

    public C2CLocationMarker(Placemark placemarkConnected, Placemark connectionLost,
            int updateInterval, boolean track) {
        super(placemarkConnected, connectionLost, updateInterval, track);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLUE);
        paint.setAlpha(50);
    }

    @Override
    public void paint(final Graphics g, final MapPos mp, final int screenCenterX,
            final int screenCenterY) {
        // Update accuracy circle
        float radius = Math.round(accuracy / ((C2CMapComponent) mapComponent).getMetersPerPixel());
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

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

}
