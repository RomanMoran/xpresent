package com.xpresent.xpresent.ui.certificates;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.adapter.OrdersListAdapter;
import com.xpresent.xpresent.adapter.SelectedItem;
import com.xpresent.xpresent.model.Item;
import com.xpresent.xpresent.requests.ServerConnector;
import com.xpresent.xpresent.ui.auth.AuthorizationActivity;
import com.xpresent.xpresent.ui.booking.BuyCertActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CertificatesFragment extends Fragment  implements SelectedItem {
    private Context Activity;
    private View view;
    private String sessionKey;
    private OrdersListAdapter ordersAdapter;
    private List<Item> orders = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Activity = getActivity();
        // get app key from storage
        SharedPreferences settings = Activity.getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        sessionKey = settings.getString("sessionKey", "");

        //check authorization
        if(sessionKey.isEmpty()) {
            view = inflater.inflate(R.layout.fragment_certificates_login, container, false);
            Button logInBtn = view.findViewById(R.id.btnLogIn);
            logInBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onClickLogIn();
                }
            });
        }
        else {
            view = inflater.inflate(R.layout.fragment_certificates, container, false);
            TextView toolbarTitle = view.findViewById(R.id.Title);
            toolbarTitle.setText(R.string.title_toolbar_my_certificates);
            RecyclerView certificatesRecycler = view.findViewById(R.id.certificatesRecycleView);
            certificatesRecycler.setLayoutManager(new LinearLayoutManager(Activity));

            ordersAdapter = new OrdersListAdapter(Activity, orders, this);
            certificatesRecycler.setAdapter(ordersAdapter);

            if(orders.isEmpty()) {
                getOrders();
            }
        }
        /*MaterialButton activateButton = view.findViewById(R.id.btnActivate);
        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check authorization
                if (!sessionKey.isEmpty()) {
                    Intent intent = new Intent(Activity, ActivateActivity.class);
                    startActivity(intent);
                }
            }
        });*/
        return view;
    }

    private void getOrders(){
        HashMap<String,String> mapPost = new HashMap<>();
        mapPost.put("action", "getorders");
        mapPost.put("session_key", sessionKey);

        ServerConnector Connector = new ServerConnector(Activity, new ServerConnector.AsyncResponse() {
            // here we get result output from POST request
            @Override
            public void processFinish(boolean success, String output) {
                if(success)
                    showResult(output);
                else Toast.makeText(Activity, output, Toast.LENGTH_LONG).show();
            }
        }, true);
        Connector.execute(mapPost);
    }

    private void showResult(String result){
        try {
            JSONObject jsonResult = new JSONObject(result);
            boolean status = jsonResult.getBoolean("status");
            if (status) {
                JSONArray orders = jsonResult.getJSONArray("orders");
                if(orders.length() > 0) {
                    for (int i = 0; i < orders.length(); i++) {
                        JSONObject order = orders.getJSONObject(i);
                        JSONArray items = order.getJSONArray("items");
                        for (int j = 0; j < items.length(); j++) {
                            JSONObject item = items.getJSONObject(j);
                            // order params
                            Map<String, String> certParams = new HashMap<>();
                            certParams.put("order_id", order.getString("id"));
                            certParams.put("status_bank", order.getString("status_bank"));
                            certParams.put("sum", order.getString("sum"));
                            certParams.put("date_insert", order.getString("date_insert"));
                            // certificate params
                            certParams.put("cert_id", item.getString("id"));
                            certParams.put("offer_id", item.getString("item_id"));
                            certParams.put("status", item.getString("status"));
                            certParams.put("item_name", item.getString("item_name"));
                            certParams.put("img", item.getString("img"));
                            certParams.put("cert_number", item.getString("potentialName"));
                            certParams.put("secret_code", item.getString("secret_code"));
                            certParams.put("item_sum", item.getString("item_sum"));
                            certParams.put("closed", item.getString("closed"));
                            certParams.put("closed_date_redeemed", item.getString("closed_date_redeemed"));
                            certParams.put("partner_id", item.getString("partner_id"));
                            certParams.put("impression_name", item.getString("impression_name"));
                            certParams.put("human_name", item.getString("human_name"));
                            certParams.put("duration_name", item.getString("duration_name"));

                            Item certItem = new Item(certParams, i);
                            this.orders.add(certItem);
                            ordersAdapter.notifyDataSetChanged();
                        }
                    }
                }
                else {
                    TextView noData = view.findViewById(R.id.noData);
                    noData.setVisibility(View.VISIBLE);
                }
            }
            else{
                String message = jsonResult.getString("message");
                Toast.makeText(Activity, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e){
            Toast.makeText(Activity, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void onClickLogIn(){
        Bundle extras = new Bundle();
        extras.putString("redirect", "certs");
        Intent intent = new Intent(Activity, AuthorizationActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    public void selectedItem(Item item){
        CertificateItemFragment certificateItemFragment = new CertificateItemFragment(item);
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, certificateItemFragment).addToBackStack(null);
        fragmentTransaction.commit();
    }

}