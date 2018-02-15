package com.daniel.FitTrackerApp.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.goal.GoalManager;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.services.DailyStatsCounter;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.UnitUtils;

import java.util.Calendar;

public class DailyStatsFragment extends Fragment {

    private TextView dailyDistance, dailyCalories, dailyDuration, dailySteps;
    private TextView currentDistance, currentDuration, currentSteps, currentActivity;
    private TextView dailyUnit, currentUnit;
    private Handler handler;
    private boolean isAvailable;
    private boolean isMetric;

    final Messenger mMessenger = new Messenger(new IncomingHandler());
    Messenger mService = null;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {

            try
            {
                mService = new Messenger(service);
                Message msg = Message.obtain(null, DailyStatsCounter.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PackageManager pm = getActivity().getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER) && !pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR))
        {
            isAvailable = false;
            return inflater.inflate(R.layout.fragment_daily_stats_not_available, container, false);
        } else {
            isAvailable = true;
            return inflater.inflate(R.layout.fragment_daily_stats, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(isAvailable){
            isMetric = PreferencesHelper.getInstance().isMetric(getActivity());
            handler = new Handler();

            dailyDistance = (TextView) view.findViewById(R.id.dailyDistance);
            dailyCalories = (TextView) view.findViewById(R.id.dailyCalories);
            dailyDuration = (TextView) view.findViewById(R.id.dailyDuration);
            dailySteps = (TextView) view.findViewById(R.id.dailySteps);
            currentSteps = (TextView) view.findViewById(R.id.currentSteps);
            currentDuration = (TextView) view.findViewById(R.id.currentDuration);
            currentDistance = (TextView) view.findViewById(R.id.currentDistance);
            currentActivity = (TextView) view.findViewById(R.id.currentActivity);
            dailyUnit = (TextView) view.findViewById(R.id.unitView);
            currentUnit = (TextView) view.findViewById(R.id.unitTextView);

            PreferencesHelper.getInstance().refreshSharedPreferences(getActivity());
            SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            PackageManager pm = getActivity().getPackageManager();
            if (!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
            } else {
                if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null)
                {
                } else{
                    getActivity().stopService(new Intent(getActivity(), DailyStatsCounter.class));
                }
            }
            Calendar calendar = Calendar.getInstance();


            long currentTime = calendar.getTimeInMillis();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long startDayTime = calendar.getTimeInMillis();

            long passedSeconds = (currentTime - startDayTime) / 1000;
            double bmr = com.daniel.FitTrackerApp.models.BMR.getBMRperSecond(PreferencesHelper.getInstance().getCurrentUserGender(getContext()),
                    PreferencesHelper.getInstance().getCurrentUserHeight(getContext()),
                    PreferencesHelper.getInstance().getCurrentUserWeight(getContext()),
                    PreferencesHelper.getInstance().getCurrentUserAge(getContext()));
            int calories = (int) (bmr * passedSeconds);

            calories = (int) GoalManager.getInstance().stats.daily.calories;

            dailyDistance.setText(UnitUtils.getDistanceString(getContext(), GoalManager.getInstance().stats.daily.distance, isMetric) + " " +
                    (isMetric ? getString(R.string.km) : getString(R.string.miles)));
            dailyDuration.setText(AppUtils.convertSecondsToString(GoalManager.getInstance().stats.daily.duration));
            dailyCalories.setText(String.valueOf(calories));
            dailySteps.setText(String.valueOf(GoalManager.getInstance().stats.daily.steps));
            currentActivity.setText("Still");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isAvailable){
            getActivity().bindService(new Intent(getActivity().getApplicationContext(), DailyStatsCounter.class), mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isAvailable){
            unbindService();
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DailyStatsCounter.MSG_STEPS: {

                    break;
                }
                case DailyStatsCounter.MSG_DETECTED_ACTIVITY:
                {
                    break;
                }
                case DailyStatsCounter.MSG_REGISTER_CLIENT:
                {
                    break;
                }
                case DailyStatsCounter.MSG_RECORDER:
                {
                    currentActivity.setText(msg.getData().getString("activity"));
                    currentSteps.setText(String.valueOf(msg.getData().getFloat("steps")));
                    currentDuration.setText(AppUtils.convertSecondsToString(msg.getData().getLong("duration")));
                    currentDistance.setText(AppUtils.doubleToString(msg.getData().getDouble("distance"))  + " " +
                            (isMetric ? getString(R.string.km) : getString(R.string.miles)));

                }
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void unbindService()
    {
        if(mConnection != null)
        {
            try {
                Message msg = Message.obtain(null, DailyStatsCounter.MSG_UNREGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            getActivity().unbindService(mConnection);
        }
    }

}