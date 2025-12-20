package com.masoud.weather;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Database db;

    NetworkReceiver networkReceiver;
    public volatile boolean isRefreshing = false;
    // Volley request queue
    private RequestQueue requestQueue;

    // Variables for Search
    private final ArrayList<CitySearch> citySearchArrayList = new ArrayList<>();
    ListView searchResults;
    CitySearchAdapter citySearchAdapter;
    TextInputEditText inputSearch;
    ProgressBar pbSearch;

    // Home page recycler view
    WeatherAdapter weatherAdapter;
    ArrayList<WeatherModel> weatherModelArrayList = new ArrayList<>();

    // For accessing location
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = Database.getInstance(this);
        loadSavedWeather();
        // Custom handle of back button
        OnBackPressedCallback backPressCallBack = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(searchResults.getVisibility() == View.VISIBLE) {
                    searchResults.setVisibility(View.GONE);
                }else {
                    if (inputSearch.hasFocus()) {
                        hideKeyboard(inputSearch);
                        inputSearch.clearFocus();
                    }else {
                        if (isEnabled()) {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backPressCallBack);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Get references to the views
        TextView txtNoNetwork = findViewById(R.id.txtNoNetwork);
        pbSearch = findViewById(R.id.pbSearch);
        ImageButton btnSearch = findViewById(R.id.btnSearch);
        Button btnGetLocation = findViewById(R.id.btnGetLocation);
        Button btnRefresh = findViewById(R.id.btnRefresh);
        RecyclerView recyclerView = findViewById(R.id.rcCities);
        inputSearch = findViewById(R.id.inputSearch);
        searchResults = findViewById(R.id.searchResults);

        // Initialize the adapter
        citySearchAdapter = new CitySearchAdapter(this, citySearchArrayList);
        searchResults.setAdapter(citySearchAdapter);
        weatherAdapter = new WeatherAdapter(weatherModelArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(weatherAdapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Handle City Search List item click
        searchResults.setOnItemClickListener((parent, view, position, id) -> {
            CitySearch city = citySearchArrayList.get(position);
            getCurrentWeatherData(city.lat, city.lon, city.name, city.country, city.state);
            searchResults.setVisibility(View.GONE);
        });

        // Search on button click
        btnSearch.setOnClickListener(v -> {
            getInputToSearch();
        });

        // Get users current location after button press
        btnGetLocation.setOnClickListener(v -> {
            checkPermissionAndGetLocation();
        });

        // Refresh current list of cities
        btnRefresh.setOnClickListener(v -> {
            pbSearch.setVisibility(View.VISIBLE);
            refreshWeatherData();
        });

        // Setup the option to do the search with keyboard search button
        inputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                getInputToSearch();
                return true;
            } return false;
        });

        // Show and hide search button on focus change
        inputSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) btnSearch.setVisibility(View.VISIBLE);
            else btnSearch.setVisibility(View.GONE);
        });

        weatherAdapter.SetOnItemLongClickListener((model, position) -> {
            db.weatherDAO().delete(model);
            weatherModelArrayList.remove(position);
            weatherAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Removed " + model.getName(), Toast.LENGTH_SHORT).show();
        });

        networkReceiver = new NetworkReceiver(new NetworkReceiver.NetworkStateListener() {
            @Override
            public void onNetworkAvailable() {
                txtNoNetwork.setVisibility(View.GONE);
                if (!weatherModelArrayList.isEmpty()) {
                    pbSearch.setVisibility(View.VISIBLE);
                    refreshWeatherData();
                } else {
                    Toast.makeText(MainActivity.this, "Back Online", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNetworkLost() {
                if (weatherModelArrayList.isEmpty()) {
                    txtNoNetwork.setText("No Network Connection Available");
                    txtNoNetwork.setVisibility(View.VISIBLE);
                } else {
                    txtNoNetwork.setText("No Network Connection Available\nData Might Be Outdated");
                    txtNoNetwork.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    private void loadSavedWeather() {
        weatherModelArrayList.clear();
        weatherModelArrayList.addAll(db.weatherDAO().getAllWeather());

        if (weatherAdapter != null) weatherAdapter.notifyDataSetChanged();
    }
    private void checkPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserCoarseLocation();
        }else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getUserCoarseLocation();
                }else {
                    Toast.makeText(this, "Permission Denied, Using Approximate IP Location", Toast.LENGTH_SHORT).show();
                    getIPLocation();
                }
            });
    private void getUserCoarseLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getIPLocation();
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        String url = String.format(Locale.ENGLISH, "http://api.openweathermap.org/geo/1.0/reverse?lat=%f&lon=%s&limit=1&appid=%s", location.getLatitude(), location.getLongitude(), BuildConfig.API_KEY);

                        JsonArrayRequest jsonArray = new JsonArrayRequest(Request.Method.GET, url, null,
                                response -> {
                                    try {
                                        JSONObject jsonObject = response.getJSONObject(0);
                                        String name = jsonObject.getString("name");
                                        String country = jsonObject.getString("country");
                                        String state = jsonObject.getString("state");
                                        getCurrentWeatherData(location.getLatitude(), location.getLongitude(), name, country, state);
                                    } catch (JSONException e) {
                                        getCurrentWeatherData(location.getLatitude(), location.getLongitude(), "Unknown", "##", "Unknown");
                                    }
                                }, error -> {
                                    getCurrentWeatherData(location.getLatitude(), location.getLongitude(), "Unknown", "##", "Unknown");
                        });
                        requestQueue.add(jsonArray);
                    }else {
                        Toast.makeText(this, "Gps Location Not Found, Using Approximate IP Location", Toast.LENGTH_SHORT).show();
                        getIPLocation();
                    }
                });
    }
    private void getIPLocation() {
        String url = String.format(Locale.ENGLISH, "https://api.ipgeolocation.io/v2/ipgeo?apiKey=%s", "0dbb20533f014fb9aac33f8c4077bf9d");

        JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject location = response.getJSONObject("location");
                        String city = location.getString("city");
                        String country = location.getString("country_code2");
                        String state = location.getString("state_prov");
                        double lat = location.getDouble("latitude");
                        double lon = location.getDouble("longitude");

                        getCurrentWeatherData(lat, lon, city, country, state);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Could Not Determine IP Location", Toast.LENGTH_SHORT).show();
                    }

                }, error -> {
            Toast.makeText(this, "Could Not Get Loation Due To Network Error", Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(jsonObject);
    }
    // Pass current search text to search method
    private void getInputToSearch() {
        String searchText = inputSearch.getText().toString();
        if (!searchText.isEmpty()) {
            pbSearch.setVisibility(View.VISIBLE);
            searchCity(inputSearch.getText().toString());
            inputSearch.setText("");
            inputSearch.clearFocus();
            hideKeyboard(inputSearch);
        }
    }

    // search city name and get lat and lon from api
    private void searchCity(String searchInput){
        String api_url = String.format("https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=10&appid=",searchInput) + BuildConfig.API_KEY ;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                api_url,
                null,
                response -> {
                    pbSearch.setVisibility(View.GONE);
                    if (response.length() > 0) {
                        citySearchArrayList.clear();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String name = jsonObject.getString("name");
                                String country = jsonObject.getString("country");
                                String state = jsonObject.getString("state");
                                double lat = jsonObject.getDouble("lat");
                                double lon = jsonObject.getDouble("lon");

                                citySearchArrayList.add(new CitySearch(name, country, state, lat, lon));
                            }
                        }catch (Exception e) {
                            Toast.makeText(this, "Error Parsing Data", Toast.LENGTH_SHORT).show();
                        }
                        citySearchAdapter.notifyDataSetChanged();
                        searchResults.setVisibility(View.VISIBLE);

                    }else {
                        Toast.makeText(MainActivity.this, "City Not Found!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Could Not Retrieve The Data", Toast.LENGTH_SHORT).show();
                    pbSearch.setVisibility(View.GONE);
                }
        );
        requestQueue.add(jsonArrayRequest);
    }

    // Function to hide keyboard after done typing
    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void getCurrentWeatherData(double lat, double lon, String cityName, String country, String state) {
        String url = String.format(Locale.ENGLISH,"https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&appid=%s", lat, lon, BuildConfig.API_KEY);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
                        String weatherMain = weather.getString("main");
                        String weatherDesc = weather.getString("description");
                        String weatherEmoji = getWeatherEmoji(weather.getString("icon"));

                        JSONObject temps = response.getJSONObject("main");
                        int temp = (int) temps.getDouble("temp");
                        int feelsLike = (int) temps.getDouble("feels_like");
                        int minTemp = (int) temps.getDouble("temp_min");
                        int maxTemp = (int) temps.getDouble("temp_max");

                        WeatherModel newWeather = new WeatherModel(cityName, country, state, weatherMain, weatherDesc, weatherEmoji, lat, lon, temp, minTemp, maxTemp, feelsLike);
                        WeatherModel existing = db.weatherDAO().getCity(cityName, country, state);

                        if (existing != null) {
                            newWeather.setId(existing.getId());
                            db.weatherDAO().update(newWeather);
                        } else {
                            db.weatherDAO().insert(newWeather);
                        }
                        loadSavedWeather();

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing weather", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void refreshWeatherData() {
        if (isRefreshing) return;
        if (weatherModelArrayList.isEmpty()) {
            pbSearch.setVisibility(View.GONE);
            Toast.makeText(this, "No Cities To Refresh", Toast.LENGTH_SHORT).show();
            return;
        }
        isRefreshing = true;
        ArrayList<WeatherModel> tempWeatherData = new ArrayList<>();
        final int[] requestCount = {weatherModelArrayList.size()};
        for (WeatherModel currentModel : weatherModelArrayList) {
            String url = String.format(Locale.ENGLISH,"https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&appid=%s", currentModel.getLat(), currentModel.getLon(), BuildConfig.API_KEY);
        JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
                        String weatherMain = weather.getString("main");
                        String weatherDesc = weather.getString("description");
                        String weatherEmoji = getWeatherEmoji(weather.getString("icon"));

                        JSONObject temps = response.getJSONObject("main");
                        int temp = (int) temps.getDouble("temp");
                        int feelsLike = (int) temps.getDouble("feels_like");
                        int minTemp = (int) temps.getDouble("temp_min");
                        int maxTemp = (int) temps.getDouble("temp_max");
                        WeatherModel updatedModel = new WeatherModel(
                                currentModel.getName(),
                                currentModel.getCountry(),
                                currentModel.getState(),
                                weatherMain,
                                weatherDesc,
                                weatherEmoji,
                                currentModel.getLat(),
                                currentModel.getLon(),
                                temp,
                                minTemp,
                                maxTemp,
                                feelsLike
                        );

                        updatedModel.setId(currentModel.getId());
                        tempWeatherData.add(updatedModel);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error Parsing Weather", Toast.LENGTH_SHORT).show();
                    } finally {
                        requestCount[0]--;
                        if (requestCount[0] == 0) {
                            finishedRefreshing(tempWeatherData);
                        }
                    }

                }, error -> {
                        requestCount[0]--;
                        tempWeatherData.add(currentModel);
                        if (requestCount[0] == 0) {
                            finishedRefreshing(tempWeatherData);
                        }
                        Toast.makeText(this, "Failed To Refresh " + currentModel.getName(), Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(jsonObject);
        }
    }

    private void finishedRefreshing(ArrayList<WeatherModel> tempWeatherData) {
        db.weatherDAO().updateAll(tempWeatherData);
        updateMainList(tempWeatherData);
        pbSearch.setVisibility(View.GONE);
        isRefreshing = false;
    }

    private void updateMainList(ArrayList<WeatherModel> weatherData) {
        weatherModelArrayList.clear();
        weatherModelArrayList.addAll(weatherData);
        weatherAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Refresh Finished", Toast.LENGTH_SHORT).show();
    }

    // Helper method to convert OpenWeatherMap icon codes to Emojis
    public static String getWeatherEmoji(String iconCode) {
        switch (iconCode) {
            // Clear Sky
            case "01d": return "☀️"; // Day
            case "01n": return "🌙"; // Night

            // Few Clouds
            case "02d": return "⛅"; // Day
            case "02n": return "☁️"; // Night

            // Scattered Clouds (Same emoji for day/night)
            case "03d":
            case "03n": return "☁️";

            // Broken Clouds (Same emoji for day/night)
            case "04d":
            case "04n": return "☁️";

            // Shower Rain (Same emoji for day/night)
            case "09d":
            case "09n": return "🌧️";

            // Rain
            case "10d": return "🌦️"; // Day sun/rain
            case "10n": return "🌧️"; // Night rain

            // Thunderstorm (Same emoji for day/night)
            case "11d":
            case "11n": return "⛈️";

            // Snow (Same emoji for day/night)
            case "13d":
            case "13n": return "❄️";

            // Mist/Fog (Same emoji for day/night)
            case "50d":
            case "50n": return "🌫️";

            default: return "❓"; // Unknown weather
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (networkReceiver != null) unregisterReceiver(networkReceiver);
    }
}