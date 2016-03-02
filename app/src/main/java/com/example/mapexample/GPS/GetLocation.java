package com.example.mapexample.GPS;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.mapexample.MapsActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class GetLocation extends AsyncTask<String, Void, LatLng> {
    private final static String tag = "getLocationFromAddress";
    private Context context;
    private GoogleMap map;
    private LatLng myLoc;
    private String[] str;
    private int radius;
    private Activity activity;
    private Handler handler;
    private ProgressDialog pd;
    public GetLocation(Context context, GoogleMap map, LatLng myLoc, String[] str, int radius, Activity activity, Handler handler){
        this.context = context;
        this.map = map;
        this.myLoc = myLoc;
        this.str = str;
        this.radius = radius;
        this.activity = activity;
        this.handler = handler;
    }

    @Override
    protected void onPreExecute(){
        Log.d(tag, "onPreExecute");
        super.onPreExecute();
        pd = new ProgressDialog(activity);
        pd.setMessage(str[5]);
        pd.show();
    }
    @Override
    protected LatLng doInBackground(String... strings) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(str[6], 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        double latitude = 0, longitude = 0;
        if(addresses.size() > 0) {
            latitude= addresses.get(0).getLatitude();
            longitude= addresses.get(0).getLongitude();
        }
        return new LatLng(latitude, longitude);
    }
    @Override
    protected void onPostExecute(LatLng result){
        super.onPostExecute(result);
        Log.d(tag, "result =" + result);
        Log.d(tag, "str[6] =" + str[6]);
        MapsActivity mapsActivity = new MapsActivity();
        if (!result.equals(new LatLng(0,0))) {
            map.clear();
            map.addMarker(new MarkerOptions().position(myLoc));
            String url = mapsActivity.getDirectionsUrl(myLoc, result);
            DownloadTask downloadTask = new DownloadTask(map,
                    result,
                    radius,
                    str[0],
                    str[1],
                    str[5],
                    str[2],
                    activity,
                    false);
            downloadTask.execute(url);
            map.animateCamera(CameraUpdateFactory.newLatLng(result));
            Toast.makeText(context, str[2], Toast.LENGTH_LONG).show();

            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("key", result.latitude + "," + result.longitude);
            msg.setData(bundle);
            handler.sendMessage(msg);
        } else {
            String badEnd = str[3]
                    + str[6] + "\n"
                    + str[4];
            Toast.makeText(context, badEnd, Toast.LENGTH_LONG).show();
        }
        if (pd.isShowing()) {
            pd.dismiss();
        }
    }
}
