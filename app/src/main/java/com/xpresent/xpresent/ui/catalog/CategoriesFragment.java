package com.xpresent.xpresent.ui.catalog;

import android.content.Context;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xpresent.xpresent.R;
import com.xpresent.xpresent.adapter.CatAdapter;
import com.xpresent.xpresent.adapter.SelectedItem;
import com.xpresent.xpresent.model.Item;
import com.xpresent.xpresent.requests.ServerConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriesFragment extends Fragment implements SelectedItem {
    private Context Activity;
    private CatAdapter adapter;
    private List<Item> items = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        Activity = this.getActivity();

        // get city from storage
        SharedPreferences settings = Activity.getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        String cityName = settings.getString("cityName", "Москва");
        TextView citiesTxt = view.findViewById(R.id.spin_city_name);
        citiesTxt.setText(cityName);

        // City is clicked
        citiesTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CitiesFragment citiesFragment = new CitiesFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, citiesFragment).addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        RecyclerView categoriesRecycler = view.findViewById(R.id.categories_list);
        categoriesRecycler.setLayoutManager(new LinearLayoutManager(Activity));

        adapter = new CatAdapter(Activity, items, this);
        categoriesRecycler.setAdapter(adapter);

        // get categories from server
        HashMap<String,String> mapPost = new HashMap<>();
        mapPost.put("action", "getcategories");

        if(items.isEmpty()) {
            ServerConnector Connector = new ServerConnector(Activity, new ServerConnector.AsyncResponse() {
                @Override
                public void processFinish(boolean success, String output) {
                    if (success)
                        showResult(output);
                    else
                        Toast.makeText(Activity, getResources().getString(R.string.no_server_connection), Toast.LENGTH_LONG).show();
                }
            }, true);
            Connector.execute(mapPost);
        }

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
        return view;
    }

    private void showResult(String result){
        try {
            JSONObject jsonResult = new JSONObject(result);
            boolean status = jsonResult.getBoolean("status");
            if (status) {
                JSONArray categories = jsonResult.getJSONArray("categories");
                for (int i = 0; i < categories.length(); i++) {
                    JSONObject category = categories.getJSONObject(i);
                    Map<String, String> catParams = new HashMap<>();
                    int catId = category.getInt("id");
                    catParams.put("id", Integer.toString(catId));
                    catParams.put("categoryName", category.getString("name"));
                    catParams.put("imageUrl", category.getString("img"));
                    Item item = new Item(catParams, i+1);
                    items.add(item);
                }
                adapter.notifyDataSetChanged();
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
     * onClick on RecyclerView (list of Categories)
     * @param categoryItem instance
     */
    @Override
    public void selectedItem(Item categoryItem) {
        int categoryId = Integer.parseInt(categoryItem.getParam("id"));
        String categoryName = categoryItem.getParam("categoryName");
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // sets
        if(categoryId == 100){
            SetsFragment setsFragment = new SetsFragment(categoryName, null);
            fragmentTransaction.replace(R.id.nav_host_fragment, setsFragment).addToBackStack(null);
        }
        // impressions
        else{
            ImpressionsFragment impressionsFragment = new ImpressionsFragment(categoryId, categoryName, null);
            fragmentTransaction.replace(R.id.nav_host_fragment, impressionsFragment).addToBackStack(null);
        }
        fragmentTransaction.commit();
    }
}