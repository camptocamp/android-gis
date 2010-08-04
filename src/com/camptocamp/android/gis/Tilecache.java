package com.camptocamp.android.gis;

import android.util.Log;

import com.nutiteq.maps.GeoMap;
import com.nutiteq.maps.UnstreamedMap;

public class Tilecache extends CH1903 implements GeoMap, UnstreamedMap {

    private String baseUrl;
    private String format;

    public Tilecache(String baseUrl, String format, int tileSize, int minZoom, int maxZoom, String copyright) {
        super(copyright, tileSize, minZoom, maxZoom);
        this.baseUrl = baseUrl;
        this.format = format;
    }

    public String buildPath(int mapX, int mapY, int zoom) {
        Log.v("Tilecache", "x=" + mapX + ", y=" + mapY);
        return String.format(baseUrl, zoom, (int) (mapX / 1000000), (int) (mapX / 1000) % 1000, (int) mapX % 1000,
                (int) (mapY / 1000000), (int) (mapY / 1000) % 1000, (int) mapY % 1000, format);
    }
}
