package com.daniel.FitTrackerApp.preferences;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.widget.EditText;

import com.daniel.FitTrackerApp.R;
import com.takisoft.fix.support.v7.preference.EditTextPreference;

public class EditTextPreferenceWithSummary extends EditTextPreference
{
    private EditText editText;

    public EditTextPreferenceWithSummary(Context context) {
        this(context, null);
    }

    public EditTextPreferenceWithSummary(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextPreferenceStyle);
    }

    public EditTextPreferenceWithSummary(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EditTextPreferenceWithSummary(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        editText = new AppCompatEditText(context, attrs);
        editText.setId(android.R.id.edit);
    }

    public EditText getEditText() {
        return editText;
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedInt(-1));
    }

    @Override
    protected boolean persistString(String value) {
        return persistInt(Integer.valueOf(value));
    }
}
