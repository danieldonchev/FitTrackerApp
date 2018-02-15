package com.daniel.FitTrackerApp.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.activities.MainActivity;
import com.daniel.FitTrackerApp.adapters.CurrentSplitsAdapter;
import com.daniel.FitTrackerApp.interfaces.SplitCallbacks;
import com.daniel.FitTrackerApp.sportactivity.SportActivityTrackingService;

public class SplitsFragment extends Fragment implements SplitCallbacks
{
    private RecyclerView recyclerView;
    private CurrentSplitsAdapter splitsAdapter;
    private SportActivityTrackingService mLocationService;

    private TextView distanceView, avgSpeedView, paceView;
    private Chronometer durationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_current_splits, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstance)
    {
        ((MainActivity)getActivity()).provideBackwardNavigation();
        v.setBackgroundColor(Color.BLACK);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        distanceView = (TextView) v.findViewById(R.id.distance);
        durationView = (Chronometer) v.findViewById(R.id.duration);
        avgSpeedView = (TextView) v.findViewById(R.id.avgSpeed);
        paceView = (TextView) v.findViewById(R.id.pace);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().bindService(new Intent(getActivity().getApplicationContext(), SportActivityTrackingService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mConnection != null)
        {
            getActivity().unbindService(mConnection);
            if(mLocationService != null)
            {
                mLocationService.setActivityOnDisplay(false);
            }
        }
        mLocationService.setChronoCallBacks(null);
    }

    private ServiceConnection mConnection = new ServiceConnection()
    {
        boolean mBound = false;
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            mBound = true;
            SportActivityTrackingService.LocalBinder binder = (SportActivityTrackingService.LocalBinder) service;
            mLocationService = binder.getService();
            mLocationService.setActivityOnDisplay(true);
            mLocationService.setChronoCallBacks(SplitsFragment.this);

            splitsAdapter = new CurrentSplitsAdapter(mLocationService.getSportActivityRecorder().getSplits());
            recyclerView.setAdapter(splitsAdapter);

            if(mLocationService.isRecording() && !mLocationService.isPaused())
            {
                startCurrentSplitChrono();
            }
        }
        public void onServiceDisconnected(ComponentName className)
        {
            mBound = false;
        }
    };

    @Override
    public void receiveData()
    {

        distanceView.setText(mLocationService.getSportActivityRecorder().getCurrentSplit().getDistanceString() + " km");
        avgSpeedView.setText(mLocationService.getSportActivityRecorder().getCurrentSplit().getAverageSpeedString());
        paceView.setText(mLocationService.getSportActivityRecorder().getCurrentSplit().getAveragePaceString());
    }

    public void startCurrentSplitChrono()
    {
        durationView.setBase(SystemClock.elapsedRealtime() - mLocationService.getSportActivityRecorder().getCurrentSplit().getCurrentTimeMs());
        durationView.start();
    }

    @Override
    public void pauseChrono()
    {
        durationView.setBase(SystemClock.elapsedRealtime() - mLocationService.getSportActivityRecorder().getCurrentSplit().getCurrentTimeMs());
        durationView.stop();
    }

    @Override
    public void unpauseChrono()
    {
        startCurrentSplitChrono();
    }

    @Override
    public void resetChrono()
    {
        startCurrentSplitChrono();
    }
}
