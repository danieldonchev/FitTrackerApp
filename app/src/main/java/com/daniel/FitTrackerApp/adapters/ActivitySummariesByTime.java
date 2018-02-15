package com.daniel.FitTrackerApp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.sportactivity.SportActivitySummariesByTime;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.UnitUtils;

import java.util.ArrayList;

public class ActivitySummariesByTime extends RecyclerView.Adapter<ActivitySummariesByTime.ViewHolder> {
    private ArrayList<SportActivitySummariesByTime> activitySummaries;
    private Context context;
    private boolean isMetric;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView distance, duration, calories, steps, dateRange;

        public ViewHolder(View v) {
            super(v);
            distance = (TextView) v.findViewById(R.id.distanceView);
            duration = (TextView) v.findViewById(R.id.durationView);
            calories = (TextView) v.findViewById(R.id.caloriesView);
            steps = (TextView) v.findViewById(R.id.stepsView);
            dateRange = (TextView) v.findViewById(R.id.dateRangeText);
        }
    }

    public ActivitySummariesByTime(Context context, ArrayList<SportActivitySummariesByTime> recordDataSummaries) {
        this.activitySummaries = recordDataSummaries;
        this.context = context;
        isMetric = PreferencesHelper.getInstance().isMetric(context);
    }

    public void setActivities(ArrayList<SportActivitySummariesByTime> recordDataSummaries) {
        this.activitySummaries = recordDataSummaries;
    }

    @Override
    public ActivitySummariesByTime.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_summaries_time, parent, false);

        return new ActivitySummariesByTime.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ActivitySummariesByTime.ViewHolder holder, int position) {

        SportActivitySummariesByTime activity = activitySummaries.get(position);
        holder.distance.setText(UnitUtils.getDistanceString(context, activity.getDistance(), isMetric));
        holder.duration.setText(AppUtils.convertSecondsToString(activity.getDuration()));
        holder.calories.setText(String.valueOf(activity.getCalories()));
        holder.steps.setText(String.valueOf(activity.getSteps()));
        holder.dateRange.setText(activity.getDateRange());
    }

    @Override
    public int getItemCount() {
        return activitySummaries.size();
    }
}