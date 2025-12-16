package com.masoud.weather;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RequestQueue requestQueue;
    private ArrayList<CitySearch> citySearchArrayList = new ArrayList<>();
    ListView searchResults;

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

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Get references to the views
        ImageButton BtnSearch = findViewById(R.id.btnSearch);
        TextInputEditText inputSearch = findViewById(R.id.inputSearch);
        searchResults = findViewById(R.id.searchResults);

        // Custom handle of back button
        OnBackPressedCallback backPressCallBack = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(searchResults.getVisibility() == View.VISIBLE) {
                    searchResults.setVisibility(View.GONE);
                }else {
                    if (inputSearch.hasFocus()) {
                        inputSearch.clearFocus();
                        hideKeyboard();
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

        // Search on button click
        BtnSearch.setOnClickListener(v -> {
            getInputToSearch(inputSearch);
        });

        // Setup the option to do the search with keyboard search button
        inputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                getInputToSearch(inputSearch);
                return true;
            } return false;
        });

        // Show and hide search button on focus change
        inputSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) BtnSearch.setVisibility(View.VISIBLE);
            else BtnSearch.setVisibility(View.GONE);
        });
    }

    // Pass current search text to search method
    private void getInputToSearch(TextInputEditText inputSearch) {
        String searchText = inputSearch.getText().toString();
        if (!searchText.isEmpty()) {
            searchCity(inputSearch.getText().toString());
            inputSearch.setText("");
            hideKeyboard();
        }
    }

    // search city name and get lat and lon from api
    private void searchCity(String searchInput){
        String api_url = String.format("http://api.openweathermap.org/geo/1.0/direct?q=%s&limit=10&appid=",searchInput) + BuildConfig.API_KEY ;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                api_url,
                null,
                response -> {
                    if (response.length() > 0) {
                        citySearchArrayList.clear();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String name = jsonObject.getString("name");
                                String country = jsonObject.getString("country");
                                double lat = jsonObject.getDouble("lat");
                                double lon = jsonObject.getDouble("lon");

                                citySearchArrayList.add(new CitySearch(name, country, lat, lon));
                            }
                        }catch (Exception e) {
                            Toast.makeText(this, "Error Parsing Data", Toast.LENGTH_SHORT).show();
                        }
                        // TODO update the adapter
                        searchResults.setVisibility(View.VISIBLE);

                    }else {
                        Toast.makeText(MainActivity.this, "City Not Found!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Could Not Retrieve The Data", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(jsonArrayRequest);
    }

    // Function to hide keyboard after done typing
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}