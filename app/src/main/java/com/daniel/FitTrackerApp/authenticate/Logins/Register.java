package com.daniel.FitTrackerApp.authenticate.Logins;

import android.content.Context;

import com.daniel.FitTrackerApp.authenticate.AbstractAuthentication;
import com.daniel.FitTrackerApp.authenticate.Authenticator;
import com.daniel.FitTrackerApp.authenticate.Users.RegisterUser;

public class Register extends AbstractAuthentication<RegisterUser>
{
    public Register(Context context)
    {
        super(context);
    }

    @Override
    public void authenticate(RegisterUser user)
    {
        Authenticator authenticator = new Authenticator(user.toJson(), context, AbstractAuthentication.REGISTER, this);
        authenticator.execute();
    }
}
