package com.daniel.FitTrackerApp.interfaces;


import com.daniel.FitTrackerApp.sportactivity.SportActivityRecorder;

public interface BoxUpdater
{
    void onUpdateStepCounter(int steps);
    void onUpdateUI(SportActivityRecorder recorder);
}
