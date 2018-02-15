package com.daniel.FitTrackerApp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.daniel.FitTrackerApp.goal.Goal;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.provider.ProviderContract;
import com.daniel.FitTrackerApp.services.NetworkService;
import com.daniel.FitTrackerApp.services.WorkoutSender;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.HttpConstants;
import com.daniel.FitTrackerApp.utils.HttpsClient;
import com.daniel.FitTrackerApp.utils.IntentServiceResultReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

import static com.daniel.FitTrackerApp.services.WorkoutSender.DATA_INTENT_STRING;
import static com.daniel.FitTrackerApp.services.WorkoutSender.URL_INTENT_STRING;

public class AppNetworkManager {

    public static void sendSportActivity(Context context, com.traker.shared.SportActivity sportActivity){
        Intent intent = new Intent(context, WorkoutSender.class);
        intent.putExtra(WorkoutSender.DATA_INTENT_STRING, sportActivity.serialize());
        intent.putExtra(URL_INTENT_STRING, API.sportActivity);
        intent.putExtra(WorkoutSender.HTTP_METHOD, HttpConstants.HTTP_POST);

        context.startService(intent);
    }

    public static void sendSportActivityUpdate(Context context, com.traker.shared.SportActivity activity){
        Intent intent = new Intent(context, WorkoutSender.class);
        intent.putExtra(WorkoutSender.DATA_INTENT_STRING, activity.serialize());
        intent.putExtra(URL_INTENT_STRING, API.sportActivity);
        intent.putExtra(WorkoutSender.HTTP_METHOD, HttpConstants.HTTP_PUT);

        context.startService(intent);
    }

    public static void sendSportActivityDelete(Context context, String id){
        Intent deleteIntent = new Intent(context, WorkoutSender.class);
        String url = API.sportActivity + "/" + id;
        deleteIntent.putExtra(WorkoutSender.URL_INTENT_STRING, url);
        deleteIntent.putExtra(WorkoutSender.HTTP_METHOD, HttpConstants.HTTP_DELETE);
        context.startService(deleteIntent);
    }

