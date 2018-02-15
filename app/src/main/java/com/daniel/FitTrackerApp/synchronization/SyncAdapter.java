package com.daniel.FitTrackerApp.synchronization;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.daniel.FitTrackerApp.API;
import com.daniel.FitTrackerApp.App;
import com.daniel.FitTrackerApp.goal.GoalManager;
import com.daniel.FitTrackerApp.authenticate.AccountAuthenticator;
import com.daniel.FitTrackerApp.dialogs.UpdateUserStatsDialog;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.provider.ProviderContract;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.HttpConstants;
import com.daniel.FitTrackerApp.utils.HttpsClient;
import com.traker.shared.Goal;
import com.traker.shared.SerializeHelper;
import com.traker.shared.Split;
import com.traker.shared.SportActivity;
import com.traker.shared.SportActivityMap;
import com.traker.shared.Weight;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import static com.daniel.FitTrackerApp.synchronization.SyncHelper.IS_USER_NEW;

public class SyncAdapter extends AbstractThreadedSyncAdapter
{
    private long lastModifiedTime = 0;
    private long lastSyncTime = 0;
    private long newSyncTime = 0;
    private boolean isSomeDataModified = false, isDataModified = false, isNew, shouldSync;
    private JSONObject lastUserModifications, lastServerModifications;
    private ContentResolver mContentResolver;
    private String accessToken, refreshToken, userID;


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        App.checkAccessToken(getContext());

        mContentResolver = getContext().getContentResolver();
        refreshToken = AccountManager.get(getContext()).peekAuthToken(account, AccountAuthenticator.AUTH_TOKEN_REFRESH);
        accessToken = AccountManager.get(getContext()).peekAuthToken(account, AccountAuthenticator.AUTH_TOKEN_ACCESS);
        userID = AccountManager.get(getContext()).getUserData(account, "id");
        isNew = extras.getBoolean(IS_USER_NEW);

