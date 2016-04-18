package com.example.mapexample.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mapexample.DBHelper.Routes;
import com.example.mapexample.R;
import com.example.mapexample.Utils.Param;

import java.util.ArrayList;

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

        ArrayList<ArrayList<String>> list = new Routes(getBaseContext()).select(Routes.Name + ", " + Routes.id, null, null, Routes.id, "asc");
        ArrayList<Param> vals = new ArrayList<Param>();
        for(ArrayList<String> c : list) {
            vals.add(new Param(Integer.parseInt(c.get(1)), c.get(0)));
        }

        final ArrayAdapter<Param> adapter = new ArrayAdapter<Param>(this,
                android.R.layout.simple_list_item_1, vals);

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
                Boolean isEnabled = telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED;
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (isEnabled && isGPSEnabled) {
                    ArrayList<ArrayList<String>> c = new Routes(getBaseContext()).select(null, Routes.id, id + "", Routes.id, "asc");
                    Intent intent = new Intent();
                    intent.putExtra("DocumentInfo", new DocumentInfo(
                                    Double.parseDouble(c.get(0).get(2)),
                                    Double.parseDouble(c.get(0).get(3)),
                                    Double.parseDouble(c.get(0).get(4)),
                                    Double.parseDouble(c.get(0).get(5)),
                                    "0")
                    );
                    Log.d(tag, intent.toString());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().
                            getString(R.string.gps_problem), Toast.LENGTH_LONG).show();
                }
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long id) {
                Log.d(tag, "item_long_click");
                final CharSequence[] items = {getResources().getString(R.string.delete), getResources().getString(R.string.rename)};
                AlertDialog.Builder builder3 = new AlertDialog.Builder(FavoriteList.this);
                builder3.setTitle(getResources().getString(R.string.enter_new_name)).setItems(items,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int item) {

                                switch (item) {
                                    case 0: {
                                        Routes route = new Routes(getBaseContext());
                                        route.delete(Routes.id, adapter.getItem(pos).getId() + "");
                                        adapter.remove(adapter.getItem(pos));
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
                                                        Log.d("update", adapter.getItemId(pos) + "");
                                                        Routes route = new Routes(getBaseContext());
                                                        route.update(
                                                                Routes.Name,
                                                                userInput.getText().toString(),
                                                                Routes.id,
                                                                adapter.getItem(pos).getId() + ""
                                                        );
                                                        ArrayList<ArrayList<String>> list = new Routes(getBaseContext()).select(Routes.Name + ", " + Routes.id, null, null, Routes.id, "asc");
                                                        ArrayList<Param> vals = new ArrayList<Param>();
                                                        for(ArrayList<String> c : list) {
                                                            vals.add(new Param(Integer.parseInt(c.get(1)), c.get(0)));
                                                        }
                                                        adapter.clear();
                                                        adapter.addAll(vals);
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
    }
}
