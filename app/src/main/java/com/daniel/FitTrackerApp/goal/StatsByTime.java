package com.daniel.FitTrackerApp.goal;

public class StatsByTime {

    public Statistics daily;
    public Statistics weekly;
    public Statistics monthly;

    public StatsByTime() {
        daily = new Statistics();
        weekly = new Statistics();
        monthly = new Statistics();
    }
}
