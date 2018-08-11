package com.daniel.FitTrackerApp.helpers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.daniel.FitTrackerApp.goal.CustomGoal;
import com.daniel.FitTrackerApp.goal.Goal;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.goal.StatsBetweenTime;
import com.daniel.FitTrackerApp.goal.StatsByTime;
import com.daniel.FitTrackerApp.provider.ProviderContract;
import com.daniel.FitTrackerApp.sportactivity.Split;
import com.daniel.FitTrackerApp.sportactivity.SportActivity;
import com.daniel.FitTrackerApp.sportactivity.SportActivitySummariesByTime;
import com.daniel.FitTrackerApp.sportactivity.SportActivitySummary;
import com.daniel.FitTrackerApp.synchronization.SyncHelper;
import com.tracker.shared.Weight;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class DBHelper extends SQLiteOpenHelper {
    //DB NAME
    private static final String DATABASE_NAME = "Traker.db";
    private static final int DATABASE_VERSION = 9;


    private String CREATE_TABLE_ACTIVITIES = "CREATE TABLE " + ProviderContract.SportActivityEntry.TABLE_NAME + " (" +
            ProviderContract.SportActivityEntry._ID + " TEXT PRIMARY KEY," +
            ProviderContract.SportActivityEntry.ACCOUNT_ID + " TEXT," +
            ProviderContract.SportActivityEntry.ACTIVITY + " TEXT," +
            ProviderContract.SportActivityEntry.DISTANCE + " REAL DEFAULT 0," +
            ProviderContract.SportActivityEntry.DURATION + " INTEGER DEFAULT 0," +
            ProviderContract.SportActivityEntry.STEPS + " INTEGER DEFAULT 0," +
            ProviderContract.SportActivityEntry.CALORIES + " INTEGER DEFAULT 0," +
            ProviderContract.SportActivityEntry.MAPDATA + " BLOB, " +
            ProviderContract.SportActivityEntry.START_TIMESTAMP + " INTEGER DEFAULT 0," +
            ProviderContract.SportActivityEntry.END_TIMESTAMP + " INTEGER DEFAULT 0, " +
            ProviderContract.SportActivityEntry.TYPE + " INTEGER DEFAULT 0, " +
            ProviderContract.SportActivityEntry.LAST_MODIFIED + " INTEGER DEFAULT 0," +
            ProviderContract.SportActivityEntry.DELETED + " INTEGER DEFAULT 0," +
            ProviderContract.SportActivityEntry.SYNCED + " INTEGER DEFAULT 0" +
            ");";

    private String CREATE_TABLE_ACTIVITY_SPLITS = "CREATE TABLE " + ProviderContract.SportActivityEntry.SPLIT_TABLE_NAME + "(" +
            ProviderContract.SportActivityEntry.SPLIT_ID + " INTEGER NOT NULL," +
            ProviderContract.SportActivityEntry.SPLIT_ACTIVITY_ID + " TEXT NOT NULL," +
            ProviderContract.SportActivityEntry.SPLIT_ACCOUNT_ID + " TEXT NOT NULL," +
            ProviderContract.SportActivityEntry.SPLIT_DISTANCE + " REAL," +
            ProviderContract.SportActivityEntry.SPLIT_DURATION + " INTEGER, " +
            "PRIMARY KEY (" + ProviderContract.SportActivityEntry.SPLIT_ID + "," + ProviderContract.SportActivityEntry.SPLIT_ACTIVITY_ID  + ")" +
            " );";

    private String CREATE_TABLE_ACCOUNT = "CREATE TABLE " + ProviderContract.AccountEntry.TABLE_NAME + " (" +
            ProviderContract.AccountEntry._ID + " TEXT PRIMARY KEY," +
            ProviderContract.AccountEntry.EMAIL + " TEXT," +
            ProviderContract.AccountEntry.SETTINGS + " TEXT," +
            ProviderContract.AccountEntry.UPDATED + " INTEGER DEFAULT 0);";

    private String CREATE_TABLE_SYNC = "CREATE TABLE " + ProviderContract.SyncEntry.TABLE_NAME + " (" +
            ProviderContract.SyncEntry._ID + " TEXT PRIMARY KEY, " +
            ProviderContract.SyncEntry.LAST_SYNC + " INTEGER DEFAULT 0," +
            ProviderContract.SyncEntry.LAST_MODIFIED + " INTEGER DEFAULT 0," +
            ProviderContract.SyncEntry.LAST_MODIFIED_ACTIVITIES + " INTEGER DEFAULT 0," +
            ProviderContract.SyncEntry.LAST_MODIFIED_SETTINGS + " INTEGER DEFAULT 0," +
            ProviderContract.SyncEntry.LAST_MODIFIED_GOALS + " INTEGER DEFAULT 0," +
            ProviderContract.SyncEntry.LAST_MODIFIED_WEIGHTS + " INTEGER DEFAULT 0" +
            ");";

    private String CREATE_TABLE_GOAL = "CREATE TABLE " + ProviderContract.GoalEntry.TABLE_NAME + " (" +
            ProviderContract.GoalEntry._ID + " TEXT PRIMARY KEY," +
            ProviderContract.GoalEntry.ACCOUNT_ID + " TEXT," +
            ProviderContract.GoalEntry.TYPE + " INTEGER," +
            ProviderContract.GoalEntry.DISTANCE + " REAL," +
            ProviderContract.GoalEntry.DURATION + " INTEGER," +
            ProviderContract.GoalEntry.CALORIES + " INTEGER," +
            ProviderContract.GoalEntry.STEPS + " INTEGER," +
            ProviderContract.GoalEntry.FROM_DATE + " INTEGER," +
            ProviderContract.GoalEntry.TO_DATE + " INTEGER," +
            ProviderContract.SportActivityColumns.LAST_MODIFIED + " INTEGER DEFAULT 0," +
            ProviderContract.SportActivityEntry.DELETED + " INTEGER DEFAULT 0," +
            ProviderContract.SportActivityEntry.SYNCED + " INTEGER DEFAULT 0" + ");";

    private String CREATE_TABLE_WEIGHT = "CREATE TABLE " + ProviderContract.WeightEntry.TABLE_NAME + " (" +
            ProviderContract.WeightEntry.DATE + " INTEGER," +
            ProviderContract.WeightEntry.ACCOUNT_ID + " TEXT," +
            ProviderContract.WeightEntry.WEIGHT + " REAL," +
            ProviderContract.WeightEntry.LAST_MODIFIED + " INTEGER," +
            ProviderContract.WeightEntry.SYNCED + " INTEGER," +
            "PRIMARY KEY (" + ProviderContract.WeightEntry.DATE + "," + ProviderContract.WeightEntry.ACCOUNT_ID + "));";

    private static DBHelper ourInstance;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new DBHelper(context);
        }
        return ourInstance;
    }

    public static synchronized DBHelper getInstance() {
        return ourInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ACTIVITIES);
        db.execSQL(CREATE_TABLE_ACCOUNT);
        db.execSQL(CREATE_TABLE_SYNC);
        db.execSQL(CREATE_TABLE_ACTIVITY_SPLITS);
        db.execSQL(CREATE_TABLE_GOAL);
        db.execSQL(CREATE_TABLE_WEIGHT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.SportActivityEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.AccountEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.SyncEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.SportActivityEntry.SPLIT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.GoalEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.WeightEntry.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.SportActivityEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.AccountEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.SyncEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.SportActivityEntry.SPLIT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.GoalEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProviderContract.WeightEntry.TABLE_NAME);
        onCreate(db);
    }

