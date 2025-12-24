# Secrets Setup Guide

This guide explains how to configure local secrets and credentials for the SketchApp project.

## Quick Start

```bash
# 1. Copy template to local.properties
cp local.properties.template local.properties

# 2. Copy google-services template
cp app/google-services.template.json app/google-services.json

# 3. Fill in your credentials (see sections below)

# 4. Verify setup
./gradlew assembleDevDebug
```

## Table of Contents

- [Why We Use local.properties](#why-we-use-localproperties)
- [Required Secrets](#required-secrets)
- [Firebase Setup](#firebase-setup)
- [Cloudinary Setup](#cloudinary-setup)
- [Troubleshooting](#troubleshooting)
- [CI/CD Configuration](#cicd-configuration)
- [Security Best Practices](#security-best-practices)

---

## Why We Use local.properties

**Security:** Sensitive credentials (API keys, OAuth client IDs) should NEVER be committed to version control.

**Architecture:** The project uses 4 product flavors (dev, qa, stag, prod), each requiring separate credentials:
- Google Web Client IDs for OAuth
- Cloudinary cloud storage credentials

**Solution:** All secrets live in `local.properties` which is gitignored.

---

## Required Secrets

Each environment (flavor) requires:

| Secret | Purpose | Where to Find |
|--------|---------|---------------|
| `google.{flavor}.webClientId` | Google Sign-In OAuth | Firebase Console → Project Settings → OAuth 2.0 Client IDs |
| `cloudinary.{flavor}.cloudName` | Cloud storage identifier | Cloudinary Dashboard → Account Details |
| `cloudinary.{flavor}.apiKey` | Cloudinary authentication | Cloudinary Dashboard → Account Details |
| `cloudinary.{flavor}.apiSecret` | Cloudinary authentication | Cloudinary Dashboard → Account Details |

---

## Firebase Setup

### Step 1: Get your Firebase configuration

If you already have a Firebase project:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **sketchapp-b00b5** (or your project)
3. Click gear icon → **Project Settings**
4. Click **Download google-services.json**
5. Save to `app/google-services.json`

If you need to create a new Firebase project, see [FIREBASE_SETUP.md](FIREBASE_SETUP.md).

### Step 2: Get Web Client ID

1. In Firebase Console → **Project Settings** → **General** tab
2. Scroll to **"Your apps"** section
3. You'll see OAuth 2.0 Client IDs including a **Web client ID**
4. Copy the Web Client ID (format: `123456-xxxxx.apps.googleusercontent.com`)
5. Paste into `local.properties`:

```properties
google.dev.webClientId=YOUR_WEB_CLIENT_ID
google.qa.webClientId=YOUR_WEB_CLIENT_ID
google.stag.webClientId=YOUR_WEB_CLIENT_ID
google.prod.webClientId=YOUR_WEB_CLIENT_ID
```

**Note:** All flavors typically use the SAME web client ID from your Firebase project.

### Step 3: Configure SHA-1 Certificate (Required for Google Sign-In)

Google Sign-In requires your app's SHA-1 fingerprint to be registered in Firebase.

**Get SHA-1 for debug keystore:**

```bash
./gradlew signingReport
```

Look for output like:
```
Variant: devDebug
SHA1: 05:F5:F2:45:A6:B9:21:9A:98:CC:79:78:FC:94:72:FD:EF:17:03:FF
```

**Add to Firebase:**

1. Firebase Console → **Project Settings**
2. Scroll to **"Your apps"**
3. Select each package name (prod, dev, qa, stag)
4. Click **"Add fingerprint"**
5. Paste SHA-1
6. Click **"Save"**
7. **Download updated google-services.json** (it will be updated with new OAuth client IDs)

**For release builds:** You must also add the SHA-1 from your release keystore.

---

## Cloudinary Setup

Cloudinary provides cloud storage for sketch images.

### Step 1: Create Cloudinary Account(s)

**Option A: Single Account with Folders** (simpler for small teams)
1. Create one Cloudinary account at [cloudinary.com](https://cloudinary.com)
2. Use folder prefixes in your upload code: `dev/`, `qa/`, `stag/`, `prod/`
3. Use same credentials for all flavors

**Option B: Separate Accounts** (recommended for production)
1. Create separate Cloudinary accounts for each environment
2. Better isolation and quota management
3. Easier to rotate credentials per environment

### Step 2: Get Credentials

1. Go to [Cloudinary Console](https://cloudinary.com/console)
2. On Dashboard, find:
   - **Cloud Name**
   - **API Key**
   - **API Secret** (click "Reveal" to see it)

3. Add to `local.properties`:

```properties
# Development
cloudinary.dev.cloudName=your-cloud-name
cloudinary.dev.apiKey=123456789012345
cloudinary.dev.apiSecret=abcdefghijklmnopqrstuvwx

# Repeat for qa, stag, prod...
```

---

## Troubleshooting

### Build Error: "MISSING SECRETS FOR FLAVOR"

**Cause:** `local.properties` doesn't exist or is missing required values.

**Solution:**
1. Ensure file is named `local.properties` (NOT `local.properties.template`)
2. Check all placeholder values (`<YOUR_...>`) are replaced
3. Verify no typos in property names
4. Run `./gradlew clean && ./gradlew assembleDevDebug`

### Google Sign-In Error: "Developer error" or "Sign-in failed"

**Cause:** SHA-1 certificate not registered or wrong Web Client ID.

**Solutions:**
1. Verify SHA-1 is added to Firebase (see Firebase Setup Step 3)
2. Download updated `google-services.json` after adding SHA-1
3. Verify `google.dev.webClientId` in `local.properties` matches Firebase Console
4. Clean and rebuild: `./gradlew clean && ./gradlew assembleDevDebug`

### File Not Found: "google-services.json"

**Cause:** File not in correct location.

**Solution:**
1. Ensure file is at `app/google-services.json`
2. NOT in `app/src/` - it should be at same level as `app/build.gradle.kts`
3. Download from Firebase Console if missing

### Values Not Updating After Changing local.properties

**Solution:**
```bash
./gradlew clean
./gradlew assembleDevDebug
```

Or in Android Studio: **Build** → **Clean Project** → **Rebuild Project**

---

## CI/CD Configuration

### GitHub Actions

To build in CI/CD, secrets must be provided as environment variables or GitHub Secrets.

**Example workflow:**

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Create local.properties
        run: |
          cat > local.properties << EOF
          sdk.dir=$ANDROID_SDK_ROOT
          google.dev.webClientId=${{ secrets.GOOGLE_WEB_CLIENT_ID }}
          google.qa.webClientId=${{ secrets.GOOGLE_WEB_CLIENT_ID }}
          google.stag.webClientId=${{ secrets.GOOGLE_WEB_CLIENT_ID }}
          google.prod.webClientId=${{ secrets.GOOGLE_WEB_CLIENT_ID }}
          cloudinary.dev.cloudName=${{ secrets.CLOUDINARY_DEV_CLOUD_NAME }}
          cloudinary.dev.apiKey=${{ secrets.CLOUDINARY_DEV_API_KEY }}
          cloudinary.dev.apiSecret=${{ secrets.CLOUDINARY_DEV_API_SECRET }}
          # ... repeat for other flavors
          EOF

      - name: Create google-services.json
        run: |
          echo '${{ secrets.GOOGLE_SERVICES_JSON }}' > app/google-services.json

      - name: Build
        run: ./gradlew assembleDevDebug
```

**Required GitHub Secrets:**
- `GOOGLE_WEB_CLIENT_ID`
- `GOOGLE_SERVICES_JSON` (entire file content as JSON string)
- `CLOUDINARY_DEV_CLOUD_NAME`, `CLOUDINARY_DEV_API_KEY`, `CLOUDINARY_DEV_API_SECRET`
- Repeat for qa, stag, prod

**To add secrets:**
1. Go to GitHub repository → **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Add each secret

---

## Security Best Practices

### DO:
- ✅ Keep `local.properties` and `google-services.json` out of version control
- ✅ Use separate credentials for each environment
- ✅ Rotate credentials if they're accidentally committed
- ✅ Use environment variables in CI/CD
- ✅ Limit access to production credentials to authorized personnel

### DON'T:
- ❌ Never commit `local.properties` or `google-services.json`
- ❌ Never hardcode credentials in source code
- ❌ Never share credentials via Slack, email, or unencrypted channels
- ❌ Never use production credentials for development/testing
- ❌ Never commit `.env`, `*.keystore`, `*.jks` files

### If Credentials Are Compromised:

1. **Firebase (google-services.json, Web Client ID):**
   - Regenerate OAuth client ID in Google Cloud Console
   - Update Firebase project settings
   - Download new `google-services.json`
   - Update `local.properties` with new Web Client ID
   - Notify team to update their local files

2. **Cloudinary:**
   - Go to Cloudinary Console → Settings → Security
   - Click **"Reset API Secret"**
   - Update `local.properties` with new secret
   - Notify team

3. **Git History Cleanup (if secrets were committed):**
   ```bash
   # WARNING: Rewrites git history - coordinate with team
   git filter-repo --path app/google-services.json --invert-paths
   git filter-repo --path local.properties --invert-paths

   # Force push (dangerous!)
   git push origin --force --all
   ```

---

## Getting Help

If you're stuck:

1. Check this guide's [Troubleshooting](#troubleshooting) section
2. Review [FIREBASE_SETUP.md](FIREBASE_SETUP.md) for detailed Firebase instructions
3. Verify `.gitignore` includes `local.properties` and `google-services.json`
4. Ask team lead for credentials if you don't have access
5. Check build logs: `./gradlew assembleDevDebug --stacktrace`

---

## Team Onboarding Checklist

For new developers joining the project:

- [ ] Clone repository
- [ ] Copy `local.properties.template` to `local.properties`
- [ ] Request Firebase credentials from team lead
- [ ] Download `google-services.json` from Firebase Console
- [ ] Request Cloudinary credentials (or create separate dev account)
- [ ] Fill in all values in `local.properties`
- [ ] Run `./gradlew signingReport` to get SHA-1
- [ ] Add SHA-1 to Firebase Console (or ask team lead)
- [ ] Download updated `google-services.json`
- [ ] Test build: `./gradlew assembleDevDebug`
- [ ] Test Google Sign-In on emulator/device
- [ ] Verify you can create and save a sketch

**Welcome to the team!**
