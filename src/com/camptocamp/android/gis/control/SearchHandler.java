package com.camptocamp.android.gis.control;

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
import android.widget.Toast;

import com.camptocamp.android.gis.BaseMap;
import com.camptocamp.android.gis.R;

public class SearchHandler extends Activity {

    private static final String TAG = BaseMap.D + "C2CSearchHandler";
    private static final String SRC = "content://%1$s.C2CSearch/search_suggest_query/%2$s?limit=50";
    protected Search search = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");
        Intent intent = getIntent();

        // Handle search query (just send the query)
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            if (search != null) {
                final Cursor c = search.query(Uri.parse(String.format(SRC, BaseMap.PKG, query)),
                        null, null, null, null);
                if (c != null) {
                    if (c.getCount() > 0) {
                        if (c.getCount() == 1) {
                            c.moveToFirst();
                            showResultActivity(c.getString(3));
                            c.close();
                        } else {
                            Builder d = new Builder(SearchHandler.this);
                            d.setCursor(c, new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showResultActivity(c.getString(3));
                                    c.close();
                                }
                            }, SearchManager.SUGGEST_COLUMN_TEXT_1);
                            d.create().show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.toast_no_result,
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.toast_no_search_provider,
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Handle suggestions query (just send geo data)
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            showResultActivity(intent.getDataString());
        }
    }

    protected void showResultActivity(String jstring) {
        Intent newintent = new Intent(BaseMap.ACTION_GOTO);
        try {
            JSONObject json = new JSONObject(jstring);
            JSONArray a = json.getJSONArray("bbox");
            newintent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            newintent.putExtra(BaseMap.EXTRA_LABEL, json.getString("label"));

            // Bbox
            newintent.putExtra(BaseMap.EXTRA_MINLON, a.getDouble(0));
            newintent.putExtra(BaseMap.EXTRA_MINLAT, a.getDouble(1));
            newintent.putExtra(BaseMap.EXTRA_MAXLON, a.getDouble(2));
            newintent.putExtra(BaseMap.EXTRA_MAXLAT, a.getDouble(3));

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            startActivity(newintent);
            finish();
        }
    }
}
