package com.gmail.korex006.mylaundry;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.gmail.korex006.mylaundry.MyLaundryDBContract.OrdersListTable;
import com.gmail.korex006.mylaundry.MyLaundryDBContract.PeopleInfoTable;

import java.util.Objects;

public class SavedOrdersActivity extends AppCompatActivity {

    private CursorAdapter cursorAdapter;
    private UtilsDialog utilsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_orders);
        setUpUIViews();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cursorAdapter.changeCursor(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cursorAdapter.getCursor() == null) {
            handleIntent(getIntent());
        }

    }

    private void handleIntent(Intent intent) {
        // Get the intent, verify the action and get the query
        ListView lv_priceList = (ListView) findViewById(R.id.lv_priceList);
        SQLiteDatabase db = new MyLaundryDBHelper(this).getReadableDatabase();
        Cursor mCursor;
        String orderList = OrdersListTable.TABLE_NAME;
        String custList = PeopleInfoTable.TABLE_NAME;
        String query = "SELECT " + orderList + "." + OrdersListTable._ID + ", "
                + orderList + "." + OrdersListTable.COLUMN_PERSON_ID + ", "
                + orderList + "." + OrdersListTable.COLUMN_ORDER_ID + ", "
                + custList + "." + PeopleInfoTable.COLUMN_CUST_NAME + ", "
                + custList + "." + PeopleInfoTable.COLUMN_VALID_EMAIL_EXIST + ", "
                + orderList + "." + OrdersListTable.COLUMN_QTY + ", "
                + orderList + "." + OrdersListTable.COLUMN_AMOUNT + ", "
                + orderList + "." + OrdersListTable.COLUMN_DELIVERY_DATE + ", "
                + orderList + "." + OrdersListTable.COLUMN_PICK_UP_DATE + ", "
                + orderList + "." + OrdersListTable.COLUMN_EMAIL

                + " FROM " + orderList
                + " INNER JOIN " + custList + " USING (" + OrdersListTable.COLUMN_PERSON_ID + ")";


        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String search = intent.getStringExtra(SearchManager.QUERY);

            query+= " WHERE " + custList + "." + PeopleInfoTable.COLUMN_CUST_NAME
                    +" LIKE '%" + search + "%'" ;
        }
        mCursor = db.rawQuery(query, null);
        if (cursorAdapter == null) {
            cursorAdapter = new CursorAdapter(this, mCursor, 0) {

                // The newView method is used to inflate a new view and return it,
                // you don't bind any data to the view at this point.
                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    return LayoutInflater.from(context).inflate(R.layout.orders_list_item, parent, false);
                }

                // The bindView method is used to bind all data to a given view
                // such as setting the text on a TextView.
                @Override
                public void bindView(View view, Context context, Cursor cursor) {

                    // Find fields to populate in inflated template

                    TextView tv_custName = (TextView) view.findViewById(R.id.tv_custName);
                    TextView tv_pickupDate = (TextView) view.findViewById(R.id.tv_pickUpDate);
                    ImageButton ib_email = (ImageButton) view.findViewById(R.id.ib_email);
                    TextView tv_qty = (TextView) view.findViewById(R.id.tv_qty);
                    TextView tv_amt = (TextView) view.findViewById(R.id.tv_amount);
                    final TextView tv_orderId = (TextView) view.findViewById(R.id.tv_orderID);
                    ImageButton ib_delete = (ImageButton) view.findViewById(R.id.ib_delete);


                    // Extract properties from cursor
                    final String orderId = cursor.getString(cursor.getColumnIndex(OrdersListTable.COLUMN_ORDER_ID));
                    String custName = cursor.getString(cursor.getColumnIndex(PeopleInfoTable.COLUMN_CUST_NAME));
                    String pickUpDate = cursor.getString(cursor.getColumnIndex(OrdersListTable.COLUMN_PICK_UP_DATE));
                    int validEmailExist = cursor.getInt(cursor.getColumnIndex(PeopleInfoTable.COLUMN_VALID_EMAIL_EXIST));
                    int emailSent = cursor.getInt(cursor.getColumnIndex(OrdersListTable.COLUMN_EMAIL));
                    String qty = cursor.getString(cursor.getColumnIndex(OrdersListTable.COLUMN_QTY));
                    String amount = cursor.getString(cursor.getColumnIndex(OrdersListTable.COLUMN_AMOUNT));

                    //Order details is made up of orderId, validEmailExist & emailSent inorder
                    // to reduce reading from database
                    final String orderDetails = orderId + "--" + validEmailExist + "--" + emailSent;

                    // Populate fields with extracted properties
                    tv_orderId.setText(orderDetails);
                    tv_custName.setText(custName);
                    tv_pickupDate.setText(pickUpDate);
                    tv_qty.setText(qty);
                    tv_amt.setText(amount);

                    // set mail drawable color
                    if (validEmailExist == 1) {
                        if (emailSent == 0) {
                            ib_email.setColorFilter(getResources().getColor(R.color.azure));
                        } else {
                            ib_email.setColorFilter(getResources().getColor(R.color.green));
                        }
                    } else {
                        ib_email.setColorFilter(getResources().getColor(R.color.red));
                    }
//                    tv_deliveryDate.setText(deliveryDate);

                    ib_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String orderDetails = tv_orderId.getText().toString();
                            String orderId = orderDetails.split("--")[0];
                            utilsDialog.showDeleteOrderDialog(orderId);
                        }
                    });
                    ib_email.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String orderDetails = tv_orderId.getText().toString();
                            String[] orderDetailsArr = orderDetails.split("--");
                            utilsDialog.showEmailClickedDialog(orderDetailsArr);

                        }
                    });
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String orderDetails = tv_orderId.getText().toString();
                            String[] orderDetailsArr = orderDetails.split("--");
                            String orderId = orderDetailsArr[0];
                            utilsDialog.showsOrderClickDialog(orderId);
                        }
                    });
                }
            };
        } else {
            cursorAdapter.changeCursor(mCursor);
        }

        lv_priceList.setAdapter(cursorAdapter);
    }

    private void setUpUIViews() {
        // set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_genToolBar);
        toolbar.setTitle("Price List");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

        // set up Drawer
        DrawerUtil.getDrawer(this, toolbar);

        // Set up utilsDialog Object
        utilsDialog = new UtilsDialog(this);

        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.searchable_activities_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
//        searchView.setSubmitButtonEnabled(true);
        // Assumes current activity is the searchable activity
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default
        searchView.setOnCloseListener(
                new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        // Call onNewIntent so as to reset Activity
                        onNewIntent(new Intent());
                        return false;
                    }
                }
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_search) {
            onSearchRequested();
        }
        return super.onOptionsItemSelected(item);
    }

}
