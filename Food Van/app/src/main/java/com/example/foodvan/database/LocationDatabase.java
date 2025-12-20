package com.example.foodvan.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.foodvan.models.DefaultLocation;
import com.example.foodvan.models.GpsSettings;
import com.example.foodvan.models.LocationHistory;

/**
 * Room Database for offline location data caching
 */
@Database(
    entities = {DefaultLocation.class, LocationHistory.class, GpsSettings.class},
    version = 1,
    exportSchema = false
)
public abstract class LocationDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "location_database";
    private static volatile LocationDatabase INSTANCE;

    public abstract LocationDao locationDao();

    public static LocationDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LocationDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            LocationDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
