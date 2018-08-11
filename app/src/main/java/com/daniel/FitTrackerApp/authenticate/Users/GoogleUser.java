package com.daniel.FitTrackerApp.authenticate.Users;
import android.os.Build;

import com.daniel.FitTrackerApp.authenticate.AbstractAuthentication;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;

public class GoogleUser implements User {
    private GoogleSignInAccount googleAccount;
    public GoogleUser(GoogleSignInAccount googleAccount) {
        this.googleAccount = googleAccount;
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AbstractAuthentication.Constants.NAME, googleAccount.getDisplayName());
            jsonObject.put(AbstractAuthentication.Constants.EMAIL, googleAccount.getEmail());
            jsonObject.put(AbstractAuthentication.Constants.DEVICE, Build.DEVICE);
            jsonObject.put(AbstractAuthentication.Constants.ACCESS_TOKEN, googleAccount.getIdToken());
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsonObject;
    }
}
