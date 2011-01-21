package com.camptocamp.android.gis;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.os.Environment;

// http://gpslogger.sourceforge.net/

public abstract class C2CExportTrace {

    // private static final String TAG = Map.D + "C2CExportTrace";
    protected static final String PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath()
            + "/Android/data/" + Map.PKG + "/traces/";
    protected static final String DF = "yyyy-MM-dd-HHmmss";
    protected String name = "";

    public C2CExportTrace() {
        new File(PATH).mkdirs();
        final SimpleDateFormat sdf = new SimpleDateFormat(DF);
        name = sdf.format(new Date(System.currentTimeMillis()));
    }

    abstract public boolean export(List<C2CLine> trace);
}
