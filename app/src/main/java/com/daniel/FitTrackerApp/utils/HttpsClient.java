package com.daniel.FitTrackerApp.utils;

import android.content.Context;

import com.daniel.FitTrackerApp.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class HttpsClient
{
    public static SSLContext sslContext = null;
    private Context context;
    private String urlString;
    //private SSLContext sslContext;

    public HttpsClient(String urlString, Context context)
    {
        this.urlString = urlString;
        this.context = context;
//        try
//        {
//            sslContext = getSslContext();
//        }
//        catch (KeyManagementException | KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException ex)
//        {
//            ex.printStackTrace();
//        }

    }

    public HttpsURLConnection setUpHttpsConnection() throws IOException
    {
        // Tell the URLConnection to use a SocketFactory from our SSLContext
        URL url = new URL(urlString);
        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
        HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
        urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());

        return urlConnection;
    }


    private SSLContext getSslContext() throws CertificateException, KeyStoreException,
            KeyManagementException, IOException, NoSuchAlgorithmException
    {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        InputStream caInput = new BufferedInputStream(context.getResources().openRawResource(R.raw.cacert));
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
}