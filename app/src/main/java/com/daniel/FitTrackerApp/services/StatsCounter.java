package com.daniel.FitTrackerApp.services;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.daniel.FitTrackerApp.receivers.AlarmReceiver;

import static com.daniel.FitTrackerApp.services.WorkoutSender.URL_INTENT_STRING;

public class StatsCounter extends IntentService  implements SensorEventListener
{
    private SensorManager sensorManager;
    private Sensor stepSensor;

    public StatsCounter() {
        super(StatsCounter.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
//        listener = new TriggerEventListener(){
//            @Override
//            public void onTrigger(TriggerEvent event) {
//                if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER)
//                {
//                    Intent intent = new Intent(Intent.ACTION_SYNC, null, getApplicationContext(), WorkoutSender.class);
//                    intent.putExtra("data", String.valueOf(event.values[0]));
//                    intent.putExtra(URL_INTENT_STRING, "https://192.168.0.102:8181/user/test-process");
//                    startService(intent);
//                }
//            }
//        };

        //sensorManager.requestTriggerSensor(listener, stepSensor);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);
        i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5 * 1000, 5 * 1000, pendingIntent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER)
        {
            Intent intent = new Intent(getApplicationContext(), WorkoutSender.class);
            intent.putExtra("data", String.valueOf(event.values[0]));
            intent.putExtra(URL_INTENT_STRING, "https://192.168.0.102:8181/user/test-process");
            startService(intent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
