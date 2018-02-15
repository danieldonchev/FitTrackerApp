package com.daniel.FitTrackerApp.models.ViewHolders;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.interfaces.RecyclerViewHolder;
import com.daniel.FitTrackerApp.models.ViewModels.TextViewSwitchView;

public class TextViewSwitchViewHolder extends RecyclerView.ViewHolder implements RecyclerViewHolder<TextViewSwitchView>
{
    private Switch aSwitch;
    private TextView textView;

    public TextViewSwitchViewHolder(View v)
    {
        super(v);
        textView = (TextView) v.findViewById(R.id.caloriesTextView);
        aSwitch = (Switch) v.findViewById(R.id.switch1);
    }

    @Override
    public void setView(TextViewSwitchView model)
    {
        textView.setText(model.getCoreText());
        aSwitch.setId(model.getSwitchId());

        boolean isSelected = model.isSelected();
        if(isSelected)
        {
            aSwitch.setChecked(true);
            aSwitch.getThumbDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            aSwitch.getTrackDrawable().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        }
        else
        {
            aSwitch.setChecked(false);
            aSwitch.getThumbDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            aSwitch.getTrackDrawable().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        }

        aSwitch.setOnCheckedChangeListener(model.getChangeListener());
    }
}
