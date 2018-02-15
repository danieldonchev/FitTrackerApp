package com.daniel.FitTrackerApp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.synchronization.SyncHelper;

public class TestFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button button = (Button) view.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               SyncHelper.requestManualSync(getActivity(), false);


//
//                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
//                List<Address> addresses;
//                try {
//                   addresses = geocoder.getFromLocation(43.2186209, 27.9171864, 15);
//                    int d = 5;
//                    int c = 5 + d;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                int b = 5;

//                Thread t1 = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            HttpsURLConnection connection = null;
//                            HttpsClient httpsClient = new HttpsClient(API.checkSync, getContext());
//                            connection = httpsClient.setUpHttpsConnection();
//                            connection.setRequestMethod("GET");
//                            String bearerAuth = "Bearer " + PreferencesHelper.getInstance().getAccessToken(getContext());
//                            long version = DBHelper.getInstance().getLastModifiedTime(getContext(), PreferencesHelper.getInstance().getCurrentUserId(getContext()), ProviderContract.SyncEntry.LAST_SYNC);
//                            connection.setRequestProperty("Authorization", bearerAuth);
//                            connection.setRequestProperty("Sync-Time", String.valueOf(version));
//                            int code = connection.getResponseCode();
//                            int b = 5;
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//
//            t1.start();

//                SET @poly =
//                ST_GeomFromText('Polygon((43.243515 27.964009,
//                43.244822 27.865874,
//                        43.179915 27.860311,
//                        43.161692 28.008654,
//                        43.243515 27.964009))');
//
//                set @pointe = ST_GeomFromText('Point(43.218358 27.918432)');
//                select st_within(@poly, @pointe);

//                mysql> SET @ls = 'LineString(1 1,2 2,3 3)';
//                mysql> SELECT ST_AsText(ST_PointN(ST_GeomFromText(@ls),2));

//            Thread t1 = new Thread(new Runnable() {
//                @Override
//                public void run() {
//
//
//                        for(int i = 0; i < 5; i++) {
//                            try {
//                                //HttpsURLConnection connection = null;
////                        HttpsClient httpsClient = new HttpsClient(API.checkSync, getContext());
////                        connection = httpsClient.setUpHttpsConnection();
//                                URL url = new URL(API.checkSync);
//                                HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
//                                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//
//                                connection.setSSLSocketFactory(HttpsClient.sslContext.getSocketFactory());
//
//                                String bearerAuth = "Bearer " + PreferencesHelper.getInstance().getAccessToken(getContext());
//                                long version = DBHelper.getInstance().getLastModifiedTime(getContext(), PreferencesHelper.getInstance().getCurrentUserId(getContext()), ProviderContract.SyncEntry.LAST_SYNC);
//
//                                connection.setDoOutput(true);
//                                connection.setInstanceFollowRedirects(false);
//                                connection.setRequestMethod("POST");
//                                connection.setRequestProperty("Content-Type", "application/json");
//                                connection.setRequestProperty("Connection", "keep-alive");
//
//                                connection.setRequestProperty("Authorization", bearerAuth);
//                                connection.setRequestProperty("Sync-Time", String.valueOf(version));
//                                connection.setRequestProperty("Content-Length", String.valueOf(1));
//
//                                connection.getOutputStream().write(String.valueOf(i).getBytes(Charset.forName("UTF-8")));
//                                //connection.getInputStream().close();
//                                connection.disconnect();
//
//                                Thread.sleep(100);
//
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                 e.printStackTrace();
//                            }
//                        }
//
//
//                }
//            });
//
//                t1.start();
//
//            }
//

            }
        });


    }
}

