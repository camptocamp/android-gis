package com.camptocamp.android.gis;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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

import com.nutiteq.utils.IOUtils;

public class SwisstopoSearch extends C2CSearch {

    private static final String TAG = Map.D + "SwisstopoSearch";

    public SwisstopoSearch() {
        super();
    }

    public SwisstopoSearch(final Context ctxt) {
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
            String url = String.format(ctxt.getString(R.string.url_search), System
                    .currentTimeMillis(), query);

            HttpEntity entity = null;
            InputStream is = null;
            Writer w = new StringWriter();
            HttpGet method = null;
            byte[] data;
            try {
                DefaultHttpClient client = new DefaultHttpClient();
                method = new HttpGet(new URI(url));
                HttpResponse response = client.execute(method);
                entity = response.getEntity();
                is = entity.getContent();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                data = IOUtils.readFully(is);
                if (method != null) {
                    method.abort();
                }
                if (entity != null) {
                    try {
                        entity.consumeContent();
                    } catch (IOException e) {
                    }
                }
                IOUtils.closeStream(is);
            }

            // Parse JSON
            try {
                JSONObject json = new JSONObject(new String(data));
                JSONArray results = json.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject elem = results.getJSONObject(i);

                    // Base data
                    String id = elem.getString("id");
                    String label = elem.getString("label");
                    String service = elem.getString("service");

                    // Icon
                    int icon;
                    if (new String("cities").equals(service)) {
                        icon = R.drawable.building;
                    } else {
                        icon = R.drawable.search;
                    }

                    answer.addRow(new Object[] { id, label, icon, elem.toString() });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                try {
                    w.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.v(TAG, "Invalid query");
        }
        return answer;
    }
}
