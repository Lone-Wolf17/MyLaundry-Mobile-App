package com.gmail.korex006.mylaundry;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.gmail.korex006.mylaundry.MyLaundryDBContract.OrdersDetailsTable;
import com.gmail.korex006.mylaundry.MyLaundryDBContract.OrdersListTable;
import com.gmail.korex006.mylaundry.MyLaundryDBContract.PeopleInfoTable;
import com.gmail.korex006.mylaundry.MyLaundryDBContract.PriceListTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class LaundryOrderActivity extends AppCompatActivity {

    public static final String EXTRA_PERSON_ID = "_ID";
    public static final String EXTRA_ORDER_ID = "order_id";
    private TextView tv_pickUpDate;
    private  TextView tv_deliveryDate;
    private UtilsDialog utilsDialog;
    private HashMap<String, String> priceDict;
    private LinearLayout linearLayout;
    private TextView tv_personId;
    private String orderId;
    private TextView tv_priceTotal;
    private TextView tv_quantityTotal;
    private boolean isEditOrder;
    private String orderIdBeingEdited;

    public void setPickUpDate(Calendar newPickUpDate) {

        pickUpDate = newPickUpDate;


        deliveryDate = (Calendar) pickUpDate.clone();


        // Set Delivery date to 3 days later
        int numofDays = Utils.calcNumofDeliveryDays(pickUpDate);
//        int dayofWeek = pickUpDate.get(Calendar.DAY_OF_WEEK);
//        switch (dayofWeek) {
//            case 7: numofDays = 4; // Saturday
//            case 6: numofDays = 5; // Friday deliver on Wednesday
//            case 5: numofDays = 5; // Thursday deliver on Tuesday
//            default: numofDays = 3;
//
//        }
//                    val delDate = date.clone()
        deliveryDate.add(Calendar.DAY_OF_YEAR, numofDays);

//        deliveryDate.add(Calendar.DAY_OF_YEAR, 3);
//        TextView tv_pickUpDate = findViewById(R.id.tv_pickUpDate);
//        Utils.updateDateText(pickUpDate, tv_pickUpDate);
        setDeliveryDate(deliveryDate);

    }

    public void setDeliveryDate(Calendar deliveryDate) {
        LaundryOrderActivity.deliveryDate = deliveryDate;
//        TextView tv_deliveryDate = findViewById(R.id.tv_pickUpDate);
//        Utils.updateDateText(deliveryDate, tv_deliveryDate);
    }

    private static Calendar pickUpDate;
    private static Calendar deliveryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laundry_order);
        setUpUIViews();


    }

    private void setUpUIViews() {

        // set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_genToolBar);
        toolbar.setTitle("Add Laundry Order");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);


        utilsDialog = new UtilsDialog(this);
        tv_pickUpDate = (TextView) findViewById(R.id.tv_pickUpDate);
        tv_deliveryDate = (TextView) findViewById(R.id.tv_deliveryDate);
        tv_pickUpDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setOrderId();
            }
        });
        tv_deliveryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setOrderId();
            }
        });
        pickUpDate = Calendar.getInstance();

        linearLayout = (LinearLayout) findViewById(R.id.linear_items);

        // Retrieve price List from DB
        priceDict = retrievePriceListFromDB();

        if (getIntent().hasExtra(EXTRA_PERSON_ID)) {
            String personId =  getIntent().getStringExtra(EXTRA_PERSON_ID);
            setUpNewOrder(personId);
        } else if (getIntent().hasExtra(EXTRA_ORDER_ID)) {
            isEditOrder = true;
            orderIdBeingEdited = getIntent().getStringExtra(EXTRA_ORDER_ID);
            setUpEditOrder(Objects.requireNonNull(orderIdBeingEdited));
        }
        Utils.updateDateText(pickUpDate, tv_pickUpDate);
        Utils.updateDateText(deliveryDate, tv_deliveryDate);
    }

    private void setUpEditOrder(String orderIdBeingEdited) {
        String[] orderIdSplit = orderIdBeingEdited.split("_");
        String personId = orderIdSplit[0];
        slotInCustDetails(personId);

        String[] pickUpDateSplit = orderIdSplit[1].split("-");
        pickUpDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(pickUpDateSplit[0]));
        pickUpDate.set(Calendar.MONTH, Integer.parseInt(pickUpDateSplit[1])-1);
        pickUpDate.set(Calendar.YEAR, Integer.parseInt(pickUpDateSplit[2]));
        setPickUpDate(pickUpDate);

        String[] deliveryDateSplit = orderIdSplit[2].split("-");
        deliveryDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(deliveryDateSplit[0]));
        deliveryDate.set(Calendar.MONTH, Integer.parseInt(deliveryDateSplit[1])-1);
        deliveryDate.set(Calendar.YEAR, Integer.parseInt(deliveryDateSplit[2]));
