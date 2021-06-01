/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 10.06.20
 */

package com.xpresent.xpresent.ui.catalog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.xpresent.xpresent.R;
import com.xpresent.xpresent.adapter.CityAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CitiesFragment extends Fragment{
    private Context Activity;
    private CityAdapter cityAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cities, container, false);
        Activity = this.getActivity();
        Resources res = getResources();

        TextView toolbarTitle = view.findViewById(R.id.Title);
        toolbarTitle.setText(res.getString(R.string.choose_city));
        ImageView backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        ListView citiesList = view.findViewById(R.id.citiesList);

        cityAdapter = new CityAdapter(Activity);
        String[] cities = res.getStringArray(R.array.cities);
        String[] cities2 = res.getStringArray(R.array.cities_declesnion); // word with declension
        cityAdapter.addItem(1, cities[0], cities2[0]);
        cityAdapter.addItem(2, cities[1], cities2[1]);
        cityAdapter.addItem(8, cities[2], cities2[2]);
        citiesList.setAdapter(cityAdapter);

        // List view is clicked
        citiesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                selectCity(position);
            }
        });
        return view;
    }

    private void selectCity(int position){
        // record city to storage
        SharedPreferences settings = Activity.getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        CityAdapter.City city =   (CityAdapter.City) cityAdapter.getItem(position);
        editor.putInt("cityId",city.getId());
        editor.putString("cityName",city.getName());
        editor.apply();
        CategoriesFragment categoriesFragment = new CategoriesFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, categoriesFragment).addToBackStack(null);
        fragmentTransaction.commit();
    }

}
