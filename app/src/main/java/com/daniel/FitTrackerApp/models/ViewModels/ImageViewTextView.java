package com.daniel.FitTrackerApp.models.ViewModels;

public class ImageViewTextView
{
    private int image;
    private String coreText;

    public ImageViewTextView() {}

    public ImageViewTextView(int image, String coreText)
    {
        this.image = image;
        this.coreText = coreText;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getCoreText() {
        return coreText;
    }

    public void setCoreText(String coreText) {
        this.coreText = coreText;
    }

}
