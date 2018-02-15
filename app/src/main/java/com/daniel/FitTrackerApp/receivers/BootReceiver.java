package com.daniel.FitTrackerApp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.daniel.FitTrackerApp.goal.GoalManager;
import com.daniel.FitTrackerApp.services.DailyStatsCounter;

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)){
            context.startService(new Intent(context, DailyStatsCounter.class));
        }
        GoalManager.getInstance().setDailyResetAlarm(context);
    }
}
