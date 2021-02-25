package com.isaac.practice.notekeeper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.isaac.practice.notekeeper.database.NoteKeeperOpenHelper;

import java.util.List;

import static com.isaac.practice.notekeeper.NoteKeeperProviderContract.*;
import static com.isaac.practice.notekeeper.database.NoteKeeperDatabaseContract.*;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

//    public static final String NOTE_INFO = "com.isaac.practice.notekeeper.NOTE_INFO";
public static final String NOTE_ID = "com.isaac.practice.notekeeper.NOTE_POSITION";
    public static final int ID_NOT_SET = -1;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    public static final int NOTE_NOTIFICATION = 0;
    public static final String NOTE_KEEPER_NOTIFICATION_CHANNEL_ID = "NoteKeeperNotification";
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
    private NoteKeeperOpenHelper mDbHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private int mNoteId;
    private SimpleCursorAdapter mAdapterCourses;
    private boolean mCoursesQueryFinished;
    private boolean mNotesQueryFinished;
    private Uri mNoteUri;
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // loading layout file
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbHelper = new NoteKeeperOpenHelper(this);

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

        // we are no longer getting list of courses from the DataManager but rather the database
//        List<CourseInfo> courses = DataManager.getInstance().getCourses();

        // create adapter to associate list of courses with the spinner
        // we can use a custom resource of our own but android provides some built in layout resources,, android.R.layout..
        // android.R.layout.simple_spinner_item - use to format selected item in the spinner
        // WE ARE NO LONGER USING ARRAY ADAPTER
        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,null,new String[] {
                CourseInfoEntry.COLUMN_COURSE_TITLE }, new int[] {
                        android.R.id.text1},0);
//        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        // associate resource we want to use for the drop down list of courses
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapterCourses);

        // connecting cursor to the simple cursor adapter
//        loadCourseData();
        // loading courses in a background thread
        LoaderManager.getInstance(this).initLoader(LOADER_COURSES, null, this);
        //method to read values from intents
        readDisplayStateValues();
        // save original note values just incase we cancel update to an existing note
//        saveOriginalNoteValues();

        // Get references to the text views
        mTextNoteTitle = (EditText) findViewById(R.id.text_note_title);
        mTextNoteText = (EditText) findViewById(R.id.text_note_text);

        if(!mIsNewNote) {
            // load this particular note's data
//            loadNoteData();
            // begin the loading process
//            getLoaderManager().initLoader(LOADER_NOTES,null,this);
            LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null, this);
