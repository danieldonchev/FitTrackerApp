package com.daniel.FitTrackerApp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.daniel.FitTrackerApp.API;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.authenticate.Users.RegisterUser;
import com.daniel.FitTrackerApp.authenticate.Logins.Register;
import com.daniel.FitTrackerApp.utils.AppUtils;

public class RegisterDialog extends Dialog
{
    private Button registerButton;
    private Context context;

    public RegisterDialog(final Context context, OnDismissListener onDismissListener)
    {
        super(context, android.R.style.Theme_Black_NoTitleBar);
        setContentView(R.layout.dialog_register);

        this.context = context;

        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));
        setOnDismissListener(onDismissListener);
        setCancelable(true);

        registerButton = (Button) findViewById(R.id.registerDialogButton);
        registerButton.setOnClickListener(onClickListener);
        }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            EditText nameText = (EditText) findViewById(R.id.nameText);
            EditText emailText = (EditText) findViewById(R.id.emailText);
            EditText passwordText = (EditText) findViewById(R.id.passwordText);
            EditText repeatPasswordText = (EditText) findViewById(R.id.repeatPasswordText);

            if(AppUtils.isValidEmail(emailText.getText().toString()))
            {
                if(nameText.getText().length() >= 2){
                    if(passwordText.getText().length() >= 6){
                        if(passwordText.getText().toString().equals(repeatPasswordText.getText().toString())){
                            Register register = new Register(context);
                            register.authenticate(new RegisterUser(nameText.getText().toString(),
                                    emailText.getText().toString(),
                                    passwordText.getText().toString()


                                    ));
                        } else {
                            Toast.makeText(context, "Passwords don't match!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(context, "Password must be atleast 6 characters!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, "Name must be atleast 2 characters", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, "Email address is invalid.", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
