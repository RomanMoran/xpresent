package com.xpresent.xpresent.ui.account;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.adapter.ImpressionsAdapter;
import com.xpresent.xpresent.adapter.SelectedItem;
import com.xpresent.xpresent.model.Item;
import com.xpresent.xpresent.requests.ServerConnector;
import com.xpresent.xpresent.ui.catalog.ImpressionActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FavoritesFragment extends Fragment  implements SelectedItem {
    private View view;
    private Context Activity;
    private ImpressionsAdapter adapter;
    private List<Item> items = new ArrayList<>();
    private Set<String> set;
    private int cityId;

    public FavoritesFragment() {
        set = null;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Activity = this.getActivity();
        SharedPreferences settings = Activity.getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        set = settings.getStringSet("favorites", null);
        view = inflater.inflate(R.layout.fragment_favorites, container, false);
        ImageView backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                //getActivity().onBackPressed();
            }
        });
        // No favorites
        if(set == null){
            TextView noImpressions = view.findViewById(R.id.noFavorites);
            noImpressions.setVisibility(View.VISIBLE);
        }
        else {
            // get city from storage
            cityId = settings.getInt("cityId", 1);
            RecyclerView impressionsRecycler = view.findViewById(R.id.impressionsRecycleView);
            impressionsRecycler.setLayoutManager(new LinearLayoutManager(Activity));
            adapter = new ImpressionsAdapter(impressionsRecycler, Activity, items, this);
            impressionsRecycler.setAdapter(adapter);
            getImpressions(false);
        }
        return view;
    }

    private void getImpressions(final boolean showLoading){
        HashMap<String,String> mapPost = new HashMap<>();
        mapPost.put("action", "favorites");
        mapPost.put("city", Integer.toString(cityId));

        String[] favorites = set.toArray(new String[set.size()]);
        if(favorites.length > 0) {
            int i = 0;
            for (String favorite : favorites) {
                mapPost.put("id[" + i++ + "]", favorite);
            }
            ServerConnector Connector = new ServerConnector(Activity, new ServerConnector.AsyncResponse() {
                @Override
                public void processFinish(boolean success, String output) {
                    if (showLoading) {
                        items.remove(items.size() - 1);
                        adapter.notifyItemRemoved(items.size());
                    }
                    if (success)
                        showResult(output);
                    else
                        Toast.makeText(Activity, getResources().getString(R.string.no_server_connection), Toast.LENGTH_LONG).show();
                }
            }, !showLoading);
            Connector.execute(mapPost);
        }
        else{
            TextView noData = view.findViewById(R.id.noFavorites);
            noData.setVisibility(View.VISIBLE);
        }
    }

    private void showResult(String result){
        try {
            JSONObject jsonResult = new JSONObject(result);
            boolean status = jsonResult.getBoolean("status");
            if (status) {
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
                    adapter.notifyDataSetChanged();
                    adapter.setLoaded();
                }
                else{
                    TextView noData = view.findViewById(R.id.noFavorites);
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

    @Override
    public void selectedItem(Item item) {
        Bundle extras = new Bundle();
        extras.putInt("impressionId", Integer.parseInt(item.getParam("id")));
        extras.putString("impressionName", item.getParam("name"));
        Intent intent = new Intent(getActivity(), ImpressionActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }
}