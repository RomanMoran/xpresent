package com.xpresent.xpresent.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xpresent.xpresent.MainActivity;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.requests.ServerConnector;
import com.xpresent.xpresent.ui.booking.BookActivity;
import com.xpresent.xpresent.ui.booking.BuyCertActivity;

import java.util.HashMap;

import static com.xpresent.xpresent.lib.Utility.sha512;

public class OtacActivity extends AppCompatActivity {
    private String sessionKey = "", clientPhone, clientEmail, clientName, redirect;
    private EditText smsCodeET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otac);

        TextView toolbarTitle = findViewById(R.id.Title);
        toolbarTitle.setText(R.string.smsCode);

        ImageView btnBack = findViewById(R.id.backBtn);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Bundle extras = getIntent().getExtras();
        sessionKey = extras.getString("sessionKey");
        clientPhone = extras.getString("clientPhone");
        clientEmail = extras.getString("clientEmail");
        clientName = extras.getString("clientName");
        redirect = extras.getString("redirect");

        smsCodeET = findViewById(R.id.etSMSCode);
        smsCodeET.requestFocus();

        // press enter on keyboard
        smsCodeET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ( (actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN ))){
                    onClickAuth();
                }
                return true;
            }
        });

        Button buttonBook = findViewById(R.id.btnAuth);
        buttonBook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickAuth();
            }
        });
    }

    private void onClickAuth() {
        String smsCode = smsCodeET.getText().toString();
        String pass = config.PASSWORD_APP;
        String hash = sha512(smsCode+pass);
        // if SMS hash code equals hash from server
        if(hash.equals(sessionKey)){
            // record sessionKey to storage
            SharedPreferences settings = getSharedPreferences("xp_client", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("sessionKey", sessionKey);
            editor.putString("clientPhone", clientPhone);
            if(!clientEmail.equals("null"))
                editor.putString("clientEmail", clientEmail);
            if(!clientName.equals("null"))
                editor.putString("clientName", clientName);
            editor.apply();

            // set Session active
            HashMap<String,String> mapPost = new HashMap<>();
            mapPost.put("action", "auth");
            mapPost.put("type", "set_session_active");
            mapPost.put("session_key", sessionKey);
            ServerConnector Connector = new ServerConnector(this, new ServerConnector.AsyncResponse() {
                @Override
                public void processFinish(boolean success, String output) {}
            }, false);
            Connector.execute(mapPost);

            if(redirect.equals("book")) {
                Intent intent = new Intent(this, BookActivity.class);
                startActivity(intent);
            }
            if(redirect.equals("buycert")) {
                Intent intent = new Intent(this, BuyCertActivity.class);
                startActivity(intent);
            }
            else{
                // TODO start CertificatesFragment
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
        else Toast.makeText(this, getResources().getString(R.string.invalid_sms_code), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // show keyboard
        smsCodeET.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(smsCodeET, 0);
            }
        },200);
    }
}