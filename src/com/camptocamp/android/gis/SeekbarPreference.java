package com.camptocamp.android.gis;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.StatFs;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekbarPreference extends DialogPreference implements OnSeekBarChangeListener {

    private Context mCtxt;
    private SharedPreferences mPrefs;
    private TextView mValue;
    private int mMax;

    public SeekbarPreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);
        mCtxt = ctxt;
    }

    @Override
    protected View onCreateDialogView() {
        View v = super.onCreateDialogView();

        mValue = (TextView) v.findViewById(R.id.prefs_size_txt);

        // Get available space on sd
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long freebytes = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        mMax = Math.round(freebytes / 1024 / 1024);

        // Get current pref value
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mCtxt);
        int current = Math.round(mPrefs.getInt(Prefs.KEY_FS_CACHING_SIZE,
                Prefs.DEFAULT_FS_CACHING_SIZE) / 1024 / 1024);

        // Prepare SeekBar
        mValue.setText(current + " / " + mMax + " MB");
        SeekBar sb = (SeekBar) v.findViewById(R.id.prefs_size_sb);
        sb.setMax(mMax);
        sb.setProgress(current);
        sb.setOnSeekBarChangeListener(this);

        return v;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mValue.setText(progress + " / " + mMax + " MB");

        // Save value in SharedPrefs
        // Limit is 2147483647 Bytes (2048 MBytes)
        Editor edit = mPrefs.edit();
        edit.putInt(Prefs.KEY_FS_CACHING_SIZE, progress * 1024 * 1024);
        edit.commit();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

}
