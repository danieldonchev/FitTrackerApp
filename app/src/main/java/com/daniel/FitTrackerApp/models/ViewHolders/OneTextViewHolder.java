package com.daniel.FitTrackerApp.models.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.interfaces.RecyclerViewHolder;
import com.daniel.FitTrackerApp.models.ViewModels.OneTextView;


public class OneTextViewHolder extends RecyclerView.ViewHolder implements RecyclerViewHolder<OneTextView>
{
    private TextView textView;

    public OneTextViewHolder(View v)
    {
        super(v);
        textView = (TextView) v.findViewById(R.id.caloriesTextView);
    }

    @Override
    public void setView(OneTextView model)
    {
        textView.setText(model.getCoreText());
    }
}
