package com.daniel.FitTrackerApp.models.ViewModels;

import android.widget.CompoundButton;

public class TextViewCheckbox
{
    private String coreText;
    private int checkboxId;
    private boolean isSelect;
    private CompoundButton.OnCheckedChangeListener changeListener;

    public TextViewCheckbox() {}

    public String getCoreText() {
        return coreText;
    }

    public void setCoreText(String coreText) {
        this.coreText = coreText;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public CompoundButton.OnCheckedChangeListener getChangeListener() {
        return changeListener;
    }

    public void setChangeListener(CompoundButton.OnCheckedChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public int getCheckboxId() {
        return checkboxId;
    }

    public void setCheckboxId(int checkboxId) {
        this.checkboxId = checkboxId;
    }

}
