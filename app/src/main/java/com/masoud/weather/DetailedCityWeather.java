package com.masoud.weather;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class DetailedCityWeather extends AppCompatActivity {

    // For listview
    ListView dcwList;
    DetailedWeatherAdapter detailedWeatherAdapter;
    ArrayList<DetailedWeatherModel> detailedWeatherModelArrayList = new ArrayList<>();

    // The weather that we clicked on
    WeatherModel clickedWeather;

    Button dcwRetry;
    ProgressBar dcwProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detailed_city_weather);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get data that we passed to this activity from main activity
        Intent intent = getIntent();
        if (intent != null && intent.getSerializableExtra("weather_model") != null) {
            clickedWeather = (WeatherModel) intent.getSerializableExtra("weather_model");
        }

        // Get view references
        TextView dcwCountry = findViewById(R.id.dcwCountry);
        TextView dcwCity = findViewById(R.id.dcwCity);
        TextView dcwState = findViewById(R.id.dcwState);
        dcwRetry = findViewById(R.id.dcwRetry);
        dcwProgressBar = findViewById(R.id.dcwProgressBar);
        dcwList = findViewById(R.id.dcwList);


        dcwCountry.setText(clickedWeather.getCountry());
        dcwCity.setText(clickedWeather.getName());
        dcwState.setText(clickedWeather.getState());

        // Setup adapter for listview
        detailedWeatherAdapter = new DetailedWeatherAdapter(this, detailedWeatherModelArrayList);
        dcwList.setAdapter(detailedWeatherAdapter);
        // Call api to get data
        getWeather();

        // Retry button that shows on api fails
        dcwRetry.setOnClickListener(v -> {
            dcwRetry.setVisibility(View.GONE);
            dcwProgressBar.setVisibility(View.VISIBLE);
            getWeather();
        });
    }
    // Function to get weather data for every 6 hours in the next 5 days
    private void getWeather() {
        String url = String.format(Locale.ENGLISH, "https://api.openweathermap.org/data/2.5/forecast?lat=%f&lon=%f&units=metric&appid=%s", clickedWeather.getLat(), clickedWeather.getLon(), BuildConfig.API_KEY);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    dcwList.setVisibility(View.VISIBLE);
                    dcwRetry.setVisibility(View.GONE);
                    dcwProgressBar.setVisibility(View.GONE);
                    detailedWeatherModelArrayList.clear();

                    try {
                        JSONArray jsonArrayRequest = response.getJSONArray("list");
                        for (int i = 0; i < jsonArrayRequest.length(); i++) {
                            if (i % 2 == 0) {
                                JSONObject currentDay = jsonArrayRequest.getJSONObject(i);

                                long date = currentDay.getLong("dt");

                                JSONObject main = currentDay.getJSONObject("main");
                                int temp = (int) main.getDouble("temp");

                                JSONObject weatherObj = currentDay.getJSONArray("weather").getJSONObject(0);
                                String weather = weatherObj.getString("main");
                                String description = weatherObj.getString("description");
                                String emoji = WeatherUtils.getWeatherEmoji(weatherObj.getString("icon"));

                                detailedWeatherModelArrayList.add(new DetailedWeatherModel(date, weather, description, emoji, temp));
                            }
                        }
                        detailedWeatherAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        dcwList.setVisibility(View.INVISIBLE);
                        dcwRetry.setVisibility(View.VISIBLE);
                        dcwProgressBar.setVisibility(View.GONE);
                    }


                }, error -> {
                    dcwList.setVisibility(View.INVISIBLE);
                    dcwRetry.setVisibility(View.VISIBLE);
                    dcwProgressBar.setVisibility(View.GONE);
        });
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}