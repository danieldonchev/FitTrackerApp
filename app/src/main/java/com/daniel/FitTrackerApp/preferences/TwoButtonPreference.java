package com.daniel.FitTrackerApp.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.daniel.FitTrackerApp.R;

@SuppressWarnings("ResourceType")
public class TwoButtonPreference extends Preference {
    private CharSequence buttonLeftText, buttonRightText;
    private CharSequence buttonLeftValue, buttonRightValue;
    private boolean isLeft;

    public TwoButtonPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public TwoButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.two_button_preference);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.TwoButtonPreference, 0, 0);

        buttonLeftValue = a.getString(R.styleable.TwoButtonPreference_buttonValueLeft);
        buttonRightValue = a.getString(R.styleable.TwoButtonPreference_buttonValueRight);
        buttonLeftText = a.getString(R.styleable.TwoButtonPreference_buttonTextLeft);
        buttonRightText = a.getString(R.styleable.TwoButtonPreference_buttonTextRight);
        a.recycle();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        final Button buttonLeft = (Button) holder.findViewById(R.id.buttonLeft);
        final Button buttonRight = (Button) holder.findViewById(R.id.buttonRight);

        buttonLeft.setText(buttonLeftText);
        buttonRight.setText(buttonRightText);

        if (isLeft) {
            selectButton(buttonLeft, buttonRight);
        } else {
            selectButton(buttonRight, buttonLeft);
        }


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLeft) {
                    isLeft = false;
                    persistString(buttonRightValue.toString());
                } else {
                    isLeft = true;
                    persistString(buttonLeftValue.toString());
                }

                notifyChanged();

            }
        };

        buttonLeft.setOnClickListener(listener);
        buttonRight.setOnClickListener(listener);
    }

    public void selectButton(Button selectedButton, Button notSelectedButton) {
        selectedButton.setBackgroundColor(Color.BLUE);
        selectedButton.setEnabled(false);
        notSelectedButton.setBackgroundColor(Color.GRAY);
        notSelectedButton.setEnabled(true);
    }

    public void setSelected(String value) {
        if(value.equals(buttonLeftValue))
        {
            isLeft = true;
        }
        else
        {
            isLeft = false;
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setSelected(restorePersistedValue ? getPersistedString("")
                : (String) defaultValue);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
    }

    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        String value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readString();  // Change this to read the appropriate data type
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeString(value);  // Change this to write the appropriate data type
        }

        // Standard creator object using an instance of this class
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
