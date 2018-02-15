package com.daniel.FitTrackerApp.models;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.daniel.FitTrackerApp.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

public class ActivityCalories
{
    public static final String RUNNING = "Running";
    public static final String WALKING = "Walking";
    public static final String CYCLING = "Cycling";

    float mphToKmh = 1.609344f;

    public static float getCurrentMET(Context context, boolean isMetric, float avgSpeed, String activity)
    {
        if(!Double.isNaN(avgSpeed))
        {
            if(isMetric)
            {
                avgSpeed = avgSpeed * 0.6213712f;
            }

            return getMET(context, activity, avgSpeed);
        }
        return 1;
    }

    private static float getMET(Context context, String activity, float intensity){
        SortedMap<Float, Float> map = new TreeMap<Float, Float>();
        boolean isFound = false, isDone = false;
        float key, value;
        XmlResourceParser xrp = context.getResources().getXml(R.xml.activity_calories);
        String text = "";
        String atr = null;
        try {
            int eventType = xrp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                    {break;}
                    case XmlPullParser.START_TAG:
                    {
                        if(isFound){
                            String name = xrp.getName();
                            if(name.equals("values")){
                                String attr1 = xrp.getAttributeValue(null, "speed");
                                String attr2 = xrp.getAttributeValue(null, "met");

                                map.put(Float.parseFloat(attr1), Float.parseFloat(attr2));
                            }
                            if(!name.equals("values")){
                                isDone = true;
                            }
                        }
                        break;
                    }
                    case XmlPullParser.END_TAG:{

                        break;
                    }
                    case XmlPullParser.TEXT:
                    {
                        text = xrp.getText();
                        break;
                    }

                }
                if(!isFound){
                    if(text.equals(activity)){
                        isFound = true;
                    }
                }
                if(isDone){
                    break;
                }

                eventType = xrp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException ex){
            ex.printStackTrace();
        }

        Float[] keys = map.keySet().toArray(new Float[map.size()]);
        float distance = Math.abs(keys[0] - intensity);
        int foundIndex = 0;

        for(int i = 0; i < keys.length; i++){
            float newDistance = Math.abs(keys[i] - intensity);
            if(newDistance < distance){
                distance = newDistance;
                foundIndex = i;
            }
        }
        float a = map.get(keys[foundIndex]);

        return a;
    }

}
