package com.daniel.FitTrackerApp.authenticate.Users;
import android.os.Build;

import com.facebook.AccessToken;
import com.daniel.FitTrackerApp.authenticate.AbstractAuthentication;

import org.json.JSONException;
import org.json.JSONObject;
public class FBUser implements User {
    private JSONObject jsonResponse;
    public FBUser(JSONObject jsonResponse) {
        this.jsonResponse = jsonResponse;
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AbstractAuthentication.Constants.NAME, getFacebookFirstName(jsonResponse) + " " + getFacebookLastName(jsonResponse));
            jsonObject.put(AbstractAuthentication.Constants.EMAIL, getFacebookEmail(jsonResponse));
            jsonObject.put(AbstractAuthentication.Constants.DEVICE, Build.DEVICE);
            jsonObject.put(AbstractAuthentication.Constants.ACCESS_TOKEN, AccessToken.getCurrentAccessToken().getToken());
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsonObject;
    }
    private String getFacebookFirstName(JSONObject object) {
        try {
            return object.getString("first_name");
        } catch (JSONException e) {
            return "";
        }
    }
    private String getFacebookLastName(JSONObject object) {
        try {
            return object.getString("last_name");
        } catch (JSONException e) {
            return "";
        }
    }
    private String getFacebookEmail(JSONObject object) {
        try {
            return object.getString("email");
        } catch (JSONException e) {
            return "";
        }
    }
    private String getFacebookBirthday(JSONObject object) {
        try {
            return object.getString("birthday");
        } catch (JSONException e) {
            return "";
        }
    }
    private String getFacebookGender(JSONObject object) {
        try {
            return object.getString("gender");
        } catch (JSONException e) {
            return "";
        }
    }
}
