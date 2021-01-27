package com.isaac.practice.notekeeper.database;

import android.provider.BaseColumns;

public final class NoteKeeperDatabaseContract {

    // private constructor prevents instantiation
    private NoteKeeperDatabaseContract() {};

    // CourseInfo table
    private static final class CourseInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "course_info";
        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_COURSE_TITLE = "course_title";

        // query to create the table
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + COLUMN_COURSE_ID + " TEXT UNIQUE NOT NULL, " +
                COLUMN_COURSE_TITLE + " TEXT NOT NULL)";

    }

    // NoteInfo table
    private static final class NoteInfoEntry implements BaseColumns{
        public static final String TABLE_NAME = "note_info";
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
        public static final String COLUMN_COURSE_ID = "course_id";

        // query to create the table
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NOTE_TITLE + " TEXT NOT NULL, " +
                COLUMN_NOTE_TEXT + " TEXT, "
                + COLUMN_COURSE_ID + " TEXT NOT NULL)";
    }

    /**
     * This is the central place for all the information about the database i.e. the schema of the database.
     * Generally contains constants describing our database
     * This is class should be non-creatable, the reason for the private constructor.
     * Contains nested classes representing each table in the database.
     * Provides -> constants for table names, constant for column names and may contain key SQL statements e.g. table creation.
     * there is no explicit method for table creation.
     *
     * Describing Table columns.
     * - Columns can have a name.
     * - Columns can have a storage class. - associate a type with column typing
     * - SQLite doesn't have rigid typing. -> i.e. any column can store any type.
     * -Storage class however influences storage affinity.
     * Five storage classes;
     *  - a. BLOB -> stores data in whatever form it comes.
     *  - b. TEXT -> stores data in a text form.
     *  - c. INTEGER -> stores data in form of integer.
     *  - d. REAL -> stores data in the form of real numbers. e.g. 123 = 123.0
     *  - e. NUMERIC -> e.g. 123 - 123, 123.0 - 123.0
     *
     *  Constraints
     *  -> Columns can also specify constraints i.e. restricts allowable content.
     *  -> Automatically enforced by Database.
     *  Examples;
     *  a. NOT NULL -> Column cannot contain null.
     *  b. UNIQUE -> No two rows can have the same value for that particular column.
     *
     * Primary Key -> provide unambigous row identity i.e. uniquely identifies each row in a table.
     *
     * Android Framework friendly tables.
     * - Internally, SQLite uses data structure to manage row in a table. And each of these row is assigned a unique integer value.
     * THis integer value provides quick and efficient row access.
     * Therefore, we need to create a table that works well with the framework i.e. give table an integer primary key.
     * The primary key will automatically be associated with that integer row tag.
     * Use BaseColumns._ID as column name.
     */
}
