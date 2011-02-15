package com.camptocamp.android.utils;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import android.util.Log;

public class Downloader {
	public static final String USERAGENT_KEY = "User-Agent";
	public static final String USERAGENT = "Android GIS (http://camptocamp.com)";

    public static String getStringResponse(String url) {
        HttpConnection conn = null;
        InputStream is = null;
        StringBuffer sb = new StringBuffer();
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
            Log.e(Downloader.class.getName(), e.getClass().getName() + " - " + e.getMessage());
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
            catch (Exception e) {
                Log.e(Downloader.class.getName(), e.getClass().getName() + " - " + e.getMessage());
            }
        }
        return sb.toString();
    }
}
