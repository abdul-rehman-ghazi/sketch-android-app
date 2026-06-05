# Graph Report - .  (2026-06-05)

## Corpus Check
- Corpus is ~39,341 words - fits in a single context window. You may not need a graph.

## Summary
- 904 nodes · 1280 edges · 86 communities (51 shown, 35 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 97 edges (avg confidence: 0.81)
- Token cost: 3,200 input · 2,100 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Drawing Canvas & Image Elements|Drawing Canvas & Image Elements]]
- [[_COMMUNITY_Architecture Docs & CLAUDE|Architecture Docs & CLAUDE.md]]
- [[_COMMUNITY_Auth Repository Data Layer|Auth Repository Data Layer]]
- [[_COMMUNITY_Sketch Repository & Drawing Data|Sketch Repository & Drawing Data]]
- [[_COMMUNITY_Sketch Management Use Cases|Sketch Management Use Cases]]
- [[_COMMUNITY_ViewModel State & Auth|ViewModel State & Auth]]
- [[_COMMUNITY_Auth Domain Interfaces|Auth Domain Interfaces]]
- [[_COMMUNITY_Bitmap Export Rendering|Bitmap Export Rendering]]
- [[_COMMUNITY_Image Crop Feature|Image Crop Feature]]
- [[_COMMUNITY_Drawing & Preferences Repository|Drawing & Preferences Repository]]
- [[_COMMUNITY_User Preferences Layer|User Preferences Layer]]
- [[_COMMUNITY_Profile & Sign-Out Flow|Profile & Sign-Out Flow]]
- [[_COMMUNITY_App Entry & Theming|App Entry & Theming]]
- [[_COMMUNITY_Gallery UI Components|Gallery UI Components]]
- [[_COMMUNITY_Navigation & Auth Flow|Navigation & Auth Flow]]
- [[_COMMUNITY_Sketch Domain Interface|Sketch Domain Interface]]
- [[_COMMUNITY_Login Screen & Google Auth|Login Screen & Google Auth]]
- [[_COMMUNITY_Cloudinary Upload Impl|Cloudinary Upload Impl]]
- [[_COMMUNITY_Drawing Canvas Touch|Drawing Canvas Touch]]
- [[_COMMUNITY_Google Sign-In Use Case|Google Sign-In Use Case]]
- [[_COMMUNITY_Drawing Toolbar|Drawing Toolbar]]
- [[_COMMUNITY_Drawing Persistence|Drawing Persistence]]
- [[_COMMUNITY_Network Result Handling|Network Result Handling]]
- [[_COMMUNITY_Cloudinary Data Interface|Cloudinary Data Interface]]
- [[_COMMUNITY_Image Crop Screen UI|Image Crop Screen UI]]
- [[_COMMUNITY_Settings Screen|Settings Screen]]
- [[_COMMUNITY_Build Configuration|Build Configuration]]
- [[_COMMUNITY_Sketch DAO Operations|Sketch DAO Operations]]
- [[_COMMUNITY_Local Database Layer|Local Database Layer]]
- [[_COMMUNITY_Cloudinary Config|Cloudinary Config]]
- [[_COMMUNITY_Project Technology Stack|Project Technology Stack]]
- [[_COMMUNITY_Sketch By ID Use Case|Sketch By ID Use Case]]
- [[_COMMUNITY_Emoji Serialization|Emoji Serialization]]
- [[_COMMUNITY_Image Serialization|Image Serialization]]
- [[_COMMUNITY_Path Serialization|Path Serialization]]
- [[_COMMUNITY_Sketch Sync Use Case|Sketch Sync Use Case]]
- [[_COMMUNITY_VSCode Debug Config|VSCode Debug Config]]
- [[_COMMUNITY_Firebase Sketch DTO|Firebase Sketch DTO]]
- [[_COMMUNITY_Network Module DI|Network Module DI]]
- [[_COMMUNITY_Room Database|Room Database]]
- [[_COMMUNITY_App Launcher Icon (hdpi)|App Launcher Icon (hdpi)]]
- [[_COMMUNITY_Claude Code Hooks|Claude Code Hooks]]
- [[_COMMUNITY_Claude Code Permissions|Claude Code Permissions]]
- [[_COMMUNITY_Brush Icon Mapping|Brush Icon Mapping]]
- [[_COMMUNITY_API Response Models|API Response Models]]
- [[_COMMUNITY_Build & CI Setup|Build & CI Setup]]
- [[_COMMUNITY_Android Instrumented Tests|Android Instrumented Tests]]
- [[_COMMUNITY_Shape Tool Domain Model|Shape Tool Domain Model]]
- [[_COMMUNITY_Gradle Wrapper & Java Upgrade|Gradle Wrapper & Java Upgrade]]
- [[_COMMUNITY_Claude Settings JSON|Claude Settings JSON]]
- [[_COMMUNITY_Drawing Color Palette|Drawing Color Palette]]
- [[_COMMUNITY_VSCode Settings|VSCode Settings]]
- [[_COMMUNITY_Network DI Module|Network DI Module]]
- [[_COMMUNITY_Round Icon (hdpi)|Round Icon (hdpi)]]
- [[_COMMUNITY_Round Icon Density (hdpi)|Round Icon Density (hdpi)]]
- [[_COMMUNITY_Round Icon Visual (hdpi)|Round Icon Visual (hdpi)]]
- [[_COMMUNITY_Launcher Icon (mdpi)|Launcher Icon (mdpi)]]
- [[_COMMUNITY_Round Icon (mdpi)|Round Icon (mdpi)]]
- [[_COMMUNITY_Icon Visual (mdpi)|Icon Visual (mdpi)]]
- [[_COMMUNITY_Launcher Icon (xhdpi)|Launcher Icon (xhdpi)]]
- [[_COMMUNITY_Icon Color Palette (xhdpi)|Icon Color Palette (xhdpi)]]
- [[_COMMUNITY_Round Icon (xhdpi)|Round Icon (xhdpi)]]
- [[_COMMUNITY_Icon Visual (xhdpi)|Icon Visual (xhdpi)]]
- [[_COMMUNITY_Launcher Icon (xxhdpi)|Launcher Icon (xxhdpi)]]
- [[_COMMUNITY_Round Icon (xxhdpi)|Round Icon (xxhdpi)]]
- [[_COMMUNITY_Icon Visual (xxhdpi)|Icon Visual (xxhdpi)]]
- [[_COMMUNITY_Launcher Icon (xxxhdpi)|Launcher Icon (xxxhdpi)]]
- [[_COMMUNITY_Icon Density (xxxhdpi)|Icon Density (xxxhdpi)]]
- [[_COMMUNITY_Round Icon (xxxhdpi)|Round Icon (xxxhdpi)]]
- [[_COMMUNITY_Round Density (xxxhdpi)|Round Density (xxxhdpi)]]
- [[_COMMUNITY_Round Visual (xxxhdpi)|Round Visual (xxxhdpi)]]
- [[_COMMUNITY_Icon Visual (xxxhdpi)|Icon Visual (xxxhdpi)]]
- [[_COMMUNITY_Get User Sketches Use Case|Get User Sketches Use Case]]
- [[_COMMUNITY_Sync Sketches Use Case|Sync Sketches Use Case]]
- [[_COMMUNITY_Update Sketch Use Case|Update Sketch Use Case]]
- [[_COMMUNITY_Instrumented Test Example|Instrumented Test Example]]
- [[_COMMUNITY_Kotlin Gradle Settings|Kotlin Gradle Settings]]

