package com.isaac.practice.notekeeper;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;

public class NoteUploadJobService extends JobService {

    public static final String EXTRA_DATA_URI = "com.isaac.practice.notekeeper.extras.DATA_URI";
    private NoteUploader mNoteUploader;

    public NoteUploadJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        AsyncTask<JobParameters, Void, Void> task = new AsyncTask<JobParameters, Void, Void>() {
            @Override
            protected Void doInBackground(JobParameters... backgroundParams) {
                JobParameters jobParameters = backgroundParams[0];
                // accessing extras
                String stringDataUri = jobParameters.getExtras().getString(EXTRA_DATA_URI);
                Uri dataUri = Uri.parse(stringDataUri);
                mNoteUploader.doUpload(dataUri);

                if (!mNoteUploader.isCanceled())
                    jobFinished(jobParameters, false);
                return null;
            }
        };
        mNoteUploader = new NoteUploader(this);
        task.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mNoteUploader.cancel();
        return true;
    }

    /**
     * JOB SCHEDULER
     * -> The Job Scheduler addresses most of our background work challenges.
     * -> It addresses;
     *  - Addressing System Challenges
     *      * Gives system more control of when a background work is run.
     *  - Addressing Developer challenges
     *      * Handles run criteria details.
     *
     *  Job Scheduler and Job Implementation
     *  -> Introduced in API 21 i.e. Android 5.0 or newer.
     *  -> It has become the preferred way to do background work.
     *  ->  * Allows system to manage resource use.
     *  ->  * Limits impact on user experience.
     *  -> * Limits impact on device.
     *
     *  Useful when many common service scenerios;
     *  Caveat -> Work may not start immediately.
     *  Work is handled as a "job"?
     *  a) Implement the job.
     *    - create the component that handles doing the work.
     *  b) Build information about the job.
     *  - Include the job run criteria.
     *  c.) Schedule the job i.e;
     *  - Pass job information to job scheduler.
     *
     *  Implementing a Job
     *  -> A job is implemented as a special service.
     *   * Must extend the JobService class.
     *   2 key methods to override;
     *    -> onStartJob() - called to indicate the job should start. -> any criteria has been met.
     *    -> onStopJob() - called to indicate job stop.
     *       - used to indicate that criteria is no longer being met.
     *   * Remember -> services must appear in the manifest.
     *   -> Mostly the same as the entry for other services.
     *   One key difference is that it must be marked with a special permission i.e. "android:permission attribute.
     *   with a value of android.permission.BIND_JOB_SERVICE.
     *
     * Job Information and Scheduling
     * We build the Job Information using the JobInfo class;
     * Default parameters -> an app-defined job ID, job implementation component.
     * others -> job criteria - you must specify at least one criteria.
     * -> job defined data.
     *
     * Created using the Builder pattern i.e. JobInfo.Builder()
     * examples of Job run Criteria
     * a. Network criteria -> Metered/Un-metered connection.
     * b. Power Criteria -> Device Charging, Battery not low
     * c. Device state -> Device is idle, Storage is not low
     * d. Timing Criteria -> Delay starting, Run at regular intervals.
     *
     * Override deadline - i.e. maximum amount of time you want the job to wait.
     * In addition to these criteria, we can also include job-defined data;
     * Allows us to associate extras with the job information.
     * -> We store in a persistableBundle.
     * -> Implementation component can then retrieve those data.
     *
     * Scheduling the Job
     * -> Get a reference to the Job Scheduler i.e. JobScheduler is a system service.
     * thus use context.getSystemService(JOB_SCHEDULER_SERVICE).
     * We then call JobScheduler.schedule(jobInfo) -> not run necessarily as soon as criteria is met.
     *
     * Performing Job Work
     * onStartJob() is called to indicate that the work should begin i.e. all criteria met.
     * It's usually called on the main application thread.
     * THerefore, u need to perform non-long running work here.
     * In case of long work, dispatch it to a separate thread.
     * Many techniques we can use;
     *  a. AsyncTask
     *  b. Send to a Handler on a different thread.
     *
     *
     *  Since we are running the work on a different thread, we to cordinate it with the JobScheduler.
     *  a. Indicate to the job scheduler that we have started to do the background work.
     *  - onStartJob return value is important in cordinating with the JobScheduler; true means job has started being done.
     *  B. Indicate that the work is done.
     *  - jobFinished() -> indicate we finished doing the job.
     *  -> call to indicate work is done.
     *  -> can optionally reschedule the job.
     *
     *  As part of performimg the job, we may need to know some of the job configuration and identification data.
     *  -> JobParameter class can help with this.
     *  JobScheduler passes to the onStartJob -> includes any job extras.
     *
     *
     */

}