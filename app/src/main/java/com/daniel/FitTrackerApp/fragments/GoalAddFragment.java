package com.daniel.FitTrackerApp.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.daniel.FitTrackerApp.goal.CustomGoal;
import com.daniel.FitTrackerApp.goal.Goal;
import com.daniel.FitTrackerApp.goal.GoalManager;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.activities.MainActivity;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.utils.UnitUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class GoalAddFragment extends Fragment{

    private Spinner spinner;
    private ImageButton addButtonDistance, addButtonDuration, addButtonCalories, addButtonSteps;
    private TextView distanceTextView, durationTextView, caloriesTextView, stepsTextView, unitTextView, hrTextView, minTextView, secTextView;
    private EditText distanceEditText, caloriesEditText, stepsEditText, hrEditText, minEditText, secEditText, dateStartEditText, dateEndEditText;
    private Button saveButton;
    private Calendar startCal, endCal;
    private boolean isMetric, isDistance, isDuration, isCalories, isSteps;
    private Goal goal;
    private boolean isEditing;
    private Date startDate, endDate;
    private double distance;
    private long duration, calories, steps;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goals_add, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity)getActivity()).provideBackwardNavigation();
        isMetric = PreferencesHelper.getInstance().isMetric(getActivity());

        startCal = Calendar.getInstance();
        endCal = Calendar.getInstance();

        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);

        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MILLISECOND, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);

        spinner = (Spinner) view.findViewById(R.id.spinner2);

        distanceEditText = (EditText) view.findViewById(R.id.distanceEditText);
        caloriesEditText = (EditText) view.findViewById(R.id.caloriesEditText);
        stepsEditText = (EditText) view.findViewById(R.id.stepsEditText);
        hrEditText = (EditText) view.findViewById(R.id.hoursEditText);
        minEditText = (EditText) view.findViewById(R.id.minEditText);
        secEditText = (EditText) view.findViewById(R.id.secEditText);
        dateEndEditText = (EditText) view.findViewById(R.id.dateEndEditText);
        dateStartEditText = (EditText) view.findViewById(R.id.dateStartEditText);

        addButtonDistance = (ImageButton) view.findViewById(R.id.imageAddButton1);
        addButtonDuration = (ImageButton) view.findViewById(R.id.imageAddButton2);
        addButtonCalories = (ImageButton) view.findViewById(R.id.imageAddButton3);
        addButtonSteps = (ImageButton) view.findViewById(R.id.imageAddButton4);

        distanceTextView = (TextView) view.findViewById(R.id.distanceTextView);
        durationTextView = (TextView) view.findViewById(R.id.durationTextView);
        caloriesTextView = (TextView) view.findViewById(R.id.caloriesTextView);
        stepsTextView = (TextView) view.findViewById(R.id.stepsTextView);
        unitTextView = (TextView) view.findViewById(R.id.distanceUnitTextView);
        hrTextView = (TextView) view.findViewById(R.id.hrTextView);
        minTextView = (TextView) view.findViewById(R.id.minTextView);
        secTextView = (TextView) view.findViewById(R.id.secTextView);

        saveButton = (Button) view.findViewById(R.id.saveButton);

        unitTextView.setText(isMetric ? getString(R.string.km) : getString(R.string.miles));
        endCal.set(Calendar.DAY_OF_MONTH, endCal.get(Calendar.DAY_OF_MONTH) + 1);
        dateStartEditText.setText(new SimpleDateFormat("yyyy-MM-dd").format(startCal.getTime()));
        dateEndEditText.setText(new SimpleDateFormat("yyyy-MM-dd").format(endCal.getTime()));

        addButtonDistance.setOnClickListener(onClickListener);
        addButtonDuration.setOnClickListener(onClickListener);
        addButtonCalories.setOnClickListener(onClickListener);
        addButtonSteps.setOnClickListener(onClickListener);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinner.getSelectedItem().equals("Custom")){
                    dateStartEditText.setVisibility(View.VISIBLE);
                    dateEndEditText.setVisibility(View.VISIBLE);
                } else {
                    dateStartEditText.setVisibility(View.GONE);
                    dateEndEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dateStartEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = new GregorianCalendar();
                DatePickerDialog datePicker = new DatePickerDialog(getActivity(), startDateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                calendar.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
                datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
            }
        });

        dateEndEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = new GregorianCalendar();
                DatePickerDialog datePicker = new DatePickerDialog(getActivity(), endDateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                calendar.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
                datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) + 1);
                datePicker.show();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isDistance && !isDuration && !isCalories && !isSteps){
                    showError("Please choose a goal");
                } else if(spinner.getSelectedItem().equals("Custom") && (startCal.getTimeInMillis() > endCal.getTimeInMillis())){
                    showError("End date should not be before start date");
                } else {
                    if(isDistance){
                        if(!distanceEditText.getText().toString().equals("")){
                            distance = isMetric ? UnitUtils.convertKMinMeters(Double.valueOf(distanceEditText.getText().toString())) : UnitUtils.convertMilesInMeters(Double.valueOf(distanceEditText.getText().toString()));
                        }
                    } else{
                        distance = 0;
                    }
                    if(isDuration) {
                        if(!hrEditText.getText().toString().equals("")){
                            duration += Integer.parseInt(hrEditText.getText().toString()) * 3600;
                        }
                        if(!minEditText.getText().toString().equals("")){
                            duration += Integer.parseInt(minEditText.getText().toString()) * 60;
                        }
                        if(!secEditText.getText().toString().equals("")){
                            duration += Integer.parseInt(secEditText.getText().toString());
                        }
                    } else{
                        duration = 0;
                    }
                    if(isCalories){
                        if(!caloriesEditText.getText().toString().equals("")){
                            calories = Long.valueOf(caloriesEditText.getText().toString());
                        }
                    } else {
                        calories = 0;
                    }
                    if (isSteps) {
                        if(!stepsEditText.getText().toString().equals("")){
                            steps = Long.valueOf(stepsEditText.getText().toString());
                        }
                    } else {
                        steps = 0;
                    }
                }
                if((distance == 0 && isDistance) || (duration == 0 && isDuration) ||
                        (calories == 0 && isCalories) || (steps == 0 && isSteps)) {
                    showError("Please choose a goal");
                } else {
                    if(!isEditing){
                        if(spinner.getSelectedItemPosition() == Goal.CUSTOM){
                            goal = new CustomGoal(distance,
                                    duration,
                                    calories,
                                    steps,
                                    startDate,
                                    endDate);
                        } else {
                            goal = new Goal(spinner.getSelectedItemPosition(),
                                    distance,
                                    duration,
                                    calories,
                                    steps);
                        }
                        int result = GoalManager.getInstance().addGoal(getActivity(), goal);
                        if(result == -1){
                            showError("Similar goal already exists");
                        } else {
                            goalSavedMessage();
                        }
                    } else {
                        goal.setDistance(distance);
                        goal.setDuration(duration);
                        goal.setCalories(calories);
                        goal.setSteps(steps);
                        GoalManager.getInstance().editGoal(getActivity(), goal);
                        goalSavedMessage();
                    }
                }
            }
        });


        if(goal != null){
            spinner.setSelection(goal.getType(), true);
            if(goal.getDistance() > 0){
                enableDistanceGoal();
                distanceEditText.setText(String.valueOf(isMetric ? UnitUtils.convertMetersToUnit(goal.getDistance(), getString(R.string.km)) :
                                                                    UnitUtils.convertMetersToUnit(goal.getDistance(), getString(R.string.miles))));
            }
            if(goal.getDuration() > 0){
                enableDurationGoal();
                long durationHour = goal.getDuration() / 3600;
                long durationMinute = (goal.getDuration() - durationHour * 3600) / 60;
                long durationSeconds = (goal.getDuration() - durationHour * 3600 - durationMinute * 60);

                hrEditText.setText(String.valueOf(durationHour));
                minEditText.setText(String.valueOf(durationMinute));
                secEditText.setText(String.valueOf(durationSeconds));
            }
            if(goal.getCalories() > 0) {
                enableCaloriesGoal();
                caloriesEditText.setText(String.valueOf(goal.getCalories()));
            }
            if(goal.getSteps() > 0) {
                enableStepsGoal();
                stepsEditText.setText(String.valueOf(goal.getSteps()));
            }
        }
    }

    public void setGoal(Goal goal){
        this.goal = goal;
        isEditing = true;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imageAddButton1:{
                    if(!isDistance){
                        enableDistanceGoal();
                    } else {
                        disableDistanceGoal();
                    }
                    break;
                }
                case R.id.imageAddButton2:{
                    if(!isDuration){
                        enableDurationGoal();
                    } else {
                        disableDurationGoal();
                    }
                    break;
                }
                case R.id.imageAddButton3:{
                    if(!isCalories){
                        enableCaloriesGoal();
                    } else {
                        disableCaloriesGoal();
                    }

                    break;
                }
                case R.id.imageAddButton4:{
                    if(!isSteps){
                      enableStepsGoal();
                    } else {
                        disableStepsGoal();
                    }

                    break;
                }
            }
        }
    };

    DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener()
    {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            startCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            startCal.set(Calendar.MONTH, monthOfYear);
            startCal.set(Calendar.YEAR, year);
            startDate = startCal.getTime();
            dateStartEditText.setText(new SimpleDateFormat("yyyy-MM-dd").format(startCal.getTime()));
        }
    };

    DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener()
    {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            endCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            endCal.set(Calendar.MONTH, monthOfYear);
            endCal.set(Calendar.YEAR, year);
            endDate = endCal.getTime();
            dateEndEditText.setText(new SimpleDateFormat("yyyy-MM-dd").format(endCal.getTime()));
        }
    };

    public void showError(String msg){
        new AlertDialog.Builder(getActivity())
                .setMessage(msg)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void enableDistanceGoal(){
        distanceEditText.setEnabled(true);
        distanceTextView.setEnabled(true);
        unitTextView.setEnabled(true);
        addButtonDistance.setImageResource(android.R.drawable.ic_menu_delete);
        isDistance = true;
    }

    private void disableDistanceGoal(){
        distanceEditText.setEnabled(false);
        distanceTextView.setEnabled(false);
        unitTextView.setEnabled(false);
        addButtonDistance.setImageResource(android.R.drawable.ic_menu_add);
        isDistance = false;
    }

    private void enableDurationGoal(){
        durationTextView.setEnabled(true);
        hrEditText.setEnabled(true);
        minEditText.setEnabled(true);
        secEditText.setEnabled(true);
        hrTextView.setEnabled(true);
        minTextView.setEnabled(true);
        secTextView.setEnabled(true);
        addButtonDuration.setImageResource(android.R.drawable.ic_menu_delete);
        isDuration = true;
    }

    private void disableDurationGoal(){
        durationTextView.setEnabled(false);
        hrEditText.setEnabled(false);
        minEditText.setEnabled(false);
        secEditText.setEnabled(false);
        hrTextView.setEnabled(false);
        minTextView.setEnabled(false);
        secTextView.setEnabled(false);
        addButtonDuration.setImageResource(android.R.drawable.ic_menu_add);
        isDuration = false;
    }

    private void enableCaloriesGoal(){
        caloriesTextView.setEnabled(true);
        caloriesEditText.setEnabled(true);
        addButtonCalories.setImageResource(android.R.drawable.ic_menu_delete);
        isCalories = true;
    }

    private void disableCaloriesGoal(){
        caloriesTextView.setEnabled(false);
        caloriesEditText.setEnabled(false);
        addButtonCalories.setImageResource(android.R.drawable.ic_menu_add);
        isCalories = false;
    }

    private void enableStepsGoal(){
        stepsTextView.setEnabled(true);
        stepsEditText.setEnabled(true);
        addButtonSteps.setImageResource(android.R.drawable.ic_menu_delete);
        isSteps = true;
    }

    private void disableStepsGoal(){
        stepsTextView.setEnabled(false);
        stepsEditText.setEnabled(false);
        addButtonSteps.setImageResource(android.R.drawable.ic_menu_add);
        isSteps = false;
    }

    private void goalSavedMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Success")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setMessage("Goal saved!");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ((MainActivity) getActivity()).setNavDrawerToggleOn();
                GoalAddFragment.this.getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        builder.show();
    }
}
