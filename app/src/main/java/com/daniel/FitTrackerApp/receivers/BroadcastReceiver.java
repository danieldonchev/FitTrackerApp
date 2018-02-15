package com.daniel.FitTrackerApp.receivers;

import android.content.Context;
import android.content.Intent;

import com.daniel.FitTrackerApp.goal.GoalManager;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GoalManager.getInstance().load(context, false);
    }
}
