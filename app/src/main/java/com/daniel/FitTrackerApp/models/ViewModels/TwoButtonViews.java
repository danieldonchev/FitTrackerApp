package com.daniel.FitTrackerApp.models.ViewModels;

import android.view.View;

public class TwoButtonViews
{
    private String titleText;
    private String buttonText1, buttonText2;
    private int buttonId1, buttonid2;
    private boolean select;
    private View.OnClickListener onClickListener;

    public TwoButtonViews() {}

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select)
    {
        this.select = select;
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getButtonText1() {
        return buttonText1;
    }

    public void setButtonText1(String buttonText1) {
        this.buttonText1 = buttonText1;
    }

    public String getButtonText2() {
        return buttonText2;
    }

    public void setButtonText2(String buttonText2) {
        this.buttonText2 = buttonText2;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener)
    {
        this.onClickListener = onClickListener;
    }

    public int getButtonId1() {
        return buttonId1;
    }

    public void setButtonId1(int buttonId1) {
        this.buttonId1 = buttonId1;
    }

    public int getButtonid2() {
        return buttonid2;
    }

    public void setButtonid2(int buttonid2) {
        this.buttonid2 = buttonid2;
    }

}
