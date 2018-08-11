package com.daniel.FitTrackerApp.services;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import com.daniel.FitTrackerApp.API;
import com.daniel.FitTrackerApp.utils.AppUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import static com.daniel.FitTrackerApp.utils.HttpConstants.HTTP_DELETE;
import static com.daniel.FitTrackerApp.utils.HttpConstants.HTTP_GET;
import static com.daniel.FitTrackerApp.utils.HttpConstants.HTTP_POST;
import static com.daniel.FitTrackerApp.utils.HttpConstants.HTTP_PUT;

public class NetworkService extends IntentService {

    public static final String DATA_INTENT_STRING = "data";
    public static final String URL_INTENT_STRING = "url_string";
    public static final String HTTP_METHOD = "http_method";

    private Object data;
    private String urlString;
    private ResultReceiver receiver;

    public NetworkService() {
        super(NetworkService.class.getName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent.hasExtra("receiver")){
            receiver = intent.getParcelableExtra("receiver");
        }

        if(intent.hasExtra(HTTP_METHOD))
        {
            if(intent.getExtras().getInt(HTTP_METHOD) == HTTP_POST)
            {
                if(intent.hasExtra(URL_INTENT_STRING) && intent.hasExtra(DATA_INTENT_STRING))
                {
                    data = intent.getExtras().get(DATA_INTENT_STRING);
                    urlString = intent.getExtras().getString(URL_INTENT_STRING);
                    postData(data, urlString);
                }
            }
            else if(intent.getExtras().getInt(HTTP_METHOD) == HTTP_GET)
            {
                if(intent.hasExtra(URL_INTENT_STRING))
                {
                    getData(intent.getExtras().getString(URL_INTENT_STRING));
                }
            }
            else if(intent.getExtras().getInt(HTTP_METHOD) == HTTP_DELETE)
            {
                if(intent.hasExtra(URL_INTENT_STRING))
                {
//                    deleteData(intent.getExtras().getString(URL_INTENT_STRING));
                }
            }
            else if(intent.getExtras().getInt(HTTP_METHOD) == HTTP_PUT)
            {
                if(intent.hasExtra(URL_INTENT_STRING) && intent.hasExtra(DATA_INTENT_STRING))
                {
//                    data = intent.getExtras().get(DATA_INTENT_STRING);
//                    urlString = intent.getExtras().getString(URL_INTENT_STRING);
//                    putData(data, urlString);
                }
            }
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    private HttpURLConnection getConnection(int httpMethod, String urlString) throws IOException
    {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        if(httpMethod == HTTP_GET){
            connection.setRequestMethod("GET");
        } else if(httpMethod == HTTP_POST) {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
        } else if(httpMethod == HTTP_DELETE) {
            connection.setRequestMethod("DELETE");
        } else if(httpMethod == HTTP_PUT) {
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
        }

        return connection;
    }

    private void postData(Object data, String urlString)
    {
        try
        {
            HttpURLConnection connection = getConnection(HTTP_POST, urlString);

            if(data instanceof byte[])
            {
                connection.setRequestProperty("Content-Type", "application/octet-stream");
                connection.getOutputStream().write((byte[]) data);
            }
            else if(data instanceof String)
            {
                connection.setRequestProperty("Content-Type", "application/json");
                connection.getOutputStream().write(data.toString().getBytes(Charset.forName("UTF-8")));
            }
            else
            {
                throw new UnsupportedOperationException();
            }
            if(connection != null)
            {
                InputStream in = connection.getInputStream();
                int code = connection.getResponseCode();
                byte[] bytes = AppUtils.readFully(in, -1, false);

                Bundle bundle = new Bundle();
                bundle.putByteArray("sportActivities", bytes);
                receiver.send(1, bundle);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void getData(String urlString){
        try {
            HttpURLConnection connection = getConnection(HTTP_GET, urlString);
            int code = connection.getResponseCode();
            InputStream is = connection.getInputStream();
            if(urlString.substring(0, API.sharedMap.length()).equals(API.sharedMap)){
                byte[] bytes = AppUtils.readFully(is, -1, false);
                Bundle bundle = new Bundle();
                bundle.putByteArray("map", bytes);
                receiver.send(2, bundle);
            } else if(urlString.equals(API.passwordToken)){
                if(connection.getResponseCode() == 200){
                    receiver.send(1, null);
                }
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}
