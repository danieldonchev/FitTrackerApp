package com.daniel.FitTrackerApp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.daniel.FitTrackerApp.R;

public class CrashActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        showCrashDialog();
    }

    private void showCrashDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("App Crashed");
        builder.setCancelable(true);
        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int id)
                {
                    finish();
                    System.exit(0);
                    //android.os.Process.killProcess(android.os.Process.myPid());
                }});
        AlertDialog alert = builder.create();
        alert.show();
    }


}
