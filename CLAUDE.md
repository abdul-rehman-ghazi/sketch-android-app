# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a native Android application built with Kotlin and Jetpack Compose, implementing MVVM architecture with Clean Architecture principles. The app features a bottom navigation interface with three main screens: Home, Profile, and Settings.

**Important:** The architecture is designed to be easily migrable to Compose Multiplatform. Keep platform-specific code isolated and maintain a clean separation between business logic and platform concerns.

**Key Technologies:**

- Jetpack Compose for UI
- Hilt for dependency injection
- Kotlin Coroutines and Flow for asynchronous operations
- Retrofit + Moshi for networking
- DataStore for local preferences
- Material 3 design components

**Package:** `com.hotmail.arehmananis.sketchapp`

## Architecture

The codebase follows Clean Architecture with three distinct layers designed for Compose Multiplatform compatibility:

```text
app/src/main/java/com/hotmail/arehmananis/sketchapp/
├── data/
│   ├── local/              # DataStore implementation (Android-specific)
│   ├── remote/             # Network DTOs, NetworkResult (platform-agnostic)
│   │   ├── dto/
│   │   ├── model/
│   │   └── NetworkResult.kt
│   └── repository/         # Repository implementations
├── domain/
│   ├── model/              # Domain entities (pure Kotlin - KMP ready)
│   ├── repository/         # Repository interfaces (pure Kotlin - KMP ready)
│   └── usecase/            # Business logic use cases (pure Kotlin - KMP ready)
├── presentation/
│   ├── feature/
│   │   ├── home/           # HomeScreen + HomeViewModel
│   │   ├── profile/        # ProfileScreen + ProfileViewModel
│   │   └── settings/       # SettingsScreen + SettingsViewModel
│   ├── common/             # Navigation.kt
│   └── theme/              # Compose theme (Color, Type, Theme)
├── di/                     # Hilt modules (Android-specific - will need KMP alternative)
└── MainActivity.kt         # Android-specific entry point
```

### Layer Rules for KMP Readiness

**Domain Layer (KMP Ready):**

- ✅ Contains pure Kotlin code - NO Android framework dependencies
- ✅ Defines repository interfaces and use cases
- ✅ Domain models represent business entities
- ✅ Can be moved to `commonMain` during KMP migration

**Data Layer (Partially KMP Ready):**

- ✅ Repository interfaces in domain are platform-agnostic
- ✅ Network DTOs and NetworkResult are pure Kotlin
- ⚠️ DataStore implementation is Android-specific (will need expect/actual)
- ⚠️ Repository implementations may need platform-specific variants
- Migration path: Use expect/actual for platform-specific storage

**Presentation Layer (Compose Multiplatform Compatible):**

- ✅ Jetpack Compose UI is compatible with Compose Multiplatform
- ✅ ViewModels use Kotlin StateFlow (KMP compatible)
- ⚠️ Navigation currently uses androidx.navigation (consider decompose or voyager for KMP)
- ⚠️ Hilt is Android-specific (Koin is KMP-compatible alternative)

**DI Layer (Android-Specific):**

- ⚠️ Hilt is Android-only
- Migration path: Koin, Kodein, or manual DI for KMP

### KMP Migration Considerations

When adding new code:

1. **Keep domain layer pure:** No `import android.*` or `import androidx.*` in domain layer
2. **Isolate platform APIs:** Wrap Android-specific APIs (DataStore, Context) behind interfaces
3. **Use Kotlin stdlib:** Prefer Kotlin standard library over platform-specific utilities
4. **Avoid Android types in domain models:** Use String, Long, Boolean instead of Uri, Parcelable, etc.
5. **Design for expect/actual:** Consider which code will need platform-specific implementations

## Build Configuration

### Product Flavors

The project has four product flavors for different environments:

- **dev** - Development environment (`applicationIdSuffix = ".dev"`)
- **qa** - QA environment (`applicationIdSuffix = ".qa"`)
- **stag** - Staging environment (`applicationIdSuffix = ".stag"`)
- **prod** - Production environment (no suffix)

Each flavor defines:

- `BASE_URL` - API base URL
- `ENVIRONMENT` - Environment name
- `app_name` - App display name

### BuildConfig Fields

Available in all flavors:

- `BuildConfig.BASE_URL` - API base URL
- `BuildConfig.ENVIRONMENT` - Environment identifier
- `BuildConfig.ENABLE_LOGGING` - Network logging flag (debug only)

