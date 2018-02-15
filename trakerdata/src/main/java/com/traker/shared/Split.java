package com.traker.shared;

public class Split
{
    private int id;
    private long duration;
    private double distance;

    public Split(int id) {
        this.id = id;
    }

    public Split(int id, long duration, double distance) {
        this(id);
        this.duration = duration;
        this.distance = distance;
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

    public int getId() {
        return id;
    }
}
