package com.example.hackathonmacroeconomics;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "data10.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "macroeconomics";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_INDIA = "india";
    public static final String COLUMN_CHINA = "china";
    public static final String COLUMN_USA = "usa";
    public static final String COLUMN_MACROECONOMICS = "macroeconomic_name";

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_YEAR + " INTEGER,"
            + COLUMN_INDIA + " REAL,"
            + COLUMN_CHINA + " REAL,"
            + COLUMN_USA + " REAL,"
            + COLUMN_MACROECONOMICS + " STRING" + ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertData(int year, double india, double china, double usa, String macroeconomics) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_YEAR, year);
        contentValues.put(COLUMN_INDIA, india);
        contentValues.put(COLUMN_CHINA, china);
        contentValues.put(COLUMN_USA, usa);
        contentValues.put(COLUMN_MACROECONOMICS, macroeconomics);
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }

    public Cursor getCountryData(String country, String macroeconomics) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_YEAR, country};
        // Sort by year
        String query = "SELECT " + COLUMN_YEAR + ", " + country + " FROM " + TABLE_NAME + " WHERE " + COLUMN_MACROECONOMICS + " = '" + macroeconomics + "' ORDER BY " + COLUMN_YEAR;
        System.out.println(query);
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }

    public Boolean isDatabaseEmpty(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_MACROECONOMICS + " = '" + tableName + "'";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() == 0) {
            return true;
        }
        return false;
    }
}
