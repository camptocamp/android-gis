package com.camptocamp.android.gis;

import android.app.SearchManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class SwisstopoSearch extends C2CSearch {

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        String query = uri.getLastPathSegment().toLowerCase();
        Log.v("SearchLocation", "query=" + query);
        MatrixCursor answer = new MatrixCursor(new String[] { BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA });
        answer.addRow(new String[] { "0", "Test0", "Test0" });
        answer.addRow(new String[] { "1", "Test1", "Test1" });
        answer.addRow(new String[] { "2", "Test2", "Test2" });
        return answer;
    }

}
