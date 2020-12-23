package com.isaac.practice.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {

//    public static final String NOTE_INFO = "com.isaac.practice.notekeeper.NOTE_INFO";
public static final String NOTE_POSITION = "com.isaac.practice.notekeeper.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // loading layout file
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // generated R class with nested class i.e. ID and layout
        // need reference to the spinner
        Spinner spinnerCourses = (Spinner) findViewById(R.id.spinner_courses);
        // 2 layouts required for spinner, one layout for current selection and another layout for each of the other selections.
        // 3 tasks involved -> getting data across and managing each of those layouts.
        // adapter - responsible for moving data over and managing each of those layouts.
        // different adapters available - some responsible for managing in-memory data sources such as Arrays and Lists while others
        // manage databases sources  that use Cursors.

        // get list of courses
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        // create adapter to associate list of courses with the spinner
        // we can use a custom resource of our own but android provides some built in layout resources,, android.R.layout..
        // android.R.layout.simple_spinner_item - use to format selected item in the spinner
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        // associate resource we want to use for the drop down list of courses
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(adapterCourses);

        //method to read values from intents
        readDisplayStateValues();

        // Get references to the text views
        EditText textNoteTitle = (EditText) findViewById(R.id.text_note_title);
        EditText textNoteText = (EditText) findViewById(R.id.text_note_text);

        if(!mIsNewNote) {
            displayNote(spinnerCourses, textNoteTitle, textNoteText);
        }
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        // get list of courses from DataManager
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        // get index of that particular course within the list
        int courseIndex = courses.indexOf(mNote.getCourse());
        // set spinner to display course at that index
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        // getting the note that was selected
//        mNote = intent.getParcelableExtra(NOTE_POSITION);
        // second value is returned if no item is found
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
//        mIsNewNote = mNote == null;
        mIsNewNote = position == POSITION_NOT_SET;

        if(!mIsNewNote) {
            mNote = DataManager.getInstance().getNotes().get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}