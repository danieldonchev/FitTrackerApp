package com.daniel.FitTrackerApp.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.daniel.FitTrackerApp.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ScrollPicker extends AlertDialog
{
    private NumberPicker numPicker;
    private PickerSuccessListener heightListener;

    public ScrollPicker(Context context, PickerSuccessListener<Integer> heightListener, int minValue, int maxValue, int pickerValue)
    {
        this(context, heightListener);

        numPicker.setMaxValue(maxValue);
        numPicker.setMinValue(minValue);
        numPicker.setValue(pickerValue);

        setButton(BUTTON_POSITIVE, "Ok", okListener);
        setButton(BUTTON_NEGATIVE, "Cancel", cancelListener);
    }

    public ScrollPicker(Context context, PickerSuccessListener<Integer> heightListener, String[] values, int pickerValue, int minValue, int maxValue)
    {
        this(context, heightListener);

        numPicker.setMinValue(minValue);
        numPicker.setMaxValue(maxValue);

        numPicker.setDisplayedValues(values);
        numPicker.setValue(pickerValue);
    }

    public ScrollPicker(Context context, PickerSuccessListener<Integer> heightListener)
    {
        super(context);
        this.heightListener = heightListener;
        LayoutInflater inflater  = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog_number_picker, null);
        setView(v);
        numPicker = (NumberPicker) v.findViewById(R.id.numPicker);
        numPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setButton(BUTTON_POSITIVE, "ok", okListener);
    }

    private DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            heightListener.onSuccess(numPicker.getValue());

        }
    };

    private DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
        }
    };
}
