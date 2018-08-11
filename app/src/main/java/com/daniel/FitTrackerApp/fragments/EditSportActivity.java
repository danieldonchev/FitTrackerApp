package com.daniel.FitTrackerApp.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.daniel.FitTrackerApp.AppNetworkManager;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.activities.MainActivity;
import com.daniel.FitTrackerApp.dialogs.ConflictActivitiesDialog;
import com.daniel.FitTrackerApp.dialogs.ConflictDialogCallback;
import com.daniel.FitTrackerApp.goal.GoalManager;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.models.ActivityCalories;
import com.daniel.FitTrackerApp.models.BMR;
import com.daniel.FitTrackerApp.provider.ProviderContract;
import com.daniel.FitTrackerApp.sportactivity.SportActivity;
import com.daniel.FitTrackerApp.sportactivity.SportActivitySummary;
import com.daniel.FitTrackerApp.utils.UnitUtils;
import com.tracker.shared.Entities.SportActivityWeb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;


public class EditSportActivity extends Fragment implements ConflictDialogCallback{

    public static final int ANSWER_OK = 1;
    public static final int ANSWER_CANCEL = 2;

    private Button saveButton;
    private EditText hrEditText, minEditText, secEditText, stepsEditText, distanceEditText, calendarEditText, timeEditText;
    private TextView unitTextView;
    private Spinner spinner;
    private Calendar activityCalendar;
    private SportActivityWeb sportActivity;
    private boolean isEditing, isMetric;
    private double distanceChange;
    private long durationChange, caloriesChange;
    private long stepsChange;
    private ArrayList<SportActivitySummary> summaries;

