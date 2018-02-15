package com.daniel.FitTrackerApp.provider;


import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.daniel.FitTrackerApp.helpers.DBHelper;

import java.util.ArrayList;

public class TrackerProvider extends ContentProvider
{
    public static final int ROUTE_ACTIVITIES = 1;
    public static final int ROUTE_ACTIVITIES_ID = 2;
    public static final int ROUTE_SYNCS = 3;
    public static final int ROUTE_SYNC_ID = 4;
    public static final int ROUTE_ACTIVITY_SPLITS = 6;
    public static final int ROUTE_ACTIVITY_DELETE_ID = 7;
    public static final int ROUTE_GOAL_ID = 8;
    public static final int ROUTE_GOALS = 9;
    public static final int ROUTE_GOAL_DELETE = 10;
    public static final int ROUTE_WEIGHT = 11;
    public static final int ROUTE_WEIGHT_DATE = 12;
    public static final int ROUTE_WEIGHT_USER_ID = 13;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, "sport_activity_entry/*", ROUTE_ACTIVITIES);
        sUriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, "sport_activity_entry/*/*", ROUTE_ACTIVITIES_ID);
        //first parameter should be 1 or 0
        sUriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, "sport_activity_entry/*/*/*", ROUTE_ACTIVITY_DELETE_ID);
        sUriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, "sync_entry", ROUTE_SYNCS);
        sUriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, "sync_entry/*", ROUTE_SYNC_ID);
        sUriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, "sport_activity_split/*/*", ROUTE_ACTIVITY_SPLITS);
        sUriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, "goal_entry/*", ROUTE_GOALS);
        sUriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, "goal_entry/*/*", ROUTE_GOAL_ID);
        sUriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, "goal_entry/*/*/*", ROUTE_GOAL_DELETE);
        sUriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, "weight_entry", ROUTE_WEIGHT);
        sUriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, "weight_entry/*", ROUTE_WEIGHT_USER_ID);
        //first parameter is the date in ms
        sUriMatcher.addURI(ProviderContract.CONTENT_AUTHORITY, "weight_entry/*/*", ROUTE_WEIGHT_DATE);
    }


    @Override
    public boolean onCreate() {
        DBHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = DBHelper.getInstance().getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);

        switch (match)
        {
            case ROUTE_ACTIVITIES_ID:
            {
                // Return a single entry, by ID.
                String userId = uri.getLastPathSegment();
                String id = uri.getPathSegments().get(1);
                builder.table(ProviderContract.SportActivityEntry.TABLE_NAME);
                builder.where(ProviderContract.SportActivityEntry._ID + "=? AND " + ProviderContract.SportActivityEntry.ACCOUNT_ID + "=? ", new String[]{id, userId});
                builder.where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                Context context = getContext();
                if(context != null)
                {
                    c.setNotificationUri(context.getContentResolver(), uri);
                }

                return c;
            }
            case ROUTE_ACTIVITIES:
            {
                // Return all known entries.
                String userId = uri.getLastPathSegment();
                builder.table(ProviderContract.SportActivityEntry.TABLE_NAME)
                        .where(ProviderContract.SportActivityEntry.ACCOUNT_ID + "=? ", new String[]{userId})
                        .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);

                Context context = getContext();
                if(context != null)
                {
                    c.setNotificationUri(context.getContentResolver(), uri);
                }

                return c;
            }
            case ROUTE_SYNC_ID:
            {
                String id = uri.getLastPathSegment();
                builder.table(ProviderContract.SyncEntry.TABLE_NAME);
                builder.where(ProviderContract.SyncEntry._ID + "=? ", id);
            }
            case ROUTE_SYNCS:
            {
                builder.table(ProviderContract.SyncEntry.TABLE_NAME)
                        .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);

                Context context = getContext();
                if(context != null)
                {
                    c.setNotificationUri(context.getContentResolver(), uri);
                }

                return c;
            }
            case ROUTE_ACTIVITY_SPLITS:
            {
                String userID = uri.getLastPathSegment();
                String activityID = uri.getPathSegments().get(1);
                String[] wheres = {userID, activityID};
                builder.table(ProviderContract.SportActivityEntry.SPLIT_TABLE_NAME);
                builder.where(ProviderContract.SportActivityEntry.SPLIT_ACCOUNT_ID + "=? AND " + ProviderContract.SportActivityEntry.SPLIT_ACTIVITY_ID + "=?",
                        wheres);
                builder.where(selection, selectionArgs);

                Cursor c = builder.query(db, projection, sortOrder);

                Context context = getContext();
                if(context != null)
                {
                    c.setNotificationUri(context.getContentResolver(), uri);
                }

                return c;
            }
            case ROUTE_GOALS:
            {
                String userID = uri.getLastPathSegment();
                String[] wheres = {userID};
                builder.table(ProviderContract.GoalEntry.TABLE_NAME);
                builder.where(ProviderContract.GoalEntry.ACCOUNT_ID + "=?", wheres);
                builder.where(selection, selectionArgs);

                Cursor c = builder.query(db, projection, sortOrder);

                Context context = getContext();
                if(context != null)
                {
                    c.setNotificationUri(context.getContentResolver(), uri);
                }

                return c;
            }
            case ROUTE_GOAL_ID:{
                String userID = uri.getLastPathSegment();
                String goalID = uri.getPathSegments().get(1);
                String[] wheres = {goalID, userID};
                builder.table(ProviderContract.GoalEntry.TABLE_NAME);
                builder.where(ProviderContract.GoalEntry._ID + "=? AND " + ProviderContract.GoalEntry.ACCOUNT_ID + "=?", wheres);
                builder.where(selection, selectionArgs);

                Cursor c = builder.query(db, projection, sortOrder);

                Context context = getContext();
                if(context != null)
                {
                    c.setNotificationUri(context.getContentResolver(), uri);
                }

                return c;
            }
            case ROUTE_WEIGHT_DATE:{
                String userID = uri.getLastPathSegment();

                String[] wheres = {userID, uri.getPathSegments().get(1)};
                builder.table(ProviderContract.WeightEntry.TABLE_NAME);
                builder.where(ProviderContract.WeightEntry.ACCOUNT_ID + "=? AND " +
                        ProviderContract.WeightEntry.DATE + "=?", wheres);
                builder.where(selection, selectionArgs);

                Cursor c = builder.query(db, projection, sortOrder);

                Context context = getContext();
                if(context != null)
                {
                    c.setNotificationUri(context.getContentResolver(), uri);
                }

                return c;
            }

            case ROUTE_WEIGHT_USER_ID:{
                String userID = uri.getLastPathSegment();

                String[] wheres = {userID};
                builder.table(ProviderContract.WeightEntry.TABLE_NAME);
                builder.where(ProviderContract.WeightEntry.ACCOUNT_ID + "=?", wheres);
                builder.where(selection, selectionArgs);

                Cursor c = builder.query(db, projection, sortOrder);

                Context context = getContext();
                if(context != null)
                {
                    c.setNotificationUri(context.getContentResolver(), uri);
                }

                return c;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match)
        {
            case ROUTE_ACTIVITIES:
            {
                long id = 0;
                try {
                    id = db.insertOrThrow(ProviderContract.SportActivityEntry.TABLE_NAME, null, values);
                } catch (SQLiteConstraintException ex){
                    String where = ProviderContract.SportActivityEntry._ID + "=?";
                    String[] whereValues = {values.getAsString(ProviderContract.SportActivityEntry._ID)};
                    update(uri, values, where, whereValues);
                }
                result = Uri.parse(ProviderContract.SportActivityEntry.CONTENT_URI + "/" + id);
                break;
            }
            case ROUTE_ACTIVITIES_ID:
            {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            case ROUTE_SYNCS:
            {
                long id = db.insertOrThrow(ProviderContract.SyncEntry.TABLE_NAME, null, values);
                result = Uri.parse(ProviderContract.SportActivityEntry.CONTENT_URI + "/" + id);
                break;
            }
            case ROUTE_SYNC_ID:
            {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            case ROUTE_ACTIVITY_SPLITS:
            {
                long id = db.insertOrThrow(ProviderContract.SportActivityEntry.SPLIT_TABLE_NAME, null, values);
                result = Uri.parse(ProviderContract.SportActivityEntry.SPLIT_CONTENT_URI + "/" + id);
                break;
            }
            case ROUTE_GOALS:
            {
                long id = 0;
                try{
                    id = db.insertOrThrow(ProviderContract.GoalEntry.TABLE_NAME, null, values);
                } catch (SQLiteConstraintException ex){
                    update(uri.buildUpon().appendPath(values.getAsString(ProviderContract.GoalEntry._ID)).build(), values, null, null);
                }

                result = Uri.parse(ProviderContract.GoalEntry.CONTENT_URI + "/" + id);
                break;
            }
            case ROUTE_WEIGHT:
            {
                long id = 0;
                try{
                     id = db.insertOrThrow(ProviderContract.WeightEntry.TABLE_NAME, null, values);
                } catch (SQLiteConstraintException ex){
                    Uri updateUri = uri.buildUpon().appendPath(String.valueOf(values.getAsLong(ProviderContract.WeightEntry.DATE)))
                            .appendPath(values.getAsString(ProviderContract.WeightEntry.ACCOUNT_ID)).build();
                    update(updateUri, values, null, null);
                }

                result = Uri.parse(ProviderContract.WeightEntry.CONTENT_URI + "/" + id);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context context = getContext();
        if(context != null){
            context.getContentResolver().notifyChange(uri, null, false);
        }

        return result;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
//        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//        final int match = sUriMatcher.match(uri);
//        switch (match) {
//            case WEATHER:
//                db.beginTransaction();
//                int returnCount = 0;
//                try {
//                    for (ContentValues value : values) {
//                        normalizeDate(value);
//                        long _id = db.insert(WeatherEntry.TABLE_NAME,
//                                null, value);
//                        if (_id != -1) {
//                            returnCount++;
//                        }
//                    }
//                    db.setTransactionSuccessful();
//                } finally {
//                    db.endTransaction();
//                }
//                getContext().getContentResolver().notifyChange(uri, null);
//                return returnCount;
//            default:
//                return super.bulkInsert(uri, values);
//        }

//        SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
//        db.setTransactionSuccessful();


        return super.bulkInsert(uri, values);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
        SelectionBuilder sb = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        int count = 0;
        switch (match)
        {
            case ROUTE_ACTIVITIES:
            {
                String userID = uri.getLastPathSegment();
                String where = ProviderContract.SportActivityEntry.ACCOUNT_ID + "=?";

                count = sb.table(ProviderContract.SportActivityEntry.TABLE_NAME)
                        .where(where, userID)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            }
            case ROUTE_ACTIVITY_DELETE_ID:
            {
                int delete = Integer.parseInt(uri.getPathSegments().get(1));
                String userID = uri.getLastPathSegment();
                String id = uri.getPathSegments().get(2);
                sb.table(ProviderContract.SportActivityEntry.TABLE_NAME)
                        .where(ProviderContract.SportActivityEntry._ID + "=? AND " + ProviderContract.SportActivityEntry.ACCOUNT_ID + "=?", new String[]{id, userID})
                        .where(selection, selectionArgs);
                if(delete == 1){
                    count = sb.delete(db);
                    delete(ProviderContract.SportActivityEntry.SPLIT_CONTENT_URI.buildUpon().appendPath(id).appendPath(userID).build(),
                            null, null);
                } else if(delete == 0) {
                    long lastSyncTime = DBHelper.getInstance().getLastModifiedTime(getContext(), userID, ProviderContract.SyncEntry.LAST_SYNC);
                    Cursor c = query(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(id).appendPath(userID).build(), new String[]{ProviderContract.SportActivityEntry.LAST_MODIFIED}, ProviderContract.SportActivityEntry.ACCOUNT_ID + "=? AND " + ProviderContract.SportActivityEntry._ID + "=?", new String[]{userID, id}, null, null);
                    if(c.moveToFirst()){
                        long lastModified = c.getLong(0);
                        if(lastModified <= lastSyncTime){
                            long currentTimestamp = System.currentTimeMillis();
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(ProviderContract.SportActivityEntry.DELETED, 1);
                            contentValues.put(ProviderContract.SportActivityEntry.LAST_MODIFIED, currentTimestamp);
                            count = sb.update(db, contentValues);
                            DBHelper.getInstance().updateLastModifiedTime(getContext(), userID, ProviderContract.SyncEntry.LAST_MODIFIED_ACTIVITIES, currentTimestamp);
                        } else {
                            count = sb.delete(db);
                            delete(ProviderContract.SportActivityEntry.SPLIT_CONTENT_URI.buildUpon().appendPath(id).appendPath(userID).build(),
                                    null, null);
                        }
                    }
                    c.close();
                }
                break;
            }
            case ROUTE_SYNCS:
            {
                count = sb.table(ProviderContract.SyncEntry.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            }
            case ROUTE_SYNC_ID:
            {
                String id = uri.getLastPathSegment();
                count = sb.table(ProviderContract.SyncEntry.TABLE_NAME)
                        .where(ProviderContract.SyncEntry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            }
            case ROUTE_ACTIVITY_SPLITS:
            {
                String userID = uri.getLastPathSegment();
                String activityID = uri.getPathSegments().get(1);
                String[] wheres = {userID, activityID};
                count = sb.table(ProviderContract.SportActivityEntry.SPLIT_TABLE_NAME)
                        .where(ProviderContract.SportActivityEntry.SPLIT_ACCOUNT_ID + "=? AND " + ProviderContract.SportActivityEntry.SPLIT_ACTIVITY_ID + "=?", wheres)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            }
            case ROUTE_GOAL_ID:
            {
                String userID = uri.getLastPathSegment();
                String goalID = uri.getPathSegments().get(1);
                String[] wheres = {userID, goalID};
                count = sb.table(ProviderContract.GoalEntry.TABLE_NAME)
                        .where(ProviderContract.GoalEntry.ACCOUNT_ID + "=? AND " + ProviderContract.GoalEntry._ID + "=?", wheres)
                        .where(selection, selectionArgs)
                        .delete(db);

                break;
            }
            case ROUTE_GOAL_DELETE:
            {
                String userID = uri.getLastPathSegment();
                int delete = Integer.parseInt(uri.getPathSegments().get(1));
                String goalID = uri.getPathSegments().get(2);
                String where = ProviderContract.GoalEntry._ID + "=? AND " + ProviderContract.GoalEntry.ACCOUNT_ID + "=?";
                String[] wheres = {goalID, userID};
                sb.table(ProviderContract.GoalEntry.TABLE_NAME)
                        .where(where, wheres)
                        .where(selection, selectionArgs);
                if(delete == 1){
                    count = sb.delete(db);
                } else if (delete == 0) {
                    long lastSyncTime = DBHelper.getInstance().getLastModifiedTime(getContext(), userID, ProviderContract.SyncEntry.LAST_SYNC);
                    Cursor c = query(ProviderContract.GoalEntry.CONTENT_URI.buildUpon().appendPath(goalID).appendPath(userID).build(), new String[]{ProviderContract.SportActivityEntry.LAST_MODIFIED}, null, null, null);
                    if(c.moveToFirst()){
                        long lastModified = c.getLong(0);
                        if(lastModified <= lastSyncTime){
                            long currentTimestamp = System.currentTimeMillis();
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(ProviderContract.GoalEntry.DELETED, 1);
                            contentValues.put(ProviderContract.GoalEntry.LAST_MODIFIED, currentTimestamp);
                            count = sb.update(db, contentValues);
                            DBHelper.getInstance().updateLastModifiedTime(getContext(), userID, ProviderContract.SyncEntry.LAST_MODIFIED_GOALS, currentTimestamp);
                        } else {
                            count = sb.delete(db);
                        }
                    }
                    c.close();
                }

                break;
            }
            case ROUTE_GOALS:
            {
                String userID = uri.getLastPathSegment();
                String[] wheres = {userID};
                count = sb.table(ProviderContract.GoalEntry.TABLE_NAME)
                        .where(ProviderContract.GoalEntry.ACCOUNT_ID + "=?", wheres)
                        .where(selection, selectionArgs)
                        .delete(db);

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Context context = getContext();
        if(context != null)
        {
            context.getContentResolver().notifyChange(uri, null, false);
        }

        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = DBHelper.getInstance().getWritableDatabase();
        SelectionBuilder sb = new SelectionBuilder();

        final int match = sUriMatcher.match(uri);
        int count;
        switch (match)
        {
            case ROUTE_ACTIVITIES:
            {
                String id = uri.getLastPathSegment();
                count = sb.table(ProviderContract.SportActivityEntry.TABLE_NAME)
                        .where(ProviderContract.SportActivityEntry.ACCOUNT_ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            }
            case ROUTE_ACTIVITIES_ID:
            {
                String userID = uri.getLastPathSegment();
                String id = uri.getPathSegments().get(1);
                count = sb.table(ProviderContract.SportActivityEntry.TABLE_NAME)
                        .where(ProviderContract.SportActivityEntry._ID + "=? AND " + ProviderContract.SportActivityEntry.ACCOUNT_ID + "=?", new String[]{id, userID})
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            }
            case ROUTE_SYNCS:
            {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            case ROUTE_SYNC_ID:
            {
                String id = uri.getLastPathSegment();
                count = sb.table(ProviderContract.SyncEntry.TABLE_NAME)
                        .where(ProviderContract.SyncEntry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            }
            case ROUTE_GOAL_ID:
            {
                String userID = uri.getLastPathSegment();
                String goalID = uri.getPathSegments().get(1);
                String[] wheres = {userID, goalID};
                count = sb.table(ProviderContract.GoalEntry.TABLE_NAME)
                        .where(ProviderContract.GoalEntry._ID + "=? AND  " + ProviderContract.GoalEntry.ACCOUNT_ID + "=?", wheres)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            }
            case ROUTE_WEIGHT_DATE:
            {
                String userID = uri.getLastPathSegment();
                String date = uri.getPathSegments().get(1);
                String[] wheres = {date, userID};
                count = sb.table(ProviderContract.WeightEntry.TABLE_NAME)
                        .where(ProviderContract.WeightEntry.DATE + "=? AND  " + ProviderContract.WeightEntry.ACCOUNT_ID + "=?", wheres)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return count;
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        return super.applyBatch(operations);
    }

    public static Uri buildUri(String name, String key, int type) {
        return Uri.parse(getUriByType(type) + name + "/" + key);
    }

    private static String getUriByType(int type) {
        switch (type) {
        }
        throw new IllegalStateException("unsupport preftype : " + type);
    }

}
