package com.camptocamp.android.gis.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.lcdui.Image;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.camptocamp.android.gis.BaseMap;
import com.camptocamp.android.gis.Line;
import com.camptocamp.android.gis.R;
import com.camptocamp.android.gis.layer.LocationMarker;
import com.nutiteq.components.PlaceIcon;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.location.LocationListener;
import com.nutiteq.location.LocationSource;

public class GpsProvider implements LocationSource, android.location.LocationListener {

    private static final String TAG = "C2CGpsProvider";
    private static final int DT_GPS = 1000 * 60 * 2; // 2mn
    private static final int DT_NETWORK = 1000 * 60 * 5; // 5mn
    private static final long UP_IDLE = 5000L;
    private static final long UP_ACTIVE = 1000L;
    private static final float UP_DIST = 5F;

    private LocationMarker marker;
    private LocationMarker marker_simple;
    private LocationManager manager;
    private Location location = null;
    private WeakReference<BaseMap> mMap;
    private int status = STATUS_CONNECTING;

    private List<Line> trace = new ArrayList<Line>(0);
    private boolean record = false;
    private boolean track = true; // Will only track 1st time -> LocationMarker:60

    public GpsProvider(BaseMap a) {
        mMap = new WeakReference<BaseMap>(a);

        final Resources resources = a.getResources();
        manager = (LocationManager) a.getSystemService(Context.LOCATION_SERVICE);
        marker_simple = new LocationMarker(new PlaceIcon(new Image(BitmapFactory.decodeResource(
                resources, R.drawable.marker))), new PlaceIcon(new Image(BitmapFactory
                .decodeResource(resources, R.drawable.marker_offline))), 0, track);
        setLocationMarker(marker_simple);
    }

    public boolean isRecord() {
        return record;
    }

    public void setBaseMap(BaseMap map) {
        mMap = new WeakReference<BaseMap>(map);
    }

