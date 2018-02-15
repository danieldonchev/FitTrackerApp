package com.daniel.FitTrackerApp.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.sportactivity.SportActivity;
import com.daniel.FitTrackerApp.sportactivity.SportActivityRecorder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


import java.util.ArrayList;


public class DailyStatsCounter extends Service implements SensorEventListener, ResultCallback<Status>
{
    public static final int MSG_STEPS = 1;
    public static final int MSG_DETECTED_ACTIVITY = 2;
    public static final int MSG_REGISTER_CLIENT = 3;
    public static final int MSG_UNREGISTER_CLIENT = 4;
    public static final int MSG_RESET_STEPS = 5;
    public static final int MSG_RECORDER = 6;
    public static final int MSG_RECORDING = 7;
    public static final int MSG_STOP_RECORDING = 8;
    public static final int MSG_RELOAD_PREFERENCES = 9;

    private GoogleApiClient mGoogleApiClient;
    Intent intent;
    PendingIntent pendingIntent;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private float startSteps, steps, allSteps;
    private boolean hasStartSteps;
    private String detectActivity, currentActivity;
    private ArrayList<DetectedActivity> detectedActivities;
    private Handler handler;
    private SportActivityRecorder recorder;
    private boolean isMetric;
    private long startStepTimestamp, firstStepTimestamp;
    private int stepsIndex = 0;
    //steps required to take to start activity tracking
    private int stepsRequired = 30;
    //time between each step
    private long timeBetweenStep = 5 * 1000L;
    //seconds it takes to save activity after the user has stopped walking
    private int stopTime = 45;
    //seconds it takes between each step to delay the saving and stopping the activity
    private long resetStopTime = 10 * 1000L;
    private boolean isRecording;
    //indicates if the user is tracking activity with GPS
    private boolean isTracking;

    private ArrayList<Integer> activityPercentages = new ArrayList<>();

