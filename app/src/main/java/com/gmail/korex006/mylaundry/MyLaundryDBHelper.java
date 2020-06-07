package com.gmail.korex006.mylaundry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gmail.korex006.mylaundry.MyLaundryDBContract.OrdersDetailsTable;
import com.gmail.korex006.mylaundry.MyLaundryDBContract.OrdersListTable;
import com.gmail.korex006.mylaundry.MyLaundryDBContract.PeopleInfoTable;
import com.gmail.korex006.mylaundry.MyLaundryDBContract.PriceListTable;

public class MyLaundryDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "MyLaundryDB";
    private static final int DB_VERSION = 9;

    MyLaundryDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PeopleInfoTable.SQL_CREATE_TABLE);
        db.execSQL(PriceListTable.SQL_CREATE_TABLE);
        db.execSQL(OrdersListTable.SQL_CREATE_TABLE);
        db.execSQL(OrdersDetailsTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + OrdersDetailsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + OrdersListTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PeopleInfoTable.TABLE_NAME);
        db.execSQL(OrdersListTable.SQL_CREATE_TABLE);
        db.execSQL(OrdersDetailsTable.SQL_CREATE_TABLE);
        db.execSQL(PeopleInfoTable.SQL_CREATE_TABLE);
    }

    void cleanTable(SQLiteDatabase db, String tableName) {
        db.delete(tableName, null, null);
    }


}
