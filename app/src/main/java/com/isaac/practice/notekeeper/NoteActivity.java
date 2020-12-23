package com.isaac.practice.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // loading layout file
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // generated R class with nested class i.e. ID and layout
        // need reference to the spinner
        mSpinnerCourses = (Spinner) findViewById(R.id.spinner_courses);
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
        mSpinnerCourses.setAdapter(adapterCourses);

        //method to read values from intents
        readDisplayStateValues();

        // Get references to the text views
        mTextNoteTitle = (EditText) findViewById(R.id.text_note_title);
        mTextNoteText = (EditText) findViewById(R.id.text_note_text);

        if(!mIsNewNote) {
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
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
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        // use implicit intent to send email
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Check out what i learnt in the pluralsight course  \"" +
                course.getTitle() + "\"\n" + mTextNoteText.getText().toString();

        // create intent and associate with action ACTION_SEND
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822"); // mime type for email messages
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    // Activities with results -> e.g camera activity, Contact Activity
    // we start it with startActivityForResult(intent, app_defined_integer);
    // app_defined_integer - differentiates results within your app
    // recieve result by calling "onActivityResult(integer_identifier,result code,intent);
    // integer identifier - App defined integer identifier, Result code -> e.g. RESULT_OK to indicate success, intent -> contains activities result
    // example -> Camera
    // start activity -> Intent action -> MediaStore.ACTION_IMAGE_CAPTURE, Extra -> MediaStore.EXTRA_OUTPUT - file in which to save full quality image to - passed as URI
    /**
     * public class MyActivity extends MyActivity {
     * // integer identifier to identify the kind of results
     * private static final int SHOW_CAMERA = 1;
     *
     * private void showCamera(Uri photoFile) {
     *     Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     *     intent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile);
     *      startActivity(intent, SHOW_CAMERA);
     * }
     *
     * // GETTING RESULTS BACK
     * Check for request code of SHOW_CAMERA - identifies the result is for our request.
     * Check for result code of RESULT_OK -> indicates success -> full quality image stored in file
     * Retrieve thumbnail -> stored in result intent as thumbnail.
     *
     * @override
     * protected void onActivityResult(int requestCode, int resultCode, Intent result) {
     *     if(requestCode == SHOW_CAMERA && resultCode == RESULT_OK) {
     *         Bitmap thumbnail = result.getParcelable("data");
     *         // do something with the thumbnail e.g. read file
     *     }
     * }
     */
    //
}