# Firebase Setup Guide for SketchApp

This guide will walk you through setting up Firebase for the SketchApp, enabling Google Sign-In authentication, Firestore database, and Cloud Storage.

> **⚠️ IMPORTANT SECURITY NOTE:**
> Never commit `google-services.json` or `local.properties` to version control!
> These files contain sensitive credentials. See [SECRETS_SETUP.md](SECRETS_SETUP.md) for details on proper secrets management.

## Prerequisites

- Android Studio installed
- SketchApp project open in Android Studio
- A Google account for Firebase Console access

---

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Enter project name: **"SketchApp"** (or your preferred name)
4. (Optional) Enable Google Analytics
5. Click **"Create project"** and wait for it to complete

---

## Step 2: Add Android App to Firebase Project

1. In Firebase Console, click **"Add app"** and select **Android**
2. Enter the Android package name: `com.hotmail.arehmananis.sketchapp`
3. (Optional) Add app nickname: "SketchApp"
4. (Optional) Add SHA-1 certificate fingerprint (required for Google Sign-In):

   **To get SHA-1 certificate:**
   - Open Terminal in Android Studio
   - Run: `./gradlew signingReport`
   - Copy the SHA-1 from the "debug" variant
   - Paste it in Firebase Console

5. Click **"Register app"**

---

## Step 3: Download google-services.json

1. Download the `google-services.json` file from Firebase Console
2. Move it to your project's `app/` directory:
   ```
   SketchApp/
   └── app/
       ├── build.gradle.kts
       ├── google-services.json  ← Place here
       └── src/
   ```
3. Verify the file is in the correct location (should be at the same level as `build.gradle.kts`)

---

## Step 4: Enable Google Sign-In

1. In Firebase Console, go to **Authentication** → **Sign-in method**
2. Click on **Google** provider
3. Toggle **Enable**
4. Enter support email (your email)
5. Click **Save**

---

## Step 5: Configure Web Client ID for Google Sign-In

The Web Client ID must be stored in `local.properties` for security.

1. In Firebase Console, go to **Project Settings** (gear icon) → **General**
2. Scroll down to **"Your apps"** section
3. Find your Android app and expand it
4. Look for **"Web client ID"** under **OAuth 2.0 Client IDs**
5. Copy the Web Client ID (format: `123456789-xxxxxxxxxxxxx.apps.googleusercontent.com`)

6. Open `local.properties` in your project root
7. Add/update these lines:
   ```properties
   google.dev.webClientId=YOUR_WEB_CLIENT_ID_HERE
   google.qa.webClientId=YOUR_WEB_CLIENT_ID_HERE
   google.stag.webClientId=YOUR_WEB_CLIENT_ID_HERE
   google.prod.webClientId=YOUR_WEB_CLIENT_ID_HERE
   ```
8. Replace `YOUR_WEB_CLIENT_ID_HERE` with your actual Web Client ID

**Note:** All flavors typically use the SAME web client ID from your Firebase project.

For detailed secrets setup, see [SECRETS_SETUP.md](SECRETS_SETUP.md).

---

## Step 6: Create Firestore Database

