package com.camptocamp.android.gis;

import com.nutiteq.services.YourNavigationDirections;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

// TODO: Implement http://www.cyclestreets.net/api/

public class C2CDirections extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directions);

        findViewById(R.id.go).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(C2CDirections.this, Map.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.setAction(Map.ACTION_ROUTE);
                i.putExtra(Map.EXTRA_MINLON, 8.225458);
                i.putExtra(Map.EXTRA_MINLAT, 46.858423);
                i.putExtra(Map.EXTRA_MAXLON, 6.633537);
                i.putExtra(Map.EXTRA_MAXLAT, 46.519463);
                i.putExtra(Map.EXTRA_TYPE, YourNavigationDirections.MOVE_METHOD_CAR);
                startActivity(i);
                finish();
            }
        });

    }

}