//        setDeliveryDate(deliveryDate);

        String selection = OrdersDetailsTable.COLUMN_ORDER_ID + " = ?";
        String[] selectionArgs = {orderIdBeingEdited};
        SQLiteDatabase db = new MyLaundryDBHelper(this).getReadableDatabase();
        Cursor cursor = db.query(OrdersDetailsTable.TABLE_NAME, null, selection, selectionArgs,
                null, null, null);
        while (cursor.moveToNext()) {
            CardView itemCard = inflateCourseCardView();
            final Spinner spn_items = (Spinner) itemCard.findViewById(R.id.spn_item);
            Spinner spn_services = (Spinner) itemCard.findViewById(R.id.spn_service);
            Spinner spn_packaging = (Spinner) itemCard.findViewById(R.id.spn_packaging);
            Spinner spn_starch = (Spinner) itemCard.findViewById(R.id.spn_starch);
            final EditText et_price = (EditText) itemCard.findViewById(R.id.et_itemPrice);
//            final TextView tv_netPrice = (TextView) itemCard.findViewById(R.id.tv_netPrice);
            final EditText et_quantity = (EditText) itemCard.findViewById(R.id.et_quantity);

            final List<String> itemsArray = new ArrayList<>(priceDict.keySet());
            final List<String> servicesArr = Arrays.asList(getResources().getStringArray(R.array.services));
            final List<String> packagingArr = Arrays.asList(getResources().getStringArray(R.array.packaging));
            final List<String> starchArr = Arrays.asList(getResources().getStringArray(R.array.starch));

            String itemType = cursor.getString(cursor.getColumnIndex(OrdersDetailsTable.COLUMN_ITEM_TYPE));
            String service = cursor.getString(cursor.getColumnIndex(OrdersDetailsTable.COLUMN_SERVICE));
            String packaging = cursor.getString(cursor.getColumnIndex(OrdersDetailsTable.COLUMN_PACKAGING));
            String starch = cursor.getString(cursor.getColumnIndex(OrdersDetailsTable.COLUMN_STARCH));
            String price = cursor.getString(cursor.getColumnIndex(OrdersDetailsTable.COLUMN_PRICE));
            String qty = cursor.getString(cursor.getColumnIndex(OrdersDetailsTable.COLUMN_QUANTITY));
//            String netPrice = cursor.getString(cursor.getColumnIndex(OrdersDetailsTable.COLUMN_NET_PRICE));

            spn_items.setSelection(itemsArray.indexOf(itemType));
            spn_services.setSelection(servicesArr.indexOf(service));
            spn_packaging.setSelection(packagingArr.indexOf(packaging));
            spn_starch.setSelection(starchArr.indexOf(starch));
            et_price.setText(price);
            et_quantity.setText(qty);
            linearLayout.addView(itemCard);
//            tv_netPrice.setText(netPrice);
        }
        cursor.close();
    }

    private void setUpNewOrder(String personId) {
        slotInCustDetails(personId);
        pickUpDate.setTimeInMillis(System.currentTimeMillis());
        setPickUpDate(pickUpDate);
    }

    private void setOrderId() {
        TextView tv_custName = (TextView) findViewById(R.id.tv_custName);
        String custName = tv_custName.getText().toString();
        String personId = tv_personId.getText().toString();
        String pickUpDateStr = Utils.formatDate(pickUpDate);
//                tv_pickUpDate.getText().toString();
        String deliveryDateStr = Utils.formatDate(deliveryDate);
//                tv_deliveryDate.getText().toString();
        orderId = personId + "_" + pickUpDateStr + "_" + deliveryDateStr;

        Button btn_addItem = findViewById(R.id.btn_addItem);
        Button btn_saveOrder = findViewById(R.id.btn_saveOrder);

        if (!allowSaveOrder()) {
            btn_addItem.setEnabled(false);
            btn_saveOrder.setEnabled(false);
            utilsDialog.showOrderExistsDialog(orderId, custName);
        } else {
            // enable the buttons if allowSaveOrder() returns true
            btn_addItem.setEnabled(true);
            btn_saveOrder.setEnabled(true);
        }

    }

    private boolean allowSaveOrder() {
        boolean result ;
        TextView tv_custName =  (TextView) findViewById(R.id.tv_custName);
        String custName = tv_custName.getText().toString();

        SQLiteDatabase db = new MyLaundryDBHelper(this).getReadableDatabase();
        String selection = OrdersListTable.COLUMN_ORDER_ID + "= ?";
        String[] selectionArgs = {orderId};
        Cursor cursor = db.query(OrdersListTable.TABLE_NAME, null, selection, selectionArgs,
                null, null, null);
        if (cursor.getCount() != 0) {
            //                utilsDialog.showOrderExistsDialog(orderId, custName);
            result = isEditOrder && orderId.equals(orderIdBeingEdited);

        } else {
            result = true;
        }
        cursor.close();
        return result;

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

    private void slotInCustDetails(String personId) {
        tv_personId = (TextView) findViewById(R.id.tv_personID);
        TextView tv_name = (TextView) findViewById(R.id.tv_custName);
        TextView tv_branch = (TextView) findViewById(R.id.tv_branch);
        SQLiteDatabase db = new MyLaundryDBHelper(this).getReadableDatabase();

        String[] columns = {
                PeopleInfoTable.COLUMN_PERSON_ID,
                PeopleInfoTable.COLUMN_CUST_NAME,
                PeopleInfoTable.COLUMN_PERSON_TYPE,
                PeopleInfoTable.COLUMN_BRANCH,
                PeopleInfoTable.COLUMN_CUST_TYPE,
                PeopleInfoTable.COLUMN_ADDRESS,
                PeopleInfoTable.COLUMN_PHONE_NUM
        };
        String selection = PeopleInfoTable.COLUMN_PERSON_ID + " =?";
        String[] selectionArgs = {personId};

        Cursor cursor = db.query(PeopleInfoTable.TABLE_NAME, columns, selection, selectionArgs,
                null, null, null);
//        int personIdPos = cursor.getColumnIndex(PeopleInfoTable.COLUMN_PERSON_ID);
        int namePos = cursor.getColumnIndex(PeopleInfoTable.COLUMN_CUST_NAME);
        int branchPos = cursor.getColumnIndex(PeopleInfoTable.COLUMN_BRANCH);
//        int custTypePos = cursor.getColumnIndex(PeopleInfoTable.COLUMN_CUST_TYPE);

        if (cursor.getCount() == 1) {
            cursor.moveToNext();
//            String personId = cursor.getString(personIdPos);
            String name = cursor.getString(namePos);
            String branch = cursor.getString(branchPos);

            tv_name.setText(name);
            tv_personId.setText(personId);
            tv_branch.setText(branch);

        }
        cursor.close();
    }


    public void onDateClick(View view) {
        utilsDialog.showDateDialog(view);
    }

    public void onAddItemClick(View view) {

        CardView v = inflateCourseCardView();
        linearLayout.addView(v);
        final ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_items);
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 700);
    }

    private CardView inflateCourseCardView() {
        LayoutInflater vi = (LayoutInflater) getLayoutInflater();
        CardView v = (CardView) vi.inflate(R.layout.item_view_layout, null);
        final List<String> keys = new ArrayList<>(priceDict.keySet());
        final Spinner spn_items = (Spinner) v.findViewById(R.id.spn_item);
        Spinner spn_services = (Spinner) v.findViewById(R.id.spn_service);
        final EditText et_price = (EditText) v.findViewById(R.id.et_itemPrice);
        final TextView tv_netPrice = (TextView) v.findViewById(R.id.tv_netPrice);
        final EditText et_quantity = (EditText) v.findViewById(R.id.et_quantity);
        final ImageButton iv_delete = (ImageButton) v.findViewById(R.id.ib_delete);

//        iv_delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CardView parentCardView = (CardView) ((View) iv_delete.getParent()).getParent();
//                linearLayout.removeView(parentCardView);
//                reCalcTotals();
//
//            }
//        });

        ArrayAdapter<String> itemsSpnAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item,
                keys);

        final String[] servicesArr = getResources().getStringArray(R.array.services);
        ArrayAdapter<String> serviceSpnAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item,
                servicesArr);

        spn_items.setAdapter(itemsSpnAdapter);
        spn_services.setAdapter(serviceSpnAdapter);

        spn_items.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String price = priceDict.get(keys.get(position));
                et_price.setText(price);
