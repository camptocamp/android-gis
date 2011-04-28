package com.camptocamp.android.utils;

import android.app.ProgressDialog;

public class Utils {

    public static void dismissDialog(ProgressDialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

}
