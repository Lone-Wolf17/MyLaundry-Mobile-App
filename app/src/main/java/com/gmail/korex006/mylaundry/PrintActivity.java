package com.gmail.korex006.mylaundry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import android.app.AlertDialog.Builder;
import android.bluetooth.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.gmail.korex006.mylaundry.MyLaundryDBContract.OrdersDetailsTable;
import com.gmail.korex006.mylaundry.MyLaundryDBContract.OrdersListTable;
import com.gmail.korex006.mylaundry.MyLaundryDBContract.PeopleInfoTable;


public class PrintActivity extends AppCompatActivity {

    public static final String ORDER_ID = "order_id";

    private Button btn_printJobCard =null;
    private Button btn_viewJobCard =null;
    private Button buttonCut=null;
    private Button buttonConnect=null;

    private TextView mprintfLog=null;
    private TextView mTipTextView=null;

    private Spinner mSpinner=null;
    private List<String>mpairedDeviceList=new ArrayList<String>();

    private BluetoothAdapter mBluetoothAdapter = null;   //��������������
    private BluetoothSocket mBluetoothSocket=null;
    OutputStream mOutputStream=null;
    /*Hint: If you are connecting to a Bluetooth serial board then try using
     * the well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However
     * if you are connecting to an Android peer then please generate your own unique UUID.*/
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Builder dialog=null;

    Set<BluetoothDevice>pairedDevices=null;
    private String jobCardStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        this.setTitle(this.getTitle()+"(C)posPrinter");

