package com.traker.shared;

import com.google.flatbuffers.FlatBufferBuilder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.UUID;

public class SportActivity extends AbstractWorkout implements FlatBufferSerializable<SportActivity>
{
    private UUID id;
    private double distance = 0;
    private long steps;
    private long startTimestamp;
    private long endTimestamp;
    private ArrayList<Split> splits;
    private SportActivityMap sportActivityMap;
    private long lastModified;

    public SportActivity()
    {
        splits = new ArrayList<>();
        sportActivityMap = new SportActivityMap();
    }

    public SportActivity(UUID id)
    {
        this.id = id;
        splits = new ArrayList<>();
        sportActivityMap = new SportActivityMap();
    }

    private SportActivity(String workout, long duration, int calories, int type)
    {
        super(workout, duration, calories, type);
        sportActivityMap = new SportActivityMap();
        splits = new ArrayList<>();
    }

    public SportActivity(UUID id, String workout, long duration, double distance, long steps, int calories, long startTimestamp, long endTimestamp, int type, long lastModified)
    {
        this(workout, duration, calories, type);
        this.id = id;
        this.distance = distance;
        this.steps = steps;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.lastModified = lastModified;
    }

    public SportActivity(UUID id, String workout, long duration, double distance, long steps, int calories, SportActivityMap sportActivityMap, long startTimestamp,
                         long endTimestamp, int type, long lastModified, ArrayList<Split> splits)
    {
        this(workout, duration, calories, type);
        this.id = id;
        this.distance = distance;
        this.steps = steps;
        this.sportActivityMap = sportActivityMap;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.lastModified = lastModified;
        this.splits = splits;
    }

    @Override
    public byte[] serialize() {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);

        int finish = getSportActivityInt(builder);

        builder.finish(finish);

        ByteBuffer buf = builder.dataBuffer();
        byte[] array = new byte[buf.remaining()];
        buf.get(array);
        return array;
    }

    @Override
    public SportActivity deserialize(byte[] bytesRead) {
        ByteBuffer buf = ByteBuffer.wrap(bytesRead);
        flatbuf.SportActivity sportActivityBufferer = flatbuf.SportActivity.getRootAsSportActivity(buf);

        this.id = UUID.fromString(sportActivityBufferer.id());
        this.workout = sportActivityBufferer.activity();
        this.startTimestamp = sportActivityBufferer.startTimestamp();
        this.endTimestamp = sportActivityBufferer.endTimestamp();
        this.distance = sportActivityBufferer.distance();
        this.duration = sportActivityBufferer.duration();
        this.steps = sportActivityBufferer.steps();
        this.calories = sportActivityBufferer.calories();
        this.type = sportActivityBufferer.type();
        this.lastModified = sportActivityBufferer.lastModified();


        if(sportActivityBufferer.sportActivityMap() != null){
            for(int i = 0; i < sportActivityBufferer.sportActivityMap().markersLength(); i++)
            {
                flatbuf.Markers marker = sportActivityBufferer.sportActivityMap().markers(i);
                sportActivityMap.getMarkers().add(new LatLng(marker.lat(), marker.lon()));
            }
            for(int i = 0; i < sportActivityBufferer.sportActivityMap().polylineLength(); i++)
            {
                flatbuf.Polyline polyline = sportActivityBufferer.sportActivityMap().polyline(i);
                sportActivityMap.getPolyline().add(new LatLng(polyline.lat(), polyline.lon()));
            }
        }
        if(sportActivityBufferer.splits() != null){
            for(int i = 0; i < sportActivityBufferer.splits().splitsLength(); i++)
            {
                flatbuf.Split split = sportActivityBufferer.splits().splits(i);
                splits.add(new Split(split.id(), split.duration(), split.distance()));
            }
        }

        return this;
    }

    public int getSportActivityInt(FlatBufferBuilder builder){
        int idString = builder.createString(id.toString());
        int activityString = builder.createString(workout);

        int cardioMapInt = 0;
        if(this.sportActivityMap != null){
            cardioMapInt = this.sportActivityMap.getBufferInt(builder);
        }
        int splits = getSplitsBufferInt(builder);

        flatbuf.SportActivity.startSportActivity(builder);
        flatbuf.SportActivity.addId(builder, idString);
        flatbuf.SportActivity.addActivity(builder, activityString);
        flatbuf.SportActivity.addSplits(builder, splits);
        flatbuf.SportActivity.addSportActivityMap(builder, cardioMapInt);
        flatbuf.SportActivity.addStartTimestamp(builder, startTimestamp);
        flatbuf.SportActivity.addEndTimestamp(builder, endTimestamp);
        flatbuf.SportActivity.addDuration(builder, duration);
        flatbuf.SportActivity.addDistance(builder, distance);
        flatbuf.SportActivity.addSteps(builder, steps);
        flatbuf.SportActivity.addCalories(builder, calories);
        flatbuf.SportActivity.addType(builder, type);
        flatbuf.SportActivity.addLastModified(builder, lastModified);

        return flatbuf.SportActivity.endSportActivity(builder);
    }

    public int getSplitsBufferInt(FlatBufferBuilder builder)
    {
        if(splits != null){
            ListIterator<Split> splitListIterator = splits.listIterator(splits.size());

            flatbuf.Splits.startSplitsVector(builder, splits.size());

            while(splitListIterator.hasPrevious())
            {
                Split split = splitListIterator.previous();
                flatbuf.Split.createSplit(builder, split.getId(), split.getDistance(), split.getDuration());
            }

            int splits = builder.endVector();

            flatbuf.Splits.startSplits(builder);
            flatbuf.Splits.addSplits(builder, splits);
            return flatbuf.Splits.endSplits(builder);
        }


        return 0;
    }

    public ArrayList<Split> getSplitsFromFlatBuffSplits(flatbuf.Splits flatBuffSplits){
        if(flatBuffSplits != null){
            for(int i = 0; i < flatBuffSplits.splitsLength(); i++)
            {
                flatbuf.Split split = flatBuffSplits.splits(i);
                this.splits.add(new Split(split.id(), split.duration(), split.distance()));
            }
        }
        return splits;
    }


    public String getWorkout() {
        return workout;
    }

    public void setWorkout(String workout) {
        this.workout = workout;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public ArrayList<Split> getSplits() {
        return splits;
    }

    public void setSplits(ArrayList<Split> splits) {
        this.splits = splits;
    }

    public SportActivityMap getSportActivityMap() {
        return sportActivityMap;
    }

    public void setSportActivityMap(SportActivityMap sportActivityMap) {
        this.sportActivityMap = sportActivityMap;
    }

    public UUID getId() {
        return id;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}
