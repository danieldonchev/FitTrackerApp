package com.daniel.FitTrackerApp.fragments;


import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.daniel.FitTrackerApp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterFragment extends Fragment
{
    private Button registerButton;
    private EditText emailText, passwordText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.dialog_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);


        emailText = (EditText) view.findViewById(R.id.emailText);
        passwordText = (EditText) view.findViewById(R.id.passwordText);
        registerButton = (Button) view.findViewById(R.id.registerDialogButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                JSONObject jsonObject = new JSONObject();
                try
                {
                    jsonObject.put("email", emailText.getText().toString());
                    jsonObject.put("password", passwordText.getText().toString());
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                RegisterSender registerSender = new RegisterSender(jsonObject.toString());
                registerSender.execute();

                getActivity().finish();
            }
        });
    }


    private class RegisterSender extends AsyncTask<Void, Void, Void>
    {
        private String userData;

        public RegisterSender(String userData)
        {
            this.userData = userData;
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
//            HttpsURLConnection connection = ht.setUpHttpsConnection("https://192.168.0.105:8181/user/register", getActivity());
//            try
//            {
//                connection.setDoOutput(true);
//                connection.setInstanceFollowRedirects(false);
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//
//                connection.getOutputStream().write(userData.getBytes(Charset.forName("UTF-8")));
//
//                InputStream in = new BufferedInputStream(connection.getInputStream());
//                String string = AppUtils.readStream(in);
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//            finally {
//                connection.disconnect();
//            }
            return null;
        }
    }
}
