package com.daniel.FitTrackerApp.sportactivity;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class SportActivitySummary extends AbstractSportActivity
{
    private UUID id;
    private long startTimeStamp;
    private long endTimeStamp;

    public SportActivitySummary(){}

    public SportActivitySummary(UUID id)
    {
        this.id = id;
    }

    public SportActivitySummary(UUID id, double distance, long duration, long startTimeStamp, long endTimeStamp, boolean isMetric)
    {
        this(id);
        this.distance = distance;
        this.duration = duration;
        this.startTimeStamp = startTimeStamp;
        this.endTimeStamp = endTimeStamp;
        this.isMetric = isMetric;
        calculateFinalData(isMetric, distance);
    }

    public String getDateString(long timestamp)
    {
        return DateFormat.getDateTimeInstance().format(new Date(timestamp));
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(long endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
