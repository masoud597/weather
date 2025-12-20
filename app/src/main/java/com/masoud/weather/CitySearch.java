package com.masoud.weather;

public class CitySearch {
    String name;
    String country;
    String state;
    double lat;
    double lon;

    public CitySearch(String name, String country,String state, double lat, double lon) {
        this.name = name;
        this.country = country;
        this.state = state;
        this.lat = lat;
        this.lon = lon;
    }
}
