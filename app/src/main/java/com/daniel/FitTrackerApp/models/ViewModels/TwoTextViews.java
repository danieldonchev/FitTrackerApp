package com.daniel.FitTrackerApp.models.ViewModels;

public class TwoTextViews
{
    private String titleText;
    private String coreText;
    private int coreId;

    public TwoTextViews() {}

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getTitleText() {
        return titleText;
    }

    public String getCoreText() {
        return coreText;
    }

    public void setCoreText(String coreText) {
        this.coreText = coreText;
    }

    public int getCoreId() {
        return coreId;
    }

    public void setCoreId(int coreId) {
        this.coreId = coreId;
    }
}
