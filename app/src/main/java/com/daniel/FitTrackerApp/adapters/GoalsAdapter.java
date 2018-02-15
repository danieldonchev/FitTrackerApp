package com.daniel.FitTrackerApp.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daniel.FitTrackerApp.goal.CustomGoal;
import com.daniel.FitTrackerApp.goal.Goal;
import com.daniel.FitTrackerApp.goal.GoalManager;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.activities.MainActivity;
import com.daniel.FitTrackerApp.fragments.GoalAddFragment;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.UnitUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.ViewHolder>{

    private ArrayList<Goal> goals;
    private boolean isMetric;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView distanceTextValue, durationTextValue, caloriesTextValue, stepsTextValue;
        public TextView distanceTextView, durationTextView, caloriesTextView, stepsTextView, distanceUnitTextView, headerText, progressBarPercentageView;
        public ImageButton editButton, deleteButton;
        public ProgressBar progressBar;

        public ViewHolder(View v)
        {
            super(v);
            distanceTextValue = (TextView) v.findViewById(R.id.distanceTextValue);
            durationTextValue = (TextView) v.findViewById(R.id.durationTextValue);
            caloriesTextValue = (TextView) v.findViewById(R.id.caloriesTextValue);
            stepsTextValue = (TextView) v.findViewById(R.id.stepsTextValue);
            distanceTextView = (TextView) v.findViewById(R.id.distanceTextView);
            durationTextView = (TextView) v.findViewById(R.id.durationTextView);
            caloriesTextView = (TextView) v.findViewById(R.id.caloriesTextView);
            stepsTextView = (TextView) v.findViewById(R.id.stepsTextView);
            distanceUnitTextView = (TextView) v.findViewById(R.id.distanceUnitTextView);
            headerText = (TextView) v.findViewById(R.id.headerText);
            editButton = (ImageButton) v.findViewById(R.id.edit_button);
            deleteButton = (ImageButton) v.findViewById(R.id.delete_button);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
            progressBarPercentageView = (TextView) v.findViewById(R.id.progress_bar_percent_text);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GoalAddFragment fragment = new GoalAddFragment();
                    fragment.setGoal(goals.get(ViewHolder.this.getAdapterPosition()));
                    ((MainActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean del = GoalManager.getInstance().deleteGoal(context, goals.get(ViewHolder.this.getAdapterPosition()));

                    GoalsAdapter.this.notifyDataSetChanged();
                    int b = 5;
                }
            });
        }
    }

    public GoalsAdapter(Context context, ArrayList<Goal> goals)
    {
        this.context = context;
        this.goals = goals;
        isMetric = PreferencesHelper.getInstance().isMetric(context);
    }

    @Override
    public GoalsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goals, parent, false);

        return new GoalsAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GoalsAdapter.ViewHolder holder, int position)
    {
        Goal goal = goals.get(position);
        if(goal.getType() == Goal.DAILY){
            holder.headerText.setText("Daily");
        } else if(goal.getType() == Goal.WEEKLY) {
            holder.headerText.setText("Weekly");
        } else if(goal.getType() == Goal.MONTHLY) {
            holder.headerText.setText("Monthly");
        } else if(goal.getType() == Goal.CUSTOM){
            holder.headerText.setText(new SimpleDateFormat("yyyy-MM-dd").format(((CustomGoal)goal).getFromDate().getTime()) + " - " + new SimpleDateFormat("yyyy-MM-dd").format(((CustomGoal)goal).getToDate().getTime()));
        }
        if(goal.getDistance() > 0){
            holder.distanceTextValue.setText(isMetric ? String.valueOf(UnitUtils.convertMetersToUnit(goal.getDistance(), "km")) : String.valueOf(UnitUtils.convertMetersToUnit(goal.getDistance(), "miles")));
            holder.distanceUnitTextView.setText(isMetric ? "km" : "miles");
        } else {
            holder.distanceTextValue.setVisibility(View.GONE);
            holder.distanceUnitTextView.setVisibility(View.GONE);
            holder.distanceTextView.setVisibility(View.GONE);
        }

        if(goal.getDuration() > 0) {
            holder.durationTextValue.setText(AppUtils.convertSecondsToString(goal.getDuration()));
        } else {
            holder.durationTextValue.setVisibility(View.GONE);
            holder.durationTextView.setVisibility(View.GONE);
        }

        if(goal.getCalories() > 0) {
            holder.caloriesTextValue.setText(String.valueOf(goal.getCalories()));
        } else {
            holder.caloriesTextValue.setVisibility(View.GONE);
            holder.caloriesTextView.setVisibility(View.GONE);
        }

        if(goal.getSteps() > 0) {
            holder.stepsTextValue.setText(String.valueOf(goal.getSteps()));
        } else {
            holder.stepsTextValue.setVisibility(View.GONE);
            holder.stepsTextView.setVisibility(View.GONE);
        }
        holder.progressBar.setProgress(goal.getProgress());
        holder.progressBarPercentageView.setText(String.valueOf(goal.getProgress()) + "%");
    }

    @Override
    public int getItemCount()
    {
        return goals.size();
    }
}
