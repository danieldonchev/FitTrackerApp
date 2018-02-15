package com.traker.shared;


import com.google.flatbuffers.FlatBufferBuilder;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;

public class Goal implements FlatBufferSerializable<Goal>{
    private UUID id;
    private int type;
    private double distance;
    private long duration;
    private long calories;
    private long steps;
    private long fromDate;
    private long toDate;
    private long lastModified;

    public Goal() {}

    public Goal(UUID id, int type, double distance, long duration, long calories, long steps, long fromDate, long toDate, long lastModified){
        this.id = id;
        this.type = type;
        this.distance = distance;
        this.duration = duration;
        this.calories = calories;
        this.steps = steps;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.lastModified = lastModified;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public byte[] serialize() {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);

        int finish = getGoalInt(builder);
        builder.finish(finish);

        ByteBuffer buf = builder.dataBuffer();
        byte[] array = new byte[buf.remaining()];
        buf.get(array);
        return array;
    }

    @Override
    public Goal deserialize(byte[] bytesRead) {
        ByteBuffer buf = ByteBuffer.wrap(bytesRead);
        flatbuf.Goal goalBufferer = flatbuf.Goal.getRootAsGoal(buf);

        id = UUID.fromString(goalBufferer.id());
        type = goalBufferer.type();
        distance = goalBufferer.distance();
        duration = goalBufferer.duration();
        calories = goalBufferer.calories();
        steps = goalBufferer.steps();
        fromDate = goalBufferer.fromDate();
        toDate = goalBufferer.toDate();
        lastModified = goalBufferer.lastModified();

        return this;
    }

    public int getGoalInt(FlatBufferBuilder builder){
        int id = builder.createString(this.id.toString());

        flatbuf.Goal.startGoal(builder);
        flatbuf.Goal.addId(builder, id);
        flatbuf.Goal.addType(builder, type);
        flatbuf.Goal.addDistance(builder, distance);
        flatbuf.Goal.addDuration(builder, duration);
        flatbuf.Goal.addCalories(builder, calories);
        flatbuf.Goal.addSteps(builder, steps);
        flatbuf.Goal.addFromDate(builder, fromDate);
        flatbuf.Goal.addToDate(builder, toDate);
        flatbuf.Goal.addLastModified(builder, lastModified);
        return flatbuf.Goal.endGoal(builder);
    }

    public int getType() {
        return type;
    }

    public double getDistance() {
        return distance;
    }

    public long getDuration() {
        return duration;
    }

    public long getCalories() {
        return calories;
    }

    public long getSteps() {
        return steps;
    }

    public long getFromDate() {
        return fromDate;
    }

    public long getToDate() {
        return toDate;
    }

    public long getLastModified() {
        return lastModified;
    }
}
