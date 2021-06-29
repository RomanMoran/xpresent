package com.xpresent.xpresent.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.xpresent.xpresent.MainActivity;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.adapter.CityAdapter;
import com.xpresent.xpresent.util.UtilKt;

public class CitySelectedActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private CityAdapter cityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cyti_selected);
        preferences = getSharedPreferences("xp_client", Context.MODE_PRIVATE);

        findViewById(R.id.backBtn).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.Title))
                .setText(UtilKt.string(this, R.string.choose_city));

        ListView citiesList = findViewById(R.id.citiesList);

        cityAdapter = new CityAdapter(this);
        String[] cities = UtilKt.stringArray(this, R.array.cities);
        String[] cities2 = UtilKt.stringArray(this, R.array.cities_declesnion);
        cityAdapter.addItem(1, cities[0], cities2[0]);
        cityAdapter.addItem(2, cities[1], cities2[1]);
        cityAdapter.addItem(8, cities[2], cities2[2]);
        citiesList.setAdapter(cityAdapter);

        citiesList.setOnItemClickListener((parent, item, position, id) -> selectCity(position));

    }

    private void selectCity(int position){
        CityAdapter.City city = (CityAdapter.City) cityAdapter.getItem(position);
        preferences.edit().putInt("cityId", city.getId()).apply();
        preferences.edit().putString("cityName", city.getName()).apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
