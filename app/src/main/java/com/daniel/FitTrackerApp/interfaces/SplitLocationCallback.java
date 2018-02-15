package com.daniel.FitTrackerApp.interfaces;

import com.google.android.gms.maps.model.MarkerOptions;

public interface SplitLocationCallback {
    void onReceiveSplitLocation(MarkerOptions markerOptions);
}
