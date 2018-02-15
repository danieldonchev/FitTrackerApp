package com.daniel.FitTrackerApp.utils;

import android.util.Log;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class NullHostNameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        Log.i("RestUtilImpl", "Approving certificate for " + hostname);
        return true;
    }


//    @Override
//    public boolean verify(String hostname, SSLSession session) {
//        Log.i(TAG, "HOST NAME " + hostname);
//        if (hostname.contentEquals("XXX.XX.XXX.XXX")) {
//            Log.i(TAG, "Approving certificate for host " + hostname);
//            return true;
//        }
//        return false;
//    }
}