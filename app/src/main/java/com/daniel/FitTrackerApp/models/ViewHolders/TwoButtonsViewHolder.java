package com.daniel.FitTrackerApp.models.ViewHolders;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.interfaces.RecyclerViewHolder;
import com.daniel.FitTrackerApp.models.ViewModels.TwoButtonViews;

public class TwoButtonsViewHolder extends RecyclerView.ViewHolder implements RecyclerViewHolder<TwoButtonViews>
{
    private TextView titleText;
    private Button button1, button2;

    public TwoButtonsViewHolder(View v)
    {
        super(v);

        titleText = (TextView) v.findViewById(R.id.titleText);
        button1 = (Button) v.findViewById(R.id.button1);
        button2 = (Button) v.findViewById(R.id.button2);
    }


    @Override
    public void setView(TwoButtonViews model)
    {
        titleText.setText(model.getTitleText());

        button1.setText(model.getButtonText1());
        button1.setId(model.getButtonId1());
        button1.setOnClickListener(model.getOnClickListener());

        button2.setText(model.getButtonText2());
        button2.setId(model.getButtonid2());
        button2.setOnClickListener(model.getOnClickListener());

        if(model.isSelect())
        {
            button1.setBackgroundColor(Color.BLUE);
            button2.setBackgroundColor(Color.GRAY);
        }
        else
        {
            button2.setBackgroundColor(Color.BLUE);
            button1.setBackgroundColor(Color.GRAY);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener)
    {
        button1.setOnClickListener(onClickListener);
        button2.setOnClickListener(onClickListener);
    }

}
