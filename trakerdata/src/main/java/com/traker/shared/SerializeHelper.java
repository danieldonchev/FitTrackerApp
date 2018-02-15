package com.traker.shared;

import com.google.flatbuffers.FlatBufferBuilder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import flatbuf.*;
import flatbuf.Weight;

public class SerializeHelper {

    public static byte[] serializeSportActivities(ArrayList<SportActivity> sportActivities){
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        ListIterator<SportActivity> iterator = sportActivities.listIterator(sportActivities.size());
        int[] sportActivityOffsets = new int[sportActivities.size()];
        int i = 0;
        while(iterator.hasPrevious()){
            SportActivity sportActivity = iterator.previous();
            sportActivityOffsets[i] = sportActivity.getSportActivityInt(builder);
            i++;
        }

        int vector = SportActivities.createSportActivitiesVector(builder, sportActivityOffsets);
        SportActivities.startSportActivities(builder);
        SportActivities.addSportActivities(builder, vector);
        int activities = SportActivities.endSportActivities(builder);

        builder.finish(activities);

        ByteBuffer buf = builder.dataBuffer();
        byte[] array = new byte[buf.remaining()];
        buf.get(array);
        return array;
    }

    public static ArrayList<SportActivity> deserializeSportActivities(byte[] bytes){
        ArrayList<SportActivity> sportActivities = new ArrayList<>();
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        SportActivities sportActivitiesBufferer = SportActivities.getRootAsSportActivities(buf);

        for(int i = 0; i < sportActivitiesBufferer.sportActivitiesLength(); i++){
            flatbuf.SportActivity sportActivity = sportActivitiesBufferer.sportActivities(i);

            flatbuf.SportActivityMap sportActivityMap = sportActivity.sportActivityMap();
            SportActivityMap map = new SportActivityMap();
            if(sportActivityMap != null){
                map.deserializeFromFlatBuffMap(sportActivityMap);
            }

            SportActivity activity = new SportActivity(UUID.fromString(sportActivity.id()),
                    sportActivity.activity(),
                    sportActivity.duration(),
                    sportActivity.distance(),
                    sportActivity.steps(),
                    sportActivity.calories(),
                    sportActivity.startTimestamp(),
                    sportActivity.endTimestamp(),
                    sportActivity.type(),
                    sportActivity.lastModified());

            activity.setSportActivityMap(map);

            Splits splits = sportActivity.splits();
            if(splits != null){
                activity.getSplitsFromFlatBuffSplits(sportActivity.splits());
            }


            sportActivities.add(activity);
        }

        return sportActivities;
    }

    public static byte[] serializeGoals(ArrayList<Goal> goals){
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        ListIterator<Goal> iterator = goals.listIterator(goals.size());
        int[] goalsOffset = new int[goals.size()];
        int i = 0;
        while(iterator.hasPrevious()){
            Goal goal = iterator.previous();
            goalsOffset[i] = goal.getGoalInt(builder);
            i++;
        }

        int vector = Goals.createGoalsVector(builder, goalsOffset);
        Goals.startGoals(builder);
        Goals.addGoals(builder, vector);
        int activities = Goals.endGoals(builder);

        builder.finish(activities);

        ByteBuffer buf = builder.dataBuffer();
        byte[] array = new byte[buf.remaining()];
        buf.get(array);
        return array;
    }

    public static ArrayList<Goal> deserializeGoals(byte[] bytes){
        ArrayList<Goal> goals = new ArrayList<>();
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        Goals goalsBufferer = Goals.getRootAsGoals(buf);

        for(int i = 0; i < goalsBufferer.goalsLength(); i++){
            flatbuf.Goal goal = goalsBufferer.goals(i);

            Goal newGoal = new Goal(UUID.fromString(goal.id()),
                    goal.type(),
                    goal.distance(),
                    goal.duration(),
                    goal.calories(),
                    goal.steps(),
                    goal.fromDate(),
                    goal.toDate(),
                    goal.lastModified());

            goals.add(newGoal);
        }

        return goals;
    }