## God Nodes (most connected - your core abstractions)
1. `DrawingViewModel` - 37 edges
2. `DrawingCanvas()` - 24 edges
3. `SketchRepositoryImpl` - 22 edges
4. `GalleryViewModel` - 18 edges
5. `SketchRepository` - 15 edges
6. `DrawingScreen()` - 15 edges
7. `Color` - 13 edges
8. `drawPath()` - 13 edges
9. `DrawingPanel()` - 13 edges
10. `AppNavigation()` - 12 edges

## Surprising Connections (you probably didn't know these)
- `cloudinaryModule` --configures_provider--> `CloudinaryException`  [INFERRED]
  /Users/abdul.rehman/Development/projects/SketchApp/app/src/main/java/com/hotmail/arehmananis/sketchapp/di/CloudinaryModule.kt → app/src/main/java/com/hotmail/arehmananis/sketchapp/data/remote/cloudinary/CloudinaryExceptions.kt
- `firebaseModule` --provides_dependency_for--> `SketchRepositoryImpl`  [INFERRED]
  /Users/abdul.rehman/Development/projects/SketchApp/app/src/main/java/com/hotmail/arehmananis/sketchapp/di/FirebaseModule.kt → app/src/main/java/com/hotmail/arehmananis/sketchapp/data/repository/SketchRepositoryImpl.kt
