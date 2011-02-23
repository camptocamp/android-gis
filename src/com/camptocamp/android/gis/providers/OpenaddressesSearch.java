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
import com.camptocamp.android.gis.control.SearchHandler;
import com.camptocamp.android.gis.control.SearchProvider;

public class OpenaddressesSearch extends SearchProvider {

    private static final String TAG = BaseMap.D + "OpenaddressesSearch";
    private static final String JSON_ID = "id";
    private static final String JSON_PROP = "properties";
    private static final String JSON_STREET = "street";
    private static final String JSON_CITY = "city";
    private static final String JSON_COUNTRY = "country";
    private static final String JSON_FEATURES = "features";
    private static final String LABEL = "%1$s, %2$s (%3$s)";
    private static final String PLUS = "+";

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        MatrixCursor answer = new MatrixCursor(new String[] { BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA });

        // Build query
        // Log.v(TAG, "query()=" + uri.toString());
        String query = URLEncoder.encode(uri.getLastPathSegment().toLowerCase());
        if (query.startsWith(PLUS)) {
            query = query.substring(1);
        }
        if (query.endsWith(PLUS)) {
            query = query.substring(0, query.length() - 1);
        }

        if (!SearchManager.SUGGEST_URI_PATH_QUERY.equals(query)) {
            // Get search results as JSON
            String url = String.format(getContext().getString(R.string.osm_search_url), query);
            byte[] data = getData(url);
            // Parse JSON
            try {
                JSONObject json = new JSONObject(new String(data));
                JSONArray results = json.getJSONArray(JSON_FEATURES);
                for (int i = 0; i < results.length(); i++) {
                    JSONObject elem = results.getJSONObject(i);

                    // Base data
                    String id = elem.getString(JSON_ID);
                    JSONObject prop = elem.getJSONObject(JSON_PROP);
                    String label = String.format(LABEL, prop.getString(JSON_STREET), prop
                            .getString(JSON_CITY), prop.getString(JSON_COUNTRY));
                    int icon = R.drawable.search;
                    JSONObject obj = new JSONObject();
                    obj.put(SearchHandler.JSON_LABEL, label);
                    obj.put(SearchHandler.JSON_BBOX, elem.getJSONArray(SearchHandler.JSON_BBOX));

                    answer.addRow(new Object[] { id, label, icon, obj.toString() });
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.v(TAG, "Invalid query");
        }
        return answer;
    }
}
