package com.daniel.FitTrackerApp.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.sportactivity.Split;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.UnitUtils;

import java.util.ArrayList;

public class CurrentSplitsAdapter extends RecyclerView.Adapter<CurrentSplitsAdapter.ViewHolder>
{
    private ArrayList<Split> data;

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView distance, duration, pace, avgSpeed;

        public ViewHolder(View v)
        {
            super(v);
            distance = (TextView) v.findViewById(R.id.distance);
            duration = (TextView) v.findViewById(R.id.duration);
            pace = (TextView) v.findViewById(R.id.pace);
            avgSpeed = (TextView) v.findViewById(R.id.avgSpeed);
        }
    }

    public CurrentSplitsAdapter(ArrayList<Split> data)
    {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_splits, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {

        Split split = data.get(position);
        holder.distance.setText(split.isMetric() ? AppUtils.doubleToString(UnitUtils.convertMetersToUnit(split.getDistance(), "km")) + " km" :
                AppUtils.doubleToString(UnitUtils.convertMetersToUnit(split.getDistance(), "miles")) + " mi");
        holder.duration.setText(split.getDurationString());
        holder.pace.setText(split.getAveragePaceString());
        holder.avgSpeed.setText(split.getAverageSpeedString());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
