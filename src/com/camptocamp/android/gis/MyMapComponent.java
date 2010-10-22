package com.camptocamp.android.gis;

import android.os.AsyncTask;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.WgsPoint;

public class MyMapComponent extends BasicMapComponent {

    private static final String TAG = Map.D + "MyMapComponent";
    private static final String KEY = "182be0c5cdcd5072bb1864cdee4d3d6e4c593f89365962.70956542";

    private int lastpanx = 0;
    private int lastpany = 0;
    private EaseTask et = null;

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

        lastpanx = panX;
        lastpany = panY;

        super.panMap(panX, panY);
    }

    @Override
    public void pointerPressed(final int x, final int y) {
        if (et != null) {
            et.cancel(true);
        }
        super.pointerPressed(x, y);
    }

    @Override
    public void pointerReleased(final int x, final int y) {
        et = new EaseTask();
        et.execute(lastpanx, lastpany);
        super.pointerReleased(x, y);
    }

    class EaseTask extends AsyncTask<Integer, Integer, Void> {

        DecelerateInterpolator di = new DecelerateInterpolator(1.0f);
        float current = 0f;
        float ip = 0f;

        protected Void doInBackground(Integer... pos) {
            Log.v(TAG, "EaseOut: x=" + pos[0] + ", y=" + pos[1]);
            int posx = 0;
            int posy = 0;
            while (current < 1.0f) {
                ip = di.getInterpolation(current);
                int x = (int) Math.floor(((ip * pos[0]) - posx));
                int y = (int) Math.floor(((ip * pos[1]) - posy));
                publishProgress(x, y);
                posx += x;
                posy += y;
                current += 0.1;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onProgressUpdate(Integer... pos) {
            panMap(pos[0], pos[1]);
        }

        protected void onPostExecute(Void unused) {
            et = null;
        }

        protected void onCancelled() {
            et = null;
        }

    }
}
