package com.daniel.FitTrackerApp.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.utils.UnitUtils;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class SplitDialog extends AlertDialog
{
    private double unitConverted;
    private String[] units;
    private String unitSelected;
    private SplitDialogListener listener;
    private Spinner unitSpinner;
    private EditText editTextHolder;

    public SplitDialog(@NonNull Context context, SplitDialogListener listener, String[] units, double defaultValue, int optionPicked) {
        super(context);
        setOwnerActivity((Activity) context);
        this.listener = listener;
        this.units = units;
        this.unitSelected = units[optionPicked];
        this.unitConverted = defaultValue;

        LayoutInflater inflater  = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.split_dialog_preference, null);
        setView(v);

        unitSpinner = (Spinner) v.findViewById(R.id.unitSpinner);
        editTextHolder = (EditText) v.findViewById(R.id.splitText);

        ArrayAdapter<String> spinnerAdapter;
        spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, units);
        unitSpinner.setAdapter(spinnerAdapter);

        unitSpinner.setSelection(optionPicked);


        editTextHolder.setText(String.format("%.2f", defaultValue));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        setButton(BUTTON_POSITIVE, "Save", okListener);
        setButton(BUTTON_NEGATIVE, "Cancel", cancelListener);
    }

    private DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(!editTextHolder.getText().toString().isEmpty())
            {
                if(!unitSelected.equals(PreferencesHelper.getInstance().getDistanceSplitUnit(getOwnerActivity())))
                {
                    listener.onSuccess(unitConverted, unitSelected);
                }
                listener.onSuccess(Double.valueOf(editTextHolder.getText().toString()), unitSpinner.getSelectedItem().toString());
            }

        }
    };

    private DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
        }
    };


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                double unit = PreferencesHelper.getInstance().getDistanceSplitDistance(getOwnerActivity());
                String unitSelected = units[position];
                double unitConverted = UnitUtils.convertMetersToUnit(unit, unitSelected);
                editTextHolder.setText(String.format("%.2f", unitConverted));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
