package com.masoud.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CitySearchAdapter extends ArrayAdapter<CitySearch> {
    public CitySearchAdapter(Context context, ArrayList<CitySearch> cityList) {
        super(context, 0, cityList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get data for current item
        CitySearch city = getItem(position);

        // Inflate our custom row layout
        if (convertView == null) convertView = LayoutInflater.from(getContext()).inflate(R.layout.city_search_row, parent, false);

        // Get references to the views in our custom row layout
        TextView csrIndex = convertView.findViewById(R.id.csrIndex);
        TextView csrCountry = convertView.findViewById(R.id.csrCountry);
        TextView csrState = convertView.findViewById(R.id.csrState);
        TextView csrName = convertView.findViewById(R.id.csrName);
        TextView csrLat = convertView.findViewById(R.id.csrLat);
        TextView csrLon = convertView.findViewById(R.id.csrLon);

        // Put the data in the custom layout
        if (city != null) {
            csrIndex.setText(String.valueOf(position + 1));
            csrCountry.setText(city.country);
            csrName.setText(city.name);
            csrState.setText(city.state);
            csrLat.setText(String.format("lat: " + city.lat));
            csrLon.setText(String.format("lon: " + city.lon));
        }

        return convertView;
    }
}
