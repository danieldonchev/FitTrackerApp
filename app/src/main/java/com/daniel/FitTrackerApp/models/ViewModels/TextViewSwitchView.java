package com.daniel.FitTrackerApp.models.ViewModels;

import android.widget.CompoundButton;

public class TextViewSwitchView
{
    private String coreText;
    private int switchId;
    private CompoundButton.OnCheckedChangeListener changeListener;
    private boolean isSelected;

    public TextViewSwitchView() {}

    public int getSwitchId() {
        return switchId;
    }

    public void setSwitchId(int switchId) {
        this.switchId = switchId;
    }

    public String getCoreText() {
        return coreText;
    }

    public void setCoreText(String coreText) {
        this.coreText = coreText;
    }

    public CompoundButton.OnCheckedChangeListener getChangeListener() {
        return changeListener;
    }

    public void setChangeListener(CompoundButton.OnCheckedChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public void setSelected(boolean isSelected)
    {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {return isSelected;}

}
