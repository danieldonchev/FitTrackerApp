package com.daniel.FitTrackerApp.test;

import android.location.Location;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class MockLocationProvider
{
    private final String providerName = "FALIP";
    private final float Accuracy = 1.0f;
    private ArrayList<Location> locationLocationLocation = new ArrayList<>();
    private int currentLocationIndex = 0;

    public ArrayList<Location> getLocationLocationLocation() {return locationLocationLocation;}

    public MockLocationProvider()
    {
//        locationLocationLocation.add(pushLocation(43.216820, 27.927622));
//        locationLocationLocation.add(pushLocation(43.2169404, 27.9076928));
        locationLocationLocation.add(pushLocation(43.212409, 27.895947, 15));
        locationLocationLocation.add(pushLocation(43.212327, 27.896719, 15));
        locationLocationLocation.add(pushLocation(43.211819, 27.897829, 15));
        locationLocationLocation.add(pushLocation(43.210924, 27.899615, 1));
        locationLocationLocation.add(pushLocation(43.211499, 27.900125, 1));
        locationLocationLocation.add(pushLocation(43.212062, 27.900661, 1));
        locationLocationLocation.add(pushLocation(43.212273, 27.901954, 1));
        locationLocationLocation.add(pushLocation(43.209744, 27.899535, 1));
        locationLocationLocation.add(pushLocation(43.212351, 27.894235, 15));
        locationLocationLocation.add(pushLocation(43.212425, 27.894015, 15));
//        locationLocationLocation.add(pushLocation(43.221589, 27.916445));
//        locationLocationLocation.add(pushLocation(43.221245, 27.916917));
//        locationLocationLocation.add(pushLocation(43.220229, 27.916584));
    }

    private Location pushLocation(double lat, double lon, float accuracy)
    {
        Location location = new Location(providerName);
        location.setAccuracy(accuracy);
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setSpeed(4.5f);
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(System.nanoTime());

        return location;
    }

    public Location randomLocation()
    {
        double randomLat = ThreadLocalRandom.current().nextDouble(43.217000, 43.220260);
        double randomLon = ThreadLocalRandom.current().nextDouble(27.910000, 27.919584);

        Location location = new Location(providerName);
        location.setAccuracy(Accuracy);
        location.setLatitude(randomLat);
        location.setLongitude(randomLon);
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(System.nanoTime());

        return location;
    }

    public Location getNextLocation()
    {
        Location location = null;
        if(locationLocationLocation.get(currentLocationIndex) != null)
        {
           location = locationLocationLocation.get(currentLocationIndex);
        }
        if(currentLocationIndex == locationLocationLocation.size() - 1)
        {
            currentLocationIndex = 0;
        }
        else
        {
            currentLocationIndex++;
        }
        return location;
    }

}
