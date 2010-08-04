package com.camptocamp.android.gis;

import android.util.Log;

import com.nutiteq.maps.TMSMap;

public class Tilecache extends TMSMap {
	// extends CH1903 implements GeoMap, UnstreamedMap {

	public Tilecache(String baseUrl, int tileSize, int minZoom, int maxZoom, String format, String copyright) {
		super(baseUrl, tileSize, minZoom, maxZoom, format, copyright);
		// TODO Auto-generated constructor stub
	}

	public String buildPath(int mapX, int mapY, int zoom) {
		Log.v("Tilecache", "x=" + mapX + ", y=" + mapY);
		return "http://localhost";
	}
}
