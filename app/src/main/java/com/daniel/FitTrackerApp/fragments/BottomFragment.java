package com.daniel.FitTrackerApp.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.activities.UIDataListActivity;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.interfaces.BoxUpdater;
import com.daniel.FitTrackerApp.sportactivity.SportActivityTrackingService;
import com.daniel.FitTrackerApp.sportactivity.SportActivityRecorder;

public class BottomFragment extends android.support.v4.app.Fragment implements BoxUpdater, ServiceConnection
{
    public static final String RESULT_STRING = "ResultString";
    public static final String RECORDING_BOX_TEXT_STRING = "RecordingBoxText";
    public static final String RECORDING_BOX_INDEX = "RecordingBoxNumber";
    private static final int RECORDING_BOX_COUNT = 4;

    private static final int ACTIVITY_RESULT_BOX = 2;

    private ButtonClickListener onClickBtnListener = new ButtonClickListener();
    private String[] recordingUI = new String[4];

    private TextView[] recordingTextViewArray = new TextView[4];
    private TextView[] recordingTextTextViewArray = new TextView[4];
    private TextView[] recordingTextUnitView = new TextView[4];
    private LinearLayout[] recordingBoxes = new LinearLayout[4];

    public SportActivityTrackingService trackingService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_bottom_record_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeView(view);
        setDefaultValues();
        onClickBottomFragmentComponents();
    }

    @Override
    public void onResume() {
        super.onResume();
        setRecordingTextUnitView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_RESULT_BOX) {
            if (resultCode == Activity.RESULT_OK) {
                int recordingBoxIndex = data.getExtras().getInt(RECORDING_BOX_INDEX);
                String result = data.getExtras().getString(RESULT_STRING);
                String lastData = data.getExtras().getString(RECORDING_BOX_TEXT_STRING);

                //switch text on views
                for (int i = 0; i < recordingUI.length; i++) {
                    if (i == recordingBoxIndex) {
                        continue;
                    }
                    if (recordingUI[i].equals(result)) {
                        recordingUI[i] = lastData;
                        recordingTextTextViewArray[i].setText(lastData);
                        PreferencesHelper.getInstance().setRecordingBox(getActivity(), i, lastData);
                    }
                }
                //set text view with result
                recordingUI[recordingBoxIndex] = result;
                recordingTextTextViewArray[recordingBoxIndex].setText(result);
                PreferencesHelper.getInstance().setRecordingBox(getActivity(), recordingBoxIndex, result);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //do nothing
            }
        }
    }

    private void initializeView(View view)
    {
        //recording boxes
        recordingBoxes[0] = (LinearLayout) view.findViewById(R.id.layoutBox1);
        recordingBoxes[1] = (LinearLayout) view.findViewById(R.id.layoutBox2);
        recordingBoxes[2] = (LinearLayout) view.findViewById(R.id.layoutBox3);
        recordingBoxes[3] = (LinearLayout) view.findViewById(R.id.layoutBox4);

        //textviews data
        for (int i = 0; i < RECORDING_BOX_COUNT; i++) {
            recordingTextViewArray[i] = (TextView) recordingBoxes[i].findViewById(R.id.recordingBoxCore);
            recordingTextUnitView[i] = (TextView) recordingBoxes[i].findViewById(R.id.recordingBoxUnit);
            recordingTextTextViewArray[i] = (TextView) recordingBoxes[i].findViewById(R.id.recordingBoxHeader);
            recordingUI[i] = PreferencesHelper.getInstance().getRecordingBox(getActivity(), i);
            recordingTextTextViewArray[i].setText(recordingUI[i]);
        }
    }

    private void onClickBottomFragmentComponents() {
        for (int i = 0; i < recordingBoxes.length; i++) {
            recordingBoxes[i].setOnClickListener(onClickBtnListener);
        }
    }

    private void setDefaultValues()
    {
        recordingTextViewArray[0].setText("0.00");
        recordingTextViewArray[1].setText("0.00");
        recordingTextViewArray[2].setText("0.00");
        recordingTextViewArray[3].setText("0.00");
    }

    private void setRecordingTextUnitView() {
        PreferencesHelper.getInstance().logAll();
        boolean isMetric = PreferencesHelper.getInstance().isMetric(getActivity());
        for (int i = 0; i < recordingUI.length; i++) {
            switch (recordingUI[i]) {
                case SportActivityRecorder.DISTANCE_STRING: {
                    recordingTextUnitView[i].setText(isMetric ? getString(R.string.kilometers) : getString(R.string.miles));
                    break;
                }
                case SportActivityRecorder.PACE_STRING: {
                    recordingTextUnitView[i].setText(isMetric ? getString(R.string.paceKM) : getString(R.string.paceMI));
                    break;
                }
                case SportActivityRecorder.AVERAGE_SPEED_STRING: {
                    recordingTextUnitView[i].setText(isMetric ? getString(R.string.speedKM) : getString(R.string.speedMI));
                    break;
                }
                case SportActivityRecorder.SPEED_STRING: {
                    recordingTextUnitView[i].setText(isMetric ? getString(R.string.speedKM) : getString(R.string.speedMI));
                    break;
                }
                default: {
                    recordingTextUnitView[i].setText("");
                }
            }
        }
    }

    @Override
    public void onUpdateStepCounter(int steps) {
        for (int i = 0; i < recordingTextTextViewArray.length; i++) {
            if (recordingTextTextViewArray[i].getText().toString().equals(SportActivityRecorder.STEPS_STRING)) {
                recordingTextViewArray[i].setText(String.valueOf(steps));
            }
        }
    }

    @Override
    public void onUpdateUI(SportActivityRecorder recorder) {
        for (int i = 0; i < recordingUI.length; i++) {
            if (recorder != null) {
                if (recorder.getData(recordingUI[i]) != null) {
                    recordingTextViewArray[i].setText(recorder.getData(recordingUI[i]));
                }
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        SportActivityTrackingService.LocalBinder binder = (SportActivityTrackingService.LocalBinder) service;
        trackingService = binder.getService();
        trackingService.setBoxUpdater(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        trackingService = null;
    }


    public class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity().getApplicationContext(), UIDataListActivity.class);
            switch (v.getId()) {
                case R.id.layoutBox1:
                    intent.putExtra(RECORDING_BOX_TEXT_STRING, recordingTextTextViewArray[0].getText());
                    intent.putExtra(RECORDING_BOX_INDEX, 0);
                    break;

                case R.id.layoutBox2:
                    intent.putExtra(RECORDING_BOX_TEXT_STRING, recordingTextTextViewArray[1].getText());
                    intent.putExtra(RECORDING_BOX_INDEX, 1);
                    break;

                case R.id.layoutBox3:
                    intent.putExtra(RECORDING_BOX_TEXT_STRING, recordingTextTextViewArray[2].getText());
                    intent.putExtra(RECORDING_BOX_INDEX, 2);
                    break;

                case R.id.layoutBox4:
                    intent.putExtra(RECORDING_BOX_TEXT_STRING, recordingTextTextViewArray[3].getText());
                    intent.putExtra(RECORDING_BOX_INDEX, 3);
                    break;
            }
            startActivityForResult(intent, ACTIVITY_RESULT_BOX);
        }
    }
}
