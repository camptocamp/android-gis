package com.camptocamp.android.gis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import android.app.SearchManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.nutiteq.utils.IOUtils;

public class SwisstopoSearch extends C2CSearch {

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        MatrixCursor answer = new MatrixCursor(new String[] { BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA });
        String query = uri.getLastPathSegment().toLowerCase();
        String[] sugg = getSuggestions(query);
        int i = 0;
        for (String s : sugg) {
            answer.addRow(new String[] { "" + i, s, s });
            i++;
        }
        return answer;
    }

    private String[] getSuggestions(String query) {
        List<String> sugg = new ArrayList<String>();

        String url = String.format(getContext().getString(R.string.url_search), System
                .currentTimeMillis(), query);

        HttpConnection conn = null;
        BufferedReader in = null;
        try {
            conn = (HttpConnection) Connector.open(url, Connector.READ);
            conn.setRequestMethod(HttpConnection.GET);
            final int responseCode = conn.getResponseCode();
            if (responseCode == HttpConnection.HTTP_OK
                    || responseCode == HttpConnection.HTTP_NOT_MODIFIED) {
                String json = "";
                in = new BufferedReader(new InputStreamReader(conn.openInputStream()), 8 * 1024);
                while ((json = in.readLine()) != null) {
                    break;
                }
                Log.v("", json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeReader(in);
            IOUtils.closeConnection(conn);
        }

        return sugg.toArray(new String[sugg.size()]);
    }
}
