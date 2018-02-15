package com.daniel.FitTrackerApp.sportactivity;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;

import com.daniel.FitTrackerApp.AppNetworkManager;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.interfaces.SplitLocationCallback;
import com.daniel.FitTrackerApp.models.BMR;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.UnitUtils;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.UUID;


import static com.daniel.FitTrackerApp.utils.AppUtils.doubleToString;

public class SportActivityRecorder extends AbstractSportActivityRecorder implements Recorder<SportActivity> {
    public static final String DISTANCE_STRING = "Distance";
    public static final String AVERAGE_SPEED_STRING = "Avg.Speed";
    public static final String SPEED_STRING = "Speed";
    public static final String PACE_STRING = "Avg.Pace";
    public static final String STEPS_STRING = "Steps";
    public static final String CALORIES_STRING = "Calories";

    private Context context;
    private SplitLocationCallback splitLocationCallback;
    private long startTimeStamp;
    private long endTimeStamp;
    private float[] pointToPointDistance = new float[1];
    private double previousDistanceTravelled;
    private double distanceMeters = 0;
    private float speed;
    private double bmrPerSecond;
    private Location mLastLocation;
    private float split, nextSplitGoal;
    private int splitID = 1;

    private int userHeight, userAge;
    private float userWeight;
    private String userGender, userID;

    private com.daniel.FitTrackerApp.sportactivity.SportActivityMap sportActivityMap;

    private SplitRecorder currentSplit;
    private ArrayList<Split> splits;

    private boolean isPaused;
    private long pausedTime;

    private double stepsLength = 0;


    public SportActivityRecorder(Context context, boolean isMetric, String activity) {
        this.isMetric = isMetric;
        this.workout = activity;
        this.context = context;
        splits = new ArrayList<>();
        this.split = nextSplitGoal = PreferencesHelper.getInstance().getDistanceSplitDistance(context);
        currentSplit = new SplitRecorder(isMetric, SystemClock.elapsedRealtime(), split);
        sportActivityMap = new com.daniel.FitTrackerApp.sportactivity.SportActivityMap();
        userGender = PreferencesHelper.getInstance().getCurrentUserGender(context);
        userHeight = PreferencesHelper.getInstance().getCurrentUserHeight(context);
        stepsLength = AppUtils.stepLength(userHeight, userGender.charAt(0));
        userWeight = PreferencesHelper.getInstance().getCurrentUserWeight(context);
        userAge = PreferencesHelper.getInstance().getCurrentUserAge(context);
        userID = PreferencesHelper.getInstance().getCurrentUserId(context);

        this.bmrPerSecond = BMR.getBMRperSecond(PreferencesHelper.getInstance().getCurrentUserGender(context),
                PreferencesHelper.getInstance().getCurrentUserHeight(context),
                PreferencesHelper.getInstance().getCurrentUserWeight(context),
                PreferencesHelper.getInstance().getCurrentUserAge(context));

    }

    public SportActivityRecorder(Context context, boolean isMetric, String activity, SplitLocationCallback locationCallback){
        this(context, isMetric, activity);
        this.splitLocationCallback = locationCallback;
    }