        if(!isNew){
            lastModifiedTime = DBHelper.getInstance().getLastModifiedTime(getContext(), userID, ProviderContract.SyncEntry.LAST_MODIFIED);
            lastSyncTime = DBHelper.getInstance().getLastModifiedTime(getContext(), userID, ProviderContract.SyncEntry.LAST_SYNC);
            lastUserModifications = DBHelper.getInstance().getLastModifiedTimes(getContext(), userID);
            shouldSync = false;
            HttpsURLConnection connection = null;

            if(lastModifiedTime > lastSyncTime){
                isSomeDataModified = true;
            }

            try {
                ContentProviderResult[] results = mContentResolver.applyBatch(ProviderContract.CONTENT_AUTHORITY, operations);

            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }

            if(lastSyncTime == 0){
                shouldSync = true;
            } else {
              getServerSyncTimes();
            }

            if(shouldSync) {

                if(lastSyncTime == 0){
                    //get settings
                    getSettings();

                    //get missing or updated activities
                    getMissingActivities();

                    //get deleted activities
                    getDeletedActivities();

                    //get missing or updated goals
                    getMissingGoals();

                    //get deleted goals
                    getDeletedGoals();

                    //get missing weights
                    getMissingWeights();

                } else {
                    try {
                        if(lastSyncTime < lastServerModifications.getLong(ProviderContract.SyncEntry.LAST_MODIFIED_SETTINGS)){
                            //get settings
                            getSettings();
                        }
                        if(lastSyncTime < lastServerModifications.getLong(ProviderContract.SyncEntry.LAST_MODIFIED_ACTIVITIES)){
                            //get missing or updated activities
                            getMissingActivities();

                            //get deleted activities
                            getDeletedActivities();
                        }
                        if(lastSyncTime < lastServerModifications.getLong(ProviderContract.SyncEntry.LAST_MODIFIED_GOALS)){
                            //get missing or updated goals
                            getMissingGoals();

                            //get deleted goals
                            getDeletedGoals();
                        }
                        if(lastSyncTime < lastServerModifications.getLong(ProviderContract.SyncEntry.LAST_MODIFIED_WEIGHTS)){
                            getMissingWeights();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(isSomeDataModified){
                long dataModifiedTimestamp = 0;

                try {
                    dataModifiedTimestamp = lastUserModifications.getLong(ProviderContract.SyncEntry.LAST_MODIFIED_ACTIVITIES);

                    if(dataModifiedTimestamp > lastSyncTime){
                        //send activities marked as deleted
                        sendMarkedAsDeletedActivites();
                        //send inserted/modified activities
                        sendModifiedActivites();
                    }

                    dataModifiedTimestamp = lastUserModifications.getLong(ProviderContract.SyncEntry.LAST_MODIFIED_SETTINGS);

                    if(dataModifiedTimestamp > lastSyncTime){
                        //send settings
                        sendSettings();
                    }

                    dataModifiedTimestamp = lastUserModifications.getLong(ProviderContract.SyncEntry.LAST_MODIFIED_GOALS);

                    if(dataModifiedTimestamp > lastSyncTime){
                        //send activities marked as deleted
                        sendDeletedGoals();
                        //send inserted/modified activities
                        sendModifiedGoals();
                    }

                    dataModifiedTimestamp = lastUserModifications.getLong(ProviderContract.SyncEntry.LAST_MODIFIED_WEIGHTS);

                    if(dataModifiedTimestamp > lastSyncTime){
                        sendModifiedWeights();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            if(newSyncTime != 0){
                DBHelper.getInstance().updateLastModifiedTime(getContext(), userID, ProviderContract.SyncEntry.LAST_MODIFIED_ACTIVITIES, newSyncTime);
                DBHelper.getInstance().updateLastModifiedTime(getContext(), userID, ProviderContract.SyncEntry.LAST_MODIFIED_SETTINGS, newSyncTime);
                DBHelper.getInstance().updateLastModifiedTime(getContext(), userID, ProviderContract.SyncEntry.LAST_MODIFIED_GOALS, newSyncTime);
                DBHelper.getInstance().updateLastModifiedTime(getContext(), userID, ProviderContract.SyncEntry.LAST_MODIFIED_WEIGHTS, newSyncTime);
                DBHelper.getInstance().updateLastModifiedTime(getContext(), userID, ProviderContract.SyncEntry.LAST_SYNC, newSyncTime);
            }
        }
    }

    private HttpsURLConnection getConnection(String url, int type, int contentType, String id, String token){
        HttpsURLConnection connection = null;
        try{
            HttpsClient httpsClient = new HttpsClient(url, getContext());
            connection = httpsClient.setUpHttpsConnection();
            App.checkAccessToken(getContext());

            String bearerAuth = "Bearer " + token;

            connection.setRequestProperty("Authorization", bearerAuth);
            connection.setRequestProperty("Sync-Time", String.valueOf(lastSyncTime));
            if(type == HttpConstants.HTTP_GET){
                connection.setRequestMethod("GET");
            } else if(type == HttpConstants.HTTP_POST){
                if(contentType == HttpConstants.HTTP_MEDIA_BYTE_ARRAY){
                    connection.setRequestProperty("Content-Type", "application/octet-stream");
                } else if(contentType == HttpConstants.HTTP_MEDIA_JSON) {
                    connection.setRequestProperty("Content-Type", "application/json");
                }
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

            } else if(type == HttpConstants.HTTP_PUT){

            } else if(type == HttpConstants.HTTP_DELETE){

            }

        } catch (IOException ex){
            ex.printStackTrace();
        } finally {
            return connection;
        }
    }

    public void getMissingActivities(){
        HttpsURLConnection connection = getConnection(API.syncActivities, HttpConstants.HTTP_GET, 0, userID, accessToken);
        int code = 0;
        try {
            code = connection.getResponseCode();
            if(code == 200){
                InputStream inputStream = connection.getInputStream();
                ArrayList<SportActivity> sportActivities = SerializeHelper.deserializeSportActivities(AppUtils.readFully(inputStream, -1, true));

                int i = 0;
                for(SportActivity sportActivity : sportActivities){
                    Log.v("SportActivity id", "number: " + String.valueOf(i++));
                    DBHelper.getInstance().addActivity(sportActivity, userID, getContext(), 1, sportActivity.getType());

                }
                if(connection.getHeaderField("Sync-Time") != null){
                    newSyncTime = Long.parseLong(connection.getHeaderField("Sync-Time"));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getDeletedActivities(){
        HttpsURLConnection connection = getConnection(API.deletedActivities, HttpConstants.HTTP_GET, 0, userID, accessToken);
        int code = 0;
        try {
            code = connection.getResponseCode();
            if(code == 200){
                InputStream inputStream = connection.getInputStream();
                JSONArray jsonArray = new JSONArray(AppUtils.readStream(inputStream));
                for(int i = 0; i < jsonArray.length(); i++){
                    int count = mContentResolver.delete(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath("1").appendPath(jsonArray.getString(i)).appendPath(userID).build(),
                            null,
                            null);
                }
                if(connection.getHeaderField("Sync-Time") != null){
                    newSyncTime = Long.parseLong(connection.getHeaderField("Sync-Time"));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException ex){
            ex.printStackTrace();
        }
    }

    public void sendMarkedAsDeletedActivites(){
        String[] projection = new String[]{ProviderContract.SportActivityEntry._ID,
                                            ProviderContract.SportActivityEntry.LAST_MODIFIED};
        String selection = ProviderContract.SportActivityEntry.DELETED + "=1";
        Cursor c = mContentResolver.query(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(userID).build(), projection, selection, null, null);
        JSONArray deletedActivites = null;
        try {
            deletedActivites = new JSONArray();
            if(c.moveToFirst()){
                do{
                    JSONObject object = new JSONObject();
                    object.put("id", c.getString(0));
                    object.put("last_modified", c.getLong(1));
                    deletedActivites.put(object);
                }while(c.moveToNext());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpsURLConnection connection = getConnection(API.deletedActivities, HttpConstants.HTTP_POST, HttpConstants.HTTP_MEDIA_JSON, userID, accessToken);
        try {

            connection.getOutputStream().write(deletedActivites.toString().getBytes(Charset.forName("UTF-8")));
            int code = connection.getResponseCode();
            if(code == 200){
                try {
                    for(int i = 0; i < deletedActivites.length(); i++){
                        mContentResolver.delete(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath("1").appendPath(deletedActivites.getString(i)).appendPath(userID).build(), null, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(connection.getHeaderField("Sync-Time") != null){
                newSyncTime = Long.parseLong(connection.getHeaderField("Sync-Time"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        c.close();
    }


    public void sendModifiedActivites(){
        String activitiesWhere = ProviderContract.SportActivityEntry.LAST_MODIFIED + ">" + String.valueOf(lastSyncTime) + " AND " +
                                    ProviderContract.SportActivityEntry.SYNCED + "=0";
        Cursor c = mContentResolver.query(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath(userID).build(),
                ProviderContract.SportActivityEntry.FULL_PROJECTION, activitiesWhere, null, null);
        ArrayList<SportActivity> sportActivities = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                List<Split> splits = new ArrayList<>();
                Cursor splitCursor = mContentResolver.query(ProviderContract.SportActivityEntry.SPLIT_CONTENT_URI.buildUpon().appendPath(c.getString(0)).appendPath(userID).build(),
                        new String[]{ProviderContract.SportActivityEntry.SPLIT_ID, ProviderContract.SportActivityEntry.SPLIT_DISTANCE, ProviderContract.SportActivityEntry.SPLIT_DURATION},
                        null, null, null);
                if(splitCursor.moveToFirst()){
                    do{
                        splits.add(new Split(splitCursor.getInt(0), splitCursor.getLong(1), splitCursor.getDouble(0)));
                    }while (splitCursor.moveToNext());
                }

                DBHelper.getInstance().getActivitySplits(getContext(), c.getString(0), userID);
                SportActivity sportActivity = new SportActivity(UUID.fromString(c.getString(0)),
                        c.getString(1),
                        c.getLong(3),
                        c.getDouble(2),
                        c.getInt(4),
                        c.getInt(5),
                        new SportActivityMap().deserialize(c.getBlob(6)),
                        c.getLong(7),
                        c.getLong(8),
                        c.getInt(9),
                        c.getLong(10),
                        (ArrayList<Split>) splits);

                sportActivities.add(sportActivity);
            }while (c.moveToNext());
        }
        c.close();
        if(sportActivities.size() > 0){
            try {
                HttpsURLConnection connection = getConnection(API.syncActivities, HttpConstants.HTTP_POST, HttpConstants.HTTP_MEDIA_BYTE_ARRAY, userID, accessToken);
                connection.getOutputStream().write(SerializeHelper.serializeSportActivities(sportActivities));
                int code = connection.getResponseCode();
                if(connection.getHeaderField("Sync-Time") != null){
                    newSyncTime = Long.parseLong(connection.getHeaderField("Sync-Time"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getSettings(){
        HttpsURLConnection connection = getConnection(API.getSettings, HttpConstants.HTTP_GET, 0, userID, accessToken);
        int code = 0;
        try {
            code = connection.getResponseCode();
            InputStream is = connection.getInputStream();
            PreferencesHelper.getInstance().setCurrentAccountSettings(getContext(), AppUtils.readStream(is));
            DBHelper.getInstance().updateAccountDetailsStatus(getContext());
            if(connection.getHeaderField("Sync-Time") != null){
                newSyncTime = Long.parseLong(connection.getHeaderField("Sync-Time"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(UpdateUserStatsDialog.DISMISS_DIALOG_INTENT));
        DBHelper.getInstance().updateAccountDetailsStatus(getContext());
    }

    public void sendSettings(){
        HttpsURLConnection connection = getConnection(API.getSettings, HttpConstants.HTTP_POST, HttpConstants.HTTP_MEDIA_JSON, userID, accessToken);

        try {
        String settings = PreferencesHelper.getInstance().getSettings(getContext());

        JSONObject object = new JSONObject();
        object.put("settings", settings);
        object.put("lastModified", lastUserModifications.getLong(ProviderContract.SyncEntry.LAST_MODIFIED_SETTINGS));

            connection.getOutputStream().write(object.toString().getBytes(Charset.forName("UTF-8")));
            int code = connection.getResponseCode();
            if(connection.getHeaderField("Sync-Time") != null){
                newSyncTime = Long.parseLong(connection.getHeaderField("Sync-Time"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void sendModifiedGoals(){
        String activitiesWhere = ProviderContract.GoalEntry.LAST_MODIFIED + ">" + String.valueOf(lastSyncTime) + " AND " +
                ProviderContract.GoalEntry.SYNCED + "=0";
        Cursor c = mContentResolver.query(ProviderContract.GoalEntry.CONTENT_URI.buildUpon().appendPath(userID).build(),
                ProviderContract.GoalEntry.FULL_PROJECTION, activitiesWhere, null, null);
        ArrayList<Goal> goals = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                Goal goal = new Goal(
                        UUID.fromString(c.getString(0)),
                        c.getInt(1),
                        c.getDouble(2),
                        c.getLong(3),
                        c.getLong(4),
                        c.getLong(5),
                        c.getLong(6),
                        c.getLong(7),
                        c.getLong(8));

                goals.add(goal);
            }while (c.moveToNext());
        }
        c.close();
        if(goals.size() > 0){
            try {
                HttpsURLConnection connection = getConnection(API.goals, HttpConstants.HTTP_POST, HttpConstants.HTTP_MEDIA_BYTE_ARRAY, userID, accessToken);
                connection.getOutputStream().write(SerializeHelper.serializeGoals(goals));
                int code = connection.getResponseCode();
                if(connection.getHeaderField("Sync-Time") != null){
                    newSyncTime = Long.parseLong(connection.getHeaderField("Sync-Time"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendDeletedGoals(){
        String[] projection = new String[]{ProviderContract.GoalEntry._ID};
        String selection = ProviderContract.SportActivityEntry.DELETED + "=1";
        Cursor c = mContentResolver.query(ProviderContract.GoalEntry.CONTENT_URI.buildUpon().appendPath(userID).build(), projection, selection, null, null);
        JSONArray deletedGoals = new JSONArray();
        if(c.moveToFirst()){
            do{
                deletedGoals.put(c.getString(0));
            }while(c.moveToNext());
        }

        HttpsURLConnection connection = getConnection(API.deletedGoals, HttpConstants.HTTP_POST, 0, userID, accessToken);
        int code = 0;
        try {
            connection.getOutputStream().write(deletedGoals.toString().getBytes(Charset.forName("UTF-8")));
            code = connection.getResponseCode();
            if(code == 200){
                try {
                    for(int i = 0; i < deletedGoals.length(); i++){
                        mContentResolver.delete(ProviderContract.GoalEntry.CONTENT_URI.buildUpon().appendPath("1").appendPath(deletedGoals.getString(i)).appendPath(userID).build(), null, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(connection.getHeaderField("Sync-Time") != null){
                newSyncTime = Long.parseLong(connection.getHeaderField("Sync-Time"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        c.close();
    }

    public void getMissingGoals(){
        HttpsURLConnection connection = getConnection(API.missingGoals, HttpConstants.HTTP_GET, 0, userID, accessToken);
        int code = 0;
        try {
            code = connection.getResponseCode();
            if(code == 200){
                InputStream inputStream = connection.getInputStream();
                ArrayList<Goal> goals = SerializeHelper.deserializeGoals(AppUtils.readFully(inputStream, -1, true));

                int i = 0;
                for(Goal goal : goals){
                    Log.v("SportActivity id", "number: " + String.valueOf(i++));

                    DBHelper.getInstance().addGoal(getContext(), userID, goal, 1);
                    if(GoalManager.getInstance() != null){
                        if (Looper.myLooper()==null)
                            Looper.prepare();
                        GoalManager.getInstance().load(getContext(), false);
                    }
                }
                if(connection.getHeaderField("Sync-Time") != null){
                    newSyncTime = Long.parseLong(connection.getHeaderField("Sync-Time"));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getDeletedGoals(){
        HttpsURLConnection connection = getConnection(API.deletedGoals, HttpConstants.HTTP_GET, 0, userID, accessToken);
        int code = 0;
        try {
            code = connection.getResponseCode();
            if(code == 200){
                InputStream inputStream = connection.getInputStream();
                JSONArray jsonArray = new JSONArray(AppUtils.readStream(inputStream));
                for(int i = 0; i < jsonArray.length(); i++){
                    int count = mContentResolver.delete(ProviderContract.GoalEntry.CONTENT_URI.buildUpon().appendPath("1").appendPath(jsonArray.getString(i)).appendPath(userID).build(),
                            null,
                            null);
                }
                if(connection.getHeaderField("Sync-Time") != null){
                    newSyncTime = Long.parseLong(connection.getHeaderField("Sync-Time"));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException ex){
            ex.printStackTrace();
        }
    }

    public void getMissingWeights(){
        HttpsURLConnection connection = getConnection(API.weights, HttpConstants.HTTP_GET, 0, userID, accessToken);
        int code = 0;
        try {
            code = connection.getResponseCode();
            if(code == 200){
                InputStream inputStream = connection.getInputStream();
                ArrayList<Weight> weights = SerializeHelper.deserializeWeights(AppUtils.readFully(inputStream, -1, true));

                for(Weight weight : weights){
                    DBHelper.getInstance().addWeight(getContext(), userID, weight, 1);
                }
                if(connection.getHeaderField("Sync-Time") != null){
                    newSyncTime = Long.parseLong(connection.getHeaderField("Sync-Time"));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendModifiedWeights(){
        String activitiesWhere = ProviderContract.WeightEntry.LAST_MODIFIED + ">" + String.valueOf(lastSyncTime) + " AND " +
                ProviderContract.WeightEntry.SYNCED + "=0";
        Cursor c = mContentResolver.query(ProviderContract.WeightEntry.CONTENT_URI.buildUpon().appendPath(userID).build(),
                ProviderContract.WeightEntry.FULL_PROJECTION, activitiesWhere, null, null);
        ArrayList<Weight> weights = new ArrayList<>();
        if(c.moveToFirst()){
            do{
                Weight weight = new Weight(c.getDouble(2),
                                            c.getLong(0),
                                            c.getLong(3));


                weights.add(weight);
            }while (c.moveToNext());
        }
        c.close();
        int code = 0;
        if(weights.size() > 0){
            try {
                HttpsURLConnection connection = getConnection(API.weights, HttpConstants.HTTP_POST, HttpConstants.HTTP_MEDIA_BYTE_ARRAY, userID, accessToken);
                connection.getOutputStream().write(SerializeHelper.serializeWeights(weights));
                code = connection.getResponseCode();
                if(connection.getHeaderField("Sync-Time") != null){
                    newSyncTime = Long.parseLong(connection.getHeaderField("Sync-Time"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getServerSyncTimes(){
        HttpsURLConnection connection = getConnection(API.syncTimes, HttpConstants.HTTP_GET, 0, userID, accessToken);
        int code = 0;
        try{
            code = connection.getResponseCode();
            shouldSync = Boolean.parseBoolean(connection.getHeaderField("Should-Sync"));
            InputStream is = connection.getInputStream();
            try {
                lastServerModifications = new JSONObject(AppUtils.readStream(is));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
