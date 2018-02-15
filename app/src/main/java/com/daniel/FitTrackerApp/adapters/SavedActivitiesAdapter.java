package com.daniel.FitTrackerApp.adapters;


import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.sportactivity.SportActivity;
import com.daniel.FitTrackerApp.sportactivity.SportActivitySummary;
import com.daniel.FitTrackerApp.utils.UnitUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;

public class SavedActivitiesAdapter extends RecyclerView.Adapter<SavedActivitiesAdapter.ViewHolder> implements OnMapReadyCallback
{
    private ArrayList<SportActivitySummary> recordDataSummaries;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private FragmentManager childFragmentManager;
    private boolean isMetric;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView distance, duration, pace, avgSpeed, startTime, endTime, typeTextView, activityTextView;

        public ViewHolder(View v)
        {
            super(v);
            distance = (TextView) v.findViewById(R.id.distance);
            duration = (TextView) v.findViewById(R.id.duration);
            pace = (TextView) v.findViewById(R.id.pace);
            avgSpeed = (TextView) v.findViewById(R.id.avgSpeed);
            startTime = (TextView) v.findViewById(R.id.startTime);
            endTime = (TextView) v.findViewById(R.id.endTime);
            typeTextView = (TextView) v.findViewById(R.id.typeTextView);
            activityTextView = (TextView) v.findViewById(R.id.activityTextView);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        params.height = 300;

        mapFragment.getView().setLayoutParams(params);
    }

    public SavedActivitiesAdapter(Context context, ArrayList<SportActivitySummary> recordDataSummaries, FragmentManager childFragmentManager)
    {
        this.context = context;
        isMetric = PreferencesHelper.getInstance().isMetric(context);
        this.recordDataSummaries = recordDataSummaries;
        this.childFragmentManager = childFragmentManager;
    }

    public void setActivities(ArrayList<SportActivitySummary> recordDataSummaries){
        this.recordDataSummaries = recordDataSummaries;
    }

    @Override
    public SavedActivitiesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SavedActivitiesAdapter.ViewHolder holder, int position)
    {
        SportActivitySummary recordDataSummary = recordDataSummaries.get(position);
        holder.startTime.setText(recordDataSummary.getDateString(recordDataSummary.getStartTimeStamp()));
        holder.endTime.setText(recordDataSummary.getDateString(recordDataSummary.getEndTimeStamp()));
        holder.distance.setText(UnitUtils.getDistanceString(context, recordDataSummary.getDistance(), isMetric) + " " +
                (isMetric ? context.getString(R.string.km) : context.getString(R.string.miles)));
        holder.duration.setText(recordDataSummary.getDurationString());
        holder.pace.setText(recordDataSummary.getAveragePaceString());
        holder.avgSpeed.setText(recordDataSummary.getAverageSpeedString());
        if(recordDataSummary.getType() == SportActivity.RECORDED){
            holder.typeTextView.setText("Recorded");
        } else if(recordDataSummary.getType() == SportActivity.MANUAL_ADD){
            holder.typeTextView.setText("Manually added");
        } else if(recordDataSummary.getType() == SportActivity.TRACKED){
            holder.typeTextView.setText("Tracked");
        }
        holder.activityTextView.setText(recordDataSummary.getWorkout());

    }

    @Override
    public int getItemCount()
    {
        return recordDataSummaries.size();
    }

}
