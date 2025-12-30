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

**Security Best Practice (NEW):**
- **Developers only need DEV (and optionally QA) secrets locally**
- **Production and Staging secrets should NEVER be stored on developer machines**
- **Prod/Stag credentials are managed exclusively in CI/CD (GitHub Secrets)**
- The build system now validates only the secrets for flavors you're actually building

---

## Required Secrets

**For local development, you only need DEV secrets:**

| Secret | Purpose | Where to Find | Required |
|--------|---------|---------------|----------|
| `google.dev.webClientId` | Google Sign-In OAuth | Firebase Console → Project Settings → OAuth 2.0 Client IDs | ✅ Yes |
| `cloudinary.dev.cloudName` | Cloud storage identifier | Cloudinary Dashboard → Account Details | ✅ Yes |
| `cloudinary.dev.apiKey` | Cloudinary authentication | Cloudinary Dashboard → Account Details | ✅ Yes |
| `cloudinary.dev.apiSecret` | Cloudinary authentication | Cloudinary Dashboard → Account Details | ✅ Yes |

**Optional - Only if you build QA flavor locally:**

| Secret | Required |
|--------|----------|
| `google.qa.webClientId` | Only if building QA |
| `cloudinary.qa.*` | Only if building QA |

**Not needed locally:**
- ❌ `*.stag.*` - Staging secrets (CI/CD only)
- ❌ `*.prod.*` - Production secrets (CI/CD only)

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
# Required for local dev
google.dev.webClientId=YOUR_WEB_CLIENT_ID

# Optional - only if building QA locally
google.qa.webClientId=YOUR_WEB_CLIENT_ID
```

**Note:** All flavors typically use the SAME web client ID from your Firebase project.

**Security:** You do NOT need to add `stag` or `prod` web client IDs locally - these are managed in CI/CD.

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
# Development (required)
cloudinary.dev.cloudName=your-cloud-name
cloudinary.dev.apiKey=123456789012345
cloudinary.dev.apiSecret=abcdefghijklmnopqrstuvwx

# QA (optional - only if building QA locally)
cloudinary.qa.cloudName=your-qa-cloud-name
cloudinary.qa.apiKey=123456789012345
cloudinary.qa.apiSecret=abcdefghijklmnopqrstuvwx
```

**Security:** Do NOT add staging or production Cloudinary credentials locally. These are managed in CI/CD (GitHub Secrets).

---

## Release Signing Setup

Release builds require signing with a keystore to be installable on devices. This section explains how to set up release signing for both local and CI/CD builds.

### Creating a Release Keystore

**For local development or initial setup:**

```bash
# Create a new keystore (one-time setup)
keytool -genkey -v -keystore release.keystore -alias sketchapp-release \
  -keyalg RSA -keysize 2048 -validity 10000

# You'll be prompted for:
# - Keystore password (remember this!)
# - Key password (can be same as keystore password)
# - Your name, organization, etc.
```

**CRITICAL:**
- ⚠️ **Save the keystore file and passwords securely!**
- ⚠️ **If you lose the keystore, you cannot update your app on Play Store**
- ⚠️ **Keystore file is gitignored - never commit it**

### Local Signing Configuration

Add signing credentials to `local.properties`:

```properties
# Release signing (for local release builds)
release.keystore.file=release.keystore
release.keystore.password=YOUR_KEYSTORE_PASSWORD
release.key.alias=sketchapp-release
release.key.password=YOUR_KEY_PASSWORD
```

**Test local release build:**
```bash
./gradlew assembleDevRelease
```

### CI/CD Signing Configuration

For GitHub Actions to build signed release APKs, you need to add the keystore and credentials as GitHub Secrets.

**Step 1: Encode keystore to Base64**

```bash
# On macOS/Linux:
base64 -i release.keystore -o keystore.base64.txt

# On Windows (PowerShell):
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) > keystore.base64.txt
```

**Step 2: Add GitHub Secrets**

Go to GitHub repository → **Settings** → **Secrets and variables** → **Actions**

Add these secrets:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `KEYSTORE_FILE_BASE64` | Contents of `keystore.base64.txt` | Base64-encoded keystore |
| `KEYSTORE_PASSWORD` | Your keystore password | Password for the keystore file |
| `KEY_ALIAS` | `sketchapp-release` | Alias used when creating the key |
| `KEY_PASSWORD` | Your key password | Password for the specific key |

**Step 3: Delete the temporary Base64 file**

```bash
# IMPORTANT: Delete the base64 file after uploading to GitHub Secrets
rm keystore.base64.txt
```

**The workflow is already configured** to use these secrets. It will:
1. Decode the Base64 keystore
2. Save it temporarily during the build
3. Use it to sign the release APK
4. Delete it after the build completes

### Getting SHA-1 from Release Keystore

For production Google Sign-In, you need the SHA-1 from your **release** keystore:

```bash
# Get SHA-1 from release keystore
keytool -list -v -keystore release.keystore -alias sketchapp-release

# Look for:
# Certificate fingerprints:
#      SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
```

**Add this SHA-1 to Firebase:**
1. Firebase Console → **Project Settings** → **Your apps**
2. Select the app package
3. Click **"Add fingerprint"**
4. Paste the release SHA-1
5. Click **"Save"**
6. Download updated `google-services.json`

### Security Best Practices for Keystores

