package com.daniel.FitTrackerApp.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;

import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daniel.FitTrackerApp.activities.ActivitiesActivity;
import com.daniel.FitTrackerApp.activities.MainActivity;
import com.daniel.FitTrackerApp.interfaces.UpdateRecordUI;
import com.daniel.FitTrackerApp.sportactivity.SportActivityMap;
import com.daniel.FitTrackerApp.sportactivity.SportActivityTrackingService;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.MapUtils;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class RecordFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback, UpdateRecordUI, ServiceConnection {
    private static final int ACTIVITY_RESULT_ACTIVITY = 3;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private Intent trackingServiceIntent;

    private TextView activityTextView;
    private Button locationButton, enableGPS, splits, startButton, activityButton;
    private ImageButton stopButton, pauseResumeButton;
    private ImageView gpsView, firstTower, secondTower, thirdTower, fourthTower, fifthTower;
    private TextView textAccuracy;
    private Chronometer chronometer, splitDuration;
    private Polyline polyline;
    private RelativeLayout topView;

    private AlertDialog.Builder builder1;
    private AlertDialog accuracyLowAlert, locationUnavalableAlert;

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

    private boolean isTrackingServiceRunning;
    private boolean isMockChecked;
    private boolean isSplits;
    private boolean wasRecording;
    private boolean isTopViewShown;
    private boolean isZoomed;

    private float gpsAccuracy = 0;

    public SportActivityTrackingService trackingService;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance) {

        mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.content_frame, mapFragment).commit();
        mapFragment.getMapAsync(this);

        trackingServiceIntent = new Intent(getActivity().getApplicationContext(), SportActivityTrackingService.class);

        RecordingInterrupted(PreferencesHelper.getInstance().wasRecording(getActivity()), AppUtils.isServiceRunning(getActivity().getApplicationContext(), SportActivityTrackingService.class));
        initializeUIComponents(view);
        setDefaultGUI();
        loadBottomFragment();
        GpsAccuracyLowBuilder();
        locationUnavailableAlertBuilder();
        splitDuration = (Chronometer) view.findViewById(R.id.duration);
        setButtons(true);

        getChildFragmentManager().addOnBackStackChangedListener(new android.support.v4.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                trackingService.setActivityOnDisplay(true);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().bindService(new Intent(getActivity().getApplicationContext(), SportActivityTrackingService.class), ((MainActivity)getActivity()).getmConnection(), Context.BIND_AUTO_CREATE);
        //setRecordingTextUnitView();

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        if (!AppUtils.isServiceRunning(getActivity().getApplicationContext(), SportActivityTrackingService.class)) {
            isTrackingServiceRunning = true;
            //startLocationServices();
        } else {
            startLocationServices();
            isTrackingServiceRunning = true;
        }
        isZoomed = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (trackingService != null) {
            if (!trackingService.isRecording() && isTrackingServiceRunning) {
                getActivity().stopService(trackingServiceIntent);
            }
            trackingService.setRecordUICallbacks(null);
        }
        if (((MainActivity)getActivity()).getmConnection() != null) {
            getActivity().unbindService(((MainActivity)getActivity()).getmConnection());
            if (trackingService != null) {
                trackingService.setActivityOnDisplay(false);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_RESULT_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                String activity = data.getStringExtra(ActivitiesActivity.ACTIVITY);
                //PreferencesHelper.getInstance().setActivity(activity);
                activityButton.setText(activity);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //do nothing
            }
        }
    }

    @NonNull
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                //restart service to start location updates
                restartService();
                if (mMap != null) {
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (!mMap.isMyLocationEnabled()) {
                            mMap.setMyLocationEnabled(true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            polyline = mMap.addPolyline(new PolylineOptions());
        }
    }

    @Override
    public void splitCallback(MarkerOptions marker) {
        mMap.addMarker(marker);
    }



    @Override
    public void updateMap() {
        if (trackingService.getSportActivityRecorder().getSportActivityMap().getPolylineOptions() != null) {
            polyline.remove();
            polyline = mMap.addPolyline(trackingService.getSportActivityRecorder().getSportActivityMap().getPolylineOptions());
        }
        if (trackingService.getSportActivityRecorder().getSportActivityMap().getCameraPosition() != null) {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(trackingService.getSportActivityRecorder().getSportActivityMap().getCameraPosition()));
        }
    }

    @Override
    public void updateAccuracy() {
        if (trackingService != null) {
            if (trackingService.getmCurrentLocation() != null) {
                if(!isZoomed){
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                                .target(new LatLng(trackingService.getmCurrentLocation().getLatitude(), trackingService.getmCurrentLocation().getLongitude()))
                                                .zoom(15)
                                                .build()));
                    isZoomed = true;
                }
                gpsAccuracy = trackingService.getmCurrentLocation().getAccuracy();
                //green
                textAccuracy.setText(String.valueOf(gpsAccuracy));
                if(gpsAccuracy < 5){
                    setGpsTowers(5);
                } else if(gpsAccuracy >= 5 && gpsAccuracy < 8){
                    setGpsTowers(4);
                } else if(gpsAccuracy >= 8 && gpsAccuracy < 10){
                    setGpsTowers(3);
                } else if(gpsAccuracy >= 10 && gpsAccuracy < 15){
                    setGpsTowers(2);
                } else if(gpsAccuracy >= 15 && gpsAccuracy < 20){
                    setGpsTowers(1);
                } else if(gpsAccuracy > 20){
                    setGpsTowers(0);
                }
            }
        }
    }

    @Override
    public void updateFragment() {
        if (trackingService != null) {
            if (trackingService.getmCurrentLocation() != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(trackingService.getmCurrentLocation().getLatitude(), trackingService.getmCurrentLocation().getLongitude()), SportActivityMap.defaultCameraZoom));
            }
            setButtons(trackingService.isLocationAvailable());
            if (trackingService.isRecording()) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
                if (trackingService.getSportActivityRecorder() != null && trackingService.getSportActivityRecorder().isPaused()) {
                    chronometer.setBase(SystemClock.elapsedRealtime() - trackingService.getSportActivityRecorder().getDuration());
                    pauseUI();
                } else {
                    unPauseUI();
                }

                if (mMap != null && trackingService.getSportActivityRecorder() != null) {
                    if (trackingService.getSportActivityRecorder().getSportActivityMap() != null) {
                        MapUtils.redrawShapesOnMap(trackingService.getSportActivityRecorder().getSportActivityMap(), mMap);
                    }
                }
                startButton.setVisibility(View.GONE);
                activityButton.setVisibility(View.GONE);
                pauseResumeButton.setVisibility(View.VISIBLE);
                splits.setVisibility(View.VISIBLE);

                if(!isTopViewShown){
                    showTopView();
                    isTopViewShown = true;
                }
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        SportActivityTrackingService.LocalBinder binder = (SportActivityTrackingService.LocalBinder) service;
        trackingService = binder.getService();
        trackingService.setActivityOnDisplay(true);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setCallbacks();
        }
        trackingService.setRecordUICallbacks(RecordFragment.this);
        if (trackingService.isSplitsOn()) {
            loadSplitsFragment();
            isSplits = true;
        }
        trackingService.updateUI();

        BottomFragment bottomFragment = (BottomFragment) getChildFragmentManager().findFragmentById(R.id.bottom_fragment);
        bottomFragment.onServiceConnected(name, service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        trackingService = null;
        android.support.v4.app.Fragment fragment = getChildFragmentManager().findFragmentById(R.id.bottom_fragment);
        if(fragment instanceof RecordFragment)
        {
            ((RecordFragment)fragment).onServiceDisconnected(name);
        }

    }

    private void startLocationServices() {
        getActivity().startService(trackingServiceIntent);
    }

    private void restartService() {
        getActivity().stopService(trackingServiceIntent);
        getActivity().startService(trackingServiceIntent);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Record: {
                    if (!trackingService.isRecording()) {
                        mMap.clear();
                        if(trackingService.getmCurrentLocation() != null && gpsAccuracy <= 20){
                            startButton.setVisibility(View.GONE);
                            pauseResumeButton.setVisibility(View.VISIBLE);
                            splits.setVisibility(View.VISIBLE);
                            startRecording(wasRecording);
                        } else if(trackingService.getmCurrentLocation() == null){
                            locationUnavalableAlert.show();
                        } else if(gpsAccuracy == 0 || gpsAccuracy > 20){
                            accuracyLowAlert.show();
                        }
                    }
                    break;
                }

                case R.id.Stop: {
                    chronometer.stop();
                    if (trackingService.isRecording()) {
                        if (!trackingService.getSportActivityRecorder().isPaused()) {
                            pauseRecording();
                        } else {
                            resumeRecording();
                        }
                    }
                    break;
                }

                case R.id.clearEverything: {
                    v.startAnimation(buttonClick);
                    //mMap.clear();
                    stopRecording();
                    break;
                }

                case R.id.mylocationButton: {
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Location location = LocationServices.FusedLocationApi.getLastLocation(trackingService.getGoogleApiClient());
                        if (location != null && mMap != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), SportActivityMap.defaultCameraZoom));
