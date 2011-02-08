package com.camptocamp.android.gis.providers;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.camptocamp.android.gis.BaseMap;
import com.camptocamp.android.gis.C2CSearch;
import com.camptocamp.android.gis.R;

public class OpenaddressesSearch extends C2CSearch {

    private static final String TAG = BaseMap.D + "OpenaddressesSearch";

    public OpenaddressesSearch() {
        super();
    }

    public OpenaddressesSearch(final Context ctxt) {
        super(ctxt);
    }

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
            Context ctxt = (context == null) ? getContext() : context;
            String url = String.format(ctxt.getString(R.string.osm_search_url), query);
            Log.v(TAG, "url=" + url);
            byte[] data = getData(url);
            // Parse JSON
            try {
                JSONObject json = new JSONObject(new String(data));
                JSONArray results = json.getJSONArray("features");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject elem = results.getJSONObject(i);

                    // Base data
                    String id = elem.getString("id");
                    JSONObject prop = elem.getJSONObject("properties");
                    String label = prop.getString("street") + ", " + prop.getString("city") + " ("
                            + prop.getString("country") + ")";
                    int icon = R.drawable.search;
                    JSONObject obj = new JSONObject();
                    obj.put("label", label);
                    obj.put("bbox", elem.getJSONArray("bbox"));

                    answer.addRow(new Object[] { id, label, icon, obj.toString() });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.v(TAG, "Invalid query");
        }
        return answer;
    }
}
