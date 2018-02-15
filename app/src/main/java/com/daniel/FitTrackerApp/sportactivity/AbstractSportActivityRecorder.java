package com.daniel.FitTrackerApp.sportactivity;

import android.os.SystemClock;

public abstract class AbstractSportActivityRecorder extends AbstractSportActivity {


    public AbstractSportActivityRecorder() {
    }

    public double getAverageSpeedMinSec() {
        return this.averageSpeed;
    }

    protected double calculateCurrentAverageSpeed(double distance) {
        return distance / getCurrentTimeHours();
    }

    public int getCurrentTimeSeconds() {
        return (int) (SystemClock.elapsedRealtime() - startTime) / 1000;
    }

    public double getCurrentTimeHours() {
        return (double) getCurrentTimeSeconds() / 3600;
    }

    public int getCurrentTimeMs() {
        return (int) (SystemClock.elapsedRealtime() - startTime);
    }

    /*
     * @param pausedTime the time at which the activity is paused
     */
    public void addPauseTime(long pausedTime) {
        this.startTime += SystemClock.elapsedRealtime() - pausedTime;
    }

    public String getCurrentTimeString() {
        String time = "";
        int hours = getCurrentTimeSeconds() / 3600;
        int minutes = (getCurrentTimeSeconds() % 3600) / 60;
        int seconds = getCurrentTimeSeconds() % 60;
        if (hours > 0) {
            time += String.valueOf(hours) + (hours > 1 ? "hours" : "hour");
        }
        if (minutes > 0) {
            time += String.valueOf(minutes) + (minutes > 1 ? "minutes" : "minute");
        }
        if (seconds > 0) {
            time += String.valueOf(seconds) + "seconds";
        }
        return time;
    }
}