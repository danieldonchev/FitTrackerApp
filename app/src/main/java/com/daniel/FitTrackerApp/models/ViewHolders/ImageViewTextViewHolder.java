package com.daniel.FitTrackerApp.models.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.interfaces.RecyclerViewHolder;
import com.daniel.FitTrackerApp.models.ViewModels.ImageViewTextView;

public class ImageViewTextViewHolder extends RecyclerView.ViewHolder implements RecyclerViewHolder<ImageViewTextView>
{
    private ImageView imageView;
    private TextView textView;

    public ImageViewTextViewHolder(View v)
    {
        super(v);
        imageView = (ImageView) v.findViewById(R.id.imageView1);
        textView = (TextView) v.findViewById(R.id.caloriesTextView);
    }

    @Override
    public void setView(ImageViewTextView model)
    {
        imageView.setImageResource(model.getImage());
        textView.setText(model.getCoreText());
    }

}
