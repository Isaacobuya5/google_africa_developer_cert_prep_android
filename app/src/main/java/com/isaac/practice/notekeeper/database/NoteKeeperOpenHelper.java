package com.isaac.practice.notekeeper.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.isaac.practice.notekeeper.utils.DatabaseDataWorker;

public class NoteKeeperOpenHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "NoteKeeper.db"; // database file
    public static final int DATABASE_VERSION = 1;

    public NoteKeeperOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // SQLiteDatabase.CursorFactory - mechanism we use to customize the behaviour of our database interaction.
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating the tables
        db.execSQL(NoteKeeperDatabaseContract.CourseInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(NoteKeeperDatabaseContract.NoteInfoEntry.SQL_CREATE_TABLE);
        // insert some initial data into the db
        DatabaseDataWorker dbWorker = new DatabaseDataWorker(db);
        dbWorker.insertCourses();
        dbWorker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // as per now no upgrade to do
    }

    /**
     * DATABASE CREATION AND ACCESS
     * a. Database Creation
     * The first time your app runs, database won't exist yet. Therefore, we need to check whether the database exists.
     * The database must be created if it doesn't exist.
     *
     * b. Database versioning
     * App database structure may change over time. Therefore, we must track the current database version.
     * -> DB must be updated whenever necessary.
     * Database Helper class helps with all these issues - SQLiteOpenHelper
     * The specific needs of creation and versioning the database is going to vary from application to application
     * thus we need to extend this class to meet our application needs.
     * * Provide the expected database version.
     * * Provide the database file name we are going to use.
     * Override the appropriate methods;
     * i.> onCreate() - called if the database doesn't exist - look for database and if it doesn't exist create a new one.
     * -> INside it, we execute SQL to create our tables. And add initial data to those tables if necessary.
     *
     * b. onUpgrade()
     * -> Called when the SQLiteOpenHelper checks the version of the database is behind the version that
     * the currently expects.
     * Execute SQL statements to upgrade our tables.
     * - Most of the time, we also want to preserve existing data.
     *
     * USING THIS CLASS IN THE APPLICATION
     * -> Instantiate in the MainActivity's onCreate(). and assign to the member field of the activity
     * This allows open helper class to be around for the lifetime of the activity.
     * Then close in the activity's onDestroy()
     * Our activity uses the open helper class mainly to access the database using the access methods e.g;
     * -> getReadableDatabase - get access to the database so as to read data from it.
     * -> getWritableDatabase() - get access to the database so as to make changes to it.
     * => Both of them returns a reference to the SQLiteDatabaseReference which provides database interaction methods.
     * For SQL statements we use execSQL.
     */

}
