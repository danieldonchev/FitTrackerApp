package com.daniel.FitTrackerApp.utils;

import android.content.Context;

import com.daniel.FitTrackerApp.R;

public class UnitUtils
{
    public static double feetInMeters = 0.3048;
    public static double yardInMeters = 0.9144;
    public static double kmInMeters = 1000;
    public static double milesInMeters = 1609.344;


    public static double kgInLBS = 2.20462262;


    public static double convertKGtoLBS(double weight)
    {
        return (weight * kgInLBS);
    }

    public static double convertLBStoKG(double weight)
    {
        return (weight / kgInLBS);
    }

    public static String lbsToString(double weight)
    {
        return String.valueOf((int) convertKGtoLBS(weight));
    }

    public static String kgToString(double weight)
    {
        return String.format("%.1f", weight);
    }

    public static String convertCMtoFTiNCH(int height)
    {
        int feetPart = (int) Math.floor((height / 2.54) / 12);
        int inchesPart = (int) Math.round((height / 2.54) - (feetPart * 12));
        return String.format("%d'%d\"", feetPart, inchesPart);
    }

    public static int convertFTiNCHtoCM(String feetinch)
    {
        String result = feetinch.replace("\"", "");
        String[] splitResult = result.split("'");

        return (int) (Integer.parseInt(splitResult[0]) * 30.48 + Integer.parseInt(splitResult[1]) * 2.54);
    }

    public static double convertMetersToUnit(double split, String unit)
    {
        if (unit.equals("meters")) {
            return split;
        }
        else if (unit.equals("km")) {
            return split / 1000;
        }
        else if (unit.equals("ft")) {
            return split / 0.3048;
        }
        else if (unit.equals("yards")) {
            return split / 0.9144;

        }
        else if (unit.equals("miles")) {
            return split / 1609.344;
        }
        return -1;
    }

    public static double convertUnitToMeters(Context context, double split, String unit)
    {
        if (unit.equals(context.getString(R.string.meters))) {
            return split;
        }
        else if (unit.equals(context.getString(R.string.km))) {
            return split * 1000;
        }
        else if (unit.equals(context.getString(R.string.ft))) {
            return split * 0.3048;
        }
        else if (unit.equals(context.getString(R.string.yards))) {
            return split * 0.9144;

        }
        else if (unit.equals(context.getString(R.string.miles))) {
            return split * 1609.344;
        }
        return -1;
    }

    public static double convertKMinMeters(double distance){
        return distance * kmInMeters;
    }

    public static double convertMilesInMeters(double distance){
        return distance * milesInMeters;
    }

    public static String getDistanceString(Context context, double distance, boolean isMetric){
        return isMetric ? AppUtils.doubleToString(UnitUtils.convertMetersToUnit(distance, context.getString(R.string.km))) :
                AppUtils.doubleToString(UnitUtils.convertMetersToUnit(distance, context.getString(R.string.miles)));
    }


}
