package com.daniel.FitTrackerApp.models.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.interfaces.RecyclerViewHolder;
import com.daniel.FitTrackerApp.models.ViewModels.HeaderView;

public class HeaderViewHolder  extends RecyclerView.ViewHolder implements RecyclerViewHolder<HeaderView>
{
    private TextView headerView;

    public HeaderViewHolder(View v)
    {
        super(v);
        headerView = (TextView) v.findViewById(R.id.caloriesTextView);
    }

    @Override
    public void setView(HeaderView model)
    {
        headerView.setText(model.getCoreText());
    }

}

