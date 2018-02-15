package com.daniel.FitTrackerApp.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.services.DailyStatsCounter;
import com.daniel.FitTrackerApp.utils.AccountUtils;
import com.daniel.FitTrackerApp.utils.Locale;
import com.daniel.FitTrackerApp.utils.UnitUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Iterator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;

public class PreferencesHelper {

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private static PreferencesHelper ourInstance = new PreferencesHelper();

    public static PreferencesHelper getInstance(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            editor = sharedPreferences.edit();
        }

        return ourInstance;
    }

    public static PreferencesHelper getInstance() {
        return ourInstance;
    }

    private PreferencesHelper() {
    }

    public void reload(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
    }

    public String getRecordingBox(Context context, int index) {
        switch (index) {
            case 0: {
                return sharedPreferences.getString(context.getString(R.string.record_box1_key), null);
            }
            case 1: {
                return sharedPreferences.getString(context.getString(R.string.record_box2_key), null);
            }
            case 2: {
                return sharedPreferences.getString(context.getString(R.string.record_box3_key), null);
            }
            case 3: {
                return sharedPreferences.getString(context.getString(R.string.record_box4_key), null);
            }
        }
        return null;
    }

    public void setRecordingBox(Context context, int index, String data) {
        switch (index) {
            case 0: {
                editor.putString(context.getString(R.string.record_box1_key), data);
                editor.apply();
                break;
            }
            case 1: {
                editor.putString(context.getString(R.string.record_box2_key), data);
                editor.apply();
                break;
            }
            case 2: {
                editor.putString(context.getString(R.string.record_box3_key), data);
                editor.apply();
                break;
            }
            case 3: {
                editor.putString(context.getString(R.string.record_box4_key), data);
                editor.apply();
                break;
            }
        }
    }

    public void setFirstTimeRun(Context context, boolean isFirstRun) {
        editor.putBoolean(context.getString(R.string.first_run_key), isFirstRun);
        editor.apply();
    }

    private void setDefaultRecordBoxes(Context context) {
        String[] boxes = context.getResources().getStringArray(R.array.default_record_boxes);
        editor.putString(context.getString(R.string.record_box1_key), boxes[0]);
        editor.putString(context.getString(R.string.record_box2_key), boxes[1]);
        editor.putString(context.getString(R.string.record_box3_key), boxes[2]);
        editor.putString(context.getString(R.string.record_box4_key), boxes[3]);
        editor.apply();
    }

    public void setIsRecording(Context context, boolean isRecording) {
        editor.putBoolean(context.getString(R.string.is_recording_key), isRecording);
        editor.apply();
    }

    public boolean wasRecording(Context context) {
        return sharedPreferences.getBoolean(context.getString(R.string.is_recording_key), false);
    }

    public boolean isFirstTimeInstalled(Context context) {
        return sharedPreferences.getBoolean(context.getString(R.string.first_run_key), true);
    }

    public boolean isSignedIn(Context context) {
        return sharedPreferences.getBoolean(context.getString(R.string.is_signed_in_key), false);
    }

    public void setIsSignedIn(Context context, boolean isGoogleSignedIn) {
        editor.putBoolean(context.getString(R.string.is_signed_in_key), isGoogleSignedIn);
        editor.apply();
    }

    public void setSignedInEmail(Context context, String email) {
        editor.putString(context.getString(R.string.email_key), email);
        editor.apply();
    }

    public void setSignedInType(Context context, int type) {
        editor.putInt(context.getString(R.string.signed_in_type_key), type);
        editor.apply();
    }

    public int getSignedInType(Context context) {
        return sharedPreferences.getInt(context.getString(R.string.signed_in_type_key), 0);
    }

    public void removePersonEmail(Context context) {
        editor.remove(context.getString(R.string.email_key));
        editor.apply();
    }

    private void setIsMetric(Context context, boolean isMetric) {
        editor.putString(context.getString(R.string.unit_key), isMetric ? context.getString(R.string.metric_value) : context.getString(R.string.imperial_value));
        editor.apply();
    }

    public boolean isMetric(Context context) {
        return sharedPreferences.getString(context.getString(R.string.unit_key), null).equals(context.getString(R.string.metric_value));
    }

    public void setDistanceSplit(Context context, float distance, String unit) {
        editor.putFloat(context.getString(R.string.split_key), distance);
        editor.putString(context.getString(R.string.split_unit_key), unit);
        editor.apply();
    }

    public void setDistanceSplitDistance(Context context, float distance) {
        editor.putFloat(context.getString(R.string.split_key), distance);
        editor.apply();
    }

    public float getDistanceSplitDistance(Context context) {
        return sharedPreferences.getFloat(context.getString(R.string.split_key), 0);
    }

    public float getDistanceSplitDistConverted(Context context) {
        float split = getDistanceSplitDistance(context);
        String unit = getDistanceSplitUnit(context);
        return (float) UnitUtils.convertMetersToUnit(split, unit);
    }

    public String getDistanceSplitUnit(Context context) {
        return sharedPreferences.getString(context.getString(R.string.split_unit_key), null);
    }

    //AbstractWorkout
    public void isDefaultHandler(boolean isDefaultHandler) {
        editor.putBoolean("DefaultHandler", isDefaultHandler);
        editor.apply();
    }

    public boolean isDefaultHandler() {
        return sharedPreferences.getBoolean("DefaultHandler", false);
    }

    public String getAccessToken(Context context) {
        return sharedPreferences.getString(context.getString(R.string.access_token_key), "");
    }

    public void setAccessToken(Context context, String token) {
        editor.putString(context.getString(R.string.access_token_key), token);
        int i = token.lastIndexOf('.') + 1;
        Jwt<Header, Claims> tokenClaims = Jwts.parser().parseClaimsJwt(token.substring(0, i));
        long time = tokenClaims.getBody().getExpiration().getTime();
        setAccessTokenExpireTime(context, time);
        editor.apply();
    }

    public void setAccessTokenExpireTime(Context context, long time) {
        editor.putLong(context.getString(R.string.access_token_expire_date_key), time);
        editor.apply();
    }

    public void removeAccessToken(Context context) {
        editor.remove(context.getString(R.string.access_token_key));
        editor.apply();
    }

    public long getAccessTokenExpireDate(Context context) {
        return sharedPreferences.getLong(context.getString(R.string.access_token_expire_date_key), 0);
    }

    public String getRefreshToken(Context context) {
        return sharedPreferences.getString(context.getString(R.string.refresh_token_key), "");
    }

    public void removeAuthToken(Context context) {
        editor.remove(context.getString(R.string.access_token_key));
        editor.apply();
    }

    public void setDefaults(Context context) {
        float split = 1000;
        double weight = context.getResources().getInteger(R.integer.default_weight);
        boolean isMetric = Locale.isMetric(context);
        setDefaultRecordBoxes(context);
        setIsMetric(context, isMetric);
        setDistanceSplit(context, split, isMetric ? context.getString(R.string.km) : context.getString(R.string.miles));
        setPreference(context.getString(R.string.height_key), context.getResources().getInteger(R.integer.default_height));
        setPreference(context.getString(R.string.weight_key), weight);
        setPreference(context.getString(R.string.birthday_key), AccountUtils.getDefaultBirthday());
        setPreference(context.getString(R.string.gender_key), context.getString(R.string.gender_male_value));

        PreferenceManager.setDefaultValues(context, R.xml.audio_preferences, false);
        PreferenceManager.setDefaultValues(context, R.xml.general_preferences, false);
        setDefaultAppData(context);
    }

    public void setDefaultAppData(Context context) {
        setIsRecording(context, false);
        setFirstTimeRun(context, false);
        isDefaultHandler(false);
    }

    public void setPreference(String key, Object value) {
        if (value instanceof Integer) {
            editor.putInt(key, (int) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Double) {
            editor.putFloat(key, ((Double) value).floatValue());
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value.equals("true") || value.equals("false")) {
            editor.putBoolean(key, Boolean.parseBoolean((String) value));
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        }

        editor.apply();
    }

    public String getBirthdayFormatted(Context context) {
        return new SimpleDateFormat("dd/MM/yyyy").format(getBirthdayTimeInMillis(context));
    }

    public long getBirthdayTimeInMillis(Context context) {
        return sharedPreferences.getLong(context.getString(R.string.birthday_key), 0);
    }

    public float getCurrentUserWeight(Context context) {
        return sharedPreferences.getFloat(context.getString(R.string.weight_key), 0);
    }

    public int getCurrentUserHeight(Context context) {
        return sharedPreferences.getInt(context.getString(R.string.height_key), 0);
    }

    public int getCurrentUserAge(Context context) {
        return AccountUtils.convertMsTimeToAge(sharedPreferences.getLong(context.getString(R.string.birthday_key), 0));
    }

    public String getCurrentUserGender(Context context) {
        return sharedPreferences.getString(context.getString(R.string.gender_key), context.getString(R.string.gender_male_value));
    }

    public String getCurrentUserId(Context context) {
        return sharedPreferences.getString(context.getString(R.string.id_key), "");
    }

    public String getCurrentUserEmail(Context context) {
        return sharedPreferences.getString(context.getString(R.string.email_key), null);
    }

    public String getCurrentUserName(Context context) {
        return sharedPreferences.getString(context.getString(R.string.name_key), null);
    }

    public boolean isAudio(Context context) {
        return sharedPreferences.getBoolean(context.getString(R.string.audio_settings_key), false);
    }

    public int getAudioRepeatTime(Context context) {
        return sharedPreferences.getInt(context.getString(R.string.audio_repeat_time_key), 0);
    }

    public void setCurrentAccount(Context context, String id, String refreshToken, String accessToken, String name, String email, int signInType) {
        editor.putString(context.getString(R.string.id_key), id);
        editor.putString(context.getString(R.string.access_token_key), accessToken);
        editor.putString(context.getString(R.string.refresh_token_key), refreshToken);
        editor.putString(context.getString(R.string.name_key), name);
        editor.putString(context.getString(R.string.email_key), email);
        editor.putInt(context.getString(R.string.signed_in_type_key), signInType);
        editor.putBoolean(context.getString(R.string.is_signed_in_key), true);
        int i = accessToken.lastIndexOf('.') + 1;
        Jwt<Header, Claims> tokenClaims = Jwts.parser().parseClaimsJwt(accessToken.substring(0, i));
        long time = tokenClaims.getBody().getExpiration().getTime();
        setAccessTokenExpireTime(context, time);
        editor.apply();
    }

    public void setCurrentAccountSettings(Context context, String jsonSettings) {
        try {
            JSONObject settings = new JSONObject(jsonSettings);
            Iterator<String> iterator = settings.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();

                if (key.equals(context.getString(R.string.height_key)) || key.equals(context.getString(R.string.audio_repeat_time_key))) {
                    PreferencesHelper.getInstance().setPreference(key, settings.getInt(key));
                } else if (key.equals(context.getString(R.string.birthday_key))) {
                    PreferencesHelper.getInstance().setPreference(key, settings.getLong(key));

                } else if (key.equals(context.getString(R.string.weight_key))) {
                    PreferencesHelper.getInstance().setPreference(key, settings.getDouble(key));
                } else if (key.equals(context.getString(R.string.split_key))) {
                    PreferencesHelper.getInstance().setDistanceSplitDistance(context, ((Double) settings.getDouble(key)).floatValue());
                } else {
                    PreferencesHelper.getInstance().setPreference(key, settings.get(key));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String getSettings(Context context) {
        JSONObject settings = new JSONObject();
        try {
            settings.put(context.getString(R.string.record_box1_key), getRecordingBox(context, 0));
            settings.put(context.getString(R.string.record_box2_key), getRecordingBox(context, 1));
            settings.put(context.getString(R.string.record_box3_key), getRecordingBox(context, 2));
            settings.put(context.getString(R.string.record_box4_key), getRecordingBox(context, 3));
            settings.put(context.getString(R.string.audio_settings_key), PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.audio_settings_key), false));
            settings.put(context.getString(R.string.audio_repeat_time_key), getAudioRepeatTime(context));
            settings.put(context.getString(R.string.audio_avg_pace_key), PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.audio_avg_pace_key), false));
            settings.put(context.getString(R.string.audio_distance_key), PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.audio_distance_key), false));
            settings.put(context.getString(R.string.audio_duration_key), PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.audio_duration_key), false));
            settings.put(context.getString(R.string.audio_speed_key), PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.audio_speed_key), false));
            settings.put(context.getString(R.string.audio_avg_speed_key), PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.audio_avg_speed_key), false));
            settings.put(context.getString(R.string.audio_steps_key), PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.audio_steps_key), false));
            settings.put(context.getString(R.string.audio_calories_key), PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.audio_calories_key), false));
            settings.put(context.getString(R.string.unit_key), isMetric(context) ? context.getString(R.string.metric_value) : context.getString(R.string.imperial_value));
            settings.put(context.getString(R.string.gender_key), getCurrentUserGender(context));
            settings.put(context.getString(R.string.split_key), getDistanceSplitDistance(context));
            settings.put(context.getString(R.string.split_unit_key), getDistanceSplitUnit(context));
            settings.put(context.getString(R.string.height_key), getCurrentUserHeight(context));
            settings.put(context.getString(R.string.weight_key), getCurrentUserWeight(context));
            settings.put(context.getString(R.string.birthday_key), getBirthdayTimeInMillis(context));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return settings.toString();
    }

