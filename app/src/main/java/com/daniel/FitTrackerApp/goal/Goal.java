package com.daniel.FitTrackerApp.goal;

import android.content.Context;
import android.support.annotation.Nullable;

import com.daniel.FitTrackerApp.R;

import java.util.Random;
import java.util.UUID;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Goal {

    public static int DAILY = 0;
    public static int WEEKLY = 1;
    public static int MONTHLY = 2;
    public static int CUSTOM = 3;

    protected UUID id;
    protected int type;
    @Nullable protected double distance;
    @Nullable protected long duration;
    @Nullable protected long calories;
    @Nullable protected long steps;
    protected int progress;
    protected long lastModified;

    public Goal() {
        this.id = UUID.randomUUID();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public Goal(int type, double distance, long duration, long calories, long steps){
        this();
        this.type = type;
        this.distance = distance;
        this.duration = duration;
        this.calories = calories;
        this.steps = steps;
    }

    public Goal(UUID id, int type, double distance, long duration, long calories, long steps){
        this.id = id;
        this.type = type;
        this.distance = distance;
        this.duration = duration;
        this.calories = calories;
        this.steps = steps;
    }

    public void calculateProgress(Context context, double distance, long duration, long calories, long steps, boolean isAppStart){
        int i = 0;
        boolean isProgressAchieved = false;
        if(this.progress == 100 || isAppStart){
            isProgressAchieved = true;
        }
        double distanceProgress = 0, durationProgress = 0, caloriesProgress = 0, stepsProgress = 0;
        if(this.distance > 0 ){
            if(this.distance <= distance){
                distanceProgress = 100;
            } else {
                distanceProgress = distance * 100 / this.distance;
            }
            i++;
        }
        if(this.duration > 0){
            if(this.duration <= duration){
                durationProgress = 100;
            } else {
                durationProgress = duration * 100 / this.duration;
            }
            i++;
        }
        if(this.calories > 0){
            if(this.calories <= calories){
                caloriesProgress = 100;
            } else {
                caloriesProgress = calories * 100 / this.calories;
            }
            i++;
        }
        if(this.steps > 0){
            if(this.steps <= steps){
                stepsProgress = 100;
            } else {
                stepsProgress = steps * 100 / this.steps;
            }
            i++;
        }
        Double progress = (distanceProgress + durationProgress + caloriesProgress + stepsProgress) / i;
         this.progress = (int)Math.round(progress);
        if(this.progress == 100 && !isProgressAchieved){
            showNotification(context);
        }
    }

    public com.tracker.shared.Goal toServerGoal(){
        return new com.tracker.shared.Goal(id,
                type,
                distance,
                duration,
                calories,
                steps,
                0,
                0,
                lastModified);
    }

    public void fromServerGoal(com.tracker.shared.Goal goal){
        this.id = goal.getId();
        this.type = goal.getType();
        this.distance = goal.getDistance();
        this.duration = goal.getDuration();
        this.calories = goal.getCalories();
        this.steps = goal.getSteps();
    }

    private void showNotification(Context context){

        String message = "";
        if(type == DAILY){
            message = "Daily goal completed";
        } else if(type == WEEKLY){
            message = "Weekly goal completed";
        } else if(type == MONTHLY){
            message = "Monthly goal completed";
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.running_icon)
                        .setContentTitle("Goal completed")
                        .setContentText(message);
        Random r = new Random();
        int i1 = r.nextInt(1000 - 100) + 100;
        int mNotificationId = 1 + i1;

        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Nullable
    public double getDistance() {
        return distance;
    }

    public void setDistance(@Nullable double distance) {
        this.distance = distance;
    }

    @Nullable
    public long getDuration() {
        return duration;
    }

    public void setDuration(@Nullable long duration) {
        this.duration = duration;
    }

    @Nullable
    public long getCalories() {
        return calories;
    }

    public void setCalories(@Nullable long calories) {
        this.calories = calories;
    }

    @Nullable
    public long getSteps() {
        return steps;
    }

    public void setSteps(@Nullable long steps) {
        this.steps = steps;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null) {
            return false;
        }
        if(!(obj instanceof Goal)){
            return false;
        }
        final Goal goal = (Goal) obj;

        if(this.id == goal.getId()){

        }
        if(goal.getType() == this.type){

            if(goal.getSteps() > 0 && this.steps > 0){
                return true;
            }
            if(goal.getDistance() > 0 && this.distance > 0){
                return true;
            }
            if(goal.getDuration() > 0 && this.duration > 0){
                return true;
            }
            if(goal.getCalories() > 0 && this.calories > 0){
                return true;
            }
        } else {
            return false;
        }

        return false;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}
