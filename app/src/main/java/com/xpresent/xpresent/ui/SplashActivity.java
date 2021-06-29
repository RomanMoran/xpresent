package com.xpresent.xpresent.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.xpresent.xpresent.MainActivity;
import com.xpresent.xpresent.config.config;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        if (preferences.getBoolean(config.PRF_FIRST_START, true)) {
            preferences.edit().putBoolean(config.PRF_FIRST_START, false).apply();
            start(CitySelectedActivity.class);
        } else {
            start(MainActivity.class);
        }
    }

    private void start(Class<?> cls) {
        startActivity(new Intent(this, cls));
        finish();
    }
}