        setUpUIViews();
        setUPViewsListeners();
        getOrderDetails();
    }

    private void getOrderDetails() {
        String orderId = getIntent().getStringExtra(ORDER_ID);

        SQLiteDatabase db = new MyLaundryDBHelper(this).getReadableDatabase();
//        String selection = OrdersListTable.COLUMN_ORDER_ID + " =?";
//        String[] selectionArgs = {orderId};
        String orderDetails = OrdersDetailsTable.TABLE_NAME;
        String orderList = OrdersListTable.TABLE_NAME;
        String custList = PeopleInfoTable.TABLE_NAME;

        String query = "SELECT " + orderList + "." + OrdersListTable.COLUMN_PERSON_ID + ", "
                + custList + "." + PeopleInfoTable.COLUMN_CUST_NAME + ", "
                + custList + "." + PeopleInfoTable.COLUMN_BRANCH + ", "
                + orderList + "." + OrdersListTable.COLUMN_PICK_UP_DATE + ", "
                + orderDetails + "." + OrdersDetailsTable.COLUMN_SERVICE + ", "
                + orderDetails + "." + OrdersDetailsTable.COLUMN_STARCH+ ", "
                + orderDetails + "." + OrdersDetailsTable.COLUMN_ITEM_TYPE + ", "
                + orderDetails + "." + OrdersDetailsTable.COLUMN_QUANTITY + ", "
                + orderDetails + "." + OrdersDetailsTable.COLUMN_PRICE + ", "
                + orderDetails + "." + OrdersDetailsTable.COLUMN_NET_PRICE + ", "
                + orderList + "." + OrdersListTable.COLUMN_DELIVERY_DATE

                + " FROM " + orderDetails
                + " INNER JOIN " + orderList + " USING (" + OrdersListTable.COLUMN_ORDER_ID + ")"
                + " INNER JOIN " + custList + " USING (" + OrdersListTable.COLUMN_PERSON_ID + ")"
                + " WHERE " + OrdersDetailsTable.COLUMN_ORDER_ID + " = '" + orderId + "'" ;

        Cursor cursor = db.rawQuery(query, null);

        StringBuilder builder = new StringBuilder();
        boolean isHeadingCreated = false;
        int totalQty = 0;
        int totalAmt = 0;
        while (cursor.moveToNext()) {
            int custIdPos = cursor.getColumnIndex(OrdersListTable.COLUMN_PERSON_ID);
            int custNamePos = cursor.getColumnIndex(PeopleInfoTable.COLUMN_CUST_NAME);
            int branchPos = cursor.getColumnIndex(PeopleInfoTable.COLUMN_BRANCH);
            int pickUpDatePos = cursor.getColumnIndex(OrdersListTable.COLUMN_PICK_UP_DATE);
            int servicePos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_SERVICE);
            int itemTypePos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_ITEM_TYPE);
            int quatityPos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_QUANTITY);
            int pricePos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_PRICE);
            int netPricePos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_NET_PRICE);
            int deliveryDatePos = cursor.getColumnIndex(OrdersListTable.COLUMN_DELIVERY_DATE);

            String custId = cursor.getString(custIdPos);
            String custName = cursor.getString(custNamePos);
            String branch = cursor.getString(branchPos);
            String pickUpDate = cursor.getString(pickUpDatePos);
            String service = cursor.getString(servicePos);
            String starch = cursor.getString(cursor.getColumnIndex(OrdersDetailsTable.COLUMN_STARCH));
            String itemType = cursor.getString(itemTypePos);
            int quantity = Integer.parseInt(cursor.getString(quatityPos));
            totalQty += quantity;
            String price = cursor.getString(pricePos);
            int netPrice =  Integer.parseInt(cursor.getString(netPricePos));
            totalAmt += netPrice;
            String deliveryDate = cursor.getString(deliveryDatePos);

            if (!isHeadingCreated) {
                builder.append("\n\tMyLaundry.NG\n"); builder.append("Cust ID : " + custId + "\n");
                builder.append("Cust Name: " + custName + "\n");
                builder.append("Branch: " + branch + "\n");
                builder.append("Pickup date: " + pickUpDate + "\n");
                builder.append("Delivery date: " + deliveryDate + "\n\n");
                builder.append("Item\tService\tStarch\tPrice\tQty\tNetPrice\n\n");
                isHeadingCreated = true;
            }

            builder.append(itemType + " "); builder.append(service + " ");
            builder.append(starch + " "); builder.append(price + " ");
            builder.append(quantity + " "); builder.append(netPrice + " ");
            builder.append("\n\n");
        }
        cursor.close();

        builder.append("Total Qty: " + totalQty + "\n");
        builder.append("Total Amount: N " + totalAmt + "\n");
        builder.append("Thank you for choosing \nMyLaundry.NG\n\n");
        jobCardStr = builder.toString();
    }

    private void setUpUIViews () {
        btn_printJobCard =(Button)findViewById(R.id.btn_printJobCard);
        btn_viewJobCard =(Button)findViewById(R.id.btn_viewJobCard);
        buttonCut=(Button)findViewById(R.id.ButtonCutPaper);
        buttonConnect=(Button)findViewById(R.id.btn_Connect);

        mSpinner=(Spinner)findViewById(R.id.deviceSpinner);

        mprintfLog=(TextView)findViewById(R.id.TextLogs);
        mTipTextView=(TextView)findViewById(R.id.textTip);

        // Set up bluetooth connect dialog
        dialog=new Builder(this);
        dialog.setTitle("posPrinter hint:");
        dialog.setMessage(getString(R.string.XPrinterhint));
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                //finish();
            }
        });

        dialog.setNeutralButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                //finish();
            }
        });

        // set up spinner adapter
        mpairedDeviceList.add(this.getString(R.string.PlsChoiceDevice));
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, mpairedDeviceList);
        mSpinner.setAdapter(mArrayAdapter);


    }

    private void setUPViewsListeners() {
        // set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_genToolBar);
        toolbar.setTitle("Print Job Card");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        // Set button Listeners
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        ButtonListener buttonListener=new ButtonListener();
        btn_printJobCard.setOnClickListener(buttonListener);
        btn_viewJobCard.setOnClickListener(buttonListener);
        buttonCut.setOnClickListener(buttonListener);
        buttonConnect.setOnClickListener(buttonListener);

        setButtonEnable(false);

        mSpinner.setOnTouchListener(new Spinner.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction()!=MotionEvent.ACTION_UP) {
                    return false;
                }
                try {
                    if (mBluetoothAdapter==null) {
                        mTipTextView.setText(getString(R.string.NotBluetoothAdapter));
                        PrintfLogs(getString(R.string.NotBluetoothAdapter));
                    }
                    else if (mBluetoothAdapter.isEnabled()) {
                        String getName=mBluetoothAdapter.getName();
                        pairedDevices=mBluetoothAdapter.getBondedDevices();
                        while (mpairedDeviceList.size()>1) {
                            mpairedDeviceList.remove(1);
                        }
                        if (pairedDevices.size()==0) {
                            dialog.create().show();
                        }
                        for (BluetoothDevice device : pairedDevices) {
                            // Add the name and address to an array adapter to show in a ListView
                            getName= device.getName() + "#" + device.getAddress();
                            mpairedDeviceList.add(getName);
                        }
                    }
                    else {
                        PrintfLogs("BluetoothAdapter not open...");
                        dialog.create().show();
                    }
                }
                catch (Exception e) {
                    // TODO: handle exception
                    Utils.showToastMessage(PrintActivity.this, e.toString());
                }
                return false;
            }
        });
    }

    private void PrintfLogs(String logs){
        this.mprintfLog.setText(logs);
    }

    private void setButtonEnable(boolean state) {
        btn_printJobCard.setEnabled(state);
        buttonCut.setEnabled(state);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SearchActivity.class));
        finish();
    }

    class ButtonListener implements OnClickListener{

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_Connect:{
                    String temString=(String) mSpinner.getSelectedItem();
                    if (mSpinner.getSelectedItemId()!=0) {
                        if (buttonConnect.getText()!=getString(R.string.Disconnected)) {
                            try {
                                mOutputStream.close();
                                mBluetoothSocket.close();
                                buttonConnect.setText(getString(R.string.Disconnected));
                                setButtonEnable(false);
                            } catch (Exception e) {
                                // TODO: handle exception
                                PrintfLogs(e.toString());
                            }
                            return;
                        }
                        temString=temString.substring(temString.length()-17);
                        try {
                            buttonConnect.setText(getString(R.string.Connecting));
                            BluetoothDevice mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(temString);
                            mBluetoothSocket= mBluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID);
                            mBluetoothSocket.connect();
                            buttonConnect.setText(getString(R.string.Connected));
                            setButtonEnable(true);
                        } catch (Exception e) {
                            // TODO: handle exception
                            PrintfLogs(getString(R.string.Disconnected));
                            buttonConnect.setText(getString(R.string.Disconnected));
                            setButtonEnable(false);
                            PrintfLogs(getString(R.string.ConnectFailed)+e.toString());
                        }

                    }
                    else {
                        PrintfLogs("Pls select a bluetooth device...");
                    }
                }
                break;
                case R.id.btn_printJobCard:{
                    try {
                        if (jobCardStr.length()==0) {
                            PrintfLogs("Pls input print data...");
                        } else {
                            mOutputStream=mBluetoothSocket.getOutputStream();
                            mOutputStream.write((jobCardStr+"\n").getBytes("GBK"));
                            mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                            mOutputStream.flush();
                            PrintfLogs("Data sent successfully...");
                        }

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        PrintfLogs(getString(R.string.PrintFaild)+e.getMessage());
                    }


                }
                break;
                case R.id.btn_viewJobCard:{
                    if (jobCardStr.length()==0) {
                        PrintfLogs("Pls input print data...");
                    } else {
                        UtilsDialog utilsDialog = new UtilsDialog(PrintActivity.this);
                        utilsDialog.showJobCardDialog(jobCardStr);
                    }

                }
                break;
                case R.id.ButtonCutPaper:{
                    try {
                        if (jobCardStr.length()==0) {
                            PrintfLogs("Pls input print data...");
                        } else {
                            mOutputStream=mBluetoothSocket.getOutputStream();
                            mOutputStream.write(new byte[]{0x0a,0x0a,0x1d,0x56,0x01});
                            mOutputStream.flush();
                        }

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        PrintfLogs(getString(R.string.CutPaperFaild)+e.getMessage());
                    }

                }
                break;
                default:
                    break;
            }

        }

    }

}
