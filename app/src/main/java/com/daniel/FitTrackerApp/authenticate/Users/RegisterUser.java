package com.daniel.FitTrackerApp.authenticate.Users;


import android.os.Build;


import com.daniel.FitTrackerApp.authenticate.AbstractAuthentication;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterUser implements User
{
    private String name;
    private String email;
    private String password;

    public RegisterUser(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;

    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AbstractAuthentication.Constants.NAME, name);
            jsonObject.put(AbstractAuthentication.Constants.EMAIL, email);
            jsonObject.put(AbstractAuthentication.Constants.PASSWORD, password);
            jsonObject.put(AbstractAuthentication.Constants.DEVICE, Build.DEVICE);
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsonObject;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
