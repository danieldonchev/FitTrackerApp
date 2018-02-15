package com.daniel.FitTrackerApp.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.daniel.FitTrackerApp.API;
import com.daniel.FitTrackerApp.App;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.provider.ProviderContract;
import com.daniel.FitTrackerApp.synchronization.SyncHelper;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.HttpsClient;
import com.traker.shared.Goal;
import com.traker.shared.SportActivity;
import com.traker.shared.Weight;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

import static com.daniel.FitTrackerApp.utils.HttpConstants.HTTP_DELETE;
import static com.daniel.FitTrackerApp.utils.HttpConstants.HTTP_GET;
import static com.daniel.FitTrackerApp.utils.HttpConstants.HTTP_POST;
import static com.daniel.FitTrackerApp.utils.HttpConstants.HTTP_PUT;

public class WorkoutSender extends IntentService
{
    public static final String DATA_INTENT_STRING = "data";
    public static final String URL_INTENT_STRING = "url_string";
    public static final String HTTP_METHOD = "http_method";


    private Object data;
    private String urlString;
    private ResultReceiver receiver;

    public WorkoutSender()
    {
        super(WorkoutSender.class.getName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if(intent.hasExtra("receiver")){
            this.receiver = intent.getParcelableExtra("receiver");
        }
        if(intent.hasExtra(HTTP_METHOD))
        {
            if(intent.getExtras().getInt(HTTP_METHOD) == HTTP_POST)
            {
                if(intent.hasExtra(URL_INTENT_STRING) && intent.hasExtra(DATA_INTENT_STRING))
                {
                    data = intent.getExtras().get(DATA_INTENT_STRING);
                    urlString = intent.getExtras().getString(URL_INTENT_STRING);
                    postData(data, urlString);
                }
            }
            else if(intent.getExtras().getInt(HTTP_METHOD) == HTTP_GET)
            {
                if(intent.hasExtra(URL_INTENT_STRING))
                {
                    getData(intent.getExtras().getString(URL_INTENT_STRING));
                }
            }
            else if(intent.getExtras().getInt(HTTP_METHOD) == HTTP_DELETE)
            {
                if(intent.hasExtra(URL_INTENT_STRING))
                {
                    deleteData(intent.getExtras().getString(URL_INTENT_STRING));
                }
            }
            else if(intent.getExtras().getInt(HTTP_METHOD) == HTTP_PUT)
            {
                if(intent.hasExtra(URL_INTENT_STRING) && intent.hasExtra(DATA_INTENT_STRING))
                {
                    data = intent.getExtras().get(DATA_INTENT_STRING);
                    urlString = intent.getExtras().getString(URL_INTENT_STRING);
                    putData(data, urlString);
                }
            }
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    private void postData(Object data, String urlString)
    {
        HttpsURLConnection connection = null;
        int code = 0;
        try
        {
            connection = getConnection(HTTP_POST, urlString);
            if(connection != null)
            {
                if(data instanceof byte[])
                {
                    connection.setRequestProperty("Content-Type", "application/octet-stream");
                    connection.getOutputStream().write((byte[]) data);
                }
                else if(data instanceof String)
                {
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.getOutputStream().write(data.toString().getBytes(Charset.forName("UTF-8")));
                }
                else
                {
                    throw new UnsupportedOperationException();
                }

                    InputStream in = connection.getInputStream();
                    code = connection.getResponseCode();
                    String string = AppUtils.readStream(in);
                    try {
                        if(urlString.equals(API.passwordToken)){
                            Bundle bundle = new Bundle();
                            bundle.putInt("responseCode", connection.getResponseCode());
                            receiver.send(1, bundle);
                        } else if(urlString.equals(API.changePassword)){
                            Bundle bundle = new Bundle();
                            bundle.putInt("responseCode", connection.getResponseCode());
                            receiver.send(2, bundle);
                        } else {
                            JSONObject response = new JSONObject(string);
                            if(response.has("data")){
                                if((response.get("data")).equals(SportActivity.class.getSimpleName()))
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(ProviderContract.SportActivityEntry.SYNCED, 1);
                                    DBHelper.getInstance().updateSportActivityTime(this,
                                            PreferencesHelper.getInstance().getCurrentUserId(this),
                                            response.getString("id"),
                                            values);
                                } else if((response.get("data")).equals("settings")){

                                } else if(response.get("data").equals(Goal.class.getSimpleName())){
                                    ContentValues values = new ContentValues();
                                    values.put(ProviderContract.GoalEntry.SYNCED, 1);
                                    DBHelper.getInstance().updateGoalTime(this,
                                            PreferencesHelper.getInstance().getCurrentUserId(this),
                                            response.getString("id"),
                                            values);
                                } else if(response.get("data").equals(Weight.class.getSimpleName())){
                                    ContentValues values = new ContentValues();
                                    values.put(ProviderContract.WeightEntry.SYNCED, 1);
                                    DBHelper.getInstance().updateWeightTime(this,
                                            PreferencesHelper.getInstance().getCurrentUserId(this),
                                            response.getLong("id"),
                                            values);
                                }
                            }
                        }

                        SyncHelper.shouldSync(this, connection);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }
        }
        catch (IOException e)
        {
                if(connection != null && receiver != null) {
                    try {
                        if (urlString.equals(API.passwordToken)) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("responseCode", connection.getResponseCode());
                            receiver.send(1, bundle);
                        } else if (urlString.equals(API.changePassword)) {
                            Bundle bundle = new Bundle();
                            bundle.putInt("responseCode", connection.getResponseCode());
                            receiver.send(2, bundle);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
            }
            e.printStackTrace();
        }
    }

    private void getData(String urlString)
    {
        int code = 0;
        HttpsURLConnection connection = null;
        try {
             connection = getConnection(HTTP_GET, urlString);
            //connection.setRequestProperty("Content-Type", "application/octet-stream");
            if(connection != null) {
                code = connection.getResponseCode();
                InputStream in = connection.getInputStream();
                if(connection.getHeaderField("Data-Type") != null){
                    if(connection.getHeaderField("Data-Type").equals(SportActivity.class.getSimpleName())){
                        //SportActivity sportActivity = new SportActivity().deserialize(AppUtils.readFully(in, -1, true));
                    } else if(connection.getHeaderField("Data-Type").equals("settings")){
                        PreferencesHelper.getInstance().setCurrentAccountSettings(getApplicationContext(), AppUtils.readStream(in));
                    }
                }
                if (connection.getHeaderField("Should-Sync") != null) {
                    if (Boolean.parseBoolean(connection.getHeaderField("Should-Sync"))) {
                        SyncHelper.requestManualSync(this, false);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteData(String urlString)
    {
        int code = 0;
        try {
            HttpsURLConnection connection = getConnection(HTTP_DELETE, urlString);
            //connection.setRequestProperty("Content-Type", "application/octet-stream");
            if(connection != null) {
                code = connection.getResponseCode();
                InputStream in = new BufferedInputStream(connection.getInputStream());
                String dataType = connection.getHeaderField("Data-Type");
                if(dataType.equals(SportActivity.class.getSimpleName())){
                    try {
                        JSONObject jsonObject = new JSONObject(AppUtils.readStream(in));
                        getContentResolver().delete(ProviderContract.SportActivityEntry.CONTENT_URI.buildUpon().appendPath("1")
                                .appendPath(jsonObject.getString("id"))
                                .appendPath(PreferencesHelper.getInstance().getCurrentUserId(getApplicationContext())).build(), null, null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if(dataType.equals(Goal.class.getSimpleName())){
                    try {
                        JSONObject jsonObject = new JSONObject(AppUtils.readStream(in));
                        getContentResolver().delete(ProviderContract.GoalEntry.CONTENT_URI.buildUpon().appendPath("1")
                                .appendPath(jsonObject.getString("id"))
                                .appendPath(PreferencesHelper.getInstance().getCurrentUserId(getApplicationContext())).build(), null, null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                SyncHelper.shouldSync(this, connection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void putData(Object data, String urlString)
    {
        int code = 0;
        try
        {
            HttpsURLConnection connection = getConnection(HTTP_PUT, urlString);
            if(connection != null)
            {
                if(data instanceof byte[])
                {
                    connection.setRequestProperty("Content-Type", "application/octet-stream");
                    connection.getOutputStream().write((byte[]) data);
                }
                else if(data instanceof String)
                {
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.getOutputStream().write(data.toString().getBytes(Charset.forName("UTF-8")));
                }
                else
                {
                    throw new UnsupportedOperationException();
                }

                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    code = connection.getResponseCode();
                    String string = AppUtils.readStream(in);
                    try {
                        JSONObject response = new JSONObject(string);
                        if((response.get("data")).equals(SportActivity.class.getSimpleName()))
                        {
                            ContentValues values = new ContentValues();
                            values.put(ProviderContract.SportActivityEntry.SYNCED, 1);
                            DBHelper.getInstance().updateSportActivityTime(this,
                                                                            PreferencesHelper.getInstance().getCurrentUserId(this),
                                                                            response.getString("id"),
                                                                            values);
                        } else if ((response.get("data")).equals(Goal.class.getSimpleName())){
                            ContentValues values = new ContentValues();
                            values.put(ProviderContract.GoalEntry.LAST_MODIFIED, connection.getHeaderField("Sync-Time"));
                            values.put(ProviderContract.GoalEntry.SYNCED, 1);
                            DBHelper.getInstance().updateGoalTime(this,
                                    PreferencesHelper.getInstance().getCurrentUserId(this),
                                    response.getString("id"),
                                    values);
                        }
                        SyncHelper.shouldSync(this, connection);
                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private HttpsURLConnection getConnection(int httpMethod, String urlString) throws IOException
    {
        HttpsURLConnection connection;
        HttpsClient httpsClient = new HttpsClient(urlString, this);
        connection = httpsClient.setUpHttpsConnection();

        App.checkAccessToken(getApplicationContext());
//        if(!DBHelper.getInstance().isSynced(getApplicationContext()))
//        {
//            SyncHelper.requestManualSync(getApplicationContext(), false);
//        } else {
            String bearerAuth = "Bearer " + PreferencesHelper.getInstance().getAccessToken(this);
            long version = DBHelper.getInstance().getLastModifiedTime(getApplicationContext(), PreferencesHelper.getInstance().getCurrentUserId(getApplicationContext()), ProviderContract.SyncEntry.LAST_SYNC);

            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            if(httpMethod == HTTP_GET){
                connection.setRequestMethod("GET");
            } else if(httpMethod == HTTP_POST) {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
            } else if(httpMethod == HTTP_DELETE) {
                connection.setRequestMethod("DELETE");
            } else if(httpMethod == HTTP_PUT) {
                connection.setRequestMethod("PUT");
                connection.setDoOutput(true);
            }

            connection.setRequestProperty("Authorization", bearerAuth);
            connection.setRequestProperty("Sync-Time", String.valueOf(version));

            return connection;
//        }

    }

}