//                            mMap.moveCamera(CameraUpdateFactory.zoomTo(SportActivityMap.defaultCameraZoom));
//                            Point pointOnScreen = mMap.getProjection().toScreenLocation(new LatLng(location.getLatitude(), location.getLongitude()));
//                            Point newPoint = new Point();
//                            newPoint.x = pointOnScreen.x;
//                            newPoint.y = pointOnScreen.y - 1;
//                            LatLng newCenterLatLng = mMap.getProjection().fromScreenLocation(newPoint);
//                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newCenterLatLng, SportActivityMap.defaultCameraZoom));
                        } else {
                            locationUnavalableAlert.show();
                        }
                    }
                    break;
                }

                case R.id.enable_gps: {
                    Intent viewIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(viewIntent);
                    break;
                }

                case R.id.splitsButton: {
                    if (trackingService.getSportActivityRecorder() != null) {
                        if (!isSplits) {
                            loadSplitsFragment();
                            isSplits = true;
                            trackingService.setSplitsOn(true);
                        } else {
                            android.support.v4.app.Fragment fragment = getChildFragmentManager().findFragmentById(R.id.content_frame);

                            if (fragment instanceof SplitsFragment) {
                                getChildFragmentManager().popBackStack();
                                isSplits = false;
                                trackingService.setSplitsOn(false);
                            }
                        }
                    }
                    break;
                }

                case R.id.activityButton: {
                    startActivityForResult(new Intent(getActivity(), ActivitiesActivity.class), ACTIVITY_RESULT_ACTIVITY);
                    break;
                }
            }
        }
    };

    private void initializeUIComponents(View v) {
        //views
        startButton = (Button) v.findViewById(R.id.Record);
        pauseResumeButton = (ImageButton) v.findViewById(R.id.Stop);
        stopButton = (ImageButton) v.findViewById(R.id.clearEverything);;
        locationButton = (Button) v.findViewById(R.id.mylocationButton);
        enableGPS = (Button) v.findViewById(R.id.enable_gps);
        splits = (Button) v.findViewById(R.id.splitsButton);
        topView = (RelativeLayout) v.findViewById(R.id.recordTopView);
        activityButton = (Button) v.findViewById(R.id.activityButton);
        chronometer = (Chronometer) v.findViewById(R.id.chronometer);
        textAccuracy = (TextView) v.findViewById(R.id.gpsAccuracyView);

        firstTower = (ImageView) v.findViewById(R.id.first_tower);
        secondTower = (ImageView) v.findViewById(R.id.second_tower);
        thirdTower = (ImageView) v.findViewById(R.id.third_tower);
        fourthTower = (ImageView) v.findViewById(R.id.fourth_tower);
        fifthTower = (ImageView) v.findViewById(R.id.fifth_tower);
        activityTextView = (TextView) v.findViewById(R.id.activityText);

        startButton.setOnClickListener(onClickListener);
        pauseResumeButton.setOnClickListener(onClickListener);
        stopButton.setOnClickListener(onClickListener);
        locationButton.setOnClickListener(onClickListener);
        enableGPS.setOnClickListener(onClickListener);
        splits.setOnClickListener(onClickListener);
        activityButton.setOnClickListener(onClickListener);
    }


    private void setDefaultGUI() {

        //activityButton.setText(PreferencesHelper.getInstance().getActivity());

        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseResumeButton.setImageResource(android.R.drawable.ic_media_pause);
        pauseResumeButton.setVisibility(View.GONE);
        stopButton.setImageResource(android.R.drawable.ic_menu_save);
        stopButton.setVisibility(View.GONE);
        splits.setVisibility(View.GONE);
        topView.setVisibility(View.GONE);
    }

    public static boolean isMockSettingsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
        if (Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0")) {
            return false;
        } else {
            return true;
        }
    }

    private void locationUnavailableAlertBuilder() {
        builder1 = new AlertDialog.Builder(getActivity());
        builder1.setMessage("Location unavailable");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        locationUnavalableAlert = builder1.create();
    }

    private void GpsAccuracyLowBuilder() {
        builder1 = new AlertDialog.Builder(getActivity());
        builder1.setMessage("Accuracy is low");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Continue",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startRecording(wasRecording);
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Wait",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        accuracyLowAlert = builder1.create();
    }

    private void chronoPause() {
        chronometer.stop();
    }

    private void chronoStart() {
        chronometer.setBase(SystemClock.elapsedRealtime() - trackingService.getSportActivityRecorder().getCurrentTimeMs());
        chronometer.start();
    }

    private void setButtons(boolean isLocationAvailable) {
        if (isLocationAvailable) {
            if (trackingService != null && trackingService.isRecording()) {
                pauseResumeButton.setEnabled(true);
            } else {
                startButton.setVisibility(View.VISIBLE);
            }
            enableGPS.setVisibility(View.GONE);
        } else {
            startButton.setVisibility(View.GONE);
            enableGPS.setVisibility(View.VISIBLE);
            pauseResumeButton.setEnabled(false);
        }
    }



    private void pauseRecording() {
        pauseUI();
        trackingService.pauseRecord();
    }

    private void resumeRecording() {
        unPauseUI();
        trackingService.unPauseRecord();
    }

    private void pauseUI() {
        pauseResumeButton.setImageResource(android.R.drawable.ic_media_play);
        stopButton.setVisibility(View.VISIBLE);
        chronoPause();
    }

    private void unPauseUI() {
        pauseResumeButton.setImageResource(android.R.drawable.ic_media_pause);
        stopButton.setVisibility(View.GONE);
        chronoStart();
    }


    private void startRecording(boolean wasRecording) {
        //hide action bar animation
        ((AppCompatActivity) getActivity()).getSupportActionBar().setShowHideAnimationEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        showTopView();
        isTrackingServiceRunning = true;
        String activity = activityButton.getText().toString();
        trackingService.startRecording(wasRecording, activity);
    }

    //show top chronometer with animation
    private void showTopView() {
            topView.setVisibility(View.VISIBLE);
            activityTextView.setText(activityButton.getText().toString());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) topView.getLayoutParams();
            params.height = (int) getResources().getDimension(R.dimen.topview_height);
            params.width = (int) getResources().getDimension(R.dimen.topview_width);
            params.topMargin = (int) -getResources().getDimension(R.dimen.topview_height);
            topView.setLayoutParams(params);
            topView.animate().translationYBy(getResources().getDimension(R.dimen.topview_height)).setDuration(1000).start();
    }

    private void stopRecording() {
        startButton.setVisibility(View.VISIBLE);
        isSplits = false;
        chronometer.stop();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setShowHideAnimationEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().show();
        trackingService.stopRecording();
        setDefaultGUI();
    }

    public void RecordingInterrupted(boolean _wasRecording, boolean isServiceRunning) {
        wasRecording = _wasRecording;
        if (wasRecording && !isServiceRunning) {
            builder1 = new AlertDialog.Builder(getActivity());
            builder1.setMessage("Seems like you crashed")
                    .setPositiveButton("Continue last activity",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startRecording(wasRecording);
                                    wasRecording = false;
                                    dialog.cancel();
                                }
                            })
                    .setNegativeButton("Start new",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    PreferencesHelper.getInstance().setIsRecording(getActivity(), false);
                                    wasRecording = false;
                                    dialog.cancel();
                                }
                            }).show();
        }

    }

    private void loadSplitsFragment() {
        android.support.v4.app.Fragment fragment = new SplitsFragment();
        android.support.v4.app.FragmentManager manager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        manager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
    }

    private void loadBottomFragment()
    {
        BottomFragment bottomFragment = new BottomFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.bottom_fragment, bottomFragment).commit();
    }

    private void invalidateGpsTowers(){
        firstTower.invalidate();
        secondTower.invalidate();
        thirdTower.invalidate();
        fourthTower.invalidate();
        fifthTower.invalidate();
    }

    private void setGpsTowers(int numberOfTowers){
        if(numberOfTowers == 1){
            firstTower.setBackgroundColor(Color.rgb(80, 225, 0));
            secondTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
            thirdTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
            fourthTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
            fifthTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
        } else if(numberOfTowers == 2){
            firstTower.setBackgroundColor(Color.rgb(80, 225, 0));
            secondTower.setBackgroundColor(Color.rgb(80, 225, 0));
            thirdTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
            fourthTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
            fifthTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
        } else if(numberOfTowers == 3){
            firstTower.setBackgroundColor(Color.rgb(80, 225, 0));
            secondTower.setBackgroundColor(Color.rgb(80, 225, 0));
            thirdTower.setBackgroundColor(Color.rgb(80, 225, 0));
            fourthTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
            fifthTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
        } else if(numberOfTowers == 4){
            firstTower.setBackgroundColor(Color.rgb(80, 225, 0));
            secondTower.setBackgroundColor(Color.rgb(80, 225, 0));
            thirdTower.setBackgroundColor(Color.rgb(80, 225, 0));
            fourthTower.setBackgroundColor(Color.rgb(80, 225, 0));
            fifthTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
        } else if(numberOfTowers == 5){
            firstTower.setBackgroundColor(Color.rgb(80, 225, 0));
            secondTower.setBackgroundColor(Color.rgb(80, 225, 0));
            thirdTower.setBackgroundColor(Color.rgb(80, 225, 0));
            fourthTower.setBackgroundColor(Color.rgb(80, 225, 0));
            fifthTower.setBackgroundColor(Color.rgb(80, 225, 0));
        } else if(numberOfTowers == 0){
            firstTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
            secondTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
            thirdTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
            fourthTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
            fifthTower.setBackgroundColor(getResources().getColor(R.color.light_grey));
        }
        invalidateGpsTowers();
    }
}