//    public String getSettings(Context context)
//    {
//        return new Gson().toJson(PreferenceManager.getDefaultSharedPreferences(context).getAll());
//    }

    public boolean hasFirstSteps(Context context) {
        return sharedPreferences.getBoolean(context.getString(R.string.has_first_steps), false);
    }

    public void setHasFirstSteps(Context context) {
        editor.putBoolean(context.getString(R.string.has_first_steps), true);
        editor.apply();
    }

    //testing
    public void saveSteps(float steps) {
        editor.putFloat("stepss", steps);
        editor.commit();
    }

    public float getSteps() {
        return sharedPreferences.getFloat("stepss", 0);
    }

    public void refreshSharedPreferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    public void logAll() {
        Log.v("SharedPreferences", sharedPreferences.getAll().toString());
    }

    public void registerListener() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterListener() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            try {
                if(mService != null){
                    Message msg = Message.obtain(null, DailyStatsCounter.MSG_RELOAD_PREFERENCES);
                    mService.send(msg);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    final Messenger mMessenger = new Messenger(new PreferencesHelper.IncomingHandler());
    Messenger mService = null;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void bindService(Context context) {
        context.bindService(new Intent(context, DailyStatsCounter.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(Context context) {
        if (mConnection != null) {
            context.unbindService(mConnection);
        }
    }
}
