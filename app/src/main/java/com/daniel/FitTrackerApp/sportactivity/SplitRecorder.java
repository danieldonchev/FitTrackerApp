package com.daniel.FitTrackerApp.sportactivity;

import com.daniel.FitTrackerApp.utils.UnitUtils;

public class SplitRecorder extends AbstractSportActivityRecorder
{
    private double splitMeters;
    private double split;
    private boolean isMetric;

    public SplitRecorder(boolean isMetric, long startTime, float splitMeters)
    {
        this.isMetric = isMetric;
        this.startTime = startTime;
        this.splitMeters = splitMeters;

        if(isMetric){
            split = UnitUtils.convertMetersToUnit(splitMeters, "km");
        } else {
            split = UnitUtils.convertMetersToUnit(splitMeters, "miles");
        }
    }


    public String getSplitString(){return String.valueOf(this.splitMeters);}

    public void calculateData(double distance)
    {
        this.distance = distance % split;
        averageSpeed = calculateCurrentAverageSpeed(this.distance);
        pace = calculateAveragePace(averageSpeed);
    }

    public Split getSplit(int id)
    {
        return new Split(id);
    }
}
