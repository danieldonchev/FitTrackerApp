package com.daniel.FitTrackerApp.authenticate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.activities.LoginActivity;
import com.daniel.FitTrackerApp.activities.MainActivity;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractAuthentication<T> implements Authentication<T> {

    public static final int SIGN_IN_NONE = -1;
    public static final int SIGN_IN_FACEBOOK = 1;
    public static final int SIGN_IN_GOOGLE = 2;
    public static final int SING_IN_LOCAL = 3;
    public static final int REGISTER = 4;

    protected JSONObject jsonResponse;
    protected Context context;

    public AbstractAuthentication(Context context)
    {
        jsonResponse = new JSONObject();
        this.context = context;
    }

    protected void onSuccess(Context context)
    {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if(context instanceof LoginActivity)
        {
            ((LoginActivity)context).finish();
        }
    }

    @Override
    public void authResponse(JSONObject object)
    {
        try
        {
            if(object != null)
            {
                jsonResponse = object;
                if(jsonResponse.optString("login").equals("success"))
                {
                    saveAccountData();
                }
                else
                {
                    Toast.makeText(context, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
    }

    protected void saveAccountData()
    {
        try
        {
            PreferencesHelper.getInstance().setCurrentAccount(context,
                                                                jsonResponse.getString("id"),
                                                                jsonResponse.getString(AccountAuthenticator.AUTH_TOKEN_REFRESH),
                                                                jsonResponse.getString(AccountAuthenticator.AUTH_TOKEN_ACCESS),
                                                                jsonResponse.getString(Constants.NAME),
                                                                jsonResponse.getString(Constants.EMAIL),
                                                                jsonResponse.getInt(Constants.LOGIN_TYPE));

            Account account = new Account(jsonResponse.getString(Constants.EMAIL), AccountAuthenticator.ACCOUNT_TYPE);
            AccountManager.get(context).addAccountExplicitly(account, null, null);
            AccountManager.get(context).setUserData(account, "id", jsonResponse.getString("id"));
            AccountManager.get(context).setAuthToken(account, AccountAuthenticator.AUTH_TOKEN_ACCESS, jsonResponse.getString(context.getString(R.string.access_token_key)));
            AccountManager.get(context).setAuthToken(account, AccountAuthenticator.AUTH_TOKEN_REFRESH, jsonResponse.getString(context.getString(R.string.refresh_token_key)));


            DBHelper.getInstance().findAndSetAccount(context, jsonResponse.getString(Constants.EMAIL), jsonResponse.getString("id"), jsonResponse.getBoolean("new_user"));

            if(context instanceof LoginActivity)
            {
                Intent intent = new Intent();

                intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, jsonResponse.getString(Constants.EMAIL));
                intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
                intent.putExtra(AccountManager.KEY_AUTHTOKEN, jsonResponse.getString(context.getString(R.string.access_token_key)));
                ((LoginActivity)context).setAccountAuthenticatorResult(intent.getExtras());
                ((LoginActivity)context).setResult(Activity.RESULT_OK, intent);
            }

            onSuccess(context);
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
    }

    public class Constants
    {
        public static final String NAME = "name";
        public static final String EMAIL = "email";
        public static final String DEVICE = "device";
        public static final String PASSWORD = "password";
        public static final String RESPONSE_TOKEN = "responseToken";
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String LOGIN_TYPE = "loginType";
    }

}
