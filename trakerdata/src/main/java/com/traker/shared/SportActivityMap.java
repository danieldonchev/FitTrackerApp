package com.traker.shared;

import com.google.flatbuffers.FlatBufferBuilder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.ListIterator;

public class SportActivityMap implements FlatBufferSerializable
{
    private ArrayList<LatLng> polyline;
    private ArrayList<LatLng> markers;

    public SportActivityMap()
    {
        polyline = new ArrayList<>();
        markers = new ArrayList<>();
    }

    public ArrayList<LatLng> getPolyline() {
        return polyline;
    }

    public void setPolyline(ArrayList<LatLng> polyline) {
        this.polyline = polyline;
    }

    public ArrayList<LatLng> getMarkers() {
        return markers;
    }

    public void setMarkers(ArrayList<LatLng> markers) {
        this.markers = markers;
    }

    @Override
    public byte[] serialize()
    {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int finish = getBufferInt(builder);

        builder.finish(finish);
        ByteBuffer buf = builder.dataBuffer();
        byte[] array = new byte[buf.remaining()];
        buf.get(array);
        return array;
    }

    @Override
    public SportActivityMap deserialize(byte[] bytesRead)
    {
        ByteBuffer buf = ByteBuffer.wrap(bytesRead);
        flatbuf.SportActivityMap sportActivityMap = flatbuf.SportActivityMap.getRootAsSportActivityMap(buf);

        return deserializeFromFlatBuffMap(sportActivityMap);
    }

    public int getBufferInt(FlatBufferBuilder builder)
    {
        ListIterator<LatLng> polyLineIterator = this.polyline.listIterator(this.polyline.size());
        ListIterator<LatLng> markerIterator = this.markers.listIterator(this.markers.size());

        flatbuf.SportActivityMap.startMarkersVector(builder, this.markers.size());

        while(markerIterator.hasPrevious())
        {
            LatLng latLng = markerIterator.previous();
            flatbuf.Markers.createMarkers(builder, latLng.latitude, latLng.longitude, 0);
        }

        int markers = builder.endVector();

        flatbuf.SportActivityMap.startPolylineVector(builder, this.polyline.size());

        while(polyLineIterator.hasPrevious())
        {
            LatLng latLng = polyLineIterator.previous();
            flatbuf.Polyline.createPolyline(builder, latLng.latitude, latLng.longitude);
        }

        int polyline = builder.endVector();

        flatbuf.SportActivityMap.startSportActivityMap(builder);
        flatbuf.SportActivityMap.addMarkers(builder, markers);
        flatbuf.SportActivityMap.addPolyline(builder, polyline);

        return flatbuf.SportActivityMap.endSportActivityMap(builder);
    }

    public SportActivityMap deserializeFromFlatBuffMap(flatbuf.SportActivityMap map){
        for(int i = 0; i < map.markersLength(); i++)
        {
            flatbuf.Markers marker = map.markers(i);
            this.markers.add(new LatLng(marker.lat(), marker.lon()));
        }
        for(int i = 0; i < map.polylineLength(); i++)
        {
            flatbuf.Polyline polyline = map.polyline(i);
            this.polyline.add(new LatLng(polyline.lat(), polyline.lon()));
        }

        return this;
    }
}
