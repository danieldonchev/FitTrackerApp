package com.daniel.FitTrackerApp.utils;

import com.daniel.FitTrackerApp.sportactivity.SportActivityMap;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MapUtils
{
    public static CameraUpdate cameraBounds(SportActivityMap googleMapData)
    {
        boolean hasPoints = false;
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        if(googleMapData.getPolylineOptions().getPoints().size() > 0)
        {
            hasPoints = true;
            for(LatLng latlng : googleMapData.getPolylineOptions().getPoints())
            {
                boundsBuilder.include(latlng);
            }
        }
        if(googleMapData.getMarkerPositions().size() > 0)
        {
            hasPoints = true;
            boundsBuilder.include(googleMapData.getMarkerPositions().get(0));
            boundsBuilder.include(googleMapData.getMarkerPositions().get(googleMapData.getMarkerPositions().size() - 1));
        }
        if(hasPoints)
        {
            LatLngBounds bounds = boundsBuilder.build();
            return CameraUpdateFactory.newLatLngBounds(bounds, 0);
        }
        else
        {
            return null;
        }

    }

    public static void redrawShapesOnMap(SportActivityMap googleMapData, GoogleMap mMap)
    {
        if (googleMapData.getMarkerPositions() != null) {
            for (LatLng latLng : googleMapData.getMarkerPositions())
            {
                int size = googleMapData.getMarkerPositions().size();
                if(latLng == googleMapData.getMarkerPositions().get(0) ||
                        latLng == googleMapData.getMarkerPositions().get(size - 1))
                {

                    mMap.addMarker(googleMapData.getStartEndMarker(latLng));
                }
                else
                {
                    mMap.addMarker(googleMapData.getSplitMarkerOptions(latLng));
                }
            }
        }
        if (googleMapData.getPolylineOptions() != null)
        {
            if(googleMapData.getPolylineOptions().getPoints().size() > 0)
            {
                mMap.addPolyline(googleMapData.getPolylineOptions());
            }
        }
    }
}
