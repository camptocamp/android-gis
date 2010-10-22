package com.camptocamp.android.gis;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.WgsPoint;

public class MyMapComponent extends BasicMapComponent {

    private static final String TAG = Map.D + "MyMapComponent";
    private static final String KEY = "182be0c5cdcd5072bb1864cdee4d3d6e4c593f89365962.70956542";
    private static final int MOVE = 0;
    private static final long DELAY = 24; // ms

    private int lastpanx = 0;
    private int lastpany = 0;

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
        super.pointerPressed(x, y);
        mHandler.removeMessages(MOVE);
        posx = 0;
        posy = 0;
        current = 0f;
    }

    @Override
    public void pointerReleased(final int x, final int y) {
        super.pointerReleased(x, y);
        Message msg = mHandler.obtainMessage(MOVE, lastpanx, lastpany);
        mHandler.sendMessageDelayed(msg, DELAY);
    }

    DecelerateInterpolator di = new DecelerateInterpolator(1.0f);
    int posx = 0;
    int posy = 0;
    float ip = 0f;
    float current = 0f;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (current <= 1.0f) {
                ip = di.getInterpolation(current);
                int x = (int) Math.floor(((ip * msg.arg1) - posx));
                int y = (int) Math.floor(((ip * msg.arg2) - posy));
                panMap(x, y);
                posx += x;
                posy += y;
                current += 0.1;
                Message msg1 = mHandler.obtainMessage(MOVE, msg.arg1, msg.arg2);
                mHandler.sendMessageDelayed(msg1, DELAY);
            }
        }
    };
}
