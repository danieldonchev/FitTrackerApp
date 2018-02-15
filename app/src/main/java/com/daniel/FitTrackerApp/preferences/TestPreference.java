package com.daniel.FitTrackerApp.preferences;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

public class TestPreference extends Preference
{
    public TestPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TestPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public TestPreference(Context context) {
        super(context);

    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        setSummary(getSharedPreferences().getString(getKey(), ""));
    }


}