- `firebaseModule` --provides_dependency_for--> `AuthRepositoryImpl`  [INFERRED]
  /Users/abdul.rehman/Development/projects/SketchApp/app/src/main/java/com/hotmail/arehmananis/sketchapp/di/FirebaseModule.kt → app/src/main/java/com/hotmail/arehmananis/sketchapp/data/repository/AuthRepositoryImpl.kt
- `appModule` --provides--> `PreferencesRepositoryImpl`  [INFERRED]
  /Users/abdul.rehman/Development/projects/SketchApp/app/src/main/java/com/hotmail/arehmananis/sketchapp/di/AppModule.kt → app/src/main/java/com/hotmail/arehmananis/sketchapp/data/repository/PreferencesRepositoryImpl.kt
- `cloudinaryModule` --provides_dependency_for--> `SketchRepositoryImpl`  [INFERRED]
  /Users/abdul.rehman/Development/projects/SketchApp/app/src/main/java/com/hotmail/arehmananis/sketchapp/di/CloudinaryModule.kt → app/src/main/java/com/hotmail/arehmananis/sketchapp/data/repository/SketchRepositoryImpl.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Multi-Flavor Secrets and Firebase Config** — app_build_gradle, app_google_services_template, concept_secrets_management, concept_product_flavors [EXTRACTED 0.95]
- **Koin DI Initialization at App Startup** — sketchapp_sketchapplication, app_build_gradle, sketchapp_mainactivity [INFERRED 0.85]
- **Sketch Local Persistence Pattern (Entity + DAO + Serializers)** — entity_sketchentity, dao_sketchdao, serializer_pathserializer, serializer_emojiserializer, serializer_imageserializer [EXTRACTED 0.95]
- **Sketch Sync Pipeline** — repository_sketchrepositoryimpl_sketchrepositoryimpl, sync_sketchsyncworker_sketchsyncworker, repository_syncschedulerrepositoryimpl_syncschedulerrepositoryimpl [INFERRED 0.85]
- **Koin DI Module Set** — di_appmodule_appmodule, di_cloudinarymodule_cloudinarymodule, di_databasemodule_databasemodule, di_firebasemodule_firebasemodule, di_networkmodule_networkmodule, di_repositorymodule_repositorymodule [INFERRED 0.85]
- **Canvas Domain Models** — model_drawingpath_drawingpath, model_emojielement_emojielement, model_imageelement_imageelement, model_brushtype_brushtype [INFERRED 0.85]
- **Stroke Preview Bubble UI Pattern** —  [EXTRACTED 1.00]
- **Image Crop Pipeline** —  [EXTRACTED 1.00]
- **Crop Rect State Management** —  [INFERRED 0.95]
- **Clean Architecture Layers** — sketchapp_claude_md_domain_layer, sketchapp_claude_md_data_layer, sketchapp_claude_md_presentation_layer, sketchapp_claude_md_di_layer [INFERRED]
- **KMP-Ready Technology Stack** — sketchapp_claude_md_koin, sketchapp_claude_md_stateflow, sketchapp_claude_md_networkresult [INFERRED]
- **Drawing Feature Components** — sketchapp_claude_md_drawingscreen, sketchapp_claude_md_drawingviewmodel, sketchapp_claude_md_drawingcanvas, sketchapp_claude_md_bitmapexporter [INFERRED]

## Communities (86 total, 35 thin omitted)

### Community 0 - "Drawing Canvas & Image Elements"
Cohesion: 0.06
Nodes (30): ImageElement, Int, List, Modifier, String, Unit, Boolean, BrushType (+22 more)

### Community 1 - "Architecture Docs & CLAUDE.md"
Cohesion: 0.07
Nodes (37): AppModule.kt, BitmapExporter.kt, Clean Architecture, Cloudinary Image Storage, CloudinaryModule.kt, Data Layer, DataStore Local Preferences, DI Layer (Koin) (+29 more)

### Community 2 - "Auth Repository Data Layer"
Cohesion: 0.06
Nodes (25): User, AuthUser, Flow, Result, String, Unit, AuthRepository, Context (+17 more)

### Community 3 - "Sketch Repository & Drawing Data"
Cohesion: 0.11
Nodes (20): DrawingPath, EmojiElement, Flow, List, Result, Sketch, SketchEntity, String (+12 more)

### Community 4 - "Sketch Management Use Cases"
Cohesion: 0.07
Nodes (22): Result, String, Unit, Flow, List, Sketch, String, Result (+14 more)

### Community 5 - "ViewModel State & Auth"
Cohesion: 0.07
Nodes (27): AuthUser, StateFlow, Boolean, Context, Float, Int, StateFlow, String (+19 more)

