package com.daniel.FitTrackerApp.activities;

import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.internal.CallbackManagerImpl;
import com.daniel.FitTrackerApp.authenticate.Users.GoogleUser;
import com.daniel.FitTrackerApp.dialogs.LoginDialog;
import com.daniel.FitTrackerApp.dialogs.RegisterDialog;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.authenticate.Logins.FBLogin;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.authenticate.Logins.GoogleLogin;
import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;

import java.util.Arrays;

public class LoginActivity extends AccountAuthenticatorActivity implements Dialog.OnDismissListener
{
    private Button registerButton, loginButton;
    private LoginButton facebookLoginButton;
    private SignInButton googleLoginButton;
    private CallbackManager callbackManager;
    private GoogleLogin googleLogin;
    private FBLogin fbLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        Drawable image = getResources().getDrawable(R.drawable.running, null);
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 2048, 2048, false);
        Drawable d = new BitmapDrawable(getResources(), bitmapResized);
        findViewById(R.id.login_layout).setBackground(d);
        if(isSignedIn())
        {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        else {
            registerButton = (Button) findViewById(R.id.register);
            loginButton = (Button) findViewById(R.id.sign_in);
            facebookLoginButton = (LoginButton) findViewById(R.id.facebook_signin_button);
            googleLoginButton = (SignInButton) findViewById(R.id.google_signin_button);

            registerButton.setOnClickListener(onClickListener);
            loginButton.setOnClickListener(onClickListener);

            googleLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {

                    googleLogin = new GoogleLogin(LoginActivity.this);
                }
            });


            // facebookLoginButton.setReadPermissions("email");
            facebookLoginButton.setReadPermissions(Arrays.asList(
                    "public_profile", "email", "user_birthday", "user_friends"));

            callbackManager = CallbackManager.Factory.create();

            fbLogin = new FBLogin(getApplicationContext());
            fbLogin.registerFacebookCallback(this, callbackManager, facebookLoginButton);

            //REFRESH ACCESS TOKEN
            AccessToken.refreshCurrentAccessTokenAsync();
            //AccessToken.getCurrentAccessToken().isExpired();
        }

    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9001)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess())
            {
                googleLogin.authenticate(new GoogleUser(result.getSignInAccount()));
            } else
            {
                Toast.makeText(this, "Can't connect to Google", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode())
        {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        else if(resultCode == Activity.RESULT_OK)
        {
            Bundle bundle = data.getExtras();
            int b = 5;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.register:
                {
                    RegisterDialog registerDialog = new RegisterDialog(LoginActivity.this, LoginActivity.this);
                    registerDialog.show();

                    break;
                }
                case R.id.sign_in:
                {
                    LoginDialog loginDialog = new LoginDialog(LoginActivity.this, LoginActivity.this);
                    loginDialog.show();
                    break;
                }
            }

            hideMainViews();
        }
    };

    private boolean isSignedIn()
    {
        if(PreferencesHelper.getInstance().isSignedIn(this))
        {
            return true;
        }
        else if(FBLogin.isFacebookLoggedIn())
        {
            return true;
        }

        return false;
    }

    private void hideMainViews()
    {
        registerButton.setVisibility(View.GONE);
        loginButton.setVisibility(View.GONE);
        facebookLoginButton.setVisibility(View.GONE);
        googleLoginButton.setVisibility(View.GONE);
    }

    private void showMainViews()
    {
        registerButton.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.VISIBLE);
        facebookLoginButton.setVisibility(View.VISIBLE);
        googleLoginButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface)
    {
        showMainViews();
    }
}
