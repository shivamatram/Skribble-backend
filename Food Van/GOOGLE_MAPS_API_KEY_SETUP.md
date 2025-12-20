# 🗺️ Google Maps API Key Setup Guide

## 🚨 **URGENT: Your map isn't showing because you need a real Google Maps API key!**

### **📋 STEP-BY-STEP SETUP:**

#### **1. Go to Google Cloud Console**
- Visit: https://console.cloud.google.com/
- Sign in with your Google account

#### **2. Create or Select Project**
- Click "Select a project" → "New Project"
- Project name: "Food Van App" 
- Click "Create"

#### **3. Enable Maps SDK**
- Go to "APIs & Services" → "Library"
- Search for "Maps SDK for Android"
- Click on it and press "ENABLE"

#### **4. Create API Key**
- Go to "APIs & Services" → "Credentials"
- Click "Create Credentials" → "API Key"
- Copy the generated API key (starts with AIza...)

#### **5. Restrict API Key (Important for Security)**
- Click on your API key to edit it
- Under "Application restrictions":
  - Select "Android apps"
  - Click "Add an item"
  - Package name: `com.example.foodvan`
  - SHA-1 certificate fingerprint: `3571572287e959eecd3714fce02ed1f8c9e4c399`
- Under "API restrictions":
  - Select "Restrict key"
  - Choose "Maps SDK for Android"
- Click "Save"

#### **6. Update AndroidManifest.xml**
Replace the placeholder key in your AndroidManifest.xml:

```xml
<!-- Replace this line in AndroidManifest.xml -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_REAL_API_KEY_HERE" />
```

### **🔐 SECURITY NOTES:**
- Never commit real API keys to public repositories
- Always restrict your API keys to your app's package name
- Monitor API usage in Google Cloud Console
- Consider using environment variables for API keys

### **💰 BILLING INFORMATION:**
- Google Maps requires a billing account
- You get $200 free credits per month
- Most development usage stays within free tier
- Enable billing in Google Cloud Console

### **🧪 TESTING:**
After updating the API key:
1. Clean and rebuild your project
2. Uninstall and reinstall the app
3. Grant location permissions when prompted
4. The map should now load properly

### **🆘 ALTERNATIVE FOR TESTING:**
If you can't set up Google Maps API immediately, you can temporarily:
1. Use a map placeholder image
2. Test other app features first
3. Set up the API key later

### **📞 NEED HELP?**
- Google Maps Platform Documentation: https://developers.google.com/maps/documentation/android-sdk/start
- Google Cloud Support: https://cloud.google.com/support
