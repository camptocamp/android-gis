package com.camptocamp.android.gis.control;

import android.content.Context;
import android.os.Build;
import android.util.FloatMath;
import android.view.MotionEvent;

import com.camptocamp.android.gis.MapComponent;
import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MotionEventWrap;
import com.nutiteq.android.RepaintHandler;

public class MyMapView extends com.nutiteq.android.MapView {

    private static final int MIN = 60;
    private static final int MAX = 120;
    private static final int ACTION_POINTER_1_DOWN = 5;
    private static final int ACTION_POINTER_2_DOWN = 261;
    private boolean mGestureInProgress;

    private float mOldDist;
    private float mNewDist;
    private float mAmount = 0;
    private float mAmountAbs;

    public MyMapView(Context context, BasicMapComponent component) {
        super(context, component);
    }

    public void set(Context context, MapComponent mc) {
        mapComponent = mc;
        mContext = context;
        appMapListener = mc.getMapListener();
        mc.setMapListener(this);
        setFocusable(true);
        setClickable(true);
        setEnabled(true);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean hasMultiTouch = Integer.parseInt(Build.VERSION.SDK) >= 5;
        int nPointer = hasMultiTouch ? MotionEventWrap.getPointerCount(event) : 1;

        if (mapComponent == null) {
            // FIXME: Why is this happening ?
            // adb shell monkey -p de.georepublic.android.gis -v 500 --throttle 20
            return false;
        }
        final int action = event.getAction();
        if (!mGestureInProgress) {
            if ((action == ACTION_POINTER_1_DOWN || action == ACTION_POINTER_2_DOWN)
                    && nPointer >= 2 && hasMultiTouch) {
                mOldDist = getDistance(event);
                // if (mOldDist > 10f) {
                // // Start MT gesture
                mGestureInProgress = true;
                // }
            }
            else {
                // Normal touch action
                final int x = (int) event.getX();
                final int y = (int) event.getY();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mapComponent.pointerPressed(x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mapComponent.pointerDragged(x, y);
                        break;
                    case MotionEvent.ACTION_UP:
                        mapComponent.pointerReleased(x, y);
                        mGestureInProgress = false;
                        break;
                }
            }
        }
        else if (action == MotionEvent.ACTION_UP) {
            mGestureInProgress = false;
        }
        else {
            if (mGestureInProgress) {
                mNewDist = getDistance(event);
                mAmount += mNewDist - mOldDist;
                mAmountAbs = Math.abs(mAmount);
                if (mAmountAbs > MIN && mAmountAbs < MAX) {
                    if (mAmount > 0) {
                        mapComponent.zoomIn();
                    }
                    else {
                        mapComponent.zoomOut();
                    }
                    mAmount = 0;
                }
                else if (mAmountAbs > MAX) {
                    mAmount = 0;
                }
                mOldDist = mNewDist;
            }
        }
        return true;
    }

    private float getDistance(final MotionEvent event) {
        final float x = MotionEventWrap.getX(event, 0) - MotionEventWrap.getX(event, 1);
        final float y = MotionEventWrap.getY(event, 0) - MotionEventWrap.getY(event, 1);
        return FloatMath.sqrt(x * x + y * y);
    }
}
