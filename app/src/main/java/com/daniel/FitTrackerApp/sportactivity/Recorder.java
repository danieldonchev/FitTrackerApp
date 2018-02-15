package com.daniel.FitTrackerApp.sportactivity;

public interface Recorder<T> {
    void start(long startTime, long startTimestamp);
    T stop();
    void pause();
    void unPause();
}