//    public boolean isInternetAvailable()
//    {
//        Runtime runtime = Runtime.getRuntime();
//        try {
//            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
//            int     exitValue = ipProcess.waitFor();
//            return (exitValue == 0);
//
//        } catch (IOException e)          { e.printStackTrace(); }
//        catch (InterruptedException e) { e.printStackTrace(); }
//
//        return false;
//    }


//        googleMapOptions = new GoogleMapOptions().liteMode(true);
//        SupportMapFragment liteMap = SupportMapFragment.newInstance(googleMapOptions);
//
//        liteMap.getMapAsync(new OnMapReadyCallback()
//        {
//            @Override
//            public void onMapReady(GoogleMap googleMap)
//            {
//                if(trackingService.isRecording())
//                {
//                    if (googleMap != null && trackingService != null)
//                    {
//                        if (trackingService.getSportActivityMap().getRecordingMarkers() != null)
//                        {
//                            for (MarkerOptions marker : trackingService.getSportActivityMap().getRecordingMarkers())
//                            {
//                                googleMap.addMarker(marker);
//                            }
//                        }
//                        if (trackingService.getSportActivityMap().getPolylineOptions() != null)
//                        {
//                            googleMap.addPolyline(trackingService.getSportActivityMap().getPolylineOptions());
//                        }
//                    }
//                    googleMap.moveCamera(cameraBounds());
//                    googleMap.snapshot(snapshotReadyCallback);
//                }
//            }
//        });