    public static void sendChangedSettings(Context context, JSONObject settings, long timestamp){
        try {
            if(settings.length() > 0)
            {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("settings", settings.toString());
                jsonObject.put("lastModified", timestamp);
                Intent intent = new Intent(context, WorkoutSender.class);
                intent.putExtra(WorkoutSender.DATA_INTENT_STRING, jsonObject.toString());
                intent.putExtra(URL_INTENT_STRING, API.userSettings);
                intent.putExtra(WorkoutSender.HTTP_METHOD, HttpConstants.HTTP_PUT);

                context.startService(intent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void sendGoal(Context context, Goal goal){
        com.traker.shared.Goal toGoal = goal.toServerGoal();
        Intent intent = new Intent(context, WorkoutSender.class);
        intent.putExtra(WorkoutSender.DATA_INTENT_STRING, toGoal.serialize());
        intent.putExtra(URL_INTENT_STRING, API.goal);
        intent.putExtra(WorkoutSender.HTTP_METHOD, HttpConstants.HTTP_POST);

        context.startService(intent);
    }

    public static void sendGoalUpdate(Context context, Goal goal){
        com.traker.shared.Goal toGoal = goal.toServerGoal();
        Intent intent = new Intent(context, WorkoutSender.class);
        intent.putExtra(WorkoutSender.DATA_INTENT_STRING, toGoal.serialize());
        intent.putExtra(URL_INTENT_STRING, API.goal);
        intent.putExtra(WorkoutSender.HTTP_METHOD, HttpConstants.HTTP_PUT);

        context.startService(intent);
    }

    public static void sendGoalDelete(Context context, String id){
        Intent deleteIntent = new Intent(context, WorkoutSender.class);
        String url = API.goal + "/" + id;
        deleteIntent.putExtra(WorkoutSender.URL_INTENT_STRING, url);
        deleteIntent.putExtra(WorkoutSender.HTTP_METHOD, HttpConstants.HTTP_DELETE);
        context.startService(deleteIntent);
    }

    public static void getPeopleActivities(Context context, JSONObject cameraBounds, IntentServiceResultReceiver resultReceiver){
        Intent intent = new Intent(context, NetworkService.class);

        intent.putExtra("receiver", resultReceiver);
        intent.putExtra(WorkoutSender.DATA_INTENT_STRING, cameraBounds.toString());
        intent.putExtra(URL_INTENT_STRING, API.peopleActivities);
        intent.putExtra(WorkoutSender.HTTP_METHOD, HttpConstants.HTTP_POST);

        context.startService(intent);
    }

    public static void getSharedMap(Context context, String activityID, String userID, IntentServiceResultReceiver resultReceiver){
        Intent intent = new Intent(context, NetworkService.class);

        intent.putExtra("receiver", resultReceiver);
        intent.putExtra(URL_INTENT_STRING, API.sharedMap + "/" + activityID + "/" + userID);
        intent.putExtra(WorkoutSender.HTTP_METHOD, HttpConstants.HTTP_GET);

        context.startService(intent);
    }

    public static void sendWeight(Context context, com.traker.shared.Weight weight){
        Intent intent = new Intent(context, WorkoutSender.class);
        intent.putExtra(WorkoutSender.DATA_INTENT_STRING, weight.serialize());
        intent.putExtra(URL_INTENT_STRING, API.weight);
        intent.putExtra(WorkoutSender.HTTP_METHOD, HttpConstants.HTTP_POST);

        context.startService(intent);
    }

    public static void getForgottenPasswordToken(Context context, String email, IntentServiceResultReceiver resultReceiver){
        Intent intent = new Intent(context, WorkoutSender.class);

        intent.putExtra("receiver", resultReceiver);
        intent.putExtra(DATA_INTENT_STRING, email);
        intent.putExtra(URL_INTENT_STRING, API.passwordToken);
        intent.putExtra(WorkoutSender.HTTP_METHOD, HttpConstants.HTTP_POST);

        context.startService(intent);
    }

    public static void sendChangePassword(Context context, JSONObject jsonObject, IntentServiceResultReceiver resultReceiver){
        Intent intent = new Intent(context, WorkoutSender.class);

        intent.putExtra("receiver", resultReceiver);
        intent.putExtra(DATA_INTENT_STRING, jsonObject.toString());
        intent.putExtra(URL_INTENT_STRING, API.changePassword);
        intent.putExtra(WorkoutSender.HTTP_METHOD, HttpConstants.HTTP_POST);

        context.startService(intent);
    }

    public static void sendProfilePic(final Context context, byte[] bytes){
        final byte[] finalBytes = bytes;
        final String bearerAuth = "Bearer " + PreferencesHelper.getInstance().getAccessToken(context);
        final long version = DBHelper.getInstance().getLastModifiedTime(context, PreferencesHelper.getInstance().getCurrentUserId(context), ProviderContract.SyncEntry.LAST_SYNC);
        final HttpsClient httpsClient = new HttpsClient(API.profilePic, context);
        final Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    int code = 0;
                    try {
                        HttpsURLConnection connection = httpsClient.setUpHttpsConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Authorization", bearerAuth);
                        connection.setRequestProperty("Sync-Time", String.valueOf(version));
                        connection.setRequestProperty("Content-Type", "application/octet-stream");
                        connection.getOutputStream().write(finalBytes);
                        code = connection.getResponseCode();

                    } catch (IOException e) {
                        if(code == 401){
                            getAccessToken(context);
                            this.run();
                        }
                        e.printStackTrace();
                    }
                }
            });
            t1.start();
    }

    public static void getAccessToken(Context context){
        HttpsURLConnection connection = null;
        int code = 0;
        try {
            HttpsClient httpsClient = new HttpsClient(API.accessToken, context);
            connection = httpsClient.setUpHttpsConnection();

            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            String refreshToken = PreferencesHelper.getInstance().getRefreshToken(context);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("refresh_token", refreshToken);
                jsonObject.put("android_id", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
                jsonObject.put("device", Build.DEVICE);

                os.write(jsonObject.toString().getBytes(Charset.forName("UTF-8")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            code = connection.getResponseCode();
            InputStream is = connection.getInputStream();
            String accessToken = AppUtils.readStream(is);

            PreferencesHelper.getInstance().setAccessToken(context, accessToken);

        } catch (IOException e) {
            if(code == 401){
                Toast.makeText(context, "refresh token invalid", Toast.LENGTH_LONG);
            }
            e.printStackTrace();
        }
    }

}
