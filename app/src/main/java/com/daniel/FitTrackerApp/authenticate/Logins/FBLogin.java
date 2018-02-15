package com.daniel.FitTrackerApp.authenticate.Logins;


import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.daniel.FitTrackerApp.authenticate.AbstractAuthentication;
import com.daniel.FitTrackerApp.authenticate.Authenticator;
import com.daniel.FitTrackerApp.authenticate.Users.FBUser;

import org.json.JSONObject;

public class FBLogin extends AbstractAuthentication<FBUser>
{

    public FBLogin(Context context)
    {
        super(context);
    }


    public void registerFacebookCallback(final Context context, CallbackManager callbackManager, LoginButton facebookLoginButton)
    {
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(final LoginResult loginResult)
            {
                final GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),  new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response)
                    {
                        //FacebookRequestError error =  response.getError();

                        authenticate(new FBUser(object));
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email,gender");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel()
            {
                // App code
            }

            @Override
            public void onError(FacebookException exception)
            {
                // App code
                Toast.makeText(context, "ERROR:No internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean isFacebookLoggedIn()
    {
        return AccessToken.getCurrentAccessToken() != null;
    }

    @Override
    public void authenticate(FBUser user)
    {
        Authenticator authenticator = new Authenticator(user.toJson(), context, AbstractAuthentication.SIGN_IN_FACEBOOK, this);
        authenticator.execute();
    }

}
