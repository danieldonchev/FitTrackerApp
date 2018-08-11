package com.daniel.FitTrackerApp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.daniel.FitTrackerApp.AppNetworkManager;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.dialogs.NumberPicker;
import com.daniel.FitTrackerApp.dialogs.PickerSuccessListener;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.utils.UnitUtils;
import com.tracker.shared.Entities.WeightWeb;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class WeightLogFragment extends Fragment {

    private ImageButton nextDateButton, previousDateButton;
    private TextView dateTextView, weightTextView, unitTextView, dailyChangeTextView, weeklyChangeTextView, monthlyChangeTextView;
    private WeightWeb weight;
    private Calendar calendar, changeCalendar;
    private String userID;
    private SimpleDateFormat simpleDateFormat;
    private boolean isMetric;
    private double dailyChange, weeklyChange, monthlyChange;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weight_log, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isMetric = PreferencesHelper.getInstance().isMetric(getActivity());
        userID = PreferencesHelper.getInstance().getCurrentUserId(getActivity());

        calendar = Calendar.getInstance();
        changeCalendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        changeCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        changeCalendar.set(Calendar.HOUR_OF_DAY, 0);
        changeCalendar.set(Calendar.MINUTE, 0);
        changeCalendar.set(Calendar.SECOND, 0);
        changeCalendar.set(Calendar.MILLISECOND, 0);

        dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        weightTextView = (TextView) view.findViewById(R.id.weightTextView);
        unitTextView = (TextView) view.findViewById(R.id.unitTextView);
        dailyChangeTextView = (TextView) view.findViewById(R.id.dailyChangeTextView);
        weeklyChangeTextView = (TextView) view.findViewById(R.id.weeklyChangeTextView);
        monthlyChangeTextView = (TextView) view.findViewById(R.id.monthlyChangeTextView);
        nextDateButton = (ImageButton) view.findViewById(R.id.imageButtonUp);
        previousDateButton = (ImageButton) view.findViewById(R.id.imageButtonDown);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        dateTextView.setText(simpleDateFormat.format(calendar.getTime()));
        weight = DBHelper.getInstance().getWeightByDate(getActivity(), userID, calendar.getTimeInMillis());

        if(weight.weight > 0){
            weightTextView.setText(String.valueOf(weight.weight)  + (isMetric ? getString(R.string.kilograms) : getString(R.string.lbs)));
            calculateChanges();
        }

        weightTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMetric) {
                    NumberPicker weightPicker = new NumberPicker(getActivity(), weightListener, getString(R.string.kilograms), weight.weight);
                    weightPicker.show();
                } else {
                    double weightImperial = UnitUtils.convertKGtoLBS(weight.weight);
                    NumberPicker weightPicker = new NumberPicker(getActivity(), weightListener, getString(R.string.lbs), weightImperial);
                    weightPicker.show();
                }
            }
        });

        previousDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                dateTextView.setText(simpleDateFormat.format(calendar.getTime()));
                weight = DBHelper.getInstance().getWeightByDate(getActivity(), userID, calendar.getTimeInMillis());
                if(weight.weight > 0){
                    weightTextView.setText(String.valueOf(weight.weight)  + (isMetric ? getString(R.string.kilograms) : getString(R.string.lbs)));
                } else {
                    weightTextView.setText("Enter Weight");
                }
                calculateChanges();
            }
        });

        nextDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                dateTextView.setText(simpleDateFormat.format(calendar.getTime()));
                weight = DBHelper.getInstance().getWeightByDate(getActivity(), userID, calendar.getTimeInMillis());
                if(weight.weight > 0){
                    weightTextView.setText(String.valueOf(weight.weight)  + (isMetric ? getString(R.string.kilograms) : getString(R.string.lbs)));
                } else {
                    weightTextView.setText("Enter Weight");
                }
                calculateChanges();
            }
        });
    }

    PickerSuccessListener<Double> weightListener = new PickerSuccessListener<Double>() {
        @Override
        public void onSuccess(Double data) {
            weightTextView.setText(String.valueOf(data));
            weight.weight = data;
            weight.date = calendar.getTimeInMillis();
            weight.lastModified = System.currentTimeMillis();
            DBHelper.getInstance().addWeight(getActivity(),
                                            PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                                            weight,
                                            0);
            AppNetworkManager.sendWeight(getActivity(), weight);
        }
    };

    private void calculateChanges(){
        changeCalendar.setTimeInMillis(calendar.getTimeInMillis());
        changeCalendar.set(Calendar.MONTH, changeCalendar.get(Calendar.MONTH) - 1);
        WeightWeb previousMonth = DBHelper.getInstance().getWeightByDate(getContext(), userID, changeCalendar.getTimeInMillis());
        changeCalendar.set(Calendar.MONTH, changeCalendar.get(Calendar.MONTH) + 1);
        changeCalendar.add(Calendar.DAY_OF_YEAR, - 1);
        WeightWeb previousDay = DBHelper.getInstance().getWeightByDate(getContext(), userID, changeCalendar.getTimeInMillis());
        changeCalendar.add(Calendar.DAY_OF_YEAR, -6);
        WeightWeb previousWeek = DBHelper.getInstance().getWeightByDate(getContext(), userID, changeCalendar.getTimeInMillis());

        if(previousDay.weight == 0){
            dailyChangeTextView.setText("-");
        } else {
            dailyChange = weight.weight - previousDay.weight;
            setTextViewChange(dailyChange, dailyChangeTextView);
        }
        if(previousWeek.weight == 0){
            weeklyChangeTextView.setText("-");
        } else {
            weeklyChange = weight.weight - previousWeek.weight;
            setTextViewChange(weeklyChange, weeklyChangeTextView);
        }
        if(previousMonth.weight == 0){
            monthlyChangeTextView.setText("-");
        } else {
            monthlyChange = weight.weight - previousMonth.weight;
            setTextViewChange(monthlyChange, monthlyChangeTextView);
        }
    }

    private void setTextViewChange(double change, TextView view){
        if(change == 0){
            view.setText("0.00" + (isMetric ? getString(R.string.kilograms) : getString(R.string.lbs)));
        } else if(change > 0){
            view.setText("+ " + String.valueOf(change) + (isMetric ? getString(R.string.kilograms) : getString(R.string.lbs)));
        } else if(change < 0){
            view.setText(String.valueOf(change) + (isMetric ? getString(R.string.kilograms) : getString(R.string.lbs)));
        }
    }
}
