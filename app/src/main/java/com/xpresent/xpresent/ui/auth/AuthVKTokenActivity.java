package com.xpresent.xpresent.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.vk.api.sdk.VK;
import com.vk.api.sdk.VKTokenExpiredHandler;

public class AuthVKTokenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VK.addTokenExpiredHandler(tokenTracker);
    }

    private VKTokenExpiredHandler tokenTracker = new VKTokenExpiredHandler(){
        @Override
        public void onTokenExpired() {
            Intent intent = new Intent(getApplicationContext(), AuthVKActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    };
}