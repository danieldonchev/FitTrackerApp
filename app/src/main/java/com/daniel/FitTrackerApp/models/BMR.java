package com.daniel.FitTrackerApp.models;

public class BMR
{
    private static BMR ourInstance = new BMR();

    public static BMR getInstance() {
        return ourInstance;
    }

    private BMR() {}

    public static double getbMR(CharSequence sex, float height, float weight, int age)
    {
            if(sex.equals("m"))
            {
                return  665 + 9.6f * weight + 1.8f * height  - 4.7f * age;
            }
            else if(sex.equals("f"))
            {
                return  66 + 13.7f * weight + 5 * height - 6.8f * age;
            }

        return 0;
    }

    public static double getBMRperSecond(CharSequence sex, float height, float weight, int age)
    {
        return getbMR(sex, height, weight, age) / 86400;
    }

    public static double getBMRperHour(CharSequence sex, float height, float weight, int age)
    {
        return getbMR(sex, height, weight, age) / 24;
    }

}