    private long startTimestamp = 0;
    private long endTimestamp = 0;
    private long duration = 0;
    private double distance = 0.0d;
    private long steps = 0;
    private int calories = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_sport_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).provideBackwardNavigation();
        isMetric = PreferencesHelper.getInstance().isMetric(getActivity());

        saveButton = (Button) view.findViewById(R.id.saveButton);
        hrEditText = (EditText) view.findViewById(R.id.hourEditText);
        minEditText = (EditText) view.findViewById(R.id.minEditText);
        secEditText = (EditText) view.findViewById(R.id.secEditText);
        stepsEditText = (EditText) view.findViewById(R.id.stepsEditText);
        distanceEditText = (EditText) view.findViewById(R.id.distanceEditText);
        calendarEditText = (EditText) view.findViewById(R.id.calendarEditText);
        timeEditText = (EditText) view.findViewById(R.id.timeEditText);
        unitTextView = (TextView) view.findViewById(R.id.distanceUnitTextView);
        spinner = (Spinner) view.findViewById(R.id.activitySpinner);

        String[] activities = {"Running", "Walking", "Cycling"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, activities);
        spinner.setAdapter(adapter);

        setView(sportActivity, isEditing);

        if(!isEditing){
            activityCalendar = Calendar.getInstance();
            activityCalendar.set(Calendar.SECOND, 0);
            setTimeText(activityCalendar);
        }

        unitTextView.setText(isMetric ? getString(R.string.km) : getString(R.string.miles));
        calendarEditText.setText(new SimpleDateFormat("yyyy-MM-dd").format(activityCalendar.getTime()));


        calendarEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = new GregorianCalendar();
                DatePickerDialog datePicker = new DatePickerDialog(getActivity(), dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                calendar.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
                datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
            }
        });

        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis(Calendar.getInstance().getTimeInMillis());

                TimePickerDialog timePicker = new TimePickerDialog(getActivity(), timeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE), false);
                timePicker.updateTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                timePicker.show();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sportActivity == null){
                    sportActivity = new SportActivityWeb(UUID.randomUUID().toString());
                    sportActivity.setWorkout(spinner.getSelectedItem().toString());
                } else {
                    distance = sportActivity.getDistance();
                    steps = sportActivity.getSteps();
                }

                if(!hrEditText.getText().toString().equals("")){
                    duration += Integer.parseInt(hrEditText.getText().toString()) * 3600;
                }
                if(!minEditText.getText().toString().equals("")){
                    duration += Integer.parseInt(minEditText.getText().toString()) * 60;
                }
                if(!secEditText.getText().toString().equals("")){
                    duration += Integer.parseInt(secEditText.getText().toString());
                }

                if(!stepsEditText.getText().toString().equals("") && Float.parseFloat(stepsEditText.getText().toString()) != steps){
                    steps = Integer.parseInt(stepsEditText.getText().toString());
                }
                if(!distanceEditText.getText().toString().equals("") && Double.parseDouble(distanceEditText.getText().toString()) != distance){
                    distance = Double.parseDouble(distanceEditText.getText().toString());
                }
                if(isEditing){
                    durationChange = duration - sportActivity.getDuration();
                    distanceChange = distance - sportActivity.getDistance();
                    stepsChange = steps - sportActivity.getSteps();
                    calories = sportActivity.getCalories();
                } else {
                    durationChange = duration;
                    distanceChange = distance;
                    stepsChange = steps;
                }

                if(activityCalendar.getTimeInMillis()  + duration * 1000 > Calendar.getInstance().getTimeInMillis())
                {
                    AlertDialog.Builder builder = getAlertDialog();
                    builder.setMessage("Can't add future activities");
                    builder.show();
                } else if(duration <= 1) {
                    AlertDialog.Builder builder = getAlertDialog();
                    builder.setMessage("Duration must be higher than 1 second");
                    builder.show();
                } else{

                    startTimestamp = activityCalendar.getTimeInMillis();
                    endTimestamp = startTimestamp + duration * 1000;
                    summaries = DBHelper.getInstance().filteredActivities(getActivity(),
                                                                            PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                                                                            sportActivity.getId().toString(),
                                                                            startTimestamp,
                                                                            endTimestamp);
                    if(summaries.size() > 0){
                        Dialog dialog = new ConflictActivitiesDialog(getActivity(), summaries, EditSportActivity.this);
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                       saveSportActivity();
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        isEditing = false;
        if(sportActivity != null){
            sportActivity.setDistance(Double.parseDouble(distanceEditText.getText().toString()));
        }

    }

    public void setSportActivity(SportActivityWeb sportActivity)
    {
        this.sportActivity = sportActivity;
        isEditing = true;
    }

    public void setView(SportActivityWeb sportActivity, boolean isEditing)
    {
        if(isEditing){
            long durationHour = sportActivity.getDuration() / 3600;
            long durationMinute = (sportActivity.getDuration() - durationHour * 3600) / 60;
            long durationSeconds = (sportActivity.getDuration() - durationHour * 3600 - durationMinute * 60);

            distanceEditText.setText(UnitUtils.getDistanceString(getActivity(), sportActivity.getDistance(), isMetric));
            hrEditText.setText(String.valueOf(durationHour));
            minEditText.setText(String.valueOf(durationMinute));
            secEditText.setText(String.valueOf(durationSeconds));

            activityCalendar = Calendar.getInstance();
            activityCalendar.setTimeInMillis(sportActivity.getStartTimestamp());

            calendarEditText.setText(new SimpleDateFormat("yyyy-MM-dd").format(activityCalendar.getTime()));
            setTimeText(activityCalendar);
        }
    }

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
    {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            activityCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            activityCalendar.set(Calendar.MONTH, monthOfYear);
            activityCalendar.set(Calendar.YEAR, year);

            calendarEditText.setText(new SimpleDateFormat("yyyy-MM-dd").format(activityCalendar.getTime()));
        }
    };

    TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            activityCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            activityCalendar.set(Calendar.MINUTE, minute);
            setTimeText(activityCalendar);
        }
    };

    private AlertDialog.Builder getAlertDialog()
    {
        return new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    private void setTimeText(Calendar calendar)
    {
        String hour = "";
        String minutes = "";
        if(calendar.get(Calendar.HOUR_OF_DAY) < 10){
            hour += "0" + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        } else {
            hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        }
        if(calendar.get(Calendar.MINUTE) < 10){
            minutes += "0" + String.valueOf(calendar.get(Calendar.MINUTE));
        } else {
            minutes = String.valueOf(calendar.get(Calendar.MINUTE));
        }
        timeEditText.setText(hour + ":" + minutes);
    }

    @Override
    public void onClick(int answer) {
        if(answer == ANSWER_OK){
            for(SportActivitySummary activitySummary : summaries){
                getActivity().getContentResolver().delete(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath("0")
                .appendPath(activitySummary.getId().toString())
                .appendPath(PreferencesHelper.getInstance().getCurrentUserId(getActivity())).build(),
                        null, null);
                GoalManager.getInstance().addStats(-activitySummary.getDistance(),
                                        -activitySummary.getDuration(),
                        -activitySummary.getCalories(),
                        -activitySummary.getSteps(),
                        -activitySummary.getStartTimeStamp());
                AppNetworkManager.sendSportActivityDelete(getActivity(), activitySummary.getId().toString());

            }
            saveSportActivity();
        } else if(answer == ANSWER_CANCEL){

        }
    }

    private void saveSportActivity(){
        sportActivity.setWorkout(spinner.getSelectedItem().toString());
        sportActivity.setEndTimestamp(endTimestamp);
        sportActivity.setStartTimestamp(startTimestamp);
        sportActivity.setDuration(duration);
        sportActivity.setDistance(isMetric ? UnitUtils.convertKMinMeters(distance) : UnitUtils.convertMilesInMeters(distance));
        sportActivity.setSteps(steps);

        sportActivity.setLastModified(System.currentTimeMillis());
        if(!isEditing){
            //sportActivity.setType(SportActivity.MANUAL_ADD);
            caloriesChange = sportActivity.getCalories();
            double avgSpeed = 0;
            if(isMetric){
                avgSpeed = distance * 1000 / duration * 3.6;
            } else {
                avgSpeed = distance * 1609.344 / duration * 2.23693629;
            }
            Double caloriesBurned = BMR.getBMRperHour(PreferencesHelper.getInstance().getCurrentUserGender(getContext()),
                    PreferencesHelper.getInstance().getCurrentUserHeight(getContext()),
                    PreferencesHelper.getInstance().getCurrentUserWeight(getContext()),
                    PreferencesHelper.getInstance().getCurrentUserAge(getContext()))
                    * ActivityCalories.getCurrentMET(getContext(), isMetric, ((Double)avgSpeed).floatValue(),
                    sportActivity.getWorkout()) * duration / 3600 + duration * BMR.getBMRperSecond(PreferencesHelper.getInstance().getCurrentUserGender(getContext()),
                    PreferencesHelper.getInstance().getCurrentUserHeight(getContext()),
                    PreferencesHelper.getInstance().getCurrentUserWeight(getContext()),
                    PreferencesHelper.getInstance().getCurrentUserAge(getContext()));

            calories = caloriesBurned.intValue();
            sportActivity.setCalories(calories);
            caloriesChange = calories;
            DBHelper.getInstance().addActivity(sportActivity, PreferencesHelper.getInstance().getCurrentUserId(getActivity()), getActivity(), 0, SportActivity.MANUAL_ADD);
            AppNetworkManager.sendSportActivity(getActivity(), sportActivity);
            isEditing = true;
        } else {
            caloriesChange = sportActivity.getCalories() - calories;
            DBHelper.getInstance().updateSportActivity(getActivity(), PreferencesHelper.getInstance().getCurrentUserId(getActivity()), sportActivity, 0);
            AppNetworkManager.sendSportActivityUpdate(getActivity(), sportActivity);
        }
        GoalManager.getInstance().addStats(isMetric ? UnitUtils.convertKMinMeters(distanceChange) : UnitUtils.convertMilesInMeters(distanceChange),
                durationChange, caloriesChange, steps, sportActivity.getStartTimestamp());

        AlertDialog.Builder builder = getAlertDialog();
        builder.setTitle("Success");
        builder.setMessage("Activity saved!");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                EditSportActivity.this.getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        builder.show();
        ((MainActivity) getActivity()).setNavDrawerToggleOn();
    }
}
