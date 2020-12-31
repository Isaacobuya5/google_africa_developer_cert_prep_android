package com.isaac.practice.notekeeper;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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


@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {

    static DataManager sDataManager;

    @Rule
    public ActivityScenarioRule<NoteListActivity> mNoteListActivityRule = new ActivityScenarioRule<>(NoteListActivity.class);

    @BeforeClass
    public static void classSetUp() {
        sDataManager = DataManager.getInstance();
    }

    @Test
    public void createNewNote() {
        final CourseInfo course = sDataManager.getCourse("java_lang");
        final String noteTitle = "My test note title";
        final String noteText = "My test note body";
        // getting reference to the fab and click to create a new note
        onView(withId(R.id.fab)).perform(click());

        onView(withId(R.id.spinner_courses)).perform(click());
        onData(allOf(instanceOf(CourseInfo.class),equalTo(course)))
                .perform(click()); // display data in spinner
        // spinner requires clicking on selected item
        onView(withId(R.id.spinner_courses))
                .check(matches(withSpinnerText(containsString(course.getTitle())))); // performing assertions

        // typing note title and note text
        onView(withId(R.id.text_note_title))
                .perform(typeText(noteTitle))
                .check(matches(withText(containsString(noteTitle))));
        onView(withId(R.id.text_note_text)) // getting View Interaction
                .perform(typeText(noteText), closeSoftKeyboard()) // performing action(s)
                .check(matches(withText(containsString(noteText)))); // performing assertions

        // pressing the back button
        pressBack();

        // Testing logic behaviour i.e. has the note really been added
        int noteIndex = sDataManager.getNotes().size() - 1;
        // get note at that position
        NoteInfo newNote = sDataManager.getNotes().get(noteIndex);
        // perform assertions
        assertEquals(newNote.getCourse(), course);
        assertEquals(newNote.getTitle(), noteTitle);
        assertEquals(newNote.getText(), noteText);
    }

    /**
     * INSTRUMENTATION TEST
     * Involves testing the Android based behaviour.
     *  Are run on an emulator or physical Android device.
     *  Shares a number of Similarities with the local JVM test i.e.
     *  Uses JUnit4 to test
     *  Test methods are marked with @Test annotation.
     *  Supports pre/post processing methods.
     *  Uses Assert class tp:
     *      - indicate expectations
     *      - fail if expectations are not met.
     *  Tests are still managed by Android Studio i.e.
     *      - run/debug tests
     *      - single test/ group tests/ all tests
     *      - display test results
     *
     *   However, there are also some differences with local JVM tests i.e.
     *   Are organized separate from local JVM tests i.e. inside androidTest source set.
     *   Relies on AndroidJunitTestRunner
     *   Class must be marked with @RunWith annotation passing in AndroidJunit4.class to indicate we want to use AndroidJunitTestRunner
     *   Requires Android environment i.e. emulator or physical android device.
     *
     *   Two kinds of Instrumentation tests include; -
     *   Intrumented Unit test and
     *   Instrumented UI test (kind of Intergration test).
     *
     *   CREATING UI TEST INTERACTION
     *   UI tests require a series of view interactions i.e
     *   a. We need a way to specify the view of interest
     *   b. a way to specify ACTION that we want to perform on that view.
     *
     *   We use Espresso.onView() to specify the view matching criteria i.e. criteria to get to the view we want to interact with.
     *   -> It returns a ViewInteraction reference associated with the matched view.
     *   - We can then use the ViewInteraction reference to to perform an action on that view.
     *
     *   SPECIFYING MATCHING CRITERIA FOR THE VIEW.
     *   We use Hamcrest matchers which allows us to specify matching criteria declaratively.
     *   Hacrest matchers however, are general purpose Java framework for doing matching.
     *   Specific to Android, we have the ViewMatchers class which provides matchers for matching Android views.
     *   -> Its methods return a Hamcrest matcher.
     *   ViewMatchers can easily be combined with Hamcrest general purpose matchers.
     *   Some of the ViewMatchers class methods include;
     *   a. withId() -> match based on ID property of the view.
     *   b. withText() -> match view based on text property.
     *   c. isDisplayed() -> match view currently displayed on the screen.
     *   d. isChecked() -> match currently checked checkable view e.g. switch, checkbox etc.
     *
     *   Some example Hamcrest matchers are;
     *   equalTo() -> Match based on the equals method.
     *   instanceOf() -> Match based on an object type.
     *   allOf() -> accepts multiple matchers i.e. matches if all passed matchers match.
     *   anyOf() -> accepts multiple matchers - matches if any of the passed matchers match.
     *
     *   PERFORMING VIEW ACTION
     *   -> ViewInteraction reference returned from Espresso.onView() has a method on it called perform().
     *   ViewInteraction.perform() allows us to perform one or more specified actions on the view.
     *   Specific actions are passed as  parameter(s).
     *   -> We use the ViewActions class to do that. It provides action methods and each method returns an action.
     *
     *   Example ViewActions methods;
     *   click() -> click on view.
     *   typeText() -> type text into the view.
     *   replaceText() -> replace view text.
     *   closeSoftKeyboard() -> closes soft keyboard.
     *
     *   STARTING TARGET ACTIVITY
     *   This is where the ActivityTestRule comes in;
     *   -> It automates the test activity life time i.e. starts activity before each test and terminates activity after test.
     *   The Activity life includes @Before and @After methods.
     *   We use it declaratively i.e.;
     *   a. Declare and Initialize as test class field and pass Desired activity as a type parameter.
     *   b. Mark that field with the @Rule annotation
     *   -> The ActivityTestRule does the job of creating and destroying the activity for us.
     *
     *   DEPENDENCIES
     *   Ensure the following packages are in Gradle file (module:app)
     *       androidTestImplementation 'androidx.test.ext:junit:1.1.2'
     *     androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
     *
     *
     * TESTING ADAPTER VIEWS AND BACK BUTTON
     * AdapterView derived views normally load their data from Adapter classes.
     * The challenge is that these views display multiple items but only a subset of the data may be loaded.
     * Therefore, rather than targetting the View, we need to implement test selection based on the target data.
     * We need some mechanism that will take care of changing the window of display data to correspond to the selection we are
     * trying to make and give as back the view associated with that selected data.
     * Espresso.onData() allows us to specify matches based on the target data.
     * In this case, since we are targetting data and not views, we use general purpose Hamcrest matchers
     * It returns a refernce to the DataInteraction which provides a number of methods for interracting/narrowing a match
     * corresponding to our selection.
     * Most often, we tend to use DataInteraction.perform() which allows us to perform action on a top level view for the entry
     * in the Adapter view. (Generally, we are clicking on item to select.
     *
     *
     * ADDING ASSERTIONS
     * Tests are meant to confirm "logic behaviour" or  "UI Behaviour"
     * We use ViewInteraction.check() to confirm some of the aspects of the View
     * passing in method(s) of ViewAssertion class that provides methods for performing assertions against the Views.
     * Common Assertion methods are;
     * a. matches - confirm the view matches the passed matcher
     * -commonly used in conjuction with ViewMatchers
     * - also confirm the View exists
     * b. doesNotExist provide ViewInteraction that identifies some view and text if that view doesn't exist.
     *
     * To verfify logic, we use the methods on the Assert class.
     */
}