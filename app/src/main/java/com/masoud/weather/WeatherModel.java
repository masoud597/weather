package com.masoud.weather;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "weather")
public class WeatherModel implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String country;
    private String state;
    private String weather;
    private String weather_description;
    private String weather_emoji;
    private double lat;
    private double lon;
    private int temp;
    private int min_temp;
    private int max_temp;
    private int feels_temp;

    public WeatherModel(String name, String country, String state, String weather, String weather_description, String weather_emoji, double lat, double lon, int temp, int min_temp, int max_temp, int feels_temp) {
        this.name = name;
        this.country = country;
        this.state = state;
        this.weather = weather;
        this.weather_description = weather_description;
        this.weather_emoji = weather_emoji;
        this.lat = lat;
        this.lon = lon;
        this.temp = temp;
        this.min_temp = min_temp;
        this.max_temp = max_temp;
        this.feels_temp = feels_temp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public String getWeather() {
        return weather;
    }

    public String getWeather_description() {
        return weather_description;
    }

    public String getWeather_emoji() {
        return weather_emoji;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getTemp() {
        return temp;
    }

    public int getMin_temp() {
        return min_temp;
    }

    public int getMax_temp() {
        return max_temp;
    }

    public int getFeels_temp() {
        return feels_temp;
    }
}
