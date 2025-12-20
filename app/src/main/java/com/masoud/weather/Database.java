package com.masoud.weather;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {WeatherModel.class}, version = 1)
public abstract class Database extends RoomDatabase {
    public abstract WeatherDAO weatherDAO();

    private static Database instance;

    public static synchronized Database getInstance(Context context){
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), Database.class, "weather_db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
