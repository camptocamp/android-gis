package com.camptocamp.android.gis.control;

import java.lang.ref.WeakReference;

import android.content.Intent;
import android.util.Log;

import com.camptocamp.android.gis.BaseMap;
import com.camptocamp.android.gis.R;
import com.nutiteq.components.Route;
import com.nutiteq.services.DirectionsService;

public class DirectionsWaiter implements com.nutiteq.services.DirectionsWaiter {

    private static final String TAG = BaseMap.D + "C2CDirectionsWaiter";
    private WeakReference<BaseMap> mActivity;

    public DirectionsWaiter(BaseMap activity) {
        mActivity = new WeakReference<BaseMap>(activity);
    }

    @Override
    public void routingParsingError(String message) {
        Log.e(TAG, "routingParsingError: " + message);
        // Toast
        final BaseMap a = mActivity.get();
        if (a != null) {
            Intent i = new Intent(BaseMap.ACTION_TOAST);
            i.putExtra(BaseMap.EXTRA_MSG, message);
            a.sendBroadcast(i);
        }
    }

    @Override
    public void routingErrors(int errors) {
        String msg = "routingErrors: ";
        final BaseMap a = mActivity.get();
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
            Intent i = new Intent(BaseMap.ACTION_TOAST);
            i.putExtra(BaseMap.EXTRA_MSG, msg);
            a.sendBroadcast(i);
        }
        else {
            msg += errors;
        }
        Log.e(TAG, msg);
    }

    @Override
    public void routeFound(Route route) {
        final BaseMap map = mActivity.get();
        if (map != null) {
            // TODO: Remove last route
            map.getMapComponent().addLine(route.getRouteLine());
        }
        else {
            Log.e(TAG, "routeFound: Map activity is not reachable");
        }
    }

    @Override
    public void networkError() {
        final String msg = "networkError";
        Log.e(TAG, msg);
        // Toast
        final BaseMap a = mActivity.get();
        if (a != null) {
            Intent i = new Intent(BaseMap.ACTION_TOAST);
            i.putExtra(BaseMap.EXTRA_MSG, msg);
            a.sendBroadcast(i);
        }
    }

}
