package com.daniel.FitTrackerApp.preferences;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.DatePicker;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.activities.MainActivity;
import com.daniel.FitTrackerApp.dialogs.NumberPicker;
import com.daniel.FitTrackerApp.dialogs.PickerSuccessListener;
import com.daniel.FitTrackerApp.dialogs.ScrollPicker;
import com.daniel.FitTrackerApp.goal.GoalManager;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.provider.ProviderContract;
import com.daniel.FitTrackerApp.utils.UnitUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ProfileSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final int METRIC_MAX_VALUE = 300;
    private static final int METRIC_MIN_VALUE = 1;
    public static final String IS_METRIC = "isMetric";

    private boolean isMetric;
    private int height;
    private double weight;
    private String[] imperialHeights;
    private Preference heightPreference, weightPreference, birthdayPreference;
    private JSONObject jsonSettingsChanged;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.profile_preferences);
        ((MainActivity)getActivity()).provideBackwardNavigation();

        isMetric = PreferencesHelper.getInstance().isMetric(getActivity());

        heightPreference = findPreference(getString(R.string.height_key));
        weightPreference = findPreference(getString(R.string.weight_key));
        birthdayPreference = findPreference(getString(R.string.birthday_key));

        height = Integer.valueOf(getPreferenceManager().getSharedPreferences().getInt(getString(R.string.height_key), 0));
        weight = Float.valueOf(getPreferenceManager().getSharedPreferences().getFloat(getString(R.string.weight_key), 0));

        setSummaries();
        jsonSettingsChanged = new JSONObject();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        ((MainActivity)getActivity()).sendChangedSettings(jsonSettingsChanged);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if(preference.getKey() == getString(R.string.height_key))
        {
            if(isMetric)
            {
                ScrollPicker scrollPicker = new ScrollPicker(getActivity(), heightListenerInt, METRIC_MIN_VALUE, METRIC_MAX_VALUE, height);
                scrollPicker.show();
            }
            else
            {
                int currentHeightIndex;
                String userHeight = UnitUtils.convertCMtoFTiNCH(height);
                imperialHeights = imperialHeights();
                for(currentHeightIndex = 0; currentHeightIndex < imperialHeights.length - 1; currentHeightIndex++)
                {
                    if(imperialHeights[currentHeightIndex].equals(userHeight))
                    {
                        break;
                    }
                }

                ScrollPicker scrollPicker = new ScrollPicker(getActivity(), heightListenerString, imperialHeights, currentHeightIndex, 0, 59);
                scrollPicker.show();
            }
        }
        else if(preference.getKey() == getString(R.string.weight_key))
        {
            int weightImperial = (int) UnitUtils.convertKGtoLBS(weight);
            if(isMetric)
            {
                NumberPicker weightPicker = new NumberPicker(getActivity(), weightListener, getString(R.string.kilograms), weight);
                weightPicker.show();
            }
            else
            {
                NumberPicker weightPicker = new NumberPicker(getActivity(), weightListener, getString(R.string.lbs), weightImperial);
                weightPicker.show();
            }
        }
        else if(preference.getKey() == getString(R.string.birthday_key))
        {
            Calendar calendar = new GregorianCalendar();
            DatePickerDialog datePicker = new DatePickerDialog(getActivity(), dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            calendar.setTimeInMillis(PreferencesHelper.getInstance().getBirthdayTimeInMillis(getActivity()));
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePicker.show();
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key == getString(R.string.gender_key)){
            try {
                jsonSettingsChanged.put(getString(R.string.gender_key), sharedPreferences.getString(key, null));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(key.length() < 12 || !key.substring(0, 12).equals("com.facebook")){
            DBHelper.getInstance().updateLastModifiedTime(getActivity(), PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                    ProviderContract.SyncEntry.LAST_MODIFIED_SETTINGS, System.currentTimeMillis());
            GoalManager.getInstance().notifyDataChange();
        }
    }

    PickerSuccessListener<Double> weightListener = new PickerSuccessListener<Double>() {
        @Override
        public void onSuccess(Double data) {
            PreferencesHelper.getInstance().setPreference(getString(R.string.weight_key), isMetric ? data : UnitUtils.convertLBStoKG(data));
            weight = data;
            setWeightSummary();

            try {
                jsonSettingsChanged.put(getString(R.string.weight_key), data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    PickerSuccessListener<Integer> heightListenerInt = new PickerSuccessListener<Integer>() {
        @Override
        public void onSuccess(Integer data) {
            PreferencesHelper.getInstance().setPreference(getString(R.string.height_key), data);
            height = data;
            setHeightSummary();

            try {
                jsonSettingsChanged.put(getString(R.string.height_key), data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    PickerSuccessListener<Integer> heightListenerString = new PickerSuccessListener<Integer>() {
        @Override
        public void onSuccess(Integer data) {
            String heightStr = imperialHeights[data];
            int heightCm = UnitUtils.convertFTiNCHtoCM(heightStr);
            PreferencesHelper.getInstance().setPreference(getString(R.string.height_key), heightCm);
            height = heightCm;
            setHeightSummary();

            try {
                jsonSettingsChanged.put(getString(R.string.height_key), data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
    {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.YEAR, year);
            PreferencesHelper.getInstance().setPreference(getString(R.string.birthday_key), calendar.getTimeInMillis());
            setBirthdaySummary();

            try {
                jsonSettingsChanged.put(getString(R.string.birthday_key), calendar.getTimeInMillis());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void sendUnitChangeBroadcast()
    {
        Intent intent = new Intent(IS_METRIC);
        intent.putExtra(IS_METRIC, PreferencesHelper.getInstance().isMetric(getActivity()));
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void setSummaries()
    {
        setHeightSummary();
        setWeightSummary();
        setBirthdaySummary();
    }

    private void setHeightSummary()
    {
        heightPreference.setSummary(isMetric ? String.valueOf(height) + " " + getString(R.string.centimeters): UnitUtils.convertCMtoFTiNCH(height));
    }

    private void setWeightSummary()
    {
        weightPreference.setSummary(isMetric ? UnitUtils.kgToString(weight) + " " + getString(R.string.kilograms) : UnitUtils.lbsToString(weight) + " " + getString(R.string.lbs));
    }

    private void setBirthdaySummary()
    {
        String birth = PreferencesHelper.getInstance().getBirthdayFormatted(getActivity());
        birthdayPreference.setSummary(PreferencesHelper.getInstance().getBirthdayFormatted(getActivity()));
    }

    public String[] imperialHeights()
    {
        String[] heights = new String[60];
        int heightCounter = 0;
        for(int feet = 3; feet < 8; feet++)
        {
            for(int inch = 0; inch < 12; inch++)
            {
                String currentHeight = String.valueOf(feet) + "'"+ String.valueOf(inch) + "\"";
                heights[heightCounter] = currentHeight;
                heightCounter++;
            }
        }
        return heights;
    }

}
