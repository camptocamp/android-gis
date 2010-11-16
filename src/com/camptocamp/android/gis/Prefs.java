package com.camptocamp.android.gis;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Prefs extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    // private PreferenceScreen ps;

    public final static String KEY_FS_CACHING = "fscaching";
    public final static boolean DEFAULT_FS_CACHING = false;

    public final static String KEY_FS_CACHING_SIZE = "fscaching_size";
    public final static int DEFAULT_FS_CACHING_SIZE = 1024 * 1024; // 1MB

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        // ps = getPreferenceScreen();
        // ps.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        // if (KEY_FS_CACHING.equals(key)) {
        // }
    }
}