//takeMapSnapshot(isInternetAvailable());


//        Handler handler = new Handler();
//        Runnable mRunnable;
//        Task task = new Task();
//        TaskCanceler taskCanceler = new TaskCanceler(task);
//        handler.postDelayed(taskCanceler, 3000);
//        task.execute();


//    public void takeMapSnapshot(boolean isOnline)
//    {
//        //mMap.moveCamera(cameraBounds());
//        mMap.setMyLocationEnabled(false);
//        mMap.setOnMapLoadedCallback(mapLoadedCallback);
//    }
//
//    GoogleMap.OnMapLoadedCallback mapLoadedCallback = new GoogleMap.OnMapLoadedCallback()
//    {
//        @Override
//        public void onMapLoaded()
//        {
//            mMap.snapshot(snapshotReadyCallback);
//            mMap.setMyLocationEnabled(true);
//        }
//    };


//    private class Task extends AsyncTask<Void, Void, Boolean>
//    {
//        @Override
//        protected Boolean doInBackground(Void... params)
//        {
//            takeMapSnapshot(isInternetAvailable());
//            return false;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result)
//        {
////            if(result)
////            {
////                takeMapSnapshot(isInternetAvailable());
////            }
////            else
////            {
////                Intent intent = new Intent(getApplicationContext(), SaveActivityFragment.class);
////                startActivity(intent);
////            }
//            Intent intent = new Intent(getApplicationContext(), SaveActivityFragment.class);
//            startActivity(intent);
//        }
//
//        @Override
//        protected  void onCancelled()
//        {
//            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
//            startActivity(intent);
//        }
//    }


