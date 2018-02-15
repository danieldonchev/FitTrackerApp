package com.daniel.FitTrackerApp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daniel.FitTrackerApp.AppNetworkManager;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.authenticate.Users.LocalUser;
import com.daniel.FitTrackerApp.authenticate.Logins.LocalLogin;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.IntentServiceResultReceiver;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginDialog extends Dialog implements IntentServiceResultReceiver.Receiver
{
    private RelativeLayout loginLayout, codeLayout, sendEmailLayout;
    private EditText loginEmail, loginPassword, loginRepeatPassword, confirmCode, confirmPassword, confirmRepeatPassword;
    private EditText sendEmail, emailText, confirmEmailText;
    private TextView forgottenPassword, enterCode, errorText, codeErrorText;
    private Button loginButton, sendEmailButton, confirmCodeButton;
    private Context context;


    public LoginDialog(Context context, OnDismissListener onDismissListener)
    {
        super(context, android.R.style.Theme_Black_NoTitleBar);
        setContentView(R.layout.dialog_login);

        this.context = context;

        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));
        setOnDismissListener(onDismissListener);
        setCancelable(true);

        loginLayout = (RelativeLayout) findViewById(R.id.loginLayout);
        codeLayout = (RelativeLayout) findViewById(R.id.confirmLayout);
        sendEmailLayout = (RelativeLayout) findViewById(R.id.forgottenPasswordLayout);

        //login layout
        loginEmail = (EditText) findViewById(R.id.loginEmailText);
        loginPassword = (EditText) findViewById(R.id.passwordText);

        forgottenPassword = (TextView) findViewById(R.id.forgottenPassText);
        enterCode = (TextView) findViewById(R.id.enterPasswordCode);

        loginButton = (Button) findViewById(R.id.loginDialogButton);

        //sendEmailLayout
        emailText = (EditText) findViewById(R.id.emailText);
        sendEmailButton = (Button) findViewById(R.id.sendButton);
        errorText = (TextView) findViewById(R.id.errorText);

        //codeLayout
        confirmCode = (EditText) findViewById(R.id.codeEditText);
        confirmPassword = (EditText) findViewById(R.id.codePasswordText);
        confirmRepeatPassword = (EditText) findViewById(R.id.codeRepeatPassword);
        confirmEmailText = (EditText) findViewById(R.id.confirmEmailText);
        confirmCodeButton = (Button) findViewById(R.id.confirmCodeButton);
        codeErrorText = (TextView) findViewById(R.id.codeErrorText);

        codeLayout.setVisibility(View.GONE);
        sendEmailLayout.setVisibility(View.GONE);

        loginButton.setOnClickListener(onClickListener);
        forgottenPassword.setOnClickListener(onClickListener);
        enterCode.setOnClickListener(onClickListener);
        sendEmailButton.setOnClickListener(onClickListener);
        confirmCodeButton.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.loginDialogButton:{
                    if(AppUtils.isValidEmail(loginEmail.getText().toString()))
                    {
                            LocalLogin basicLogin = new LocalLogin(context);
                            basicLogin.authenticate(new LocalUser(loginEmail.getText().toString(), loginPassword.getText().toString()));
                    }
                    else
                    {
                        Toast.makeText(context, "Email address is invalid.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.forgottenPassText:{
                    loginLayout.setVisibility(View.GONE);
                    sendEmailLayout.setVisibility(View.VISIBLE);
                    break;
                }
                case R.id.enterPasswordCode:{
                    loginLayout.setVisibility(View.GONE);
                    sendEmailLayout.setVisibility(View.GONE);
                    codeLayout.setVisibility(View.VISIBLE);
                    break;
                }
                case R.id.sendButton:{
                    if (AppUtils.isValidEmail(emailText.getText().toString())) {
                        IntentServiceResultReceiver mReceiver = new IntentServiceResultReceiver(new Handler());
                        mReceiver.setReceiver(LoginDialog.this);
                        AppNetworkManager.getForgottenPasswordToken(getContext(), emailText.getText().toString(), mReceiver);
                    } else {
                        Toast.makeText(context, "Email address is invalid.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case R.id.confirmCodeButton:{
                    try {
                        JSONObject data = new JSONObject();
                        data.put("email", confirmEmailText.getText().toString());
                        data.put("password", confirmPassword.getText().toString());
                        data.put("code", confirmCode.getText().toString());
                        IntentServiceResultReceiver mReceiver = new IntentServiceResultReceiver(new Handler());
                        mReceiver.setReceiver(LoginDialog.this);
                        AppNetworkManager.sendChangePassword(getContext(), data, mReceiver);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if(resultCode == 1){
            int responseCode = resultData.getInt("responseCode");
            if(responseCode == 200){
                errorText.setText("An email with a code for your password retrieval has been sent!");
                codeLayout.setVisibility(View.VISIBLE);
                loginLayout.setVisibility(View.GONE);
                sendEmailLayout.setVisibility(View.GONE);
            } else if(responseCode == 404){
                errorText.setText("An account with that email does not exist. Try again.");
            } else {
                errorText.setText("An error occured.");
            }
        } else if(resultCode == 2){
            int responseCode = resultData.getInt("responseCode");
            if(responseCode == 200){
                Toast.makeText(getContext(), "Password changed successfuly", Toast.LENGTH_LONG);
                codeLayout.setVisibility(View.GONE);
                sendEmailLayout.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
            } else if(responseCode == 404){
                codeErrorText.setText("Code has expired");
            } else {
                codeErrorText.setText("An error occured.");
            }
        }
    }
}
