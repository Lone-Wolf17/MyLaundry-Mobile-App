package com.gmail.korex006.mylaundry;

import android.provider.BaseColumns;

class MyLaundryDBContract {
    private MyLaundryDBContract(){}

    // Class for People info table
    static final class PeopleInfoTable implements BaseColumns {
        static final String TABLE_NAME = "PEOPLE_INFO_TABLE";
        static final String COLUMN_PERSON_ID = "cust_id";
        static final String COLUMN_CUST_NAME = "cust_name";
        static final String COLUMN_TITLE = "title";
        static final String COLUMN_PERSON_TYPE = "person_type";
        static final String COLUMN_BRANCH = "branch";
        static final String COLUMN_CUST_TYPE = "cust_type";
        static final String COLUMN_ADDRESS = "address";
        static final String COLUMN_PHONE_NUM = "phone_num";


        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                +_ID + " INTEGER PRIMARY KEY" + ", "
                + COLUMN_PERSON_ID + " TEXT NOT NULL" + ", "
                + COLUMN_CUST_NAME + " TEXT" + ", "
                + COLUMN_TITLE + " TEXT" + ", "
                + COLUMN_PERSON_TYPE + " TEXT NOT NULL" + ", "
                + COLUMN_BRANCH + " TEXT NOT NULL" + ", "
                + COLUMN_CUST_TYPE + " TEXT" + ", "
                + COLUMN_ADDRESS + " TEXT" + ", "
                + COLUMN_PHONE_NUM + " TEXT"
                + ")";
    }

    // Class for Price List Table
    static final class PriceListTable implements BaseColumns {
        static final String TABLE_NAME = "PRICE_LIST_TABLE";
        static final String COLUMN_ITEM_NAME = "item_name";
        static final String COLUMN_DEFAULT_PRICE = "default_price";

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                +_ID + " INTEGER PRIMARY KEY" + ", "
                + COLUMN_ITEM_NAME + " TEXT NOT NULL" + ", "
                + COLUMN_DEFAULT_PRICE + " TEXT NOT NULL"
                + ")";
    }

    // Class for Orders List Table
    static final class OrdersListTable implements BaseColumns {
        static final String TABLE_NAME = "ORDERS_LIST_TABLE";
        static final String COLUMN_ORDER_ID = "order_id";
        static final String COLUMN_PERSON_ID = "cust_id";
        static final String COLUMN_PICK_UP_DATE = "pickUp_date";
        static final String COLUMN_DELIVERY_DATE = "delivery_date";
        static final String COLUMN_QTY = "total_quantity";
        static final String COLUMN_AMOUNT = "total_amount";


        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                +_ID + " INTEGER PRIMARY KEY" + ", "
                + COLUMN_ORDER_ID + " TEXT UNIQUE NOT NULL" + ", "
                + COLUMN_PERSON_ID + " TEXT NOT NULL" + ", "
                + COLUMN_PICK_UP_DATE + " TEXT NOT NULL" + ", "
                + COLUMN_DELIVERY_DATE + " TEXT NOT NULL" + ", "
                + COLUMN_QTY + " TEXT NOT NULL" + ", "
                + COLUMN_AMOUNT + " TEXT NOT NULL"
                + ")";
    }


    // Class for Orders List Table
    static final class OrdersDetailsTable implements BaseColumns {
        static final String TABLE_NAME = "ORDERS_Details_TABLE";
        static final String COLUMN_ORDER_ID = "order_id";
        static final String COLUMN_SERVICE = "service_type";
        static final String COLUMN_STARCH = "starch";
        static final String COLUMN_ITEM_TYPE = "item_type";
        static final String COLUMN_PRICE = "item_price";
        static final String COLUMN_QUANTITY = "quantity";
        static final String COLUMN_NET_PRICE = "net_price";


        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                +_ID + " INTEGER PRIMARY KEY" + ", "
                + COLUMN_ORDER_ID + " TEXT NOT NULL" + ", "
                + COLUMN_SERVICE + " TEXT NOT NULL" + ", "
                + COLUMN_STARCH+ " TEXT NOT NULL" + ", "
                + COLUMN_ITEM_TYPE + " TEXT NOT NULL" + ", "
                + COLUMN_PRICE + " TEXT NOT NULL" + ", "
                + COLUMN_QUANTITY + " TEXT NOT NULL" + ", "
                + COLUMN_NET_PRICE + " TEXT NOT NULL"
                + ")";
    }
}
