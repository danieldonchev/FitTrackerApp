package com.daniel.FitTrackerApp.interfaces;

import com.google.android.gms.maps.model.MarkerOptions;

public interface UpdateRecordUI
{
    void splitCallback(MarkerOptions marker);
    void updateMap();
    void updateAccuracy();
    void updateFragment();
}
