package com.camptocamp.android.gis;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nutiteq.cache.AndroidFileSystemCache;
import com.nutiteq.cache.Cache;
import com.nutiteq.cache.CachingChain;
import com.nutiteq.cache.MemoryCache;

public class C2CCaching extends CachingChain {

    private static final String TAG = Map.D + "C2CCaching";

    // An image is ~25kB => 1MB = 40 cached images
    private static final int SCREENCACHE = 8 * 1024; // Bytes
    private static final File FSCACHEDIR = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath()
            + "/Android/data/" + Map.PKG);

    public C2CCaching(Context ctxt) {
        super(createCacheLevels(ctxt));
    }

    private static Cache[] createCacheLevels(Context ctxt) {
        Cache[] cl = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        if (prefs.getBoolean(Prefs.KEY_FS_CACHING, Prefs.DEFAULT_FS_CACHING)) {
            int size = prefs.getInt(Prefs.KEY_FS_CACHING_SIZE, Prefs.DEFAULT_FS_CACHING_SIZE);
            Log.v(TAG, "fs caching on, size=" + size);
            cl = new Cache[] { new MemoryCache(SCREENCACHE),
                    new AndroidFileSystemCache(ctxt, Map.APP, FSCACHEDIR, size) };
        } else {
            Log.v(TAG, "fs caching off");
            cl = new Cache[] { new MemoryCache(SCREENCACHE) };
        }
        return cl;
    }

    public Cache[] getCacheLevels() {
        return cacheLevels;
    }
}
