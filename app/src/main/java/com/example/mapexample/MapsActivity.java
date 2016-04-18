package com.example.mapexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mapexample.Activities.DocumentInfo;
import com.example.mapexample.Activities.FavoriteList;
import com.example.mapexample.Activities.Settings;
import com.example.mapexample.Alarm.AlarmService;
import com.example.mapexample.DBHelper.Routes;
import com.example.mapexample.GPS.DistanceTask;
import com.example.mapexample.GPS.DownloadTask;
import com.example.mapexample.GPS.GetLocation;
import com.example.mapexample.GPS.firstMarkerMaker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    private LatLng newLoc = null;
    private LatLng myLoc = null;
    private int radius;
    private final static String tag = "maps_activity";
    private static String lang = null;
    private SharedPreferences prefs;
    private GoToDestination task;
    private static boolean isActive = false;
    private boolean flagButton = true;
    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager.WakeLock partialWakeLock;
    private PowerManager powerManager;
    private Resources res;
    private TelephonyManager telephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(tag, "onCreate");
        setContentView(R.layout.activity_maps);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        context = getApplicationContext();
        res = getResources();
        setUpMapIfNeeded();
        telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (flagButton) {
                    if (!myLoc.equals(new LatLng(0, 0)) &&
                            (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED)) {
                        Log.d(tag, "map_touched");
                        newLoc = latLng;
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(myLoc));
                        String url = getDirectionsUrl(myLoc, newLoc);
                        DownloadTask downloadTask = new DownloadTask(mMap,
                                newLoc,
                                radius,
                                res.getString(R.string.distance),
                                res.getString(R.string.duration),
                                res.getString(R.string.calcul),
                                res.getString(R.string.tap_marker),
                                MapsActivity.this,
                                true);
                        downloadTask.execute(url);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(newLoc));
                    } else {
                        Toast.makeText(context,
                                res.getString(R.string.gps_problem),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(tag, "marker_touched");
                if (marker.getPosition().equals(newLoc)) {
                    optionDialog(myLoc, newLoc);
                } else
                    Log.d(tag, marker.getId());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                return true;
            }
        });

        lang = prefs.getString("lang", "default");
        if (lang.equals("default")) {
            lang = getResources().getConfiguration().locale.getCountry();
        }
        radius = Integer.parseInt(prefs.getString("radius", "250"));
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
        createWakeLocks();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(tag, "onStart()");
        isActive = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (partialWakeLock.isHeld()) {
            partialWakeLock.release();
        }
        Log.d(tag, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        partialWakeLock.acquire();
        Log.d(tag, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(tag, "onStop()");
        isActive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(tag, "onDestroy()");
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void setUpMapIfNeeded() {
        Log.d(tag, "map_set_up");
        if (mMap == null) {
            String type = prefs.getString("map_type", "MAP_TYPE_NORMAL");
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if ("MAP_TYPE_HYBRID".equals(type)) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            } else {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
            if (mMap != null) {
                myLoc = getMyPosition();
            }
        }
    }

    public LatLng getMyPosition() {
        Log.d(tag, "getting_my_position");
        String[] str = {res.getString(R.string.gps_start_problem),
                res.getString(R.string.gps_settings),
                res.getString(R.string.start_gps_settings),
                res.getString(R.string.cancel)};
        try {
            firstMarkerMaker gps = new firstMarkerMaker(this, str);
            //LatLng location = gps.getLatLng(true);
            LatLng location = new LatLng(gps.getLatitude(), gps.getLongitude());
            Log.d(tag, location.toString());
            if (!((location.latitude == 0) && (location.longitude == 0))) {
                mMap.addMarker(new MarkerOptions().position(location));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(
                        location).zoom(16).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            } else {
                gps.showSettingsAlert();
            }
            return location;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(0, 0);
    }

    public static String getDirectionsUrl(LatLng origin, LatLng dest) {
        Log.d(tag, "get_direction_url");

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String sensor = "sensor=false";

        String language = "language=" + lang;

        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + language;

        String output = "json";

        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    private void Dialog(final LatLng start, final LatLng end) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(res.getString(R.string.add_favorite));
        alert.setMessage(res.getString(R.string.enter_name));
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString().isEmpty() ? "new route" : input.getText().toString();
                Routes route = new Routes(getBaseContext());
                route.insert(route.getMaxIdValue() + 1, value, start.latitude, start.longitude, end.latitude, end.longitude);
                Toast.makeText(context,
                        res.getString(R.string.add_to_favorite), Toast.LENGTH_LONG).show();
            }
        });

        alert.setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public LatLng Converter(String locationAddress) {
        int index = locationAddress.indexOf(",");
        String lat = locationAddress.substring(0, index).trim();
        String lng = locationAddress.substring(index + 1).trim();
        Double latitude = Double.parseDouble(lat);
        Double longitude = Double.parseDouble(lng);
        return new LatLng(latitude, longitude);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_1, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.favorites: {
                if (flagButton) {
                    Intent i = new Intent(this, FavoriteList.class);
                    startActivityForResult(i, 0);
                }
            }
            break;
            case R.id.settings: {
                if (flagButton) {
                    Intent intent = new Intent(MapsActivity.this, Settings.class);
                    startActivity(intent);
                }
            }
            break;
            case R.id.search_item: {
                if (flagButton) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);

                    alert.setTitle(res.getString(R.string.search_smth));
                    alert.setMessage("");
                    final EditText input = new EditText(this);
                    alert.setView(input);
                    alert.setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString();
                            if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
                                try {
                                    Log.d(tag, value.replace("\n", "&").replace(" ", "&"));
                                    String[] str = {
                                            res.getString(R.string.distance),
                                            res.getString(R.string.duration),
                                            res.getString(R.string.tap_marker),
                                            res.getString(R.string.address),
                                            res.getString(R.string.get_addres_location_problem),
                                            res.getString(R.string.calcul),
                                            value,
                                            value.replace("\n", "&").replace(" ", "&")
                                    };
                                    Handler hand = new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            try {
                                                Bundle bundle = msg.getData();
                                                String date = bundle.getString("key");
                                                newLoc = Converter(date);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    new GetLocation(
                                            context,
                                            mMap,
                                            myLoc,
                                            str,
                                            radius,
                                            MapsActivity.this,
                                            hand).execute(value.replace("\n", "&").replace(" ", "&"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(context,
                                        res.getString(R.string.gps_problem),
                                        Toast.LENGTH_LONG).show();
                            }
                            dialog.dismiss();
                        }
                    });

                    alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    });

                    alert.show();
                }
            }
            break;
            case R.id.get_my_loc: {
                Log.d(tag, "get_my_position_button");
                if (newLoc != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(myLoc)
                            .zoom(16)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    mMap.clear();
                    myLoc = getMyPosition();

                }
            }
            break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case (0): {
                    if (resultCode == Activity.RESULT_OK) {
                        DocumentInfo myDoc = data.getParcelableExtra("DocumentInfo");
                        myLoc = new LatLng(
                                Double.parseDouble(myDoc.getStartLatitude()),
                                Double.parseDouble(myDoc.getStartLongitude())
                        );
                        newLoc = new LatLng(
                                Double.parseDouble(myDoc.getEndLatitude()),
                                Double.parseDouble(myDoc.getEndLongitude())
                        );
                        Log.d(tag, myLoc.toString());
                        Log.d(tag, newLoc.toString());
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(myLoc));
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                        new CameraPosition.Builder()
                                                .target(myLoc)
                                                .zoom(16)
                                                .build()
                                )
                        );
                        new DownloadTask(mMap,
                                newLoc,
                                radius,
                                res.getString(R.string.distance),
                                res.getString(R.string.duration),
                                res.getString(R.string.calcul),
                                res.getString(R.string.tap_marker),
                                MapsActivity.this,
                                true)
                                .execute(getDirectionsUrl(myLoc, newLoc));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void optionDialog(final LatLng start, final LatLng end) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        String[] str = {
                res.getString(R.string.start_task),
                res.getString(R.string.cancel_running_task),
                res.getString(R.string.add_favorite),
                res.getString(R.string.cancel)};
        dialog.setTitle(res.getString(R.string.confirm_your_choice))
                .setItems(str, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case (0): {
                                if (flagButton) {
                                    Log.d(tag, "AsyncTask started!");
                                    flagButton = false;
                                    task = new GoToDestination(context,
                                            mMap,
                                            myLoc,
                                            newLoc,
                                            radius + 1501.0,
                                            radius);
                                    task.execute();
                                } else {
                                    Toast.makeText(getApplicationContext(), getResources().
                                            getString(R.string.task_started), Toast.LENGTH_LONG).show();
                                }
                                dialog.dismiss();
                            }
                            break;
                            case (1): {
                                if (!flagButton) {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
                                    alertDialog.setMessage(getResources().getString(R.string.cancel_task));
                                    alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            flagButton = true;
                                            task.cancel(true);
                                            Log.d(tag, "Task canceled!");
                                        }
                                    });
                                    alertDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                                    alertDialog.show();
                                } else {
                                    Toast.makeText(getApplicationContext(), getResources().
                                            getString(R.string.task_not_started), Toast.LENGTH_LONG).show();
                                }
                            }
                            break;
                            case (2): {
                                Dialog(start, end);
                            }
                            break;
                            case (3): {
                                dialog.dismiss();
                            }
                            break;
                        }
                    }
                });
        dialog.show();
    }

    public void stopIsScreenOff(boolean flag) {
        flagButton = true;
        Log.d(tag, "AsyncTask stopped!stopIsScreenOff");
        startService(new Intent(this, AlarmService.class));
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        String goodEnd = getResources().getString(R.string.we_have_come);
        String badEnd = getResources().getString(R.string.gps_dismissed);
        if (flag) {
            alertDialog.setTitle(goodEnd);
        } else {
            alertDialog.setTitle(badEnd);
        }

        alertDialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                stopService(new Intent(MapsActivity.this, AlarmService.class));
                dialog.dismiss();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void stopIsScreenOn(boolean flag) {
        flagButton = true;
        Log.d(tag, "AsyncTask stopped!stopIsScreenOn");
        String goodEnd = getResources().getString(R.string.we_have_come);
        String badEnd = getResources().getString(R.string.gps_dismissed);
        Context context = getApplicationContext();

        Intent notificationIntent = new Intent(context, MapsActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.source3)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(getResources().getString(R.string.app_name));
        if (flag) {
            builder.setContentText(goodEnd);
        } else {
            builder.setContentText(badEnd);
        }
        Notification notification = builder.build();
        notification.vibrate = new long[]{1000, 1000, 1000, 1000, 1000};

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(101, notification);
    }

    public void stopIsActive(boolean flag) {
        flagButton = true;
        Log.d(tag, "AsyncTask stopped!stopIsActive");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        String goodEnd = getResources().getString(R.string.we_have_come);
        String badEnd = getResources().getString(R.string.gps_dismissed);
        if (flag) {
            alertDialog.setTitle(goodEnd);
        } else {
            alertDialog.setTitle(badEnd);
        }
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        alertDialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                stopService(new Intent(MapsActivity.this, AlarmService.class));
                dialog.dismiss();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
        v.vibrate(1000);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public class GoToDestination extends AsyncTask<Void, String, Void> {
        private final static String tag = "go_to_destination";
        private LatLng start;
        private LatLng end;
        private Double distance;
        private int mRadius;
        private GoogleMap mMap;
        private Context context;
        private Thread back;

        public GoToDestination(Context context, GoogleMap mMap, LatLng start, LatLng end, Double distance, int mRadius) {
            this.context = context;
            this.mMap = mMap;
            this.start = start;
            this.end = end;
            this.distance = distance;
            this.mRadius = mRadius;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final LatLng nullPoint = new LatLng(0.0, 0.0);
            back = new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    firstMarkerMaker gps;
                    Integer sleepTime = 0;
                    while (distance >= mRadius) {
                        if (isCancelled()) break;
                        if (distance >= radius + 1500) {
                            Log.d(tag, "getLocationByNetwork");
                            gps = new firstMarkerMaker(context);
                        } else {
                            Log.d(tag, "getLocationByGps");
                            gps = new firstMarkerMaker(context, true);
                        }
                        if (gps.canGetLocation()) Log.d(tag, "canGetLocation");
                        start = new LatLng(gps.getLatitude(), gps.getLongitude());
                        Log.d(tag, start.toString());
                        if (start.equals(nullPoint) || !isOnline()) {
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("key", "Can't get current location!");
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                            break;
                        }
                        myLoc = start;
                        try {
                            if (distance <= radius + 350) {
                                Log.d(tag, "Get distance by distanceTo function");
                                Location loc1 = new Location("Point start");
                                loc1.setLatitude(start.latitude);
                                loc1.setLongitude(start.longitude);
                                Location loc2 = new Location("Point end");
                                loc2.setLatitude(end.latitude);
                                loc2.setLongitude(end.longitude);
                                distance = (double) loc1.distanceTo(loc2);
                            } else {
                                Log.d(tag, "Get distance by GoogleApi");
                                String url = MapsActivity.getDirectionsUrl(start, end);
                                publishProgress(url);
                                String dist = new DistanceTask().execute(url).get();
                                distance = Double.parseDouble(dist);
                            }
                            Log.d(tag, "Distance: " + distance.toString());
                            if (distance <= mRadius) break;
                            if (distance > 75000) sleepTime = 300000;
                            if (distance <= 50000) sleepTime = 240000;
                            if (distance <= 25000) sleepTime = 120000;
                            if (distance <= 15000) sleepTime = 60000;
                            if (distance <= 7500) sleepTime = 30000;
                            if (distance <= 4000) sleepTime = 15000;
                            if (distance <= 2000) sleepTime = 10000;
                            if (distance <= 1000) sleepTime = 5000;
                            Log.d(tag, "thread sleep " + sleepTime.toString());
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!isCancelled() && !start.equals(nullPoint) && isOnline()) {
                        Log.d(tag, "We've come!");
                        Message msg = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("key", "We've come!");
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                    Looper.loop();
                }
            });
            back.start();
            Log.d(tag, "doInBackGround_Finished");
            return null;
        }

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                try {
                    Log.d(tag, "handleMessage");
                    Bundle bundle = msg.getData();
                    String date = bundle.getString("key");
                    Log.d(tag, date);
                    wakeDevice(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        protected void onProgressUpdate(String... url) {
            super.onProgressUpdate();
            Log.d(tag, "onProgressUpdate");
            mMap.clear();
            new DownloadTask(mMap,
                    newLoc,
                    mRadius,
                    res.getString(R.string.distance),
                    res.getString(R.string.duration),
                    res.getString(R.string.calcul),
                    res.getString(R.string.tap_marker),
                    MapsActivity.this,
                    false).execute(url);
            mMap.addMarker(new MarkerOptions().position(start));
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            back.interrupt();
            Log.d(tag, "Canceled");
        }
    }

    protected void createWakeLocks() {
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Loneworker - FULL WAKE LOCK");
        partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);
    }

    public void wakeDevice(String date) {
        Log.d(tag, "wakeDevice");
        if (!powerManager.isScreenOn()) {
            mWakeLock.acquire();
            MapsActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(new ComponentName(context, MapsActivity.class));
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            Log.d(tag, "Screen Off");
            if (date.equals("We've come!")) {
                stopIsScreenOff(true);
            } else {
                stopIsScreenOff(false);
            }
        } else {
            Log.d(tag, "Screen On");
            if (!isActive) {
                if (date.equals("We've come!")) {
                    stopIsScreenOn(true);
                } else {
                    stopIsScreenOn(false);
                }
            } else {
                if (date.equals("We've come!")) {
                    stopIsActive(true);
                } else {
                    stopIsActive(false);
                }
            }
        }
    }
}

