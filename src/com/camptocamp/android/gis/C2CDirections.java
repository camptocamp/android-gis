package com.camptocamp.android.gis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nutiteq.components.WgsPoint;
import com.nutiteq.services.YourNavigationDirections;

// TODO: Implement http://www.cyclestreets.net/api/ ?

public class C2CDirections extends Activity {

    // private static final String TAG = Map.D + "C2CDirections";
    protected static final int PICK = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                // Parse fields
                String start = ((TextView) findViewById(R.id.start)).getText().toString();
                String end = ((TextView) findViewById(R.id.end)).getText().toString();
                WgsPoint p1 = null;
                WgsPoint p2 = null;
                try {
                    p1 = WgsPoint.parsePoint(0, start, ",");
                } catch (Exception e) {
                    // Reverse geocoding
                }
                try {
                    p2 = WgsPoint.parsePoint(0, end, ",");
                } catch (Exception e) {
                    // Reverse geocoding
                }

                // Start routing request
                if (p1 != null && p2 != null) {
                    Intent i = new Intent(C2CDirections.this, Map.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    i.setAction(Map.ACTION_ROUTE);
                    i.putExtra(Map.EXTRA_MINLON, p1.getLon());
                    i.putExtra(Map.EXTRA_MINLAT, p1.getLat());
                    i.putExtra(Map.EXTRA_MAXLON, p2.getLon());
                    i.putExtra(Map.EXTRA_MAXLAT, p2.getLat());
                    i.putExtra(Map.EXTRA_TYPE, YourNavigationDirections.MOVE_METHOD_CAR);
                    startActivity(i);
                } else {
                    Toast.makeText(C2CDirections.this, "FIXME: Invalid route!", Toast.LENGTH_SHORT)
                            .show();
                }
                finish();
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        // GPS coordinate from map
        if (Map.ACTION_PICK.equals(intent.getAction())) {
            String coord = intent.getStringExtra(Map.EXTRA_COORD);
            int field = intent.getIntExtra(Map.EXTRA_FIELD, R.id.start);

            // Add string to field
            final EditText txt = (EditText) findViewById(field);
            txt.setText(coord);
            txt.setSelection(coord.length());
            txt.setSelected(true);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: Use Activity.createPendingResult()
        if (resultCode == RESULT_OK) {
            String addr = "";

            // Address from contact
            Uri contact = data.getData();
            Cursor c = managedQuery(contact, null, null, null, null);
            if (c.moveToFirst()) {
                addr = c
                        .getString(c
                                .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
            }

            // Add string to field
            final EditText txt = (EditText) findViewById(requestCode);
            txt.setText(addr);
            txt.setSelection(addr.length());
            txt.setSelected(true);
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
}
