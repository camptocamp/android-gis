package com.camptocamp.android.gis;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

import com.nutiteq.location.providers.AndroidGPSProvider;

public class C2CGpsProvider extends AndroidGPSProvider {

    private WeakReference<Map> mMap;

    public C2CGpsProvider(Map a) {
        super((LocationManager) a.getSystemService(Context.LOCATION_SERVICE), 1000L);
        mMap = new WeakReference<Map>(a);
    }

    @Override
    public void onProviderDisabled(final String provider) {
        final Map a = mMap.get();
        if (a != null) {
            a.isTrackingPosition = false;
            a.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            Toast.makeText(a, R.string.toast_gps_disabled, Toast.LENGTH_SHORT).show();
        }
    }
}
