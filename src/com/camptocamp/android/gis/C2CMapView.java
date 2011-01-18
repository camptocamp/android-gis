package com.camptocamp.android.gis;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.nutiteq.BasicMapComponent;
import com.nutiteq.android.MapView;

// Based on packages/apps/Gallery3D/src/com/cooliris/media/ScaleGestureDetector.java

public class C2CMapView extends MapView {

    private static final String TAG = "C2CMapView";

    // Tracking individual fingers
    // private float mTopFingerBeginY;
    // private float mBottomFingerBeginY;
    // private float mTopFingerCurrY;
    // private float mBottomFingerCurrY;

    private boolean mGestureInProgress;
    private double mAmoutZoomed = 0;

    public C2CMapView(Context context, BasicMapComponent component) {
        super(context, component);
    }

    public boolean onTouchEvent(final MotionEvent event) {
        final int action = event.getAction();

        if (!mGestureInProgress) {
            if ((action == MotionEvent.ACTION_POINTER_1_DOWN || action == MotionEvent.ACTION_POINTER_2_DOWN)
                    && event.getPointerCount() >= 2) {
                // Start gesture
                mGestureInProgress = true;
                mAmoutZoomed = 0;
//                mTopFingerBeginY = event.getY(0);
//                mBottomFingerBeginY = event.getY(1);
                Log.v(TAG, "start multitouch");
            } else {
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
        } else if (action == MotionEvent.ACTION_UP) {
            Log.v(TAG, "cur Zoom=" + mapComponent.getZoom());
            Log.v(TAG, "new Zoom=" + (mapComponent.getZoom() + (int) Math.round(mAmoutZoomed)));
            // Gesture action is finished
            Log.v(TAG, "stop multitouch");
            mGestureInProgress = false;
            // mapComponent.setZoom(mapComponent.getZoom() + (int)
            // Math.round(mAmoutZoomed));
            // mapComponent.createZoomBufferAndUpdateScreen2(0, false);
        } else {
            // Gesture is in progress
//            mTopFingerBeginY = mTopFingerCurrY;
//            mBottomFingerBeginY = mBottomFingerCurrY;
//            mTopFingerCurrY = event.getY(0);
//            mBottomFingerCurrY = event.getY(1);

            // // Do some zooming calculation
            // double factor = 0.1;
            // if (mTopFingerBeginY < mBottomFingerBeginY) {
            // // Top is Top (pointer_1)
            // if (mTopFingerBeginY > mTopFingerCurrY) {
            // mapComponent.zoomIn2(factor);
            // mAmoutZoomed += factor;
            // } else {
            // mapComponent.zoomOut2(factor);
            // mAmoutZoomed -= factor;
            // }
            // } else {
            // // Top is Bottom (pointer_2)
            // if (mBottomFingerBeginY > mBottomFingerCurrY) {
            // mapComponent.zoomIn2(factor);
            // mAmoutZoomed += factor;
            // } else {
            // mapComponent.zoomOut2(factor);
            // mAmoutZoomed -= factor;
            // }
            // }
            // Log.v(TAG, "diff=" + factor);
        }

        return true;
    }
}
