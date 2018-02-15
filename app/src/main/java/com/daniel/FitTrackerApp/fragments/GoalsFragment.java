package com.daniel.FitTrackerApp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daniel.FitTrackerApp.goal.Goal;
import com.daniel.FitTrackerApp.goal.GoalManager;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.adapters.GoalsAdapter;
import com.daniel.FitTrackerApp.utils.DividerItemDecoration;

import java.util.ArrayList;

public class GoalsFragment extends Fragment {

    private LinearLayoutManager layoutManager;
    private ArrayList<Goal> goals;
    private RecyclerView recyclerView;
    private GoalsAdapter goalsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        goals = GoalManager.getInstance().goals;
        goalsAdapter = new GoalsAdapter(getContext(), goals);

        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(goalsAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        //recyclerView.addOnItemTouchListener(recyclerItemClickListener);
    }

}
