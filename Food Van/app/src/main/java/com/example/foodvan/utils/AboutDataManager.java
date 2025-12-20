package com.example.foodvan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.example.foodvan.models.AboutData;
import com.example.foodvan.models.ChangelogEntry;
import com.example.foodvan.models.TeamMember;
import com.example.foodvan.models.ContactInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * AboutDataManager - Handles backend integration, caching, and data management
 * for the About Food Van screen with Firebase and local storage
 */
public class AboutDataManager {
    private static final String TAG = "AboutDataManager";
    private static final String PREFS_NAME = "about_data_cache";
    private static final String KEY_ABOUT_DATA = "cached_about_data";
    private static final String KEY_LAST_UPDATE = "last_update_time";
    private static final String KEY_CHANGELOG = "cached_changelog";
    private static final String KEY_TEAM_MEMBERS = "cached_team_members";
    private static final String KEY_CONTACT_INFO = "cached_contact_info";
    
    // Cache validity time (24 hours)
    private static final long CACHE_VALIDITY_TIME = 24 * 60 * 60 * 1000;
    
    private Context context;
    private SharedPreferences preferences;
    private Gson gson;
    private DatabaseReference databaseRef;
    
    // Listeners for data updates
    public interface OnDataLoadedListener {
        void onAboutDataLoaded(AboutData aboutData);
        void onChangelogLoaded(List<ChangelogEntry> changelog);
        void onTeamMembersLoaded(List<TeamMember> teamMembers);
        void onContactInfoLoaded(ContactInfo contactInfo);
        void onError(String error);
    }
    
