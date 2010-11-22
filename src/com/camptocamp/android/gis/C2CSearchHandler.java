package com.camptocamp.android.gis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.SearchManager;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class C2CSearchHandler extends Activity {

    private static final String TAG = Map.D + "C2CSearchHandler";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");
        Intent intent = getIntent();

        // Handle search query (just send the query)
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            C2CSearch search = new SwisstopoSearch(getApplicationContext());
            // C2CSearch search = new
            // OpenaddressesSearch(getApplicationContext());

            final Cursor c = search.query(Uri.parse("content://" + Map.PKG
                    + ".C2CSearch/search_suggest_query/" + query + "?limit=50"), null, null, null,
                    null);
            if (c != null && c.getCount() > 0) {
                Builder d = new Builder(C2CSearchHandler.this);
                d.setCursor(c, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showResultActivity(c.getString(3));
                    }
                }, SearchManager.SUGGEST_COLUMN_TEXT_1);
                d.create().show();
            }
        }
        // Handle suggestions query (just send geo data)
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            showResultActivity(intent.getDataString());
        }
    }

    private void showResultActivity(String jstring) {
        Intent newintent = new Intent(C2CSearchHandler.this, Map.class);
        newintent.setAction(Map.ACTION_GOTO);
        try {
            JSONObject json = new JSONObject(jstring);
            JSONArray a = json.getJSONArray("bbox");
            newintent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            newintent.putExtra(Map.EXTRA_LABEL, json.getString("label"));

            // Bbox
            newintent.putExtra(Map.EXTRA_MINX, a.getDouble(0));
            newintent.putExtra(Map.EXTRA_MINY, a.getDouble(1));
            newintent.putExtra(Map.EXTRA_MAXX, a.getDouble(2));
            newintent.putExtra(Map.EXTRA_MAXY, a.getDouble(3));

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            startActivity(newintent);
            finish();
        }
    }
}
