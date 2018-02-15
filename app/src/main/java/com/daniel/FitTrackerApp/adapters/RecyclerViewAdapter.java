package com.daniel.FitTrackerApp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.models.ViewHolders.ImageViewTextViewHolder;
import com.daniel.FitTrackerApp.models.ViewModels.HeaderView;
import com.daniel.FitTrackerApp.models.ViewModels.ImageViewTextView;
import com.daniel.FitTrackerApp.models.ViewModels.OneTextView;
import com.daniel.FitTrackerApp.models.ViewModels.TextViewCheckbox;
import com.daniel.FitTrackerApp.models.ViewModels.TextViewSwitchView;
import com.daniel.FitTrackerApp.models.ViewModels.TwoButtonViews;
import com.daniel.FitTrackerApp.models.ViewModels.TwoTextViews;
import com.daniel.FitTrackerApp.models.ViewHolders.HeaderViewHolder;
import com.daniel.FitTrackerApp.models.ViewHolders.OneTextViewHolder;
import com.daniel.FitTrackerApp.models.ViewHolders.TextViewCheckboxViewHolder;
import com.daniel.FitTrackerApp.models.ViewHolders.TextViewSwitchViewHolder;
import com.daniel.FitTrackerApp.models.ViewHolders.TwoButtonsViewHolder;
import com.daniel.FitTrackerApp.models.ViewHolders.TwoTextViewsViewHolder;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Object[] data;
    private Context context;

    private static final int ITEM_TYPE_HEADER = 0;
    private static final int ITEM_TYPE_TEXTVIEW = 1;
    private static final int ITEM_TYPE_TWO_TEXTVIEWS = 2;
    private static final int ITEM_TYPE_BUTTONS = 3;
    private static final int ITEM_TYPE_IMAGE_TEXT = 4;
    private static final int ITEM_TYPE_CHECKBOX = 5;
    private static final int ITEM_TYPE_SWITCH = 6;

    public RecyclerViewAdapter(Object[] data, Context context)
    {
        this.data = data;
        this.context = context;
    }

    public RecyclerViewAdapter(ArrayList<?> data, Context context)
    {
        this.data = data.toArray();
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ITEM_TYPE_HEADER)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.vh_header_view, parent, false);
            return new HeaderViewHolder(view);
        }
        else if (viewType == ITEM_TYPE_TEXTVIEW)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.vh_one_textview_item, parent, false);
            return new OneTextViewHolder(view);
        }
        else if (viewType == ITEM_TYPE_TWO_TEXTVIEWS)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.vh_two_text_views_item, parent, false);
            return new TwoTextViewsViewHolder(view);
        }
        else if(viewType == ITEM_TYPE_IMAGE_TEXT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.vh_imageview_text, parent, false);
            return new ImageViewTextViewHolder(view);
        }
        else if(viewType == ITEM_TYPE_CHECKBOX)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.vh_text_checkbox, parent, false);
            return new TextViewCheckboxViewHolder(view);
        }
        else if (viewType == ITEM_TYPE_BUTTONS)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.vh_two_buttons_item, parent, false);
            return new TwoButtonsViewHolder(view);
        }
        else if(viewType == ITEM_TYPE_SWITCH)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.vh_switch_view, parent, false);
            return new TextViewSwitchViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        int itemType = getItemViewType(position);

        if(itemType == ITEM_TYPE_HEADER)
        {
            ((HeaderViewHolder)holder).setView((HeaderView)data[position]);
        }
        else if(itemType == ITEM_TYPE_TEXTVIEW)
        {
            ((OneTextViewHolder)holder).setView((OneTextView)data[position]);
        }
        else if(itemType == ITEM_TYPE_TWO_TEXTVIEWS)
        {
            ((TwoTextViewsViewHolder)holder).setView((TwoTextViews)data[position]);
        }
        else if(itemType == ITEM_TYPE_IMAGE_TEXT)
        {
            ((ImageViewTextViewHolder)holder).setView((ImageViewTextView)data[position]);
        }
        else if(itemType == ITEM_TYPE_BUTTONS)
        {
            ((TwoButtonsViewHolder)holder).setView((TwoButtonViews)data[position]);
        }
        else if(itemType == ITEM_TYPE_CHECKBOX)
        {
            ((TextViewCheckboxViewHolder)holder).setView((TextViewCheckbox)data[position]);
        }
        else if(itemType == ITEM_TYPE_SWITCH)
        {
            ((TextViewSwitchViewHolder)holder).setView((TextViewSwitchView)data[position]);
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        if(data[position] instanceof HeaderView)
        {
            return ITEM_TYPE_HEADER;
        }
        else if(data[position] instanceof OneTextView)
        {
            return ITEM_TYPE_TEXTVIEW;
        }
        else if(data[position] instanceof TwoTextViews)
        {
            return ITEM_TYPE_TWO_TEXTVIEWS;
        }
        else if(data[position] instanceof ImageViewTextView)
        {
            return ITEM_TYPE_IMAGE_TEXT;
        }
        else if(data[position] instanceof TextViewCheckbox)
        {
            return ITEM_TYPE_CHECKBOX;
        }
        else if(data[position] instanceof TwoButtonViews)
        {
            return ITEM_TYPE_BUTTONS;
        }
        else if(data[position] instanceof TextViewSwitchView)
        {
            return ITEM_TYPE_SWITCH;
        }

        return -1;
    }

    @Override
    public int getItemCount()
    {
        return data.length;
    }
}
