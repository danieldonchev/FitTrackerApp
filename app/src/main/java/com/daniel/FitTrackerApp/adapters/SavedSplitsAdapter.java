package com.daniel.FitTrackerApp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.sportactivity.Split;

import java.util.ArrayList;

public class SavedSplitsAdapter extends ArrayAdapter<Split>
{
    private ArrayList<Split> splits;
    private TextView distanceView, durationView, avgSpeedView, avgPaceView;

    public SavedSplitsAdapter(Context context, int textViewResourceId, ArrayList<Split> splits)
    {
        super(context, textViewResourceId, splits);
        this.splits = splits;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View v = convertView;

        if (v == null)
        {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.item_splits, parent, false);
        }

        Split split = splits.get(position);

        distanceView = (TextView) v.findViewById(R.id.distance);
        durationView = (TextView) v.findViewById(R.id.duration);
        avgSpeedView = (TextView) v.findViewById(R.id.avgSpeed);
        avgPaceView = (TextView) v.findViewById(R.id.pace);

        distanceView.setText(split.getDistanceString());
        durationView.setText(split.getDurationString());
        avgSpeedView.setText(split.getAverageSpeedString());
        avgPaceView.setText(split.getAveragePaceString());

        return v;
    }
}

