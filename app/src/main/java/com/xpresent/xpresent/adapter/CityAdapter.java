/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 25.05.20 10:14
 */
package com.xpresent.xpresent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.xpresent.xpresent.R;
import java.util.ArrayList;

public class CityAdapter extends BaseAdapter {
    private LayoutInflater inflter;
    private ArrayList<City> citiesList = new ArrayList<>();

    public CityAdapter(Context applicationContext) {
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return citiesList.size();
    }

    @Override
    public Object getItem(int i) {
        return citiesList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return citiesList.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.item_city_list, null);
        TextView cityText = view.findViewById(R.id.name);
        cityText.setText(citiesList.get(i).getName());
        return view;
    }

    public void addItem(int id, String name, String name_conv) {
        citiesList.add(new City(id, name, name_conv));
    }

    public static class City{
        private int id;
        private String name, nameIn;

        City(int cid, String cname, String namein){
            id = cid;
            name = cname;
            nameIn = namein;
        }

        public int getId(){return id;}
        public String getName(){return name;}
        public String getNameIn(){return nameIn;}
    }
}