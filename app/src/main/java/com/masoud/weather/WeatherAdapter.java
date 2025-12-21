package com.masoud.weather;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {
    private final ArrayList<WeatherModel> weatherModelArrayList;

    // Listener for long clicks to delete items
    private OnItemLongclickListener longClickListener;

    public WeatherAdapter(ArrayList<WeatherModel> weatherData) {
        this.weatherModelArrayList = weatherData;
    }
    // Inflate the custom view for each item
    @NonNull
    @Override
    public WeatherAdapter.WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_cardview, parent, false);
        return new WeatherViewHolder(view);
    }
    // Setup our view with our data
    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.WeatherViewHolder holder, int position) {
        WeatherModel weatherData = weatherModelArrayList.get(position);

        holder.txtTemp.setText(weatherData.getTemp() + "℃");
        holder.txtEmoji.setText(weatherData.getWeather_emoji());
        holder.txtCountry.setText(weatherData.getCountry());
        holder.txtCity.setText(weatherData.getName());
        holder.txtState.setText(weatherData.getState());
        holder.txtWeather.setText(weatherData.getWeather());
        holder.txtWeatherDesc.setText(weatherData.getWeather_description());
        holder.txtTempMin.setText(weatherData.getMin_temp() + "℃");
        holder.txtTempMax.setText(weatherData.getMax_temp() + "℃");
        holder.txtTempFeels.setText(weatherData.getFeels_temp() + "℃");

        // Setup on click listener to go to the detailed activity
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, DetailedCityWeather.class);
            intent.putExtra("weather_model", weatherData);
            context.startActivity(intent);
        });
        // Use our custom listener for long clicks on each item
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(weatherData, position);
                return true;
            }
            return false;
        });

    }
    // Return list size
    @Override
    public int getItemCount() {
        return (weatherModelArrayList != null) ? weatherModelArrayList.size() : 0;
    }
    // Get references to every component
    public static class WeatherViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTemp;
        public TextView txtEmoji;
        public TextView txtCountry;
        public TextView txtCity;
        public TextView txtState;
        public TextView txtWeather;
        public TextView txtWeatherDesc;
        public TextView txtTempMin;
        public TextView txtTempMax;
        public TextView txtTempFeels;

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTemp = itemView.findViewById(R.id.txtTemp);
            txtEmoji = itemView.findViewById(R.id.txtEmoji);
            txtCountry = itemView.findViewById(R.id.txtCountry);
            txtCity = itemView.findViewById(R.id.txtCity);
            txtState = itemView.findViewById(R.id.txtState);
            txtWeather = itemView.findViewById(R.id.txtWeather);
            txtWeatherDesc = itemView.findViewById(R.id.txtWeatherDesc);
            txtTempMin = itemView.findViewById(R.id.txtTempMin);
            txtTempMax = itemView.findViewById(R.id.txtTempMax);
            txtTempFeels = itemView.findViewById(R.id.txtTempFeels);
        }
    }

    // Listener for long clicks to delete items
    public interface OnItemLongclickListener {
        void onItemLongClick(WeatherModel model, int position);
    }
    public void SetOnItemLongClickListener(OnItemLongclickListener listener){
        this.longClickListener = listener;
    }



}
