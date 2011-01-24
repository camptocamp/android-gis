package com.camptocamp.android.gis;

import java.lang.ref.WeakReference;

import android.util.Log;

import com.nutiteq.components.Line;
import com.nutiteq.components.Route;
import com.nutiteq.services.DirectionsWaiter;

public class C2CDirectionsWaiter implements DirectionsWaiter {

    // FIXME: use broadcast intent to notify errors on UI thread

    private static final String TAG = Map.D + "C2CDirectionsWaiter";
    private WeakReference<Map> mActivity;
    private Line routing = null;

    public C2CDirectionsWaiter(Map activity) {
        mActivity = new WeakReference<Map>(activity);
    }

    @Override
    public void routingParsingError(String message) {
        Log.e(TAG, "Routing: " + message);
    }

    @Override
    public void routingErrors(int errors) {
        Log.e(TAG, "Routing: " + errors);
    }

    @Override
    public void routeFound(Route route) {
        final Map a = mActivity.get();
        if (a != null) {
            if (routing != null) {
                a.mapComponent.removeLine(routing);
            }
            routing = route.getRouteLine();
            a.mapComponent.addLine(routing);
        } else {
            Log.e(TAG, "Routing: Map activity is not reachable");
        }
    }

    @Override
    public void networkError() {
        Log.e(TAG, "Routing: Network error");
    }

}
