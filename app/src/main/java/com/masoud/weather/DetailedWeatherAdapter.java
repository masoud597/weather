package com.masoud.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DetailedWeatherAdapter extends ArrayAdapter<DetailedWeatherModel> {
    public DetailedWeatherAdapter(Context context, ArrayList<DetailedWeatherModel> dwModel) {
        super(context, 0, dwModel);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        DetailedWeatherModel dwModel = getItem(position);

        if (convertView == null) convertView = LayoutInflater.from(getContext()).inflate(R.layout.dcw_list_row, parent, false);

        TextView dcwDate = convertView.findViewById(R.id.dcwDate);
        TextView dcwWeather = convertView.findViewById(R.id.dcwWeather);
        TextView dcwWeatherDesc = convertView.findViewById(R.id.dcwWeatherDesc);
        TextView dcwEmoji = convertView.findViewById(R.id.dcwEmoji);
        TextView dcwMin = convertView.findViewById(R.id.dcwTemp);


        if (dwModel != null) {
            Date dateObj = new Date(dwModel.getDate() * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE HH:mm, dd MMM", Locale.ENGLISH);
            dcwDate.setText(sdf.format(dateObj));
            dcwWeather.setText(dwModel.getWeatherMain());
            dcwWeatherDesc.setText(dwModel.getWeatherDesc());
            dcwEmoji.setText(dwModel.getWeatherEmoji());
            dcwMin.setText(String.valueOf(dwModel.getTemp()) + "℃");
        }

        return convertView;

    }
}
