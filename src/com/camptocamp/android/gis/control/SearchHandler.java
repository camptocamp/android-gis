package com.camptocamp.android.gis.control;

import java.lang.ref.WeakReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.camptocamp.android.gis.BaseMap;
import com.camptocamp.android.gis.R;

public abstract class SearchHandler extends ListActivity {

    private static final String TAG = BaseMap.D + "SearchHandler";
    private static final String SRC = "content://%1$s.C2CSearch/search_suggest_query/%2$s?limit=50";
    private ProgressDialog dialog;

    public static final String JSON_BBOX = "bbox";
    public static final String JSON_LABEL = "label";
    public static final String JSON_ID = "label";

    protected Search search = null; // abstract ?
    protected String query = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");
        Intent intent = getIntent();

        // Handle search query (just send the query)
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            if (search != null) {
                new QueryTask(SearchHandler.this).execute(query);
            }
            else {
                Toast.makeText(getApplicationContext(), R.string.toast_no_search_provider,
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Handle suggestions query (just send geo data)
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            showResultActivity(intent.getDataString());
        }
    }

    protected void showResult(final Cursor c) {
        if (c != null) {
            if (c.getCount() > 0) {
                if (c.getCount() == 1) {
                    c.moveToFirst();
                    showResultActivity(c.getString(3));
                    c.close();
                }
                else {
                    Builder alertDialog = new Builder(SearchHandler.this);
                    alertDialog.setTitle(R.string.dialog_search_results);
                    alertDialog.setCursor(c, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showResultActivity(c.getString(3));
                            c.close();
                        }
                    }, SearchManager.SUGGEST_COLUMN_TEXT_1);
                    alertDialog.create().show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), R.string.toast_no_result,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    protected void showResultActivity(String jstring) {
        Intent newintent = new Intent(BaseMap.ACTION_GOTO);
        try {
            JSONObject json = new JSONObject(jstring);
            JSONArray a = json.getJSONArray(JSON_BBOX);
            newintent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            newintent.putExtra(BaseMap.EXTRA_LABEL, json.getString(JSON_LABEL));

            // Bbox
            newintent.putExtra(BaseMap.EXTRA_MINLON, a.getDouble(0));
            newintent.putExtra(BaseMap.EXTRA_MINLAT, a.getDouble(1));
            newintent.putExtra(BaseMap.EXTRA_MAXLON, a.getDouble(2));
            newintent.putExtra(BaseMap.EXTRA_MAXLAT, a.getDouble(3));

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            startActivity(newintent);
            finish();
        }
    }

    private class QueryTask extends AsyncTask<String, Void, Cursor> {

        private WeakReference<Activity> mActivity;

        public QueryTask(Activity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        protected void onPreExecute() {
            final Activity a = mActivity.get();
            if (a != null) {
                dialog = ProgressDialog.show(a, "", "FIXME: Searching");
            }
        }

        @Override
        protected Cursor doInBackground(String... query) {
            return search.query(Uri.parse(String.format(SRC, BaseMap.PKG, query[0])), null, null,
                    null, null);
        }

        @Override
        protected void onPostExecute(Cursor c) {
            final Activity a = mActivity.get();
            if (a != null) {
                dialog.dismiss();
            }
            showResult(c);
        }

    }
}
