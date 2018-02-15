package com.traker.shared;

public abstract class AbstractWorkout
{
    protected String workout;
    protected long duration = 0;
    protected int calories;
    protected int type;

    public AbstractWorkout() {}

    public AbstractWorkout(String workout, long duration, int calories, int type)
    {
        this.workout = workout;
        this.duration = duration;
        this.calories = calories;
        this.type = type;
    }

    public String getWorkout() {
        return workout;
    }

    public void setWorkout(String workout) {
        this.workout = workout;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