//                String quantity = et_quantity.getText().toString();
                reCalcNetPrice(tv_netPrice, et_price, et_quantity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spn_services.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemType = (String) spn_items.getSelectedItem();
                int price = Integer.parseInt(Objects.requireNonNull(priceDict.get(selectedItemType)));
                String selectedService = servicesArr[position];
                int servicePrice = price;
                if (selectedService.equals(getString(R.string.EL))){
                    servicePrice = price*2;
                } else if (selectedService.equals(getString(R.string.NI))){
                    servicePrice = (int) (price*0.5);
                } else if (selectedService.equals(getString(R.string.WhiteNL))) {
                    servicePrice = price+100;
                } else if (selectedService.equals(getString(R.string.WhiteEL))) {
                    servicePrice = (price+100)*2;
                }
                et_price.setText(String.valueOf(servicePrice));
//                String quantity = et_quantity.getText().toString();

                reCalcNetPrice(tv_netPrice, et_price, et_quantity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        et_quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                String quantity = s.toString();
//                String price = et_price.getText().toString();
                reCalcNetPrice(tv_netPrice, et_price, et_quantity);
            }
        });
        et_price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                String quantity = et_quantity.getText().toString();
//                String price = s.toString();
                reCalcNetPrice(tv_netPrice, et_price, et_quantity);
            }
        });

        return v;
    }

    private void reCalcNetPrice(TextView tv_netPrice, EditText et_price, EditText et_quantity) {
        String itemPrice = et_price.getText().toString();
        String quantity = et_quantity.getText().toString();
        if (!itemPrice.isEmpty() && !quantity.isEmpty()){
            et_price.setBackgroundResource(R.drawable.laundry_activity_text_bg);
            et_quantity.setBackgroundResource(R.drawable.laundry_activity_text_bg);
            int netPrice = Integer.parseInt(itemPrice) * Integer.parseInt(quantity);
            tv_netPrice.setText(String.valueOf(netPrice));
            reCalcTotals();
        } else {
            if (itemPrice.isEmpty()){
                et_price.setBackgroundResource(R.drawable.laundry_activity_text_error_bg);
            } else{
                et_quantity.setBackgroundResource(R.drawable.laundry_activity_text_error_bg);
            }
        }

    }

    private void reCalcTotals() {
        int totalQuantity = 0;
        int totalNetPrice = 0;
        for (int i=0; i<linearLayout.getChildCount(); i++) {
            CardView cv = (CardView) linearLayout.getChildAt(i);
            EditText et_quantity = (EditText) cv.findViewById(R.id.et_quantity);
            TextView tv_netPrice = (TextView) cv.findViewById(R.id.tv_netPrice);

            String netPriceStr = tv_netPrice.getText().toString();
            String quantityStr = et_quantity.getText().toString();
            if (!netPriceStr.isEmpty()) {
                int netPrice = Integer.parseInt(tv_netPrice.getText().toString());
                totalNetPrice += netPrice;
            }

            if (!quantityStr.isEmpty()) {
                int quantity = Integer.parseInt(et_quantity.getText().toString());
                totalQuantity += quantity;
            }
        }
        tv_priceTotal = (TextView) findViewById(R.id.tv_priceTotal);
        tv_quantityTotal = (TextView) findViewById(R.id.tv_quantityTotal);
        tv_priceTotal.setText(String.valueOf(totalNetPrice));
        tv_quantityTotal.setText(String.valueOf(totalQuantity));
    }

    private boolean isValidated() {
        if (linearLayout.getChildCount() == 0) {
            return false;
        }
        for (int i=0; i<linearLayout.getChildCount(); i++) {
            CardView cv = (CardView) linearLayout.getChildAt(i);
            EditText et_quantity = (EditText) cv.findViewById(R.id.et_quantity);
            EditText et_itemPrice = (EditText) cv.findViewById(R.id.et_itemPrice);

            String quantity = et_quantity.getText().toString();
            String price = et_itemPrice.getText().toString();
            if (price.isEmpty() || quantity.isEmpty()){
                return false;
            }
        }

        return true;
    }

    public void onSaveOrderClick(View view) {
        if (allowSaveOrder()) {
            if (isValidated()) {
                String totalquantity = tv_quantityTotal.getText().toString();
                String totalAmt = tv_priceTotal.getText().toString();
                utilsDialog.showConfirmSaveDialog(pickUpDate, deliveryDate, totalquantity, totalAmt);
            } else {
                Utils.showToastMessage(this, "Error: Some vital information are " +
                        "missing please check Order!!");
            }
        }

    }
}