    Messenger mMessenger = new Messenger(new IncomingHandler());
    Messenger clientMessenger;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                {
                    clientMessenger = msg.replyTo;
                    try
                    {
                        Message message = Message.obtain(null, MSG_REGISTER_CLIENT);
                        Bundle bundle = new Bundle();
                        bundle.putString("detectActivity", detectActivity);
                        bundle.putFloat("steps", allSteps);

                        message.setData(bundle);
                        clientMessenger.send(message);
                    }
                    catch (RemoteException ex)
                    {
                        ex.printStackTrace();
                    }
                    break;
                }
                case MSG_UNREGISTER_CLIENT:
                {
                    clientMessenger = null;
                    break;
                }
                case MSG_RESET_STEPS:
                {
                    resetSteps();
                    break;
                }
                case MSG_RECORDING:
                {
                    isTracking = true;
                    if(isRecording){
                        runnable.run();
                    } else {
                        resetRecording();
                    }
                    break;
                }
                case MSG_STOP_RECORDING:
                {
                    isTracking = false;
                    break;
                }
                case MSG_RELOAD_PREFERENCES:{
                    PreferencesHelper.getInstance().reload(getApplicationContext());
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        DBHelper.getInstance(getApplicationContext());
        intent = new Intent(getApplicationContext(), DailyStatsCounter.class);
        pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        startSteps = 0;
        steps = 0;
        isMetric = PreferencesHelper.getInstance().isMetric(getApplicationContext());
        buildGoogleApiClientLocation(this);

        handler = new Handler();
        mGoogleApiClient.connect();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (ActivityRecognitionResult.hasResult(intent)) {
            if (ActivityRecognitionResult.extractResult(intent) != null) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                detectedActivities = (ArrayList<DetectedActivity>) result.getProbableActivities();
                ArrayList<String> activities = new ArrayList<>();
                ArrayList<Integer> confidences = new ArrayList<>();
                for (DetectedActivity activity : detectedActivities) {
                    if (activity.getType() == DetectedActivity.WALKING && activity.getConfidence() > 75) {
                        detectActivity = "Walking";
                        currentActivity = "Walking";
                        startNewDetectedActivity();
                    } else if (activity.getType() == DetectedActivity.RUNNING && activity.getConfidence() > 75) {
                        detectActivity = "Running";
                        currentActivity = "Running";
                        startNewDetectedActivity();
                    } else if (activity.getType() == DetectedActivity.ON_FOOT && activity.getConfidence() > 75) {
                        detectActivity = "OnFoot";
                    }else if (activity.getType() == DetectedActivity.IN_VEHICLE && activity.getConfidence() > 75) {
                        detectActivity = "Vehicle";
                    } else if (activity.getType() == DetectedActivity.STILL && activity.getConfidence() > 75) {
                        detectActivity = "Still";
                    } else if (activity.getType() == DetectedActivity.TILTING && activity.getConfidence() > 75) {
                        detectActivity = "Tilting";
                    }  else if (activity.getType() == DetectedActivity.ON_BICYCLE && activity.getConfidence() > 75) {
                        detectActivity = "Bicycle";
                    } else if (activity.getType() == DetectedActivity.UNKNOWN) {
                        detectActivity = "Unknown";
                    } else {
                        detectActivity = "Unknown";
                    }
                    activities.add(detectActivity);
                    confidences.add(activity.getConfidence());
                }
                if(clientMessenger != null)
                {
                    try
                    {
                        Message msg = Message.obtain(null, MSG_DETECTED_ACTIVITY);
                        Bundle bundle = new Bundle();
                        bundle.putString("detectActivity", detectActivity);
                        bundle.putStringArrayList("activities", activities);
                        bundle.putIntegerArrayList("confidences", confidences);
                        msg.setData(bundle);
                        clientMessenger.send(msg);
                    }
                    catch (RemoteException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        sendBroadcast(new Intent("RestartService"));
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if(!isTracking) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                if (hasStartSteps) {
                    steps = event.values[0] - startSteps;
                    allSteps += steps;
                    startSteps = event.values[0];

                    if (!isRecording) {
                        //if each step time is below 5 seconds 30 times start reording
                        if (startStepTimestamp > 0 && SystemClock.elapsedRealtime() - startStepTimestamp < timeBetweenStep) {
                            stepsIndex++;
                        } else {
                            resetRecording();
                        }

                        //if 30 steps criteria is met start recording
                        if (stepsRequired == stepsIndex) {
                            startRecording(firstStepTimestamp);
                        }
                    }

                    if (isRecording) {
                        if (startStepTimestamp > 0 && SystemClock.elapsedRealtime() - startStepTimestamp < resetStopTime) {
                            handler.removeCallbacks(runnable);
                            handler.postDelayed(runnable, stopTime * 1000);
                        }
                        recorder.calculateDataFromSteps(allSteps);

                        if (clientMessenger != null) {
                            try {
                                Bundle bundle = new Bundle();
                                bundle.putString("activity", recorder.getWorkout());
                                bundle.putFloat("steps", recorder.getSteps());
                                bundle.putDouble("distance", recorder.getDistance());
                                bundle.putLong("duration", recorder.getCurrentTimeSeconds());
                                Message msg = Message.obtain(null, MSG_RECORDER);
                                msg.setData(bundle);
                                clientMessenger.send(msg);
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    startStepTimestamp = SystemClock.elapsedRealtime();

                    if (clientMessenger != null) {
                        try {
                            Bundle bundle = new Bundle();
                            bundle.putFloat("steps", allSteps);
                            Message msg = Message.obtain(null, MSG_STEPS);
                            msg.setData(bundle);
                            clientMessenger.send(msg);
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    startSteps = event.values[0];
                    hasStartSteps = true;
                    firstStepTimestamp = SystemClock.elapsedRealtime();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {

        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            try {
                detectActivity = "Connection failed";
                Message msg = Message.obtain(null, MSG_DETECTED_ACTIVITY);
                Bundle bundle = new Bundle();
                bundle.putString("detectActivity", detectActivity);
                msg.setData(bundle);
                clientMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private synchronized void buildGoogleApiClientLocation(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .addApi(ActivityRecognition.API)
                .build();
    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    public float getSteps() {
        return allSteps;
    }

    public void resetSteps() {
        allSteps = 0;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            recorder.addPauseTime(startStepTimestamp);
            recorder.calculateDataFromSteps(allSteps);
            recorder.stop();

            resetRecording();
            stopActivityRecognition();
        }
    };

    private void startActivityRecognition(){
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 0, pendingIntent).setResultCallback(DailyStatsCounter.this);
    }

    private void stopActivityRecognition(){
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, pendingIntent);
    }

    private void resetRecording(){
        isRecording = false;
        hasStartSteps = false;
        stepsIndex = 0;
        steps = 0;
        startSteps = 0;
        allSteps = 0;
    }

    private void startRecording(long timeStarted){
        if(currentActivity == null){
            currentActivity = "Walking";
        }
        recorder = new SportActivityRecorder(getApplicationContext(), isMetric, currentActivity);
        recorder.start(timeStarted, System.currentTimeMillis());
        recorder.setType(SportActivity.TRACKED);
        startActivityRecognition();
        isRecording = true;
    }

    private void startNewDetectedActivity(){
        if(isRecording){
            if(recorder.getCurrentTimeSeconds() < 60){
                recorder.setWorkout(currentActivity);
            } else {
                if(!recorder.getWorkout().equals(currentActivity)){
                    runnable.run();
                    startRecording(SystemClock.elapsedRealtime());
                }
            }
        }
    }

}
