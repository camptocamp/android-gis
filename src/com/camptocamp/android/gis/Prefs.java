package com.camptocamp.android.gis;

import java.io.File;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

import com.nutiteq.cache.CacheIndexDatabaseHelper;

public class Prefs extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private static final String TAG = Map.D + "Prefs";
    private PreferenceScreen ps;

    public final static String KEY_FS_CACHING = "fscaching";
    public final static boolean DEFAULT_FS_CACHING = false;

    public final static String KEY_FS_CACHING_SIZE = "fscachingsize";
    public final static int DEFAULT_FS_CACHING_SIZE = 0; // Bytes

    public final static String KEY_FS_CACHING_REMOVE = "fscachingremove";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        ps = getPreferenceScreen();
        ps.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // Remove fs cache click listener
        ((Preference) ps.findPreference(KEY_FS_CACHING_REMOVE))
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        removeFsCache();
                        return true;
                    }
                });

        // Check sdcard availability
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            ((Preference) ps.findPreference(KEY_FS_CACHING)).setEnabled(false);
            ((Preference) ps.findPreference(KEY_FS_CACHING_REMOVE)).setEnabled(false);
            ((Preference) ps.findPreference(KEY_FS_CACHING_SIZE)).setEnabled(false);
        }

        // Summaries
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long avail = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        long total = Math.round(avail / 1024 / 1024);
        long current = Math.round(ps.getSharedPreferences().getLong(KEY_FS_CACHING_SIZE,
                DEFAULT_FS_CACHING_SIZE) / 1024 / 1024);
        ((Preference) ps.findPreference(KEY_FS_CACHING_SIZE)).setSummary(current + " / " + total
                + " MB");
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        // if (KEY_FS_CACHING.equals(key)) {
        // }
    }

    private void removeFsCache() {
        final Context ctxt = Prefs.this;
        final AlertDialog.Builder dialog = new AlertDialog.Builder(ctxt);
        dialog.setMessage(R.string.dialog_fs_cache_remove);
        dialog.setPositiveButton(R.string.btn_yes, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                new RemoveCacheTask(Prefs.this).execute();
            }
        });
        dialog.setNegativeButton(R.string.btn_no, null);
        dialog.show();
    }

    class RemoveCacheTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<Activity> mActivity;

        public RemoveCacheTask(Activity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }

        protected Void doInBackground(Void... unused) {
            final Activity a = mActivity.get();
            if (a != null) {
                CacheIndexDatabaseHelper db = new CacheIndexDatabaseHelper(a, Map.APP);
                db.open();

                // Remove files on sdcard
                final String ext = C2CCaching.FSCACHEDIR.getPath() + "/";
                Cursor c = db.database.rawQuery("select resource_path from cache_index", null);
                while (c.moveToNext()) {
                    File f = new File(ext + c.getString(0));
                    if (!f.delete()) {
                        Log.e(TAG, "error while deleting " + f.getPath());
                    }
                }
                c.close();

                // Remove Database entries
                db.database.rawQuery("delete from cache_index", null);

                db.close();
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            Toast.makeText(Prefs.this, R.string.toast_fscacheremove, Toast.LENGTH_SHORT).show();
        }

        protected void onCancelled() {
            Toast.makeText(Prefs.this, R.string.toast_fscacheremove_error, Toast.LENGTH_SHORT)
                    .show();
        }

    }

}
