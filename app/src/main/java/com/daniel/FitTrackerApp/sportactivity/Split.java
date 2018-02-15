package com.daniel.FitTrackerApp.sportactivity;

public class Split extends AbstractSportActivity
{
    private double split;
    private int id;

    public Split(int id) {
        this.id = id;
    }

    public Split(int id, long startTime)
    {
        this.distance = 0;
        this.startTime = startTime;
    }

    public Split(int id, long startTime, float split)
    {
        this.startTime = startTime;
        this.split = split;
    }

    public Split(int id, long duration, double distance, boolean isMetric)
    {
        this(id);
        this.isMetric = isMetric;
        this.split = distance;
        this.duration = duration;
        this.distance = distance;
    }

    public String getSplitString(){return String.valueOf(this.split);}

    public int getId() {
        return id;
    }
}
