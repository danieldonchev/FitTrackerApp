package com.daniel.FitTrackerApp.sportactivity;

import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
//import com.google.flatbuffers.FlatBufferBuilder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.ListIterator;

public class SportActivityMap
{
    public static float defaultCameraZoom = 17f;

    private PolylineOptions polylineOptions;
    private CameraPosition cameraPosition;
    private ArrayList<LatLng> markerPositions;

    public SportActivityMap()
    {
        markerPositions = new ArrayList<>();
        polylineOptions =  new PolylineOptions().width(25)
                                                .color(Color.BLUE)
                                                .geodesic(true);
    }

    public ArrayList<LatLng> getMarkerPositions() {return markerPositions;}

    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    public void setPolylineOptions(PolylineOptions polylineOptions) {this.polylineOptions = polylineOptions;}

    public CameraPosition getCameraPosition() {return cameraPosition;}

    public void setCameraPosition(Location location) {this.cameraPosition = cameraBuilder(location);}

    public CameraPosition cameraBuilder(Location location)
    {
        return new CameraPosition.Builder()
                .target(locationLatLng(location))
                .bearing(location.getBearing())
                .zoom(SportActivityMap.defaultCameraZoom).build();
    }

    public com.tracker.shared.Entities.SportActivityMap toSharedSportActivityMap()
    {
        com.tracker.shared.Entities.SportActivityMap sportActivityMap = new com.tracker.shared.Entities.SportActivityMap();
        ListIterator<LatLng> polylineIterator = this.polylineOptions.getPoints().listIterator();
        ListIterator<LatLng> markerIterator = this.markerPositions.listIterator();

        for(LatLng latLng : polylineOptions.getPoints())
        {
            com.tracker.shared.Entities.LatLng simpleLatLng = new com.tracker.shared.Entities.LatLng(latLng.latitude, latLng.longitude);
            sportActivityMap.getPolyline().add(simpleLatLng);
        }

        for(LatLng latLng : markerPositions)
        {
            com.tracker.shared.Entities.LatLng simpleLatLng = new com.tracker.shared.Entities.LatLng(latLng.latitude, latLng.longitude);
            sportActivityMap.getMarkers().add(simpleLatLng);
        }

        return sportActivityMap;
    }



//    public byte[] serializeThis()
//    {
//        FlatBufferBuilder builder = new FlatBufferBuilder(0);
//        int finish = getBufferInt(builder);
//
//        builder.finish(finish);
//        ByteBuffer buf = builder.dataBuffer();
//        byte[] array = new byte[buf.remaining()];
//        buf.get(array);
//        return array;
//    }
//
//    public int getBufferInt(FlatBufferBuilder builder)
//    {
//        ListIterator<LatLng> polyLineIterator = this.polylineOptions.getPoints().listIterator(this.polylineOptions.getPoints().size());
//        ListIterator<LatLng> markerIterator = this.markerPositions.listIterator(this.markerPositions.size());
//
//        flatbuf.SportActivityMap.startMarkersVector(builder, this.markerPositions.size());
//
//        while(markerIterator.hasPrevious())
//        {
//            LatLng latLng = markerIterator.previous();
//            flatbuf.Markers.createMarkers(builder, latLng.latitude, latLng.longitude, 0);
//        }
//
//        int markers = builder.endVector();
//
//        flatbuf.SportActivityMap.startPolylineVector(builder, this.polylineOptions.getPoints().size());
//
//        while(polyLineIterator.hasPrevious())
//        {
//            LatLng latLng = polyLineIterator.previous();
//            flatbuf.Polyline.createPolyline(builder, latLng.latitude, latLng.longitude);
//        }
//
//        int polyline = builder.endVector();
//
//        flatbuf.SportActivityMap.startSportActivityMap(builder);
//        flatbuf.SportActivityMap.addMarkers(builder, markers);
//        flatbuf.SportActivityMap.addPolyline(builder, polyline);
//
//        return flatbuf.SportActivityMap.endSportActivityMap(builder);
//    }

    public void deserialize(byte[] bytesRead)
    {
//        ByteBuffer buf = ByteBuffer.wrap(bytesRead);
//        flatbuf.SportActivityMap sportActivityMap = flatbuf.SportActivityMap.getRootAsSportActivityMap(buf);

        com.tracker.shared.Entities.SportActivityMap map = new com.tracker.shared.Entities.SportActivityMap();
        map.deserialize(bytesRead);

        for(int i = 0; i < map.getMarkers().size(); i++){
            this.markerPositions.add(new LatLng(map.getMarkers().get(i).latitude, map.getMarkers().get(i).longitude));
        }

        for(int i = 0; i < map.getPolyline().size(); i++){
            this.polylineOptions.add(new LatLng(map.getPolyline().get(i).latitude, map.getPolyline().get(i).longitude));
        }

        //deserializeFromFlatBuffMap(sportActivityMap);
    }

//    public SportActivityMap deserializeFromFlatBuffMap(flatbuf.SportActivityMap map){
//        for(int i = 0; i < map.markersLength(); i++)
//        {
//            flatbuf.Markers marker = map.markers(i);
//            this.markerPositions.add(new LatLng(marker.lat(), marker.lon()));
//        }
//        for(int i = 0; i < map.polylineLength(); i++)
//        {
//            flatbuf.Polyline polyline = map.polyline(i);
//            this.polylineOptions.add(new LatLng(polyline.lat(), polyline.lon()));
//        }
//
//        return this;
//    }

    public MarkerOptions addSplitMarker(LatLng latLng)
    {
        markerPositions.add(latLng);
        return getSplitMarkerOptions(latLng);
    }

    public MarkerOptions addStartEndMarkers(LatLng latLng)
    {
        MarkerOptions markerOptions = getStartEndMarker(latLng);
        markerPositions.add(latLng);
        return markerOptions;
    }

    public MarkerOptions getSplitMarkerOptions(LatLng latLng)
    {
        return  new MarkerOptions().position(latLng)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
    }

    public MarkerOptions getStartEndMarker(LatLng latLng)
    {
        return new MarkerOptions().position(latLng);
    }

    private LatLng locationLatLng(Location location)
    {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}
