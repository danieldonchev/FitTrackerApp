package com.traker.shared;

import com.google.flatbuffers.FlatBufferBuilder;

import java.nio.ByteBuffer;
import java.util.UUID;

import flatbuf.Polyline;

public class SportActivityWithOwner extends AbstractWorkout implements FlatBufferSerializable{

    private String activityID;
    private String userID;
    private String name;
    private double distance = 0;
    private long steps;
    private long startTimestamp;
    private long endTimestamp;
    private LatLng latLng;
    private byte[] profilePic;

    public SportActivityWithOwner(){}


    @Override
    public byte[] serialize() {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);

        int finish = getSportActivityInt(builder);

        builder.finish(finish);

        ByteBuffer buf = builder.dataBuffer();
        byte[] array = new byte[buf.remaining()];
        buf.get(array);
        return array;
    }

    @Override
    public Object deserialize(byte[] bytesRead) {
        ByteBuffer buf = ByteBuffer.wrap(bytesRead);
        flatbuf.SportActivityWithOwner sportActivityWithOwner = flatbuf.SportActivityWithOwner.getRootAsSportActivityWithOwner(buf);

        this.name = sportActivityWithOwner.name();
        this.userID = sportActivityWithOwner.userId();
        this.activityID = sportActivityWithOwner.activityId();
        this.workout = sportActivityWithOwner.activity();
        this.startTimestamp = sportActivityWithOwner.startTimestamp();
        this.endTimestamp = sportActivityWithOwner.endTimestamp();
        this.distance = sportActivityWithOwner.distance();
        this.duration = sportActivityWithOwner.duration();
        this.steps = sportActivityWithOwner.steps();
        Polyline polyline = sportActivityWithOwner.startPoint();
        latLng = new LatLng(polyline.lat(), polyline.lon());
        ByteBuffer imgBuffer = sportActivityWithOwner.profilePicAsByteBuffer();
        if(imgBuffer != null){
            byte[] b = new byte[imgBuffer.remaining()];
            imgBuffer.get(b);
            profilePic = b;
        }

        return this;
    }

    public int getSportActivityInt(FlatBufferBuilder builder){
        int activityString = builder.createString(workout);
        int nameString = builder.createString(name);
        int idString = builder.createString(userID);
        int activityIDString = builder.createString(activityID);
        int profPic = 0;
        if(profilePic != null){
            profPic = builder.createByteVector(profilePic);
        }

        flatbuf.SportActivity.startSportActivity(builder);

        flatbuf.SportActivityWithOwner.addActivity(builder, activityString);
        flatbuf.SportActivityWithOwner.addName(builder, nameString);
        flatbuf.SportActivityWithOwner.addActivityId(builder, activityIDString);
        flatbuf.SportActivityWithOwner.addUserId(builder, idString);
        flatbuf.SportActivityWithOwner.addStartTimestamp(builder, startTimestamp);
        flatbuf.SportActivityWithOwner.addEndTimestamp(builder, endTimestamp);
        flatbuf.SportActivityWithOwner.addDuration(builder, duration);
        flatbuf.SportActivityWithOwner.addDistance(builder, distance);
        flatbuf.SportActivityWithOwner.addStartPoint(builder, Polyline.createPolyline(builder, latLng.latitude, latLng.longitude));
        flatbuf.SportActivityWithOwner.addProfilePic(builder, profPic);

        return flatbuf.SportActivityWithOwner.endSportActivityWithOwner(builder);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public byte[] getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(byte[] profilePic) {
        this.profilePic = profilePic;
    }

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