    public com.daniel.FitTrackerApp.sportactivity.SportActivityMap getSportActivityMap() {
        return sportActivityMap;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(long endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public void setSplits(ArrayList<Split> splits) {
        this.splits = splits;
    }

    public ArrayList<Split> getSplits() {
        return splits;
    }

    public void setmLastLocation(Location mLastLocation) {
        this.mLastLocation = mLastLocation;
    }

    public Location getmLastLocation() {
        return mLastLocation;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public double getSpeed() {
        if (Double.isNaN(this.speed)) {
            return 0.0;
        }
        return this.speed;
    }

    public void setNextSplitGoal() {
        float currentDistance = (float) distance;
        nextSplitGoal = (float) distance + split - ((float) distance % split);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getSpeedString() {
        return doubleToString(this.speed);
    }

    public String getStepsString() {
        return doubleToString(this.steps);
    }

    public String getCaloriesString() {
        return String.format("%.2f", this.calories);
    }

    public SplitRecorder getCurrentSplit() {
        return currentSplit;
    }

    public double calculateDistanceTravelled(Location location) {
        if (location != null && mLastLocation != null && location != mLastLocation) {
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    mLastLocation.getLatitude(), mLastLocation.getLongitude(), pointToPointDistance);
            distanceMeters += pointToPointDistance[0];
        }
        return distanceMeters;
    }

    private float currentSpeed(Float speed) {
        return isMetric ? (float) (speed * 3.6) : (float) (speed * 2.2369362920544);
    }

    private void calculateSplitFromSteps(){
        if(nextSplitGoal <= distanceMeters){
            addCurrentSplit();
            nextSplitGoal += split;
            startNewSplit();
        }
    }

    private void calculateSplitFromLocation(Location location) {
        if (mLastLocation == null) {
            mLastLocation = location;
        }
        if (previousDistanceTravelled == 0) {
            previousDistanceTravelled = distanceMeters;
        }

        double heading = SphericalUtil.computeHeading(locationLatLng(mLastLocation), locationLatLng(location));

        if (nextSplitGoal <= distanceMeters) {
            addCurrentSplit();
            float offSet = nextSplitGoal - (float) previousDistanceTravelled * 1000;
            LatLng latLng = SphericalUtil.computeOffset(locationLatLng(mLastLocation), offSet, heading);
            nextSplitGoal += split;
            startNewSplit();

            MarkerOptions markerOptions = sportActivityMap.addSplitMarker(latLng);
            splitLocationCallback.onReceiveSplitLocation(markerOptions);
        }
        currentSplit.calculateData(distance);

        previousDistanceTravelled = distance;
    }

    public void calculateDataFromLocation(Location location)
    {
        distanceMeters = calculateDistanceTravelled(location);
        speed = currentSpeed(location.getSpeed());
        calculateDistance();
        calculateData();
        calculateSplitFromLocation(location);
    }

    public void calculateDataFromSteps(Float steps){
        this.steps = steps.intValue();
        distanceMeters = stepsLength * steps;
        //speed = currentSpeed(location.getSpeed());
        calculateDistance();
        calculateData();
        calculateSplitFromSteps();
    }

    public void calculateDistance() {
        if (isMetric) {
            distance = distanceMeters / 1000;
        } else {
            distance = distanceMeters * 0.000621371192;
        }
    }

    public void calculateData(){
        averageSpeed = calculateCurrentAverageSpeed(distance);
        pace = calculateAveragePace(averageSpeed);
    }

    private void addCurrentSplit() {
        currentSplit.setDuration(currentSplit.getCurrentTimeSeconds());
        currentSplit.calculateFinalData(isMetric, split);
        Split split = new Split(splitID++, currentSplit.getDuration(), currentSplit.getDistance(), isMetric);
        split.calculateFinalData(isMetric, split.distance);
        splits.add(split);
    }

    private void addFinalSplit() {
        currentSplit.setDuration(currentSplit.getCurrentTimeSeconds());
        currentSplit.setDistance(distanceMeters % split);
        splits.add(new Split(splitID++, currentSplit.getDuration(), currentSplit.getDistance(), isMetric));
    }

    private void startNewSplit() {
        currentSplit = new SplitRecorder(isMetric, SystemClock.elapsedRealtime(), split);
    }

    public void start(long startTime, long startTimeStamp){
        this.startTime = startTime;
        this.startTimeStamp = startTimeStamp;
    }

    public SportActivity stop() {
        if (isPaused) {
            addPauseTime(pausedTime);
            currentSplit.addPauseTime(pausedTime);
        }

        isPaused = false;
        duration = getCurrentTimeSeconds();
        endTimeStamp = System.currentTimeMillis();
        lastModified = System.currentTimeMillis();
        calculateFinalData(context, isMetric, userGender, userHeight, userWeight, userAge, distance);
        addFinalSplit();
        com.traker.shared.SportActivity sportActivity = toDTO();

        DBHelper.getInstance().addActivity(sportActivity, userID, context, 0, type);
        AppNetworkManager.sendSportActivity(context, sportActivity);

        return toSportActivity();
    }

    public void pause() {
        if (!isPaused) {
            setDuration(getCurrentTimeMs());
            pausedTime = SystemClock.elapsedRealtime();
            isPaused = true;
        }
    }

    public void unPause() {
        if (isPaused) {
            addPauseTime(pausedTime);
            currentSplit.addPauseTime(pausedTime);
            isPaused = false;
        }
    }

    public com.traker.shared.SportActivity toDTO(){
        ArrayList<com.traker.shared.Split> splitsDTO = new ArrayList<>();
        for(Split split : splits){
            splitsDTO.add(new com.traker.shared.Split(split.getId(), split.duration, split.distance));
        }

        return new com.traker.shared.SportActivity(UUID.randomUUID(),
                                                    workout,
                                                    duration,
                                                    distanceMeters,
                                                    steps,
                                                    calories,
                                                    sportActivityMap.toSharedSportActivityMap(),
                                                    startTimeStamp,
                                                    endTimeStamp,
                                                    type,
                                                    lastModified,
                                                    splitsDTO);
    }

    public com.daniel.FitTrackerApp.sportactivity.SportActivity toSportActivity()
    {
        return new SportActivity(workout,
                                duration,
                                isMetric ? UnitUtils.convertKMinMeters(distance) : UnitUtils.convertMilesInMeters(distance),
                                steps,
                                calories,
                                sportActivityMap,
                                startTimeStamp,
                                endTimeStamp,
                                lastModified,
                                splits
                                );
    }

    public String getData(String data) {
        switch (data) {
            case DISTANCE_STRING:
                return this.getDistanceString();
            case SPEED_STRING:
                return this.getSpeedString();
            case AVERAGE_SPEED_STRING:
                return this.getAverageSpeedString();
            case PACE_STRING:
                return this.getAveragePaceString();
        }
        return null;
    }

    private LatLng locationLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }


    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }
}