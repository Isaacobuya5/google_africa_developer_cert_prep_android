package com.isaac.practice.notekeeper.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.isaac.practice.notekeeper.NoteActivity;
import com.isaac.practice.notekeeper.NoteInfo;
import com.isaac.practice.notekeeper.R;
import com.isaac.practice.notekeeper.database.NoteKeeperDatabaseContract;

import java.util.List;

import static com.isaac.practice.notekeeper.database.NoteKeeperDatabaseContract.*;

public class NotesRecyclerAdapter extends RecyclerView.Adapter<NotesRecyclerAdapter.ViewHolder>{

//    private List<NoteInfo> mNotes;
    private Cursor mCursor;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int mCoursePos;
    private int mNoteTitlePos;
    private int mIdPos;

    public NotesRecyclerAdapter(Cursor cursor, Context context) {
//        this.mNotes = notes;
        mCursor = cursor;
        // we need positions of items we are interested in
        populateColumnPositions();
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    private void populateColumnPositions() {
        // if we don't have cursor yet simply return.
        if (mCursor == null) return;
        // else -> get column indexes from the cursor.
        // for now we are using course id
        mCoursePos = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        // this id will be sent via intent
        mIdPos = mCursor.getColumnIndex(NoteInfoEntry._ID);
    }

    public void changeCursor(Cursor cursor) {
        // if we have an existing cursor close it.
        if (mCursor != null) mCursor.close();
        // assign the cursor we received to our field
        mCursor = cursor;
        populateColumnPositions();
        // notify the recycler view that data set has changed.
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // before we create a ViewHolder, we need to create the View itself
        // false - we don't want this newly created view automatically attached to its parent.
        View itemView = mLayoutInflater.inflate(R.layout.item_note_list, parent, false);
        // return an instance of newly created view holder class.
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // move cursor to the specified position
        mCursor.moveToPosition(position);
        // get the values at that position
        String course = mCursor.getString(mCoursePos);
        String noteTitle = mCursor.getString(mNoteTitlePos);
        int id = mCursor.getInt(mIdPos);

        // gets reference of the ViewHolder from the recycler view and position of the data we want to display
        // get a note at that particular position.
//        NoteInfo note = mNotes.get(position);
        // displaying data values
        holder.mTextCourse.setText(course);
        holder.mTextTitle.setText(noteTitle);
        // set position of this view holder for purposes of click
//        holder.mId = position;
//        holder.mId = note.getId();
        holder.mId = id;
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    // step 1 - custom ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTextCourse;
        public final TextView mTextTitle;
        // we need to know the position of the ViewHolder
        public int mId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextCourse = (TextView) itemView.findViewById(R.id.text_course);
            mTextTitle = (TextView) itemView.findViewById(R.id.text_title);
            // associate click listener with this view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, NoteActivity.class);
                    // set intent extra
                    intent.putExtra(NoteActivity.NOTE_ID, mId);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    /**
     * USING CURSOR WITH RECYCLERVIEW ADAPTER
     * -> The Adapter data source is now going to change since we are going to use cursor as a data source rather than a list.
     * We are going to pass cursor to the constructor of the adapter.
     * We also need a method to allow us change cursor that is associated with RecyclerView adapter.
     *
     * Adapter View Management
     * -> Remember we use our view holder class to create individual view instances.
     * -> The RecyclerView then manages those views as a pool.
     * The Role of View Holder instances are;
     *  -> hold references to the contained views.
     *  -> handles the view interactions.
     *   Adapter data display.
     *   -> Remember that when our recycler view is interacting with our adapter, it will indicate to us the position
     *   of the data it want displayed.
     *   -> So our cursor will have to move to a specific position. Whe we get to that position, we have to get the values of the columns at that
     *   position from the cursor.
     *   Then we use those values to set the display values within our view holder.
     *
     *   Thus in summary these are the steps;
     *   -> Move the cursor to the specified position.
     *   -> Retrieve desired column values.
     *   -> Set display values using ViewHolder.
     */
}
