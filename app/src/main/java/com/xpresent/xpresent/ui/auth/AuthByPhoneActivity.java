package com.xpresent.xpresent.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
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

import com.xpresent.xpresent.R;
import com.xpresent.xpresent.lib.Utility;
import com.xpresent.xpresent.requests.ServerConnector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthByPhoneActivity extends AppCompatActivity {

    private Context Activity = this;
    private String clientPhone;
    private String redirect;
    private EditText etPhone;
    private Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_by_phone);

        TextView toolbarTitle = findViewById(R.id.Title);
        toolbarTitle.setText(R.string.action_login_phone);

        ImageView btnBack = findViewById(R.id.backBtn);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        etPhone = findViewById(R.id.etPhone);
        etPhone.requestFocus();

        buttonSend = findViewById(R.id.btnSendOTC);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickSendOTAC();
            }
        });

        // press enter on keyboard
        etPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ( (actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN ))){
                    onClickSendOTAC();
                }
                return true;
            }
        });

        Bundle extras = getIntent().getExtras();
        redirect = extras.getString("redirect");
    }

    public void onClickSendOTAC(){
        String phone = etPhone.getText().toString();
        EditText etPhoneCode = findViewById(R.id.etPhoneCode);
        String phoneCode = etPhoneCode.getText().toString();
        if(!Utility.isPhoneValid(phone) || phoneCode.equals("")){
            Toast.makeText(this, getResources().getString(R.string.enter_valid_phone), Toast.LENGTH_LONG).show();
        }
        else {
            buttonSend.setEnabled(false);
            clientPhone = phoneCode+phone;
            HashMap<String,String> mapPost = new HashMap<>();
            mapPost.put("action", "auth");
            mapPost.put("phone", clientPhone);

            ServerConnector Connector = new ServerConnector(Activity, new ServerConnector.AsyncResponse() {
                @Override
                public void processFinish(boolean success, String output) {
                    if(success)
                        showResult(output);
                    else Toast.makeText(Activity, output, Toast.LENGTH_LONG).show();
                }
            }, true);
            Connector.execute(mapPost);
        }
    }

    private void showResult(String output) {
        try {
            buttonSend.setEnabled(true);
            JSONObject jsonResult = new JSONObject(output);
            boolean status = jsonResult.getBoolean("status");
            if (status) {
                String sessionKey = jsonResult.getString("key");
                String email = jsonResult.getString("email");
                String name = jsonResult.getString("name");

                Bundle extras = new Bundle();
                extras.putString("type", "phone");
                extras.putString("sessionKey", sessionKey);
                extras.putString("clientPhone", clientPhone);
                extras.putString("clientName", name);
                extras.putString("clientEmail", email);
                extras.putString("redirect", redirect);
                Intent intent = new Intent(this, OtacActivity.class);
                intent.putExtras(extras);
                startActivity(intent);
            }
            else{
                Toast.makeText(this,  jsonResult.getString("error"), Toast.LENGTH_LONG).show();
            }
        } catch (
                JSONException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // show keyboard
        etPhone.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager)
                        Activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(etPhone, 0);
            }
        },200);
    }
}