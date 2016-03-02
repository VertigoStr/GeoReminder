package com.example.mapexample.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {
    private final static String tag = "data_base_helper";
    private static final String DATABASE_NAME = "db.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DEBUG_TAG = DataBaseHelper.class.getSimpleName();
    private static final boolean LOGV = false;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(tag, "constructor");
    }
    public void dropTables(SQLiteDatabase db) {
        Log.d(tag,"dropTables");
        if (LOGV) {
            Log.d(DEBUG_TAG, "onDropTables called");
        }
        db.execSQL("DROP TABLE IF EXISTS " + Elements.Element.TABLE_NAME);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        if (LOGV) {
            Log.v(DEBUG_TAG, "onCreate()");
        }
        db.execSQL("CREATE TABLE " + Elements.Element.TABLE_NAME + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , " + Elements.Element.ElementColumns.Name
                + " TEXT NOT NULL, " + Elements.Element.ElementColumns.startLatitude + " TEXT NOT NULL, "
                + Elements.Element.ElementColumns.startLongitude + " TEXT NOT NULL, "
                + Elements.Element.ElementColumns.endLatitude + " TEXT NOT NULL, "
                + Elements.Element.ElementColumns.endLongitude + " TEXT NOT NULL );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("LOG_TAG", "Обновление базы данных с версии " + oldVersion
                + " до версии " + newVersion + ", которое удалит все старые данные");
        onCreate(db);
    }
}
