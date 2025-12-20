package com.example.foodvan.utils;

import android.util.Log;
import androidx.fragment.app.Fragment;

/**
 * Helper class to test fragment functionality
 */
public class FragmentTestHelper {
    
    private static final String TAG = "FragmentTestHelper";
    
    public static void logFragmentCreation(Fragment fragment, String fragmentName) {
        Log.d(TAG, "Fragment created: " + fragmentName);
        Log.d(TAG, "Fragment class: " + fragment.getClass().getSimpleName());
        Log.d(TAG, "Fragment is added: " + fragment.isAdded());
        Log.d(TAG, "Fragment is visible: " + fragment.isVisible());
    }
    
    public static void logFragmentViewCreated(Fragment fragment, String fragmentName) {
        Log.d(TAG, "Fragment view created: " + fragmentName);
        Log.d(TAG, "Fragment has view: " + (fragment.getView() != null));
        if (fragment.getView() != null) {
            Log.d(TAG, "Fragment view ID: " + fragment.getView().getId());
        }
    }
    
    public static void logButtonClick(String fragmentName, String buttonName) {
        Log.d(TAG, "Button clicked in " + fragmentName + ": " + buttonName);
    }
    
    public static void logFragmentError(String fragmentName, String error) {
        Log.e(TAG, "Error in " + fragmentName + ": " + error);
    }
    
    public static void logFragmentSuccess(String fragmentName, String action) {
        Log.i(TAG, "Success in " + fragmentName + ": " + action);
    }
}
