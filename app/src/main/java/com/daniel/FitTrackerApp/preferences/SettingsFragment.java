package com.daniel.FitTrackerApp.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.activities.MainActivity;
import com.daniel.FitTrackerApp.dialogs.SplitDialog;
import com.daniel.FitTrackerApp.dialogs.SplitDialogListener;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.provider.ProviderContract;
import com.daniel.FitTrackerApp.utils.UnitUtils;
import com.takisoft.fix.support.v7.preference.EditTextPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private JSONObject jsonSettingsChanged;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.general_preferences);

        setSummaries();
        jsonSettingsChanged = new JSONObject();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public boolean onPreferenceTreeClick(android.support.v7.preference.Preference preference) {
        if (preference.getKey() == getActivity().getResources().getString(R.string.audio_preferences_key)) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new AudioSettingsFragment()).addToBackStack(null).commit();
        }
        else if (preference.getKey() == getString(R.string.profile_settings)) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new ProfileSettingsFragment()).addToBackStack(null).commit();
        }
        else if(preference.getKey() == getString(R.string.split_key))
        {
            showSplitDialog();
        }
        return super.onPreferenceTreeClick(preference);
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (sharedPreferences == null) return;
        if (sharedPreferences instanceof EditTextPreference) {
            ((EditTextPreference) sharedPreferences).setSummary(((EditTextPreference) sharedPreferences).getText());
        }

        try {
            if(key.length() < 12 || !key.substring(0, 12).equals("com.facebook")){
                if(key.equals(getString(R.string.split_key))){
                    jsonSettingsChanged.put(key, sharedPreferences.getFloat(key, 0));
                } else if(key.equals(getString(R.string.split_unit_key))){
                    jsonSettingsChanged.put(key, sharedPreferences.getString(key, null));
                } else if(key.equals(getString(R.string.unit_key))){
                    jsonSettingsChanged.put(key, sharedPreferences.getString(key, null));
                }
                DBHelper.getInstance().updateLastModifiedTime(getActivity(), PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                        ProviderContract.SyncEntry.LAST_MODIFIED_SETTINGS, System.currentTimeMillis());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    SplitDialogListener splitDialogListener = new SplitDialogListener() {
        @Override
        public void onSuccess(double split, String unit) {

            PreferencesHelper.getInstance().setPreference(getString(R.string.split_key), UnitUtils.convertUnitToMeters(getActivity(), split, unit));
            PreferencesHelper.getInstance().setPreference(getString(R.string.split_unit_key), unit);
        }
    };

    private void showSplitDialog()
    {
        boolean isMetric = PreferencesHelper.getInstance().isMetric(getActivity());
        String[] units;
        int index = -1;
        if(isMetric)
        {
            units = getResources().getStringArray(R.array.metric_units);
            index = Arrays.asList(units).indexOf(PreferencesHelper.getInstance().getDistanceSplitUnit(getActivity()));
        }
        else
        {
            units = getResources().getStringArray(R.array.imperial_units);
            index = Arrays.asList(units).indexOf(PreferencesHelper.getInstance().getDistanceSplitUnit(getActivity()));
        }

        double defaultValue = PreferencesHelper.getInstance().getDistanceSplitDistance(getActivity());
        if(index == -1)
        {
            if(isMetric)
            {
                index = Arrays.asList(units).indexOf(getString(R.string.kilometers));

            }
            else
            {
                index = Arrays.asList(units).indexOf(getString(R.string.miles));
            }
        }

        SplitDialog splitDialog = new SplitDialog(getActivity(), splitDialogListener, units, UnitUtils.convertMetersToUnit(defaultValue, units[index]), index);
        splitDialog.show();
    }

    public void setSummaries()
    {
        if(PreferencesHelper.getInstance().isMetric(getActivity()))
        {
            findPreference(getString(R.string.split_key)).setSummary(String.format("%.2f",UnitUtils.convertMetersToUnit(PreferencesHelper.getInstance().getDistanceSplitDistance(getActivity()), getString(R.string.kilometers)))  + " " + getString(R.string.kilometers));
        }
        else
        {
            findPreference(getString(R.string.split_key)).setSummary(String.format("%.2f",UnitUtils.convertMetersToUnit(PreferencesHelper.getInstance().getDistanceSplitDistance(getActivity()), getString(R.string.miles)))  + " " + getString(R.string.miles));
        }
    }

}
