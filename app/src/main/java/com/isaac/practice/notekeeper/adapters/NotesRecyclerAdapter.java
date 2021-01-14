package com.isaac.practice.notekeeper.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.isaac.practice.notekeeper.NoteInfo;
import com.isaac.practice.notekeeper.R;

import java.util.List;

public class NotesRecyclerAdapter extends RecyclerView.Adapter<NotesRecyclerAdapter.ViewHolder>{

    private List<NoteInfo> mNotes;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public NotesRecyclerAdapter(List<NoteInfo> notes, Context context) {
        this.mNotes = notes;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
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
        // gets reference of the ViewHolder from the recycler view and position of the data we want to display
        // get a note at that particular position.
        NoteInfo note = mNotes.get(position);
        // displaying data values
        holder.mTextCourse.setText(note.getCourse().getTitle());
        holder.mTextTitle.setText(note.getTitle());
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

    // step 1 - custom ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextCourse;
        private TextView mTextTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextCourse = (TextView) itemView.findViewById(R.id.text_course);
            mTextTitle = (TextView) itemView.findViewById(R.id.text_title);
        }
    }
}
