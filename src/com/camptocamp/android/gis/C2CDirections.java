package com.camptocamp.android.gis;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.camptocamp.android.gis.providers.OsmGeocoding;
import com.nutiteq.components.Place;
import com.nutiteq.components.WgsPoint;
import com.nutiteq.services.YourNavigationDirections;

// TODO: Implement http://www.cyclestreets.net/api/

public class C2CDirections extends Activity {

    private static final String TAG = Map.D + "C2CDirections";
    protected static final int PICK = 0;

    private final List<WgsPoint> pts = new ArrayList<WgsPoint>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.directions);

        findViewById(R.id.start_choice).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseMethod(R.id.start);
            }
        });

        findViewById(R.id.end_choice).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseMethod(R.id.end);
            }
        });

        findViewById(R.id.go).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getDirections();
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        // GPS coordinate from map
        if (Map.ACTION_PICK.equals(intent.getAction())) {
            new SearchTask(intent.getIntExtra(Map.EXTRA_FIELD, R.id.start), intent
                    .getStringExtra(Map.EXTRA_COORD)).execute();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Address from contact
            String addr = "";
            Cursor c = managedQuery(data.getData(), null, null, null, null);
            if (c.moveToFirst()) {
                addr = c
                        .getString(c
                                .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
            }
            new SearchTask(requestCode, addr).execute();
        }
    }

    private void chooseMethod(final int field) {
        final CharSequence[] items = { "Contact", "Point on map" };
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Choose point");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                case 0:
                    // List contact with geographic information
                    Intent i1 = new Intent(Intent.ACTION_PICK);
                    i1.setType(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_TYPE);
                    startActivityForResult(i1, field);
                    break;
                case 1:
                    // Show map and start/end marker
                    Intent i2 = new Intent(C2CDirections.this, Map.class);
                    i2.setAction(Map.ACTION_PICK);
                    i2.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    i2.putExtra(Map.EXTRA_FIELD, field);
                    startActivityForResult(i2, 0);
                    break;
                default:
                }
            }
        });
        dialog.show();
    }

    private void getDirections() {
        // Start routing request
        if (pts.size() == 2) {
            final WgsPoint p1 = pts.get(0);
            final WgsPoint p2 = pts.get(1);
            Intent i = new Intent(C2CDirections.this, Map.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            i.setAction(Map.ACTION_ROUTE);
            i.putExtra(Map.EXTRA_MINLON, p1.getLon());
            i.putExtra(Map.EXTRA_MINLAT, p1.getLat());
            i.putExtra(Map.EXTRA_MAXLON, p2.getLon());
            i.putExtra(Map.EXTRA_MAXLAT, p2.getLat());
            i.putExtra(Map.EXTRA_TYPE, YourNavigationDirections.MOVE_METHOD_CAR);
            startActivity(i);
            finish();
        } else {
            // FIXME: change the color of the field bg to red
            Toast.makeText(C2CDirections.this, "FIXME: Invalid route", Toast.LENGTH_SHORT).show();
        }
    }

    // TODO: Being static
    private class SearchTask extends AsyncTask<Void, Void, WgsPoint> {

        private String pt;
        private Place[] places = null;
        private int field;

        public SearchTask(int field, String pt) {
            this.field = field;
            this.pt = pt;
        }

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected WgsPoint doInBackground(Void... unused) {
            try {
                return WgsPoint.parsePoint(0, pt, ",");
            } catch (Exception e) {
                places = new OsmGeocoding(pt).getPoints();
            }
            return null;
        }

        @Override
        protected void onPostExecute(WgsPoint result) {
            // Add point
            if (result != null) {
                Log.i(TAG, "got a point as lon,lat string (from map obviously)");
                //FIXME: Reverse geocode
                addToField(field, result.getLon() + "," + result.getLat(), result);

            } else {
                if (places.length == 1) {
                    Log.i(TAG, "only one suggestion");
                    addToField(field, places[0]);

                } else if (places.length > 1) {
                    Log.i(TAG, "multiple suggestions");
                    final CharSequence[] items = toArray(places);
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(C2CDirections.this);
                    dialog.setTitle("FIXME: From/To:");
                    dialog.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addToField(field, places[which]);
                        }
                    });
                    dialog.setNegativeButton("Cancel", null);
                    dialog.show();
                } else {
                    Log.i(TAG, "no suggestion");
                    Toast
                            .makeText(C2CDirections.this, "FIXME: No suggestions",
                                    Toast.LENGTH_SHORT).show();
                }

            }
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private void addToField(int field, Place pl) {
        addToField(field, pl.getName(), pl.getWgs());
    }

    private void addToField(int field, String addr, WgsPoint pt) {
        if (pt != null) {
            pts.add(pt);
        }
        final EditText txt = (EditText) findViewById(field);
        txt.setText(addr);
        txt.setSelection(addr.length());
        txt.setSelected(true);
    }

    private CharSequence[] toArray(Place[] places) {
        if (places != null) {
            int len = places.length;
            if (len > 0) {
                CharSequence[] items = new CharSequence[len];
                for (int i = 0; i < len; i++) {
                    if (places[i] != null && places[i].getName() != null) {
                        items[i] = places[i].getName();
                    } else {
                        items[i] = "";
                    }
                }
                return items;
            }
        }
        return null;
    }
}
