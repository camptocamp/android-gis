package com.camptocamp.android.gis.control;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.camptocamp.android.gis.BaseMap;
import com.camptocamp.android.gis.R;
import com.camptocamp.android.utils.Utils;


// TODO: Refactor and remove ACTION_GOTO from here (in showResultActivity())

public abstract class SearchHandler extends ListActivity {

    // private static final String TAG = BaseMap.D + "SearchHandler";
    private static final String SRC = "content://%1$s.control.SearchProvider"
            + "/search_suggest_query/%2$s";
    protected ProgressDialog mDialog;

    public static final String JSON_BBOX = "bbox";
    public static final String JSON_LABEL = "label";
    public static final String JSON_ID = "label";

    protected SearchProvider mSearch = null;
    protected String mQuery = "";

    abstract protected void showResult(final Cursor c);

    abstract protected void showResultActivity(String dataString);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        // Query the provider
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mQuery = intent.getStringExtra(SearchManager.QUERY);
            if (mSearch != null) {
                new QueryTask(SearchHandler.this).execute(mQuery);
            }
            else {
                Toast.makeText(getApplicationContext(), R.string.toast_no_search_provider,
                        Toast.LENGTH_SHORT).show();
            }
        }
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            showResultActivity(intent.getDataString());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mDialog = null;
        super.onConfigurationChanged(newConfig);
    }

    /**
     * The text display in the waiting dialogue
     * 
     * @return the text resource id.
     */
    protected int getWaitingText() {
        return R.string.dialog_searching;
    }

    // TODO: Use android.content.CursorLoader() instead
    protected class QueryTask extends AsyncTask<String, Void, Cursor> {

        private WeakReference<Activity> mActivity;

        public QueryTask(Activity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        protected void onPreExecute() {
            final Activity a = mActivity.get();
            if (a != null) {
                mDialog = ProgressDialog.show(a, "", getString(getWaitingText()));
            }
        }

        @Override
        protected Cursor doInBackground(String... query) {
            return mSearch.query(Uri.parse(String.format(SRC, BaseMap.PKG, query[0])), null, null,
                    null, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            showResult(cursor);
            mActivity = null;
            Utils.dismissDialog(mDialog);
        }

        @Override
        protected void onCancelled() {
            mActivity = null;
            Utils.dismissDialog(mDialog);
        }

    }
}
