package com.daniel.FitTrackerApp.interfaces;

import com.daniel.FitTrackerApp.sportactivity.SportActivity;

public interface StopRecordingCallBacks
{
    void stopRecording();
    void onReceiveSportActivity(SportActivity sportActivity);
}
