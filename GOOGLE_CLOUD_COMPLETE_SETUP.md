# Complete Google Sign-In Setup Guide for Personal Wallet

## 📋 **Table of Contents**

1. [Prerequisites](#prerequisites)
2. [Part 1: Get Your SHA-1 Fingerprint](#part-1-get-your-sha-1-fingerprint)
3. [Part 2: Create Google Cloud Project](#part-2-create-google-cloud-project)
4. [Part 3: Configure OAuth Consent Screen](#part-3-configure-oauth-consent-screen)
5. [Part 4: Create OAuth Credentials](#part-4-create-oauth-credentials)
6. [Part 5: Update Your Android App](#part-5-update-your-android-app)
7. [Part 6: Testing](#part-6-testing)
8. [Troubleshooting](#troubleshooting)
9. [FAQ](#faq)

---

## Prerequisites

Before you begin, ensure you have:
- ✅ A Google account
- ✅ Android Studio installed
- ✅ Your Personal Wallet project open in Android Studio
- ✅ Internet connection
- ✅ Access to a terminal/command prompt

**Estimated Time**: 20-30 minutes

---

## Part 1: Get Your SHA-1 Fingerprint

The SHA-1 fingerprint uniquely identifies your app for Google Sign-In.

### Option A: Using Gradle (Recommended)

#### Windows:
1. Open Command Prompt or PowerShell
2. Navigate to your project directory:
   ```cmd
   cd C:\GitHub\YoussefSolh\PersonalWallet
   ```
3. Run the signing report command:
   ```cmd
   gradlew signingReport
   ```

#### Mac/Linux:
```bash
cd /path/to/PersonalWallet
./gradlew signingReport
```

### Option B: Using Keytool

#### Windows:
```cmd
keytool -keystore "%USERPROFILE%\.android\debug.keystore" -list -v -storepass android
```

#### Mac/Linux:
```bash
keytool -keystore ~/.android/debug.keystore -list -v -storepass android
```

### 📝 Find Your SHA-1

Look for output similar to this:
```
Variant: debug
Config: debug
Store: C:\Users\YourName\.android\debug.keystore
Alias: AndroidDebugKey
MD5: AA:BB:CC:DD:EE:FF:11:22:33:44:55:66:77:88:99:00
SHA1: 1A:2B:3C:4D:5E:6F:7A:8B:9C:0D:1E:2F:3A:4B:5C:6D:7E:8F:9A:0B
SHA-256: ...
Valid until: ...
```

**Important:** Copy the **SHA-1** value (format: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX)

### ⚠️ Debug vs Release

- **Debug builds**: Use debug.keystore SHA-1 (for development)
- **Release builds**: Use your release keystore SHA-1 (for production)

💡 **Tip**: Save both SHA-1 values for later!

---

## Part 2: Create Google Cloud Project

### Step 1: Go to Google Cloud Console

1. Open your browser and go to: https://console.cloud.google.com/
2. Sign in with your Google account

### Step 2: Create a New Project

1. Click the **project dropdown** (top-left, next to "Google Cloud")
2. Click **"NEW PROJECT"**
3. Fill in project details:
   - **Project name**: `Personal Wallet` (or any name you prefer)
   - **Organization**: None (unless you have one)
   - **Location**: No organization
4. Click **"CREATE"**

**Wait time**: ~10-30 seconds for project creation

### Step 3: Select Your Project

1. Once created, click the **project dropdown** again
2. Select your new project: **Personal Wallet**
3. Verify the project name appears in the top bar

---

## Part 3: Configure OAuth Consent Screen

Google requires you to configure an OAuth consent screen before creating credentials.

### Step 1: Navigate to OAuth Consent Screen

1. In the left sidebar, click **"APIs & Services"**
2. Click **"OAuth consent screen"**

   **If you don't see it**:
   - Click the **hamburger menu** (☰) top-left
   - Hover over **"APIs & Services"**
   - Select **"OAuth consent screen"**

### Step 2: Choose User Type

1. Select **"External"** (for testing with any Google account)
2. Click **"CREATE"**

**Note**: "Internal" is only available for Google Workspace organizations.

### Step 3: Fill OAuth Consent Screen Details

#### App Information:
- **App name**: `Personal Wallet`
- **User support email**: Your email address (select from dropdown)
- **App logo**: Optional (you can upload a 120x120 logo later)

#### App Domain (Optional for testing):
- **Application home page**: Leave blank for now
- **Application privacy policy link**: Leave blank for now
- **Application terms of service link**: Leave blank for now

#### Developer Contact Information:
- **Email addresses**: Your email address

#### Click **"SAVE AND CONTINUE"**

### Step 4: Scopes

1. On the "Scopes" page, click **"ADD OR REMOVE SCOPES"**
2. Find and select these scopes:
   - ✅ `.../auth/userinfo.email` (See your email address)
   - ✅ `.../auth/userinfo.profile` (See your personal info)
   - ✅ `openid` (Associate you with your personal info)
3. Click **"UPDATE"**
4. Click **"SAVE AND CONTINUE"**

### Step 5: Test Users

1. Click **"+ ADD USERS"**
2. Enter your Google email address (for testing)
3. Add any other Gmail accounts you want to test with
4. Click **"ADD"**
5. Click **"SAVE AND CONTINUE"**

### Step 6: Summary

1. Review your settings
2. Click **"BACK TO DASHBOARD"**

✅ **OAuth Consent Screen is now configured!**

---

## Part 4: Create OAuth Credentials

Now you'll create two OAuth clients: one for Android and one for Web.

### Step 1: Navigate to Credentials

1. In the left sidebar, click **"Credentials"**
2. Click **"+ CREATE CREDENTIALS"** (at the top)
3. Select **"OAuth client ID"**

### Step 2: Create Android OAuth Client

#### A. Select Application Type:
- **Application type**: Select **"Android"**

#### B. Fill in Details:
- **Name**: `Personal Wallet Android` (or any descriptive name)
- **Package name**: `com.youssefsolh.personalwallet`

  ⚠️ **IMPORTANT**: This MUST match your app's package name exactly!

  **To verify**: Check `app/build.gradle.kts`:
  ```kotlin
  namespace = "com.youssefsolh.personalwallet"
  ```

- **SHA-1 certificate fingerprint**: Paste your SHA-1 from Part 1
  - Example: `1A:2B:3C:4D:5E:6F:7A:8B:9C:0D:1E:2F:3A:4B:5C:6D:7E:8F:9A:0B`

#### C. Create:
- Click **"CREATE"**
- You'll see a popup: **"OAuth client created"**
- Click **"OK"** (you don't need to copy anything from this popup)

### Step 3: Create Web OAuth Client

**Why do you need both?** Android client authenticates the app, but the Web client ID token is used for backend verification.

#### A. Create Another Credential:
1. Click **"+ CREATE CREDENTIALS"** again
2. Select **"OAuth client ID"** again

#### B. Select Application Type:
- **Application type**: Select **"Web application"**

#### C. Fill in Details:
- **Name**: `Personal Wallet Web` (or any descriptive name)
- **Authorized JavaScript origins**: Leave empty for now
- **Authorized redirect URIs**: Leave empty for now

#### D. Create:
- Click **"CREATE"**
- You'll see: **"OAuth client created"**
- **THIS TIME, COPY THE CLIENT ID!**

  📋 **Copy this value - you'll need it soon!**

  Format: `123456789012-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com`

- Click **"OK"**

### Step 4: Find Your Web Client ID (if you missed it)

1. On the Credentials page, you'll see both clients listed
2. Find the one with type **"OAuth 2.0 Client IDs"** and name **"Personal Wallet Web"**
3. Click the **download icon** (↓) or the **client name**
4. Copy the **Client ID**

---

## Part 5: Update Your Android App

### Step 1: Update strings.xml

1. In Android Studio, open: `app/src/main/res/values/strings.xml`

2. Find this line:
   ```xml
   <string name="default_web_client_id" translatable="false">YOUR_WEB_CLIENT_ID_HERE</string>
   ```

3. Replace `YOUR_WEB_CLIENT_ID_HERE` with your **Web Client ID** from Step 4:
   ```xml
   <string name="default_web_client_id" translatable="false">123456789012-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com</string>
   ```

4. Save the file (Ctrl+S / Cmd+S)

### Step 2: Sync Gradle

1. Click **"Sync Now"** if prompted
2. Or: **File → Sync Project with Gradle Files**

### Step 3: Clean and Rebuild

```bash
# In terminal:
gradlew clean
gradlew assembleDebug
```

Or in Android Studio:
- **Build → Clean Project**
- **Build → Rebuild Project**

---

## Part 6: Testing

### Step 1: Install the App

1. Connect your Android device or start an emulator
2. Click **Run** (green play button) in Android Studio
3. Or use:
   ```bash
   gradlew installDebug
   ```

### Step 2: Test Google Sign-In

1. **Launch the app**
2. On the login screen, tap **"Sign in with Google"**
3. You should see:
   - Account picker dialog
   - Your Google account(s) listed
4. **Select your account**
5. You may see a consent screen (first time only)
6. Tap **"Continue"** or **"Allow"**
7. You should be signed in successfully!

### Step 3: Verify Sign-In

After successful sign-in:
- ✅ You should see the Dashboard screen
- ✅ Your profile info should be loaded
- ✅ You can create wallets and transactions

### Step 4: Check Logs

In Android Studio, open **Logcat** and filter by:
- **Tag**: `AuthService` or `AuthRepository`
- Look for:
  ```
  D/AuthService: User signed in successfully
  D/AuthRepository: Successfully signed in: your.email@gmail.com
  ```

---

## Troubleshooting

### Error: "Sign in failed" or "12500: Sign in failed"

**Cause**: SHA-1 fingerprint mismatch or OAuth not configured

**Solutions**:
1. **Verify SHA-1**:
   - Run `gradlew signingReport` again
   - Compare with SHA-1 in Google Cloud Console
   - Make sure they match EXACTLY (including colons)

2. **Check Package Name**:
   - Google Cloud Console: `com.youssefsolh.personalwallet`
   - Must match `namespace` in `app/build.gradle.kts`

3. **Wait for Propagation**:
   - Google Cloud changes can take 5-10 minutes
   - Try again after waiting

4. **Clear App Data**:
   - Settings → Apps → Personal Wallet → Storage → Clear Data
   - Reinstall the app

### Error: "API not enabled"

**Solution**:
1. Go to: https://console.cloud.google.com/apis/library
2. Search for: **"Google Sign-In API"** or **"Google Identity Services"**
3. Click **"ENABLE"**
4. Wait 1-2 minutes, then try again

### Error: "Invalid token" or "Token verification failed"

**Cause**: Web Client ID mismatch

**Solutions**:
1. **Verify Web Client ID**:
   - Open `strings.xml`
   - Compare with Web Client ID in Google Cloud Console
   - Make sure it's the **Web** client, not Android client

2. **Check for typos**:
   - No extra spaces
   - Complete ID (ends with `.apps.googleusercontent.com`)

3. **Rebuild the app**:
   ```bash
   gradlew clean assembleDebug
   ```

### Error: "Developer Error" Dialog

**Cause**: Android OAuth client not configured or SHA-1 mismatch

**Solution**:
1. Verify you created **both** Android AND Web OAuth clients
2. Check SHA-1 in Android client matches your debug.keystore
3. Verify package name is correct

### Error: "Sign in not configured"

**Cause**: Client ID is still "YOUR_WEB_CLIENT_ID_HERE"

**Solution**:
1. Open `strings.xml`
2. Replace placeholder with real Web Client ID
3. Rebuild app

### App Crashes on Sign-In

**Check Logcat for**:
1. `AuthService` or `AuthRepository` errors
2. Missing dependencies
3. ProGuard issues (release build)

**Solutions**:
1. Ensure all dependencies are synced
2. Check ProGuard rules in `app/proguard-rules.pro`
3. Try debug build first

### OAuth Consent Screen Shows "Unverified App"

**This is NORMAL for development!**

**What it means**:
- Your app isn't verified by Google
- Only happens for external OAuth apps
- Safe to continue for testing

**To bypass**:
1. Click **"Advanced"**
2. Click **"Go to Personal Wallet (unsafe)"**
3. Grant permissions

**To remove warning** (optional, for production):
1. Complete Google's app verification process
2. See: https://support.google.com/cloud/answer/7454865

---

## FAQ

### Q: Do I need to pay for Google Cloud?

**A:** No! Google Sign-In API is free for most usage. You only pay if you exceed very high quotas (millions of requests).

### Q: Can I test with any Gmail account?

**A:** Yes, if you configured OAuth as "External". Add test users in the OAuth consent screen.

### Q: What's the difference between Android and Web OAuth clients?

**A:**
- **Android client**: Authenticates the app using SHA-1 and package name
- **Web client**: Provides ID token for backend verification
- **You need BOTH** for Google Sign-In to work properly

### Q: Do I need to configure Firebase?

**A:** No! This setup uses Google Sign-In directly, not Firebase Authentication.

### Q: Can I use the same Google Cloud project for multiple apps?

**A:** Yes, but create separate OAuth clients for each app (different package names/SHA-1s).

### Q: How do I sign in on a release build?

**A:**
1. Get your **release keystore SHA-1**
2. Add it to the Android OAuth client in Google Cloud Console:
   - Go to Credentials → Your Android Client → Edit
   - Add another SHA-1 fingerprint
3. Rebuild release APK

### Q: What if I change my package name?

**A:** Update the package name in the Android OAuth client in Google Cloud Console.

### Q: Can I use OAuth for backend authentication?

**A:** Yes! The ID token can be verified on your backend. See Google's documentation on ID token verification.

### Q: Guest mode works but Google Sign-In doesn't?

**A:** This is a configuration issue. Follow troubleshooting steps above. Guest mode doesn't require Google setup.

---

## Additional Resources

### Official Documentation:
- **Google Sign-In for Android**: https://developers.google.com/identity/sign-in/android/start
- **OAuth 2.0**: https://developers.google.com/identity/protocols/oauth2
- **API Console Help**: https://support.google.com/googleapi

### Video Tutorials:
- Search YouTube for: "Android Google Sign-In tutorial"
- Look for recent videos (2023-2024)

### Community Support:
- Stack Overflow: Tag `google-signin` + `android`
- Google Groups: https://groups.google.com/g/google-identity-dev

---

## Checklist

Use this checklist to track your progress:

- [ ] Part 1: Got SHA-1 fingerprint
- [ ] Part 2: Created Google Cloud project
- [ ] Part 3: Configured OAuth consent screen
- [ ] Part 4: Created Android OAuth client
- [ ] Part 4: Created Web OAuth client
- [ ] Part 4: Copied Web Client ID
- [ ] Part 5: Updated strings.xml
- [ ] Part 5: Cleaned and rebuilt app
- [ ] Part 6: Tested Google Sign-In
- [ ] Part 6: Successfully signed in!

---

## Next Steps

After Google Sign-In is working:

1. **Test with Multiple Accounts**:
   - Sign in
   - Sign out
   - Sign in with different account
   - Verify data persistence

2. **Test Edge Cases**:
   - Enable/disable airplane mode during sign-in
   - Close app during sign-in
   - Revoke permissions in Google account settings

3. **Production Preparation**:
   - Add release keystore SHA-1
   - Consider app verification
   - Add privacy policy and terms of service
   - Test with non-developer accounts

4. **Consider Security**:
   - Add biometric authentication
   - Implement session timeout
   - Add backend token verification

---

## Summary

You've successfully set up Google Sign-In for your Personal Wallet app! 🎉

**What you configured**:
- ✅ Google Cloud Project
- ✅ OAuth Consent Screen
- ✅ Android OAuth Client (with SHA-1)
- ✅ Web OAuth Client (for ID tokens)
- ✅ App configuration (strings.xml)

**What your app can now do**:
- ✅ Authenticate users with Google
- ✅ Access user profile information
- ✅ Provide seamless sign-in experience
- ✅ Fallback to Guest mode if needed

**Need help?** Review the [Troubleshooting](#troubleshooting) section or check the [FAQ](#faq).

---

**Last Updated**: November 25, 2025
**Version**: 1.1.0
**Status**: Production Ready 🚀
