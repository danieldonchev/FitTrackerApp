package com.daniel.FitTrackerApp.models.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.interfaces.RecyclerViewHolder;
import com.daniel.FitTrackerApp.models.ViewModels.TwoTextViews;


public class TwoTextViewsViewHolder extends RecyclerView.ViewHolder implements RecyclerViewHolder<TwoTextViews>
{
    private TextView titleView, coreView;

    public TwoTextViewsViewHolder(View v)
    {
        super(v);

        titleView = (TextView) v.findViewById(R.id.titleView);
        coreView = (TextView) v.findViewById(R.id.coreView);
    }

    @Override
    public void setView(TwoTextViews model)
    {
        titleView.setText(model.getTitleText());
        coreView.setText(model.getCoreText());
        coreView.setId(model.getCoreId());
    }
}