**KMP Note:** BuildConfig is Android-specific. For KMP, use expect/actual or configuration objects.

## Common Commands

```bash
# Build debug APK for dev environment
./gradlew assembleDevDebug

# Build debug APKs for all flavors
./gradlew assembleDebug

# Build release APK for production
./gradlew assembleProdRelease

# Install and run dev debug on connected device
./gradlew installDevDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build artifacts
./gradlew clean

# Generate release APKs for all flavors
./gradlew assembleRelease
```

## Navigation Structure

The app uses Compose Navigation with a bottom navigation bar:

- **Routes:** Defined in `Navigation.kt` as sealed `Screen` objects
- **Screens:** Home, Profile, Settings
- **Navigation Pattern:** Bottom navigation with state preservation
- **Entry Point:** `AppNavigation()` composable in MainActivity

When adding new screens:

1. Add Screen object to `Navigation.kt`
2. Add route to bottomNavItems list if it should appear in bottom nav
3. Add composable destination to NavHost

**KMP Migration Note:** Consider using [Decompose](https://github.com/arkivanov/Decompose) or [Voyager](https://github.com/adrielcafe/voyager) for KMP-compatible navigation.

## Error Handling

The project uses `NetworkResult` sealed interface for network operations (pure Kotlin, KMP compatible):

```kotlin
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T)
    data class Error(val exception: Exception, val message: String?)
    object Loading
}
```

**NetworkException types:**

- `NoInternetException` - Network connectivity issues
- `TimeoutException` - Request timeout
- `ServerException` - HTTP errors with status code
- `UnknownException` - Unexpected errors

**Usage:**

- Use `safeApiCall { }` extension function for network calls
- Convert to domain `Result<T>` with `.toResult()` when crossing layers
- Repositories return `Result<T>` to domain/presentation layers

**KMP Note:** This error handling pattern is already KMP-ready. Retrofit+OkHttp can be replaced with Ktor for full KMP support.

## Dependency Injection (Hilt)

All DI modules are in the `di/` package:

- **AppModule** - Application-level dependencies (Context, DataStore)
- **NetworkModule** - Retrofit, OkHttpClient, Moshi configuration
- **RepositoryModule** - Repository interface → implementation bindings

**Conventions:**

- ViewModels: Annotate with `@HiltViewModel` and use constructor injection
- Activities: Annotate with `@AndroidEntryPoint`
- Use `@Singleton` scope for app-level dependencies
- Bind interfaces with `@Binds` in modules

**KMP Migration Note:** Hilt is Android-only. For KMP, migrate to [Koin](https://insert-koin.io/) which supports multiplatform.

## State Management

ViewModels expose UI state using StateFlow (KMP compatible):

```kotlin
private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
```

**UI State Patterns:**

- Use sealed interfaces for UI state (Loading, Success, Error)
- Collect state in composables with `collectAsStateWithLifecycle()`
- Keep UI logic in composables, business logic in use cases

**KMP Note:** StateFlow is fully KMP compatible. For ViewModels, use androidx.lifecycle.ViewModel (now supports KMP) or create custom base class.

## Theme and Theming

The app supports three theme modes (defined in `ThemeMode` enum):

- `LIGHT` - Always light theme
- `DARK` - Always dark theme
- `SYSTEM` - Follow system theme

Theme preference is stored in DataStore and managed through SettingsViewModel. MainActivity reads the theme preference and applies it to the entire app.

**KMP Note:** Theme preference storage will need expect/actual implementation for different platforms.

## Testing

**Unit Tests:** Located in `app/src/test/`

- Test use cases and ViewModels
- Use fake repositories for testing
- Use `kotlinx-coroutines-test` for testing coroutines (KMP compatible)

**Instrumented Tests:** Located in `app/src/androidTest/`

- Test UI with Compose testing APIs
- Use `@HiltAndroidTest` for tests requiring DI

**KMP Testing:** Domain layer tests can run on all platforms without modification.

## Dependencies Management

The project uses Gradle version catalogs (`gradle/libs.versions.toml`):

**Key Dependencies:**

- Compose BOM: 2024.02.00
- Hilt: 2.50 (Android-only)
- Retrofit: 2.9.0 (Android/JVM - use Ktor for KMP)
- Moshi: 1.15.0 (Android/JVM - use kotlinx.serialization for KMP)
- Navigation Compose: 2.7.7
- DataStore: 1.0.0 (Android-only - multiplatform-settings for KMP)

When adding dependencies, update `libs.versions.toml` first, then reference in `build.gradle.kts` with `libs.dependency.name`.

**KMP-Friendly Alternatives:**

- DI: Koin instead of Hilt
- Networking: Ktor instead of Retrofit+OkHttp
- JSON: kotlinx.serialization instead of Moshi
- Storage: multiplatform-settings or expect/actual wrapper around DataStore
- Navigation: Decompose or Voyager instead of androidx.navigation

## Code Conventions

**Kotlin:**

- Use data classes for models and DTOs
- Prefer `val` over `var`
- Use sealed classes/interfaces for restricted type hierarchies
- Leverage Kotlin's null safety
- **Avoid platform-specific types in shared code**

**Compose:**

- Hoist state to make composables stateless when possible
- Use `remember` and `rememberSaveable` appropriately
- Preview composables with `@Preview` annotations (Android-specific)
- Keep composable functions focused and small
- **Compose code is mostly KMP-ready**

**Repository Pattern:**

- Interfaces in `domain/repository/` (pure Kotlin)
- Implementations in `data/repository/` (may be platform-specific)
- Return `Result<T>` from repository methods
- Handle all data source interactions in repositories
- **Design repositories for expect/actual if they use platform APIs**

**ViewModels:**

- Never pass Android Context to ViewModels
- Use `viewModelScope` for coroutines
- Expose UI state via StateFlow
- Keep ViewModels thin - business logic goes in use cases
- **ViewModels are now KMP-compatible with androidx.lifecycle 2.8.0+**

## Secrets Management

**CRITICAL:** Never commit sensitive credentials to version control!

### Protected Files (gitignored):

- `local.properties` - Contains API keys and OAuth credentials
- `app/google-services.json` - Firebase configuration with project credentials
- `*.keystore`, `*.jks` - Signing keys for release builds

### Configuration:

- All secrets live in `local.properties`
- Template available: `local.properties.template`
- BuildConfig reads secrets at compile time
- CI/CD uses GitHub Secrets (see `.github/workflows/build.yml`)

### For new developers:

See [SECRETS_SETUP.md](SECRETS_SETUP.md) for complete setup instructions.

### Adding new secrets:

1. Add to `local.properties.template` with placeholder
2. Add to `local.properties` with real value (gitignored)
3. Update `app/build.gradle.kts` to read the value
4. Add validation to `validateSecrets()` function
5. Document in `SECRETS_SETUP.md`
6. Add to GitHub Secrets for CI/CD

### KMP Note:

Current secrets management uses Android Gradle BuildConfig. For KMP migration:
- Replace BuildConfig with expect/actual configuration objects
- Use multiplatform-settings for encrypted local storage
- Keep Firebase/Cloudinary credentials in platform-specific implementations

## Multiplatform Migration Path

If/when migrating to Compose Multiplatform:

1. **Phase 1 - Prepare Domain Layer:**
   - Ensure domain layer has zero Android dependencies
   - Move domain layer to `commonMain`
   - Create expect/actual for any remaining platform code

2. **Phase 2 - Refactor Data Layer:**
   - Replace Retrofit with Ktor
   - Replace Moshi with kotlinx.serialization
   - Create expect/actual for DataStore (use multiplatform-settings)
   - Move shared data code to `commonMain`

3. **Phase 3 - Migrate DI:**
   - Replace Hilt with Koin
   - Set up Koin modules in `commonMain`

4. **Phase 4 - Migrate Presentation:**
   - Move ViewModels to `commonMain`
   - Move Compose UI to `commonMain`
   - Replace androidx.navigation with Decompose or Voyager
   - Keep platform-specific UI in `androidMain`, `iosMain`, etc.

5. **Phase 5 - Platform-Specific:**
   - Create platform-specific entry points (MainActivity, etc.)
   - Handle platform-specific permissions and APIs

## Important Notes

- Single-module architecture (not modularized into feature modules)
- 100% Jetpack Compose - no XML layouts
- No Room database currently implemented (for KMP, use SQLDelight)
- Networking is set up but API service interfaces should be defined as needed
- Release builds enable R8 code shrinking and obfuscation
- Java target: JVM 21
- **Architecture prioritizes KMP readiness - maintain this separation**