// checks every time - probably needs some rework
//                if(!isMockChecked)
//                {
//                    boolean isMock;
//                    if (cacert.os.Build.LAST_MODIFIED_ACTIVITIES.SDK_INT >= 18)
//                    {
//                        isMock = location.isFromMockProvider();
//                    }
//                    else
//                    {
//                        isMock = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
//                    }
//                    if(isMock)
//                    {
//                        alert11.show();
//                    }
//                    isMockChecked = true;
//                }

//        if(isMetric)
//        {
//            for(int i = 0; i < recordingUI.length; i++)
//            {
//                switch (recordingUI[i])
//                {
//                    case SportActivityBufferer.SPEED_STRING:
//                    {
//                        recordingTextViewArray[i].setText("0.00" + getResources().getString(R.string.speedKM));
//                        break;
//                    }
//                    case SportActivityBufferer.AVERAGE_SPEED_STRING:
//                    {
//                        recordingTextViewArray[i].setText("0.00" + getResources().getString(R.string.speedKM));
//                        break;
//                    }
//                    case SportActivityBufferer.DISTANCE_STRING:
//                    {
//                        recordingTextViewArray[i].setText("0.00" + getResources().getString(R.string.kilometers));
//                        break;
//                    }
//                    case SportActivityBufferer.PACE_STRING:
//                    {
//                        recordingTextViewArray[i].setText("0.00" + getResources().getString(R.string.paceKM));
//                        break;
//                    }
//                    case SportActivityBufferer.STEPS_STRING:
//                    {
//                        recordingTextViewArray[i].setText("0");
//                        break;
//                    }
//                }
//            }
//        }
//        else
//        {
//            for(int i = 0; i < recordingUI.length; i++)
//            {
//                switch (recordingUI[i])
//                {
//                    case SportActivityBufferer.SPEED_STRING:
//                    {
//                        recordingTextViewArray[i].setText("0.00" + getResources().getString(R.string.speedMI));
//                        break;
//                    }
//                    case SportActivityBufferer.AVERAGE_SPEED_STRING:
//                    {
//                        recordingTextViewArray[i].setText("0.00" + getResources().getString(R.string.speedMI));
//                        break;
//                    }
//                    case SportActivityBufferer.DISTANCE_STRING:
//                    {
//                        recordingTextViewArray[i].setText("0.00" + getResources().getString(R.string.miles));
//                        break;
//                    }
//                    case SportActivityBufferer.PACE_STRING:
//                    {
//                        recordingTextViewArray[i].setText("0.00" + getResources().getString(R.string.paceMI));
//                        break;
//                    }
//                    case SportActivityBufferer.STEPS_STRING:
//                    {
//                        recordingTextViewArray[i].setText("0");
//                        break;
//                    }
//                }
//            }
//        }