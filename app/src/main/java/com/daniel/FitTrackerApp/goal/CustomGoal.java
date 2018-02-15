package com.daniel.FitTrackerApp.goal;


import android.support.annotation.Nullable;

import java.util.Date;
import java.util.UUID;

public class CustomGoal extends Goal {

    @Nullable
    private Date fromDate;
    @Nullable
    private Date toDate;
    @Nullable
    private StatsBetweenTime stats;

    public CustomGoal(double distance, long duration, long calories, long steps, Date fromDate, Date toDate){
        super();
        this.setType(Goal.CUSTOM);
        this.distance = distance;
        this.duration = duration;
        this.calories = calories;
        this.steps = steps;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public CustomGoal(UUID id, double distance, long duration, long calories, long steps, long fromDate, long toDate){
        super(id, Goal.CUSTOM, distance, duration, calories, steps);
        this.fromDate = new Date(fromDate);
        this.toDate = new Date(toDate);
    }

    public void calculateProgress() {
        int i = 0;
        double distanceProgress = 0, durationProgress = 0, caloriesProgress = 0, stepsProgress = 0;
        if(this.distance > 0){
            distanceProgress = stats.distance * 100 / this.distance;
            i++;
        }
        if(this.duration > 0){
            durationProgress = stats.duration * 100 / this.duration;
            i++;
        }
        if(this.calories > 0){
            caloriesProgress = stats.calories * 100 / this.calories;
            i++;
        }
        if(this.steps > 0){
            stepsProgress = stats.steps * 100 / this.steps;
            i++;
        }
        Double progress = (distanceProgress + durationProgress + caloriesProgress + stepsProgress) / i;
        this.progress = (int)Math.round(progress);
    }

    @Override
    public com.traker.shared.Goal toServerGoal() {
        com.traker.shared.Goal goal = new com.traker.shared.Goal(id,
                CUSTOM,
                distance,
                duration,
                calories,
                steps,
                fromDate.getTime(),
                toDate.getTime(),
                lastModified);
        return goal;
    }

    @Override
    public void fromServerGoal(com.traker.shared.Goal goal) {
        this.id = goal.getId();
        this.distance = goal.getDistance();
        this.duration = goal.getDuration();
        this.calories = goal.getCalories();
        this.steps = goal.getSteps();
        fromDate.setTime(goal.getFromDate());
        toDate.setTime(goal.getToDate());
    }

    @Nullable
    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(@Nullable Date fromDate) {
        this.fromDate = fromDate;
    }

    @Nullable
    public Date getToDate() {
        return toDate;
    }

    public void setToDate(@Nullable Date toDate) {
        this.toDate = toDate;
    }

    @Nullable
    public StatsBetweenTime getStats() {
        return stats;
    }

    public void setStats(@Nullable StatsBetweenTime stats) {
        this.stats = stats;
    }
}
