package com.daniel.FitTrackerApp.sportactivity;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class SportActivitySummariesByTime {

    private String dateRange;
    private double distance;
    private long duration;
    private long calories;
    private long steps;
    private Date startWeek;
    private Date endWeek;
    private Date month;

    public SportActivitySummariesByTime(double distance, long duration, long calories, long steps, Date startWeek, Date endWeek){
        this.distance = distance;
        this.duration = duration;
        this.calories = calories;
        this.steps = steps;
        this.startWeek = startWeek;
        this.endWeek = endWeek;

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        start.clear();
        end.clear();

        start.setTimeInMillis(startWeek.getTime());
        end.setTimeInMillis(endWeek.getTime());

        dateRange = start.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " ";
        dateRange += String.valueOf(start.get(Calendar.DAY_OF_MONTH));
        if(start.get(Calendar.YEAR) != end.get(Calendar.YEAR)){
            dateRange += ", " + String.valueOf(start.get(Calendar.YEAR));
        }
        dateRange += " - ";
        if(start.get(Calendar.MONTH) != end.get(Calendar.MONTH)){
            dateRange += end.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " ";
        }
        dateRange += end.get(Calendar.DAY_OF_MONTH);
        if(start.get(Calendar.YEAR) != end.get(Calendar.YEAR)){
            dateRange += ", " + String.valueOf(end.get(Calendar.YEAR));
        }

    }

    public SportActivitySummariesByTime(double distance, long duration, long calories, long steps, Date month){
        this.distance = distance;
        this.duration = duration;
        this.calories = calories;
        this.steps = steps;
        this.month = month;

            Calendar calendar = GregorianCalendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);
            String currentYearString = "";
            calendar.clear();
            calendar.setTimeInMillis(this.getMonth().getTime());
            dateRange = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            if(currentYear != calendar.get(Calendar.YEAR)){
                dateRange += " " + String.valueOf(calendar.get(Calendar.YEAR));
            }
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getCalories() {
        return calories;
    }

    public void setCalories(long calories) {
        this.calories = calories;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public Date getStartWeek() {
        return startWeek;
    }

    public void setStartWeek(Date startWeek) {
        this.startWeek = startWeek;
    }

    public Date getEndWeek() {
        return endWeek;
    }

    public void setEndWeek(Date endWeek) {
        this.endWeek = endWeek;
    }

    public Date getMonth() {
        return month;
    }

    public void setMonth(Date month) {
        this.month = month;
    }
}
