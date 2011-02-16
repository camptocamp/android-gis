package com.camptocamp.android.gis.providers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Image;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.camptocamp.android.gis.BaseMap;
import com.camptocamp.android.gis.utils.Downloader;
import com.nutiteq.components.Place;

//http://ws.geonames.org/postalCodeSearch?placename=Lausanne&maxRows=1
//http://wiki.openstreetmap.org/wiki/Geocoding
// TODO: Implements OpenGeoCoding.org

public class OsmGeocoding {

    private static final int LIM = 10;
    private static final String URL = "http://nominatim.openstreetmap.org/search?"
            + "format=json&email=aubort.jeanbaptiste@gmail.com&limit=" + LIM + "&"
            + "accept-language=fr,%20en&q=";
    private static final String URL_REVERSE = "http://nominatim.openstreetmap.org/reverse?"
            + "format=json&email=aubort.jeanbaptiste@gmail.com&limit=" + LIM + "&"
            + "accept-language=fr,%20en&";
    private String searchUrl;
    private boolean reverse;

    public OsmGeocoding(String query) {
        this(query, false);
    }

    /**
     * OsmGeocoding if reverse is true, query must be in the form:
     * lon=0.0&lat=0.0
     * 
     * @param query
     *            Streetname, country, WGS84 coordinate to search
     * @param reverse
     *            reverse resolves lat/lon coordinaite to streetname
     */
    public OsmGeocoding(String query, boolean reverse) {
        if (reverse) {
            searchUrl = URL_REVERSE + URLEncoder.encode(query);
        } else {
            searchUrl = URL + URLEncoder.encode(query);
        }
        Log.v(getClass().getName(), searchUrl);
    }

    public Place[] getPoints() {
        Place[] places = null;
        try {
            final String res = Downloader.getStringResponse(searchUrl);
            if (reverse) {
                places = new Place[0];
                final JSONObject o = new JSONObject(res);
                places[0] = new Place(o.getInt("place_id"), o.getString("display_name"), Image
                        .createImage("/images/def_kml.png"), 0, 0);
            } else {
                JSONArray json = new JSONArray(res);
                int len = json.length();
                places = new Place[len];
                for (int i = 0; i < len; i++) {
                    final JSONObject o = json.getJSONObject(i);
                    places[i] = new Place(o.getInt("place_id"), o.getString("display_name"), Image
                            .createImage("/images/def_kml.png"), o.getDouble("lon"), o
                            .getDouble("lat"));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return places;
    }
}