    public static byte[] serializeSportActivitiesWithOwners(ArrayList<SportActivityWithOwner> sportActivities){
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        ListIterator<SportActivityWithOwner> iterator = sportActivities.listIterator(sportActivities.size());
        int[] sportActivityOffsets = new int[sportActivities.size()];
        int i = 0;
        while(iterator.hasPrevious()){
            SportActivityWithOwner sportActivity = iterator.previous();
            sportActivityOffsets[i] = sportActivity.getSportActivityInt(builder);
            i++;
        }

        int vector = SportActivities.createSportActivitiesVector(builder, sportActivityOffsets);
        SportActivities.startSportActivities(builder);
        SportActivities.addSportActivities(builder, vector);
        int activities = SportActivities.endSportActivities(builder);

        builder.finish(activities);

        ByteBuffer buf = builder.dataBuffer();
        byte[] array = new byte[buf.remaining()];
        buf.get(array);
        return array;
    }

    public static ArrayList<SportActivityWithOwner> deserializeSportActivityWithOwners(byte[] bytes){
        ArrayList<SportActivityWithOwner> sportActivities = new ArrayList<>();
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        flatbuf.SportActivitiesWithOwner sportActivitiesBufferer = flatbuf.SportActivitiesWithOwner.getRootAsSportActivitiesWithOwner(buf);

        for(int i = 0; i < sportActivitiesBufferer.sportActivitiesLength(); i++){
            flatbuf.SportActivityWithOwner sportActivity = sportActivitiesBufferer.sportActivities(i);

            SportActivityWithOwner activity = new SportActivityWithOwner();
            activity.setWorkout(sportActivity.activity());
            activity.setDuration(sportActivity.duration());
            activity.setDistance(sportActivity.distance());
            activity.setSteps(sportActivity.steps());
            activity.setStartTimestamp(sportActivity.startTimestamp());
            activity.setEndTimestamp(sportActivity.endTimestamp());
            activity.setName(sportActivity.name());
            Polyline polyline = sportActivity.startPoint();
            activity.setLatLng(new LatLng(polyline.lat(), polyline.lon()));
            ByteBuffer imgBuffer = sportActivity.profilePicAsByteBuffer();
            if(imgBuffer != null){
                byte[] b = new byte[imgBuffer.remaining()];
                imgBuffer.get(b);
                activity.setProfilePic(b);
            }
            activity.setUserID(sportActivity.userId());
            activity.setActivityID(sportActivity.activityId());


            sportActivities.add(activity);
        }

        return sportActivities;
    }

    public static byte[] serializeWeights(ArrayList<com.traker.shared.Weight> weights){
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        ListIterator<com.traker.shared.Weight> iterator = weights.listIterator(weights.size());
        int[] weightsOffset = new int[weights.size()];
        int i = 0;
        while(iterator.hasPrevious()){
            com.traker.shared.Weight weight = iterator.previous();
            weightsOffset[i] = weight.weightInt(builder);
            i++;
        }

        int vector = Weights.createWeightsVector(builder, weightsOffset);
        Weights.startWeights(builder);
        Weights.addWeights(builder, vector);
        int weightsInt = Weights.endWeights(builder);

        builder.finish(weightsInt);

        ByteBuffer buf = builder.dataBuffer();
        byte[] array = new byte[buf.remaining()];
        buf.get(array);
        return array;
    }

    public static ArrayList<com.traker.shared.Weight> deserializeWeights(byte[] bytes){
        ArrayList<com.traker.shared.Weight> weights = new ArrayList<>();

        ByteBuffer buf = ByteBuffer.wrap(bytes);
        flatbuf.Weights weightsBufferer = flatbuf.Weights.getRootAsWeights(buf);

        for(int i = 0; i < weightsBufferer.weightsLength(); i++){
            flatbuf.Weight weight = weightsBufferer.weights(i);
            weights.add(new com.traker.shared.Weight(weight.weight(), weight.date(), weight.lastModified()));
        }

        return weights;
    }
}
