package com.isaac.practice.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {

    private ArrayAdapter<NoteInfo> mAdapterNotes;

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
        mAdapterNotes.notifyDataSetChanged();
    }

    private void initializeDisplayContent() {
        final ListView listNotes = (ListView) findViewById(R.id.list_notes);
        // get content to place in the list
        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        // adapter
        mAdapterNotes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notes);
        listNotes.setAdapter(mAdapterNotes);

        // handle user selection - i.e. click on a single item within a list
        // accepts an interface that is to be implemented by this Activity or create another class, inner class  or implement as anonymous class as shown below
        listNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // launch note activity
                // Intents describes a desired operation i.e. target plus additional info to be passed i.e. extras
                Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
                // extras - name/value pairs - added to intents with putExtras
                // target can access the intent by calling getIntent()
                // use intent.getXXXExtra to retrieve extras
                // intents operates within the Activity process - travels from outside initially
                // we can also send intents to leave our process and travel to an outside activity process
                // intents must therefore be cross-process friendly i.e. limit allowable extras
                // supported extras types -> Primitive Types, Arrays of supported types, Some Array Lists and few special types.
                // most reference types are not supported (not cross process friendly) thus requires special handling.
                // Reference types therefore needs to be flattened i.e. converted to bytes
                // options -> a. Java serialization - not preffererd because it is run time expensive
                //b. Parcelable - much more efficient - explicitely implement behaviour - harder to implement than serialization
                // IMPLEMENTING PARCELABLE
                // -> describeContents -> indicates special behaviour(most cases we don't have thus return 0)
                // -> writeToParcel -> Receives a Parcel instance - use Parcel.writeXXX to store content.
                // Provide "public static final CREATOR" field of type Parcelable.Creator
                // thus you must implement Parcelable.Creator interface
                // ->a.  createFromParcel - responsible to create new type instance.
                // -> receives a Parcel instance -> b. then use Parcel.readXX to access content.
                // -> newArray() - receives a size, responsible to create array of type.
                // MAKE noteInfo class parcelable

                // get note info corresponding to the note selected
                // mark the listNote (local variable) final to allow us reference it from inside the anonymous class
//                NoteInfo note = (NoteInfo) listNotes.getItemAtPosition(position);
                // we use Strings as intent names -> use constants in the destination activity
                // NOTE -> Consider using Parcelable when appropriate e.g. intent that travels across process boundaries coz all activites/components in same app runs within a single process

//                intent.putExtra(NoteActivity.NOTE_INFO, note);
                intent.putExtra(NoteActivity.NOTE_POSITION, position);

                startActivity(intent);
            }
        });
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
     *
     */
}