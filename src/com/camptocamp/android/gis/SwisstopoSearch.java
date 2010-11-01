package com.camptocamp.android.gis;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.SearchManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.nutiteq.utils.IOUtils;

public class SwisstopoSearch extends C2CSearch {

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        MatrixCursor answer = new MatrixCursor(new String[] { BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA });
        String query = URLEncoder.encode(uri.getLastPathSegment().toLowerCase());

        if (!SearchManager.SUGGEST_URI_PATH_QUERY.equals(query)) {
            // Get search results as JSON
            String url = String.format(getContext().getString(R.string.url_search), System
                    .currentTimeMillis(), query);

            DefaultHttpClient client = new DefaultHttpClient();
            InputStream is = null;
            Writer w = new StringWriter();
            try {
                HttpGet method = new HttpGet(new URI(url));
                HttpResponse response = client.execute(method);
                is = response.getEntity().getContent();
                Reader r = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8 * 1024);
                char[] bb = new char[1024];
                int n;
                while ((n = r.read(bb)) != -1) {
                    w.write(bb, 0, n);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeStream(is);
            }

            // Parse JSON
            try {
                JSONObject json = new JSONObject(w.toString());
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
            }
        }
        return answer;
    }
}
