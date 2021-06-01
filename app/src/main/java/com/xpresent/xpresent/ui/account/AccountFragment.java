package com.xpresent.xpresent.ui.account;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.requests.ServerConnector;
import com.xpresent.xpresent.ui.auth.AuthorizationActivity;
import com.xpresent.xpresent.ui.catalog.ImpressionsFragment;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    private String sessionKey;
    private View view;
    private Context Activity;
    private int cashback;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        Activity = this.getActivity();
        TextView toolbarTitle = view.findViewById(R.id.Title);
        toolbarTitle.setText(R.string.my_account);

        // get app key from storage
        SharedPreferences settings = getActivity().getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        sessionKey = settings.getString("sessionKey", "");
        String clientName = settings.getString("clientName", "");
        String clientPhotoUrl = settings.getString("clientPhoto", "");
        ImageView clientPhotoIV = view.findViewById(R.id.clientPhoto);
        if(!clientPhotoUrl.equals("")) {
            Glide.with(view)
                    .load(clientPhotoUrl)
                    .apply(new RequestOptions().override(config.REVIEW_IMG_WIDTH, config.REVIEW_IMG_HEIGHT))
                    .placeholder(R.drawable.placeholder_details)
                    .centerCrop()
                    .into(clientPhotoIV);
        }
        TextView clientNameTV = view.findViewById(R.id.clientName);
        clientNameTV.setText(clientName);

        // get client cashback
        getCashBack();
        // Favorites
        CardView favoritesBtn = view.findViewById(R.id.favorites);
        favoritesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoritesFragment favoritesFragment = new FavoritesFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, favoritesFragment).addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        CardView logOffBtn = view.findViewById(R.id.logOffBtn);
        CardView logInBtn = view.findViewById(R.id.logInBtn);
        //check authorization
        if(sessionKey.isEmpty()) {
            logOffBtn.setVisibility(View.GONE);
            logInBtn.setVisibility(View.VISIBLE);
            logInBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onClickLogIn();
                }
            });
        }
        else {
            logInBtn.setVisibility(View.GONE);
            logOffBtn.setVisibility(View.VISIBLE);
            logOffBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onClickLogOff();
                }
            });
        }
        return view;
    }

    private void onClickLogOff(){
        SharedPreferences settings = getActivity().getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        settings.edit().clear().apply();
        AccountFragment accountFragment = new AccountFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, accountFragment).addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void onClickLogIn(){
        Bundle extras = new Bundle();
        extras.putString("redirect", "certs");
        Intent intent = new Intent(Activity, AuthorizationActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    private void getCashBack(){
        HashMap<String, String> mapPost = new HashMap<>();
        mapPost.put("action", "cashback");
        mapPost.put("session_key", sessionKey);
        ServerConnector Connector = new ServerConnector(Activity, new ServerConnector.AsyncResponse() {
            @Override
            public void processFinish(boolean success, String output) {
                if (success) {
                    try {
                        JSONObject jsonResult = new JSONObject(output);
                        boolean status = jsonResult.getBoolean("status");
                        if (status) {
                            TextView cashbackTxt = view.findViewById(R.id.cashback);
                            cashback = jsonResult.getInt("cashback");
                            String cashbackStr = cashback+" "+config.RUB;
                            cashbackTxt.setText(cashbackStr);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, false);
        Connector.execute(mapPost);
    }
}