### Community 6 - "Auth Domain Interfaces"
Cohesion: 0.07
Nodes (22): AuthUser, Flow, Result, String, Unit, Flow, Result, Unit (+14 more)

### Community 7 - "Bitmap Export Rendering"
Cohesion: 0.07
Nodes (28): android, Bitmap, Boolean, DrawingPath, EmojiElement, ImageElement, Int, List (+20 more)

### Community 8 - "Image Crop Feature"
Cohesion: 0.07
Nodes (34): CropHandle Enum, CropRect Data Class, CropResult Data Class, DrawingScreen.kt (modified for crop), ImageCropScreen.kt, ImageCropViewModel.kt, ImageCropScreenTest, ImageCropViewModelTest (+26 more)

### Community 9 - "Drawing & Preferences Repository"
Cohesion: 0.08
Nodes (21): Bitmap, Boolean, Result, String, Flow, UserPreferences, Flow, Result (+13 more)

### Community 10 - "User Preferences Layer"
Cohesion: 0.08
Nodes (18): Flow, UserPreferences, Flow, UserPreferences, UserPreferences, Boolean, SettingsUiState, StateFlow (+10 more)

### Community 11 - "Profile & Sign-Out Flow"
Cohesion: 0.08
Nodes (23): AuthUser, Flow, Result, Unit, AuthUser, Modifier, ProfileUiState, String (+15 more)

### Community 12 - "App Entry & Theming"
Cohesion: 0.08
Nodes (20): Flow, UserPreferences, Boolean, SettingsViewModel, Boolean, Application, AuthViewModel, Bundle (+12 more)

### Community 13 - "Gallery UI Components"
Cohesion: 0.12
Nodes (25): Boolean, Modifier, String, Int, Boolean, String, List, Modifier (+17 more)

### Community 14 - "Navigation & Auth Flow"
Cohesion: 0.11
Nodes (20): AuthUser, String, Result, String, String, GoogleSignInHelper, ErrorContent(), GoogleSignInButton() (+12 more)

### Community 15 - "Sketch Domain Interface"
Cohesion: 0.15
Nodes (14): DrawingPath, EmojiElement, Flow, List, Result, Sketch, String, Unit (+6 more)

### Community 16 - "Login Screen & Google Auth"
Cohesion: 0.09
Nodes (24): GoogleSignInHelper, LoginScreen, LoginUiState, LoginViewModel, GradientButton / GradientOutlinedButton, Navigation (AppNavigation + Screen sealed class), BitmapExporter (createBitmapFromPaths), BrushIconMapper (+16 more)

### Community 17 - "Cloudinary Upload Impl"
Cohesion: 0.18
Nodes (14): CloudinaryRawUploadResult, CloudinaryUploadResult, Int, Result, String, Unit, CloudinaryDataSourceImpl, CloudinaryConfigException (+6 more)

### Community 18 - "Drawing Canvas Touch"
Cohesion: 0.30
Nodes (21): androidx, Boolean, Color, com, DrawingPath, Float, drawAirbrush(), drawArrowShape() (+13 more)

### Community 19 - "Google Sign-In Use Case"
Cohesion: 0.14
Nodes (14): AuthUser, Result, String, StateFlow, String, Error, Idle, Loading (+6 more)

### Community 20 - "Drawing Toolbar"
Cohesion: 0.21
Nodes (19): Boolean, BrushType, Color, Float, Modifier, ShapeTool, String, ActionStrip() (+11 more)

### Community 21 - "Drawing Persistence"
Cohesion: 0.11
Nodes (11): Bitmap, Boolean, Result, String, Result, Sketch, Int, SaveDrawingUseCase (+3 more)

### Community 22 - "Network Result Handling"
Cohesion: 0.28
Nodes (14): Result, Nothing, Error, Loading, NetworkException, NetworkResult, NoInternetException, safeApiCall() (+6 more)

### Community 23 - "Cloudinary Data Interface"
Cohesion: 0.26
Nodes (7): CloudinaryRawUploadResult, CloudinaryUploadResult, Int, Result, String, Unit, CloudinaryDataSource

### Community 24 - "Image Crop Screen UI"
Cohesion: 0.22
Nodes (12): android, Float, Offset, String, CropHandle, CropRect, DisplayBounds, handleScreenPositions() (+4 more)

### Community 25 - "Settings Screen"
Cohesion: 0.27
Nodes (12): Boolean, Modifier, SettingsUiState, SettingsViewModel, String, ThemeMode, SettingsScreen(), SettingsScreenContent() (+4 more)

