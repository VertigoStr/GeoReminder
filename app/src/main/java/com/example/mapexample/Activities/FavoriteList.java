package com.example.mapexample.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.example.mapexample.DataBase.Controller;
import com.example.mapexample.DataBase.DataBaseHelper;
import com.example.mapexample.DataBase.Elements;
import com.example.mapexample.R;

public class FavoriteList extends Activity {
    private final static String tag = "favorite_list";
    boolean isGPSEnabled = false;
    protected LocationManager locationManager;
    final Context context = this;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag, "onCreate");
        setContentView(R.layout.favorite_list_view);
        DataBaseHelper dbhelper = new DataBaseHelper(getBaseContext());
        SQLiteDatabase sqliteDB = dbhelper.getReadableDatabase();
        final String[] from = { Elements.Element.ElementColumns.Name, BaseColumns._ID };
        final Cursor c = sqliteDB.query(Elements.Element.TABLE_NAME, null, null, null, null, null,
               Elements.Element.DEFAULT_SORT);
        final int[] to = new int[] { R.id.text1 };
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.list,
                c, from, to);
        final ListView lv = (ListView) findViewById(R.id.listView1);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Log.d(tag, "item_click");
                locationManager = (LocationManager) getApplicationContext()
                        .getSystemService(LOCATION_SERVICE);
                TelephonyManager telephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                Boolean isEnabled;
                if(telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED){
                    isEnabled = true;
                }else{
                    isEnabled = false;
                }
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (isEnabled && isGPSEnabled) {
                    DataBaseHelper dbhelper = new DataBaseHelper(getBaseContext());
                    SQLiteDatabase sqliteDB = dbhelper.getReadableDatabase();
                    Cursor c = sqliteDB.query(Elements.Element.TABLE_NAME, null, BaseColumns._ID + "=" + id, null, null, null,
                            null);
                    String stLat = "";
                    String stLon = "";
                    String eLat = "";
                    String eLon = "";
                    if (c.moveToFirst()) {
                        stLat = c.getString(c.getColumnIndex(Elements.Element.ElementColumns.startLatitude));
                        stLon = c.getString(c.getColumnIndex(Elements.Element.ElementColumns.startLongitude));
                        eLat = c.getString(c.getColumnIndex(Elements.Element.ElementColumns.endLatitude));
                        eLon = c.getString(c.getColumnIndex(Elements.Element.ElementColumns.endLongitude));
                    }
                    dbhelper.close();
                    sqliteDB.close();
                    Intent intent = new Intent();
                    intent.putExtra("DocumentInfo", new DocumentInfo(
                                    Double.parseDouble(stLat),
                                    Double.parseDouble(stLon),
                                    Double.parseDouble(eLat),
                                    Double.parseDouble(eLon),
                                    "0")
                    );
                    Log.d(tag, intent.toString());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), getResources().
                            getString(R.string.gps_problem), Toast.LENGTH_LONG).show();
                }
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long id) {
                Log.d(tag, "item_long_click");
                final CharSequence[] items = { getResources().getString(R.string.delete), getResources().getString(R.string.rename) };
                AlertDialog.Builder builder3 = new AlertDialog.Builder(FavoriteList.this);
                builder3.setTitle(getResources().getString(R.string.enter_new_name)).setItems(items,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int item) {

                                switch (item) {
                                    case 0: {
                                        DataBaseHelper dbhelper = new DataBaseHelper(getBaseContext());
                                        SQLiteDatabase sqliteDB = dbhelper.getReadableDatabase();
                                        Controller.delete(getBaseContext(), adapter.getItemId(pos));
                                        final Cursor c = sqliteDB.query(Elements.Element.TABLE_NAME, null, null, null, null, null,
                                                Elements.Element.DEFAULT_SORT);
                                        adapter.changeCursor(c);
                                        dbhelper.close();
                                        sqliteDB.close();
                                    }
                                    break;
                                    case 1: {
                                        LayoutInflater li = LayoutInflater.from(context);
                                        View promptsView = li.inflate(R.layout.enter_new_name, null);
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                context);
                                        alertDialogBuilder.setView(promptsView);
                                        final EditText userInput = (EditText) promptsView
                                                .findViewById(R.id.editTextDialogUserInput);
                                        alertDialogBuilder.setCancelable(false).setPositiveButton(getResources().getString(R.string.ok),
                                                new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {

                                                        DataBaseHelper dbhelper = new DataBaseHelper(getBaseContext());
                                                        SQLiteDatabase sqliteDB = dbhelper.getReadableDatabase();
                                                        Controller.update(getBaseContext(), userInput
                                                                .getText().toString(), adapter.getItemId(pos));
                                                        final Cursor c = sqliteDB.query(Elements.Element.TABLE_NAME, null, null, null, null, null,
                                                                Elements.Element.DEFAULT_SORT);
                                                        adapter.changeCursor(c);
                                                        dbhelper.close();
                                                        sqliteDB.close();
                                                    }
                                                }).setNegativeButton(getResources().getString(R.string.cancel),
                                                new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {

                                                        dialog.cancel();
                                                    }
                                                });
                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialog.show();
                                    }
                                    break;
                                }
                            }
                        });
                builder3.show();
                return true;
            }
        });
        dbhelper.close();
        sqliteDB.close();
    }
}
