package com.xpresent.xpresent.ui.auth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.vk.api.sdk.VK;
import com.vk.api.sdk.VKApiCallback;
import com.vk.api.sdk.auth.VKAccessToken;
import com.vk.api.sdk.auth.VKAuthCallback;
import com.vk.api.sdk.auth.VKScope;
import com.xpresent.xpresent.MainActivity;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.model.vk.VKUser;
import com.xpresent.xpresent.requests.ServerConnector;
import com.xpresent.xpresent.requests.vk.VKUserCommand;
import com.xpresent.xpresent.ui.booking.BookActivity;
import com.xpresent.xpresent.ui.booking.BuyCertActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AuthVKActivity extends AppCompatActivity {
    private Activity activity;
    private String redirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_auth_v_k);

        Bundle extras = getIntent().getExtras();
        redirect = extras.getString("redirect");

        List<VKScope> scopes = new ArrayList<>();
        scopes.add(VKScope.PHOTOS);
        VK.login(this, scopes);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(!VK.onActivityResult(requestCode, resultCode, data, new VKAuthCallback() {
            @Override
            public void onLogin(@NotNull VKAccessToken vkAccessToken) {
                getUserInfo();
            }

            @Override
            public void onLoginFailed(int i) {
                //TODO show login screen
                //Toast.makeText(activity, "Login ERROR!", Toast.LENGTH_LONG).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getUserInfo() {
        VK.execute(new VKUserCommand(activity), new VKApiCallback<VKUser>(){
            @Override
            public void success(VKUser user) {
                HashMap<String,String> mapPost = new HashMap<>();
                mapPost.put("action", "auth");
                mapPost.put("type", "auth_by_vk");
                mapPost.put("vk_id", Integer.toString(user.getId()));
                mapPost.put("name", user.getFio());
                mapPost.put("phone", user.getPhone());
                mapPost.put("photo", user.getPhoto());
                // record user params to storage
                SharedPreferences settings = getSharedPreferences("xp_client", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("vk_id", user.getId());
                editor.putString("clientName", user.getFio());
                editor.putString("clientPhone", user.getPhone());
                editor.putString("clientPhoto", user.getPhoto());
                editor.apply();

                ServerConnector Connector = new ServerConnector(activity, new ServerConnector.AsyncResponse() {
                    @Override
                    public void processFinish(boolean success, String output) {
                        if(success)
                            showResult(output);
                        else Toast.makeText(activity, output, Toast.LENGTH_LONG).show();
                    }
                }, false);
                Connector.execute(mapPost);
            }

            @Override
            public void fail(@NotNull Exception e) {
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
            }

        });
    }

    private void showResult(String output) {
        try {
            JSONObject jsonResult = new JSONObject(output);
            boolean status = jsonResult.getBoolean("status");
            if (status) {
                String sessionKey = jsonResult.getString("key");
                String email = jsonResult.getString("email");
                // record sessionKey to storage
                SharedPreferences settings = getSharedPreferences("xp_client", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("sessionKey", sessionKey);
                editor.putString("clientEmail", email);
                editor.apply();
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
        } catch (
                JSONException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}