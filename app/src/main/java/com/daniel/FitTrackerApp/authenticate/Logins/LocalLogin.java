package com.daniel.FitTrackerApp.authenticate.Logins;
import android.content.Context;

import com.daniel.FitTrackerApp.authenticate.AbstractAuthentication;
import com.daniel.FitTrackerApp.authenticate.Authenticator;
import com.daniel.FitTrackerApp.authenticate.Users.LocalUser;
public class LocalLogin extends AbstractAuthentication<LocalUser> {
    public LocalLogin(Context context) {
        super(context);
    }
    @Override
    public void authenticate(LocalUser user) {
        Authenticator authenticator = new Authenticator(user.toJson(), context, AbstractAuthentication.SING_IN_LOCAL, this);
        authenticator.execute();
    }
}