//            displayNote();
        }
    }

    private void loadCourseData() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns, null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
        // associate the cursor with the SimpleCursorAdaptor
        mAdapterCourses.changeCursor(cursor);
    }

    private void loadNoteData() {
        // get connection to the database.
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String courseId = "android_intent";
        String titleStart = "dynamic";

//        String selection = NoteInfoEntry.COLUMN_COURSE_ID + "= ? AND " + NoteInfoEntry.COLUMN_NOTE_TITLE + " LIKE ?";
        String selection = NoteInfoEntry._ID + "=?";
//        String[] selectionArgs = { courseId, titleStart + "%"};
        String[] selectionArgs = {Integer.toString(mNoteId)};
        final String[] noteColumns = {
                NoteInfoEntry._ID,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };

        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs, null, null, null);
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        displayNote();
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

    private void displayNote() {

        // getting the values
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        // get list of courses from DataManager
//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        CourseInfo course = DataManager.getInstance().getCourse(courseId);
        // get index of that particular course within the list

        // SETTING THE CURRENTLY SELECTED COURSE FROM THE CURSOR

//        int courseIndex = courses.indexOf(course);

        int courseIndex = getIndexOfCourseId(courseId);
        // set spinner to display course at that index
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);
    }

    private int getIndexOfCourseId(String courseId) {
        // get a reference to the cursor used to populate the spinner
        Cursor cursor = mAdapterCourses.getCursor();
        // column index of the column holding the course id
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;
        // walk through the cursor row by row
        boolean more = cursor.moveToFirst();
        while (more) {
            // get course id for the current row
            String cursorCourseId = cursor.getString(courseIdPos);
            // is that the course id we are looking for
            if (courseId.equals(cursorCourseId)) break;
            // moving to the next row
            courseRowIndex++;
            // moving cursor to the next row
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        // getting the note that was selected
//        mNote = intent.getParcelableExtra(NOTE_POSITION);
        // second value is returned if no item is found
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
//        mIsNewNote = mNote == null;
        mIsNewNote = mNoteId == ID_NOT_SET;

        if(mIsNewNote) {
            // create a backing store incase a new note
            createNewNote();
        }
//        } else {
//            mNote = DataManager.getInstance().getNotes().get(mNoteId);
            // load this particular note from the datanbase

        }

    private void createNewNote() {
        AsyncTask<ContentValues, Integer, Uri> task = new AsyncTask<ContentValues, Integer, Uri>() {
            private ProgressBar mProgressBar;

            @Override
            protected void onPreExecute() {
                // runs on the mai thread
                mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(1);
            }

            @Override
            protected Uri doInBackground(ContentValues... params) {
                ContentValues insertValues = params[0];
                Uri rowUri = getContentResolver().insert(Notes.CONTENT_URI, insertValues);
                simulateLongRunningWork(); // simulate slow database work
//                mProgressBar.setProgress(2); // won't work here because not running on the main thread
                publishProgress(2);
                simulateLongRunningWork(); // simulate slow work with data
//                mProgressBar.setProgress(3);
                publishProgress(3);
                return rowUri;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue = values[0];
                mProgressBar.setProgress(progressValue);
            }

            @Override
            protected void onPostExecute(Uri uri) {
                mNoteUri = uri;
                mProgressBar.setVisibility(View.GONE);
            }
        };
//        DataManager dm = DataManager.getInstance();
//        mNotePosition = dm.createNewNote();
//        // get note at that position and assign to mNote
//        mNote = dm.getNotes().get(mNotePosition);
        ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID, "");
        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");

        task.execute(values);

//        SQLiteDatabase db = mDbHelper.getWritableDatabase();
//        mNoteId = (int) db.insert(NoteInfoEntry.TABLE_NAME, null, values);
        // getting a reference to the ContentResolver
        // this shouldn't be performed on the main thread.
//        mNoteUri = getContentResolver().insert(Notes.CONTENT_URI, values);
    }

    private void simulateLongRunningWork() {
        try {
            Thread.sleep(2000);
        } catch(Exception ex) {}
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
        } else if(id == R.id.action_set_reminder) {
            showReminderNotification();
        }

        return super.onOptionsItemSelected(item);
    }

    // displaying notifications
    private void showReminderNotification() {
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        int noteId = (int) ContentUris.parseId(mNoteUri);


        Intent noteActivityIntent = new Intent(this, NoteActivity.class);
        noteActivityIntent.putExtra(NoteActivity.NOTE_ID, noteId);

        // note backuo service intent
        Intent backupServiceIntent = new Intent(this, NoteBackupService.class);
        backupServiceIntent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);

        // create notification channel
        createNotificationChannel();

        final Bitmap picture = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notekeeper");
        builder.setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_stat_note_reminder)
                .setContentTitle("Review Note")
                .setContentText(noteText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLargeIcon(picture)
                .setTicker("Review note")
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(noteText)
                .setBigContentTitle(noteTitle)
                .setSummaryText("Review note"))
                .setContentIntent(
                        PendingIntent.getActivity(this, 0, noteActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .addAction(0, "View all notes", PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(0, "Backup notes", PendingIntent.getService(this, 0, backupServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true);

        mNotificationManager.notify(NOTE_KEEPER_NOTIFICATION_CHANNEL_ID, NOTE_NOTIFICATION, builder.build());
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
        displayNote();

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
                // delete note from the database
                deleteNoteFromDatabase();
//                DataManager.getInstance().removeNote(mNotePosition);
            } else {
                // need to FIX this
                // if cancelled existing note then store the orignial values
                storePreviousNoteValues();
            }
        } else {
            // save note when user leaves the note
            saveNote();
        }
    }

    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + "=?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                return null;
            }
        };
        task.execute();
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mNoteActivityViewModel.getOriginalCourseId());
        mNote.setCourse(course);
        mNote.setTitle(mNoteActivityViewModel.getOriginalNoteTitle());
        mNote.setText(mNoteActivityViewModel.getOriginalNoteText());
    }

    private void saveNote() {
        // get users current selection
        String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        saveNoteToDatabase(courseId,noteTitle,noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {
        // sepecify which note to update
        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(mNoteId)};
        // identify columns to update
        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);
        // get database reference
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // do the update
        db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
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

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_NOTES)
            loader = createLoaderNotes();
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();
        return loader;
    }

    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;
