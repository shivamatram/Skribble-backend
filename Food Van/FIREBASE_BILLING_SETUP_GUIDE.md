# 💳 Firebase Billing Setup - Complete Guide

## 🎯 **Enable Billing for Firebase Phone Authentication**

### **Step 1: Go to Firebase Console**
1. **Open**: https://console.firebase.google.com/
2. **Select your Food Van project**
3. **Click the gear icon** ⚙️ (Project Settings)

### **Step 2: Navigate to Usage and Billing**
1. **Click on**: "Usage and billing" tab
2. **Or go to**: "Plan" section in left sidebar
3. **You'll see**: Current plan status (likely "Spark - Free")

### **Step 3: Upgrade to Blaze Plan (Pay-as-you-go)**
1. **Click**: "Modify plan" or "Upgrade"
2. **Select**: "Blaze (Pay as you go)" plan
3. **Click**: "Continue" or "Select plan"

### **Step 4: Link Billing Account**
1. **You'll be redirected** to Google Cloud Billing
2. **Choose existing billing account** OR **Create new one**

#### **If Creating New Billing Account:**
1. **Click**: "Create billing account"
2. **Enter**: Account name (e.g., "Food Van App Billing")
3. **Select**: Country/region
4. **Add payment method**: Credit/Debit card or Bank account
5. **Fill in**: Billing address details
6. **Click**: "Start my free trial" or "Submit and enable billing"

#### **If Using Existing Billing Account:**
1. **Select**: Your existing billing account from dropdown
2. **Click**: "Set account"

### **Step 5: Confirm Billing Setup**
1. **Return to Firebase Console**
2. **Check**: Plan should now show "Blaze (Pay as you go)"
3. **Verify**: Usage and billing tab shows linked account

---

## 💰 **Cost Information (Don't Worry - It's Cheap!)**

### **Firebase Phone Authentication Pricing:**
- ✅ **First 10 verifications/day**: **FREE**
- ✅ **After 10 verifications**: **$0.01 per verification**
- ✅ **Perfect for development**: You won't be charged during testing

### **Example Costs:**
- **100 verifications/month**: ~$2.70
- **500 verifications/month**: ~$14.70
- **1000 verifications/month**: ~$29.70

### **Free Tier Benefits:**
- **10 phone verifications per day** at no cost
- **Generous limits** for development and testing
- **No charges** until you exceed free limits

---

## 🔒 **Set Up Billing Alerts (Recommended)**

### **Step 1: Go to Google Cloud Console**
1. **Open**: https://console.cloud.google.com/
2. **Select your Firebase project**
3. **Navigate to**: Billing → Budgets & alerts

### **Step 2: Create Budget Alert**
1. **Click**: "Create budget"
2. **Set budget amount**: $5 or $10 (for safety)
3. **Set alert thresholds**: 50%, 90%, 100%
4. **Add email notifications**
5. **Click**: "Finish"

---

## ⚡ **Alternative: Use Test Phone Numbers (No Billing Required)**

### **If You Don't Want to Enable Billing Yet:**

1. **Go to Firebase Console** → Authentication → Sign-in method → Phone
2. **Scroll to**: "Phone numbers for testing"
3. **Add test numbers**:
   - Phone: `+91 9876543210`, Code: `123456`
   - Phone: `+91 9999999999`, Code: `654321`
   - Phone: `+91 8888888888`, Code: `111111`

### **Test in Your App:**
- **Enter**: `9876543210`
- **Use code**: `123456`
- **Works immediately** without billing setup!

---

## 🚨 **Troubleshooting Billing Issues**

### **If Billing Setup Fails:**
1. **Check card details**: Ensure valid payment method
2. **Try different browser**: Clear cache or use incognito
3. **Contact support**: Google Cloud Billing support
4. **Use test numbers**: As temporary workaround

### **If Still Getting BILLING_NOT_ENABLED:**
1. **Wait 10-15 minutes** after enabling billing
2. **Clear app data** and restart
3. **Check APIs are enabled**:
   - Firebase Authentication API
   - Identity Toolkit API
   - Cloud IAM API
4. **Verify project ID** matches your app

### **Common Issues:**
- ❌ **Card declined**: Try different payment method
- ❌ **Region restrictions**: Some regions have limitations
- ❌ **Account verification**: May need to verify Google account
- ❌ **API not enabled**: Enable required APIs first

---

## 📋 **Billing Setup Checklist**

- [ ] **Firebase Console** → Project Settings → Usage and billing
- [ ] **Upgrade to Blaze plan** (Pay-as-you-go)
- [ ] **Link billing account** (new or existing)
- [ ] **Verify plan shows "Blaze"** in Firebase Console
- [ ] **Set up billing alerts** (recommended)
- [ ] **Test phone verification** in your app
- [ ] **Add test numbers** (optional but recommended)

---

## 🎯 **Quick Test After Setup**

### **Test Your Phone Verification:**
1. **Run Food Van app**
2. **Go to**: Profile → Edit Personal Information
3. **Enter phone**: Your real number or test number
4. **Tap**: "Send Verification Code"
5. **Should work** without BILLING_NOT_ENABLED error ✅

---

## 💡 **Pro Tips**

### **For Development:**
- ✅ **Use test numbers** to avoid SMS charges
- ✅ **Set low billing alerts** ($5-10)
- ✅ **Monitor usage** in Firebase Console
- ✅ **Free tier covers** most development needs

### **For Production:**
- ✅ **Enable billing** for real users
- ✅ **Monitor costs** regularly
- ✅ **Set appropriate budgets**
- ✅ **Consider SMS costs** in your app pricing

---

## 🔗 **Quick Links**

- **Firebase Console**: https://console.firebase.google.com/
- **Google Cloud Billing**: https://console.cloud.google.com/billing
- **Firebase Pricing**: https://firebase.google.com/pricing
- **Phone Auth Pricing**: https://firebase.google.com/docs/auth/usage-pricing

**Remember**: You can start with test numbers (no billing) and enable billing later when you're ready for production!
