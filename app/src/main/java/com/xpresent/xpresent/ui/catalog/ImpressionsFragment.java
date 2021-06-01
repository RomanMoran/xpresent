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
import com.xpresent.xpresent.adapter.SelectedItem;
import com.xpresent.xpresent.adapter.LoadMoreInterface;
import com.xpresent.xpresent.model.Item;
import com.xpresent.xpresent.requests.ServerConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImpressionsFragment extends Fragment implements SelectedItem {
    private Context Activity;
    private View view;
    private TextView catNameTV;
    private ImpressionsAdapter adapter;
    private List<Item> items = new ArrayList<>();
    private String categoryName, search, orderBy, order;
    private int cityId, categoryId, maxPage, currentPage;
    private boolean isClicked;
    private String currentOrder;

    ImpressionsFragment(int catId, String catName, String src){
        categoryName = catName;
        categoryId = catId;
        currentPage = 1;
        search = (src == null) ? "" : src;
        orderBy = "star";
        order = currentOrder = "DESC";
        items.clear();
        isClicked = false;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_impressions, container, false);

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

        RecyclerView impressionsRecycler = view.findViewById(R.id.impressionsRecycleView);
        impressionsRecycler.setLayoutManager(new LinearLayoutManager(Activity));

        adapter = new ImpressionsAdapter(impressionsRecycler, Activity, items, this);
        impressionsRecycler.setAdapter(adapter);

        getImpressions(false);

        // auto loading of impressions into recyclerview
        adapter.setLoadMoreInterface(new LoadMoreInterface() {
            @Override
            public void onLoadMoreInterface() {
                if(currentPage<=maxPage) {
                    items.add(null); // show loading item
                    adapter.notifyItemInserted(items.size() - 1);
                    getImpressions(true);
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

        final Chip orderByPrice = view.findViewById(R.id.orderByPrice);
        final Chip orderByRating = view.findViewById(R.id.orderByRating);
        // order by Price
        orderByPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentOrder = currentOrder.equals("DESC") ? "ASC" : "DESC";
                currentPage = 1;
                items.clear();
                orderBy = "price";
                order = currentOrder; // from low to high
                orderByPrice.setChecked(true);
                orderByRating.setChecked(false);
                getImpressions(false);
            }
        });
        // order by Rating
        orderByRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentOrder = currentOrder.equals("DESC") ? "ASC" : "DESC";
                currentPage = 1;
                items.clear();
                orderBy = "star";
                order = currentOrder; // from high to low
                orderByPrice.setChecked(false);
                orderByRating.setChecked(true);
                getImpressions(false);
            }
        });

        return view;
    }

    private void getImpressions(final boolean showLoading){
        HashMap<String,String> mapPost = new HashMap<>();
        mapPost.put("action", "getimpressions");
        mapPost.put("rank", Integer.toString(categoryId));
        mapPost.put("page", Integer.toString(currentPage));
        mapPost.put("city", Integer.toString(cityId));
        if(!orderBy.equals("")) {
            mapPost.put("orderby", orderBy);
            mapPost.put("order", order);
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
                JSONArray impressions = jsonResult.getJSONArray("impressions");
                int impCount = impressions.length();
                if(impCount > 0) {
                    for (int i = 0; i < impCount; i++) {
                        JSONObject impression = impressions.getJSONObject(i);
                        Map<String, String> impParams = new HashMap<>();
                        impParams.put("id", impression.getString("id"));
                        impParams.put("name", impression.getString("name"));
                        impParams.put("content_item", impression.getString("content_item"));
                        impParams.put("img", impression.getString("img"));
                        impParams.put("price", impression.getString("price"));
                        impParams.put("old_price", impression.getString("old_price"));
                        impParams.put("tag", impression.getString("tag"));
                        impParams.put("tag_color", impression.getString("tag_color"));
                        impParams.put("grade",  impression.getString("star"));
                        impParams.put("reviews_count", impression.getString("reviews_count"));
                        Item item = new Item(impParams, i);
                        // add Impression data to list
                        items.add(item);
                    }
                    currentPage++;
                    adapter.notifyDataSetChanged();
                    adapter.setLoaded();
                }
                else{
                    TextView noData = view.findViewById(R.id.noImpressions);
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
            extras.putInt("impressionId", Integer.parseInt(item.getParam("id")));
            extras.putString("impressionName", item.getParam("name"));
            Intent intent = new Intent(getActivity(), ImpressionActivity.class);
            intent.putExtras(extras);
            startActivity(intent);
            isClicked = false;
        }
    }
}