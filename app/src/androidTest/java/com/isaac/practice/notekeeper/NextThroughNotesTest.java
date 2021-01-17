package com.isaac.practice.notekeeper;

import static org.junit.Assert.*;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static org.hamcrest.Matchers.*;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;

import java.util.List;


@RunWith(AndroidJUnit4.class)
public class NextThroughNotesTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void NextThroughNotes() {
        //open navigation drawer
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        // select notes
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_notes));
        // select first note within RecyclerView
        onView(withId(R.id.list_items)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // getting a reference to the NoteList from the DataManager
        List<NoteInfo> notes = DataManager.getInstance().getNotes();

        // the index of the first note
        int index = 0;
        // get note at that index
        NoteInfo note = notes.get(index);
        // check if the correct note has been display
        // spinner
        onView(withId(R.id.spinner_courses)).check(matches(withSpinnerText(note.getCourse().getTitle())));
        // note title
        onView(withId(R.id.text_note_title)).check(matches(withText(note.getTitle())));
        // note text
        onView(withId(R.id.text_note_text)).check(matches(withText(note.getText())));
    }
}