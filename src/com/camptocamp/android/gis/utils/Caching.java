package com.camptocamp.android.gis.utils;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Debug;
import android.os.Environment;
import android.os.Debug.MemoryInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.camptocamp.android.gis.BaseMap;
import com.nutiteq.cache.AndroidFileSystemCache;
import com.nutiteq.cache.Cache;
import com.nutiteq.cache.CachingChain;
import com.nutiteq.cache.MemoryCache;

public class Caching extends CachingChain {

    private static final String TAG = BaseMap.D + "C2CCaching";
    protected static final int MEMORYCACHE_LENGTH = 50; // # of elements
    // protected static final int MEMORYCACHE_SIZE = 480 * 1024; // Size in Bytes
    protected static final File FSCACHEDIR = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath()
            + "/Android/data/" + BaseMap.PKG + "/cache");

    public Caching(final Context ctxt) {
        super(createCacheLevels(ctxt));
    }

    private static Cache[] createCacheLevels(final Context context) {

        MemoryInfo mem = new MemoryInfo();
        Debug.getMemoryInfo(mem);
        // final int memorycache_size = mem.dalvikPrivateDirty * 1024;
        final int memorycache_size = 256 * 1024;

        android.util.Log.e(TAG, "---DEBUG---");
        android.util.Log.e(TAG, "dalvikPss=" + mem.dalvikPss);
        android.util.Log.e(TAG, "dalvikPrivateDirty=" + mem.dalvikPrivateDirty);
        android.util.Log.e(TAG, "dalvikSharedDirty=" + mem.dalvikSharedDirty);
        android.util.Log.e(TAG, "---");
        // android.util.Log.e(TAG, "getTotalPss=" + mem.getTotalPss());
        // android.util.Log.e(TAG, "getTotalPrivateDirty=" + mem.getTotalPrivateDirty());
        // android.util.Log.e(TAG, "getTotgetTotalSharedDirtyalPss=" + mem.getTotalSharedDirty());
        // android.util.Log.e(TAG, "---");

        Cache[] cl = null;

        // MemoryCache + FsCache
        WeakReference<Context> weakCtxt = new WeakReference<Context>(context);
        Context ctxt = weakCtxt.get();
        if (ctxt != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
            if (prefs.getBoolean(Prefs.KEY_FS_CACHING, Prefs.DEFAULT_FS_CACHING)) {
                int size = prefs.getInt(Prefs.KEY_FS_CACHING_SIZE, Prefs.DEFAULT_FS_CACHING_SIZE);
                Log.v(TAG, "fs caching on, size=" + size);
                cl = new Cache[] { new MemoryCache(MEMORYCACHE_LENGTH, memorycache_size),
                        new AndroidFileSystemCache(ctxt, BaseMap.APP, FSCACHEDIR, size) };
            }
        }

        // MemoryCache only
        if (cl == null) {
            Log.v(TAG, "fs caching off");
            cl = new Cache[] { new MemoryCache(MEMORYCACHE_LENGTH, memorycache_size) };
        }
        return cl;
    }
}
