package com.daniel.FitTrackerApp.models.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.interfaces.RecyclerViewHolder;
import com.daniel.FitTrackerApp.models.ViewModels.TextViewCheckbox;

public class TextViewCheckboxViewHolder extends RecyclerView.ViewHolder implements RecyclerViewHolder<TextViewCheckbox>
{
    private CheckBox checkBox;
    private TextView textView;

    public TextViewCheckboxViewHolder(View v)
    {
        super(v);

        checkBox = (CheckBox) v.findViewById(R.id.checkbox1);
        textView = (TextView) v.findViewById(R.id.caloriesTextView);
    }

    @Override
    public void setView(TextViewCheckbox model)
    {
        checkBox.setId(model.getCheckboxId());
        checkBox.setOnCheckedChangeListener(model.getChangeListener());
        checkBox.setChecked(model.isSelect());
        textView.setText(model.getCoreText());
    }
}
