package com.daniel.FitTrackerApp.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.daniel.FitTrackerApp.activities.MainActivity;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.sportactivity.SportActivity;
import com.daniel.FitTrackerApp.sportactivity.SportActivityMap;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.utils.MapUtils;
import com.daniel.FitTrackerApp.utils.UnitUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

import static com.daniel.FitTrackerApp.utils.AppUtils.doubleToString;


public class SaveActivityFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback
{
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private TextView durationView, distanceView, avgPaceView, avgSpeedView, stepsView, caloriesTextView;
    private SportActivity sportActivity;
    private String sportActivityId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_save, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance)
    {
        ((MainActivity)getActivity()).provideBackwardNavigation();
        mapFragment = SupportMapFragment.newInstance(new GoogleMapOptions().liteMode(true));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapPreview);
        }
        else
        {
            mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapPreview);
        }

        distanceView = (TextView) view.findViewById(R.id.distanceView);
        durationView = (TextView) view.findViewById(R.id.durationView);
        avgSpeedView = (TextView) view.findViewById(R.id.avgSpeedView);
        avgPaceView = (TextView) view.findViewById(R.id.avgPaceView);
        stepsView = (TextView) view.findViewById(R.id.stepsView);
        caloriesTextView = (TextView) view.findViewById(R.id.caloriesTextView);

        mapFragment.getMapAsync(this);

        if(sportActivityId != null)
        {
            sportActivity = DBHelper.getInstance().getActivity(getActivity(), UUID.fromString(sportActivityId), PreferencesHelper.getInstance().getCurrentUserId(getActivity()));
        }

        setUI();
        setMapUI();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        if(mMap != null && sportActivity != null)
        {
            setMapUI();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public void setSportActivity(SportActivity sportActivity)
    {
        this.sportActivity = sportActivity;
    }

    public void setSportActivityId(String id)
    {
        this.sportActivityId = id;
    }

    public void setUI()
    {
        boolean isMetric = PreferencesHelper.getInstance().isMetric(getActivity());
        distanceView.setText(isMetric ? doubleToString(UnitUtils.convertMetersToUnit(sportActivity.getDistance(), getString(R.string.km))) :
                    doubleToString(UnitUtils.convertMetersToUnit(sportActivity.getDistance(), getString(R.string.miles))));
        durationView.setText(sportActivity.getDurationString());
        avgSpeedView.setText(sportActivity.getAverageSpeedString());
        avgPaceView.setText(sportActivity.getAveragePaceString());
        stepsView.setText(sportActivity.getStepsString());
        caloriesTextView.setText(String.valueOf(sportActivity.getCalories()));
    }

    public void setMapUI()
    {
        if(mMap != null)
        {
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
            {
                @Override
                public void onMapClick(LatLng latLng) {
                    Toast.makeText(getActivity().getApplicationContext(), "Make toasts great again", Toast.LENGTH_SHORT).show();
                }
            });

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
    }
}
