# SketchApp

A native Android drawing application built with Jetpack Compose, featuring cloud sync, Google authentication, and multi-environment support.

## Features

- 🎨 Drawing canvas with multiple brush types and colors
- ☁️ Cloud storage integration (Firebase Storage + Cloudinary)
- 🔐 Google Sign-In authentication
- 📱 Material 3 design with light/dark theme
- 🔄 Automatic sketch synchronization
- 🏗️ Multi-environment support (dev/qa/stag/prod)

## Tech Stack

- **UI:** Jetpack Compose with Material 3
- **Architecture:** MVVM + Clean Architecture (KMP-ready)
- **DI:** Koin
- **Networking:** Ktor
- **Database:** Room
- **Auth:** Firebase Authentication
- **Cloud Storage:** Firebase Storage + Cloudinary
- **Language:** Kotlin

## Project Setup

### Prerequisites

- Android Studio Hedgehog or later
- JDK 21
- Android SDK 34
- Firebase account
- Cloudinary account

### First-Time Setup

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd SketchApp
   ```

2. **Configure secrets:**
   ```bash
   # Copy template files
   cp local.properties.template local.properties
   cp app/google-services.template.json app/google-services.json

   # Fill in your credentials (see SECRETS_SETUP.md)
   # Edit local.properties and google-services.json
   ```

3. **Set up Firebase:**
   - Follow [FIREBASE_SETUP.md](FIREBASE_SETUP.md) for detailed instructions
   - Download your `google-services.json` from Firebase Console
   - Get your Web Client ID and add to `local.properties`

4. **Set up Cloudinary:**
   - Create account at [cloudinary.com](https://cloudinary.com)
   - Get Cloud Name, API Key, and API Secret
   - Add to `local.properties` (see [SECRETS_SETUP.md](SECRETS_SETUP.md))

5. **Build the project:**
   ```bash
   ./gradlew assembleDevDebug
   ```

6. **Run on device/emulator:**
   - Click Run in Android Studio
   - Or: `./gradlew installDevDebug`

**Detailed setup instructions:** See [SECRETS_SETUP.md](SECRETS_SETUP.md)

## Build Variants

The project has 4 product flavors for different environments:

| Flavor | Package Suffix | Base URL | Use Case |
|--------|----------------|----------|----------|
| **dev** | `.dev` | `api-dev.sketchapp.com` | Daily development |
| **qa** | `.qa` | `api-qa.sketchapp.com` | QA testing |
| **stag** | `.stag` | `api-stag.sketchapp.com` | Staging/pre-production |
| **prod** | *(none)* | `api.sketchapp.com` | Production release |

### Building Different Flavors

```bash
# Debug builds
./gradlew assembleDevDebug
./gradlew assembleQaDebug
./gradlew assembleStagDebug
./gradlew assembleProdDebug

# Release builds
./gradlew assembleDevRelease
./gradlew assembleProdRelease
```

## Project Structure

```
app/src/main/java/com/hotmail/arehmananis/sketchapp/
├── data/
│   ├── local/              # Room database, DataStore
│   ├── remote/             # Firebase, Cloudinary, Ktor
│   └── repository/         # Repository implementations
├── domain/
│   ├── model/              # Domain entities (KMP-ready)
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Business logic use cases
├── presentation/
│   ├── feature/            # Screens and ViewModels
│   │   ├── auth/           # Login screen
│   │   ├── gallery/        # Sketch gallery
│   │   └── drawing/        # Drawing canvas
│   ├── common/             # Navigation
│   └── theme/              # Compose theme
└── di/                     # Koin modules
```

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Specific flavor
./gradlew testDevDebugUnitTest
```

## Security

**⚠️ NEVER commit these files:**
- `local.properties` - Contains API keys and secrets
- `app/google-services.json` - Firebase credentials
- `*.keystore` / `*.jks` - Signing keys

**If you accidentally commit secrets:**
1. Immediately rotate all compromised credentials
2. Follow [SECRETS_SETUP.md § Security Best Practices](SECRETS_SETUP.md#security-best-practices)
3. Clean git history with `git filter-repo`

## Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Make your changes
3. Ensure tests pass: `./gradlew test`
4. Build all flavors: `./gradlew assembleDebug`
5. Commit: `git commit -m "feat: your feature"`
6. Push: `git push origin feature/your-feature`
7. Create a Pull Request

## Documentation

- [SECRETS_SETUP.md](SECRETS_SETUP.md) - Secrets and credentials configuration
- [FIREBASE_SETUP.md](FIREBASE_SETUP.md) - Firebase setup guide
- [CLAUDE.md](CLAUDE.md) - Development guidelines for Claude Code
- [Firebase Documentation](https://firebase.google.com/docs)
- [Cloudinary Documentation](https://cloudinary.com/documentation)

## License

[Your License Here]

---

**Need help?** See [SECRETS_SETUP.md](SECRETS_SETUP.md) or ask the team lead.
