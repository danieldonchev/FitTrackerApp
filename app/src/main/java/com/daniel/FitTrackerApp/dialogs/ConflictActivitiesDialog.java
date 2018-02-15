package com.daniel.FitTrackerApp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.adapters.SavedActivitiesByTimeAdapter;
import com.daniel.FitTrackerApp.fragments.EditSportActivity;
import com.daniel.FitTrackerApp.sportactivity.SportActivitySummary;
import com.daniel.FitTrackerApp.utils.DividerItemDecoration;

import java.util.ArrayList;

public class ConflictActivitiesDialog extends Dialog {

    private Context context;
    private ArrayList<SportActivitySummary> summaries;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private SavedActivitiesByTimeAdapter adapter;
    private ConflictDialogCallback callback;
    private Button okButton, cancelButton;

    public ConflictActivitiesDialog(@NonNull Context context, ArrayList<SportActivitySummary> summaries, final ConflictDialogCallback callback) {
        super(context);
        this.summaries = summaries;
        this.context = context;
        this.callback = callback;

        setContentView(R.layout.dialog_conflict_activities);
        okButton = (Button) findViewById(R.id.okButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        adapter = new SavedActivitiesByTimeAdapter(context, summaries);
        layoutManager = new LinearLayoutManager(context.getApplicationContext());
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClick(EditSportActivity.ANSWER_OK);
                ConflictActivitiesDialog.this.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClick(EditSportActivity.ANSWER_CANCEL);
                ConflictActivitiesDialog.this.dismiss();
            }
        });
    }
}