//        Uri uri = Uri.parse("content://com.isaac.practice.notekeeper.provider");
        Uri uri = Courses.CONTENT_URI;
//        String[] courseColumns = {
//                CourseInfoEntry.COLUMN_COURSE_TITLE,
//                CourseInfoEntry.COLUMN_COURSE_ID,
//                CourseInfoEntry._ID
//        };
        // use constants from Content Provider contract class
        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };

        return new CursorLoader(this, uri, courseColumns, null, null,Courses.COLUMN_COURSE_TITLE);
//        return new CursorLoader(this) {
//            @Override
//            public Cursor loadInBackground() {
//                SQLiteDatabase db = mDbHelper.getReadableDatabase();
//
//
//                return db.query(CourseInfoEntry.TABLE_NAME, courseColumns, null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
//            }
//        };
    }

    private CursorLoader createLoaderNotes() {
        mNotesQueryFinished = false;
//        return new CursorLoader(this) {
//            @Override
//            public Cursor loadInBackground() {
//                SQLiteDatabase db = mDbHelper.getReadableDatabase();
//
//                String selection = NoteInfoEntry._ID + " = ?";
//                String[] selectionArgs = {Integer.toString(mNoteId)};
//
//                final String[] noteColumns = {
//                        NoteInfoEntry._ID,
//                        NoteInfoEntry.COLUMN_COURSE_ID,
//                        NoteInfoEntry.COLUMN_NOTE_TITLE,
//                        NoteInfoEntry.COLUMN_NOTE_TEXT
//                };
//
//                return db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
//                        selection, selectionArgs, null, null, null);
//            }
//        };
        final String[] noteColumns = {
//                        Notes._ID,
                        Notes.COLUMN_COURSE_ID,
                        Notes.COLUMN_NOTE_TITLE,
                        Notes.COLUMN_NOTE_TEXT
                };
        // query our content provider
        // build the row Uri
        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        return new CursorLoader(this,mNoteUri,noteColumns,null,null,null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES)
            loadFinishedNotes(data);
        else if (loader.getId() == LOADER_COURSES)
            loadFinishedCourses(data);
    }

    private void loadFinishedCourses(Cursor data) {
        // associate this cursor with the adapter
        mAdapterCourses.changeCursor(data);
        mCoursesQueryFinished = true;
        displayNoteWhenQueriesFinished();
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToFirst();
        mNotesQueryFinished = true;
        displayNoteWhenQueriesFinished();
//        displayNote();
    }

    private void displayNoteWhenQueriesFinished() {
        if(mNotesQueryFinished && mCoursesQueryFinished)
            displayNote();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // close the cursor once done
        if (loader.getId() == LOADER_NOTES) {
            if (mNoteCursor == null)
                mNoteCursor.close();
        } else if (loader.getId() == LOADER_COURSES)
            mAdapterCourses.changeCursor(null);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTE_KEEPER_NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // register the channel with the system; you can't change the importance
            // or other notification behaviours after this
            mNotificationManager = getSystemService(NotificationManager.class);
            mNotificationManager.createNotificationChannel(channel);
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

    /**
     * FILTERING SQL QUERIES
     * Plan is to decouple th NoteActivity from the MainActivity.
     * Goal is have the NoteActivity read the selected note directly from the NoteKeeper.db
     *
     * Often we want only a subset of table rows rather than all the rows.
     * We can achieve that by passing a selection criteria to the query method.
     * Parts of a simple selection include;
     *  - a. column name
     *  - b. operator
     *  - c. value
     * example -> course_id = "android_intents"
     * common operators include;
     * = or ==, != or <>, >, <, =>, =<
     * LIKE -> allows us to do strings pattern matching.
     * example note_title LIKE "dynamic%" -> % represents zero or more characters
     * AND -> combines two conditions. True result only if both conditions are true
     * example -> course_id = "android_intent" AND note_title LIKE "dynamic%"
     * OR -> True result when one or both are true.
     *
     * Row selection parameters.
     * Selection is passed to a query() in two parts;
     * a. selection clause as a string -> uses ?'s as value position holders
     * b. selection value as a string array -> values replace ?'s in that order.
     * Benefits of separating selection and values are;
     * -> Protects against SQL attacks.
     * -> Helps query performance.
     *
     *
     * PERFORMING LIFE CYCLE AWARE DATA LOADING WITH LOADERS
     * We need to avoid performing queries as part of the activity's main flow.
     * As the amount of data in the database increases and as queries get more complex, queries can become time consuming.
     * Running such time consuming tasks in the main thread, can cause inconsistent UI performance.
     * May often cause "Application Not Responding Error Message"
     * So we need to find a way of running the queries on a different thread then pass the results back to the main thread.
     * We can do this manually though not necessary since Android provides a way to achieve this.
     * => Android provides a Lifecycle Aware Data Loading model that relies on something called Loaders.
     * Loaders - a. allows us to run queries on the background thread. b. Cooperates with the Activity LifeCycle.
     * We don't work directly with loaders but often with LoaderManager.
     * => LoaderManager knows how to coordinate a Loader and and an Activity.
     * => Each Activity has a single LoaderManager - which manages all the loaders for an activity.
     * therefore an activity can have one or more loaders.
     * => LoaderManager a. initiates a loader excecution.
     * and b. initiates Loader related clean up. c. -> Provides loader related notification.
     *
     * Accessing an Activity's LoaderManager
     * -> use getLoaderManager().
     * -> When we are using LoaderManager to load data, we don't load the data directly but rather ask LoaderManager
     * to load the data for us.
     * Thus we need to initialize data loading process using a method initLoader().
     * -> The method accepts an integer ID which uniquely identifies the loader within the activity.
     * -> We also need to pass the LoaderCallback interface which notifies us of the key steps in the loading process.
     * It divides processing into 3 steps;
     * a. Request for the loader i.e. create and return the loader. -> LoaderCallbacks.onCreateLoader().
     * b. Data is ready i.e. we receive reference to the cursor. -> LoaderCallbacks.onLoadFinished().
     * c. Time to clean up i.e. Tells loader that its time to clean up i.e. close associated cursor -> LoaderCallbacks.onLoadReset().
     *
     *
     * CursorLoader
     * Remember -> the purpose of the loader is to run the queries on the background thread.
     * Loader does so in a way that cooperates with the Activity's life cycle.
     * CursorLoader is designed specifically for loading cursor based data.
     * It makes working with SQLite based data easier as well as works well with a special kind of component -> Content Providers.
     * In this case since we are working with SQLite based data, we need to override loadInBackground() and then inside the
     * method issue a database query and then return the cursor.
     *
     *      * Challenges of Multiple loaders
     *      * Loaders may be running in parallel and we don't know which one will finish first
     *      * e.g. if the notes finishes before the courses then we might have a problem.
     *      * we can solve this by adding flags to each and checking against those flags
     *
     *      MAKING DATA CHANGES
     *  -> Requires a connection to the database using getWritableDatabase() of the Open helper class.
     *  -> Returns reference to the SQLiteDatabase which we use to perform the actual operations.
     *  -> Operations are still table based i.e. performed against a specific table.
     *  -> The operations will affect rows and columns within the table.
     *  -> Three basic ways we can change content of data within our database;
     *  * Update -> Modify column values of existing row(s) in a table.
     *  * Insert -> Create a new row in a table.
     *  * Delete -> Remove existing row(s) from a table.
     *
     *  Update Operation
     *  -> We use SQLiteDatabase.update().
     *  We need to provide the following;
     *  a. table name
     *  b. name of columns to change.
     *  c. new column values
     *  d. row selection criteria.
     *  the update operation returns the number of rows affected.
     *  specifying column and values -> use the ContentValues class - holds a list of column and values.
     *  specifying selection criteria - Pass selection clause and arguments.
     *
     *
     * INSERTING DATA
     * Use SQLiteDatabase.insert().
     * Specify;
     * -> table name
     * -> ContentValues with column values.
     * return value is the id of newly inserted row.
     *
     * Delete
     * SQLiteDatabase.delete()
     * -> Provide table name
     * -> Row selection criteria.
     * returns number of rows deleted
     *
     *
     * Always avoid any database action on the main thread.
     * - don't execute database operation
     * for update, insert or delete we can use variety of threading solutions available e.g.
     * AsyncTask
     * implementing database interaction
     * -> extend AsyncTask class.
     * number of methods to override.
     * @override doInBackground to add a database code.
     *
     * -> we then call the execute method and the AsyncTask takes care
     * of running the task in a background method.
     *
     *
     * ASYNC TASK
     * Most UI Work is performed on the main thread.
     * Many pragmatic UI operations only allowed to occur on the main thread.
     *
     * Doing Work on the Background Thread.
     * -> long-running work should be performed on the background thread.
     * When the work is done, we want to present the work results.
     *    - this may require interacting with the UI.
     *
     *   Doing Work with Async Task
     *   Async Task  class;
     *      - Manages the threading details.
     *      - Provides methods for each phase.
     *      - Methods are run on the appropriate thread.
     *  To work with AsyncTask;
     *  a) Define new class that extends AsyncTask
     *  b) Override the appropriate methods.
     *
     *  Methods;
     *  a) doInBackground()
     *      - runs in the background thread.
     *      - add code to do the actual work.
     *
     * b) onPostExecute()
     *      - runs on the main thread.
     *      - add code to present work results.
     *
     * Passing Data between AsyncTask methods;
     * -> To indicate AsyncTask processing, we call the execute() on AsyncTask.
     * execute() -> Accepts a variable length parameter list.
     * AsyncTask takes care of passing parameters to the doInBackground().
     * That allows doInBackground() to make use of the values passed in the execute().
     * When done, it needs to present the work results.
     *
     * Providing background results
     * -> doInBackground() has a return type.
     * -> AsyncTask takes care of passing the return value to the onPostExecute().
     * -> Each of these data values needs to have a type.
     * AsyncTask task = new AsyncTask<
     * Type1, -> type we want to pass to the doInBackground
     * Void, -> optional -> see later
     * Type3 -> return type of the doInBackground
     * >(){
     * @override
     * protected Type3 doInBackground(Type1 ...param) {
     *     Type3 result = //result of work
     *     return result;
     * }
     *
     * @override
     * protected void onPostExecute(Type3 t3) {
     *     // set the UI with the value
     * }
     * }
     *
     * Providing Progress Updates with AsyncTask
     *AsyncTask provides a way to pass progress information from the background thread to the main thread.
     * We need to tell AsyncTask what type of information that ought to be
     * -> The purpose of the second argument to AsyncTask ( Void -> Integer )
     * @override
     * onProgressUpdate()
     *
     * Working with Handlers
     * The main thread has a special kind of thread known as the Looper thread.
     * A Looper thread has a Looper and a MessageQueue.
     * The way the main thread receives the work that it is supposed to do is that the work is fed into the
     * MessageQueue (work such as user interaction, system events).
     * The Looper then reads that work off the Message Queue , it takes that work and dispatches it into something
     * called the Handler.
     * It is the Handler that then actually performs the work.
     * When our application is started up, our app has a default handler that does most of the System work.
     * In addition to the Handler that the System creates, we can also create our own Handler and associate with a Looper.
     *
     * Working with LooperThread.
     * Has a Looper and a MessageQueue.
     * It has the ability to dispatch work instances off to the handler.
     * Main thread is the common instance of the Looper thread that we can interact with.
     * However, our application can create additional looper threads if needed.
     * Handlers are the main point of interaction because it allows us to do the work we need to do in our looper thread.
     * We can also use Handler instance to enqueue work into the Message Queue of the looper thread.
     * When the looper encounters that work, it will then dispatch that work back to our Handler instances.
     *
     * Constructing a Handler
     * Must be associated with a looper.
     * By default i.e. a default constructor uses the current thread's looper.
     * But sometimes, we want to create a Handler on the background thread and the associate it with our main thread.
     * We can associate with the main thread by constructing with Looper.getMainLooper.
     * Allows us to get looper associated with the main application thread. and construct handler to associate with that looper.
     * Once handler is created, it is bound to that looper.
     * Therefore, you can enqueue work from any thread. Work will always be performed on the thread of associated looper.
     *
     * Primary uses of Handlers therefore are;
     * a) Sending work to one thread from another  -> e.g. how Async Task works.
     * b) Schedule work for future execution i.e. we can indicate in a MessageQueue that we don't want work until sometime
     * in the near future.
     * Looper will take care of dispatching that work to our handler at the right time.
     * Exmple use case -> MainActivity onResume() - opening drawer.
     *
     *
     */
}