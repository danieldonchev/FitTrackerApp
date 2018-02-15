package com.daniel.FitTrackerApp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.RecyclerItemClickListener;
import com.daniel.FitTrackerApp.adapters.ActivitySummariesByTime;
import com.daniel.FitTrackerApp.adapters.SavedActivitiesAdapter;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.provider.ProviderContract;
import com.daniel.FitTrackerApp.sportactivity.SportActivitySummariesByTime;
import com.daniel.FitTrackerApp.sportactivity.SportActivitySummary;
import com.daniel.FitTrackerApp.utils.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class HistoryFragment extends Fragment
{
    private ArrayList<SportActivitySummary> activities = new ArrayList<>();
    private ArrayList<SportActivitySummariesByTime> activitiesByTime = new ArrayList<>();
    private boolean isLoading;
    private int itemsCount = 7;
    private int visibleItems = itemsCount, totalItemCount, firstVisibleItem, visibleItemCount;
    private int previousTotalItems = itemsCount;
    private TextView sortedByTextView;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private SavedActivitiesAdapter activitiesAdapter;
    private ActivitySummariesByTime activitiesByTimeAdapter;
    private Spinner timeSpinner, sortSpinner, orderSpinner;
    private ImageButton closeActivitiesByTimeButton;
    private boolean isSortedByTime, isActivitiesByTime;
    private String sortBy, time, order;
    private final String[] times = {"Single", "Weekly", "Monthly"};
    private final String[] sorts = {"Time", "Distance", "Duration", "Calories"};
    private final String[] orders = {"asc", "desc"};
    private int timeType;
    private Date startDate, endDate, monthDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstance)
    {
        activitiesAdapter = new SavedActivitiesAdapter(getActivity(), activities, getFragmentManager());
        activitiesByTimeAdapter = new ActivitySummariesByTime(getActivity(), activitiesByTime);

        timeSpinner = (Spinner) v.findViewById(R.id.first_spinner);
        sortSpinner = (Spinner) v.findViewById(R.id.second_spinner);
        orderSpinner = (Spinner) v.findViewById(R.id.order_spinner);
        sortedByTextView = (TextView) v.findViewById(R.id.sorted_by_text);
        closeActivitiesByTimeButton = (ImageButton) v.findViewById(R.id.imageButton);

        time = times[0];
        sortBy = ProviderContract.SportActivityEntry.START_TIMESTAMP;
        order = orders[1];
        setSpinners();

        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.addOnItemTouchListener(recyclerItemClickListener);
        recyclerView.setNestedScrollingEnabled(false);

        if(isSortedByTime){
            resetList();
            recyclerView.setAdapter(activitiesByTimeAdapter);
        } else {
            resetList();
            recyclerView.setAdapter(activitiesAdapter);
        }

        activities = DBHelper.getInstance().getInstance().getActivitiesSummary(getActivity(),
                0,
                itemsCount,
                PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                sortBy,
                order);
        activitiesAdapter.setActivities(activities);
        handler.post(notifyAdapter);


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                Log.v("Visible items", String.valueOf(visibleItemCount) + ", " + String.valueOf(visibleItems));
                if(totalItemCount < previousTotalItems)
                {
                    previousTotalItems = totalItemCount;
                }
                if(isLoading && (totalItemCount > previousTotalItems))
                {
                    isLoading = false;
                    previousTotalItems = totalItemCount;
                }
                if(!isLoading && (firstVisibleItem + visibleItemCount) >= totalItemCount)
                {
                    isLoading = true;
                    if(isSortedByTime && !isActivitiesByTime){
                        activitiesByTime.addAll(DBHelper.getInstance().getActivitiesSummaryByTime(
                                getActivity(),
                                timeType,
                                visibleItems,
                                itemsCount,
                                PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                                sortBy,
                                order));
                    } else {
                        if(isActivitiesByTime){
                            if(timeType == Calendar.WEEK_OF_YEAR){
                                getActivitiesByWeek();
                            } else if(timeType == Calendar.MONTH){
                                getActivitiesByMonth();
                            }
                        } else {
                            activities.addAll(DBHelper.getInstance().getActivitiesSummary(getActivity(),
                                    visibleItems,
                                    itemsCount,
                                    PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                                    sortBy,
                                    order));
                        }
                    }
                    visibleItems += itemsCount;
                    handler.post(notifyAdapter);
                }
            }
        });

        closeActivitiesByTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isActivitiesByTime = false;
                sortedByTextView.setVisibility(View.GONE);
                closeActivitiesByTimeButton.setVisibility(View.GONE);
                getActivities();

            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    RecyclerItemClickListener recyclerItemClickListener = new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener()
    {
        @Override
        public void onItemClick(View view, int position)
        {
            if(isSortedByTime && !isActivitiesByTime){
                sortedByTextView.setText(activitiesByTime.get(position).getDateRange());
                if(timeType == Calendar.WEEK_OF_YEAR){
                    startDate = activitiesByTime.get(position).getStartWeek();
                    endDate = activitiesByTime.get(position).getEndWeek();
                    getActivitiesByWeek();
                } else if(timeType == Calendar.MONTH){
                    monthDate = activitiesByTime.get(position).getMonth();
                    getActivitiesByMonth();
                }

                resetList();
                activitiesAdapter.setActivities(activities);
                recyclerView.setAdapter(activitiesAdapter);
                handler.post(notifyAdapter);

                sortedByTextView.setVisibility(View.VISIBLE);
                closeActivitiesByTimeButton.setVisibility(View.VISIBLE);
                isActivitiesByTime = true;
            } else {
                UUID recordId = activities.get(position).getId();
                SelectedActivityFromDB fragment = new SelectedActivityFromDB();
                fragment.setSportActivity(getActivity(), recordId);
                isActivitiesByTime = false;
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
            }

        }

        @Override
        public void onItemLongClick(View view, int position) {
        }
    });

    Handler handler = new Handler();

    final Runnable notifyAdapter = new Runnable() {
        public void run() {
            if(isSortedByTime){
                activitiesByTimeAdapter.notifyDataSetChanged();
            } else {
                activitiesAdapter.notifyDataSetChanged();
            }
        }
    };

    private void setSpinners(){

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, times);
        timeSpinner.setAdapter(timeAdapter);


        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, sorts);
        sortSpinner.setAdapter(sortAdapter);

        ArrayAdapter<String> orderAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, orders);
        orderSpinner.setAdapter(orderAdapter);

        timeSpinner.setSelection(0, false);
        sortSpinner.setSelection(0, false);
        orderSpinner.setSelection(1, false);

        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                time = parent.getItemAtPosition(position).toString();
                isActivitiesByTime = false;
                getActivities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                if(selectedItem.equals(sorts[0])){
                    sortBy = ProviderContract.SportActivityEntry.START_TIMESTAMP;
                } else if(selectedItem.equals(sorts[1])){
                    sortBy = ProviderContract.SportActivityEntry.DISTANCE;
                } else if(selectedItem.equals(sorts[2])){
                    sortBy = ProviderContract.SportActivityEntry.DURATION;
                } else if(selectedItem.equals(sorts[3])){
                    sortBy = ProviderContract.SportActivityEntry.CALORIES;
                }
                getActivities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                order = parent.getItemAtPosition(position).toString();
                getActivities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void resetList(){
            visibleItems = itemsCount;
            isLoading = false;
    }

    private void getActivities(){

        if(isActivitiesByTime){
            if(timeType == Calendar.WEEK_OF_YEAR){
                getActivitiesByWeek();
            } else if(timeType == Calendar.MONTH){
                getActivitiesByMonth();
            }
            resetList();
            handler.post(notifyAdapter);
        } else {
            if(time.equals(times[0])){
                activities = DBHelper.getInstance().getActivitiesSummary(getActivity(),
                        0,
                        itemsCount,
                        PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                        sortBy,
                        order);

                activitiesAdapter.setActivities(activities);
                recyclerView.setAdapter(activitiesAdapter);
                isSortedByTime = false;
            } else if(time.equals(times[1])){
                activitiesByTime = DBHelper.getInstance().getActivitiesSummaryByTime(getActivity(), Calendar.WEEK_OF_YEAR,
                        0,
                        itemsCount,
                        PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                        sortBy,
                        order);
                activitiesByTimeAdapter.setActivities(activitiesByTime);
                recyclerView.setAdapter(activitiesByTimeAdapter);
                isSortedByTime = true;
                timeType = Calendar.WEEK_OF_YEAR;
            } else if(time.equals(times[2])){
                activitiesByTime = DBHelper.getInstance().getActivitiesSummaryByTime(getActivity(), Calendar.MONTH,
                        0,
                        itemsCount,
                        PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                        sortBy,
                        order);
                activitiesByTimeAdapter.setActivities(activitiesByTime);
                recyclerView.setAdapter(activitiesByTimeAdapter);
                isSortedByTime = true;
                timeType = Calendar.MONTH;
            }

            resetList();
            handler.post(notifyAdapter);

        }
    }

    private void getActivitiesByWeek(){
        activities = DBHelper.getInstance().getSportActivitySummariesBetween(getActivity(),
                0,
                itemsCount,
                PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                sortBy,
                order,
                startDate,
                endDate);
    }

    private void getActivitiesByMonth(){
        activities = DBHelper.getInstance().getSportActivitySummariesMonth(getActivity(),
                0,
                itemsCount,
                PreferencesHelper.getInstance().getCurrentUserId(getActivity()),
                sortBy,
                order,
                monthDate);
    }
}
