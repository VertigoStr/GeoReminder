package com.example.mapexample.DataBase;

import android.provider.BaseColumns;
import android.util.Log;

public class Elements {
    private final static String tag = "elements";
    public static class Element{
        public static final String TABLE_NAME = "Routes";
        public static final String DEFAULT_SORT = ElementColumns.Name + " DESC";
        public long id;
        public String Name;
        public String startLatitude;
        public String startLongitude;
        public String endLatitude;
        public String endLongitude;

        public void setStartLatitude(String value){
            startLatitude = value;
        }
        public void setStartLongitude(String value){
            startLongitude = value;
        }
        public void setEndLatitude(String value){
            endLatitude = value;
        }
        public void setEndLongitude(String value){
            endLongitude = value;
        }
        public void setName(String value) { Name = value; }
        public void setId(long value) { id = value;}
        public String getStartLatitude(){
            return startLatitude;
        }
        public String getStartLongitude(){
            return startLongitude;
        }
        public String  getEndLatitude(){
            return endLatitude;
        }
        public String getEndLongitude(){
            return endLongitude;
        }
        public String getName() { return Name; }
        public long getId() { return id; }

        public String toString() {
            Log.d(tag, "to_string");
            StringBuilder builder = new StringBuilder();
            builder.append(Name);
            return builder.toString();
        }
        public class ElementColumns implements BaseColumns {
            public static final String Name = "Name";
            public static final String startLatitude = "startLatitude";
            public static final String startLongitude = "startLongitude";
            public static final String endLatitude = "endLatitude";
            public static final String endLongitude = "endLongitude";
        }
    }
}
