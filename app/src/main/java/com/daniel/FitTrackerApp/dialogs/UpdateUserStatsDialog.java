package com.daniel.FitTrackerApp.dialogs;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daniel.FitTrackerApp.AppNetworkManager;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.authenticate.AccountAuthenticator;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.provider.ProviderContract;
import com.daniel.FitTrackerApp.utils.UnitUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UpdateUserStatsDialog extends Dialog
{
    public static final int METRIC_MAX_VALUE = 300;
    public static final int METRIC_MIN_VALUE = 1;
    public static final int IMPERIAL_MAX_VALUE = 59;
    public static final int IMPERIAL_MIN_VALUE = 0;

    public static final String DISMISS_DIALOG_INTENT = "dismiss";

    private Context context;
    private RelativeLayout heightLayout, weightLayout, birthdayLayout;
    private TextView heightHeader, heightCore, weightHeader, weightCore, birthHeader, birthCore, headerText;
    private Button doneButton;
    private ProgressBar progressBar;
    private JSONObject jsonObject;
    private String[] imperialHeights;
    private boolean isMetric;
    private int userHeight;
    private double userWeight;
    private String userBirthday;
    private long userBirthdayMs;

    public UpdateUserStatsDialog(Context context) {
        super(context);
        this.context = context;
        jsonObject = new JSONObject();

        setContentView(R.layout.dialog_update_userstats);

        setCanceledOnTouchOutside(false);
        isMetric = PreferencesHelper.getInstance().isMetric(getContext());
        userHeight = PreferencesHelper.getInstance().getCurrentUserHeight(getContext());
        userWeight = PreferencesHelper.getInstance().getCurrentUserWeight(getContext());
        userBirthday = PreferencesHelper.getInstance().getBirthdayFormatted(getContext());

        heightLayout = (RelativeLayout) findViewById(R.id.heightLayout);
        weightLayout = (RelativeLayout) findViewById(R.id.weightLayout);
        birthdayLayout = (RelativeLayout) findViewById(R.id.birthdayLayout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        headerText = (TextView) findViewById(R.id.headerText);
        doneButton = (Button) findViewById(R.id.doneButton);

        Account currentAccount = null;
        for(Account account : AccountManager.get(getContext()).getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE)){
            if(account.name.equals(PreferencesHelper.getInstance().getCurrentUserEmail(getContext()))){
                currentAccount = account;
            }
        }

        if(ContentResolver.isSyncActive(currentAccount, ProviderContract.CONTENT_AUTHORITY)){

            heightLayout.setVisibility(View.GONE);
            weightLayout.setVisibility(View.GONE);
            birthdayLayout.setVisibility(View.GONE);
            doneButton.setVisibility(View.GONE);
            headerText.setText("Getting settings from server, please wait.");
        } else {
            progressBar.setVisibility(View.GONE);
            setView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, new IntentFilter(DISMISS_DIALOG_INTENT));
    }

    @Override
    public void onBackPressed()
    {}

    private void setView()
    {
        heightLayout.setOnClickListener(onClickListener);
        weightLayout.setOnClickListener(onClickListener);
        birthdayLayout.setOnClickListener(onClickListener);

        heightHeader = (TextView) heightLayout.findViewById(R.id.titleView);
        heightCore = (TextView) heightLayout.findViewById(R.id.coreView);

        weightHeader = (TextView) weightLayout.findViewById(R.id.titleView);
        weightCore = (TextView) weightLayout.findViewById(R.id.coreView);

        birthHeader = (TextView) birthdayLayout.findViewById(R.id.titleView);
        birthCore = (TextView) birthdayLayout.findViewById(R.id.coreView);


        heightHeader.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        heightCore.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        weightHeader.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        weightCore.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        birthHeader.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        birthCore.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        heightHeader.setText("Height");
        heightCore.setText(isMetric ? String.valueOf(userHeight) + " " + getContext().getString(R.string.centimeters): UnitUtils.convertCMtoFTiNCH(userHeight));

        weightHeader.setText("Weight");
        weightCore.setText(isMetric ? UnitUtils.kgToString(userWeight) + " " + getContext().getString(R.string.kilograms) : UnitUtils.lbsToString(userWeight) + " " + getContext().getString(R.string.lbs));

        birthHeader.setText("Birthday");
        birthCore.setText(userBirthday);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PreferencesHelper.getInstance().setPreference(getContext().getString(R.string.weight_key), userWeight);
                PreferencesHelper.getInstance().setPreference(getContext().getString(R.string.height_key), userHeight);
                PreferencesHelper.getInstance().setPreference(getContext().getString(R.string.birthday_key), userBirthdayMs);
                if(jsonObject.length() > 0)
                {
                    sendData();
                }
                DBHelper.getInstance().updateAccountDetailsStatus(getContext());
                dismiss();
            }
        });
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.heightLayout:
                {
                    if(isMetric)
                    {
                        ScrollPicker scrollPicker = new ScrollPicker(getContext(), heightListenerInt, METRIC_MIN_VALUE, METRIC_MAX_VALUE, userHeight);
                        scrollPicker.show();
                    }
                    else
                    {
                        int currentHeightIndex;
                        String userHeight = UnitUtils.convertCMtoFTiNCH(UpdateUserStatsDialog.this.userHeight);
                        imperialHeights = imperialHeights();
                        for(currentHeightIndex = 0; currentHeightIndex < imperialHeights.length - 1; currentHeightIndex++)
                        {
                            if(imperialHeights[currentHeightIndex].equals(userHeight))
                            {
                                break;
                            }
                        }

                        ScrollPicker scrollPicker = new ScrollPicker(getContext(), heightListenerString, imperialHeights, currentHeightIndex, 0, 59);
                        scrollPicker.show();
                    }
                    break;
                }
                case R.id.weightLayout:
                {
                    int weightImperial = (int) UnitUtils.convertKGtoLBS(userWeight);
                    if(isMetric)
                    {
                        NumberPicker weightPicker = new NumberPicker(getContext(), weightListener, getContext().getString(R.string.kilograms), userWeight);
                        weightPicker.show();
                    }
                    else
                    {
                        NumberPicker weightPicker = new NumberPicker(getContext(), weightListener, getContext().getString(R.string.lbs), weightImperial);
                        weightPicker.show();
                    }
                    break;
                }
                case R.id.birthdayLayout:
                {
                    Calendar calendar = Calendar.getInstance();
                    DatePickerDialog datePicker = new DatePickerDialog(context, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                    datePicker.show();
                    break;
                }
            }
        }
    };


    PickerSuccessListener<Double> weightListener = new PickerSuccessListener<Double>() {
        @Override
        public void onSuccess(Double data) {
            weightCore.setText(data.toString());

            userWeight = isMetric ? data : UnitUtils.convertLBStoKG(data);
            weightCore.setText(isMetric ? UnitUtils.kgToString(userWeight) + " " + getContext().getString(R.string.kilograms) : UnitUtils.lbsToString(userWeight) + " " + getContext().getString(R.string.lbs));
            try
            {
                jsonObject.put(getContext().getString(R.string.weight_key), data);
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }
            //Settings.changeWeight(data.floatValue());
        }
    };

    PickerSuccessListener<Integer> heightListenerInt = new PickerSuccessListener<Integer>() {
        @Override
        public void onSuccess(Integer data) {
            heightCore.setText(data.toString());
            PreferencesHelper.getInstance().setPreference(getContext().getString(R.string.height_key), data);
            try
            {
                jsonObject.put(getContext().getString(R.string.height_key), data);
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }
            //Settings.changeHeight(data);
        }
    };

    PickerSuccessListener<Integer> heightListenerString = new PickerSuccessListener<Integer>() {
        @Override
        public void onSuccess(Integer data) {
            String height = imperialHeights[data];
            userHeight = UnitUtils.convertFTiNCHtoCM(height);
            heightCore.setText(height);
            try
            {
                jsonObject.put(getContext().getString(R.string.height_key), userHeight);
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }
            //Settings.changeHeight(heightCm);
        }
    };

    PickerSuccessListener<Date> dateListener = new PickerSuccessListener<Date>() {
        @Override
        public void onSuccess(Date data) {

        }
    };

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

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
    {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.YEAR, year);
            birthCore.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTimeInMillis()));
            userBirthdayMs = calendar.getTimeInMillis();

            try
            {
                jsonObject.put(getContext().getString(R.string.birthday_key), calendar.getTimeInMillis());
            }
            catch (JSONException ex)
            {
                ex.printStackTrace();
            }
        }
    };

    private void sendData()
    {
        AppNetworkManager.sendChangedSettings(getContext(), jsonObject, System.currentTimeMillis());
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateUserStatsDialog.this.dismiss();
        }
    };
}
