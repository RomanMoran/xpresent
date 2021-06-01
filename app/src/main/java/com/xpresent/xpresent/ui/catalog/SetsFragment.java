/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 24.05.20
 */

package com.xpresent.xpresent.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.adapter.ImpressionsAdapter;
import com.xpresent.xpresent.adapter.LoadMoreInterface;
import com.xpresent.xpresent.adapter.SelectedItem;
import com.xpresent.xpresent.adapter.SetsAdapter;
import com.xpresent.xpresent.model.Item;
import com.xpresent.xpresent.requests.ServerConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetsFragment extends Fragment implements SelectedItem {
    private Context Activity;
    private View view;
    private TextView catNameTV;
    private SetsAdapter adapter;
    private List<Item> items = new ArrayList<>();
    private String categoryName, search;
    private int to;
    private int cityId, maxPage, currentPage;
    private boolean isClicked;
    private Chip orderByMale, orderByFeMale, orderByCouple, orderByChild;

    SetsFragment(String catName, String src){
        categoryName = catName;
        currentPage = 1;
        search = (src == null) ? "" : src;
        to = 0;
        items.clear();
        isClicked = false;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sets, container, false);

        Activity = this.getActivity();

        catNameTV = view.findViewById(R.id.title);
        catNameTV.setText(categoryName);
        ImageView backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        // get city from storage
        SharedPreferences settings = Activity.getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        cityId = settings.getInt("cityId", 1);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(Activity, 2, GridLayoutManager.VERTICAL, false);
        RecyclerView setsRecycler = view.findViewById(R.id.setsRecycleView);
        setsRecycler.setLayoutManager(gridLayoutManager);

        adapter = new SetsAdapter(setsRecycler, Activity, items, this);
        setsRecycler.setAdapter(adapter);

        getSets(false, to);

        // auto loading of impressions into recyclerview
        adapter.setLoadMoreInterface(new LoadMoreInterface() {
            @Override
            public void onLoadMoreInterface() {
                if(currentPage<=maxPage) {
                    items.add(null); // show loading item
                    adapter.notifyItemInserted(items.size() - 1);
                    getSets(true, to);
                }
            }
        });

        // search
        RelativeLayout searchBtn = view.findViewById(R.id.searchBlock);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment searchFragment = new SearchFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, searchFragment).addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        // order by TO (male, female, ...)
        orderByMale = view.findViewById(R.id.orderByMale);
        orderByFeMale = view.findViewById(R.id.orderByFeMale);
        orderByCouple = view.findViewById(R.id.orderByCouple);
        orderByChild = view.findViewById(R.id.orderByChild);
        orderByMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to = orderByMale.isChecked() ? 1 : 0;
                getSertsByTO(to);
                orderByFeMale.setChecked(false);
                orderByCouple.setChecked(false);
                orderByChild.setChecked(false);
            }
        });

        orderByFeMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to = orderByFeMale.isChecked() ? 2 : 0;
                getSertsByTO(to);
                orderByMale.setChecked(false);
                orderByCouple.setChecked(false);
                orderByChild.setChecked(false);
            }
        });

        orderByCouple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to = orderByCouple.isChecked() ? 15 : 0;
                getSertsByTO(to);
                orderByFeMale.setChecked(false);
                orderByMale.setChecked(false);
                orderByChild.setChecked(false);
            }
        });

        orderByChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to = orderByChild.isChecked() ? 18 : 0;
                getSertsByTO(to);
                orderByFeMale.setChecked(false);
                orderByCouple.setChecked(false);
                orderByMale.setChecked(false);
            }
        });

        return view;
    }

    private void getSertsByTO(int to){
        currentPage = 1;
        items.clear();
        getSets(false, to);
    }

    private void getSets(final boolean showLoading, int to){
        HashMap<String,String> mapPost = new HashMap<>();
        mapPost.put("action", "getsets");
        mapPost.put("page", Integer.toString(currentPage));
        mapPost.put("city", Integer.toString(cityId));
        if(to != 0) {
            mapPost.put("to", Integer.toString(to));
        }
        if(!search.equals(""))
            mapPost.put("search", search);

        ServerConnector Connector = new ServerConnector(Activity, new ServerConnector.AsyncResponse() {
            @Override
            public void processFinish(boolean success, String output) {
                if(showLoading){
                    items.remove(items.size()-1);
                    adapter.notifyItemRemoved(items.size());
                }
                if(success)
                    showResult(output);
                else Toast.makeText(Activity, getResources().getString(R.string.no_server_connection), Toast.LENGTH_LONG).show();
            }
        }, !showLoading);
        Connector.execute(mapPost);
    }

    private void showResult(String result){
        try {
            JSONObject jsonResult = new JSONObject(result);
            boolean status = jsonResult.getBoolean("status");
            if (status) {
                String maxPageStr = jsonResult.getString("max_page");
                String Title = categoryName+" ("+jsonResult.getString("num_items")+")";
                catNameTV.setText(Title);
                maxPage =Integer.parseInt(maxPageStr);
                JSONArray sets = jsonResult.getJSONArray("sets");
                int setCount = sets.length();
                if(setCount > 0) {
                    for (int i = 0; i < setCount; i++) {
                        JSONObject set = sets.getJSONObject(i);
                        Map<String, String> impParams = new HashMap<>();
                        impParams.put("id", set.getString("id"));
                        impParams.put("name", set.getString("name"));
                        impParams.put("impression_count", set.getString("impression_count"));
                        impParams.put("img", set.getString("img"));
                        impParams.put("price", set.getString("price"));
                        impParams.put("old_price", set.getString("old_price"));
                        impParams.put("tag", set.getString("tag"));
                        impParams.put("tag_color", set.getString("tag_color"));
                        Item item = new Item(impParams, i);
                        // add Set data to list
                        items.add(item);
                    }
                    currentPage++;
                    adapter.notifyDataSetChanged();
                    adapter.setLoaded();
                }
                else{
                    TextView noData = view.findViewById(R.id.noSets);
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

    /**
     * onClick on RecyclerView (list of Impressions)
     * @param item instance
     */
    @Override
    public void selectedItem(Item item) {
        // restrict double clicking
        if(!isClicked) {
            isClicked = true;
            Bundle extras = new Bundle();
            extras.putInt("setId", Integer.parseInt(item.getParam("id")));
            extras.putString("setName", item.getParam("name"));
            Intent intent = new Intent(getActivity(), SetActivity.class);
            intent.putExtras(extras);
            startActivity(intent);
            isClicked = false;
        }
    }
}