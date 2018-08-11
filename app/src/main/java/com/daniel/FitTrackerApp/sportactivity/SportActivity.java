package com.daniel.FitTrackerApp.sportactivity;

import java.util.ArrayList;
import java.util.UUID;

import static com.daniel.FitTrackerApp.utils.AppUtils.doubleToString;

public class SportActivity extends AbstractSportActivity
{
    public static final int RECORDED = 1;
    public static final int TRACKED = 2;
    public static final int MANUAL_ADD = 3;

    private String id;
    private SportActivityMap sportActivityMap;
    private long startTimestamp;
    private long endTimestamp;
    private ArrayList<Split> splits;

    public SportActivity(String id){
        this.id = id;
    }

    public SportActivity(String id, String activity)
    {
        this.workout = activity;
        this.id = id;
        splits = new ArrayList<>();
        sportActivityMap = new SportActivityMap();
    }

    public SportActivity(String activity, long duration, double distance, long steps, int calories, SportActivityMap sportActivityMap, long startTimestamp,
                         long endTimestamp, long lastModified, ArrayList<Split> splits)
    {
        this(UUID.randomUUID().toString(), activity);
        this.duration = duration;
        this.distance = distance;
        this.steps = steps;
        this.calories = calories;
        this.sportActivityMap = sportActivityMap;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.lastModified = lastModified;
        this.splits = splits;
    }

    public com.tracker.shared.Entities.SportActivityWeb toShortSportActivityServer()
    {
        return new com.tracker.shared.Entities.SportActivityWeb(id.toString(),
                workout,
                duration,
                distance,
                (int) steps,
                (int)calories,
                startTimestamp,
                endTimestamp,
                lastModified);
    }

    public String getStepsString() {
        return doubleToString(this.steps);
    }

    public SportActivityMap getSportActivityMap() {
        return sportActivityMap;
    }

    public void setSportActivityMap(SportActivityMap sportActivityMap) {
        this.sportActivityMap = sportActivityMap;
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

    public String getId() {
        return id;
    }

    public ArrayList<Split> getSplits() {
        return splits;
    }

    public void setSplits(ArrayList<Split> splits) {
        this.splits = splits;
    }
}
