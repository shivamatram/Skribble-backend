# Firebase Setup Instructions for Food Van App

## Current Issue
Your app is using a demo/placeholder `google-services.json` file with invalid API keys, causing authentication failures.

## Fix Steps

### 1. Create Firebase Project
1. Go to https://console.firebase.google.com/
2. Click "Create a project" or select existing project
3. Enter project name: "Food Van App" (or your preferred name)
4. Enable Google Analytics (optional)
5. Click "Create project"

### 2. Add Android App
1. In Firebase Console, click "Add app" → Android icon
2. Enter package name: `com.example.foodvan`
3. Enter app nickname: "Food Van Android"
4. Leave SHA-1 empty for now (can add later for Google Sign-In)
5. Click "Register app"

### 3. Download Configuration
1. Download the `google-services.json` file
2. Replace the current file at: `app/google-services.json`
3. The new file will have real API keys and project IDs

### 4. Enable Authentication
1. In Firebase Console, go to "Authentication" → "Sign-in method"
2. Enable "Email/Password" provider
3. Enable "Google" provider (optional, for social login)
4. Save changes

### 5. Enable Realtime Database
1. Go to "Realtime Database" → "Create database"
2. Choose location (closest to your users)
3. Start in "Test mode" for development
4. Click "Enable"

### 6. Test the App
1. Clean and rebuild the project
2. Test user registration
3. Check Firebase Console for new users

## Security Rules (Optional)
For production, update Realtime Database rules:
```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

## Troubleshooting
- Ensure package name matches exactly: `com.example.foodvan`
- Clean and rebuild after replacing google-services.json
- Check Firebase Console for error logs
- Verify internet connection during testing
