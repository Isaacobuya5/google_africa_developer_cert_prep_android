package com.isaac.practice.notekeeper;

import org.junit.Test;

import static org.junit.Assert.*;

public class DataManagerTest {

    @Test
    public void createNewNote() {
        // To create a new note we need course, noteTitle and noteText.
        DataManager dm = DataManager.getInstance();
        final CourseInfo course = dm.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText = "This is my test body";
        // create a new note and get the index
        int noteIndex = dm.createNewNote();
        // get note at that particular position
        NoteInfo newNote = dm.getNotes().get(noteIndex);
        newNote.setCourse(course);
        newNote.setTitle(noteTitle);
        newNote.setText(noteText);
        // now we have a note fully set,, time to assert
        // get a note from that index
        NoteInfo compareNote = dm.getNotes().get(noteIndex);
        // assert
        assertEquals(compareNote.getCourse(), newNote.getCourse());
        assertEquals(compareNote.getTitle(), newNote.getTitle());
        assertEquals(compareNote.getText(), newNote.getText());
    }

    @Test
    public void findSimilarNotes() {
        DataManager dm = DataManager.getInstance();
        final CourseInfo course = dm.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText1 = "This is the body text of my test note";
        final String noteText2  = "This is the body of my second test note";

        int noteIndex1 = dm.createNewNote();
        NoteInfo newNote1 = dm.getNotes().get(noteIndex1);
        newNote1.setCourse(course);
        newNote1.setTitle(noteTitle);
        newNote1.setText(noteText1);

        int noteIndex2 = dm.createNewNote();
        NoteInfo newNote2 = dm.getNotes().get(noteIndex2);
        newNote2.setCourse(course);
        newNote2.setTitle(noteTitle);
        newNote2.setText(noteText2);

        int foundIndex1 = dm.findNote(newNote1);
        assertEquals(noteIndex1, foundIndex1);

        int foundIndex2 = dm.findNote(newNote2);
        assertEquals(noteIndex2, foundIndex2);
    }

    /**
     * Testing needs to be a core task because it is essential to delivering a quality software.
     * In android we focus mainly on functional testing which invloves verifying that a piece of
     * code behaves as expected.
     * 2 main aspects of tesing in Android;
     * a. unit testing - test individual unit of a code i.e. feature/behaviour.
     * b. intergration testing - testing how the pieces come together i.e. application behaviour (mostly involves testing UI).
     *
     * Challenges of Testing Android Apps
     * -> Full testing normally requires Android environment i.e. emulator / physical device.
     * Strategy for testing in Android therefore involves;
     * a. Testing Java based behaviour locally leveraging the JVM on Desktop.
     * b. Testing Android based behaviour on Android environment.
     *
     * Local JVM tests
     * Android Studio sets up separate source set for JVM testing.
     * Android studio makes it easy to manage tests i.e.;
     * -> can run/debug test.
     * -> single test, group test or all tests.
     * -> Displays test results.
     * Each unit test is a separate method marked with @Test annotation. JUnit takes care of running those methods.
     *
     * Organization of tests
     * -> Tests are grouped within classes which provides purely organization convenience.
     * -> These classes allows us to execute tests in a grouping.
     * -> Also allows us to set up setup/teardown behaviours for groupings.
     *
     * Testing with JUnit.
     * JUnit provides us with an Assert class which allows us to indicate the expected results.
     * -> It Fail tests when expectations are not met.
     * -> Methods provided by Assert class includes;
     *  - assertSame(object1, object2) - asserts if two references points to the same object.
     *  - assertEquals(object1, object2) - asserts if two objects/ variables are equal.
     *  - assertNull() - asserts null reference.
     *
     *  Steps to creating JVM test
     *  -> Open the Class whose method you want to test then press CTRL + SHIFT + T.
     *  Click Create New Test >> Verify the Details >> Choose method(s) to test >> OK.
     *
     *  Dealing with failed test
     *  2 main reasons that may cause test failure are;
     *  - Exception thrown
     *  - Assertion failure
     *  Use "Debug Test" option
     *
     * Assuring Test Consistency
     * -> Tests must be reliably i.e.
     *  - should not depend on another test
     *  - should not be impacted by side effects of other tests.
     *  Therefore to assure consistency, tests should normally start from the same state since test order is not guranteed.
     *  We therefore need ways to set/reset the state of the test.
     *  Possible solutions;
     *  a. Test Pre-processing
     * @Before annotation - runs before each test in a class.
     * @BeforeClass annotation - runs once before all tests in that class. Method must be static.
     * b. Test Post-processing
     * @After - runs after each test in a class
     * @AfterClass - runs once after all tests in a class. Method must be static.
     *
     */
}