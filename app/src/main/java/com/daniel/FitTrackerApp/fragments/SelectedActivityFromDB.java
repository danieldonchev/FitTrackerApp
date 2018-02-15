package com.daniel.FitTrackerApp.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.daniel.FitTrackerApp.AppNetworkManager;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.activities.MainActivity;
import com.daniel.FitTrackerApp.activities.SelectedActivityFromDBSplits;
import com.daniel.FitTrackerApp.goal.GoalManager;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.provider.ProviderContract;
import com.daniel.FitTrackerApp.sportactivity.SportActivity;
import com.daniel.FitTrackerApp.sportactivity.SportActivityMap;
import com.daniel.FitTrackerApp.utils.MapUtils;
import com.daniel.FitTrackerApp.utils.UnitUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.UUID;

public class SelectedActivityFromDB extends Fragment implements OnMapReadyCallback
{
    public static final String DELETED_ACTIVITY = "deleted_activity";

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private SportActivity sportActivity;
    private TextView distanceView, durationView, avgSpeedView, avgPaceView, stepsView, caloriesView;
    private Button splitsButton;
    private ImageButton deleteActivityButton, editActivityButton;
    private boolean isMetric;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_useractivity_fromdb, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity)getActivity()).provideBackwardNavigation();
        isMetric = PreferencesHelper.getInstance().isMetric(getActivity());
        setData(sportActivity, view);
        final ContentResolver mContentResolver = getActivity().getContentResolver();

        final Uri uri = ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath("0").appendPath(sportActivity.getId().toString())
                                            .appendPath(PreferencesHelper.getInstance().getCurrentUserId(getActivity())).build();

        mapFragment = SupportMapFragment.newInstance(new GoogleMapOptions().liteMode(true));
        getChildFragmentManager().beginTransaction().replace(R.id.content_frame, mapFragment).commit();

        mapFragment.getMapAsync(this);


        splitsButton = (Button) view.findViewById(R.id.splitsButton);
        splitsButton.setOnClickListener(onClickListener);

        deleteActivityButton = (ImageButton) view.findViewById(R.id.deleteButton);
        deleteActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentResolver.delete(uri, null, null);
                mContentResolver.notifyChange(uri, null, false);
                GoalManager.getInstance().addStats(-sportActivity.getDistance(),
                                                    -sportActivity.getDuration(),
                                                    -sportActivity.getCalories(),
                                                    -sportActivity.getSteps(),
                                                    sportActivity.getStartTimestamp());
                AppNetworkManager.sendSportActivityDelete(getActivity(), sportActivity.getId().toString());

                getActivity().getSupportFragmentManager().popBackStack();
                ((MainActivity) getActivity()).setNavDrawerToggleOn();
                ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
                ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        });

        editActivityButton = (ImageButton) view.findViewById(R.id.edit_activity_button);
        editActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditSportActivity fragment = new EditSportActivity();
                fragment.setSportActivity(sportActivity.toShortSportActivityServer());
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        if(mMap != null)
        {
            mMap.setOnMapLoadedCallback(mapLoadedCallback);
        }
    }

    GoogleMap.OnMapLoadedCallback mapLoadedCallback = new GoogleMap.OnMapLoadedCallback()
    {
        @Override
        public void onMapLoaded()
        {
            CameraUpdate cameraUpdate = MapUtils.cameraBounds(sportActivity.getSportActivityMap());
            if(cameraUpdate != null)
            {
                mMap.moveCamera(MapUtils.cameraBounds(sportActivity.getSportActivityMap()));
            }
            float zoom = mMap.getCameraPosition().zoom;
            if(zoom > SportActivityMap.defaultCameraZoom)
            {
                mMap.moveCamera(CameraUpdateFactory.zoomTo(SportActivityMap.defaultCameraZoom));
            }
            MapUtils.redrawShapesOnMap(sportActivity.getSportActivityMap(), mMap);
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.splitsButton:
                {
                    Intent intent = new Intent(getActivity(), SelectedActivityFromDBSplits.class);
                    intent.putExtra("activityID", sportActivity.getId().toString());
                    startActivity(intent);
                    break;
                }
            }
        }
    };

    private void setData(SportActivity recordingSportActivity, View view)
    {
        distanceView = (TextView) view.findViewById(R.id.distanceView);
        durationView = (TextView) view.findViewById(R.id.durationView);
        avgPaceView = (TextView) view.findViewById(R.id.avgPaceView);
        avgSpeedView = (TextView) view.findViewById(R.id.avgSpeedView);
        stepsView = (TextView) view.findViewById(R.id.stepsView);
        caloriesView = (TextView) view.findViewById(R.id.caloriesTextView);

        distanceView.setText(UnitUtils.getDistanceString(getActivity(), sportActivity.getDistance(), isMetric));
        durationView.setText(recordingSportActivity.getDurationString());
        avgPaceView.setText(recordingSportActivity.getAveragePaceString());
        avgSpeedView.setText(recordingSportActivity.getAverageSpeedString());
        stepsView.setText(recordingSportActivity.getStepsString());
        caloriesView.setText(String.valueOf(recordingSportActivity.getCalories()));
    }

    public void setSportActivity(Context context, UUID id)
    {
        sportActivity = DBHelper.getInstance().getActivity(context, id, PreferencesHelper.getInstance().getCurrentUserId(context));
    }
}
