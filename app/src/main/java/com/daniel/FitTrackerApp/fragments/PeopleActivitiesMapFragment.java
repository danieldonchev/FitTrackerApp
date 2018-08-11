package com.daniel.FitTrackerApp.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daniel.FitTrackerApp.AppNetworkManager;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.IntentServiceResultReceiver;
import com.daniel.FitTrackerApp.utils.UnitUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.tracker.shared.SerializeHelper;
import com.tracker.shared.SportActivityWithOwner;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;


public class PeopleActivitiesMapFragment extends Fragment implements OnMapReadyCallback, LocationListener, IntentServiceResultReceiver.Receiver {

    private RelativeLayout activityPreview;
    private TextView distanceText, unitText, durationText, caloriesText, stepsText;
    private ImageView profilePicImageView;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean isZoomedOnLocation, isCameraMoving;
    private boolean isBoundSet;
    private ArrayList<Marker> markers, selectedActivityMarkers;
    private Polyline currentPolyline;
    private com.tracker.shared.SportActivityMap map;
    private ArrayList<SportActivityWithOwner> sportActivityWithOwners;
    private boolean isMetric;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_people_activities_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isMetric = PreferencesHelper.getInstance().isMetric(getActivity());

        activityPreview = (RelativeLayout) view.findViewById(R.id.activityPreview);
        distanceText = (TextView) view.findViewById(R.id.distanceTextView);
        unitText = (TextView) view.findViewById(R.id.unitTextView);
        durationText = (TextView) view.findViewById(R.id.durationTextView);
        caloriesText = (TextView) view.findViewById(R.id.caloriesTextView);
        stepsText = (TextView) view.findViewById(R.id.stepsTextView);
        profilePicImageView = (ImageView) view.findViewById(R.id.profilePic);

        activityPreview.setVisibility(View.GONE);

        mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.content_frame, mapFragment).commit();
        mapFragment.getMapAsync(this);
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(3000);
        buildGoogleApiClientLocation(getActivity());
        markers = new ArrayList<>();
        selectedActivityMarkers = new ArrayList<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
            mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
                    isCameraMoving = true;
                }
            });
            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    if(isCameraMoving){

                        for(Marker marker : markers){
                            marker.remove();
                        }
                        getSharedActivities();
                    }
                    isCameraMoving = false;
                }
            });

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if(currentPolyline != null){
                        currentPolyline.remove();
                    }

                    for(Marker marker : selectedActivityMarkers){
                        marker.remove();
                    }
                    activityPreview.setVisibility(View.GONE);
                }
            });

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                        Object object = marker.getTag();
                        if(object != null){
                            int index = (Integer) object;
                            SportActivityWithOwner activityWithOwner = sportActivityWithOwners.get(index);
                            IntentServiceResultReceiver mReceiver = new IntentServiceResultReceiver(new Handler());
                            mReceiver.setReceiver(PeopleActivitiesMapFragment.this);
                            AppNetworkManager.getSharedMap(getActivity(), activityWithOwner.getActivityID(),
                                    activityWithOwner.getUserID(), mReceiver);

                            if(currentPolyline != null){
                                currentPolyline.remove();
                            }
                            for(Marker activityMarker : selectedActivityMarkers){
                                activityMarker.remove();
                            }

                            activityPreview.setVisibility(View.VISIBLE);
                            if(activityWithOwner.getProfilePic() != null){
                                profilePicImageView.setImageBitmap(BitmapFactory.decodeByteArray(activityWithOwner.getProfilePic(), 0, activityWithOwner.getProfilePic().length));
                            } else {
                                profilePicImageView.setImageResource(R.drawable.potato);
                            }
                            distanceText.setText(isMetric ? AppUtils.doubleToString(UnitUtils.convertMetersToUnit(activityWithOwner.getDistance(), "km")) :
                                                            AppUtils.doubleToString(UnitUtils.convertMetersToUnit(activityWithOwner.getDistance(), "miles")));
                            unitText.setText(isMetric ? getString(R.string.km) : getString(R.string.miles));
                            durationText.setText(AppUtils.convertSecondsToString(activityWithOwner.getDuration()));
                            caloriesText.setText(String.valueOf(activityWithOwner.getCalories()));
                            stepsText.setText(String.valueOf(activityWithOwner.getSteps()));
                        }

                    return false;
                }
            });
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        if(!isZoomedOnLocation){
            if(location.getAccuracy() < 25){
                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));

                mMap.moveCamera(center);
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
            isZoomedOnLocation = true;
        }
    }

    public void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient.isConnected()) {
                com.google.android.gms.location.LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            com.google.android.gms.location.LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    private synchronized void buildGoogleApiClientLocation(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(failedListener)
                .addApi(com.google.android.gms.location.LocationServices.API)
                .build();
    }

    GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
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

    private void getSharedActivities(){
        VisibleRegion vr = mMap.getProjection().getVisibleRegion();
        LatLng topLeft = vr.farLeft;
        LatLng topRight = vr.farRight;
        LatLng bottomLeft = vr.nearLeft;
        LatLng bottomRight = vr.nearRight;
        JSONObject bounds = new JSONObject();
        BigDecimal decimal = new BigDecimal(String.valueOf(vr.farLeft.latitude)).setScale(3, BigDecimal.ROUND_FLOOR);
        try {
            bounds.put("topLeftLat", String.format("%.5f", topLeft.latitude));
            bounds.put("topLeftLong", String.format("%.5f", topLeft.longitude));
            bounds.put("topRightLat", String.format("%.5f", topRight.latitude));
            bounds.put("topRightLong", String.format("%.5f", topRight.longitude));
            bounds.put("bottomLeftLat", String.format("%.5f", bottomLeft.latitude));
            bounds.put("bottomLeftLong", String.format("%.5f", bottomLeft.longitude));
            bounds.put("bottomRightLat", String.format("%.5f", bottomRight.latitude));
            bounds.put("bottomRightLong", String.format("%.5f", bottomRight.longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        IntentServiceResultReceiver mReceiver = new IntentServiceResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        AppNetworkManager.getPeopleActivities(getActivity(), bounds, mReceiver);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if(resultCode == 1){
            int i = 0;
            sportActivityWithOwners = SerializeHelper.deserializeSportActivityWithOwners(resultData.getByteArray("sportActivities"));
            for(SportActivityWithOwner activity : sportActivityWithOwners) {
                if(mMap != null){
                    byte[] bytes = activity.getProfilePic();
                    Marker marker = null;
                    Float widthF = getResources().getDimension(R.dimen.mini_profile_pic_width);
                    Float heightF = getResources().getDimension(R.dimen.mini_profile_pic_height);
                    int width = widthF.intValue();
                    int height = heightF.intValue();
                    if(bytes != null){
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        bitmap =  bitmap.createScaledBitmap(bitmap, width, height, true);
                        marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(activity.getLatLng().latitude, activity.getLatLng().longitude))
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    } else {
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.potato);
                        bitmap =  bitmap.createScaledBitmap(bitmap, width, height, true);
                        marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(activity.getLatLng().latitude, activity.getLatLng().longitude))
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    }

                    marker.setTag(i);
                    markers.add(marker);
                    i++;
                }
            }
        } else if(resultCode == 2){
            map = new com.tracker.shared.SportActivityMap().deserialize(resultData.getByteArray("map"));
            if (map != null) {
                for (com.tracker.shared.LatLng latLng : map.getMarkers())
                {
                    int size = map.getMarkers().size();

                    if(latLng == map.getMarkers().get(size - 1))
                    {
                        LatLng location = new LatLng(latLng.latitude, latLng.longitude);
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(location)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        selectedActivityMarkers.add(mMap.addMarker(markerOptions));
                    }
                    else
                    {
                        LatLng location = new LatLng(latLng.latitude, latLng.longitude);
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(location);
                        selectedActivityMarkers.add(mMap.addMarker(markerOptions));
                    }
                }
            }
            if (map.getPolyline() != null)
            {
                PolylineOptions polyline = new PolylineOptions();
                for (com.tracker.shared.LatLng latLng : map.getPolyline()) {
                    polyline.add(new LatLng(latLng.latitude, latLng.longitude));
                }
                currentPolyline = mMap.addPolyline(polyline);
            }
        }
    }
}
