# 🔑 Firebase Authentication API - Enable Step-by-Step

## 🎯 **Enable Firebase Authentication API**

### **Step 1: Go to Google Cloud Console**
1. **Open**: https://console.cloud.google.com/
2. **Select your Firebase project** (same project as your Food Van app)
3. **Ensure you're in the correct project** - check project name at the top

### **Step 2: Navigate to APIs & Services**
1. **Click the hamburger menu** (☰) in the top-left
2. **Go to**: "APIs & Services" → "Library"
3. **Or use direct link**: https://console.cloud.google.com/apis/library

### **Step 3: Search for Firebase Authentication API**
1. **In the search bar**, type: `Firebase Authentication API`
2. **Click on**: "Firebase Authentication API" from results
3. **Alternative search**: "Identity Toolkit API" (same service)

### **Step 4: Enable the API**
1. **Click the "ENABLE" button**
2. **Wait for activation** (usually takes 10-30 seconds)
3. **You'll see**: "API enabled" confirmation

### **Step 5: Verify API is Enabled**
1. **Go to**: APIs & Services → "Enabled APIs & services"
2. **Look for**: "Firebase Authentication API" or "Identity Toolkit API"
3. **Status should show**: "Enabled"

---

## 🔧 **Additional Required APIs**

### **Enable Identity and Access Management (IAM) API:**
1. **Search for**: `Identity and Access Management (IAM) API`
2. **Click on it** and press **"ENABLE"**

### **Enable Cloud Identity Toolkit API:**
1. **Search for**: `Identity Toolkit API`
2. **Click on it** and press **"ENABLE"**

---

## 📱 **Verify in Firebase Console**

### **Check Firebase Authentication Setup:**
1. **Go to**: https://console.firebase.google.com/
2. **Select your Food Van project**
3. **Navigate to**: Authentication → Sign-in method
4. **Ensure Phone provider is enabled**
5. **Add test numbers** (recommended):
   - Phone: `+91 9876543210`
   - SMS Code: `123456`

---

## ⚡ **Quick Verification Commands**

### **Check API Status via gcloud CLI (Optional):**
```bash
# List enabled APIs
gcloud services list --enabled --filter="name:identitytoolkit"

# Enable API via command line
gcloud services enable identitytoolkit.googleapis.com
```

---

## 🚨 **Troubleshooting**

### **If API Enable Button is Grayed Out:**
1. **Check billing**: Ensure billing account is linked
2. **Check permissions**: You need "Editor" or "Owner" role
3. **Try different browser**: Clear cache or use incognito

### **If Still Getting BILLING_NOT_ENABLED:**
1. **Wait 5-10 minutes** after enabling APIs
2. **Clear app data** and try again
3. **Check quotas**: APIs & Services → Quotas
4. **Verify project ID** matches your app

### **Common API Names to Look For:**
- ✅ **Firebase Authentication API**
- ✅ **Identity Toolkit API** 
- ✅ **Cloud Identity and Access Management (IAM) API**
- ✅ **Google Cloud Identity and Access Management (IAM) API**

---

## 🎯 **Expected Result**

After enabling these APIs, your phone verification should work without the "BILLING_NOT_ENABLED" error.

### **Test Your Setup:**
1. **Run your Food Van app**
2. **Go to**: Profile → Edit Personal Information
3. **Enter phone number**: `9876543210` (if using test numbers)
4. **Tap "Verify"**
5. **Enter OTP**: `123456`
6. **Should complete successfully** ✅

---

## 📋 **API Enable Checklist**

- [ ] **Firebase Authentication API** - Enabled
- [ ] **Identity Toolkit API** - Enabled  
- [ ] **Cloud IAM API** - Enabled
- [ ] **Billing Account** - Linked (can be free tier)
- [ ] **Phone Provider** - Enabled in Firebase Console
- [ ] **Test Numbers** - Added (optional but recommended)
- [ ] **SHA-1 Fingerprint** - Added to Firebase project

---

## 🔗 **Quick Links**

- **Google Cloud Console**: https://console.cloud.google.com/
- **Firebase Console**: https://console.firebase.google.com/
- **APIs Library**: https://console.cloud.google.com/apis/library
- **Enabled APIs**: https://console.cloud.google.com/apis/dashboard

Once you complete these steps, your Firebase Phone Authentication should work without billing errors!
