package com.camptocamp.android.gis;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.os.Environment;

// http://gpslogger.sourceforge.net/

public abstract class C2CExportTrace {

    // private static final String TAG = Map.D + "C2CExportTrace";
    private static final String DF = "yyyy-MM-dd-HHmmss";
    protected final static String UTF8 = "utf-8";
    protected static final String PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath()
            + "/Android/data/" + Map.PKG + "/traces/";
    protected String name = "";

    public C2CExportTrace() {
        new File(PATH).mkdirs();
        name = new SimpleDateFormat(DF).format(new Date(System.currentTimeMillis()));
    }

    abstract public boolean export(List<C2CLine> trace);
}
