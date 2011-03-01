package com.camptocamp.android.gis.control;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.camptocamp.android.gis.BaseMap;
import com.camptocamp.android.gis.R;

// TODO: Refactor and remove ACTION_GOTO from here (in showResultActivity())

public abstract class SearchHandler extends ListActivity {

    // private static final String TAG = BaseMap.D + "SearchHandler";
    private static final String SRC = "content://%1$s.control.SearchProvider"
            + "/search_suggest_query/%2$s";
    private ProgressDialog dialog;

    public static final String JSON_BBOX = "bbox";
    public static final String JSON_LABEL = "label";
    public static final String JSON_ID = "label";

    protected SearchProvider search = null;
    protected String query = "";

    abstract protected void showResult(final Cursor c);

    abstract protected void showResultActivity(String dataString);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    /**
     * The text display in the waiting dialogue
     * @return the text resource id.
     */
    protected int getWaitingText() {
        return R.string.dialog_searching;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
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
                dialog = ProgressDialog.show(a, "", getString(getWaitingText()));
            }
        }

        @Override
        protected Cursor doInBackground(String... query) {
            return search.query(Uri.parse(String.format(SRC, BaseMap.PKG, query[0])), null, null,
                    null, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            final Activity activity = mActivity.get();
            showResult(cursor);
            if (activity != null && dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
        }

    }
}