//    public UUID addActivity(SportActivity sportActivity, String id, Context context, int type) {
//
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(ProviderContract.AccountEntry._ID, sportActivity.getId().toString());
//        contentValues.put(ProviderContract.SportActivityEntry.ACCOUNT_ID, id);
//        contentValues.put(ProviderContract.SportActivityEntry.ACTIVITY, sportActivity.getWorkout());
//        contentValues.put(ProviderContract.SportActivityEntry.DISTANCE, sportActivity.getDistance());
//        contentValues.put(ProviderContract.SportActivityEntry.DURATION, sportActivity.getDuration());
//        contentValues.put(ProviderContract.SportActivityEntry.STEPS, sportActivity.getSteps());
//        contentValues.put(ProviderContract.SportActivityEntry.CALORIES, sportActivity.getCalories());
//        contentValues.put(ProviderContract.SportActivityEntry.MAPDATA, sportActivity.getSportActivityMap().serializeThis());
//        contentValues.put(ProviderContract.SportActivityEntry.START_TIMESTAMP, sportActivity.getStartTimestamp());
//        contentValues.put(ProviderContract.SportActivityEntry.END_TIMESTAMP, sportActivity.getEndTimestamp());
//        contentValues.put(ProviderContract.SportActivityEntry.RECORDED, type);
//        contentValues.put(ProviderContract.SportActivityEntry.LAST_MODIFIED, sportActivity.getLastModified());
//
//        ContentResolver mContentResolver = context.getContentResolver();
//
//        mContentResolver.insert(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(id).build(), contentValues);
//
//        ContentValues[] arrayValues = new ContentValues[sportActivity.getSplits().size()];
//        int i = 0;
//        for (Split split : sportActivity.getSplits()) {
//            arrayValues[i] = new ContentValues();
//            arrayValues[i].put(ProviderContract.SportActivityEntry.SPLIT_ID, split.getId());
//            arrayValues[i].put(ProviderContract.SportActivityEntry.SPLIT_ACTIVITY_ID, sportActivity.getId().toString());
//            arrayValues[i].put(ProviderContract.SportActivityEntry.SPLIT_ACCOUNT_ID, id);
//            arrayValues[i].put(ProviderContract.SportActivityEntry.SPLIT_DISTANCE, split.getDistance());
//            arrayValues[i].put(ProviderContract.SportActivityEntry.SPLIT_DURATION, split.getDuration());
//            i++;
//        }
//        mContentResolver.bulkInsert(ProviderContract.SportActivityEntry.SPLIT_CONTENT_URI.buildUpon().appendPath(sportActivity.getId().toString()).appendPath(id).build(), arrayValues);
//        updateLastModifiedTime(context, id, ProviderContract.SyncEntry.LAST_MODIFIED_ACTIVITIES, System.currentTimeMillis());
//        return sportActivity.getId();
//    }

    public UUID addActivity(com.tracker.shared.SportActivity sportActivity, String id, Context context, int synced, int type){

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProviderContract.AccountEntry._ID, sportActivity.getId().toString());
        contentValues.put(ProviderContract.SportActivityEntry.ACCOUNT_ID, id);
        contentValues.put(ProviderContract.SportActivityEntry.ACTIVITY, sportActivity.getWorkout());
        contentValues.put(ProviderContract.SportActivityEntry.DISTANCE, sportActivity.getDistance());
        contentValues.put(ProviderContract.SportActivityEntry.DURATION, sportActivity.getDuration());
        contentValues.put(ProviderContract.SportActivityEntry.STEPS, sportActivity.getSteps());
        contentValues.put(ProviderContract.SportActivityEntry.CALORIES, sportActivity.getCalories());
        contentValues.put(ProviderContract.SportActivityEntry.MAPDATA, sportActivity.getSportActivityMap().serialize());
        contentValues.put(ProviderContract.SportActivityEntry.START_TIMESTAMP, sportActivity.getStartTimestamp());
        contentValues.put(ProviderContract.SportActivityEntry.END_TIMESTAMP, sportActivity.getEndTimestamp());
        contentValues.put(ProviderContract.SportActivityEntry.TYPE, type);
        contentValues.put(ProviderContract.SportActivityEntry.LAST_MODIFIED, sportActivity.getLastModified());
        contentValues.put(ProviderContract.SportActivityEntry.SYNCED, synced);

        ContentResolver mContentResolver = context.getContentResolver();

        mContentResolver.insert(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(id).build(), contentValues);
        addSplits(sportActivity.getId().toString(), id, sportActivity.getSplits(), mContentResolver);

        updateLastModifiedTime(context, id, ProviderContract.SyncEntry.LAST_MODIFIED_ACTIVITIES, System.currentTimeMillis());
        return sportActivity.getId();
    }

    public ArrayList<SportActivitySummary> filteredActivities(Context context, String userID, String activityID ,long startTimestamp, long endTimestamp){
        ArrayList<SportActivitySummary> activities = new ArrayList<>();
        String[] projection = {
                ProviderContract.SportActivityEntry._ID,
                ProviderContract.SportActivityEntry.DISTANCE,
                ProviderContract.SportActivityEntry.DURATION,
                ProviderContract.SportActivityEntry.START_TIMESTAMP,
                ProviderContract.SportActivityEntry.END_TIMESTAMP};

        String where = "((? BETWEEN " + ProviderContract.SportActivityEntry.START_TIMESTAMP + " AND " + ProviderContract.SportActivityEntry.END_TIMESTAMP + ") AND " +
                "(? BETWEEN " + ProviderContract.SportActivityEntry.START_TIMESTAMP + " AND " + ProviderContract.SportActivityEntry.END_TIMESTAMP + ")) AND " +
                ProviderContract.SportActivityEntry._ID + "<> ? AND " + ProviderContract.SportActivityEntry.DELETED + "=0";

        String[] selectArgs = {String.valueOf(startTimestamp),
                                String.valueOf(endTimestamp),
                                activityID};

        Cursor c =  context.getContentResolver().query(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(userID).build(),
                projection, where, selectArgs, null);

        ArrayList<SportActivitySummary> summaries = new ArrayList<>();

        //BETWEEN ACTIVITY
        if(c.moveToFirst()){
            SportActivitySummary summary =  new SportActivitySummary(UUID.fromString(c.getString(0)));
            summary.setDistance(c.getDouble(1));
            summary.setDuration(c.getLong(2));
            summary.setStartTimeStamp(c.getLong(3));
            summary.setEndTimeStamp(c.getLong(4));
            activities.add(summary);
        } else {
            c.close();
            String whereOverlap = "((? BETWEEN " + ProviderContract.SportActivityEntry.START_TIMESTAMP + " AND " + ProviderContract.SportActivityEntry.END_TIMESTAMP + ") OR " +
                    "(? BETWEEN " + ProviderContract.SportActivityEntry.START_TIMESTAMP + " AND " + ProviderContract.SportActivityEntry.END_TIMESTAMP + ")) AND " +
                    ProviderContract.SportActivityEntry._ID + "<> ? AND " + ProviderContract.SportActivityEntry.DELETED + "=0";
            c =  context.getContentResolver().query(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(userID).build(),
                    projection, whereOverlap, selectArgs, null);

            //OVERLAPS START OR END TIME OF ACTIVITY
            if(c.moveToFirst()){
                do{
                    SportActivitySummary summary =  new SportActivitySummary(UUID.fromString(c.getString(0)));
                    summary.setDistance(c.getDouble(1));
                    summary.setDuration(c.getLong(2));
                    summary.setStartTimeStamp(c.getLong(3));
                    summary.setEndTimeStamp(c.getLong(4));
                    activities.add(summary);
                } while (c.moveToNext());
            }

            c.close();

            String whereOldBetween = "(" + ProviderContract.SportActivityEntry.START_TIMESTAMP + " BETWEEN " + " ? AND ?) AND (" +
                    ProviderContract.SportActivityEntry.END_TIMESTAMP + " BETWEEN " + " ? AND ?) AND " +
                    ProviderContract.SportActivityEntry._ID + "<> ? AND " + ProviderContract.SportActivityEntry.DELETED + "=0";
            String[] selectArgsOldBetween = {String.valueOf(startTimestamp),
                    String.valueOf(endTimestamp),
                    String.valueOf(startTimestamp),
                    String.valueOf(endTimestamp),
                    activityID};

            c =  context.getContentResolver().query(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(userID).build(),
                    projection, whereOldBetween, selectArgsOldBetween, null);

            //OLD ACTIVITY INSIDE NEW ACTIVITY
            if(c.moveToFirst()){
                do{
                    SportActivitySummary summary =  new SportActivitySummary(UUID.fromString(c.getString(0)));
                    summary.setDistance(c.getDouble(1));
                    summary.setDuration(c.getLong(2));
                    summary.setStartTimeStamp(c.getLong(3));
                    summary.setEndTimeStamp(c.getLong(4));
                    activities.add(summary);
                } while (c.moveToNext());
            }

            c.close();
        }

        return activities;
    }

    public int addSplits(String sportActivityID, String userID, List<com.tracker.shared.Split> splits, ContentResolver contentResolver){

        try {
            if(splits != null){
                ContentValues[] arrayValues = new ContentValues[splits.size()];
                int i = 0;
                for (com.tracker.shared.Split split : splits) {
                    arrayValues[i] = new ContentValues();
                    arrayValues[i].put(ProviderContract.SportActivityEntry.SPLIT_ID, split.getId());
                    arrayValues[i].put(ProviderContract.SportActivityEntry.SPLIT_ACTIVITY_ID, sportActivityID);
                    arrayValues[i].put(ProviderContract.SportActivityEntry.SPLIT_ACCOUNT_ID, userID);
                    arrayValues[i].put(ProviderContract.SportActivityEntry.SPLIT_DISTANCE, split.getDistance());
                    arrayValues[i].put(ProviderContract.SportActivityEntry.SPLIT_DURATION, split.getDuration());
                    i++;
                }
                contentResolver.bulkInsert(ProviderContract.SportActivityEntry.SPLIT_CONTENT_URI.buildUpon().appendPath(sportActivityID).appendPath(userID).build(), arrayValues);
            }
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public ArrayList<Split> getActivitySplits(Context context, String activityID, String userID) {
        ArrayList<Split> splits = new ArrayList<>();
        boolean isMetric = PreferencesHelper.getInstance().isMetric(context);
        String[] projection = {ProviderContract.SportActivityEntry.SPLIT_ID,
                ProviderContract.SportActivityEntry.SPLIT_ACTIVITY_ID,
                ProviderContract.SportActivityEntry.SPLIT_ACCOUNT_ID,
                ProviderContract.SportActivityEntry.SPLIT_DURATION,
                ProviderContract.SportActivityEntry.SPLIT_DISTANCE};

        ContentResolver mContentResolver = context.getContentResolver();
        Cursor c = mContentResolver.query(ProviderContract.SportActivityEntry.SPLIT_CONTENT_URI.buildUpon().appendPath(activityID.toString()).appendPath(userID).build(), projection, null, null, null);
        if (c.moveToFirst()) {
            do {
                Split split = new Split(c.getInt(0));
                split.setDuration(c.getLong(3));
                split.setDistance(c.getDouble(4));
                split.calculateFinalData(isMetric, split.getDistance());
                splits.add(split);
            } while (c.moveToNext());
        }

        return splits;
    }


    public long addAccount(Context context, String email, String id, boolean isNew) {
        SQLiteDatabase db = getWritableDatabase();
        PreferencesHelper.getInstance().setDefaults(context);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProviderContract.AccountEntry._ID, id);
        contentValues.put(ProviderContract.AccountEntry.EMAIL, email);
        contentValues.put(ProviderContract.AccountEntry.SETTINGS, PreferencesHelper.getInstance().getSettings(context));

        long recordID = db.insert(ProviderContract.AccountEntry.TABLE_NAME, null, contentValues);
        insertSyncEntry(context, id);
        PreferencesHelper.getInstance().setPreference(context.getString(R.string.id_key), id);
        db.close();
        if(!isNew){
            SyncHelper.requestManualSync(context, isNew);
        } else {
            SyncHelper.cancelSync(context);
        }

        return recordID;
    }

    public int updateAccountSettings(Context context, String id, String settings) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProviderContract.AccountEntry.SETTINGS, settings);
        String clause = ProviderContract.AccountEntry._ID + "=?";
        db.update(ProviderContract.AccountEntry.TABLE_NAME, contentValues, clause, new String[]{id});
        db.close();
        return -1;
    }

    public void updateAccountDetailsStatus(Context context) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProviderContract.AccountEntry.UPDATED, 1);
        String id = PreferencesHelper.getInstance().getCurrentUserId(context);
        db.update(ProviderContract.AccountEntry.TABLE_NAME, contentValues, ProviderContract.AccountEntry._ID + "=?", new String[]{id});
        db.close();
    }

    public void findAndSetAccount(Context context, String email, String id, boolean isNew) {

        String settings = getAccountSettings(id);
        if(settings != null){
            PreferencesHelper.getInstance().setCurrentAccountSettings(context, settings);
            PreferencesHelper.getInstance().setDefaultAppData(context);
            Account[] accounts = AccountManager.get(context).getAccounts();
            for(Account account : accounts){
                if(account.name.equals(email)){
                    PreferencesHelper.getInstance().setPreference(context.getString(R.string.id_key), AccountManager.get(context).getUserData(account, "id"));
                }
            }
            if(!isNew){
                SyncHelper.requestManualSync(context, false);
            }
        } else {
            addAccount(context, email, id, isNew);
        }

    }

    public ArrayList<SportActivitySummary> getActivitiesSummary(Context context, int limit, int count, String userId, String orderBy, String order) {
        ArrayList<SportActivitySummary> activitySummaries = new ArrayList<>();
        boolean isMetric = PreferencesHelper.getInstance().isMetric(context);

        String[] PROJECTION = {
                ProviderContract.SportActivityEntry._ID,
                ProviderContract.SportActivityEntry.DISTANCE,
                ProviderContract.SportActivityEntry.DURATION,
                ProviderContract.SportActivityEntry.START_TIMESTAMP,
                ProviderContract.SportActivityEntry.END_TIMESTAMP,
                ProviderContract.SportActivityEntry.TYPE,
                ProviderContract.SportActivityEntry.ACTIVITY
        };

        ContentResolver mContentResolver = context.getContentResolver();
        String sort = orderBy + " " + order + " limit " + String.valueOf(count) + " OFFSET " + String.valueOf(limit);
        Cursor c = mContentResolver.query(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(userId).build(), PROJECTION, ProviderContract.SportActivityEntry.ACCOUNT_ID + "=? AND " + ProviderContract.SportActivityEntry.DELETED + "=0", new String[]{userId}, sort);

        if (c.moveToFirst()) {
            do {
                SportActivitySummary recordDataSummary = new SportActivitySummary(
                        UUID.fromString(c.getString(0)),
                        c.getDouble(1),
                        c.getLong(2),
                        c.getLong(3),
                        c.getLong(4),
                        PreferencesHelper.getInstance().isMetric(context));
                recordDataSummary.setType(c.getInt(5));
                recordDataSummary.setWorkout(c.getString(6));
                activitySummaries.add(recordDataSummary);
            } while (c.moveToNext());
        }

        return activitySummaries;
    }

    public ArrayList<SportActivitySummariesByTime> getActivitiesSummaryByTime(Context context, int time, int limit, int count, String userId, String orderBy, String order){
        ArrayList<SportActivitySummariesByTime> activitySummaries = new ArrayList<>();
        boolean isMetric = PreferencesHelper.getInstance().isMetric(context);
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();
        int offset = timeZone.getRawOffset();

        SQLiteDatabase db = getReadableDatabase();
        StringBuilder statement = new StringBuilder();
        String timePeriod = null;
        if(time == Calendar.YEAR){
            timePeriod = "Y";
        } else if(time == Calendar.MONTH){
            timePeriod = "m";
            statement.append( "SELECT "+
                    "sum(" + ProviderContract.SportActivityEntry.DISTANCE + ") as " + ProviderContract.SportActivityEntry.DISTANCE +  "," +
                    "sum(" + ProviderContract.SportActivityEntry.DURATION + ") as " + ProviderContract.SportActivityEntry.DURATION +  "," +
                    "sum(" + ProviderContract.SportActivityEntry.CALORIES + ") as " + ProviderContract.SportActivityEntry.CALORIES +  "," +
                    "sum(" + ProviderContract.SportActivityEntry.STEPS + ") as " + ProviderContract.SportActivityEntry.STEPS +  "," +
                    "strftime(" + DatabaseUtils.sqlEscapeString("%" + timePeriod) + ",((" + ProviderContract.SportActivityEntry.START_TIMESTAMP + "+" + String.valueOf(offset) + ")/1000)," + DatabaseUtils.sqlEscapeString("unixepoch") + ") as time," +
                    "strftime(" + DatabaseUtils.sqlEscapeString("%Y") + ",((" + ProviderContract.SportActivityEntry.START_TIMESTAMP + "+" + String.valueOf(offset) +  ")/1000)," + DatabaseUtils.sqlEscapeString("unixepoch") + ") as time_of_year" +
                    " FROM " + ProviderContract.SportActivityEntry.TABLE_NAME +
                    " where " + ProviderContract.SportActivityEntry.ACCOUNT_ID + "=?" +
                    " group by time, time_of_year" +
                    " order by " + orderBy + " " + order +
                    " limit " + String.valueOf(count) + " OFFSET " + String.valueOf(limit) +
                    ";");
        } else if(time == Calendar.WEEK_OF_YEAR){
            statement.append("SELECT " +
                    "sum(" + ProviderContract.SportActivityEntry.DISTANCE + ") as " + ProviderContract.SportActivityEntry.DISTANCE +  "," +
                    "sum(" + ProviderContract.SportActivityEntry.DURATION + ") as " + ProviderContract.SportActivityEntry.DURATION +  "," +
                    "sum(" + ProviderContract.SportActivityEntry.CALORIES + ") as " + ProviderContract.SportActivityEntry.CALORIES +  "," +
                    "sum(" + ProviderContract.SportActivityEntry.STEPS + ") as " + ProviderContract.SportActivityEntry.STEPS +  "," +
                    "date((" + ProviderContract.SportActivityEntry.START_TIMESTAMP + "+" + String.valueOf(offset) +  ")/1000," + DatabaseUtils.sqlEscapeString("unixepoch") + "," + DatabaseUtils.sqlEscapeString("-6 days") + "," + DatabaseUtils.sqlEscapeString("weekday 1") + ") as start_week_date,"  +
                    "date((" + ProviderContract.SportActivityEntry.START_TIMESTAMP + "+" + String.valueOf(offset) +  ")/1000," + DatabaseUtils.sqlEscapeString("unixepoch") + "," + DatabaseUtils.sqlEscapeString("0 days") + "," + DatabaseUtils.sqlEscapeString("weekday 0") + ")"  +
                    " FROM " + ProviderContract.SportActivityEntry.TABLE_NAME +
                    " where " + ProviderContract.SportActivityEntry.ACCOUNT_ID + "=?" +
                    " group by start_week_date" +
                    " order by " + orderBy + " " + order +
                    " limit " + String.valueOf(count) + " OFFSET " + String.valueOf(limit) +
                    ";");
        }

        String[] selectionsArgs = {userId};
        Cursor c = db.rawQuery(statement.toString(), selectionsArgs);
        if (c.moveToFirst()) {
            do {
                SportActivitySummariesByTime sportActivitySummary = null;
                if(time == Calendar.MONTH){
                    String month = c.getString(4);
                    String year = c.getString(5);
                    DateFormat format = new SimpleDateFormat("yyyy-dd-MM");
                    String currentDate = year + "-" + "01-" + month;
                    Date date = null;
                    try {
                        date = format.parse(currentDate);
                        sportActivitySummary = new SportActivitySummariesByTime(
                                c.getDouble(0),
                                c.getLong(1),
                                c.getLong(2),
                                c.getLong(3),
                                date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if(time == Calendar.WEEK_OF_YEAR){
                    Date startDate;
                    Date endDate;
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        String start = c.getString(4);
                        String end = c.getString(5);
                        startDate = format.parse(c.getString(4));
                        endDate = format.parse(c.getString(5));
                        sportActivitySummary = new SportActivitySummariesByTime(
                                c.getDouble(0),
                                c.getLong(1),
                                c.getLong(2),
                                c.getLong(3),
                                startDate,
                                endDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                activitySummaries.add(sportActivitySummary);
            } while (c.moveToNext());
        }

        return activitySummaries;
    }

    public ArrayList<SportActivitySummary> getSportActivitySummariesBetween(Context context, int limit, int count, String userId, String orderBy, String order, Date fromDate, Date toDate) {
        ArrayList<SportActivitySummary> sportActivities = new ArrayList<>();
        boolean isMetric = PreferencesHelper.getInstance().isMetric(context);

        String[] PROJECTION = {
                ProviderContract.SportActivityEntry._ID,
                ProviderContract.SportActivityEntry.DISTANCE,
                ProviderContract.SportActivityEntry.DURATION,
                ProviderContract.SportActivityEntry.START_TIMESTAMP,
                ProviderContract.SportActivityEntry.END_TIMESTAMP,
                ProviderContract.SportActivityEntry.TYPE,
                ProviderContract.SportActivityEntry.ACTIVITY,
                ProviderContract.SportActivityEntry.CALORIES

        };

        String where = ProviderContract.SportActivityEntry.DELETED + "=0 AND " +
                        ProviderContract.SportActivityEntry.START_TIMESTAMP + " between ? and ?";
        String[] wheres = {String.valueOf(fromDate.getTime()), String.valueOf(toDate.getTime())};

        ContentResolver mContentResolver = context.getContentResolver();
        String sort = orderBy + " " + order + " limit " + String.valueOf(count) + " OFFSET " + String.valueOf(limit);
        Cursor c = mContentResolver.query(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(userId).build(), PROJECTION, where, wheres, sort);

        if (c.moveToFirst()) {
            do {
                SportActivitySummary recordDataSummary = new SportActivitySummary(
                        UUID.fromString(c.getString(0)),
                        c.getDouble(1),
                        c.getLong(2),
                        c.getLong(3),
                        c.getLong(4),
                        PreferencesHelper.getInstance().isMetric(context));
                recordDataSummary.setType(c.getInt(5));
                recordDataSummary.setWorkout(c.getString(6));
                recordDataSummary.setCalories(c.getInt(7));
                sportActivities.add(recordDataSummary);
            } while (c.moveToNext());
        }

        return sportActivities;
    }

    public ArrayList<SportActivitySummary> getSportActivitySummariesMonth(Context context, int limit, int count, String userId, String orderBy, String order, Date fromDate) {
        ArrayList<SportActivitySummary> sportActivities = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();
        int offset = timeZone.getRawOffset();

        String[] projection = {
                ProviderContract.SportActivityEntry._ID,
                ProviderContract.SportActivityEntry.DISTANCE,
                ProviderContract.SportActivityEntry.DURATION,
                ProviderContract.SportActivityEntry.START_TIMESTAMP,
                ProviderContract.SportActivityEntry.END_TIMESTAMP,
                ProviderContract.SportActivityEntry.CALORIES,
                "strftime(" + DatabaseUtils.sqlEscapeString("%m") + ",((" + ProviderContract.SportActivityEntry.START_TIMESTAMP + "+" + String.valueOf(offset) + ")/1000)," + DatabaseUtils.sqlEscapeString("unixepoch") + ") as time_month",
                "strftime(" + DatabaseUtils.sqlEscapeString("%Y") + ",((" + ProviderContract.SportActivityEntry.START_TIMESTAMP + "+" + String.valueOf(offset) + ")/1000)," + DatabaseUtils.sqlEscapeString("unixepoch") + ") as time_year"};


        calendar.setTimeInMillis(fromDate.getTime());
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        String where = ProviderContract.SportActivityEntry.DELETED + "=0 AND time_month=? AND time_year=?";
        String[] wheres = {"0" + String.valueOf(month), String.valueOf(year)};

        ContentResolver mContentResolver = context.getContentResolver();
        String sort = orderBy + " " + order + " limit " + String.valueOf(count) + " OFFSET " + String.valueOf(limit);
        Cursor c = mContentResolver.query(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(userId).build(), projection, where, wheres, sort);

        if (c.moveToFirst()) {
            do {
                SportActivitySummary recordDataSummary = new SportActivitySummary(
                        UUID.fromString(c.getString(0)),
                        c.getDouble(1),
                        c.getLong(2),
                        c.getLong(3),
                        c.getLong(4),
                        PreferencesHelper.getInstance().isMetric(context));
                recordDataSummary.setCalories(c.getInt(5));
                sportActivities.add(recordDataSummary);
            } while (c.moveToNext());
        }

        return sportActivities;
    }

    public com.daniel.FitTrackerApp.sportactivity.SportActivity getActivity(Context context, UUID id, String userID) {
        SportActivity sportActivity = null;
        boolean isMetric = PreferencesHelper.getInstance().isMetric(context);
        Cursor c = context.getContentResolver().query(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(id.toString()).appendPath(userID).build(), ProviderContract.SportActivityEntry.FULL_PROJECTION, null, null, null);

        int parameterIndex = 0;
        if (c.moveToFirst()) {
            sportActivity = new SportActivity(UUID.fromString(c.getString(parameterIndex++)), c.getString(parameterIndex++));
            sportActivity.setDistance(c.getDouble(parameterIndex++));
            sportActivity.setDuration(c.getInt(parameterIndex++));
            sportActivity.setSteps(c.getInt(parameterIndex++));
            sportActivity.setCalories(c.getInt(parameterIndex++));
            sportActivity.getSportActivityMap().deserialize(c.getBlob(parameterIndex++));
            sportActivity.setStartTimestamp(c.getLong(parameterIndex++));
            sportActivity.setEndTimestamp(c.getLong(parameterIndex++));
            sportActivity.setLastModified(c.getLong(parameterIndex++));

            sportActivity.calculateFinalData(context,
                    isMetric,
                    PreferencesHelper.getInstance().getCurrentUserGender(context),
                    PreferencesHelper.getInstance().getCurrentUserHeight(context),
                    PreferencesHelper.getInstance().getCurrentUserWeight(context),
                    PreferencesHelper.getInstance().getCurrentUserAge(context),
                    sportActivity.getDistance());
        }

        return sportActivity;
    }

    public int updateSportActivityTime(Context context, String userID, String activityID, ContentValues values) {
        context.getContentResolver().update(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(activityID).appendPath(userID).build(),
                values, null, null);

        return 0;
    }

    public int updateSportActivity(Context context, String userID, com.tracker.shared.SportActivity sportActivity, int synced) {

        long timestamp = System.currentTimeMillis();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProviderContract.AccountEntry._ID, sportActivity.getId().toString());
        contentValues.put(ProviderContract.SportActivityEntry.ACCOUNT_ID, userID);
        contentValues.put(ProviderContract.SportActivityEntry.DISTANCE, sportActivity.getDistance());
        contentValues.put(ProviderContract.SportActivityEntry.DURATION, sportActivity.getDuration());
        contentValues.put(ProviderContract.SportActivityEntry.STEPS, sportActivity.getSteps());
        contentValues.put(ProviderContract.SportActivityEntry.START_TIMESTAMP, sportActivity.getStartTimestamp());
        contentValues.put(ProviderContract.SportActivityEntry.END_TIMESTAMP, sportActivity.getEndTimestamp());
        contentValues.put(ProviderContract.SportActivityEntry.SYNCED, synced);
        contentValues.put(ProviderContract.SportActivityEntry.LAST_MODIFIED, timestamp);

        context.getContentResolver().update(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(sportActivity.getId().toString()).appendPath(userID).build(),
                contentValues, null, null);

        updateLastModifiedTime(context, userID, ProviderContract.SyncEntry.LAST_MODIFIED_ACTIVITIES, timestamp);
        return 0;
    }

    public boolean isCurrentAccountUpdated(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {ProviderContract.AccountEntry.UPDATED};
        String selection = ProviderContract.AccountEntry.EMAIL + "=?";
        String[] selectArgs = {email};

        Cursor c = db.query(ProviderContract.AccountEntry.TABLE_NAME, columns, selection, selectArgs, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            return c.getInt(0) > 0;
        }

        return false;
    }

    private long insertSyncEntry(Context context, String userID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProviderContract.SyncEntry._ID, userID);
        contentValues.put(ProviderContract.SyncEntry.LAST_SYNC, 0);
        contentValues.put(ProviderContract.SyncEntry.LAST_MODIFIED_ACTIVITIES, 0);

        ContentResolver mContentResolver = context.getContentResolver();
        mContentResolver.insert(ProviderContract.SyncEntry.CONTENT_URI, contentValues);

        return 0;
    }

    public long updateLastModifiedTime(Context context, String userID, String syncEntry, long timestamp){
        ContentValues contentValues = new ContentValues();
        contentValues.put(syncEntry, timestamp);
        contentValues.put(ProviderContract.SyncEntry.LAST_MODIFIED, timestamp);
        String where = ProviderContract.SyncEntry._ID + "=?";
        String[] values = {userID};
        context.getContentResolver().update(ProviderContract.SyncEntry.CONTENT_URI.buildUpon().appendPath(userID).build(),
                contentValues, where, values);

        return 0;
    }

    public long updateLastSync(Context context, String userID, long timestamp){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProviderContract.SyncEntry.LAST_SYNC, timestamp);
        String where = ProviderContract.SyncEntry._ID + "=?";
        String[] values = {userID};
        context.getContentResolver().update(ProviderContract.SyncEntry.CONTENT_URI.buildUpon().appendPath(userID).build(),
                contentValues, where, values);

        return 0;
    }

    public long getLastModifiedTime(Context context, String userID, String syncEntry){
        String[] columns = {syncEntry};

        Cursor c = context.getContentResolver().query(ProviderContract.SyncEntry.CONTENT_URI.buildUpon().appendPath(userID).build(),
                columns, null, null, null);
        if (c.moveToFirst()) {
            return c.getLong(0);
        }

        return -1;
    }

    public JSONObject getLastModifiedTimes(Context context, String userID){
        JSONObject jsonObject = new JSONObject();
        String[] projection = {ProviderContract.SyncEntry.LAST_MODIFIED_ACTIVITIES,
                                ProviderContract.SyncEntry.LAST_MODIFIED_SETTINGS,
                                ProviderContract.SyncEntry.LAST_MODIFIED_GOALS,
                                ProviderContract.SyncEntry.LAST_MODIFIED_WEIGHTS};

        Cursor c = context.getContentResolver().query(ProviderContract.SyncEntry.CONTENT_URI.buildUpon().appendPath(userID).build(),
                projection, null, null, null);

        if(c.moveToFirst()){
            try {
                jsonObject.put(ProviderContract.SyncEntry.LAST_MODIFIED_ACTIVITIES, c.getLong(0));
                jsonObject.put(ProviderContract.SyncEntry.LAST_MODIFIED_SETTINGS, c.getLong(1));
                jsonObject.put(ProviderContract.SyncEntry.LAST_MODIFIED_GOALS, c.getLong(2));
                jsonObject.put(ProviderContract.SyncEntry.LAST_MODIFIED_WEIGHTS, c.getLong(3));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonObject;
    }

    public String getAccountSettings(String id){
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + ProviderContract.AccountEntry.SETTINGS + " FROM " +
                ProviderContract.AccountEntry.TABLE_NAME + " WHERE " +
                ProviderContract.AccountEntry._ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{id});
        String settings = null;

        if(cursor.moveToFirst()){
            settings = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return settings;
    }

    public int addGoal(Context context, String userID, com.tracker.shared.Goal goal, int synced){

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProviderContract.GoalEntry._ID, goal.getId().toString());
        contentValues.put(ProviderContract.GoalEntry.ACCOUNT_ID, userID);
        contentValues.put(ProviderContract.GoalEntry.TYPE, goal.getType());
        contentValues.put(ProviderContract.GoalEntry.DISTANCE, goal.getDistance());
        contentValues.put(ProviderContract.GoalEntry.DURATION, goal.getDuration());
        contentValues.put(ProviderContract.GoalEntry.CALORIES, goal.getCalories());
        contentValues.put(ProviderContract.GoalEntry.STEPS, goal.getSteps());
        contentValues.put(ProviderContract.GoalEntry.FROM_DATE, goal.getFromDate());
        contentValues.put(ProviderContract.GoalEntry.FROM_DATE, goal.getToDate());
        contentValues.put(ProviderContract.GoalEntry.LAST_MODIFIED, goal.getLastModified());
        contentValues.put(ProviderContract.GoalEntry.SYNCED, synced);

        context.getContentResolver().insert(ProviderContract.GoalEntry.CONTENT_URI.buildUpon().appendPath(userID).build(), contentValues);
        updateLastModifiedTime(context, userID, ProviderContract.SyncEntry.LAST_MODIFIED_GOALS, System.currentTimeMillis());

        return 0;
    }



    public int updateGoalTime(Context context, String userID, String goalID, ContentValues values) {

        context.getContentResolver().update(ProviderContract.GoalEntry.CONTENT_URI.buildUpon().appendPath(goalID).appendPath(userID).build(),
                values, null, null);

        return 0;
    }



    public ArrayList<Goal> getGoals(Context context, String userID){
        ArrayList<Goal> goals = new ArrayList<>();
        String[] projection = {ProviderContract.GoalEntry._ID,
                                ProviderContract.GoalEntry.TYPE,
                                ProviderContract.GoalEntry.DISTANCE,
                                ProviderContract.GoalEntry.DURATION,
                                ProviderContract.GoalEntry.CALORIES,
                                ProviderContract.GoalEntry.STEPS,
                                ProviderContract.GoalEntry.FROM_DATE,
                                ProviderContract.GoalEntry.TO_DATE};

        Cursor c = context.getContentResolver().query(ProviderContract.GoalEntry.CONTENT_URI.buildUpon().appendPath(userID).build(), projection, null, null, null);

        if(c.moveToFirst()){
            do {
                Goal goal;
                int type = c.getInt(1);
                if(type != Goal.CUSTOM){
                    goal = new Goal(UUID.fromString(c.getString(0)),
                            type,
                            c.getDouble(2),
                            c.getLong(3),
                            c.getLong(4),
                            c.getLong(5));
                } else {
                    goal = new CustomGoal(UUID.fromString(c.getString(0)),
                            c.getDouble(2),
                            c.getLong(3),
                            c.getLong(4),
                            c.getLong(5),
                            c.getLong(6),
                            c.getLong(7));
                }
                goals.add(goal);
            } while (c.moveToNext());
        }

        return goals;
    }

    public StatsByTime getStats(Context context, String userID){
        StatsByTime stats = new StatsByTime();
        Calendar cal = Calendar.getInstance();
        TimeZone timeZone = cal.getTimeZone();
        int offset = timeZone.getRawOffset();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        StringBuilder dailyBuilder = new StringBuilder();
        StringBuilder weeklyBuilder = new StringBuilder();
        StringBuilder monthlyBuilder = new StringBuilder();

        String state = "SELECT " +
                "sum(" + ProviderContract.SportActivityEntry.DISTANCE + ")," +
                "sum(" + ProviderContract.SportActivityEntry.DURATION + ")," +
                "sum(" + ProviderContract.SportActivityEntry.CALORIES + ")," +
                "sum(" + ProviderContract.SportActivityEntry.STEPS + ") FROM " + ProviderContract.SportActivityEntry.TABLE_NAME +
                " where " +
                ProviderContract.SportActivityEntry.DELETED + "=0 AND " +
                ProviderContract.SportActivityEntry.ACCOUNT_ID + "=" + DatabaseUtils.sqlEscapeString(userID) + " AND " +
                ProviderContract.SportActivityEntry.START_TIMESTAMP + ">";

        dailyBuilder.append(state);
        weeklyBuilder.append(state);
        monthlyBuilder.append(state);

        long dailyTime = cal.getTimeInMillis();
        dailyBuilder.append(String.valueOf(dailyTime));
        int day = cal.get(Calendar.DAY_OF_MONTH);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c1 = db.rawQuery(dailyBuilder.toString(), null);
        if(c1.moveToFirst()){
            do {
                stats.daily.distance = c1.getDouble(0);
                stats.daily.duration = c1.getLong(1);
                stats.daily.calories = c1.getLong(2);
                stats.daily.steps = c1.getLong(3);
            } while(c1.moveToNext());
        }
        c1.close();
        db.close();
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
        {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        int daye = cal.get(Calendar.DAY_OF_MONTH);
        long weekTime = cal.getTimeInMillis();
        weeklyBuilder.append(String.valueOf(weekTime));
        db = getReadableDatabase();
        Cursor c2 = db.rawQuery(weeklyBuilder.toString(), null);
        if(c2.moveToFirst()){
            do {
                stats.weekly.distance = c2.getDouble(0);
                stats.weekly.duration = c2.getLong(1);
                stats.weekly.calories = c2.getLong(2);
                stats.weekly.steps = c2.getLong(3);
            } while(c2.moveToNext());
        }
        c2.close();
        db.close();
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        long monthlyTime = cal.getTimeInMillis() + offset;
        monthlyBuilder.append(String.valueOf(monthlyTime));
        db = getReadableDatabase();
        Cursor c3 = db.rawQuery(monthlyBuilder.toString(), null);
        if(c3.moveToFirst()){
            do {
                stats.monthly.distance = c3.getDouble(0);
                stats.monthly.duration = c3.getLong(1);
                stats.monthly.calories = c3.getLong(2);
                stats.monthly.steps = c3.getLong(3);
            } while(c3.moveToNext());
        }
        c3.close();
        db.close();
        return stats;
    }

    public StatsBetweenTime getStatsBetweenTime(Context context, String userId, long fromDate, long toDate) {
        StatsBetweenTime stats = new StatsBetweenTime();
        boolean isMetric = PreferencesHelper.getInstance().isMetric(context);
        SQLiteDatabase db = getReadableDatabase();

        String statement = "SELECT " +
                "sum(" + ProviderContract.SportActivityEntry.DISTANCE + ")," +
                "sum(" + ProviderContract.SportActivityEntry.DURATION + ")," +
                "sum(" + ProviderContract.SportActivityEntry.CALORIES + ")," +
                "sum(" + ProviderContract.SportActivityEntry.STEPS + ") FROM " + ProviderContract.SportActivityEntry.TABLE_NAME +
                " where " + ProviderContract.SportActivityEntry.DELETED + "=0 AND " +
                ProviderContract.SportActivityEntry.ACCOUNT_ID + "=? AND " +
                ProviderContract.SportActivityEntry.START_TIMESTAMP + " BETWEEN ? AND ?";

        String[] values = {userId, String.valueOf(fromDate), String.valueOf(toDate)};
        Cursor c = db.rawQuery(statement, values);

        if (c.moveToFirst()) {
            stats.distance = c.getDouble(0);
            stats.duration = c.getLong(1);
            stats.calories = c.getLong(2);
            stats.startTime = c.getLong(3);
        }
        stats.startTime = fromDate;
        stats.endTime = toDate;

        return stats;
    }

    public Weight getWeightByDate(Context context, String userID, long date){
        Weight weight = new Weight();
        String[] projection = {ProviderContract.WeightEntry.WEIGHT,
                                ProviderContract.WeightEntry.DATE};

        Cursor c = context.getContentResolver().query(ProviderContract.WeightEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(date)).appendPath(userID).build(),
                projection,
                null,
                null,
                null);

        if(c.moveToFirst()){
            weight.weight = c.getDouble(0);
            weight.date = c.getLong(1);
        }
        return  weight;
    }

    public int addWeight(Context context, String userID, Weight weight, int synced){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProviderContract.WeightEntry.ACCOUNT_ID, userID);
        contentValues.put(ProviderContract.WeightEntry.WEIGHT, weight.weight);
        contentValues.put(ProviderContract.WeightEntry.DATE, weight.date);
        contentValues.put(ProviderContract.WeightEntry.LAST_MODIFIED, weight.lastModified);
        contentValues.put(ProviderContract.WeightEntry.SYNCED, synced);

        Uri uri = context.getContentResolver().insert(ProviderContract.WeightEntry.CONTENT_URI, contentValues);
        updateLastModifiedTime(context, userID, ProviderContract.SyncEntry.LAST_MODIFIED_WEIGHTS, System.currentTimeMillis());
        return 0;
    }

    public void updateWeightTime(Context context, String userID, long date, ContentValues values){
        context.getContentResolver().update(ProviderContract.GoalEntry.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(date))
                        .appendPath(userID).build(),
                        values, null, null);

    }

    public boolean isSynced(Context context){
        String[] projection = {ProviderContract.SyncEntry.LAST_MODIFIED,
                                ProviderContract.SyncEntry.LAST_SYNC};

        Cursor c = context.getContentResolver().query(ProviderContract.SyncEntry.CONTENT_URI.buildUpon().appendPath(PreferencesHelper.getInstance().getCurrentUserId(context)).build(),
                projection, null, null, null);

        if(c.moveToFirst()){
            if(c.getLong(0) > c.getLong(1)){
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

}
