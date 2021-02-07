package com.isaac.practice.notekeeper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.isaac.practice.notekeeper.database.NoteKeeperDatabaseContract;
import com.isaac.practice.notekeeper.database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.isaac.practice.notekeeper.database.NoteKeeperOpenHelper;

import static com.isaac.practice.notekeeper.NoteKeeperProviderContract.*;
import static com.isaac.practice.notekeeper.database.NoteKeeperDatabaseContract.*;

public class NoteKeeperProvider extends ContentProvider {

    private NoteKeeperOpenHelper mDbOpenHelper;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;

    public static final int NOTES = 1;

    public static final int NOTES_EXPANDED = 2;

    public static final int NOTES_ROW = 3;

    // static initializer
    static {
        sUriMatcher.addURI(AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
        // adding rowId Uri
        sUriMatcher.addURI(AUTHORITY, Notes.PATH + "/#", NOTES_ROW);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // getting reference to the database
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        // initialize rowId
        long rowId = -1;
        // initialize row uri
        Uri rowUri = null;
        // checking the table's uri
        int uriMatch = sUriMatcher.match(uri);
        // perform operation according to matched Uri
        switch(uriMatch) {
            case NOTES:
                // proceed to insert to notes table - returns row id
                rowId = db.insert(NoteInfoEntry.TABLE_NAME, null,values);
                // constructing the row Uri = table Uri + /rowId
                rowUri = ContentUris.withAppendedId(Notes.CONTENT_URI, rowId);
                break;
            case COURSES:
                rowId = db.insert(CourseInfoEntry.TABLE_NAME,null,values);
                rowUri = ContentUris.withAppendedId(Courses.CONTENT_URI, rowId);
                break;
            case NOTES_EXPANDED:
                // we don't want to insert into this table thus throw an exception
                throw new UnsupportedOperationException("Not yet implemented");
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        return rowUri;
    }

    @Override
    public boolean onCreate() {
        mDbOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case COURSES:
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, selection,selectionArgs,null,null, sortOrder);
            break;
            case NOTES:
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, selection,selectionArgs,null,null, sortOrder);
                break;
            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(db,projection,selection,selectionArgs,sortOrder);
                break;
            case NOTES_ROW:
                // extracting the rowId
                Long rowId = ContentUris.parseId(uri);
                String rowSelection = NoteInfoEntry._ID + " = ?";
                String[] rowSelectionArgs = new String[] {
                        Long.toString(rowId)
                };
                cursor = db.query(NoteInfoEntry.TABLE_NAME,projection,rowSelection,rowSelectionArgs,null,null,null);
                break;
        }
        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // ADD CODE TO TABLE QUALIFY THAT BELONG TO BOTH
        String[] columns = new String[projection.length];
        for (int idx = 0; idx < projection.length; idx++) {
            columns[idx] = projection[idx].equals(BaseColumns._ID) || projection[idx].equals(CoursesIdColumns.COLUMN_COURSE_ID)?
                    NoteInfoEntry.getQName(projection[idx]) : projection[idx];
        }
        String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME + " ON " +
                NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);
        return db.query(tablesWithJoin,columns,selection,selectionArgs,null,null,sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Content Provider Organization
     * -> Content providers expose tables - multiple tables.
     * -> may expose a table that does not match the structure of the underlying tables i.e. - abstraction.
     * Therefore, we need a way to describe content provider.
     * i.e. identify available tables.
     * -> identify table columns.
     *
     * Content Provider Cobtract class
     * Presents the public appearance of the data.
     * -> We want to avoid exposing details of the structure.
     * -> Focus is on data access.
     * -> Include base URI for the content provider.
     * -> In our contract class, table information is in nested classes.
     * - have a separate class for each table.
     * Focus on Content Provider's public representation.
     *
     * Each class describe how to access the table.
     * * names of the available columns.
     * * table URI.
     * Table URIs -> content://com/../path
     * - Starts with content scheme.
     * - Include provider authority.
     * - Table is added as a path
     * - Use Uri.withAppendedPath static method.
     *
     * Defining column name constants
     * -> We could add constants directly to the classes.
     * -> Columns often duplicated across tables i.e. same name and same meaning
     * -> This could lead to redundant columns.
     * Better to manage column constants separate from the table classes.
     * Columns constants managed in interfaces.
     * -> Interfaces group related columns
     * -> Simplifies organization and maintainance.
     * Interfaces the nested within contract classes.
     *
     * Interfaces nested within contract class.
     * -> Should not be used outside of the provider.
     * -> Usually marked protected.
     *
     *Associating constants with table classes.
     * -> Have each of the table classes implement appropriate interfaces.
     *
     *
     * Matching Content URIs
     * Content Providers often need to support many URIs.
     * Interpreting Uris can be changing.
     * UriMatcher class helps with  this - traPnslates Uri/Uri pattern to specific integer
     * In order to use the UriMatcher in our Content Provider, we need to prepare the Uri matcher.
     *
     * Preparing the Uri matcher
     * -> Have a content provider contain a static field for Uri matcher.
     * -> Content Provider will also have a static initializer where we will add a valid URL.
     * -> Use addUri() -> map each Uri to an integer constant.
     *
     * Handling Requests with UriMatcher.
     * Use match().
     * -> Translate URI to an integer constant.
     * -> Take action based on the constant.
     *
     * Interacting with Content Providers.
     * RECAP -> Some of the Content Providers operations include;
     * - query(), insert(), update() and delete().
     * We have to access the Content Provider to perform the above operations on the SQLite database.
     * CursorLoader is commonly used for query() but we also need a way to handle non-query operations.
     * The CursorLoader actually uses the ContentResolver to access the Content Provider.
     * We therefore can access the ContentResolver our selves and use it to access the ContentProvider i.e. we get a reference to it.
     * ContentResolver is available to us from our current 'context' using the method getContentResolver() that gives us back a
     * reference to the ContentResolver.
     *ContentResolver therefore acts us an intermediary and exposes methods for each operation.
     * These methods accepts a "Uri". It then locates the content provider and delegates the actual operation out to the
     * content provider so that the content provider can do the actual operation.
     *
     * Inserting Row into the table.
     * -> Use ContentResolver.insert().
     * -> We pass in the Uri of the table that we want to insert the new row into.
     * -> We also pass in values as "ContentValues".
     * -> ContentValues allows us to identify the columns and set each value for those columns.
     * -> The ContentResolver.insert() returns a Uri for the new row. -> it is handled by ContentProvider.insert().
     * -> ContentProvider.insert() receives the Uri and the ContentValues passed.
     * -> So we need to determine the target with UriMatcher.match().
     * -> We then insert into the sqlite database. -> this returns the row id.
     * -> We are then responsible for taking that row id and returning back a Uri that corresponds to that row id.
     * The row Uri is based on the table Uri. We then take the row id that we received from the database instance insert()
     * and append that as a path on to the end of the table Uri.
     * We use ContentUris.withAppendId()
     *
     * Data Interaction and Row Uris
     * Remember Row URI = table Uri + /row_id
     * Row URIs are the primary ways we can interact with a sepecific row in Content Providers tables.
     * So we intent to use the to;
     * a. Querying specific row.
     * b. Doing update/delete specific row.
     * They are used mostly for convenience purposes because they behave exactly the same as passing the table URI with "_ID = ?" selection criteria.
     * That means that our Content Provider has to expand its URL handling.
     *
     * Content Providers URL handling
     * -> Thus, in addition to handling the table Uri, we also need to handle the row Uri.
     * -> We still use the UriMatcher class -> that knows how to match row Uris separate from table Uris.
     * Supports wild card to matching for ID value.
     * -> Use # in the place of ID value in the addUri() call
     *
     * Content Provider handling for the Row Uri
     * -> We need to do the actual db operation of fetching a particular note.
     * -> But the database needs " _ID = ? " Selection criteria.
     * -> We therefore need to extract _ID drom Row URI -> Use ContentUris.parseId()
     *
     */
}