package com.camptocamp.android.gis;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.nutiteq.cache.AndroidFileSystemCache;
import com.nutiteq.cache.Cache;
import com.nutiteq.cache.CachingChain;
import com.nutiteq.cache.MemoryCache;

public class C2CCaching extends CachingChain {

    // An image is ~25kB => 1MB = 40 cached images
    private static final int SCREENCACHE = 1024 * 1024; // Bytes
    private static final int FSCACHESIZE = 32 * 1024 * 1024; // Bytes
    private static final File FSCACHEDIR = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath()
            + "/Android/data/" + Map.PKG);

    public C2CCaching(Context ctxt) {
        super(new Cache[] { new MemoryCache(SCREENCACHE),
                new AndroidFileSystemCache(ctxt, Map.APP, FSCACHEDIR, FSCACHESIZE) });
    }

    public byte[] get(final String cacheKey) {
        if (cacheLevels[0].contains(cacheKey)) {
            Log.v("Caching", "cache from memory cache=" + cacheLevels[0].get(cacheKey));
            return cacheLevels[0].get(cacheKey);
        } else if (cacheLevels[1].contains(cacheKey)) {
            Log.v("Caching", "cache from fs");
            return cacheLevels[1].get(cacheKey);
        }
        return null;
    }
}
