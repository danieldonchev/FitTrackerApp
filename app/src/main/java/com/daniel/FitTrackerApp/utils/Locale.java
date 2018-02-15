package com.daniel.FitTrackerApp.utils;

import android.content.Context;

public final class Locale
{
    private Locale(Context context) {}

    public static boolean isMetric(Context context)
    {
        String countryCode;
        java.util.Locale currentLocale = context.getResources().getConfiguration().locale;
        countryCode = currentLocale.getISO3Country();

        if("US".equals(countryCode))
        {
            return false;
        }
        if("LR".equals(countryCode))
        {
            return false;
        }
        return !"MM".equals(countryCode);
    }
}
