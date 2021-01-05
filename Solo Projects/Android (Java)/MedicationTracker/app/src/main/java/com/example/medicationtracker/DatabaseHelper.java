package com.example.medicationtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    public String DATABASE_NAME;
    private static final int DATABASE_VERSION = 1;
    private String name;
    private Map<String, String> column_map;

    private String create;

    public DatabaseHelper(Context context, String database, String name, Map<String, String> columns) {
        super(context, database, null, DATABASE_VERSION);
        this.name = name;
        this.DATABASE_NAME = database;
        String text = "";
        List<String> headers = new ArrayList<>(columns.keySet());
        for (int i = 0; i < headers.size(); i++){
            text += headers.get(i) + " " + columns.get(headers.get(i)) + ", ";
        }
        this.create = "CREATE TABLE IF NOT EXISTS "
                + name + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " + text.substring(0, text.length() - 2) + ");";
        this.column_map = columns;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(this.create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + this.name + "'");
        onCreate(db);
    }

    public void reboot() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS '" + this.name + "'");
        onCreate(db);
        db.close();
    }

    public String delete_database() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS '" + this.name + "'");
        db.close();
        return DATABASE_NAME;
    }

    public int get_id(long row_id){
        String selectQuery = "SELECT id FROM " + this.name + " WHERE rowid=" + row_id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        int id = -1;

        if (c.moveToFirst()) {
            do {
                id += c.getInt(0) + 1;
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return id;
    }

    public int addRow(Map<String, Object> data) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Creating content values
        ContentValues values = new ContentValues();
        List<String> headers = new ArrayList<>(data.keySet());
        for (int i = 0; i < headers.size(); i++){
            Object value = data.get(headers.get(i));
            if (value != null){
                Class<?> type = value.getClass();
                if (type == String.class){
                    values.put(headers.get(i), (String) value);
                }
                else if (type == Float.class){
                    values.put(headers.get(i), (Float) value);
                }
                else if (type == Integer.class){
                    values.put(headers.get(i), (Integer) value);
                }
                else if (type == Long.class){
                    values.put(headers.get(i), (Long) value);
                }
                else if (type == Boolean.class){
                    values.put(headers.get(i), (Boolean) value);
                }
            }
        }

        long row = db.insert(this.name, null, values);

        db.close();

        return get_id(row);
    }

    public int update(Map<String, Object> data, String[] constraints) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Creating content values
        ContentValues values = new ContentValues();
        List<String> headers = new ArrayList<>(data.keySet());
        for (int i = 0; i < headers.size(); i++){
            Object value = data.get(headers.get(i));
            if (value != null){
                Class<?> type = value.getClass();
                if (type == String.class){
                    values.put(headers.get(i), (String) value);
                }
                else if (type == Float.class){
                    values.put(headers.get(i), (Float) value);
                }
                else if (type == Integer.class){
                    values.put(headers.get(i), (Integer) value);
                }
                else if (type == Long.class){
                    values.put(headers.get(i), (Long) value);
                }
                else if (type == Boolean.class){
                    values.put(headers.get(i), (Boolean) value);
                }
            }
            else {
                values.put(headers.get(i), (String) null);
            }
        }

        String constraint_string = "";

        for (int i = 0; i < constraints.length; i++){
            constraint_string += constraints[i] + " AND ";
        }
        constraint_string = constraint_string.substring(0, constraint_string.length() - 5);


        long row = db.update(this.name, values, constraint_string, null);

        db.close();

        return get_id(row);
    }

    public void deleteRows(String[] constraints) {

        String constraint_string = "";
        if (constraints != null && constraints.length > 0){
            constraint_string += " WHERE ";
            for (int i = 0; i < constraints.length; i++){
                constraint_string += constraints[i] + " AND ";
            }
            constraint_string = constraint_string.substring(0, constraint_string.length() - 5);
        }

        String selectQuery = "DELETE FROM " + this.name + constraint_string;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(selectQuery);
        db.close();
    }

    public void delete_single_constraint(String constraint) {

        String constraint_string = "";
        if (constraint != null){
            constraint_string += " WHERE " + constraint;
        }

        String selectQuery = "DELETE FROM " + this.name + constraint_string;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(selectQuery);
        db.close();
    }

    public ArrayList<Map<String, Object>> getRows(String[] columns, String[] constraints, String[] sort, boolean distinct) {
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        String column_string = "";
        if (distinct){
            column_string += "DISTINCT ";
        }

        if (columns == null){
            column_string += "*";
        }
        else{
            for (int i = 0; i < columns.length; i++){
                column_string += columns[i] + ", ";
            }
            column_string = column_string.substring(0, column_string.length() - 2);
        }
        String constraint_string = "";
        if (constraints != null && constraints.length > 0){
            constraint_string += " WHERE ";
            for (int i = 0; i < constraints.length; i++){
                constraint_string += constraints[i] + " AND ";
            }
            constraint_string = constraint_string.substring(0, constraint_string.length() - 5);
        }
        String sort_string = "";
        if (sort != null && sort.length > 1){
            sort_string += " ORDER BY ";
            for (int i = 0; i < sort.length; i += 2){
                sort_string += sort[i] + " " + sort[i + 1] + ", ";
            }
            sort_string = sort_string.substring(0, sort_string.length() - 2);
        }
        String selectQuery = "SELECT " + column_string.substring(0) + " FROM " + this.name + constraint_string + sort_string;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (c.moveToFirst()) {
            do {
                Map<String, Object> data = new HashMap<>();
                for (int i = 0; i < c.getColumnCount(); i++) {
                    int type = c.getType(i);

                    if (type == Cursor.FIELD_TYPE_STRING){
                        data.put(c.getColumnName(i), c.getString(i));
                    }
                    else if (type == Cursor.FIELD_TYPE_FLOAT){
                        data.put(c.getColumnName(i), c.getFloat(i));
                    }
                    else if (type == Cursor.FIELD_TYPE_NULL) {
                        data.put(c.getColumnName(i), null);
                    }
                    else if (!c.getColumnName(i).equals("id") && this.column_map.get(c.getColumnName(i)).equals("BIGINT")){
                        data.put(c.getColumnName(i), c.getLong(i));
                    }
                    else if (type == Cursor.FIELD_TYPE_INTEGER){
                        data.put(c.getColumnName(i), c.getInt(i));
                    }
                }
                results.add(data);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return results;
    }

    public Map<String, Object> getSingleRow(String[] columns, String[] constraints) {

        String column_string = "";
        if (columns == null){
            column_string += "*";
        }
        else{
            for (int i = 0; i < columns.length; i++){
                column_string += columns[i] + ", ";
            }
            column_string = column_string.substring(0, column_string.length() - 2);
        }


        String constraint_string = "";
        if (constraints != null && constraints.length > 0){
            constraint_string += " WHERE ";
            for (int i = 0; i < constraints.length; i++){
                constraint_string += constraints[i] + " AND ";
            }
            constraint_string = constraint_string.substring(0, constraint_string.length() - 5);
        }
        String selectQuery = "SELECT " + column_string.substring(0) + " FROM " + this.name + constraint_string;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        Map<String, Object> data = new HashMap<>();
        if (c.moveToFirst()) {
            for (int i = 0; i < c.getColumnCount(); i++) {
                int type = c.getType(i);

                if (type == Cursor.FIELD_TYPE_STRING){
                    data.put(c.getColumnName(i), c.getString(i));
                }
                else if (type == Cursor.FIELD_TYPE_FLOAT){
                    data.put(c.getColumnName(i), c.getFloat(i));
                }
                else if (!c.getColumnName(i).equals("id") && this.column_map.get(c.getColumnName(i)).equals("BIGINT")){
                    data.put(c.getColumnName(i), c.getLong(i));
                }
                else if (type == Cursor.FIELD_TYPE_INTEGER){
                    data.put(c.getColumnName(i), c.getInt(i));
                }
                else if (type == Cursor.FIELD_TYPE_NULL) {
                    data.put(c.getColumnName(i), null);
                }
            }
        }
        c.close();
        db.close();
        return data;
    }

    public ArrayList<Object> getSingleColumn(String column, String[] constraints, String sort, boolean distinct) {
        ArrayList<Object> results = new ArrayList<>();

        String constraint_string = "";
        if (constraints != null && constraints.length > 0){
            constraint_string += " WHERE ";
            for (int i = 0; i < constraints.length; i++){
                constraint_string += constraints[i] + " AND ";
            }
            constraint_string = constraint_string.substring(0, constraint_string.length() - 5);
        }
        String sort_string = "";
        if (sort != null){
            sort_string += " ORDER BY " + column + " " + sort;
        }
        if (distinct){
            column = "DISTINCT " + column;
        }
        String selectQuery = "SELECT " + column + " FROM " + this.name + constraint_string + sort_string;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (c.moveToFirst()) {
            do {
                Map<String, Object> data = new HashMap<>();
                for (int i = 0; i < c.getColumnCount(); i++) {
                    int type = c.getType(i);

                    if (type == Cursor.FIELD_TYPE_STRING){
                        results.add(c.getString(i));
                    }
                    else if (type == Cursor.FIELD_TYPE_FLOAT){
                        results.add(c.getFloat(i));
                    }
                    else if (!c.getColumnName(i).equals("id") && this.column_map.get(c.getColumnName(i)).equals("BIGINT")){
                        results.add(c.getLong(i));
                    }
                    else if (type == Cursor.FIELD_TYPE_INTEGER){
                        results.add(c.getInt(i));
                    }
                    else if (type == Cursor.FIELD_TYPE_NULL) {
                        results.add(null);
                    }
                }
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return results;
    }

    public Object getObject(String column, String[] constraints) {
        String constraint_string = "";
        if (constraints != null && constraints.length > 0){
            constraint_string += " WHERE ";
            for (int i = 0; i < constraints.length; i++){
                constraint_string += constraints[i] + " AND ";
            }
            constraint_string = constraint_string.substring(0, constraint_string.length() - 5);
        }
        String selectQuery = "SELECT " + column + " FROM " + this.name + constraint_string;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        Object object = null;

        if (c.moveToFirst()) {
            int type = c.getType(0);

            if (type == Cursor.FIELD_TYPE_STRING){
                object = c.getString(0);
            }
            else if (type == Cursor.FIELD_TYPE_FLOAT){
                object = c.getFloat(0);
            }
            else if (!c.getColumnName(0).equals("id") && this.column_map.get(c.getColumnName(0)).equals("BIGINT")){
                object = c.getLong(0);
            }
            else if (type == Cursor.FIELD_TYPE_INTEGER){
                object = c.getInt(0);
            }
        }
        c.close();
        db.close();
        return object;
    }

    public List<String> get_column_names(){
        SQLiteDatabase clients = this.getReadableDatabase();
        Cursor dbCursor = clients.query(this.name, null, null, null, null, null, null);
        List<String> columnNames = Arrays.asList(dbCursor.getColumnNames());
        clients.close();
        dbCursor.close();

        return columnNames;
    }
}