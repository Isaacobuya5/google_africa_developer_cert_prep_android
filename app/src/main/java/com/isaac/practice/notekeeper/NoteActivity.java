package com.isaac.practice.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

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
    private int mNotePosition;
    private boolean mIsCancelling;
//    private String mMOriginalCourseId;
//    private String mMOriginalNoteTitle;
//    private String mMOriginalNoteText;
    private NoteActivityViewModel mNoteActivityViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // loading layout file
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ViewModelProvider to manage instances of our view model across configuration change
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(), ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mNoteActivityViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        // restore state from the bundle if the viewModel instance had been destroyed alongside activity
        // if viewmodel instance is still present, no need to restore from savedInstance state
        if (mNoteActivityViewModel.mIsNewlyCreated && savedInstanceState != null) {
            // go ahead and restore the state
            mNoteActivityViewModel.restoreState(savedInstanceState);
        }

        // ViewModel instance now exists
        mNoteActivityViewModel.mIsNewlyCreated = false;

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
        // save original note values just incase we cancel update to an existing note
        saveOriginalNoteValues();

        // Get references to the text views
        mTextNoteTitle = (EditText) findViewById(R.id.text_note_title);
        mTextNoteText = (EditText) findViewById(R.id.text_note_text);

        if(!mIsNewNote) {
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
        }
    }

    private void saveOriginalNoteValues() {
        if (mIsNewNote)
            return;
//        mMOriginalCourseId = mNote.getCourse().getCourseId();
//        mMOriginalNoteTitle = mNote.getTitle();
//        mMOriginalNoteText = mNote.getText();
        mNoteActivityViewModel.setOriginalCourseId(mNote.getCourse().getCourseId());
        mNoteActivityViewModel.setOriginalNoteTitle(mNote.getTitle());
        mNoteActivityViewModel.setOriginalNoteText(mNote.getText());

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

        if(mIsNewNote) {
            // create a backing store incase a new note
            createNewNote();
        } else {
            mNote = DataManager.getInstance().getNotes().get(position);
        }
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
        // get note at that position and assign to mNote
        mNote = dm.getNotes().get(mNotePosition);
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
        } else if(id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        } else if(id == R.id.action_next){
            displayNextNote();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // getting reference to the menu item "next"
        MenuItem item = menu.findItem(R.id.action_next);
        // get the last note index
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        // enable/ disable next button
        item.setEnabled(mNotePosition < lastNoteIndex); // always enabled prior to last note
        // problem - gets called when the note is initially displayed thus the need for inValidateOptionsMenu
        // which ensures that this method gets called again when we move to the next note
        return super.onPrepareOptionsMenu(menu);
    }

    private void displayNextNote() {
        // ensure to save the note before "next"
        saveNote();
        // increment the current note position
        ++mNotePosition;
        // get the corresponding note at that positon
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);
        // display that note
        displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);

        // calls onPrepareOptionsMenu again
        invalidateOptionsMenu();

        saveOriginalNoteValues();

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


    // Tasks - collection of activities that users interacts with when performing a certain job.
    // flow through an app is managed as a task.
    // Task is managed as a stack -> i.e. back stack
    // back button -> removes activity from a task
    // selecting activity adds it to a task.
    // we need to think how ro manage state within the flow

    @Override
    protected void onPause() {
        super.onPause();
        // if we are cancelling
        if(mIsCancelling) {
            // remove note from our backing store - if only we have created it new
            if(mIsNewNote) {
                DataManager.getInstance().removeNote(mNotePosition);
            } else {
                // if cancelled existing note then store the orignial values
                storePreviousNoteValues();
            }
        } else {
            // save note when user leaves the note
            saveNote();
        }
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mNoteActivityViewModel.getOriginalCourseId());
        mNote.setCourse(course);
        mNote.setTitle(mNoteActivityViewModel.getOriginalNoteTitle());
        mNote.setText(mNoteActivityViewModel.getOriginalNoteText());
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }
    // hint -> write to backing store when leaving an activity.
    // saving changes -> handle in onPause
    // new entries -> handle in onCreate

    // onSavedInstance saves  state upon activty destruction and recreation
    // use together with ViewModel for a complete state management solution

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the instance state to a bundle
        if (outState != null) {
            mNoteActivityViewModel.saveState(outState);
        }
    }

    /**
     * WORKING WITH OPTIONS MENU
     * Options Menu - allow us to provide actions for out app.
     * By default, options normally appear under the action overflow.
     * When we want to create an options menu, we define them in the menu resource.
     * The root component of that file is <menu></menu>
     * Each action is then defined as a menu item within that <menu></menu>
     * Two key properties of menu item are id and title.
     *
     * ASSOCIATING OPTIONS MENU WITH AN ACTIVITY.
     * There is no automatic association between options menu and an activity.
     * To create an association, we override the onCreateOptionsMenu()  method.
     * It receives a menu reference and then we are responsible for attaching our menu items to the menu that is passed in.
     * We do that by inflating menu resource with the menu inflater.
     * The Activity class provides method getMenuInflater() which gives us access to the menu inflater.
     * Then we simply inflate our menu using that returned menu inflater.
     *
     * HANDLING MENU ITEM SELECTION.
     * @Override onOptionsItemSelected call back method.
     * Receives the menu item reference.
     * Retrieve menu item id value - Access using MenuItem.getItemId() and the perform whatever work that we want to do.
     *
     *
     * MENU ITEMS AS APP BAR ACTIONS
     * Limitations of Action Overflow menus are;
     * - Not immediately disoverable.
     * - Access takes multiple steps.
     * Advantages of App Bar Actions are;
     * a. Menu items are visible on the app bar.
     * b. Improve access to the common menu items.
     * c. Normally have icons associated with them.
     *
     * Making menu item as app bar actions;
     *  Use showAsAction property with common values such as;
     * a. ifRoom -> menu item appear as app bar actions when space allows.
     * - menu items are given prefernce in top to bottom order.
     * b. always - always displays menu items as app bar actions.
     * use vary SPARINGLY infact it is recommended that no more than two menu items should be displayed as "always".
     * c. withText - show text with action when space allows.
     * - can be combined with ifRoom or always values.
     *
     * CHANGING MENU ITEMS AT RUN TIME
     * Application state can change menu state i.e.
     * 1. may need to add/remove menu items.
     * 2. may need to enable/disable menu items.
     * 2 KEY METHODS
     * a. System calls
     * onPrepareOptionsMenu receives reference to the current menu and is
     * usually called after onCreateOptionsMenu and before menu is displayed.
     * b. Application calls.
     * invalidateOptionsMenu() - call when menu state may need to change.
     * - System schedules a call to onPrepareOptionsMenu - thus it gets called and gives us another chance to
     * modify the state of our application.
     */
}