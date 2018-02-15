package com.daniel.FitTrackerApp.authenticate;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;

import com.daniel.FitTrackerApp.API;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.HttpsClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class Authenticator extends AsyncTask<Void, Void, Void>
{
    private JSONObject userData;
    private String receivedStr;
    private HttpsClient httpsClient;
    private Authentication response;
    private JSONObject jsonResponse;
    private int loginType;

    public Authenticator(JSONObject userData, Context context, int loginType, Authentication response)
    {
        this.userData = userData;
        try {
            this.userData.put("androidId", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.response = response;
        this.loginType = loginType;


        switch (loginType)
        {
            case AbstractAuthentication.SING_IN_LOCAL:
            {
                httpsClient = new HttpsClient(API.localLogin, context);
                break;
            }
            case AbstractAuthentication.REGISTER:
            {
                httpsClient = new HttpsClient(API.register, context);
                break;
            }
            case AbstractAuthentication.SIGN_IN_GOOGLE:
            {
                httpsClient = new HttpsClient(API.googleLogin, context);
                break;
            }
            case AbstractAuthentication.SIGN_IN_FACEBOOK:
            {
                httpsClient = new HttpsClient(API.fbLogin, context);
                break;
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
        HttpsURLConnection connection = null;
        try
        {
            connection = httpsClient.setUpHttpsConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            //connection.setConnectTimeout(15 * 1000);


            connection.getOutputStream().write(userData.toString().getBytes(Charset.forName("UTF-8")));
            int rCode = connection.getResponseCode();
            String message = connection.getResponseMessage();
            InputStream in = new BufferedInputStream(connection.getInputStream());
            receivedStr = AppUtils.readStream(in);
            try
            {
                jsonResponse = new JSONObject(receivedStr);
                Iterator<String> it = userData.keys();
                while(it.hasNext())
                {
                    String key = it.next();
                    jsonResponse.put(key, userData.getString(key));
                }

                if(loginType == AbstractAuthentication.REGISTER)
                {
                    jsonResponse.put(AbstractAuthentication.Constants.LOGIN_TYPE, AbstractAuthentication.SING_IN_LOCAL);
                }
                else
                {
                    jsonResponse.put(AbstractAuthentication.Constants.LOGIN_TYPE, loginType);
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }
        catch (FileNotFoundException | SocketTimeoutException ex)
        {
            try
            {
                jsonResponse = new JSONObject();
                int responseCode = connection.getResponseCode();
                jsonResponse.put("authenticate", "fail");
                jsonResponse.put("response code", connection.getResponseCode());
                if(responseCode == 409)
                {
                    jsonResponse.put("message", "Account already exists");
                }
                else if(responseCode == 401)
                {
                    jsonResponse.put("message", "Invalid username or password");
                }
                else
                {
                    jsonResponse.put("message", "Cannot connect to server");
                }

            }
            catch (JSONException | IOException e)
            {
                e.printStackTrace();
            }
        }
        catch (IOException e)
        {
                e.printStackTrace();
        }
        finally {
            if(connection != null)
            {
                connection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        response.authResponse(jsonResponse);
    }
}
