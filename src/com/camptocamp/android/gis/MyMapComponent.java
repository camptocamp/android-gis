package com.camptocamp.android.gis;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.WgsPoint;

public class MyMapComponent extends BasicMapComponent {

    // private static final String TAG = Map.D + "MyMapComponent";
    private static final String KEY = "182be0c5cdcd5072bb1864cdee4d3d6e4c593f89365962.70956542";

    // private int panStartX = 0;
    // private int panStartY = 0;
    // private long panStartTime = 0;
    // private EaseTask et = null;

    public MyMapComponent(WgsPoint middlePoint, int zoom) {
        super(KEY, Map.VDR, Map.APP, 1, 1, middlePoint, zoom);
    }

    @Override
    public void panMap(int panX, int panY) {
        int zoom = getZoom();
        MapPos pos = getInternalMiddlePoint();

        // Don't pan outside of map size
        if ((pos.getX() > displayedMap.getMapWidth(zoom) && panX > 0)
                || (pos.getX() < 0 && panX < 0)) {
            panX = 0;
        }
        if ((pos.getY() > displayedMap.getMapHeight(zoom) && panY > 0)
                || (pos.getY() < 0 && panY < 0)) {
            panY = 0;
        }

        super.panMap(panX, panY);
    }

    // @Override
    // public void pointerPressed(final int x, final int y) {
    // if (et != null) {
    // et.cancel(true);
    // }
    //
    // panStartTime = SystemClock.elapsedRealtime();
    // panStartX = x;
    // panStartY = y;
    // super.pointerPressed(x, y);
    // }
    //
    // @Override
    // public void pointerReleased(final int x, final int y) {
    // // http://en.wikipedia.org/wiki/Equations_of_Motion
    // // http://www.robertpenner.com/easing/easing_demo.html
    //
    // float t = SystemClock.elapsedRealtime() - panStartTime; // ms
    // float dx = -(x - panStartX); // px
    // float dy = -(y - panStartY); // px
    // float vx = dx / t; // px / ms
    // float vy = dy / t; // px / ms
    // float a = 9.81f;
    //
    // int dx2 = (int) Math.floor(dx - (vx * vx / 2 * 0.1));
    // int dy2 = (int) Math.floor(dy - (vy * vy / 2 * 0.1));
    //
    // // Log.v(TAG, "distance=" + d);
    // // Log.v(TAG, "duration=" + t);
    // // Log.v(TAG, "speed=" + v);
    //
    // // v = d/t
    // // a = v/t
    // // v = at
    //
    // // Log.v(TAG, "---");
    //
    // // v(t) = v0 + at;
    // // v²+2a(x-d) = 0
    // // v² + 2ax - 2ad = 0
    // // x = (2ad - v²)/2a
    // // x = d-(v²/2a)
    //
    // et = new EaseTask();
    // et.execute(dx2, dy2);
    //
    // super.pointerReleased(x, y);
    // }
    //
    // class EaseTask extends AsyncTask<Integer, Integer, Void> {
    //
    // DecelerateInterpolator di = new DecelerateInterpolator(0.5f);
    // float current = 0f;
    //
    // protected Void doInBackground(Integer... pos) {
    // Log.v(TAG, "EaseOut: x=" + pos[0] + ", y=" + pos[1]);
    // while (current < 1.0f) {
    // int x = (int) Math.floor(di.getInterpolation(current) * pos[0]);
    // int y = (int) Math.floor(di.getInterpolation(current) * pos[1]);
    // publishProgress(x, y);
    // current += 0.1;
    // try {
    // Thread.sleep(30);
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // }
    // return null;
    // }
    //
    // protected void onProgressUpdate(Integer... pos) {
    // panMap(pos[0], pos[1]);
    // }
    //
    // protected void onPostExecute(Void unused) {
    // et = null;
    // }
    //
    // protected void onCancelled() {
    // et = null;
    // }
    //
    // }
}
