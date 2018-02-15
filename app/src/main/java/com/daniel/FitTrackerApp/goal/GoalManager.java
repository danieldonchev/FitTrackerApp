package com.daniel.FitTrackerApp.goal;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.daniel.FitTrackerApp.AppNetworkManager;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.provider.ProviderContract;

import java.util.ArrayList;
import java.util.Calendar;

public class GoalManager {
    private static final GoalManager ourInstance = new GoalManager();

    public ArrayList<Goal> goals;
    public StatsByTime stats;
    private Context context;

    public static GoalManager getInstance() {
        return ourInstance;
    }

    private GoalManager() {
    }

    public void load(Context context, boolean isAppStart){
        String userID = PreferencesHelper.getInstance().getCurrentUserId(context);
        int offset = Calendar.getInstance().getTimeZone().getRawOffset();
        stats = DBHelper.getInstance().getStats(context, userID);
        this.context = context;

        goals = DBHelper.getInstance().getGoals(context, userID);
        stats.daily.calories = calculateDailyCalories();
        for(Goal goal : goals){
            if(goal instanceof CustomGoal){
                ((CustomGoal) goal).setStats(DBHelper.getInstance().getStatsBetweenTime(context,
                        userID,
                        ((CustomGoal) goal).getFromDate().getTime() + offset,
                        ((CustomGoal) goal).getToDate().getTime() + offset));
            }
           calculateGoalProgress(goal, isAppStart);
        }
        refresh(context);
    }

    public int addGoal(Context context, Goal newGoal){
        for(Goal goal : goals){
            if(goal.equals(newGoal)){
                return -1;
            }
        }
        goals.add(newGoal);
        calculateGoalProgress(newGoal, false);
        newGoal.setLastModified(System.currentTimeMillis());
        DBHelper.getInstance().addGoal(context,
                                            PreferencesHelper.getInstance().getCurrentUserId(context),
                                            newGoal.toServerGoal(),
                                            0);
        AppNetworkManager.sendGoal(context, newGoal);
        return 0;
    }

    public void editGoal(Context context, Goal newGoal){
        for(Goal goal : goals){
            if(goal.equals(newGoal)){
                goal.setDistance(newGoal.getDistance());
                goal.setDuration(newGoal.getDuration());
                goal.setCalories(newGoal.getCalories());
                goal.setSteps(newGoal.getSteps());
                calculateGoalProgress(newGoal, false);

                DBHelper.getInstance().addGoal(context,
                        PreferencesHelper.getInstance().getCurrentUserId(context),
                        goal.toServerGoal(),
                        0);
                AppNetworkManager.sendGoalUpdate(context, newGoal);
                break;
            }
        }
    }

    public boolean deleteGoal(Context context, Goal newGoal){
        context.getContentResolver().delete(ProviderContract.GoalEntry.CONTENT_URI.buildUpon().appendPath("0").
                        appendPath(newGoal.getId().toString()).
                        appendPath(PreferencesHelper.getInstance().getCurrentUserId(context)).build(),
                null, null);
        AppNetworkManager.sendGoalDelete(context, newGoal.getId().toString());
        return goals.remove(newGoal);
    }

    private void calculateGoalProgress(Goal goal, boolean isAppStart){
        if(goal.getType() == Goal.DAILY){
            goal.calculateProgress(context, stats.daily.distance, stats.daily.duration, stats.daily.calories, stats.daily.steps, isAppStart);
        } else if(goal.getType() == Goal.WEEKLY){
            goal.calculateProgress(context, stats.weekly.distance, stats.weekly.duration, stats.weekly.calories, stats.weekly.steps, isAppStart);
        } else if(goal.getType() == Goal.MONTHLY) {
            goal.calculateProgress(context, stats.monthly.distance, stats.monthly.duration, stats.monthly.calories, stats.monthly.steps, isAppStart);
        } else if(goal.getType() == Goal.CUSTOM){
            ((CustomGoal)goal).calculateProgress();
        }
    }

    public void notifyChange(){
        for(Goal goal : goals){
            calculateGoalProgress(goal, false);
        }
    }

    public void notifyDataChange(){
        load(context, false);
    }