### Community 26 - "Build Configuration"
Cohesion: 0.18
Nodes (10): String, validateSecrets(), client, configuration_version, project_info, project_id, project_number, storage_bucket (+2 more)

### Community 27 - "Sketch DAO Operations"
Cohesion: 0.30
Nodes (5): Flow, List, SketchEntity, String, SketchDao

### Community 28 - "Local Database Layer"
Cohesion: 0.24
Nodes (6): Sketch, Sketch Serialization Pattern (paths/emojis/images to JSON), fromDomain(), SketchEntity, migrate(), SupportSQLiteDatabase

### Community 29 - "Cloudinary Config"
Cohesion: 0.27
Nodes (4): CloudinaryConfig, CloudinaryRawUploadResult, CloudinaryUploadResult, KMP (Kotlin Multiplatform) Readiness

### Community 30 - "Project Technology Stack"
Cohesion: 0.25
Nodes (8): Cloudinary, Firebase Auth + Storage, Jetpack Compose, KMP (Kotlin Multiplatform) Readiness, Koin DI, MVVM + Clean Architecture, Product Flavors (dev/qa/stag/prod), SketchApp

### Community 31 - "Sketch By ID Use Case"
Cohesion: 0.29
Nodes (5): Boolean, Result, Sketch, String, GetSketchByIdUseCase

### Community 32 - "Emoji Serialization"
Cohesion: 0.53
Nodes (4): EmojiElement, List, String, EmojiSerializer

### Community 33 - "Image Serialization"
Cohesion: 0.53
Nodes (4): ImageElement, List, String, ImageSerializer

### Community 34 - "Path Serialization"
Cohesion: 0.53
Nodes (4): DrawingPath, List, String, PathSerializer

### Community 35 - "Sketch Sync Use Case"
Cohesion: 0.33
Nodes (4): Result, String, Unit, SyncSketchesUseCase

### Community 36 - "VSCode Debug Config"
Cohesion: 0.33
Nodes (4): configurations, version, tasks, version

### Community 37 - "Firebase Sketch DTO"
Cohesion: 0.67
Nodes (3): com, fromDomain(), SketchDto

### Community 38 - "Network Module DI"
Cohesion: 0.67
Nodes (3): String, log(), provideBaseUrl()

### Community 39 - "Room Database"
Cohesion: 0.50
Nodes (3): SketchDatabase, RoomDatabase, SketchDao

### Community 40 - "App Launcher Icon (hdpi)"
Cohesion: 0.50
Nodes (4): Android Robot Mascot (white silhouette), SketchApp Launcher Icon (hdpi), hdpi screen density resource (mipmap-hdpi), Green rounded-square background with white Android robot figure

### Community 45 - "Build & CI Setup"
Cohesion: 0.67
Nodes (3): build.gradle.kts (top-level), Firebase Setup Guide, Android Release Build CI pipeline

## Knowledge Gaps
- **266 isolated node(s):** `PreToolUse`, `allow`, `version`, `configurations`, `java.configuration.updateBuildConfiguration` (+261 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **35 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SketchRepositoryImpl` connect `Sketch Repository & Drawing Data` to `Drawing & Preferences Repository`, `Auth Repository Data Layer`, `Cloudinary Upload Impl`, `Sketch Domain Interface`?**
  _High betweenness centrality (0.195) - this node is a cross-community bridge._
- **Why does `AppNavigation()` connect `Navigation & Auth Flow` to `Bitmap Export Rendering`, `Profile & Sign-Out Flow`, `App Entry & Theming`, `Gallery UI Components`, `Image Crop Screen UI`, `Settings Screen`?**
  _High betweenness centrality (0.176) - this node is a cross-community bridge._
- **Why does `DrawingViewModel` connect `Drawing Canvas & Image Elements` to `ViewModel State & Auth`, `Drawing Persistence`?**
  _High betweenness centrality (0.164) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `DrawingCanvas()` (e.g. with `.onDraw()` and `.onDrawEnd()`) actually correct?**
  _`DrawingCanvas()` has 10 INFERRED edges - model-reasoned connections that need verification._
- **Are the 4 inferred relationships involving `SketchRepositoryImpl` (e.g. with `cloudinaryModule` and `databaseModule`) actually correct?**
  _`SketchRepositoryImpl` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `PreToolUse`, `allow`, `version` to the rest of the system?**
  _273 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Drawing Canvas & Image Elements` be split into smaller, more focused modules?**
  _Cohesion score 0.05901639344262295 - nodes in this community are weakly interconnected._