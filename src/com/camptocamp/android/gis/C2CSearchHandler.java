package com.camptocamp.android.gis;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class C2CSearchHandler extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("C2CSearchHandler", "onCreate()");
        Intent intent = getIntent();
        Intent newintent = new Intent(C2CSearchHandler.this, Map.class);

        // Handle search query (just send the query)
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // String query = intent.getStringExtra(SearchManager.QUERY);
            // TODO: DO SEARCH USING PROVIDER, SHOW LIST AND USE BELOW TO GO TO
            // MAP APP
        }
        // Handle suggestions query (just send geo data)
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            try {
                JSONObject json = new JSONObject(intent.getDataString());
                JSONArray a = json.getJSONArray("bbox");
                newintent.setAction(Map.ACTION_GOTO);
                newintent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                newintent.putExtra(Map.EXTRA_LABEL, json.getString("label"));

                // Bbox
                SwisstopoMap m = new SwisstopoMap("", "", Map.ZOOM);
                newintent.putExtra(Map.EXTRA_MINX, (int) Math.round(m.CHxtoPIX(a.getDouble(0))));
                newintent.putExtra(Map.EXTRA_MINY, (int) Math.round(m.CHytoPIX(a.getDouble(1))));
                newintent.putExtra(Map.EXTRA_MAXX, (int) Math.round(m.CHxtoPIX(a.getDouble(2))));
                newintent.putExtra(Map.EXTRA_MAXY, (int) Math.round(m.CHytoPIX(a.getDouble(3))));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        startActivity(newintent);
        finish();
    }
}
