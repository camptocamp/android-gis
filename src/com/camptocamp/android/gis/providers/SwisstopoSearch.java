package com.camptocamp.android.gis.providers;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.SearchManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.camptocamp.android.gis.BaseMap;
import com.camptocamp.android.gis.R;
import com.camptocamp.android.gis.control.SearchProvider;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.WgsPoint;

public class SwisstopoSearch extends SearchProvider {

    private static final String TAG = BaseMap.D + "SwisstopoSearch";

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        MatrixCursor answer = new MatrixCursor(new String[] { BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA });

        // Build query
        // Log.v(TAG, "query()=" + uri.toString());
        String query = URLEncoder.encode(uri.getLastPathSegment().toLowerCase());
        if (query.startsWith("+")) {
            query = query.substring(1);
        }
        if (query.endsWith("+")) {
            query = query.substring(0, query.length() - 1);
        }

        if (!SearchManager.SUGGEST_URI_PATH_QUERY.equals(query)) {
            // Get search results as JSON
            String url = String.format(getContext().getString(R.string.st_url_search), System
                    .currentTimeMillis(), query);
            byte[] data = getData(url);

            // Parse JSON
            try {
                JSONObject json = new JSONObject(new String(data));
                JSONArray results = json.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject elem = results.getJSONObject(i);

                    // Base data
                    Integer id = elem.getInt("id");
                    String label = elem.getString("label");
                    String service = elem.getString("service");

                    // Icon
                    int icon;
                    if (new String("cities").equals(service)) {
                        icon = R.drawable.building;
                    }
                    else {
                        icon = R.drawable.search;
                    }

                    // Transform bbox to WGS point
                    JSONArray bbox = elem.getJSONArray("bbox");
                    SwisstopoMap m = new SwisstopoMap("", "", SwisstopoComponent.ZOOM);
                    Log.v(TAG, "bbox=" + bbox.toString());
                    WgsPoint min = m
                            .mapPosToWgs(
                                    new MapPos((int) Math.round(m.CHxtoPIX(bbox.getDouble(0))),
                                            (int) Math.round(m.CHytoPIX(bbox.getDouble(1))),
                                            SwisstopoComponent.ZOOM)).toWgsPoint();
                    WgsPoint max = m
                            .mapPosToWgs(
                                    new MapPos((int) Math.round(m.CHxtoPIX(bbox.getDouble(2))),
                                            (int) Math.round(m.CHytoPIX(bbox.getDouble(3))),
                                            SwisstopoComponent.ZOOM)).toWgsPoint();
                    bbox = new JSONArray("[" + min.getLon() + ", " + min.getLat() + ", "
                            + max.getLon() + ", " + max.getLat() + "]");
                    elem.put("bbox", bbox);

                    answer.addRow(new Object[] { id, label, icon, elem.toString() });
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.v(TAG, "Invalid query=" + query);
        }
        return answer;
    }
}
