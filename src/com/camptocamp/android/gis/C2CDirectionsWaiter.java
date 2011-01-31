package com.camptocamp.android.gis;

import java.lang.ref.WeakReference;

import android.content.Intent;
import android.util.Log;

import com.nutiteq.components.Route;
import com.nutiteq.services.DirectionsService;
import com.nutiteq.services.DirectionsWaiter;

public class C2CDirectionsWaiter implements DirectionsWaiter {

    private static final String TAG = Map.D + "C2CDirectionsWaiter";
    private WeakReference<Map> mActivity;

    public C2CDirectionsWaiter(Map activity) {
        mActivity = new WeakReference<Map>(activity);
    }

    @Override
    public void routingParsingError(String message) {
        Log.e(TAG, "routingParsingError: " + message);
        // Toast
        final Map a = mActivity.get();
        if (a != null) {
            Intent i = new Intent(Map.ACTION_TOAST);
            i.putExtra(Map.EXTRA_MSG, message);
            a.sendBroadcast(i);
        }
    }

    @Override
    public void routingErrors(int errors) {
        String msg = "routingErrors: ";
        final Map a = mActivity.get();
        if (a != null) {
            switch (errors) {
            case DirectionsService.ERROR_DESTINATION_ADDRESS_NOT_FOUND:
                msg += a.getString(R.string.toast_route_dst_not_found);
                break;
            case DirectionsService.ERROR_FROM_ADDRESS_NOT_FOUND:
                msg += a.getString(R.string.toast_route_src_not_found);
                break;
            case DirectionsService.ERROR_FROM_AND_DESTINATION_ADDRESS_SAME:
                msg += a.getString(R.string.toast_route_same);
                break;
            case DirectionsService.ERROR_ROUTE_NOT_FOUND:
                msg += a.getString(R.string.toast_route_not_found);
                break;
            default:
                msg += a.getString(R.string.toast_route_unknown);
            }
            // Toast
            Intent i = new Intent(Map.ACTION_TOAST);
            i.putExtra(Map.EXTRA_MSG, msg);
            a.sendBroadcast(i);
        } else {
            msg += errors;
        }
        Log.e(TAG, msg);
    }

    @Override
    public void routeFound(Route route) {
        final Map a = mActivity.get();
        if (a != null) {
            // TODO: Remove last route
            a.mapComponent.addLine(route.getRouteLine());
        } else {
            Log.e(TAG, "routeFound: Map activity is not reachable");
        }
    }

    @Override
    public void networkError() {
        final String msg = "networkError";
        Log.e(TAG, msg);
        // Toast
        final Map a = mActivity.get();
        if (a != null) {
            Intent i = new Intent(Map.ACTION_TOAST);
            i.putExtra(Map.EXTRA_MSG, msg);
            a.sendBroadcast(i);
        }
    }

}
