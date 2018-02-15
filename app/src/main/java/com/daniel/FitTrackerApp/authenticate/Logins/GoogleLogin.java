package com.daniel.FitTrackerApp.authenticate.Logins;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.daniel.FitTrackerApp.authenticate.AbstractAuthentication;
import com.daniel.FitTrackerApp.authenticate.Authenticator;
import com.daniel.FitTrackerApp.authenticate.Users.GoogleUser;
import com.daniel.FitTrackerApp.activities.LoginActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class GoogleLogin extends AbstractAuthentication<GoogleUser>
{
    private GoogleApiClient mGoogleApiClient;
    private final String SERVER_CLIENT_ID = "1044874343985-i4opmbc3kkqv7rhof9t0luehtqcqfj1c.apps.googleusercontent.com";

    public GoogleLogin(Context context)
    {
        super(context);
        buildGoogleApiClientSignIn(context);
        mGoogleApiClient.connect();
    }

    private void signIn()
    {
        if(mGoogleApiClient != null)
        {
            if(mGoogleApiClient.isConnected())
            {
                ((LoginActivity)context).startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient), 9001);
                mGoogleApiClient.disconnect();
            }
        }
    }

    private synchronized void buildGoogleApiClientSignIn(Context context)
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .requestIdToken(SERVER_CLIENT_ID)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedCallback)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks()
    {
        @Override
        public void onConnected(@Nullable Bundle bundle)
        {
            signIn();
        }

        @Override
        public void onConnectionSuspended(int i)
        {
            Toast.makeText(context, "FAILED BREAD", Toast.LENGTH_SHORT).show();
        }
    };

    GoogleApiClient.OnConnectionFailedListener connectionFailedCallback = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
        {
            Toast.makeText(context, "BIG FAILU", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void authenticate(GoogleUser user)
    {
        Authenticator authenticator = new Authenticator(user.toJson(),
                                                        context,
                                                        AbstractAuthentication.SIGN_IN_GOOGLE,
                                                        this);
        authenticator.execute();
        mGoogleApiClient.disconnect();
    }


}
