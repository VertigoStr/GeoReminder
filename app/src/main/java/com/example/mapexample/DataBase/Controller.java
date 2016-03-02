package com.example.mapexample.DataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

public class Controller {
    private final static String tag = "controller";
    private static final boolean LOGV = false;
    private static int maxRowsInNames = -1;
    private static final String TAG = Controller.class.getSimpleName();

    private Controller() {
    }

    public static int getMaxRowsInElemnts() {

        return maxRowsInNames;
    }

    public static ArrayList<Elements.Element> readElements(Context context) {
        Log.d(tag, "read");
        ArrayList<Elements.Element> list = null;
        try {
            DataBaseHelper dbhelper = new DataBaseHelper(context);
            SQLiteDatabase sqliteDB = dbhelper.getReadableDatabase();
            String[] columnsToTake = { BaseColumns._ID, Elements.Element.ElementColumns.Name };
            Cursor cursor = sqliteDB.query(Elements.Element.TABLE_NAME, columnsToTake, null, null, null, null,
                    Elements.Element.DEFAULT_SORT);
            if (cursor.moveToFirst()) {
                list = new ArrayList<Elements.Element>();
            }
            while (cursor.moveToNext()) {
                Elements.Element oneRow = new Elements.Element();
                oneRow.setId(cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID)));
                oneRow.setName(cursor.getString(cursor.getColumnIndexOrThrow(Elements.Element.ElementColumns.Name)));
                list.add(oneRow);
            }
            cursor.close();
            dbhelper.close();
        } catch (Exception e) {
            Log.e(TAG, "Failed to select Names.", e);
        }
        return list;
    }

    public static void setMaxRowsInNames(int maxRowsInNames) {

        Controller.maxRowsInNames = maxRowsInNames;
    }


    public static void update(Context context, String comment, long l) {

        try {
            DataBaseHelper dbhelper = new DataBaseHelper(context);
            SQLiteDatabase sqliteDB = dbhelper.getWritableDatabase();
            String quer = null;
            int countRows = -1;
            Cursor cursor = sqliteDB.query(Elements.Element.TABLE_NAME, new String[] { "count(*)" }, null, null, null,
                    null, Elements.Element.DEFAULT_SORT);
            if (cursor.moveToFirst()) {
                countRows = cursor.getInt(0);
                if (LOGV) {
                    Log.v(TAG, "Count in Names table" + String.valueOf(countRows));
                }
            }
            cursor.close();
            quer = String.format("UPDATE " + Elements.Element.TABLE_NAME + " SET " + Elements.Element.ElementColumns.Name
                    + " = '" + comment + "' WHERE " + BaseColumns._ID + " = " + l);
            Log.d("", "" + quer);
            sqliteDB.execSQL(quer);
            sqliteDB.close();
            dbhelper.close();
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed open database. ", e);
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update Names. ", e);
        }
    }

    public static void delete(Context context, long l) {
        Log.d(tag, "delete");
        DataBaseHelper dbhelper = new DataBaseHelper(context);
        SQLiteDatabase sqliteDB = dbhelper.getWritableDatabase();
        sqliteDB.delete(Elements.Element.TABLE_NAME, BaseColumns._ID  + " = " + l, null);
        sqliteDB.close();
        dbhelper.close();
    }

    public static void write(Context context,
                             String Name,
                             String startLatitude,
                             String startLongitude,
                             String endLatitude,
                             String endLongitude) {
        Log.d(tag, "write");

        try {

            DataBaseHelper dbhelper = new DataBaseHelper(context);

            SQLiteDatabase sqliteDB = dbhelper.getWritableDatabase();
            String quer = null;
            int countRows = -1;

            Cursor cursor = sqliteDB.query(Elements.Element.TABLE_NAME, new String[] { "count(*)" }, null, null, null,
                    null, Elements.Element.DEFAULT_SORT);
            if (cursor.moveToFirst()) {
                countRows = cursor.getInt(0);
                if (LOGV) {
                    Log.v(TAG, "Count in Names table" + String.valueOf(countRows));
                }
            }
            cursor.close();
            if ((maxRowsInNames == -1) || (maxRowsInNames >= countRows)) {
                quer = String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (%s, %s, %s, %s ,%s);",
                        Elements.Element.TABLE_NAME,
                        Elements.Element.ElementColumns.Name,
                        Elements.Element.ElementColumns.startLatitude,
                        Elements.Element.ElementColumns.startLongitude,
                        Elements.Element.ElementColumns.endLatitude,
                        Elements.Element.ElementColumns.endLongitude,
                        Name, startLatitude, startLongitude, endLatitude, endLongitude);
            }
            sqliteDB.execSQL(quer);
            sqliteDB.close();
            dbhelper.close();
        } catch (SQLiteException e) {
            Log.e(TAG, "Failed open rimes database. ", e);
        } catch (SQLException e) {
            Log.e(TAG, "Failed to insert Names. ", e);
        }
    }
}
