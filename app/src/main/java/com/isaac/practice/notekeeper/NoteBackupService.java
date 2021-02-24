package com.isaac.practice.notekeeper;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NoteBackupService extends IntentService {

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String EXTRA_COURSE_ID = "com.isaac.practice.notekeeper.EXTRA_COURSE_ID";

    public NoteBackupService() {
        super("NoteBackupService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String backupId = intent.getStringExtra(EXTRA_COURSE_ID);
            // PERFORM THE ACTION
            NoteBackup.doBackup(this, backupId);
        }
    }

}