1. In Firebase Console, go to **Firestore Database** (not Realtime Database)
2. Click **"Create database"**
3. Choose **"Start in test mode"** for now (we'll add security rules later)
4. Select a Cloud Firestore location (choose closest to your users)
5. Click **"Enable"**

### Add Security Rules

Once database is created:

1. Go to **Firestore Database** → **Rules**
2. Replace the default rules with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /sketches/{sketchId} {
      // Users can only read/write their own sketches
      allow read, write: if request.auth != null &&
                            request.auth.uid == resource.data.userId;

      // Users can create sketches for themselves
      allow create: if request.auth != null &&
                       request.auth.uid == request.resource.data.userId;
    }
  }
}
```

3. Click **"Publish"**

---

## Step 7: Create Cloud Storage Bucket

1. In Firebase Console, go to **Storage**
2. Click **"Get started"**
3. Choose **"Start in test mode"**
4. Click **"Next"**
5. Select a Cloud Storage location (same as Firestore)
6. Click **"Done"**

### Add Storage Security Rules

1. Go to **Storage** → **Rules**
2. Replace the default rules with:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /sketch_images/{userId}/{sketchId} {
      // Anyone authenticated can read
      allow read: if request.auth != null;

      // Only the owner can write
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

3. Click **"Publish"**

---

## Step 8: Verify Setup

### Check Files

Ensure these files exist:
- ✅ `app/google-services.json`
- ✅ `app/build.gradle.kts` has correct GOOGLE_CLIENT_ID

### Build the Project

1. In Android Studio, click **"Sync Project with Gradle Files"** (elephant icon with arrow)
2. Wait for sync to complete
3. Build the project: **Build** → **Make Project** (Ctrl+F9 / Cmd+F9)
4. Fix any errors if they appear

---

## Step 9: Test the App

### Run on Device/Emulator

1. Connect Android device or start emulator
2. Click **Run** (green play button)
3. Wait for app to install and launch

### Test Google Sign-In

1. App should show Login screen
2. Tap **"Sign in with Google"**
3. Select your Google account
4. Grant permissions if prompted
5. You should be redirected to Gallery screen

### Test Drawing

1. Tap the **+ button** to create a new sketch
2. Draw on the canvas with different brushes
3. Tap **Save** icon
4. You should return to Gallery
5. Your sketch should appear in the gallery

### Verify Firebase Data

**Check Authentication:**
1. Go to Firebase Console → **Authentication** → **Users**
2. You should see your signed-in user

**Check Firestore:**
1. Go to Firebase Console → **Firestore Database** → **Data**
2. You should see a `sketches` collection
3. Each sketch document contains metadata (title, userId, etc.)

**Check Storage:**
1. Go to Firebase Console → **Storage** → **Files**
2. Navigate to `sketch_images/{userId}/`
3. You should see PNG images of your sketches

---

## Troubleshooting

### Issue: "google-services.json not found"

**Solution:**
- Ensure `google-services.json` is in the `app/` directory
- Sync project with Gradle files
- Clean and rebuild: **Build** → **Clean Project** → **Rebuild Project**

### Issue: Google Sign-In fails with "Developer error"

**Solution:**
- Verify SHA-1 certificate is added to Firebase Console
- Ensure GOOGLE_CLIENT_ID in `build.gradle.kts` matches Firebase Console
- Check that `google-services.json` is up to date

### Issue: "Permission denied" errors in Firestore/Storage

**Solution:**
- Verify security rules are published
- Check that user is authenticated
- Ensure userId in data matches authenticated user's UID

### Issue: Sketches not syncing

**Solution:**
- Check internet connection
- Verify Firestore and Storage are enabled
- Check Logcat for error messages (filter by "SketchRepository" or "SketchSync")

---

## Production Checklist

Before releasing to production:

- [ ] Update Firestore rules from test mode to production rules (see Step 6)
- [ ] Update Storage rules from test mode to production rules (see Step 7)
- [ ] Add SHA-1 for release keystore to Firebase Console
- [ ] Test app with release build
- [ ] Set up Firebase Analytics (optional)
- [ ] Configure Firebase Crashlytics (optional)
- [ ] Review Firebase quota limits for your plan

---

## Additional Resources

- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Firebase Authentication](https://firebase.google.com/docs/auth)
- [Cloud Firestore](https://firebase.google.com/docs/firestore)
- [Cloud Storage](https://firebase.google.com/docs/storage)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)

---

## Support

If you encounter issues:
1. Check Logcat in Android Studio for error messages
2. Verify all Firebase services are enabled in Console
3. Ensure all configuration files are in place
4. Review security rules for typos

**Happy sketching! 🎨**
