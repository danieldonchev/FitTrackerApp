package com.daniel.FitTrackerApp.sportactivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;

import com.daniel.FitTrackerApp.activities.MainActivity;
import com.daniel.FitTrackerApp.interfaces.BoxUpdater;
import com.daniel.FitTrackerApp.interfaces.SplitCallbacks;
import com.daniel.FitTrackerApp.interfaces.SplitLocationCallback;
import com.daniel.FitTrackerApp.interfaces.StopRecordingCallBacks;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.interfaces.UpdateRecordUI;
import com.daniel.FitTrackerApp.models.TTS;
import com.daniel.FitTrackerApp.services.DailyStatsCounter;
import com.daniel.FitTrackerApp.test.MockLocationProvider;
import com.daniel.FitTrackerApp.preferences.ProfileSettingsFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SportActivityTrackingService extends Service implements LocationListener, SensorEventListener, SplitLocationCallback {

    public static final String STOPPED_RECORDING = "StoppedRecording";
    public static final String SPORT_ACTIVITY_ID = "RecordId";

    private SportActivityRecorder sportActivityRecorder;

    private GoogleApiClient googleApiClient;
    private SensorManager sensorManager;
    private Sensor countSensor;
    private StopRecordingCallBacks stopRecordingCallBack;
    private SplitCallbacks chronoCallBacks;
    private UpdateRecordUI updateRecordUI;
    private BoxUpdater boxUpdater;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;

    private Timer timer;
    private Runnable testLocationRunnable;
    private Handler handler = new Handler();
    private int delay = 1000;
    private int timerVoice = 25;
    private int timerSaveData = 5000;
    private int lowAccuracyThreshHold = 11;

    MockLocationProvider MLP = new MockLocationProvider();

    private boolean isRecording;
    private boolean isPaused;
    private boolean isForeGround;
    private boolean isLocationAvailable;
    private boolean isLocationAvailableChecked;
    private boolean isActivityOnDisplay;
    private boolean isSplitsOn;
    private boolean isMetric;
    private boolean isStartingPositionSet;

    private Intent stopRecordingIntent, pauseRecordingIntent;
    private IntentFilter notificationFilter;

    private Notification notification;
    private NotificationManager mNotificationManager;
    private PendingIntent stopRecordingPendingIntent, pauseRecordingPendingIntent, notificationPendingIntent;

    private Location[] currentLocations = new Location[3];
    Queue<Location> queue = new LinkedList<Location>();
    private int locationIndex = 2;

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public Location getmCurrentLocation() {
        return mCurrentLocation;
    }

    public SportActivityRecorder getSportActivityRecorder() {
        return sportActivityRecorder;
    }

    public boolean isLocationAvailable() {
        return isLocationAvailable;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public boolean isSplitsOn() {
        return isSplitsOn;
    }

    public void setSplitsOn(boolean splitsOn) {
        isSplitsOn = splitsOn;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public void setActivityOnDisplay(boolean activityOnDisplay) {
        isActivityOnDisplay = activityOnDisplay;
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SportActivityTrackingService getService() {
            return SportActivityTrackingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private BroadcastReceiver GpsLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                locationAvailableSender();
            }
        }
    };

    private BroadcastReceiver NotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if ("PauseRecording".equals(action)) {
                if (!sportActivityRecorder.isPaused()) {
                    mNotificationManager.notify(1337, notificationResumeBuilder());
                    pauseRecord();
                } else {
                    unPauseRecord();
                    mNotificationManager.notify(1337, notificationPauseBuilder());
                }
                sendUpdateBroadcast();
            } else if ("StopRecording".equals(action)) {
                stopRecording();
            }
        }
    };

    private BroadcastReceiver unitSystemChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isRecording) {
                sportActivityRecorder.setMetric(intent.getBooleanExtra(ProfileSettingsFragment.IS_METRIC, true));
                sportActivityRecorder.calculateDataFromLocation(sportActivityRecorder.getmLastLocation());
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        isMetric = PreferencesHelper.getInstance().isMetric(this);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)
                .setFastestInterval(1000);

        buildGoogleApiClientLocation(this);
        googleApiClient.connect();

        stopRecordingIntent = new Intent().setAction("StopRecording");
        pauseRecordingIntent = new Intent().setAction("PauseRecording");

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        notificationPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, notificationIntent, 0);

        pauseRecordingPendingIntent = PendingIntent.getBroadcast(this, 1337, pauseRecordingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        stopRecordingPendingIntent = PendingIntent.getBroadcast(this, 1337, stopRecordingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification = notificationPauseBuilder();

        notificationFilter = new IntentFilter();
        notificationFilter.addAction("PauseRecording");
        notificationFilter.addAction("StopRecording");

        registerReceiver(NotificationReceiver, notificationFilter);
        registerReceiver(GpsLocationReceiver, new IntentFilter((LocationManager.PROVIDERS_CHANGED_ACTION)));
        LocalBroadcastManager.getInstance(this).registerReceiver(unitSystemChangeReceiver, new IntentFilter(ProfileSettingsFragment.IS_METRIC));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isLocationAvailableChecked) {
            locationAvailableSender();
            isLocationAvailableChecked = true;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        googleApiClient.disconnect();
        stopLocationUpdates();

        if (!isRecording) {
            stopForeground(true);
        }
        unregisterReceiver(GpsLocationReceiver);
        unregisterReceiver(NotificationReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(unitSystemChangeReceiver);
    }

    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;
        if(isRecording){
            if(location.getAccuracy() <= 7){
                queue.add(location);
                locationIndex = 2;
            } else {
                if(locationIndex >= 0){
                    if(location.hasAccuracy() && location.getAccuracy() < 30){
                        currentLocations[locationIndex] = location;
                        locationIndex--;
                    }
                } else {
                    Location bestLocation = currentLocations[2];
                    for(int i = 1; i >= 0; i--){

                        if(bestLocation.getAccuracy() >= currentLocations[i].getAccuracy()){
                            bestLocation = currentLocations[i];
                        }
                    }
                    queue.add(bestLocation);
                    locationIndex = 2;
                    currentLocations[locationIndex] = location;
                }
            }
        }

        if (isActivityOnDisplay && updateRecordUI != null) {
            updateRecordUI.updateAccuracy();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isRecording() && !sportActivityRecorder.isPaused()) {
            sportActivityRecorder.setSteps(getSportActivityRecorder().getSteps() + 1);
            if (boxUpdater != null) {
                boxUpdater.onUpdateStepCounter((int)sportActivityRecorder.getSteps());
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void stopRecording() {
        isRecording = false;
        isForeGround = false;
        try {
            Message msg = Message.obtain(null, DailyStatsCounter.MSG_STOP_RECORDING);
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService();
        SportActivity sportActivity = sportActivityRecorder.stop();
        LatLng lastPosition;
        try {
            lastPosition = sportActivityRecorder.getSportActivityMap().getPolylineOptions().getPoints().get(sportActivityRecorder.getSportActivityMap().getPolylineOptions().getPoints().size() - 1);
        } catch (ArrayIndexOutOfBoundsException ex){
            lastPosition = null;
        }
        if(lastPosition != null){
            if (updateRecordUI != null) {
                updateRecordUI.splitCallback(sportActivityRecorder.getSportActivityMap().addStartEndMarkers(lastPosition));
            } else {
                sportActivityRecorder.getSportActivityMap().addStartEndMarkers(lastPosition);
            }
        } else {
            if(sportActivityRecorder.getSportActivityMap().getMarkerPositions().size() == 1){
                sportActivityRecorder.getSportActivityMap().addStartEndMarkers(sportActivityRecorder.getSportActivityMap().getMarkerPositions().get(0));
            }
        }

        startSaveView(sportActivity);
        PreferencesHelper.getInstance().setIsRecording(this, isRecording);

        handler.removeCallbacks(recordingRunnable);
        handler.removeCallbacks(testLocationRunnable);
        sensorManager.unregisterListener(this);
        timer.cancel();
        stopForeground(true);
    }

    public void startRecording(boolean wasRecording, String activity) {
        isRecording = true;
        if (!isForeGround) {
            isForeGround = true;
            runAsForeground();
        }
        bindService(new Intent(this, DailyStatsCounter.class), mConnection, Context.BIND_AUTO_CREATE);

        setRecordingData(wasRecording, activity);

        if(mCurrentLocation != null && mCurrentLocation.getAccuracy() < 13){
            queue.add(mCurrentLocation);
        }
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        TTSstart();
       // timer.schedule(cacheCurrentRecordData, 0, timerSaveData * 1000);

//        mock location

//        testLocationRunnable = new Runnable() {
//            @Override
//            public void run()
//            {
//                Location location = MLP.getNextLocation();
//                //Location location = MLP.randomLocation();
//
//
//                LocationServices.FusedLocationApi.setMockLocation(googleApiClient, location);
//                handler.postDelayed(this, 1000);
//                startLocationUpdates();
//            }
//        };
//        handler.postDelayed(testLocationRunnable, 1000);

//        mock location

        sendUpdateBroadcast();
        handler.post(recordingRunnable);
        //timer.schedule(cacheCurrentRecordData, 0, timerSaveData);
    }

    private void setRecordingData(boolean wasRecording, String activity) {
        if (!wasRecording) {
            initializeRecordingComponents(activity);
        } else {
//            sportActivityRecorder = new SportActivityRecorder(this, PreferencesHelper.getInstance().isMetric(this));
//            byte[] array = new byte[0];
//            try {
//                RandomAccessFile file = new RandomAccessFile(getCacheDir() + "/currentRecordData", "r");
//                array = new byte[(int) file.length()];
//                file.readFully(array);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            sportActivityRecorder.deserializeThis(array);
//            sportActivityRecorder.setNextSplitGoal();
//            sportActivityRecorder.setStartTime(SystemClock.elapsedRealtime() - sportActivityRecorder.getDuration());
        }
    }

    private void startSaveView(SportActivity sportActivity) {
        sportActivity.calculateFinalData(isMetric, sportActivity.distance);
        if (!isActivityOnDisplay) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(STOPPED_RECORDING, STOPPED_RECORDING);
            intent.putExtra(SPORT_ACTIVITY_ID, sportActivity.getId().toString());
            startActivity(intent);

        } else {
            if (stopRecordingCallBack != null) {
                stopRecordingCallBack.onReceiveSportActivity(sportActivity);
                stopRecordingCallBack.stopRecording();
//                SaveRecordDataToDb saveRecordDataToDb = new SaveRecordDataToDb();
//                saveRecordDataToDb.execute();
            }
        }
    }

    private Notification notificationPauseBuilder() {
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.running_icon)
                .setContentTitle("Traker")
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText("Recording activity")
                .addAction(android.R.drawable.ic_media_pause, "Pause", pauseRecordingPendingIntent)
                .addAction(android.R.drawable.ic_media_pause, "Stop", stopRecordingPendingIntent)
                .setContentIntent(notificationPendingIntent).build();
    }

    private Notification notificationResumeBuilder() {
        return new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.running_icon)
                .setContentTitle("Traker")
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText("Recording activity")
                .addAction(android.R.drawable.ic_media_play, "Resume", pauseRecordingPendingIntent)
                .addAction(android.R.drawable.ic_media_pause, "Stop", stopRecordingPendingIntent)
                .setContentIntent(notificationPendingIntent).build();
    }

    private void initializeRecordingComponents(String activity) {
        sportActivityRecorder = new SportActivityRecorder(this, PreferencesHelper.getInstance().isMetric(this), activity, this);
        sportActivityRecorder.start(SystemClock.elapsedRealtime(), System.currentTimeMillis());
        sportActivityRecorder.getSportActivityMap().setCameraPosition(mCurrentLocation);
        sportActivityRecorder.setType(SportActivity.RECORDED);
    }

    private void setStartingLocation(Location location) {
        if (mCurrentLocation != null) {
            if(updateRecordUI != null){
                updateRecordUI.splitCallback(sportActivityRecorder.getSportActivityMap().addStartEndMarkers(locationLatLng(location)));

            } else {
                sportActivityRecorder.getSportActivityMap().addStartEndMarkers(locationLatLng(location));
            }
            sportActivityRecorder.getSportActivityMap().getPolylineOptions().add(locationLatLng(location));
        }
    }

    private void runAsForeground() {
        startForeground(1337, notification);
    }

    private void sendUpdateBroadcast() {
        if (updateRecordUI != null) {
            updateRecordUI.updateFragment();

            if (sportActivityRecorder != null) {

                updateRecordUI.updateMap();
                if(boxUpdater != null)
                {
                    boxUpdater.onUpdateUI(sportActivityRecorder);
                    boxUpdater.onUpdateStepCounter((int)sportActivityRecorder.getSteps());
                }

            }
            if (mCurrentLocation != null) {
                updateRecordUI.updateAccuracy();
            }
        }
    }

    private void locationAvailableSender() {
        if (!isLocationEnabled(getApplicationContext())) {

            sportActivityRecorder.pause();
            isLocationAvailable = false;
            sendUpdateBroadcast();
        } else {
            isLocationAvailable = true;
            sendUpdateBroadcast();
        }
    }

    public boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private LatLng locationLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    Runnable recordingRunnable = new Runnable() {
        @Override
        public void run() {
                if (mCurrentLocation != null) {
                    if (isRecording && !sportActivityRecorder.isPaused()) {
                        Location location = queue.poll();
                        if(location != null) {
                            if (!isStartingPositionSet) {
                                setStartingLocation(location);
                                isStartingPositionSet = true;
                            }

                            //draws lines
                            sportActivityRecorder.getSportActivityMap().getPolylineOptions().add(locationLatLng(location));
                            //needs to stay at current location
                            sportActivityRecorder.getSportActivityMap().setCameraPosition(mCurrentLocation);
                            sportActivityRecorder.calculateDataFromLocation(location);
                            if (updateRecordUI != null) {
                                updateRecordUI.updateMap();
                            }
                            if (chronoCallBacks != null) {
                                chronoCallBacks.receiveData();
                            }
                            sportActivityRecorder.setmLastLocation(location);
                        }
                        }
                    }
                sportActivityRecorder.calculateData();
                if(boxUpdater != null)
                {
                    boxUpdater.onUpdateUI(sportActivityRecorder);
                }
            handler.postDelayed(this, delay);
        }
    };

    @Override
    public void onReceiveSplitLocation(MarkerOptions markerOptions) {
            if (updateRecordUI != null) {
                updateRecordUI.splitCallback(markerOptions);
            }
            if (chronoCallBacks != null) {
                chronoCallBacks.resetChrono();
            }
    }

    TimerTask cacheCurrentRecordData = new TimerTask() {
        @Override
        public void run() {
            PreferencesHelper.getInstance().setIsRecording(getApplicationContext(), isRecording);
            saveCurrentRecordToCache();
        }
    };

    private TimerTask timeSplitVoice() {

        return new TimerTask() {
            @Override
            public void run() {
                String speech = "Time: " + sportActivityRecorder.getCurrentTimeString() + ".";

                if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.audio_distance_key), false)){
                    speech += "Distance: " + sportActivityRecorder.getDistanceString() + (isMetric ? getResources().getString(R.string.kilometers) : getResources().getString(R.string.miles)) + ".";
                }
                if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.audio_speed_key), false)){
                    speech += "Current speed: " + sportActivityRecorder.getSpeedString() + (isMetric ? getString(R.string.speedKM) : getString(R.string.speedMI)) + ".";
                }
                if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.audio_avg_speed_key), false)){
                    speech += "Average speed: " + sportActivityRecorder.getAverageSpeedString() + (isMetric ? getString(R.string.speedKM) : getString(R.string.speedMI)) + ".";
                }
                if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.audio_avg_pace_key), false)){
                    speech += "Average pace: " + sportActivityRecorder.getAveragePaceString() + (isMetric ? getString(R.string.speedKM) : getString(R.string.speedMI)) + ".";
                }
                if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.audio_steps_key), false)){
                    speech += "Steps: " + sportActivityRecorder.getStepsString() + ".";
                }
                if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.audio_calories_key), false)){
                    speech += "Calories: " + sportActivityRecorder.getCaloriesString() + ".";
                }
                TTS tts = new TTS(getApplicationContext(), speech);
            }
        };
    }

    void timerStop() {
        timer.cancel();
    }

    void TTSstart() {

        timer = new Timer(true);
        if(PreferencesHelper.getInstance().isAudio(getApplicationContext()))
        {
            timerVoice = PreferencesHelper.getInstance().getAudioRepeatTime(getApplicationContext());
            if(timerVoice > 0){
                int timerV = timerVoice - sportActivityRecorder.getCurrentTimeSeconds() % timerVoice;
                timer.schedule(timeSplitVoice(), 1000 * timerV, 1000 * timerVoice);
            }
        }
    }

    public void setCallbacks(StopRecordingCallBacks calls) {
        stopRecordingCallBack = calls;
    }

    public void setChronoCallBacks(SplitCallbacks calls) {
        chronoCallBacks = calls;
        if (chronoCallBacks != null) {
            if (isRecording) {
                chronoCallBacks.receiveData();
                if (sportActivityRecorder.isPaused()) {
                    chronoCallBacks.pauseChrono();
                }
            }
        }
    }

    public void setRecordUICallbacks(UpdateRecordUI calls) {
        updateRecordUI = calls;
    }

    public void setBoxUpdater(BoxUpdater calls)
    {
        boxUpdater = calls;
    }

    private void saveCurrentRecordToCache() {
//        try {
//            File outputFile = new File(getCacheDir(), "currentRecordData");
//            FileOutputStream stream = new FileOutputStream(outputFile);
//            try {
//                stream.write(sportActivityRecorder.serializeThis(SystemClock.elapsedRealtime()));
//            } finally {
//                stream.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void pauseRecord() {

        sportActivityRecorder.pause();

        mNotificationManager.notify(1337, notificationResumeBuilder());
        if (chronoCallBacks != null) {
            chronoCallBacks.pauseChrono();
        }
        timerStop();
    }

    public void unPauseRecord() {

        sportActivityRecorder.unPause();

        mNotificationManager.notify(1337, notificationPauseBuilder());
        if (chronoCallBacks != null) {
            chronoCallBacks.unpauseChrono();
        }
        sendUpdateBroadcast();

        TTSstart();
    }

    public void updateUI() {
        if (updateRecordUI != null) {
            updateRecordUI.updateFragment();
            updateRecordUI.updateAccuracy();
            if(boxUpdater != null)
            {
                boxUpdater.onUpdateUI(sportActivityRecorder);
            }
        }
    }

    private class SaveRecordDataToDb extends AsyncTask<Void, Void, Void> {
        public UUID id;
        @Override
        protected Void doInBackground(Void... params) {
            com.traker.shared.SportActivity sportActivity = sportActivityRecorder.toDTO();
            id = (DBHelper.getInstance().addActivity(sportActivity, PreferencesHelper.getInstance().getCurrentUserId(getApplicationContext()), getApplicationContext(), 0,SportActivity.RECORDED));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            //stopRecordingCallBack.onReceiveSportActivity(sportActivityRecorder.toSportActivity());
        }
    }

    GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
//            LocationServices.FusedLocationApi.setMockMode(googleApiClient, true);
//            LocationServices.FusedLocationApi.setMockLocation(googleApiClient, MLP.getNextLocation());
            startLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    GoogleApiClient.OnConnectionFailedListener failedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }
    };

    private synchronized void buildGoogleApiClientLocation(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(failedListener)
                .addApi(com.google.android.gms.location.LocationServices.API)
                .build();
    }

    public void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (googleApiClient.isConnected()) {
                com.google.android.gms.location.LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
            }
        }
    }

    public void stopLocationUpdates() {
        if (googleApiClient.isConnected()) {
            com.google.android.gms.location.LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    final Messenger mMessenger = new Messenger(new SportActivityTrackingService.IncomingHandler());
    Messenger mService = null;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            try
            {
                mService = new Messenger(service);
                Message msg = Message.obtain(null, DailyStatsCounter.MSG_RECORDING);
                mService.send(msg);
            }
            catch (RemoteException ex)
            {
                ex.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void unbindService()
    {
        if(mConnection != null)
        {
            unbindService(mConnection);
        }
    }
}