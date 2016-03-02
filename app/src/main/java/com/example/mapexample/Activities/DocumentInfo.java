package com.example.mapexample.Activities;

import android.os.Parcel;
import android.os.Parcelable;

public class DocumentInfo implements Parcelable{
    private String startLatitude;
    private String startLongitude;
    private String endLatitude;
    private String endLongitude;
    private String sound;
    public DocumentInfo(Double startLatitude, Double startLongitude, Double endLatitude, Double endLongitude, String sound){
        this.startLatitude = startLatitude.toString();
        this.startLongitude = startLongitude.toString();
        this.endLatitude = endLatitude.toString();
        this.endLongitude = endLongitude.toString();
        this.sound = sound;
    }
    public DocumentInfo(Parcel in){
        String[] data = new String[5];
        in.readStringArray(data);
        startLatitude = data[0];
        startLongitude = data[1];
        endLatitude = data[2];
        endLongitude = data[3];
        sound = data[4];
    }
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
    public void setSound(String value){
        sound = value;
    }
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
    public String getSound(){
        return sound;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {startLatitude, startLongitude, endLatitude, endLongitude, sound});
    }
    public static final Parcelable.Creator<DocumentInfo> CREATOR = new Parcelable.Creator<DocumentInfo>() {

        @Override
        public DocumentInfo createFromParcel(Parcel source) {
            return new DocumentInfo(source);
        }

        @Override
        public DocumentInfo[] newArray(int size) {
            return new DocumentInfo[size];
        }
    };
}
