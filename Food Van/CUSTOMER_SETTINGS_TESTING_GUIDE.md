# Customer Settings - Quick Start & Testing Guide

## 🚀 Quick Start

### 1. Build & Run
```bash
# Sync Gradle files
File → Sync Now

# Run on device/emulator
Shift + F10  (or Run → Run 'app')
```

### 2. Access Settings
```
Customer Dashboard → Three-dot menu (⋮) → Settings
```

### 3. Test Features
- ✅ Toggle notifications ON/OFF
- ✅ Select map view preference
- ✅ Toggle GPS usage
- ✅ Select theme (Light/Dark/System)
- ✅ Choose language
- ✅ Click on Edit Profile, Phone Verification, etc.

---

## 📱 UI/UX Verification Checklist

### Layout & Spacing
- [ ] No overlapping with status bar
- [ ] Content padding consistent (16dp)
- [ ] Card corners properly rounded (12dp)
- [ ] Section spacing adequate (16-20dp)
- [ ] Item spacing consistent (8-12dp)

### Typography
- [ ] Title readable (20-22sp)
- [ ] Section headers clear (16-18sp)
- [ ] Item labels legible (14-16sp)
- [ ] Hints visible (12-14sp)

### Color & Theme
- [ ] Orange accent visible in toggles
- [ ] Icons not colored (flat design)
- [ ] Text contrast sufficient
- [ ] Dark mode looks good
- [ ] Primary color appears correctly

### Interactive Elements
- [ ] Switches toggle smoothly
- [ ] Radio buttons work properly
- [ ] Buttons respond to taps
- [ ] Tap targets ≥ 48dp
- [ ] Feedback immediate (toast)

### Navigation
- [ ] Back button returns to Dashboard
- [ ] Edit Profile opens correctly
- [ ] Phone Verification accessible
- [ ] Saved Addresses opens
- [ ] Payment Methods opens
- [ ] Help & Support opens

### Icons & Content Descriptions
- [ ] Icons visible and appropriately colored
- [ ] All icons have contentDescription
- [ ] Icons match Material 3 style
- [ ] Icons are 24dp size

---

## 🧪 Functional Testing

### Notifications Section
```
1. Master Toggle OFF
   ✓ Sub-toggles disabled (grayed out)
   ✓ Sub-toggles cannot be tapped
   
2. Master Toggle ON
   ✓ Sub-toggles enabled
   ✓ Sub-toggles respond to taps
   
3. Toggle Order Updates
   ✓ Toast: "Settings saved successfully"
   ✓ Toggle state persists after restart
   
4. Toggle Promotions
   ✓ Toggle state persists
   
5. Toggle System Alerts
   ✓ Toggle state persists
```

### Location & Map Section
```
1. Toggle GPS
   ✓ Toast: "Settings saved successfully"
   
2. Select Map View
   ✓ Radio button selection works
   ✓ Only one option selected at a time
   ✓ Setting persists after restart
   
3. Test All Map View Options
   ✓ Map View
   ✓ List View
   ✓ Both Views
```

### Privacy & Data Section
```
1. Toggle Data Sharing
   ✓ State persists
   
2. Toggle Recommendations
   ✓ State persists
   
3. Clear Search History
   ✓ Confirmation dialog appears
   ✓ Toast: "Search history cleared"
   
4. Clear Recently Viewed
   ✓ Confirmation dialog appears
   ✓ Toast: "Recently viewed items cleared"
```

### Display & Theme Section
```
1. Select Theme Mode
   ✓ System Default selected by default
   ✓ Can select Light theme
   ✓ Can select Dark theme
   ✓ Setting persists
   ✓ Note: May require app restart to fully apply
   
2. High Contrast Toggle
   ✓ State persists
   
3. Select Language
   ✓ Dialog shows English, Hindi, Marathi
   ✓ Selection updates display
   ✓ Toast: "Language changed to [language]"
   ✓ Note: Requires app restart to apply
```

### Orders & History Section
```
1. Select Order Sort
   ✓ Newest First (default)
   ✓ Oldest First
   ✓ Status Based
   ✓ Only one selected at time
   ✓ Setting persists
   
2. Toggle Order Suggestions
   ✓ State persists
```

### Help & Legal Section
```
1. Tap Help & Support
   ✓ Opens CustomerHelpSupportActivity
   
2. Tap Terms & Conditions
   ✓ Toast: "Terms & Conditions screen coming soon"
   
3. Tap Privacy Policy
   ✓ Toast: "Privacy Policy screen coming soon"
   
4. Tap About Food Van
   ✓ Toast: "About screen coming soon"
```

---

## 💾 Persistence Testing

### Test Settings Persist on Restart
```
1. Toggle: Notifications OFF
2. Select: Map View = "List View"
3. Press Home button (or close app)
4. Reopen Food Van app
5. Go back to Settings
6. Verify:
   ✓ Notifications still OFF
   ✓ Map View still "List View"
```

### Test Multiple Settings
```
1. Enable all toggles
2. Select Light theme
3. Select Hindi language
4. Select Oldest First sort
5. Restart app
6. Check Settings
7. Verify all settings maintained
```

### Test Cloud Sync (if Firebase connected)
```
1. Toggle a setting
2. Check Firebase Realtime Database
3. Path: customers/{userId}/settings/
4. Verify the setting appears there
5. Check lastUpdated timestamp
```

---

## 🔗 Integration Testing

