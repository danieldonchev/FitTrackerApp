package com.daniel.FitTrackerApp;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class BridgeWebViewClass {

    @JavascriptInterface
    public void reCaptchaCallbackInAndroid(String g_response){
        Log.d("reCaptcha", "token" + g_response);
    }


    @SuppressWarnings("unused")
    @JavascriptInterface
    public void processHTML(String html)
    {
        int b = 5;
        // process the html as needed by the app
    }
}
