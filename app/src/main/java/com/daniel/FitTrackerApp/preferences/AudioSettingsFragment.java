package com.daniel.FitTrackerApp.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.activities.MainActivity;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.provider.ProviderContract;

import org.json.JSONException;
import org.json.JSONObject;


public class AudioSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private JSONObject jsonSettingsChanged;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity)getActivity()).provideBackwardNavigation();
        jsonSettingsChanged = new JSONObject();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.audio_preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        findPreference(getString(R.string.audio_repeat_time_key)).setSummary(String.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(getString(R.string.audio_repeat_time_key), 0)));
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        ((MainActivity)getActivity()).sendChangedSettings(jsonSettingsChanged);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(sharedPreferences == null) return;
        try
        {
            if(key == getString(R.string.audio_repeat_time_key)) {
                findPreference(getString(R.string.audio_repeat_time_key)).setSummary(String.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(key, 0)));
                jsonSettingsChanged.put(key, sharedPreferences.getInt(key, 0));
                DBHelper.getInstance().updateLastModifiedTime(getActivity(), PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                        ProviderContract.SyncEntry.LAST_MODIFIED_SETTINGS, System.currentTimeMillis());
            }
            if(key.length() < 12 || !key.substring(0, 12).equals("com.facebook")){
                jsonSettingsChanged.put(key, sharedPreferences.getBoolean(key, false));
                DBHelper.getInstance().updateLastModifiedTime(getActivity(), PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                        ProviderContract.SyncEntry.LAST_MODIFIED_SETTINGS, System.currentTimeMillis());
            }
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
    }
}
