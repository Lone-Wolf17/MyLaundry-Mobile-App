package com.gmail.korex006.mylaundry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import com.gmail.korex006.mylaundry.MyLaundryDBContract.PriceListTable;

public class MyLaundryVirtualTables implements BaseColumns {
    private static final String TAG = "VirtualDB";

    //The columns we'll include in the dictionary table
    static final String COL_ITEM_NAME = "item_name";
    static final String COL_PRICE = "default_price";

    private static final String DB_NAME = "MyLaundry";
    private static final String FTS_VIRTUAL_TABLE = "FTS";
    private static final int DB_VERSION = 3;

    private final DBOpenHelper dbOpenHelper;

    public DBOpenHelper getDbOpenHelper() {
        return dbOpenHelper;
    }

    MyLaundryVirtualTables(Context context) {
        dbOpenHelper = new DBOpenHelper(context);
    }

    Cursor getItemMatches(String query, String[] columns) {
        String selection = COL_ITEM_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};
        return query(selection, selectionArgs, columns);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);

        Cursor cursor = builder.query(dbOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

     static class DBOpenHelper extends SQLiteOpenHelper {

        private final Context helperContext;
        private SQLiteDatabase mDB;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        _ID + "," +
                        COL_ITEM_NAME + ", " +
                        COL_PRICE + ")";

        DBOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            helperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDB = db;
            mDB.execSQL(FTS_TABLE_CREATE);
            loadPriceList();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            repopulateTables(db);
        }

         void repopulateTables(SQLiteDatabase db) {
            if (db == null) {
                db = getWritableDatabase();
            }
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }

        private void loadPriceList() {
            new Thread(new Runnable() {
                public void run() {
                    loadItems();
                }
            }).start();
        }

        private void loadItems() {
            SQLiteDatabase db = new MyLaundryDBHelper(helperContext).getReadableDatabase();
            Cursor cursor = db.query(PriceListTable.TABLE_NAME, null, null, null,
                    null, null, null);

            while (cursor.moveToNext()) {
                String item = cursor.getString(cursor.getColumnIndex(PriceListTable.COLUMN_ITEM_NAME));
                String price = cursor.getString(cursor.getColumnIndex(PriceListTable.COLUMN_DEFAULT_PRICE));
                long id = addItem(item, price);
                if (id < 0) {
                    Log.e(TAG, "unable to add item: " + item);
                }
            }

        }

        private long addItem(String item, String price) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_ITEM_NAME, item);
            initialValues.put(COL_PRICE, price);

            return mDB.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }
    }
}
