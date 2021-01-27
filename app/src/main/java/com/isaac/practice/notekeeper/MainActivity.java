package com.isaac.practice.notekeeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.isaac.practice.notekeeper.adapters.CoursesRecyclerAdapter;
import com.isaac.practice.notekeeper.adapters.NotesRecyclerAdapter;
import com.isaac.practice.notekeeper.database.NoteKeeperOpenHelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

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

    private void initializeDisplayContent() {
        // getting a reference to recyclerview
        mRecyclerItems = (RecyclerView) findViewById(R.id.list_items);
        // LayoutManager
        mNotesLayoutManager = new LinearLayoutManager(this);

        // LayoutManager for courses
        mGridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.course_grid_span));

        // get content to place in the list
        List<NoteInfo> notes = DataManager.getInstance().getNotes();

        // get list of courses
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        // our adapter here

        // our adapter
        mNotesRecyclerAdapter = new NotesRecyclerAdapter(notes, this);
        mCoursesRecyclerAdapter = new CoursesRecyclerAdapter(courses, this);

        displayNotes();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // need a way to tell adapter that list has changed due to new note creation
        mNotesRecyclerAdapter.notifyDataSetChanged();
        // we want to update the nav header when we return to this activity from settings screen
        updateNavHeader();
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
        }
        return super.onOptionsItemSelected(item);
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
     */
}
