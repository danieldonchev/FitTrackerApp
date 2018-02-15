package com.daniel.FitTrackerApp.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.daniel.FitTrackerApp.API;
import com.daniel.FitTrackerApp.AppNetworkManager;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.provider.ProviderContract;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.HttpsClient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class DownloadImageService extends IntentService
{
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    public DownloadImageService()
    {
        super(DownloadImageService.class.getName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle bundle = new Bundle();
        ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String url = intent.getStringExtra("url");
        String name = intent.getStringExtra("name");
        String path = intent.getStringExtra("path");
        Bitmap bitmap;

        try
        {
            URL aURL = new URL(url);
            URLConnection connection = aURL.openConnection();
            connection.connect();
            connection.setConnectTimeout(10 * 1000);
            InputStream is = connection.getInputStream();

            BufferedInputStream bis = new BufferedInputStream(is);

            bitmap = BitmapFactory.decodeStream(bis);
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            IOUtils.copy(is, bs);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bs);
            AppNetworkManager.sendProfilePic(getApplicationContext(), bs.toByteArray());
            AppUtils.cacheBitmap(getApplicationContext(), bitmap, name, path);
            bis.close();
            is.close();

            bundle.putString("image", getCacheDir() + "/photos/" + name);
            receiver.send(STATUS_FINISHED, null);
        } catch (Exception e)
        {
            receiver.send(STATUS_ERROR, null);
        }
    }

    private void sendProfilePic(byte[] bytes){
        try {
            String bearerAuth = "Bearer " + PreferencesHelper.getInstance().getAccessToken(this);
            long version = DBHelper.getInstance().getLastModifiedTime(getApplicationContext(), PreferencesHelper.getInstance().getCurrentUserId(getApplicationContext()), ProviderContract.SyncEntry.LAST_SYNC);
            HttpsClient httpsClient = new HttpsClient(API.profilePic, getApplicationContext());
            HttpsURLConnection connection = httpsClient.setUpHttpsConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", bearerAuth);
            connection.setRequestProperty("Sync-Time", String.valueOf(version));
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.getOutputStream().write((byte[]) bytes);
            int code = connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