### Test Navigation Integration
```
1. Open Settings from Dashboard menu
   ✓ No errors
   ✓ Smooth transition
   
2. Tap Edit Profile
   ✓ Opens EditPersonalInfoActivity
   ✓ Can edit info
   ✓ Back button returns to Settings
   
3. Tap Phone Verification
   ✓ Opens PhoneVerificationActivity
   ✓ Back button returns to Settings
   
4. Tap Saved Addresses
   ✓ Opens SavedAddressesActivity
   ✓ Back button returns to Settings
   
5. Tap Payment Methods
   ✓ Opens PaymentMethodsActivity
   ✓ Back button returns to Settings
   
6. Back from Settings
   ✓ Returns to Customer Dashboard
   ✓ Dashboard state maintained
```

### Test No Side Effects
```
1. Verify Vendor Dashboard unaffected
   ✓ Vendor menu unchanged
   ✓ Vendor settings work normally
   
2. Verify Customer Dashboard unaffected
   ✓ Map loads normally
   ✓ Cart works normally
   ✓ Orders accessible
   ✓ Profile accessible
   ✓ Notifications work normally
```

---

## 📱 Device Testing

### Screen Sizes
- [ ] 5" phone (720x1280)
- [ ] 5.5" phone (1080x1920)
- [ ] 6" phone (1080x2220)
- [ ] 6.5" tablet (1440x2560)

### Orientations
- [ ] Portrait (scrollable content)
- [ ] Landscape (content accessible)

### Android Versions
- [ ] Android 10 (API 29)
- [ ] Android 11 (API 30)
- [ ] Android 12 (API 31)
- [ ] Android 13 (API 33)
- [ ] Android 14 (API 34)
- [ ] Android 15 (API 35)

### Themes
- [ ] Light theme
- [ ] Dark theme
- [ ] System theme (based on device setting)

---

## 🐛 Debugging Tips

### Check SharedPreferences
```java
// Add to CustomerSettingsActivity for debugging
SharedPreferences pref = getSharedPreferences("CustomerSettings", Context.MODE_PRIVATE);
Map<String, ?> allEntries = pref.getAll();
for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
    Log.d("SettingsDebug", entry.getKey() + " = " + entry.getValue().toString());
}
```

### Monitor ViewModel LiveData
```java
// Already built-in, watch Logcat
// Search for "SettingsManager" and "CustomerSettingsViewModel"
```

### Check Firebase (if available)
```
1. Open Firebase Console
2. Project: Food Van
3. Realtime Database
4. Path: customers/{uid}/settings
5. Verify values update in real-time
```

### Network & Permissions
```
1. Settings → Apps → Food Van
   ✓ Check permissions granted
   ✓ Notifications allowed
   ✓ Location enabled (for GPS test)
   
2. Test offline mode
   ✓ Turn off WiFi + Mobile data
   ✓ Change settings
   ✓ Verify stored locally
   ✓ Turn on connectivity
   ✓ Verify Firebase syncs
```

---

## ✅ Final Verification Checklist

Before marking as complete:

### Code Quality
- [ ] No compile errors
- [ ] No runtime crashes
- [ ] No ANR (Application Not Responding)
- [ ] No memory leaks
- [ ] Logcat clean

### UI Quality
- [ ] All text readable
- [ ] All buttons accessible
- [ ] No UI glitches
- [ ] Animations smooth
- [ ] Loading states handled

### Functionality
- [ ] All toggles work
- [ ] All radio groups work
- [ ] All buttons navigate correctly
- [ ] Settings persist correctly
- [ ] No data loss

### Compliance
- [ ] Material 3 design followed
- [ ] Orange accent used consistently
- [ ] Accessibility requirements met
- [ ] No hardcoded values
- [ ] Responsive design

### Integration
- [ ] Dashboard menu works
- [ ] Navigation safe
- [ ] No side effects
- [ ] Related screens accessible
- [ ] Back button behavior correct

### Documentation
- [ ] README created
- [ ] Code commented
- [ ] String resources complete
- [ ] Implementation guide detailed

---

## 🎯 Expected Behavior Summary

| Feature | Expected | Status |
|---------|----------|--------|
| Settings opens from menu | Immediate launch | ✅ |
| All sections visible | Scrollable view | ✅ |
| Toggles switch instantly | State change immediate | ✅ |
| Radio selections work | One option selected | ✅ |
| Language dialog shows options | Dialog with 3 languages | ✅ |
| Clear confirmations appear | Dialog before action | ✅ |
| Navigation to other screens | Link opens activity | ✅ |
| Back button returns | Safe return to Dashboard | ✅ |
| Settings persist | Survives app restart | ✅ |
| Dark mode supported | Proper colors applied | ✅ |
| Status bar clear | No overlapping | ✅ |

---

## 📞 Troubleshooting Quick Ref

| Problem | Solution |
|---------|----------|
| Settings not loading | Check Firebase credentials |
| Toggles not responding | Verify SwitchMaterial IDs match layout |
| Settings not persisting | Check SharedPreferences permissions |
| Dark mode looks wrong | Clear app cache & restart |
| Navigation crashing | Verify target activities exist |
| Overlapping status bar | Check `fitsSystemWindows="true"` |
| Text not readable | Verify text sizes in dimens.xml |
| Sync not working | Check Firebase Realtime DB rules |

---

**Last Updated**: December 6, 2025
**Ready for Testing**: ✅ YES
