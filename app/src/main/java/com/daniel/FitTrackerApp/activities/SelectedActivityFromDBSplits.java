package com.daniel.FitTrackerApp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.adapters.SavedSplitsAdapter;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.sportactivity.Split;

import java.util.ArrayList;

public class SelectedActivityFromDBSplits extends BaseActivity
{
    private ArrayList<Split> splits;
    private ListView splitsListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String id;
        if(getIntent().hasExtra("activityID"))
        {
            id = getIntent().getStringExtra("activityID");
            splits = DBHelper.getInstance().getActivitySplits(getApplicationContext(), id, PreferencesHelper.getInstance().getCurrentUserId(this));
        }

        splitsListView = (ListView) findViewById(R.id.savedSplits);
        SavedSplitsAdapter savedSplitsAdapter = new SavedSplitsAdapter(getApplicationContext(), R.layout.item_splits, splits);
        splitsListView.setAdapter(savedSplitsAdapter);
    }

    @Override
    protected int getLayoutResource()
    {
        return R.layout.activity_activitysplits_db;
    }
}
