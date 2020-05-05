package com.gmail.korex006.mylaundry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

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
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.korex006.mylaundry.MyLaundryDBContract.PriceListTable;

import java.util.Objects;

public class PriceListActivity extends AppCompatActivity {


    private CursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_list);

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
        if (cursorAdapter==null) {
            handleIntent(getIntent());
        }

    }

    private void handleIntent(Intent intent) {
        // Get the intent, verify the action and get the query
        ListView lv_priceList = (ListView) findViewById(R.id.lv_priceList);
        Cursor mCursor;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            MyLaundryVirtualTables db = new MyLaundryVirtualTables(this);
            String query = intent.getStringExtra(SearchManager.QUERY);
            mCursor = db.getItemMatches(query, null);
        } else {
            SQLiteDatabase db = new MyLaundryDBHelper(this).getReadableDatabase();
            mCursor = db.query(PriceListTable.TABLE_NAME, null, null, null,
                    null, null, null);
        }
        if (cursorAdapter == null) {
            cursorAdapter = new CursorAdapter(this, mCursor, 0) {

                // The newView method is used to inflate a new view and return it,
                // you don't bind any data to the view at this point.
                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    return LayoutInflater.from(context).inflate(R.layout.price_list_item, parent, false);
                }

                // The bindView method is used to bind all data to a given view
                // such as setting the text on a TextView.
                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    // Find fields to populate in inflated template
                    TextView tv_itemName = (TextView) view.findViewById(R.id.tv_itemName);
                    TextView tv_itemPrice = (TextView) view.findViewById(R.id.tv_itemPrice);
                    // Extract properties from cursor
                    String itemName = cursor.getString(cursor.getColumnIndexOrThrow(PriceListTable.COLUMN_ITEM_NAME));
                    int price = cursor.getInt(cursor.getColumnIndexOrThrow(PriceListTable.COLUMN_DEFAULT_PRICE));
                    // Populate fields with extracted properties
                    tv_itemName.setText(itemName);
                    tv_itemPrice.setText(String.valueOf(price));
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.searchable_activities_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSubmitButtonEnabled(true);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_search) {
            if (!item.isActionViewExpanded()) {
                onSearchRequested();
            }

        }
        return super.onOptionsItemSelected(item);
    }
}
