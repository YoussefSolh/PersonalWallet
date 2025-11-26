# Google Sign-In Setup - Quick Start

## 📌 Overview

This app supports two authentication methods:
1. **Google Sign-In** (recommended) - Sync data across devices
2. **Guest mode** (no account required) - Use locally without sign-in

---

## ⚡ **Quick Setup (5 Steps)**

### Step 1: Get SHA-1 Fingerprint
```cmd
# Windows
gradlew signingReport

# Mac/Linux
./gradlew signingReport
```
Copy the SHA-1 value (format: `XX:XX:XX:...`)

### Step 2: Create Google Cloud Project
1. Go to https://console.cloud.google.com/
2. Create new project: "Personal Wallet"

### Step 3: Configure OAuth
1. **OAuth consent screen** → External → Add your email
2. Add scopes: email, profile, openid
3. Add test users

### Step 4: Create Credentials
1. Create **Android** OAuth client with:
   - Package: `com.youssefsolh.personalwallet`
   - SHA-1: (from Step 1)

2. Create **Web** OAuth client
   - Copy the **Client ID**

### Step 5: Update App
1. Open `app/src/main/res/values/strings.xml`
2. Replace `YOUR_WEB_CLIENT_ID_HERE` with your Web Client ID
3. Rebuild: `gradlew clean assembleDebug`

---

## 📚 **Need Detailed Instructions?**

### 🎯 For Comprehensive Setup

**See:** [`GOOGLE_CLOUD_COMPLETE_SETUP.md`](GOOGLE_CLOUD_COMPLETE_SETUP.md)

This 20+ page guide includes:
- ✅ Detailed step-by-step instructions with screenshots descriptions
- ✅ Multiple methods for each step
- ✅ Windows, Mac, Linux specific commands
- ✅ Comprehensive troubleshooting section
- ✅ FAQ with 10+ common questions
- ✅ Visual workflow diagrams
- ✅ Checklist to track progress
- ✅ Production deployment guide

### 🔧 For Technical Details

**See:** [`GOOGLE_SIGNIN_IMPLEMENTATION_REPORT.md`](GOOGLE_SIGNIN_IMPLEMENTATION_REPORT.md)

Technical report including:
- Code architecture overview
- Security considerations
- Testing strategies
- Migration paths
- Best practices

---

## 🚨 **Common Issues**

### Error 12500: "Sign in failed"
- **Fix**: Verify SHA-1 matches exactly
- Wait 5-10 minutes for Google to propagate changes

### "Invalid token"
- **Fix**: Check Web Client ID in `strings.xml`
- Ensure it ends with `.apps.googleusercontent.com`

### "Developer Error"
- **Fix**: Create both Android AND Web OAuth clients
- Verify package name: `com.youssefsolh.personalwallet`

**More troubleshooting**: See [`GOOGLE_CLOUD_COMPLETE_SETUP.md`](GOOGLE_CLOUD_COMPLETE_SETUP.md#troubleshooting)

---

## ✅ **Testing Checklist**

- [ ] SHA-1 fingerprint obtained
- [ ] Google Cloud project created
- [ ] OAuth consent screen configured
- [ ] Android OAuth client created (with SHA-1)
- [ ] Web OAuth client created (with Client ID)
- [ ] strings.xml updated with Web Client ID
- [ ] App rebuilt successfully
- [ ] Sign-in tested and working

---

## 🎯 **What's Next?**

After Google Sign-In works:
1. Test with multiple accounts
2. Test sign-out and re-sign-in
3. Add release keystore SHA-1 for production
4. Consider app verification for public release

---

## 📖 **Documentation Index**

| Document | Purpose | Audience |
|----------|---------|----------|
| **GOOGLE_SIGNIN_SETUP.md** (this file) | Quick start guide | All users |
| **GOOGLE_CLOUD_COMPLETE_SETUP.md** | Detailed setup with troubleshooting | First-time users |
| **GOOGLE_SIGNIN_IMPLEMENTATION_REPORT.md** | Technical implementation details | Developers |
| **BUILD_FIXES.md** | Build error solutions | All users |
| **IMPLEMENTATION_SUMMARY.md** | All features overview | All users |

---

## 💡 **Pro Tips**

1. **Save your SHA-1 values** (debug and release) for future reference
2. **Add multiple test users** in OAuth consent screen
3. **Keep your Web Client ID secure** - don't commit to public repos
4. **Use Guest mode** as fallback - always works offline
5. **Check Logcat** filter by `AuthService` or `AuthRepository` for debugging

---

## 🆘 **Need Help?**

1. **Read the comprehensive guide**: [`GOOGLE_CLOUD_COMPLETE_SETUP.md`](GOOGLE_CLOUD_COMPLETE_SETUP.md)
2. **Check troubleshooting section** in the comprehensive guide
3. **Review technical report**: [`GOOGLE_SIGNIN_IMPLEMENTATION_REPORT.md`](GOOGLE_SIGNIN_IMPLEMENTATION_REPORT.md)
4. **Check Logcat** for detailed error messages
5. **Stack Overflow**: Tag `google-signin` + `android`

---

**Status**: ✅ Production Ready (after configuration)
**Last Updated**: November 25, 2025
**Version**: 2.0 (Enhanced)
