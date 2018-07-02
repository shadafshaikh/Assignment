package com.test.assignment.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.test.assignment.root.LocationSetter;

import java.util.ArrayList;

/**
 *
 * Created by Shadaf Shaikh on 1/07/2018
 */
public final class DatabaseHelper extends SQLiteOpenHelper {

    //BASIC CONSTANTS
    private static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";

    //DB INFO
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Assignment.db";


    private static final String CREATE_LOCATION_TABLE = CREATE_TABLE_IF_NOT_EXISTS +
            TC.TABLE_LOCATION + " (" +
            TC.COL_LAT + REAL_TYPE + COMMA_SEP +
            TC.COL_LONG + REAL_TYPE +
            " )";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_LOCATION_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * @param modelType
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @return
     * @Usage use this method to get all the data from particular table.
     */
    public Object getData(ModelType modelType, String[] columns, String selection,
                          String[] selectionArgs, String groupBy, String having, String orderBy,
                          String limit)  {

        switch (modelType) {
            case LOCATION_DATA:
                return allLocation(columns, selection, selectionArgs, groupBy, having, orderBy, limit);
            default:
                return null;
        }

    }


    private Object allLocation(String[] columns, String selection, String[] selectionArgs,
                                 String groupBy, String having, String orderBy, String limit) {
        ArrayList<LocationSetter> data = new ArrayList<>();
        try {

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cur = db.query(
                    TC.TABLE_LOCATION,  // The table to query
                    columns,                               // The columns to return
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    groupBy,                                     // don't group the rows
                    having,                                     // don't filter by row groups
                    orderBy,
                    limit                              // The sort order
            );
            if (cur != null && cur.getCount() > 0) {
                cur.moveToFirst();
                do {
                    LocationSetter model = new LocationSetter(cur.getDouble(cur.getColumnIndex(TC.COL_LAT)),
                            cur.getDouble(cur.getColumnIndex(TC.COL_LONG)));
                    data.add(model);
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return data;
        }

    }


    public long insertData(Object model, ModelType modelType) {
        long id = 0;
        String tableName = "";
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        switch (modelType) {

            case LOCATION_DATA:
                deleteTableData();
                LocationSetter ob = (LocationSetter) model;
                contentValues.put(TC.COL_LAT,ob.getLatitude());
                contentValues.put(TC.COL_LONG,ob.getLongitude());
                tableName = TC.TABLE_LOCATION;
                break;

        }

        try {
            id = database.insert(tableName, null, contentValues);
            Log.v("Fetched Data", id + "");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.close();
            contentValues.clear();
        }
    return id;
    }

    public void deleteTableData(){
        SQLiteDatabase database = this.getWritableDatabase();
        int rows = database.delete(TC.TABLE_LOCATION, null, null);
    }

    public enum ModelType {
        LOCATION_DATA
    }

    /**
     * Common class for maintaining all the column names of all the table.
     */
    public static abstract class TC implements BaseColumns {
        public static final String TABLE_LOCATION = "location";

        //LOCATION COLUMNS
        public static final String COL_LAT = "ct_name";
        public static final String COL_LONG = "ct_color";

    }

}
