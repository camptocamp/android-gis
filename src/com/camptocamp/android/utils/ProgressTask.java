package com.camptocamp.android.utils;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public abstract class ProgressTask<Params, Progress, Result> extends
        AsyncTask<Params, Progress, Result> {

    // Activity that launch the task
    protected final WeakReference<Activity> mActivity;
    // Used progress dialog
    protected ProgressDialog mDialog;
    // Message do display
    protected final String mMessage;

    public ProgressTask(final Activity activity, final String message) {
        mActivity = new WeakReference<Activity>(activity);
        mMessage = message;
    }

    @Override
    protected void onPreExecute() {
        final Activity activity = mActivity.get();
        if (activity != null) {
            // Show modal dialog
            mDialog = new ProgressDialog(activity);
            mDialog.setMessage(mMessage);
            mDialog.show();
        }
    }

    @Override
    protected void onPostExecute(final Result result) {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void onCancelled() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
