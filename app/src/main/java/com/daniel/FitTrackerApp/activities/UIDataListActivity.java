package com.daniel.FitTrackerApp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.RecyclerItemClickListener;
import com.daniel.FitTrackerApp.adapters.RecyclerViewAdapter;
import com.daniel.FitTrackerApp.fragments.BottomFragment;
import com.daniel.FitTrackerApp.sportactivity.SportActivityRecorder;
import com.daniel.FitTrackerApp.models.ViewModels.ImageViewTextView;
import com.daniel.FitTrackerApp.utils.DividerItemDecoration;

import java.util.ArrayList;

public class UIDataListActivity extends BaseActivity
{
    private ArrayList<ImageViewTextView> uiDataArray = new ArrayList<>();
    private RecyclerViewAdapter uiDataAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        uiDataArray = createUIData();
        uiDataAdapter = new RecyclerViewAdapter(uiDataArray, this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(uiDataAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addOnItemTouchListener(recyclerItemClickListener());
    }

    @Override
    protected int getLayoutResource()
    {
        return R.layout.activities_activity;
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        super.onBackPressed();
    }

    private ArrayList<ImageViewTextView> createUIData()
    {
        ArrayList<ImageViewTextView> data = new ArrayList<>();

        data.add(new ImageViewTextView(R.drawable.distance_icon, SportActivityRecorder.DISTANCE_STRING));
        data.add(new ImageViewTextView(R.drawable.avg_speed_icon, SportActivityRecorder.AVERAGE_SPEED_STRING));
        data.add(new ImageViewTextView(R.drawable.speed_icon, SportActivityRecorder.SPEED_STRING));
        data.add(new ImageViewTextView(R.drawable.pace_icon, SportActivityRecorder.PACE_STRING));
        data.add(new ImageViewTextView(R.drawable.steps_icon, SportActivityRecorder.STEPS_STRING));

        return data;
    }

    private RecyclerItemClickListener recyclerItemClickListener()
    {
        return new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                ImageViewTextView result =  uiDataArray.get(position);
                Intent intent = new Intent();
                getIntent().getExtras().getInt(BottomFragment.RECORDING_BOX_INDEX);
                intent.putExtra(BottomFragment.RESULT_STRING, result.getCoreText());
                intent.putExtra(BottomFragment.RECORDING_BOX_TEXT_STRING, getIntent().getStringExtra(BottomFragment.RECORDING_BOX_TEXT_STRING));
                intent.putExtra(BottomFragment.RECORDING_BOX_INDEX, getIntent().getExtras().getInt(BottomFragment.RECORDING_BOX_INDEX));
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onItemLongClick(View view, int position)
            {

            }
        });
    }
}
