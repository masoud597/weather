package com.masoud.weather;

public class DetailedWeatherModel {
    private long date;
    private String weatherMain;
    private String weatherDesc;
    private String weatherEmoji;
    private int temp;


    public DetailedWeatherModel(long date, String weatherMain, String weatherDesc, String weatherEmoji, int temp) {
        this.date = date;
        this.weatherMain = weatherMain;
        this.weatherDesc = weatherDesc;
        this.weatherEmoji = weatherEmoji;
        this.temp = temp;

    }

    public long getDate() {
        return date;
    }

    public String getWeatherMain() {
        return weatherMain;
    }

    public String getWeatherDesc() {
        return weatherDesc;
    }

    public String getWeatherEmoji() {
        return weatherEmoji;
    }

    public int getTemp() {
        return temp;
    }

}
