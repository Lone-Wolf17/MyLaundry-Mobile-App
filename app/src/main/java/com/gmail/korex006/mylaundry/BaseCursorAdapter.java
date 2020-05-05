package com.gmail.korex006.mylaundry;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseCursorAdapter<V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;

    public abstract void onBindViewHolder(V holder, Cursor cursor);

    public BaseCursorAdapter(Cursor cursor) {
        setHasStableIds(true);
        changeCursor(cursor);
    }

    @Override
    public void onBindViewHolder(@NonNull V holder, int position) {

        if (!mDataValid) {
            throw new IllegalStateException("Cannot bind view holder when cursor is in invalid state");
        }

        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Could not move to position " + position + " when cursor is in an illegal state");
        }

        onBindViewHolder(holder, mCursor);
    }

    @Override
    public int getItemCount() {
        if (mDataValid) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (!mDataValid) {
            throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Could not move cursor to position " + position + " when trying to get an item id");
        }

        return mCursor.getLong(mRowIdColumn);
    }

    public Cursor getItem(int position) {
        if (!mDataValid) {
            throw new IllegalStateException("Cannot lookup item id when cursor is in invalid state.");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Could not move cursor to position " + position + " when trying to get an item id");
        }
        return mCursor;
    }

    public void changeCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return;
        }

        if (newCursor != null) {
            mCursor = newCursor;
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
            mCursor = null;
            mRowIdColumn = -1;
            mDataValid = false;
        }
    }
}

