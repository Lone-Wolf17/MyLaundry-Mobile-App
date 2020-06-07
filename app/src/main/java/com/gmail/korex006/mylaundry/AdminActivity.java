package com.gmail.korex006.mylaundry;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gmail.korex006.mylaundry.MyLaundryDBContract.OrdersDetailsTable;
import com.gmail.korex006.mylaundry.MyLaundryDBContract.OrdersListTable;
import com.gmail.korex006.mylaundry.MyLaundryDBContract.PeopleInfoTable;
import com.gmail.korex006.mylaundry.MyLaundryDBContract.PriceListTable;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;


public class AdminActivity extends AppCompatActivity {
    public static final int PICK_PERSON_LIST_FILE_RESULT_CODE = 1;
    private static final int PICK_PRICE_LIST_FILE_RESULT_CODE = 2;
    // Request code for creating a PDF document.
    static final int CREATE_FILE_RESULT_CODE = 3;
    private static final int PICK_ORDER_DETAILS_FILE_RESULT_CODE = 4;

    private Uri personFileUri;
    private Uri priceFileUri;

    private TextView tv_personFilePath;
    private TextView tv_priceFilePath;
    private Uri saveFileUri;
    private boolean isFirstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        setUpUIViews();
    }

    private void setUpUIViews() {
        // set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_genToolBar);
        toolbar.setTitle("IT Admin");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // set up Drawer
        DrawerUtil.getDrawer(this, toolbar);

        tv_personFilePath = (TextView) findViewById(R.id.tv_person_file_path);
        tv_priceFilePath = (TextView) findViewById(R.id.tv_price_file_path);

    }

    private void processPersonListCSV() {
        // using tab as separator
        String cvsSplitBy = "\t";

        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
                    getContentResolver().openInputStream(personFileUri))));
            String line = "";
            MyLaundryDBHelper dbHelper = new MyLaundryDBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dbHelper.cleanTable(db, PeopleInfoTable.TABLE_NAME);
            reader.readLine(); // the first line is The table heading

            boolean isSuccessful = true;


            while ((line = reader.readLine()) != null) {
                builder.append(line);
                String[] custDetailsArr = line.split(cvsSplitBy, -1);

                // Ensure that Person ID, Person Type, Branch & Cust Type are not missing
                if (custDetailsArr[0].isEmpty() || custDetailsArr[3].isEmpty() || custDetailsArr[4].isEmpty() || custDetailsArr[5].isEmpty()) {
                    dbHelper.cleanTable(db, PeopleInfoTable.TABLE_NAME);
                    Utils.showToastMessage(this,
                            "Error: Please check csv data. Some required data are missing");
                    isSuccessful = false;
                    break;
                } else {
                    String personType = custDetailsArr[3];
                    String branch = custDetailsArr[4];
                    String custType = custDetailsArr[5];
                    String custBranch = custType.equals("MO") && personType.equals("Customer")
                            ? ("Mobile " + branch) : branch;
                    String email = custDetailsArr[7].toLowerCase();


                    int validEmailExists = Utils.validateEmail(email) ? 1 : 0;

                    ContentValues peopleInfo = new ContentValues();
                    peopleInfo.put(PeopleInfoTable.COLUMN_PERSON_ID, custDetailsArr[0]);
                    peopleInfo.put(PeopleInfoTable.COLUMN_CUST_NAME, custDetailsArr[1]);
                    peopleInfo.put(PeopleInfoTable.COLUMN_TITLE, custDetailsArr[2]);
                    peopleInfo.put(PeopleInfoTable.COLUMN_PERSON_TYPE, custDetailsArr[3]);
                    peopleInfo.put(PeopleInfoTable.COLUMN_BRANCH, custBranch);
                    peopleInfo.put(PeopleInfoTable.COLUMN_CUST_TYPE, custDetailsArr[5]);
                    peopleInfo.put(PeopleInfoTable.COLUMN_ADDRESS, custDetailsArr[6]);
                    peopleInfo.put(PeopleInfoTable.COLUMN_EMAIL, email);
                    peopleInfo.put(PeopleInfoTable.COLUMN_PHONE_NUM, custDetailsArr[8]);
                    peopleInfo.put(PeopleInfoTable.COLUMN_VALID_EMAIL_EXIST, validEmailExists);
                    long rowNum = db.insertOrThrow(PeopleInfoTable.TABLE_NAME, null, peopleInfo);
                    if (rowNum == -1) {
                        isSuccessful = false;
                        break;
                    }
                }

            }
            db.close();
            if (isSuccessful) {
                Utils.showToastMessage(this, "People Database Update successful");
            }


        } catch (IOException e) {
                e.printStackTrace();
                Utils.showToastMessage(this, "Error!!!");
            } finally {
                if (reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
      }


    public void onUpdatePeopleDBClick(View view) {
        if (personFileUri != null) {
            processPersonListCSV();
        } else {
            Utils.showToastMessage(this, "Please select a file first !!");
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            switch (requestCode) {
                case PICK_PERSON_LIST_FILE_RESULT_CODE: {
                    personFileUri = data.getData();
                    assert personFileUri != null;
                    tv_personFilePath.setText(personFileUri.getLastPathSegment());
                    break;
                }
                case PICK_PRICE_LIST_FILE_RESULT_CODE : {
                    priceFileUri = data.getData();
                    assert personFileUri != null;
                    tv_priceFilePath.setText(priceFileUri.getLastPathSegment());
                    break;
                }
                case PICK_ORDER_DETAILS_FILE_RESULT_CODE: // (*) grouped two cases
                case CREATE_FILE_RESULT_CODE:
                    saveFileUri = data.getData();
                    extractOrderDbCsv();
                    break;
            }
        }

    }

    public void onSelectFileClick(View view) {
        int id = view.getId();
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        if (id == R.id.tv_person_file_path) {
            startActivityForResult(chooseFile, PICK_PERSON_LIST_FILE_RESULT_CODE);
        }
        else if (id == R.id.tv_price_file_path) {
            startActivityForResult(chooseFile, PICK_PRICE_LIST_FILE_RESULT_CODE);
        }
        else if (id == R.id.btn_extractOrderDetails) {
            startActivityForResult(chooseFile, PICK_ORDER_DETAILS_FILE_RESULT_CODE);
        }

    }

    public void onUpdatePriceListClick(View view) {
        if (priceFileUri != null) {
            processPriceListCSV();
        } else {
            Utils.showToastMessage(this, "Please select a file first!!");
        }
    }

    private void processPriceListCSV() {
        String cvsSplitBy = "\t";

        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getContentResolver().openInputStream(priceFileUri))));
            String line = "";
            MyLaundryDBHelper dbHelper = new MyLaundryDBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dbHelper.cleanTable(db, PriceListTable.TABLE_NAME);
            reader.readLine(); // the first line is The table heading


            while ((line = reader.readLine()) != null) {
                builder.append(line);

                // use comma as separator
                String[] item = line.split(cvsSplitBy, -1);
                if (item[0].isEmpty() || item[1].isEmpty()) {
                    dbHelper.cleanTable(db, PriceListTable.TABLE_NAME);
                    Utils.showToastMessage(this,
                            "Error: Please check csv data. Some required data are missing");
                    break;
                } else {
                    ContentValues peopleInfo = new ContentValues();
                    peopleInfo.put(PriceListTable.COLUMN_ITEM_NAME, item[0]);
                    peopleInfo.put(PriceListTable.COLUMN_DEFAULT_PRICE, item[1]);
                    db.insert(PriceListTable.TABLE_NAME, null, peopleInfo);
                }

            }
            db.close();

            // repopulate the virtual table used for searching in the PriceList Activity
            new MyLaundryVirtualTables(this).getDbOpenHelper().repopulateTables(null);
            Utils.showToastMessage(this, "Price List Update successful");

        } catch (IOException e) {
            e.printStackTrace();
            Utils.showToastMessage(this, "An Error has occurred!!!");
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void extractOrderDbCsv() {
        SQLiteDatabase db = new MyLaundryDBHelper(this).getReadableDatabase();

        Cursor cursor = db.query(OrdersListTable.TABLE_NAME, null, null, null,
                null, null, null);

        if (cursor.getCount() != 0) {
            HashMap<String,String> priceDict = retrievePriceListFromDB();
            String orderDetails = OrdersDetailsTable.TABLE_NAME;
            String orderList = OrdersListTable.TABLE_NAME;
            String custList = PeopleInfoTable.TABLE_NAME;
            String query = "SELECT " + orderList + "." + OrdersListTable.COLUMN_PERSON_ID + ", "
                    + custList + "." + PeopleInfoTable.COLUMN_CUST_NAME + ", "
                    + orderList + "." + OrdersListTable.COLUMN_PICK_UP_DATE + ", "
                    + orderDetails + "." + OrdersDetailsTable.COLUMN_SERVICE + ", "
                    + orderDetails + "." + OrdersDetailsTable.COLUMN_ITEM_TYPE + ", "
                    + orderDetails + "." + OrdersDetailsTable.COLUMN_QUANTITY + ", "
                    + orderDetails + "." + OrdersDetailsTable.COLUMN_PRICE + ", "
                    + orderDetails + "." + OrdersDetailsTable.COLUMN_NET_PRICE + ", "
                    + orderList + "." + OrdersListTable.COLUMN_DELIVERY_DATE

                    + " FROM " + orderDetails
                    + " INNER JOIN " + orderList + " USING (" + OrdersListTable.COLUMN_ORDER_ID + ")"
                    + " INNER JOIN " + custList + " USING (" + OrdersListTable.COLUMN_PERSON_ID + ")"
                    ;
            cursor = db.rawQuery(query, null);

            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()) {
                int custIdPos = cursor.getColumnIndex(OrdersListTable.COLUMN_PERSON_ID);
                int custNamePos = cursor.getColumnIndex(PeopleInfoTable.COLUMN_CUST_NAME);
                int pickUpDatePos = cursor.getColumnIndex(OrdersListTable.COLUMN_PICK_UP_DATE);
                int servicePos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_SERVICE);
                int itemTypePos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_ITEM_TYPE);
                int quatityPos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_QUANTITY);
                int pricePos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_PRICE);
                int netPricePos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_NET_PRICE);
                int deliveryDatePos = cursor.getColumnIndex(OrdersListTable.COLUMN_DELIVERY_DATE);

                String custId = cursor.getString(custIdPos);
                String custName = cursor.getString(custNamePos);
                String pickUpDate = cursor.getString(pickUpDatePos);
                String service = cursor.getString(servicePos);
                String itemType = cursor.getString(itemTypePos);
                String quantity = cursor.getString(quatityPos);
                String price = cursor.getString(pricePos);
                int netPrice =  Integer.parseInt(cursor.getString(netPricePos));
                String deliveryDate = cursor.getString(deliveryDatePos);

                String[] serviceAndDes = getServiceAndDes(service);
                service = serviceAndDes[0];
                String desciption = serviceAndDes[1];

                String defaultPrice = priceDict.get(itemType);
                assert defaultPrice != null;
                int idealNetPrice = Integer.parseInt(defaultPrice) * Integer.parseInt(quantity);
                String discountOrPremium = "";
                if (idealNetPrice != netPrice) {
                    discountOrPremium = String.valueOf(netPrice-idealNetPrice);
                }

                builder.append(custId); builder.append(",");
                builder.append(custName); builder.append(",");
                builder.append(pickUpDate); builder.append(",");
                builder.append(service); builder.append(",");
                builder.append(itemType); builder.append(",");
                builder.append(desciption); builder.append(",");
                builder.append(quantity); builder.append(",");
                builder.append("5/1/2020"); builder.append(",");
                builder.append(defaultPrice); builder.append(",");
                builder.append(discountOrPremium); builder.append(",");
                builder.append(netPrice); builder.append(",");
                builder.append(deliveryDate); builder.append(",");
                builder.append(pickUpDate); builder.append("\n");
            }

            String orderCsv = builder.toString();
            saveOrderDocument(saveFileUri, orderCsv);


        } else {
            Utils.showToastMessage(this, "No records found!! ");
        }

        cursor.close();
    }

    private String[] getServiceAndDes(String service) {
        String serviceType = "Normal Laundry";
        String description = "coloured";
//        final String NI = getString(R.string.NI);
//        String EL = getString(R.string.EL);
//        String EI = getString(R.string.EI);
//        String WhiteEL = getString(R.string.WhiteEL);
//        String WhiteNL = getString(R.string.WhiteNL);

        switch (service) {
            case "NI":
                serviceType = "Normal Ironing";
                description = "";
                break;
            case "EL":
                serviceType = "Express Laundry";
                break;
            case "EI":
                serviceType = "Express Ironing";
                description = "";
                break;
            case "WhiteEL":
                serviceType = "Express Laundry";
                description = "White";
                break;
            case "WhiteNL":
                description = "White";
                break;
        }
        return new String[]{serviceType, description};
    }

    private HashMap<String, String> retrievePriceListFromDB() {
        SQLiteDatabase db = new MyLaundryDBHelper(this).getReadableDatabase();

        String[] columns = {
                PriceListTable.COLUMN_ITEM_NAME,
                PriceListTable.COLUMN_DEFAULT_PRICE,
        };

        Cursor cursor = db.query(PriceListTable.TABLE_NAME, columns, null,
                null, null, null, null);
        int itemNamePos = cursor.getColumnIndex(PriceListTable.COLUMN_ITEM_NAME);
        int itemDefaultPricePos = cursor.getColumnIndex(PriceListTable.COLUMN_DEFAULT_PRICE);

        HashMap<String, String> priceDict = new HashMap<String, String>();
        while (cursor.moveToNext()) {
            String itemName = cursor.getString(itemNamePos);
            String itemDefaultPrice = cursor.getString(itemDefaultPricePos);

            priceDict.put(itemName, itemDefaultPrice);
        }
        cursor.close();
        return priceDict;

    }

    boolean checkFirstRun() {

        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_KEY_CODE = "version_code";
        final int DOESNT_EXIST = -1;

        // Get the current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_KEY_CODE, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            // this is a normal run
            return false;
        } else {
            if (savedVersionCode == DOESNT_EXIST) {
                // TODO This is a new install (or the user cleared the shared preferences)

            } else if (currentVersionCode > savedVersionCode) {
                // TODO This is an upgrade

            }

            // Update the shared preferences with the current version code
            prefs.edit().putInt(PREF_VERSION_KEY_CODE, currentVersionCode).apply();
            return true;

        }
    }

    void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "orderDetails.csv");

        startActivityForResult(intent, CREATE_FILE_RESULT_CODE);
    }

    private void saveOrderDocument(Uri uri, String orderCsv) {
        try {
            ParcelFileDescriptor pfd = getContentResolver().
                    openFileDescriptor(uri, "w");
            assert pfd != null;
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(orderCsv.getBytes());
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            pfd.close();
            new UtilsDialog(this).showOrdersDetailsExtractedSucessfullyDialog();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onExtractOrderDetailsClick(View view) {
        isFirstRun = checkFirstRun();
        if (isFirstRun) {
            new UtilsDialog(this).showCreateOrderDetailsFileDialog(this);
        } else {
            onSelectFileClick(view);
        }
    }

    public void onResetEmailLoginClick(View view) {
        new UtilsDialog(this).resetEmailLoginDialog();
    }
}
