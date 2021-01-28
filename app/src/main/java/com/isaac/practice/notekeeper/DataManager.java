package com.isaac.practice.notekeeper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.isaac.practice.notekeeper.database.NoteKeeperDatabaseContract;
import com.isaac.practice.notekeeper.database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.isaac.practice.notekeeper.database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.isaac.practice.notekeeper.database.NoteKeeperOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager ourInstance = null;

    private List<CourseInfo> mCourses = new ArrayList<>();
    private List<NoteInfo> mNotes = new ArrayList<>();

    public static DataManager getInstance() {
        if(ourInstance == null) {
            ourInstance = new DataManager();
            // no longer loading notes from the data manager
            // instead, we want to load from the database.

//            ourInstance.initializeCourses();
//            ourInstance.initializeExampleNotes();
        }
        return ourInstance;
    }

    public static void loadFromDatabase(NoteKeeperOpenHelper dbHelper) {
        // get connection to the database
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // issue query to the database to read courses
        final String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry.COLUMN_COURSE_TITLE
        };
        final Cursor courseCursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns, null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE + " DESC");
        // walk through the results while populating the list
        loadCoursesFromDatabase(courseCursor);

        // issue query to read notes
        final String[] noteColumns = {
                NoteInfoEntry._ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT,
                NoteInfoEntry.COLUMN_COURSE_ID
        };

        String notesOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;

        final Cursor noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, null, null, null, null, notesOrderBy);
        loadNotesFromDatabase(noteCursor);
    }

    private static void loadNotesFromDatabase(Cursor cursor) {
        // get column positions
        int idPos = cursor.getColumnIndex(NoteInfoEntry._ID);
        int noteTitlePos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        int noteTextPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        int courseIdPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);

        // clear list initially
        DataManager dm = getInstance();
        dm.mNotes.clear();

        while(cursor.moveToNext()) {
            int id = cursor.getInt(idPos);
            String noteTitle = cursor.getString(noteTitlePos);
            String noteText = cursor.getString(noteTextPos);
            String courseId = cursor.getString(courseIdPos);

            // get the particular course
            CourseInfo noteCourse = dm.getCourse(courseId);
            NoteInfo note = new NoteInfo(noteCourse, noteTitle, noteText,id);

            dm.mNotes.add(note);
        }
        cursor.close();
    }

    private static void loadCoursesFromDatabase(Cursor cursor) {
        // getting the column index
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseTitlePos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);

        // start with a fresh list each time
        DataManager dm = getInstance();
        dm.mCourses.clear();

        while(cursor.moveToNext()) {
            String courseId = cursor.getString(courseIdPos);
            String courseTitle = cursor.getString(courseTitlePos);
            // construct new course
            CourseInfo course = new CourseInfo(courseId,courseTitle, null);
            // add to the list
            dm.mCourses.add(course);
        }
        // close the cursor once done
        cursor.close();
    }

    public String getCurrentUserName() {
        return "Jim Wilson";
    }

    public String getCurrentUserEmail() {
        return "jimw@jwhh.com";
    }

    public List<NoteInfo> getNotes() {
        return mNotes;
    }

    public int createNewNote() {
        NoteInfo note = new NoteInfo(null, null, null);
        mNotes.add(note);
        return mNotes.size() - 1;
    }

    public int findNote(NoteInfo note) {
        for(int index = 0; index < mNotes.size(); index++) {
            if(note.equals(mNotes.get(index)))
                return index;
        }

        return -1;
    }

    public void removeNote(int index) {
        mNotes.remove(index);
    }

    public List<CourseInfo> getCourses() {
        return mCourses;
    }

    public CourseInfo getCourse(String id) {
        for (CourseInfo course : mCourses) {
            if (id.equals(course.getCourseId()))
                return course;
        }
        return null;
    }

    public List<NoteInfo> getNotes(CourseInfo course) {
        ArrayList<NoteInfo> notes = new ArrayList<>();
        for(NoteInfo note:mNotes) {
            if(course.equals(note.getCourse()))
                notes.add(note);
        }
        return notes;
    }

    public int getNoteCount(CourseInfo course) {
        int count = 0;
        for(NoteInfo note:mNotes) {
            if(course.equals(note.getCourse()))
                count++;
        }
        return count;
    }

    private DataManager() {
    }

    //region Initialization code

    private void initializeCourses() {
        mCourses.add(initializeCourse1());
        mCourses.add(initializeCourse2());
        mCourses.add(initializeCourse3());
        mCourses.add(initializeCourse4());
    }

    public void initializeExampleNotes() {
        final DataManager dm = getInstance();

        CourseInfo course = dm.getCourse("android_intents");
        course.getModule("android_intents_m01").setComplete(true);
        course.getModule("android_intents_m02").setComplete(true);
        course.getModule("android_intents_m03").setComplete(true);
        mNotes.add(new NoteInfo(course, "Dynamic intent resolution",
                "Wow, intents allow components to be resolved at runtime"));
        mNotes.add(new NoteInfo(course, "Delegating intents",
                "PendingIntents are powerful; they delegate much more than just a component invocation"));

        course = dm.getCourse("android_async");
        course.getModule("android_async_m01").setComplete(true);
        course.getModule("android_async_m02").setComplete(true);
        mNotes.add(new NoteInfo(course, "Service default threads",
                "Did you know that by default an Android Service will tie up the UI thread?"));
        mNotes.add(new NoteInfo(course, "Long running operations",
                "Foreground Services can be tied to a notification icon"));

        course = dm.getCourse("java_lang");
        course.getModule("java_lang_m01").setComplete(true);
        course.getModule("java_lang_m02").setComplete(true);
        course.getModule("java_lang_m03").setComplete(true);
        course.getModule("java_lang_m04").setComplete(true);
        course.getModule("java_lang_m05").setComplete(true);
        course.getModule("java_lang_m06").setComplete(true);
        course.getModule("java_lang_m07").setComplete(true);
        mNotes.add(new NoteInfo(course, "Parameters",
                "Leverage variable-length parameter lists"));
        mNotes.add(new NoteInfo(course, "Anonymous classes",
                "Anonymous classes simplify implementing one-use types"));

        course = dm.getCourse("java_core");
        course.getModule("java_core_m01").setComplete(true);
        course.getModule("java_core_m02").setComplete(true);
        course.getModule("java_core_m03").setComplete(true);
        mNotes.add(new NoteInfo(course, "Compiler options",
                "The -jar option isn't compatible with with the -cp option"));
        mNotes.add(new NoteInfo(course, "Serialization",
                "Remember to include SerialVersionUID to assure version compatibility"));
    }

    private CourseInfo initializeCourse1() {
        List<ModuleInfo> modules = new ArrayList<>();
        modules.add(new ModuleInfo("android_intents_m01", "Android Late Binding and Intents"));
        modules.add(new ModuleInfo("android_intents_m02", "Component activation with intents"));
        modules.add(new ModuleInfo("android_intents_m03", "Delegation and Callbacks through PendingIntents"));
        modules.add(new ModuleInfo("android_intents_m04", "IntentFilter data tests"));
        modules.add(new ModuleInfo("android_intents_m05", "Working with Platform Features Through Intents"));

        return new CourseInfo("android_intents", "Android Programming with Intents", modules);
    }

    private CourseInfo initializeCourse2() {
        List<ModuleInfo> modules = new ArrayList<>();
        modules.add(new ModuleInfo("android_async_m01", "Challenges to a responsive user experience"));
        modules.add(new ModuleInfo("android_async_m02", "Implementing long-running operations as a service"));
        modules.add(new ModuleInfo("android_async_m03", "Service lifecycle management"));
        modules.add(new ModuleInfo("android_async_m04", "Interacting with services"));

        return new CourseInfo("android_async", "Android Async Programming and Services", modules);
    }

    private CourseInfo initializeCourse3() {
        List<ModuleInfo> modules = new ArrayList<>();
        modules.add(new ModuleInfo("java_lang_m01", "Introduction and Setting up Your Environment"));
        modules.add(new ModuleInfo("java_lang_m02", "Creating a Simple App"));
        modules.add(new ModuleInfo("java_lang_m03", "Variables, Data Types, and Math Operators"));
        modules.add(new ModuleInfo("java_lang_m04", "Conditional Logic, Looping, and Arrays"));
        modules.add(new ModuleInfo("java_lang_m05", "Representing Complex Types with Classes"));
        modules.add(new ModuleInfo("java_lang_m06", "Class Initializers and Constructors"));
        modules.add(new ModuleInfo("java_lang_m07", "A Closer Look at Parameters"));
        modules.add(new ModuleInfo("java_lang_m08", "Class Inheritance"));
        modules.add(new ModuleInfo("java_lang_m09", "More About Data Types"));
        modules.add(new ModuleInfo("java_lang_m10", "Exceptions and Error Handling"));
        modules.add(new ModuleInfo("java_lang_m11", "Working with Packages"));
        modules.add(new ModuleInfo("java_lang_m12", "Creating Abstract Relationships with Interfaces"));
        modules.add(new ModuleInfo("java_lang_m13", "Static Members, Nested Types, and Anonymous Classes"));

        return new CourseInfo("java_lang", "Java Fundamentals: The Java Language", modules);
    }

    private CourseInfo initializeCourse4() {
        List<ModuleInfo> modules = new ArrayList<>();
        modules.add(new ModuleInfo("java_core_m01", "Introduction"));
        modules.add(new ModuleInfo("java_core_m02", "Input and Output with Streams and Files"));
        modules.add(new ModuleInfo("java_core_m03", "String Formatting and Regular Expressions"));
        modules.add(new ModuleInfo("java_core_m04", "Working with Collections"));
        modules.add(new ModuleInfo("java_core_m05", "Controlling App Execution and Environment"));
        modules.add(new ModuleInfo("java_core_m06", "Capturing Application Activity with the Java Log System"));
        modules.add(new ModuleInfo("java_core_m07", "Multithreading and Concurrency"));
        modules.add(new ModuleInfo("java_core_m08", "Runtime Type Information and Reflection"));
        modules.add(new ModuleInfo("java_core_m09", "Adding Type Metadata with Annotations"));
        modules.add(new ModuleInfo("java_core_m10", "Persisting Objects with Serialization"));

        return new CourseInfo("java_core", "Java Fundamentals: The Core Platform", modules);
    }

    public int createNewNote(CourseInfo course, String noteTitle, String noteText) {
        int index = createNewNote();
        NoteInfo newNote = getNotes().get(index);
        newNote.setCourse(course);
        newNote.setTitle(noteTitle);
        newNote.setText(noteText);
        return index;
    }
    //endregion

    /**
     * READING DATA FROM SQLITE DATABASE
     * Data Access in SQLite has 3 distinct phases;
     * a. Request database connection => SQLiteOpenHelper.getReadableDatabase() = SQLiteDatabase.
     * b. Issue a query to identify what we need from the database => SQLiteDatabase.query();
     * The SQLiteDatabase.query() accepts a number of parameters that allow us to specify the query details.
     * The two most fundamental parameters are ; -> table name and columns we want to be returned.
     * For parameters we don't want to pass, we simply pass null.
     * c. Moving through the results
     * SQLiteDatabase.query() returns a reference to the Cursor.
     * Cursor provides us access to the results and allows us to walk through the results row by row.
     * To provide us with this row to row access, the cursor must maintain the position of each row.
     * Cursor is initially positioned before the first row.
     * Therefore, we must move the cursor to the desired row.
     * There are many ways to move the cursor but the most common is Cursor.moveToNext().
     * Cursor.moveToNext() moves the cursor sequentially to the next row.
     * Returns true if succesfully moved and false otherwise.
     * Other methods
     * -> Cursor.moveToPrevious() - positions the cursor to the previous row in the result also return true and false for each case.
     * Cursor.moveToFirst(), Cursor.moveToLast() and Cursor.moveToPosition() also provides direct access to the rows.
     *
     * Accessing row values from the result
     * The cursor allows us to get the current row column values of whatever row the cursor is positioned.
     * We use Cursor.getTypes() - e.g. getString(), getInt() etc - accepts paremeter which is the index of the column.
     * Uses a zero based index.
     * => The column index does not correspond to the column index on the table but rather the column index on the query.
     * => We want to avoid hard-coding the column index values inside the Cursor.getType() as this could make our code very fragile.
     * We Cursor.getColumnIndex() which helps us get the index of that particular column.
     * Accepts column_name as a parameter and returns the index of that particular column.
     *
     * => Close Cursor when done by calling the Cursor.close() because the cursor consumes system resources
     * thus we want to avoid resource leak.
     *
     * Ordering results
     * Initially, when we load notes and courses, their order is specified as undefined.
     * We can therefore specify a row order.
     * The order is passed as a string to the query immediately after column name.
     * We can order by multiple columns with the first column acting as the primary sort.
     * We can order by descending order by specifying DESC.
     */

}
