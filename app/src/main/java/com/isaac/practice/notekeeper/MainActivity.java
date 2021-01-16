package com.isaac.practice.notekeeper;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.isaac.practice.notekeeper.adapters.CoursesRecyclerAdapter;
import com.isaac.practice.notekeeper.adapters.NotesRecyclerAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });
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
        mGridLayoutManager = new GridLayoutManager(this, 2);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            displayNotes();
        } else if(id == R.id.nav_courses) {
            displayCourses();
        } else if(id == R.id.nav_send) {
            handleSelection("Don't you think you've shared enough");
        } else if(id == R.id.nav_send) {
            handleSelection("You can send here");
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        // set the menu to checked
        menu.findItem(menu_id).setChecked(true);
    }

    private void handleSelection(String message) {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
}
