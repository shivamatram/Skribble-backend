package com.example.foodvan.utils;

import android.content.Context;
import android.util.Log;

import com.example.foodvan.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;

public class MapStyleUtils {
    
    private static final String TAG = "MapStyleUtils";

    /**
     * Sets the map style based on the theme preference
     * @param googleMap The GoogleMap instance
     * @param context The application context
     * @param isDarkMode Whether to apply dark mode style
     */
    public static void setMapStyle(GoogleMap googleMap, Context context, boolean isDarkMode) {
        try {
            if (isDarkMode) {
                // Apply dark theme
                boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
                );
                if (!success) {
                    Log.e(TAG, "Style parsing failed for dark theme");
                    // Fallback to default dark style
                    applyDefaultDarkStyle(googleMap);
                }
            } else {
                // Apply light theme
                boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_light)
                );
                if (!success) {
                    Log.e(TAG, "Style parsing failed for light theme");
                    // Fallback to default light style
                    applyDefaultLightStyle(googleMap);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying map style: " + e.getMessage());
            // Apply fallback style
            if (isDarkMode) {
                applyDefaultDarkStyle(googleMap);
            } else {
                applyDefaultLightStyle(googleMap);
            }
        }
    }

    /**
     * Applies a default dark style using inline JSON
     */
    private static void applyDefaultDarkStyle(GoogleMap googleMap) {
        try {
            String darkStyleJson = "[\n" +
                "  {\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#212121\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"elementType\": \"labels.icon\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"visibility\": \"off\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#757575\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"elementType\": \"labels.text.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#212121\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"administrative\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#757575\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road\",\n" +
                "    \"elementType\": \"geometry.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#2c2c2c\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#8a8a8a\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"water\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#000000\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";

            googleMap.setMapStyle(new MapStyleOptions(darkStyleJson));
        } catch (Exception e) {
            Log.e(TAG, "Error applying default dark style: " + e.getMessage());
        }
    }

    /**
     * Applies a default light style (standard Google Maps style)
     */
    private static void applyDefaultLightStyle(GoogleMap googleMap) {
        try {
            // Reset to default Google Maps style
            googleMap.setMapStyle(null);
        } catch (Exception e) {
            Log.e(TAG, "Error applying default light style: " + e.getMessage());
        }
    }

    /**
     * Gets the appropriate map style for food delivery apps
     */
    public static void setFoodDeliveryStyle(GoogleMap googleMap, Context context, boolean isDarkMode) {
        try {
            String styleJson;
            
            if (isDarkMode) {
                styleJson = getFoodDeliveryDarkStyle();
            } else {
                styleJson = getFoodDeliveryLightStyle();
            }
            
            googleMap.setMapStyle(new MapStyleOptions(styleJson));
        } catch (Exception e) {
            Log.e(TAG, "Error applying food delivery style: " + e.getMessage());
            setMapStyle(googleMap, context, isDarkMode);
        }
    }

    private static String getFoodDeliveryLightStyle() {
        return "[\n" +
            "  {\n" +
            "    \"featureType\": \"poi\",\n" +
            "    \"elementType\": \"labels.text\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"visibility\": \"off\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"poi.business\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"visibility\": \"off\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road\",\n" +
            "    \"elementType\": \"labels.icon\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"visibility\": \"off\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"transit\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"visibility\": \"off\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "]";
    }

    private static String getFoodDeliveryDarkStyle() {
        return "[\n" +
            "  {\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#1d2c4d\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#8ec3b9\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"elementType\": \"labels.text.stroke\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#1a3646\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road\",\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#38414e\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road\",\n" +
            "    \"elementType\": \"geometry.stroke\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#212a37\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#9ca5b3\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"water\",\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#0e1626\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"poi\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"visibility\": \"off\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "]";
    }
}
