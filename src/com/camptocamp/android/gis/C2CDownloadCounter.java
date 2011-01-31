package com.camptocamp.android.gis;

import com.nutiteq.net.NutiteqDownloadCounter;

public class C2CDownloadCounter extends NutiteqDownloadCounter {

    // private static final String TAG = Map.D + "C2CDownloadCounter";

    @Override
    public void networkRequest(final String url) {
        super.networkRequest(url);
        // Log.v(TAG, "networkRequests=" + getNumberOfNetworkRequests());
    }

}
