package com.camptocamp.android.utils;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import android.util.Log;

import com.nutiteq.utils.IOUtils;

import de.georepublic.android.gis.ui.Main;

public class Downloader {

    public static final String TAG = Main.D + "Downloader";
    public static final String USERAGENT_KEY = "User-Agent";
    public static final String USERAGENT = "Android GIS (http://camptocamp.com)";

    public static String getStringResponse(String url) {
        HttpConnection conn = null;
        InputStream is = null;
        final StringBuffer sb = new StringBuffer();
        try {
            conn = (HttpConnection) Connector.open(url, Connector.READ);
            conn.setRequestProperty(USERAGENT_KEY, USERAGENT);
            is = conn.openInputStream();
            int chr;
            while ((chr = is.read()) != -1) {
                sb.append((char) chr);
            }
        }
        catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        finally {
            IOUtils.closeStream(is);
            IOUtils.closeConnection(conn);
        }
        return sb.toString();
    }
}
