package com.isaac.practice.notekeeper;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.isaac.practice.notekeeper.adapters.CoursesRecyclerAdapter;
import com.isaac.practice.notekeeper.adapters.NotesRecyclerAdapter;
import com.isaac.practice.notekeeper.database.NoteKeeperDatabaseContract;
import com.isaac.practice.notekeeper.database.NoteKeeperOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static com.isaac.practice.notekeeper.NoteKeeperProviderContract.*;
import static com.isaac.practice.notekeeper.database.NoteKeeperDatabaseContract.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>{

    public static final int LOADER_NOTES = 0;
    private NotesRecyclerAdapter mNotesRecyclerAdapter;
    private RecyclerView mRecyclerItems;
    private LinearLayoutManager mNotesLayoutManager;
    private GridLayoutManager mGridLayoutManager;
    private CoursesRecyclerAdapter mCoursesRecyclerAdapter;
    private NoteKeeperOpenHelper mNoteKeeperOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Using StrictMode to detect undesirable operations
        enableStrictMode();

        mNoteKeeperOpenHelper = new NoteKeeperOpenHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });

        // setting default settings on shared preference - prevent override upon setting value
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.sync_preferences, false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // handling the hambugger icon behaviour
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initializeDisplayContent();

    }

    private void enableStrictMode() {
        // should only be used in debugging / testing mode
        if (BuildConfig.DEBUG) {
            // building a thread policy
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll() // detect undesirable operation
                    .penaltyLog() // set desired action to take.
                    .build();

            StrictMode.setThreadPolicy(policy);
        }
    }

    private void initializeDisplayContent() {

        // load notes and courses from the database
        DataManager.loadFromDatabase(mNoteKeeperOpenHelper);

        // getting a reference to recyclerview
        mRecyclerItems = (RecyclerView) findViewById(R.id.list_items);
        // LayoutManager
        mNotesLayoutManager = new LinearLayoutManager(this);

        // LayoutManager for courses
        mGridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.course_grid_span));

        // get content to place in the list
        // we are now getting notes from database
//        List<NoteInfo> notes = DataManager.getInstance().getNotes();

        // get list of courses
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        // our adapter here

        // our adapter
        mNotesRecyclerAdapter = new NotesRecyclerAdapter(null, this);
        mCoursesRecyclerAdapter = new CoursesRecyclerAdapter(courses, this);

        displayNotes();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // need a way to tell adapter that list has changed due to new note creation
//        mNotesRecyclerAdapter.notifyDataSetChanged();

        // load notes from the database
