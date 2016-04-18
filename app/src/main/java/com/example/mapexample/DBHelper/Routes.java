package com.example.mapexample.DBHelper;

import android.content.Context;
import java.util.ArrayList;

public class Routes extends DBHelperController {
	private final String TABLE_NAME = "Routes";
    public static final String id = "id";
	public static final String Name = "Name";
	public static final String start_lat = "start_lat";
	public static final String start_lon = "start_lon";
	public static final String end_lat = "end_lat";
	public static final String end_lon = "end_lon";
	private Context context;

	public Routes(Context context) {
		this.context = context;
	}

    public int getMaxIdValue() {
        return Integer.parseInt(super.executeQuery(context, "SELECT max(id) FROM " + TABLE_NAME).get(0).get(0));
    }

	public void insert(Integer id, String Name, Double start_lat, Double start_lon, Double end_lat, Double end_lon) {
        Name = "\"" + Name + "\"";
		super.execute(context, "INSERT INTO " + TABLE_NAME + " values(" + id + ", " + Name + ", " + start_lat + ", " + start_lon + ", " + end_lat + ", " + end_lon + ");");
	}

	public void delete(String whatField, String whatValue) {
		super.delete(context, TABLE_NAME, whatField + " = " + whatValue);
	}

	public void update(String whatField, String whatValue, String whereField, String whereValue) {
		super.execute(context, "UPDATE " + TABLE_NAME + " set " + whatField + " = \"" + whatValue + "\" where " + whereField + " = \"" + whereValue + "\";");
	}

	public ArrayList<ArrayList<String>> select(String fields, String whatField, String whatValue, String sortField, String sort) {
        String query = "SELECT ";
        query += fields == null ? " * FROM " + TABLE_NAME : fields + " FROM " + TABLE_NAME;
        query += whatField != null && whatValue != null ? " WHERE " + whatField + " = \"" + whatValue + "\"" : "";
        query += sort != null && sortField != null ? " order by " + sortField + " " + sort : "";
		return super.executeQuery(context, query);
	}

    public ArrayList<ArrayList<String>> getExecuteResult(String query) {
        return super.executeQuery(context, query);
    }

    public void execute(String query) {
        super.execute(context, query);
    }
}