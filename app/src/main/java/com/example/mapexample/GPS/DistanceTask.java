package com.example.mapexample.GPS;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

public class DistanceTask extends AsyncTask<String, Void, String > {

    private final static String tag = "distance_task";


    @Override
    protected String doInBackground(String... url) {
        Log.d(tag, "do_in_background");
        JSONObject jObject;
        String distance = null;
        String data = "";

        try{
            HttpConnection http = new HttpConnection();
            data = http.readUrl(url[0]);
        }catch(Exception e){
            Log.d("Background Task", e.toString());
        }

        try {
            jObject = new JSONObject(data);
            PathJSONParser parser = new PathJSONParser();
            distance = parser.getInfo(jObject, "distanceValue");
            Log.d(tag, distance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return distance;
    }
}
