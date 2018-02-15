package com.daniel.FitTrackerApp.authenticate;


import org.json.JSONObject;

public interface Authentication<T>
{
    void authenticate(T user);
    void authResponse(JSONObject object);
}