    public void addStats(double distance, long duration, long calories, long steps, long time){
        Calendar statsCalendar = Calendar.getInstance();
        statsCalendar.setTimeInMillis(time);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);


        if(statsCalendar.getTimeInMillis() > calendar.getTimeInMillis()){
            stats.daily.distance += distance;
            stats.daily.duration += duration;
            stats.daily.calories += calories;
            stats.daily.steps += steps;
            if(stats.daily.distance < 0){ stats.daily.distance = 0;}
            if(stats.daily.duration< 0){ stats.daily.duration = 0;}
            if(stats.daily.calories < 0){ stats.daily.calories = 0;}
            if(stats.daily.steps < 0){ stats.daily.steps = 0;}
        }

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
        {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        if(statsCalendar.getTimeInMillis() > calendar.getTimeInMillis()){
            stats.weekly.distance += distance;
            stats.weekly.duration += duration;
            stats.weekly.calories += calories;
            stats.weekly.steps += steps;
            if(stats.weekly.distance < 0){ stats.weekly.distance = 0;}
            if(stats.weekly.duration< 0){ stats.weekly.duration = 0;}
            if(stats.weekly.calories < 0){ stats.weekly.calories = 0;}
            if(stats.weekly.steps < 0){ stats.weekly.steps = 0;}
        }

        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        if(statsCalendar.getTimeInMillis() > calendar.getTimeInMillis()){
            stats.monthly.distance += distance;
            stats.monthly.duration += duration;
            stats.monthly.calories += calories;
            stats.monthly.steps += steps;
            if(stats.monthly.distance < 0){ stats.monthly.distance = 0;}
            if(stats.monthly.duration< 0){ stats.monthly.duration = 0;}
            if(stats.monthly.calories < 0){ stats.monthly.calories = 0;}
            if(stats.monthly.steps < 0){ stats.monthly.steps = 0;}
        }

        for(Goal goal : goals){
            if(goal instanceof CustomGoal){
                if(statsCalendar.getTimeInMillis() > ((CustomGoal) goal).getFromDate().getTime() &&
                        statsCalendar.getTimeInMillis() < ((CustomGoal) goal).getToDate().getTime()){
                    if (((CustomGoal) goal).getStats().endTime > System.currentTimeMillis()){
                        ((CustomGoal) goal).getStats().distance += distance;
                        ((CustomGoal) goal).getStats().duration += duration;
                        ((CustomGoal) goal).getStats().calories += calories;
                        ((CustomGoal) goal).getStats().steps += steps;
                        if(((CustomGoal) goal).getStats().distance < 0){ ((CustomGoal) goal).getStats().distance = 0;}
                        if(((CustomGoal) goal).getStats().duration < 0){ ((CustomGoal) goal).getStats().duration = 0;}
                        if(((CustomGoal) goal).getStats().calories < 0){ ((CustomGoal) goal).getStats().calories = 0;}
                        if(((CustomGoal) goal).getStats().steps < 0){ ((CustomGoal) goal).getStats().steps = 0;}
                    }
                }
            }
        }
        notifyChange();
    }

    public void setDailyResetAlarm(Context context){
        Intent intent = new Intent("NewDay");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 9011, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 60 * 1000, pendingIntent);
    }

    private void refresh(Context context){

        //Looper.prepare();
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                stats.daily.calories = calculateDailyCalories();
                notifyDataChange();
                handler.postDelayed(this, 60 * 30 * 1000);
            }
        };



        handler.postDelayed(runnable, 60 * 30 * 1000);
    }

    private int calculateDailyCalories(){
        Calendar calendar = Calendar.getInstance();

        long currentTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startDayTime = calendar.getTimeInMillis();

        long passedSeconds = (currentTime - startDayTime) / 1000;
        double bmr = com.daniel.FitTrackerApp.models.BMR.getBMRperSecond(PreferencesHelper.getInstance().getCurrentUserGender(context),
                PreferencesHelper.getInstance().getCurrentUserHeight(context),
                PreferencesHelper.getInstance().getCurrentUserWeight(context),
                PreferencesHelper.getInstance().getCurrentUserAge(context));
        int calories = (int) (bmr * passedSeconds);
        return (int) (calories + GoalManager.getInstance().stats.daily.calories);
    }
}