//        loadNotes(); -> we want to perform on a different thread
        LoaderManager.getInstance(this).restartLoader(LOADER_NOTES, null, this);
        // we want to update the nav header when we return to this activity from settings screen
        updateNavHeader();

        openDrawer();
    }

    private void openDrawer() {
        // create a handler instance and associate with LooperThread of the Main Thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(Gravity.LEFT);
            }
        },1000);
    }

    private void loadNotes() {
        SQLiteDatabase db = mNoteKeeperOpenHelper.getReadableDatabase();
        // issue query to read notes
        final String[] noteColumns = {
                NoteInfoEntry._ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT,
                NoteInfoEntry.COLUMN_COURSE_ID
        };

        String notesOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;

        final Cursor noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, null, null, null, null, notesOrderBy);
        // associate this cursor with notes recycler adapter
        mNotesRecyclerAdapter.changeCursor(noteCursor);
    }

    private void updateNavHeader() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView txtUsername = (TextView) headerView.findViewById(R.id.text_user_name);
        TextView txtEmailAddress = (TextView) headerView.findViewById(R.id.text_user_email);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String username = pref.getString("user_display_name", "");
        String emailAddress = pref.getString("pref_user_email_addres", "");

        txtUsername.setText(username);
        txtEmailAddress.setText(emailAddress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // do any default thing when back button pressed.
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_backup_notes) {
            backupNotes();
        }
        return super.onOptionsItemSelected(item);
    }

    private void backupNotes() {
        Intent intent = new Intent(this, NoteBackupService.class);
        intent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
        startService(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            displayNotes();
        } else if(id == R.id.nav_courses) {
            displayCourses();
        } else if(id == R.id.nav_share) {
            handleShare();
        } else if(id == R.id.nav_send) {
            handleSelection(R.string.nav_send_message);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
        }

    private void handleShare() {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, "Share to - " + PreferenceManager.getDefaultSharedPreferences(this).getString("user_favorite_social_network", ""), Snackbar.LENGTH_LONG).show();
    }

    private void displayCourses() {
        mRecyclerItems.setLayoutManager(mGridLayoutManager);
        mRecyclerItems.setAdapter(mCoursesRecyclerAdapter);
        setMenuChecked(R.id.nav_courses);
    }

    private void displayNotes() {
        // associate layout manager with the recyclerview
        mRecyclerItems.setLayoutManager(mNotesLayoutManager);
        // associate adapter with recycler view
        mRecyclerItems.setAdapter(mNotesRecyclerAdapter);

        setMenuChecked(R.id.nav_notes);
    }

    private void setMenuChecked(int menu_id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        // getting menus within navigation view
        Menu menu = navigationView.getMenu();

        // expensive operation
        SQLiteDatabase db = mNoteKeeperOpenHelper.getReadableDatabase();

        // set the menu to checked
        menu.findItem(menu_id).setChecked(true);
    }

    private void handleSelection(int messageId) {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, messageId, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        mNoteKeeperOpenHelper.close();
        super.onDestroy();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_NOTES) {
            final String[] noteColumns = {
//                    NoteInfoEntry.getQName(NoteInfoEntry._ID),
                    Notes._ID,
                    Notes.COLUMN_NOTE_TITLE,
                    Notes.COLUMN_COURSE_TITLE
            };
//            final String notesOrderBy = CourseInfoEntry.COLUMN_COURSE_TITLE + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
            final String notesOrderBy = Notes.COLUMN_COURSE_TITLE + "," + Notes.COLUMN_NOTE_TITLE;

//            loader = new CursorLoader(this) {
//                @Override
//                public Cursor loadInBackground() {
//                    SQLiteDatabase db = mNoteKeeperOpenHelper.getReadableDatabase();
//                    final String[] noteColumns = {
//                            NoteInfoEntry.getQName(NoteInfoEntry._ID),
//                            NoteInfoEntry.COLUMN_NOTE_TITLE,
//                            CourseInfoEntry.COLUMN_COURSE_TITLE
//                    };
//
//                    String notesOrderBy = CourseInfoEntry.COLUMN_COURSE_TITLE + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
//
//                    String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " +
//                            CourseInfoEntry.TABLE_NAME + " ON " +
//                            NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
//                            CourseInfoEntry.getQName( CourseInfoEntry.COLUMN_COURSE_ID);
//
//                    return db.query(tablesWithJoin, noteColumns, null, null, null, null, notesOrderBy);
//                }
            loader = new CursorLoader(this, Notes.CONTENT_EXPANDED_URI, noteColumns,null,null,notesOrderBy);
        };
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES)
            mNotesRecyclerAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES)
            mNotesRecyclerAdapter.changeCursor(null);
    }

    /**
     * TESTING MENU, NAVIGATION DRAWER AND RECYCLERVIEW
     * Is much like testing other views i.e;
     * -> Still relies on the ViewInteraction class.
     * -> We find view by matchers and execute actions on the ViewInteraction object returned.
     * -> However, the differences comes with the Action classes used more so for navigation drawer and recyclerview.
     *
     * NAVIGATION DRAWER TESTS
     * we use DrawerActions class - to interact with our drawer such as open/close drawer.
     * We use NavigationViewActions class - to interact with our NavigationView i.e. select an option.
     *
     * RECYCLERVIEW TESTS
     * We use RecyclerViewActions class to interact with the RecyclerView such as scrolling or Perform actions
     * on RecyclerView items.
     *
     *
     * CONTENT PROVIDER
     * -> Is an android component that enscapulates data, exposes that data through a standard interface
     * and optionally makes that data available to multiple programs.
     * Content Provider is therefore;
     * * a way to expose data
     * * Data is exposed via standard API.
     *
     * Content provider visibility
     * -> Can be limited to an app that implements it.
     * -> Can also be available to other apps.
     *
     * Concept is independent of data storage.
     * -> Can serve both local and remote data.
     * -> Can merge both data
     * -> Static and/or progmatically-produced data.
     * -> Implementation enscapulates those details.
     *
     * SQLite vs Content Provider.
     * SQLite
     * -> Is a data storage and management solution.
     * -> Data accessed through SQLite library.
     * -> Accessible by apps that owns file.
     *
     * Content Providers
     * -> A Data access solution.
     * -> Data accessed through a standard API.
     * -> Access can be available to other apps.
     *
     * Content Provider Common Implementations
     * -> Most commonly we implement them in conjuction with SQLite.
     * -> SQLite provides the backing store i.e. data stoarge.
     * -> Content Providers enscapulates the details.
     * -> May make data available to other apps.
     *
     * Creating a Content Provider
     * Extend ContentProvider class.
     * Remember they are also components and therefore have a life cycle associated with them.
     * We must therefore implement the life cycle methods.
     * Implement data lookup.
     * -Implement data modification methods.
     *
     * Content Provider identification
     * -> Content Provider may be visible throughout the device.
     * -> Identification should be globally unique.
     * -> The way we do this is to associate an authority
     *  * which uniquely identifies content provider.
     *  Use reverse domain name format.
     *  Normally use app package name followed by the provider.
     *
     *  -> Decide visibility to other apps
     *  On older devices, it defaults to being visible to other apps
     *  while on new devices, it defaults to being not visible to other apps.
     *  -> We therefore need to mark it as "exported" to make it available.
     *-> Enabled - means the system can start this content provider.
     *
     * Implementing a Content Provider
     * -> Most often we normally implement on to of SQLite
     * -> Should enscapulate all database related details
     * -> Performs all database interaction methods.
     *
     * It exposes methods similar to SQLite API
     * -> Data requests are handled with the query method.
     * -> Delegate work to SQLite.
     *
     *
     * Requesting data from Content Provider
     * Similar to querying SQLite
     * -> Avoid performing on main thread.
     *
     * Use CursorLoader
     * -> Which runs query on the background thread.
     * -> Cooperates with the activity lifecycle.
     *-> Initiate query process using LoaderManager.
     *
     * CursorLoader understands querying ContentProviders
     * No need to overload in loadInBackground.
     * Pass content provider identifier to CursorLoader constructor.
     * Then CursorLoader takes the details of locating the Content Provider and issuing the query.
     *
     * We use Universal Resource Identifiers i.e. Uri to identify the Content Providers.
     * Has a URI scheme of content i.e. content://com....
     *
     *      * ANDROID THREADING
     *      * It is important to protect the main thread because it is responsible for maintaining the User experience.
     *      * Because some operations are often complex, you may be performing unexpected operation on the main thread.
     *      * Some examples of long running operations include;
     *      *  -> Reading from the "disk storage".
     *      *  -> Writing to the "disk storage".
     *      *  -> Interacting with the network.
     *      *
     *      *  To help us detect these undesirable operations, Android provides the StrictMode class;
     *      *  StrictMode class can;
     *      *    -> detect undesirable operations.
     *      *    -> Enforces penalties when detected.
     *      *  We can therefore use it in the debugging/testing phase of our application.
     *      *  We build a desired thread policy i.e. what we want to detect and what we want the penalties to be.
     *      *  Then we set that policy at app/activity start.
     *      *
     *      *  Setting the Thread Policy
     *      *  -> We use StrictMode.setThreadPolicy - accepts an instance of StrictMode.ThreadPolicy.
     *      *
     *      *  Creating the ThreadPolicy
     *      *  -> We use a builder pattern to create a thread policy.
     *      *  -> We have a class StrictMode.ThreadPolicy.Builder
     *      *  2 Phases;
     *      *      a) set whatever we want to detect.
     *      *      detectDiskReads, detectDiskWrites, detectNetwork, detectAll
     *      *      b) decide what we want the penalties to be
     *      *      penaltyLog, penaltyException, penaltyDialog, penaltyDeath
     *
     *      PERFORMING BACK GROUND WORK WITH SERVICES
     *      Performing Background work on Activities have some limitations;
     *      Activities can initiate background work using - CursorLoader, AsyncTask etc.
     *      Limitations;
     *      Activity have a life time.
     *      -> The life time of an activity is tied to user interaction.
     *      -> Activity life time ending can impact the background work life time.
     *      Therefore, the background thread may get cleaned up before the work is complete.
     *
     *      Background Work and Services
     *      -> Services allow us to perform non-UI work within our application.
     *      -> Services make Android aware that the work we are doing is a meaningful work even though we are not presenting a User Interface.
     *
     *      Android keeps the process alive while the background work within the Service continues.
     *      Once service is done, it gets cleaned up and then the process is cleaned up.
     *
     *      Service
     *      -> Android is a component oriented platform -> Activity, ContentProviders, Service
     *      Service therefore being an Android component also has a life time and does not present a UI.
     *      Services allows us to perform long running background work.
     *              - used for performing work longer than few seconds.
     *              - continues running even if the user switches to another app.
     *       We submit work to a service using an intent.
     *       We create intent similar to activity's intent.
     *       Associate any needed extras.
     *       Pass intent to context.startService(intent).
     *
     *       Implementing a Service
     *       Services extend the Service class;
     *       Service class provides;
     *        * lifecycle methods.
     *        * method to receive work
     *        -> Developer is left to handle a lot of details.
     * Therefore, if we implement a service by directly inheriting from the service class, we have to deal with;
     *          a. Threading Behaviour
     *          - Work is received on the main thread, therefore we need to dispatch the work on a different thread.
     *          b. Handling of multiple work submissions.
     *          System will start service when needed. We are limited to one running instance of a service at a time.
     *          -> Additional work submissions are sent to that running service.
     *          Therefore, we do not want to wait until the existing work is finished. OR
     *          We want to spin up another thread and do the work at the same time
     *          c. Service lifetime
     *          -> Determine when to shutdown.
     *          -> Determine how to behave when shutdown by the Android system.
     *
     *
     *          Alternative way of Implementing a Service
     *          Implementing a Service
     *          -> IntentService class allows us to implement a Service without having to deal with all those details.
     *          -> Simplifies Service implementation thus it deals with thr issues above;
     *          a. Threading Issues
     *          -> Creates a background LooperThread that is separate from our main application thread.
     *          Work is then performed on this background LooperThread.
     *          b. Dealing with multiple work submissions
     *          Remember, LooperThread has a Message Queue.
     *              -> Each work that comes in is placed in the MessageQueue.
     *              -> Submission is done one at a time.
     *              -> Submission is performed in the order received.
     *
     *           c. Services lifetime
     *           Shutdown when the current work is complete, and there is no more in the queue.
     *
     *           Implementation
     *           -> We extend the IntentService class
     *              -> Provide the default constructor that calls the base class constructor.
     *              -> Pass the string containing the service name / used primarily for debugging purposes.
     *              -> override the appropriate methods;
     *              * onHandleIntent
     *               -> Receives intent passed to the startService.
     *               -> Performing Service work in this method.
     *               -> Runs on the background LooperThread.
     *
     *               * We can also override other service methods if needed;
     *                  -> Be sure to call the base implementation.
     *                  -> Helpful when specific work is needed at service creation, destruction etc.
     *
     *
     *
     */
}