    public AboutDataManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.databaseRef = FirebaseDatabase.getInstance().getReference("about_food_van");
    }
    
    /**
     * Load About data with caching strategy
     */
    public void loadAboutData(OnDataLoadedListener listener) {
        // Check if cached data is valid
        if (isCacheValid()) {
            loadFromCache(listener);
        } else {
            loadFromFirebase(listener);
        }
    }
    
    /**
     * Force refresh from Firebase (bypass cache)
     */
    public void forceRefresh(OnDataLoadedListener listener) {
        loadFromFirebase(listener);
    }
    
    /**
     * Check if cached data is still valid
     */
    private boolean isCacheValid() {
        long lastUpdate = preferences.getLong(KEY_LAST_UPDATE, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastUpdate) < CACHE_VALIDITY_TIME;
    }
    
    /**
     * Load data from local cache
     */
    private void loadFromCache(OnDataLoadedListener listener) {
        try {
            // Load about data
            String aboutDataJson = preferences.getString(KEY_ABOUT_DATA, null);
            if (aboutDataJson != null) {
                AboutData aboutData = gson.fromJson(aboutDataJson, AboutData.class);
                listener.onAboutDataLoaded(aboutData);
            }
            
            // Load changelog
            String changelogJson = preferences.getString(KEY_CHANGELOG, null);
            if (changelogJson != null) {
                ChangelogEntry[] changelogArray = gson.fromJson(changelogJson, ChangelogEntry[].class);
                List<ChangelogEntry> changelog = new ArrayList<>();
                for (ChangelogEntry entry : changelogArray) {
                    changelog.add(entry);
                }
                listener.onChangelogLoaded(changelog);
            }
            
            // Load team members
            String teamMembersJson = preferences.getString(KEY_TEAM_MEMBERS, null);
            if (teamMembersJson != null) {
                TeamMember[] teamMembersArray = gson.fromJson(teamMembersJson, TeamMember[].class);
                List<TeamMember> teamMembers = new ArrayList<>();
                for (TeamMember member : teamMembersArray) {
                    teamMembers.add(member);
                }
                listener.onTeamMembersLoaded(teamMembers);
            }
            
            // Load contact info
            String contactInfoJson = preferences.getString(KEY_CONTACT_INFO, null);
            if (contactInfoJson != null) {
                ContactInfo contactInfo = gson.fromJson(contactInfoJson, ContactInfo.class);
                listener.onContactInfoLoaded(contactInfo);
            }
            
            Log.d(TAG, "Data loaded from cache successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading from cache", e);
            // If cache fails, try loading from Firebase
            loadFromFirebase(listener);
        }
    }
    
    /**
     * Load data from Firebase and update cache
     */
    private void loadFromFirebase(OnDataLoadedListener listener) {
        Log.d(TAG, "Loading data from Firebase...");
        
        // Load about data
        databaseRef.child("app_info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        AboutData aboutData = dataSnapshot.getValue(AboutData.class);
                        if (aboutData != null) {
                            // Cache the data
                            cacheAboutData(aboutData);
                            listener.onAboutDataLoaded(aboutData);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing about data", e);
                        listener.onError("Failed to load about data");
                    }
                } else {
                    // Provide default data if not available
                    AboutData defaultData = getDefaultAboutData();
                    listener.onAboutDataLoaded(defaultData);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Firebase error loading about data", databaseError.toException());
                // Try to load from cache as fallback
                loadFromCache(listener);
            }
        });
        
        // Load changelog
        databaseRef.child("changelog").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<ChangelogEntry> changelog = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                        ChangelogEntry entry = entrySnapshot.getValue(ChangelogEntry.class);
                        if (entry != null) {
                            changelog.add(entry);
                        }
                    }
                } else {
                    // Provide default changelog if not available
                    changelog = getDefaultChangelog();
                }
                cacheChangelog(changelog);
                listener.onChangelogLoaded(changelog);
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Firebase error loading changelog", databaseError.toException());
                listener.onError("Failed to load changelog");
            }
        });
        
        // Load team members
        databaseRef.child("team").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<TeamMember> teamMembers = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                        TeamMember member = memberSnapshot.getValue(TeamMember.class);
                        if (member != null) {
                            teamMembers.add(member);
                        }
                    }
                } else {
                    // Provide default team members if not available
                    teamMembers = getDefaultTeamMembers();
                }
                cacheTeamMembers(teamMembers);
                listener.onTeamMembersLoaded(teamMembers);
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Firebase error loading team members", databaseError.toException());
                listener.onError("Failed to load team members");
            }
        });
        
        // Load contact info
        databaseRef.child("contact").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ContactInfo contactInfo;
                if (dataSnapshot.exists()) {
                    contactInfo = dataSnapshot.getValue(ContactInfo.class);
                } else {
                    // Provide default contact info if not available
                    contactInfo = getDefaultContactInfo();
                }
                cacheContactInfo(contactInfo);
                listener.onContactInfoLoaded(contactInfo);
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Firebase error loading contact info", databaseError.toException());
                listener.onError("Failed to load contact info");
            }
        });
    }
    
    /**
     * Cache methods for different data types
     */
    private void cacheAboutData(AboutData aboutData) {
        preferences.edit()
            .putString(KEY_ABOUT_DATA, gson.toJson(aboutData))
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .apply();
    }
    
    private void cacheChangelog(List<ChangelogEntry> changelog) {
        preferences.edit()
            .putString(KEY_CHANGELOG, gson.toJson(changelog))
            .apply();
    }
    
    private void cacheTeamMembers(List<TeamMember> teamMembers) {
        preferences.edit()
            .putString(KEY_TEAM_MEMBERS, gson.toJson(teamMembers))
            .apply();
    }
    
    private void cacheContactInfo(ContactInfo contactInfo) {
        preferences.edit()
            .putString(KEY_CONTACT_INFO, gson.toJson(contactInfo))
            .apply();
    }
    
    /**
     * Default data methods for offline/fallback scenarios
     */
    private AboutData getDefaultAboutData() {
        // Create default changelog, team members, and contact info
        List<ChangelogEntry> defaultChangelog = getDefaultChangelog();
        List<TeamMember> defaultTeamMembers = getDefaultTeamMembers();
        ContactInfo defaultContactInfo = getDefaultContactInfo();
        
        return new AboutData(
            "Delicious food delivery at your fingertips!",  // tagline
            "Order from your favorite local restaurants and food vendors with ease.",  // shortDescription
            "Welcome to Food Van - your ultimate food delivery companion! We connect you with amazing local restaurants and food vendors, bringing delicious meals right to your doorstep. From street food to gourmet dining, discover flavors from around your city and satisfy your cravings with just a few taps.",  // mission
            "2.1.0",  // currentVersion
            "203",  // buildNumber
            "December 2024",  // lastUpdated
            defaultChangelog,  // changelog
            defaultTeamMembers,  // teamMembers
            defaultContactInfo  // contactInfo
        );
    }
    
    private List<ChangelogEntry> getDefaultChangelog() {
        List<ChangelogEntry> changelog = new ArrayList<>();
        
        List<String> highlights1 = new ArrayList<>();
        highlights1.add("Comprehensive About screen");
        highlights1.add("Team credits and social links");
        highlights1.add("Caching for offline access");
        changelog.add(new ChangelogEntry("2.1.0", "2024-12-19", "About Screen Update", highlights1));
        
        List<String> highlights2 = new ArrayList<>();
        highlights2.add("Material 3 design updates");
        highlights2.add("Improved navigation");
        changelog.add(new ChangelogEntry("2.0.5", "2024-12-15", "UI Improvements", highlights2));
        
        List<String> highlights3 = new ArrayList<>();
        highlights3.add("Enhanced ordering system");
        highlights3.add("Vendor dashboard");
        highlights3.add("Better performance");
        changelog.add(new ChangelogEntry("2.0.0", "2024-12-10", "Major Update", highlights3));
        
        return changelog;
    }
    
    private List<TeamMember> getDefaultTeamMembers() {
        List<TeamMember> teamMembers = new ArrayList<>();
        teamMembers.add(new TeamMember("Development Team", "Android Development", null, null));
        teamMembers.add(new TeamMember("Design Team", "UI/UX Design", null, null));
        teamMembers.add(new TeamMember("QA Team", "Quality Assurance", null, null));
        return teamMembers;
    }
    
    private ContactInfo getDefaultContactInfo() {
        return new ContactInfo(
            "support@foodvan.com",
            "+1234567890",
            "https://foodvan.com",
            "https://instagram.com/foodvan",
            "https://twitter.com/foodvan",
            "https://facebook.com/foodvan"
        );
    }
    
    /**
     * Clear all cached data
     */
    public void clearCache() {
        preferences.edit().clear().apply();
        Log.d(TAG, "Cache cleared");
    }
    
    /**
     * Get cache status information
     */
    public boolean isCacheAvailable() {
        return preferences.contains(KEY_ABOUT_DATA);
    }
    
    public long getLastUpdateTime() {
        return preferences.getLong(KEY_LAST_UPDATE, 0);
    }
}