package com.daniel.FitTrackerApp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.sportactivity.SportActivitySummary;
import com.daniel.FitTrackerApp.utils.UnitUtils;

import java.util.ArrayList;

public class SavedActivitiesByTimeAdapter extends RecyclerView.Adapter<SavedActivitiesByTimeAdapter.ViewHolder>
{
    private ArrayList<SportActivitySummary> recordDataSummaries;
    private boolean isMetric;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView distance, duration, pace, avgSpeed, startTime, endTime;

        public ViewHolder(View v)
        {
            super(v);
            distance = (TextView) v.findViewById(R.id.distance);
            duration = (TextView) v.findViewById(R.id.duration);
            pace = (TextView) v.findViewById(R.id.pace);
            avgSpeed = (TextView) v.findViewById(R.id.avgSpeed);
            startTime = (TextView) v.findViewById(R.id.startTime);
            endTime = (TextView) v.findViewById(R.id.endTime);
        }
    }

    public SavedActivitiesByTimeAdapter(Context context, ArrayList<SportActivitySummary> recordDataSummaries)
    {
        this.context = context;
        isMetric = PreferencesHelper.getInstance().isMetric(context);
        this.recordDataSummaries = recordDataSummaries;
    }

    public void setActivities(ArrayList<SportActivitySummary> recordDataSummaries){
        this.recordDataSummaries = recordDataSummaries;
    }

    @Override
    public SavedActivitiesByTimeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);

        return new SavedActivitiesByTimeAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SavedActivitiesByTimeAdapter.ViewHolder holder, int position)
    {
        SportActivitySummary recordDataSummary = recordDataSummaries.get(position);
        holder.startTime.setText(recordDataSummary.getDateString(recordDataSummary.getStartTimeStamp()));
        holder.endTime.setText(recordDataSummary.getDateString(recordDataSummary.getEndTimeStamp()));
        holder.distance.setText(UnitUtils.getDistanceString(context, recordDataSummary.getDistance(), isMetric));
        holder.duration.setText(recordDataSummary.getDurationString());
        holder.pace.setText(recordDataSummary.getAveragePaceString());
        holder.avgSpeed.setText(recordDataSummary.getAverageSpeedString());
    }

    @Override
    public int getItemCount()
    {
        return recordDataSummaries.size();
    }

}
