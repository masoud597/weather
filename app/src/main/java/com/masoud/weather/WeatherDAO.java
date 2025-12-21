package com.masoud.weather;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface WeatherDAO {

    @Insert
    void insert(WeatherModel model);

    @Delete
    void delete(WeatherModel model);

    @Query("SELECT * FROM weather")
    List<WeatherModel> getAllWeather();

    // Used to check if a city already exists before adding it in the list
    @Query("SELECT * FROM weather where name = :name AND country = :country AND state = :state LIMIT 1")
    WeatherModel getCity(String name, String country, String state);

    @Update
    void update(WeatherModel model);

    @Update
    void updateAll(List<WeatherModel> models);

}