    /**
     * Herited from android
     */
    @Override
    public void onLocationChanged(Location loc) {
        Log.v(TAG, "loc=" + loc.getLatitude() + ", " + loc.getLongitude() + ", "
                + loc.getAccuracy() + ", " + loc.getProvider());
        if (loc != null && isBetterLocation(loc)) {
            // Update Bearing
            // FIXME THIS IS BULLSHIT
            // if (loc.hasBearing()) {
            // setLocationMarker(new LocationMarker(new PlaceIcon(rotateImage(Utils
            // .createImage("/res/drawable/direction.png"))), 0, track));
            // }
            // else {
            // setLocationMarker(marker_simple);
            // }

            // Update Marker
            marker.setLocation(new WgsPoint(loc.getLongitude(), loc.getLatitude()));
            marker.setAccuracy(loc.getAccuracy());

            // Update trace
            // TODO: Cut if signal lost ?
            if (record) {
                BaseMap map = mMap.get();
                if (map != null && location != null) {
                    Line line = new Line(new WgsPoint[] {
                            new WgsPoint(location.getLongitude(), location.getLatitude()),
                            new WgsPoint(loc.getLongitude(), loc.getLatitude()) });
                    map.getMapComponent().addLine(line);
                    trace.add(line);
                }
            }
            // Network location no more needed as primary source
            if (LocationManager.GPS_PROVIDER.equals(loc.getProvider()) && location != null
                    && LocationManager.NETWORK_PROVIDER.equals(location)) {
                manager.removeUpdates(GpsProvider.this);
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UP_ACTIVE, UP_DIST,
                        GpsProvider.this);
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UP_IDLE, UP_DIST,
                        GpsProvider.this);
            }
            location = loc;
        }
        status = STATUS_CONNECTED;
    }

    @Override
    public void onProviderEnabled(String provider) {
        status = STATUS_CONNECTING;
    }

    @Override
    public void onProviderDisabled(final String provider) {
        final BaseMap map = mMap.get();
        // TODO: allow location from cellid/wifi only
        // GPS provider is disabled
        if (map != null && LocationManager.GPS_PROVIDER.equals(provider)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(map);
            dialog.setTitle(R.string.toast_gps_disabled);
            dialog.setPositiveButton(R.string.btn_ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    map.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            dialog.show();
            map.setTrackingPosition(false);
        }
        status = STATUS_CONNECTION_LOST;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (status == LocationProvider.AVAILABLE) {
            this.status = STATUS_CONNECTED;
        }
        else {
            this.status = STATUS_CONNECTION_LOST;
        }
    }

    /**
     * Herited from nutiteq
     */
    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public WgsPoint getLocation() {
        return new WgsPoint(location.getLongitude(), location.getLatitude());
    }

    @Override
    public void setLocationMarker(com.nutiteq.location.LocationMarker marker) {
        this.marker = (LocationMarker) marker;
        marker.setLocationSource(this);
        final BaseMap map = mMap.get();
        if (map != null) {
            marker.setMapComponent(map.getMapComponent());
        }
        else {
            marker.quit();
        }
    }

    @Override
    public LocationMarker getLocationMarker() {
        return marker;
    }

    @Override
    public void start() {
        // get last known location
        Location loc = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc != null) {
            loc.setProvider(LocationManager.NETWORK_PROVIDER);
            loc.setTime(System.currentTimeMillis());
            onLocationChanged(loc);
        }
        // Register for location updates
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UP_IDLE, UP_DIST,
                GpsProvider.this);
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UP_ACTIVE, UP_DIST,
                GpsProvider.this);
    }

    @Override
    public void quit() {
        status = STATUS_CONNECTION_LOST;
        manager.removeUpdates(GpsProvider.this);
        if (marker != null) {
            marker.quit();
            // FIXME: Marker needs to repaint
            // marker.update();
        }
    }

    @Override
    public void addLocationListener(LocationListener listener) {
        // Unused
    }

    /**
     * Custom
     */

    // private Image rotateImage(Image img) {
    // Bitmap src = img.getBitmap();
    // int width = src.getWidth();
    // int height = src.getHeight();
    // Matrix matrix = new Matrix();
    // matrix.postRotate(45);
    // Bitmap dst = Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
    // return new Image(dst);
    // }

    // http://developer.android.com/guide/topics/location/obtaining-user-location.html#BestEstimate
    private boolean isBetterLocation(Location loc) {
        // return true; // FIXME: just for dev
        if (location == null) {
            return true;
        }

        // Check if the location is new or old
        long timeDelta = loc.getTime() - location.getTime();
        boolean isSignificantlyNewer;
        boolean isSignificantlyOlder;
        if (LocationManager.GPS_PROVIDER.equals(loc.getProvider())) {
            isSignificantlyNewer = timeDelta > DT_GPS;
            isSignificantlyOlder = timeDelta < -DT_GPS;
        }
        else {
            isSignificantlyNewer = timeDelta > DT_NETWORK;
            isSignificantlyOlder = timeDelta < -DT_NETWORK;
        }
        boolean isNewer = timeDelta > 0;

        // Difference is significant
        if (isSignificantlyNewer) {
            Log.v(TAG, "isSignificantlyNewer");
            return true;
        }
        else if (isSignificantlyOlder) {
            Log.v(TAG, "isSignificantlyOlder");
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (loc.getAccuracy() - location.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
        boolean isFromSameProvider = isSameProvider(loc.getProvider(), location.getProvider());

        // Determine location quality
        if (isMoreAccurate) {
            Log.v(TAG, "isMoreAccurate");
            return true;
        }
        else if (isNewer && !isLessAccurate) {
            Log.v(TAG, "isNewer");
            return true;
        }
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            Log.v(TAG, "isNewer && isFromSameProvider");
            return true;
        }

        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void setRecord(boolean rec) {
        record = rec;
    }

    public List<Line> getTrace() {
        return trace;
    }
}
