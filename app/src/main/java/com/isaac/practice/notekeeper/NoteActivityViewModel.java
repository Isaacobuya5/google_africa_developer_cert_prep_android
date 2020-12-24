package com.isaac.practice.notekeeper;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

public class NoteActivityViewModel extends ViewModel {

    // saving activity's instance state in view model
    // reccommended to mark as private fields and generate getters and setters
    private String mOriginalCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;

    public static final String ORIGINAL_NOTE_COURSE_ID = "com.jwhh.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.jwhh.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.jwhh.notekeeper.ORIGINAL_NOTE_TEXT";

    public boolean mIsNewlyCreated = true;

    public String getOriginalCourseId() {
        return mOriginalCourseId;
    }

    public void setOriginalCourseId(String originalCourseId) {
        mOriginalCourseId = originalCourseId;
    }

    public String getOriginalNoteTitle() {
        return mOriginalNoteTitle;
    }

    public void setOriginalNoteTitle(String originalNoteTitle) {
        mOriginalNoteTitle = originalNoteTitle;
    }

    public String getOriginalNoteText() {
        return mOriginalNoteText;
    }

    public void setOriginalNoteText(String originalNoteText) {
        mOriginalNoteText = originalNoteText;
    }


    public void saveState(Bundle outState) {
        outState.putString(ORIGINAL_NOTE_COURSE_ID, getOriginalCourseId());
        outState.putString(ORIGINAL_NOTE_TITLE, getOriginalNoteTitle());
        outState.putString(ORIGINAL_NOTE_TEXT, getOriginalNoteText());
    }

    public void restoreState(Bundle inState) {
        setOriginalCourseId(inState.getString(ORIGINAL_NOTE_COURSE_ID));
        setOriginalNoteTitle(inState.getString(ORIGINAL_NOTE_TITLE));
        setOriginalNoteText(inState.getString(ORIGINAL_NOTE_TEXT));
    }
}
