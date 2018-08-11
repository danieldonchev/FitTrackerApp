package com.daniel.FitTrackerApp.authenticate.Users;
import android.os.Build;

import com.daniel.FitTrackerApp.authenticate.AbstractAuthentication;

import org.json.JSONException;
import org.json.JSONObject;
public class LocalUser implements User {
    private String password;
    private String email;
    public LocalUser(String email, String password) {
        this.email = email;
        this.password = password;
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AbstractAuthentication.Constants.EMAIL, email);
            jsonObject.put(AbstractAuthentication.Constants.PASSWORD, password);
            jsonObject.put(AbstractAuthentication.Constants.DEVICE, Build.DEVICE);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsonObject;
    }
}
