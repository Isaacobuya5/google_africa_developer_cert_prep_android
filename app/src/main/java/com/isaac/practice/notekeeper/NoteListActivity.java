package com.isaac.practice.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.isaac.practice.notekeeper.adapters.NotesRecyclerAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {

    private ArrayAdapter<NoteInfo> mAdapterNotes;
    private NotesRecyclerAdapter mNotesRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NoteListActivity.this, NoteActivity.class));
            }
        });

        // populating our list view - simple view to display list
        initializeDisplayContent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // need a way to tell adapter that list has changed due to new note creation
        mNotesRecyclerAdapter.notifyDataSetChanged();
    }

    private void initializeDisplayContent() {
        // getting a reference to recyclerview
        final RecyclerView recyclerNotes = (RecyclerView) findViewById(R.id.list_notes);
        // LayoutManager
        final LinearLayoutManager notesLayoutManager = new LinearLayoutManager(this);
        // associate layout manager with the recyclerview
        recyclerNotes.setLayoutManager(notesLayoutManager);
        // get content to place in the list
        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        // our adapter
        mNotesRecyclerAdapter = new NotesRecyclerAdapter(null, this);
        // associate adapter with recycler view
        recyclerNotes.setAdapter(mNotesRecyclerAdapter);
    }

    /**
     * RECYCLERVIEW
     * Initially ListView was used to display a list of items but they had some limitations such as;
     * -> Difficult to customize.
     * -> Performance changes in some cases.
     * -> Always displays vertical lists.
     *
     * But with RecyclerView, list display is divided into distinct phases and each phase provides a chance to customize.
     * -> RecyclerView provides efficient display management.
     *
     * STEPS
     * a. Associate LayoutManager with RecyclerView.
     * -> Layout Manager is responsible for how the individual items are arranged.
     * b. Associate an adapter with the recyclerview.
     * -> The adapter is backed by some data. It is also responsible for creating individual view instances.
     * -> Adapter takes the View instances and places it in the RecyclerView.
     * -> Adapter creates additional view instances to allow display of additional rows in the recyclerview.
     * -> * Adapter is also responsible to take that data and then load it into View items within RecyclerView.
     *
     * DEVELOPING RECYCLERVIEW COMPONENTS
     * a. Design the RecyclerView as part of a layout resource.
     * b. Create and associate a layout manager.
     * c. Design the item view i.e. individual item as a layout resource.
     * d. Create and associate an adapter. -> 3 functions i.e.
     * -> Constructs item view instances.
     * -> Manages data interaction.
     * -> Associate data items with item views.
     *
     * LAYOUT MANAGERS
     * RecyclerView.LayoutManagers is the base class.
     * We can extend it to create custom layout managers but no need since Android already provides several implementations
     * which caters for most needs.
     * Examples;
     * LinearLayoutManager - similar to ListView but with more features.
     * GridLayoutManager - items are organized as a grid.
     * We can specify span i.e. columns for vertical orientation.
     * Adjacent items are consistently sized.
     * StaggeredGridLayoutManager - items are organized as a grid but each item is consistently sized.
     *
     *  BINDING DATA TO RECYCLERVIEW
     *
     *  a. Item View Holder - views are managed using view holder patterns which;
     *  -> Holds a reference to the top level view.
     *  -> Holds a refernce to the contained views.
     *  We do that by implementing a Custom ViewHolder class which extends RecyclerView
     *  -> Have fields inside it for all of the contained views that will be used to display information to the views.
     *  Steps of Implementing RecyclerView adapter
     *  Extend the RecyclerView.Adapter
     *  -> We pass View Holder class as a type parameter i.e. the custom view holder class..
     *  -> Most often, ViewHolder class are often nested within the adapter class..
     *
     *  RecyclerView.Adapter has a number of methods that we can override to provide custom behaviour.
     *  The three most common methods are;
     *  getItemCount() - return a number of items.
     *  onCreateViewHolder() - creates our item views and stores item view references in the view holder.
     *  (stores information of that item view in the custom view holder class.)
     *  onBindViewHolder() - receives ViewHolder from the recycler view and the position of the data we want to display.
     *  Set data values (display values) using ViewHolder.
     *
     * ITEM SELECTION HANDLING
     * RecyclerView has no implicit support for item selection.
     * Instead, it allows contained views to each add their own click even handlers.
     * Allows multiple selection within an item.
     * In this case we Associate onClick listener with top level view, however some cases may need it on contained views
     *
     */

}