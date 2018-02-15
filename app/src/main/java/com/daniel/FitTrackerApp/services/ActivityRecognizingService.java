package com.daniel.FitTrackerApp.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;


public class ActivityRecognizingService extends IntentService{

    public ActivityRecognizingService() {
        super(ActivityRecognizingService.class.getName());
    }

    public ActivityRecognizingService(String name) {
        super(name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent))
        {
            if(ActivityRecognitionResult.extractResult(intent) != null)
            {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

                List<DetectedActivity> detectedActivities = result.getProbableActivities();

                Intent i = new Intent(Intent.ACTION_SYNC, null, getApplicationContext(), WorkoutSender.class);

                for(DetectedActivity activity : detectedActivities)
                {
                    if(activity.getType() == DetectedActivity.WALKING || activity.getType() == DetectedActivity.RUNNING)
                    {
                        Toast.makeText(getApplicationContext(), "not running or walking + " + activity.getConfidence(), Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "not running or walking + " + activity.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

    }
}
