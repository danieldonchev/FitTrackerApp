package com.daniel.FitTrackerApp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.daniel.FitTrackerApp.services.DailyStatsCounter;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        context.startService(new Intent(context, DailyStatsCounter.class));
    }
}
