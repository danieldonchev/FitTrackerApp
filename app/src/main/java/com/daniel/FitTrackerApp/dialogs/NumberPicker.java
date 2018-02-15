package com.daniel.FitTrackerApp.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class NumberPicker extends AlertDialog
{
    private PickerSuccessListener pickerListener;
    private EditText editTextHolder;

    public NumberPicker(Context context, PickerSuccessListener pickerListener, String message, int defaultValue)
    {
        this(context, pickerListener, message);
        editTextHolder.setText(String.valueOf(defaultValue));
        editTextHolder.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    public NumberPicker(Context context, PickerSuccessListener pickerListener, String message, double defaultValue)
    {
        this(context, pickerListener, message);
        editTextHolder.setText(String.format("%.2f", defaultValue));
    }

    public NumberPicker(Context context, PickerSuccessListener pickerListener, String message)
    {
        super(context);
        this.pickerListener = pickerListener;

        LayoutInflater inflater  = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.settings_weightbox, null);
        setView(v);

        editTextHolder = (EditText) v.findViewById(R.id.numPickerDecimal);
        TextView messageTextView = (TextView) v.findViewById(R.id.numPickerUnit);

        messageTextView.setText(message);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        setButton(BUTTON_POSITIVE, "Ok", okListener);
        setButton(BUTTON_NEGATIVE, "Cancel", cancelListener);

        editTextHolder.setText("0");
    }



    private DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            float number = Float.parseFloat(editTextHolder.getText().toString());
            pickerListener.onSuccess(Double.valueOf(number));
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
