package com.example.foodvan.utils;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Room TypeConverters for complex data types
 * Handles conversion of List<String> to/from JSON for database storage
 */
public class Converters {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromStringList(List<String> value) {
        if (value == null) {
            return null;
        }
        return gson.toJson(value);
    }

    @TypeConverter
    public static List<String> fromString(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }
}
