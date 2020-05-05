package com.gmail.korex006.mylaundry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gmail.korex006.mylaundry.MyLaundryDBContract.PeopleInfoTable;

public class SearchActivity extends AppCompatActivity {
    private Button btn_searchName;
    private Button btn_SearchID;
    private EditText et_searchText;
    private SearchResultsRecyclerAdapter searchResultsRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setUPUIViews();
    }

    private void setUPUIViews() {
        // set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_genToolBar);
        //toolbar.setTitle(getResources().getString(R.string.cgpa_calc));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // set up Drawer
        DrawerUtil.getDrawer(this, toolbar);

        btn_SearchID = (Button) findViewById(R.id.btn_search_name);
        btn_searchName = (Button) findViewById(R.id.btn_search_id);
        et_searchText = (EditText) findViewById(R.id.et_searchText);

        RecyclerView mRecyclerItems = (RecyclerView) findViewById(R.id.rv_searchResults);
        searchResultsRecyclerAdapter = new SearchResultsRecyclerAdapter();
        mRecyclerItems.setAdapter(searchResultsRecyclerAdapter);


    }

    public void onSearchClick(View view) {
        int id = view.getId();
        String searchText = et_searchText.getText().toString();
        if (searchText.equals("")) {
            Utils.showToastMessage(this, "Please Name or ID to search for");
        } else {
            if (id == R.id.btn_search_id) {
                searchDB(searchText, PeopleInfoTable.COLUMN_PERSON_ID);
            }
            else if (id == R.id.btn_search_name) {
                searchDB(searchText, PeopleInfoTable.COLUMN_CUST_NAME);
            }
        }

    }

    private void searchDB(String searchText, String column) {
        SQLiteDatabase db = new MyLaundryDBHelper(this).getReadableDatabase();
        RecyclerView linearLayout = (RecyclerView) findViewById(R.id.rv_searchResults);
        linearLayout.removeAllViews();

        String[] columns = {PeopleInfoTable._ID,
                PeopleInfoTable.COLUMN_PERSON_ID,
                PeopleInfoTable.COLUMN_CUST_NAME,
                PeopleInfoTable.COLUMN_CUST_TYPE};
        String selection = column + " LIKE '%" + searchText + "%'";
        //String[] selectionArgs = {searchText};
        Cursor cursor = db.query(PeopleInfoTable.TABLE_NAME,columns, selection, null,
                null, null, null);
        if (cursor.getCount() == 0) {
            Utils.showToastMessage(this, "No record Found for search Text");
        } else {
            searchResultsRecyclerAdapter.changeCursor(cursor);
            Utils.hideKeyboard(this);
        }
    }

    private CardView inflateSearchResultCardView(String personId, String name, String custType) {
        LayoutInflater vi = (LayoutInflater) getLayoutInflater();
        CardView v = (CardView) vi.inflate(R.layout.result_view_layout, null);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //editCourse(v);
                Utils.showToastMessage(SearchActivity.this, "Click acknowledged");
            }
        });
        TextView personIdTV = (TextView) v.findViewById(R.id.tv_personID);
        // show an abridged ID $ branch to efficienty manage space
        String idString = personId.charAt(0) +"-"+ personId.substring(5);
        String branchStr = custType + personId.substring(5, 7);
        personIdTV.setText(idString);
        TextView nameTV = (TextView) v.findViewById(R.id.tv_custName);
        nameTV.setText(name);
        TextView branchTV = (TextView) v.findViewById(R.id.tv_branch);
        branchTV.setText(branchStr);
        return v;
    }

}
