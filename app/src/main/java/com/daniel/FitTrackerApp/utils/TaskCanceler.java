package com.daniel.FitTrackerApp.utils;

import android.os.AsyncTask;

public class TaskCanceler implements Runnable
{
    private AsyncTask task;

    public TaskCanceler(AsyncTask task)
    {
        this.task = task;
    }

    @Override
    public void run()
    {
        if (task.getStatus() == AsyncTask.Status.RUNNING )
        {
            task.cancel(true);
        }
    }
}