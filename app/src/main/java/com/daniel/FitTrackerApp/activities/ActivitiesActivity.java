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
import com.daniel.FitTrackerApp.models.ViewModels.ImageViewTextView;
import com.daniel.FitTrackerApp.utils.DividerItemDecoration;

import java.util.ArrayList;

public class ActivitiesActivity extends BaseActivity
{
    public static final String ACTIVITY = "Activity";

    private ArrayList<ImageViewTextView> data = new ArrayList<>();
    private RecyclerViewAdapter dataAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        data = createData();
        dataAdapter = new RecyclerViewAdapter(data, this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(dataAdapter);
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

    private ArrayList<ImageViewTextView> createData()
    {
        ArrayList<ImageViewTextView> data = new ArrayList<>();

        data.add(new ImageViewTextView(R.drawable.running_icon, "Running"));
        data.add(new ImageViewTextView(R.drawable.walking, "Walking"));
        data.add(new ImageViewTextView(R.drawable.cycling, "Cycling"));

        return data;
    }

    private RecyclerItemClickListener recyclerItemClickListener()
    {
        return new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                ImageViewTextView result =  data.get(position);
                Intent intent = new Intent();
                intent.putExtra(ACTIVITY, result.getCoreText());
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
