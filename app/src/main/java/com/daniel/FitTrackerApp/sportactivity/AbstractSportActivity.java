package com.daniel.FitTrackerApp.sportactivity;

import android.content.Context;

import com.daniel.FitTrackerApp.models.ActivityCalories;
import com.daniel.FitTrackerApp.models.BMR;

import java.util.Locale;

import static com.daniel.FitTrackerApp.utils.AppUtils.doubleToString;

public abstract class AbstractSportActivity
{
    protected String workout;
    protected long startTime = 0;
    protected long duration = 0;
    protected double pace = 0;
    protected double distance = 0;
    protected double averageSpeed = 0;
    protected long steps = 0;
    protected boolean isMetric;
    protected int calories;
    protected int type;
    protected long lastModified = 0;

    public AbstractSportActivity()
    {}

    // Calculates average speed, pace and calories
    public void calculateFinalData(Context context, boolean isMetric, String gender, int height, float weight, int age, double distance) {
        calculateFinalData(isMetric, distance);
        Double caloriesBurned = BMR.getBMRperHour(gender, height, weight, age) * ActivityCalories.getCurrentMET(context, isMetric, ((Double)averageSpeed).floatValue(), workout) * duration / 3600 + duration * BMR.getBMRperSecond(gender, height, weight, age);
        calories = caloriesBurned.intValue();
    }

    // Calculates average speed, pace wthiout calories
    public void calculateFinalData(boolean isMetric, double distance) {
        this.distance = distance;
        averageSpeed = calculateFinalAverageSpeed(isMetric, distance);
        pace = calculateAveragePace(averageSpeed);
    }

    public String getDurationString() {
        return convertSecondsToString(this.duration);
    }

    public String getDistanceString() {
        if(distance > 0){
            return doubleToString(this.distance);
        } else {
            return "0.00";
        }
    }

    public String getAverageSpeedString() {
        if (this.averageSpeed > 0) {
            return doubleToString(this.averageSpeed);
        } else {
            return "0.00";
        }
    }

    public double getFinalTimeHours() {
        return (double) duration / 3600;
    }

    protected double calculateFinalAverageSpeed(boolean isMetric, double distance) {
        if(isMetric){
            return distance / getDuration() * 3.6;
        } else {
            return distance / getDuration() * 2.23693629;
        }

    }

    public String getAveragePaceString() {
        double milisecs = getPace() * 100;
        int miliseconds = (int) milisecs % 100;
        int hours = (int) getPace() / 3600;
        int minutes = ((int) getPace() % 3600) / 60;
        int seconds = (int) getPace() % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%1d:%02d:%02d", hours, minutes, seconds);
        }
        if (minutes > 0 || minutes == 0 && hours == 0) {
            return String.format(Locale.getDefault(), "%1d:%02d.%02d", minutes, seconds, miliseconds);
        }
        return "00.00";
    }

    public double calculateAveragePace(double averageSpeed) {
        return averageSpeed == 0 ? 0 : (3600 / averageSpeed);
    }

    protected String convertSecondsToString(long seconds) {
        long h = seconds / 3600;
        long rem = seconds % 3600;
        long m = rem / 60;
        long s = rem % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    }

    public double getDistance() {
        if (Double.isNaN(this.distance)) {
            return 0.0;
        }
        return this.distance;
    }

    public double getPace() {
        if (Double.isNaN(this.pace)) {
            return 0.0;
        }
        return this.pace;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    //in seconds
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setPace(double pace) {
        this.pace = pace;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public boolean isMetric() {
        return isMetric;
    }

    public void setMetric(boolean metric) {
        isMetric = metric;
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

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getWorkout() {
        return workout;
    }

    public void setWorkout(String workout) {
        this.workout = workout;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
