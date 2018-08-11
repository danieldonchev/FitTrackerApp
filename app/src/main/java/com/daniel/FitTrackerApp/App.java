package com.daniel.FitTrackerApp;

import android.app.Application;
import android.content.Context;

//import cacert.support.multidex.MultiDex;

import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.utils.HttpsClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class App extends Application
{
    private static Application application;
    public static Thread.UncaughtExceptionHandler defaultHandler;

    public static Application getApplication()
    {
        return application;
    }

    public static Context getContext()
    {
        return getApplication().getApplicationContext();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        try {
            HttpsClient.sslContext = getSslContext();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);

        application = this;
        PreferencesHelper.getInstance(this);
        //Testing
        //PreferencesHelper.getInstance().isDefaultHandler(false);
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

    }

    @Override
    protected void attachBaseContext(Context context)
    {
        super.attachBaseContext(context);
//        if(!AppUtils.hasLollipop())
//        {
//            MultiDex.install(this);
//        }
    }

    private SSLContext getSslContext() throws CertificateException, KeyStoreException,
            KeyManagementException, IOException, NoSuchAlgorithmException
    {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        InputStream caInput = new BufferedInputStream(this.getResources().openRawResource(R.raw.cacert));
        Certificate ca = cf.generateCertificate(caInput);
        System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext sslCntxt = SSLContext.getInstance("TLS");
        sslCntxt.init(null, tmf.getTrustManagers(), null);


        return sslCntxt;
    }

    public static void checkAccessToken(Context context){
        long accessTokenExpireTime = PreferencesHelper.getInstance().getAccessTokenExpireDate(context);
        if(accessTokenExpireTime - 2 * 60 * 1000 < System.currentTimeMillis()){
            AppNetworkManager.getAccessToken(context);
        }
    }
}
