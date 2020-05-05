package com.gmail.korex006.mylaundry;

import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.korex006.mylaundry.MyLaundryDBContract.PeopleInfoTable;

public class SearchResultsRecyclerAdapter
        extends BaseCursorAdapter<SearchResultsRecyclerAdapter.searchResultViewHolder> {
//    private final Context mContext;
//    private final LayoutInflater mLayoutInflater;
    private Cursor mCursor;
    private int personIDPos;
//    private int mIdPos;
    private int namePos;
    private int custTypePos;

    public SearchResultsRecyclerAdapter() {
        super(null);
    }

    @Override
    public searchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_view_layout,
                parent, false);

        return new searchResultViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(searchResultViewHolder viewHolder, Cursor cursor) {
        String personId = cursor.getString(personIDPos);
        String name = cursor.getString(namePos);
        String custType = cursor.getString(custTypePos);
//        int id = cursor.getInt(mIdPos);

        // show an abridged ID $ branch to efficienty manage space
        String idString = personId.charAt(0) +"-"+ personId.substring(5);
        String branchStr = custType + personId.substring(5, 7);

        viewHolder.mTvPersonID.setText(idString);
        viewHolder.mTvName.setText(name);
        viewHolder.mTvBranch.setText(branchStr);
        viewHolder.mId = personId;

    }

    @Override
    public void changeCursor (Cursor cursor) {
        super.changeCursor(cursor);
        populateColumnsPositions(cursor);
    }

    private void populateColumnsPositions(Cursor cursor) {
        if (cursor == null) {
            return;
        }
        // Get Column indexes from cursor
        personIDPos = cursor.getColumnIndex(PeopleInfoTable.COLUMN_PERSON_ID);
        namePos = cursor.getColumnIndex(PeopleInfoTable.COLUMN_CUST_NAME);
        custTypePos = cursor.getColumnIndex(PeopleInfoTable.COLUMN_CUST_TYPE);

    }


    class searchResultViewHolder extends RecyclerView.ViewHolder {

        final TextView mTvPersonID;
        String mId;
        final TextView mTvName;
        final TextView mTvBranch;

        searchResultViewHolder(@NonNull final View itemView) {
            super(itemView);
            mTvPersonID = (TextView) itemView.findViewById(R.id.tv_personID);
            mTvName = (TextView) itemView.findViewById(R.id.tv_custName);
            mTvBranch = (TextView) itemView.findViewById(R.id.tv_branch);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(itemView.getContext(), LaundryOrderActivity.class);
//                    String personID = mTvPersonID.getText().toString();
                    intent.putExtra(LaundryOrderActivity.EXTRA_PERSON_ID, mId);

                    itemView.getContext().startActivity(intent);
//                    Snackbar.make(v, mTvPersonID.getText() + "\n" + mTvName.getText(),
//                            Snackbar.LENGTH_LONG).show();
//                    Utils.showToastMessage(v.getContext(), "Click acknowledged");
                }
            });
        }
    }
}
