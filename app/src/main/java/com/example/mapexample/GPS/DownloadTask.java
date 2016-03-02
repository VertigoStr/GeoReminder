package com.example.mapexample.GPS;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DownloadTask extends AsyncTask<String, Void, String> {
    private final static String tag = "download_task";
    private GoogleMap map;
    private LatLng end;
    private int radius;
    private String dist;
    private String durat;
    private String calcul;
    private String tapMarker;
    private Activity activity;
    private ProgressDialog pd;
    private boolean flag;
    public DownloadTask(GoogleMap map, LatLng end, int radius, String dist, String durat, String calcul, String tapMarker, Activity activity, boolean flag){
        this.end = end;
        this.map = map;
        this.radius = radius;
        this.dist = dist;
        this.durat = durat;
        this.activity = activity;
        this.calcul = calcul;
        this.flag = flag;
        this.tapMarker = tapMarker;
    }
    @Override
    protected void onPreExecute(){
        if (flag) {
            Log.d(tag, "onPreExecute");
            super.onPreExecute();
            pd = new ProgressDialog(activity);
            pd.setMessage(calcul);
            pd.show();
        }
    }
    @Override
    protected String doInBackground(String... url) {
        Log.d(tag, "do_in_background");
        String data = "";

        try{
            HttpConnection http = new HttpConnection();
            data = http.readUrl(url[0]);
        }catch(Exception e){
            Log.d("Background Task", e.toString());
        }
        return data;
    }
    @Override
    protected void onPostExecute(String result) {
        Log.d(tag, "on_post_execute");
        super.onPostExecute(result);
        ParserTask parserTask = new ParserTask(map, end, radius, dist, durat);
        parserTask.execute(result);
        if (flag) {
            if (pd.isShowing()) {
                pd.dismiss();
                Toast.makeText(activity, tapMarker, Toast.LENGTH_LONG).show();
            }
        }
    }
}

class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {
    private final static String tag = "parser_task";
    private GoogleMap map;
    private LatLng end;
    private int radius;
    private String dist;
    private String durat;
    ParserTask(GoogleMap map, LatLng end, int radius, String dist, String durat){
        this.end = end;
        this.map = map;
        this.radius = radius;
        this.dist = dist;
        this.durat = durat;
    }
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
        Log.d(tag, "do_in_background");
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try{
            jObject = new JSONObject(jsonData[0]);
            PathJSONParser parser = new PathJSONParser();
            routes = parser.parse(jObject);
        }catch(Exception e){
            e.printStackTrace();
        }
        return routes;
    }


    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        Log.d(tag, "on_post_execute");
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;
        String distance = "";
        String duration = "";

        if(result.size()<1){
            Log.d("No Points", "No Points");
            return;
        }


        for(int i=0;i<result.size();i++){
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();


            List<HashMap<String, String>> path = result.get(i);


            for(int j=0;j<path.size();j++){
                HashMap<String,String> point = path.get(j);

                if(j==0){
                    distance = (String)point.get("distance");
                    continue;
                }else if(j==1){
                    duration = (String)point.get("duration");
                    continue;
                }

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }


            lineOptions.addAll(points);
            lineOptions.width(6);
            lineOptions.color(Color.YELLOW);
        }
        Marker marker = map.addMarker((new MarkerOptions().position(end))
                .title(dist + distance)
                .snippet(durat + duration)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        marker.showInfoWindow();

        CircleOptions circleOptions = new CircleOptions()
                .center(end).radius(radius)
                .strokeColor(Color.BLUE)
                .strokeWidth(5);
        map.addCircle(circleOptions);
        map.addPolyline(lineOptions);
    }
}