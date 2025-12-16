package com.masoud.weather;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public class FavoritesDAO {
    @Query("SELECT * FROM favorites")
    List<Favorites> getFavorites() { return null; }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(Favorites favorites) { }

    @Delete
    void deleteFavorite(Favorites favorites) { }


}