**DO:**
- ✅ Store keystore in a secure location (password manager, encrypted drive)
- ✅ Make encrypted backups of the keystore
- ✅ Use strong passwords (min 12 characters)
- ✅ Keep keystore password separate from source code
- ✅ Document the keystore location for team leads
- ✅ Use GitHub Secrets for CI/CD signing

**DON'T:**
- ❌ Never commit keystore files (`.keystore`, `.jks`) to Git
- ❌ Never share keystore via email or chat
- ❌ Never use the debug keystore for production
- ❌ Never store keystore passwords in plain text
- ❌ Never give everyone access to production keystore

**If keystore is lost:**
- You cannot update existing app on Play Store
- You must create new app listing with new package name
- All existing users must uninstall and reinstall

**If keystore is compromised:**
- Immediately remove from Play Store if possible
- Create new keystore and publish as new app
- Notify users of security issue

---

## Troubleshooting

### Build Error: "MISSING SECRETS FOR FLAVOR"

**Cause:** `local.properties` doesn't exist or is missing required values for the flavor you're building.

**Solution:**
1. Ensure file is named `local.properties` (NOT `local.properties.template`)
2. Check all placeholder values (`<YOUR_...>`) are replaced
3. Verify no typos in property names
4. Run `./gradlew clean && ./gradlew assembleDevDebug`

**Note:** The build system now only validates secrets for the flavor you're actually building. If you're building `dev`, you only need dev secrets in `local.properties`. You do NOT need to configure stag or prod secrets locally.

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

**Current workflow (Dev only):**

The project's GitHub Actions workflow (`.github/workflows/build.yml`) currently builds only the `dev` flavor. It uses:
- Real secrets for dev environment
- Dummy values for other flavors (to satisfy Gradle validation)

**Example workflow for building dev:**

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
          java-version: '17'
          distribution: 'temurin'

      - name: Create local.properties
        run: |
          cat > local.properties << EOF
          sdk.dir=$ANDROID_SDK_ROOT

          # Real dev secrets
          google.dev.webClientId=${{ secrets.GOOGLE_WEB_CLIENT_ID }}
          cloudinary.dev.cloudName=${{ secrets.CLOUDINARY_DEV_CLOUD_NAME }}
          cloudinary.dev.apiKey=${{ secrets.CLOUDINARY_DEV_API_KEY }}
          cloudinary.dev.apiSecret=${{ secrets.CLOUDINARY_DEV_API_SECRET }}

          # Dummy values for other flavors (not building these)
          google.qa.webClientId=dummy
          cloudinary.qa.cloudName=dummy
          cloudinary.qa.apiKey=dummy
          cloudinary.qa.apiSecret=dummy
          # ... same for stag/prod
          EOF

      - name: Create google-services.json
        run: |
          echo '${{ secrets.GOOGLE_SERVICES_JSON }}' > app/google-services.json

      - name: Build
        run: ./gradlew assembleDevDebug
```

**Required GitHub Secrets (for dev builds):**
- `GOOGLE_WEB_CLIENT_ID`
- `GOOGLE_SERVICES_JSON` (entire file content as JSON string)
- `CLOUDINARY_DEV_CLOUD_NAME`, `CLOUDINARY_DEV_API_KEY`, `CLOUDINARY_DEV_API_SECRET`

**For production builds:** Add separate GitHub Secrets for prod environment and update the workflow. Never use production secrets in dev builds.

**To add secrets:**
1. Go to GitHub repository → **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Add each secret

---

## Security Best Practices

### DO:
- ✅ Keep `local.properties` and `google-services.json` out of version control
- ✅ Use separate credentials for each environment
- ✅ **Only store DEV (and optionally QA) secrets on developer machines**
- ✅ **Keep production/staging secrets exclusively in CI/CD**
- ✅ Rotate credentials if they're accidentally committed
- ✅ Use environment variables in CI/CD
- ✅ Limit access to production credentials to authorized personnel only

### DON'T:
- ❌ Never commit `local.properties` or `google-services.json`
- ❌ Never hardcode credentials in source code
- ❌ Never share credentials via Slack, email, or unencrypted channels
- ❌ **Never store production or staging credentials on your local machine**
- ❌ Never use production credentials for development/testing
- ❌ Never commit `.env`, `*.keystore`, `*.jks` files

### Environment-Specific Security (NEW):

**Development machines should have:**
- ✅ Dev credentials (required)
- ✅ QA credentials (optional, if you test QA builds)
- ❌ NO staging credentials
- ❌ NO production credentials

**CI/CD should have:**
- All environment credentials stored as GitHub Secrets
- Separate workflows for each environment
- Production builds should require approval/protection rules

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
- [ ] Request **DEV** Firebase credentials from team lead (NOT production!)
- [ ] Download `google-services.json` from Firebase Console
- [ ] Request **DEV** Cloudinary credentials (or create separate dev account)
- [ ] Fill in **only DEV values** in `local.properties` (QA optional)
- [ ] Run `./gradlew signingReport` to get SHA-1
- [ ] Add SHA-1 to Firebase Console (or ask team lead)
- [ ] Download updated `google-services.json`
- [ ] Test build: `./gradlew assembleDevDebug`
- [ ] Test Google Sign-In on emulator/device
- [ ] Verify you can create and save a sketch

**Important:** You should only have DEV credentials on your machine. If someone asks you to configure production or staging secrets locally, verify with the team lead first.

**Welcome to the team!**
