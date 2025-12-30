import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

// Read local.properties for sensitive credentials
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

// Validate that required secrets are configured for the flavor being built
fun validateSecrets(flavor: String) {
    // Check if this flavor is being built by examining the Gradle task
    val taskNames = gradle.startParameter.taskNames.map { it.lowercase() }
    val isFlavorBeingBuilt = taskNames.any { task ->
        task.contains(flavor.lowercase()) ||
        task.contains("all") ||
        (task == "build" || task == "assemble")
    }

    // Skip validation if this flavor is not being built
    if (!isFlavorBeingBuilt) {
        println("ℹ️  Skipping secrets validation for '$flavor' (not being built)")
        return
    }

    val requiredSecrets = listOf(
        "google.${flavor}.webClientId",
        "cloudinary.${flavor}.cloudName",
        "cloudinary.${flavor}.apiKey",
        "cloudinary.${flavor}.apiSecret"
    )

    val missingSecrets = requiredSecrets.filter {
        localProperties[it]?.toString().isNullOrBlank()
    }

    if (missingSecrets.isNotEmpty()) {
        throw GradleException(
            """
            |
            |========================================================================
            | MISSING SECRETS FOR FLAVOR: $flavor
            |========================================================================
            |
            | The following secrets are not configured in local.properties:
            | ${missingSecrets.joinToString("\n| - ")}
            |
            | Please copy local.properties.template to local.properties
            | and fill in the required values.
            |
            | See SECRETS_SETUP.md for detailed instructions.
            |
            | NOTE: You only need to configure secrets for environments you build.
            | For local development, you typically only need 'dev' secrets.
            |========================================================================
            |
            """.trimMargin()
        )
    }
}

android {
    namespace = "com.hotmail.arehmananis.sketchapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hotmail.arehmananis.sketchapp"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // NOTE: GOOGLE_CLIENT_ID is now set per-flavor in productFlavors block
        // See local.properties.template for configuration instructions
    }

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            validateSecrets("dev")
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"

            buildConfigField("String", "BASE_URL", "\"https://api-dev.sketchapp.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"Development\"")

            // Google OAuth Web Client ID from local.properties
            buildConfigField("String", "GOOGLE_CLIENT_ID",
                "\"${localProperties["google.dev.webClientId"] ?: ""}\"")

            // Cloudinary configuration from local.properties
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME",
                "\"${localProperties["cloudinary.dev.cloudName"] ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_API_KEY",
                "\"${localProperties["cloudinary.dev.apiKey"] ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET",
                "\"${localProperties["cloudinary.dev.apiSecret"] ?: ""}\"")

            resValue("string", "app_name", "Sketch Dev")
        }

        create("qa") {
            validateSecrets("qa")
            dimension = "environment"
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"

            buildConfigField("String", "BASE_URL", "\"https://api-qa.sketchapp.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"QA\"")

            // Google OAuth Web Client ID from local.properties
            buildConfigField("String", "GOOGLE_CLIENT_ID",
                "\"${localProperties["google.qa.webClientId"] ?: ""}\"")

            // Cloudinary configuration from local.properties
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME",
                "\"${localProperties["cloudinary.qa.cloudName"] ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_API_KEY",
                "\"${localProperties["cloudinary.qa.apiKey"] ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET",
                "\"${localProperties["cloudinary.qa.apiSecret"] ?: ""}\"")

            resValue("string", "app_name", "Sketch QA")
        }

        create("stag") {
            validateSecrets("stag")
            dimension = "environment"
            applicationIdSuffix = ".stag"
            versionNameSuffix = "-stag"

            buildConfigField("String", "BASE_URL", "\"https://api-stag.sketchapp.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"Staging\"")

            // Google OAuth Web Client ID from local.properties
            buildConfigField("String", "GOOGLE_CLIENT_ID",
                "\"${localProperties["google.stag.webClientId"] ?: ""}\"")

            // Cloudinary configuration from local.properties
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME",
                "\"${localProperties["cloudinary.stag.cloudName"] ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_API_KEY",
                "\"${localProperties["cloudinary.stag.apiKey"] ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET",
                "\"${localProperties["cloudinary.stag.apiSecret"] ?: ""}\"")

            resValue("string", "app_name", "Sketch Staging")
        }

        create("prod") {
            validateSecrets("prod")
            dimension = "environment"
            // No suffix for production

            buildConfigField("String", "BASE_URL", "\"https://api.sketchapp.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"Production\"")

            // Google OAuth Web Client ID from local.properties
            buildConfigField("String", "GOOGLE_CLIENT_ID",
                "\"${localProperties["google.prod.webClientId"] ?: ""}\"")

            // Cloudinary configuration from local.properties
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME",
                "\"${localProperties["cloudinary.prod.cloudName"] ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_API_KEY",
                "\"${localProperties["cloudinary.prod.apiKey"] ?: ""}\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET",
                "\"${localProperties["cloudinary.prod.apiSecret"] ?: ""}\"")

            resValue("string", "app_name", "Sketch App")
        }
    }

    signingConfigs {
        create("release") {
            // Try to read from environment variables first (CI/CD)
            // Fall back to local.properties for local builds
            storeFile = file(
                System.getenv("KEYSTORE_FILE")
                    ?: localProperties["release.keystore.file"]?.toString()
                    ?: "release.keystore"
            )
            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: localProperties["release.keystore.password"]?.toString()
            keyAlias = System.getenv("KEY_ALIAS")
                ?: localProperties["release.key.alias"]?.toString()
            keyPassword = System.getenv("KEY_PASSWORD")
                ?: localProperties["release.key.password"]?.toString()
        }
    }

    buildTypes {
        debug {
            buildConfigField("Boolean", "ENABLE_LOGGING", "true")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("Boolean", "ENABLE_LOGGING", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.12.2")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.6")

    // Koin - KMP Dependency Injection
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.2.0")

    // Lifecycle - KMP ViewModel
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Ktor - KMP Networking
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)

    // Kotlinx Serialization - KMP JSON Parsing
    implementation(libs.kotlinx.serialization.json)

    // Firebase (BOM handles versions)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Image Loading
    implementation(libs.coil.compose)

    // Google Sign-In (Credentials Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    // WorkManager for background sync
    implementation(libs.androidx.work.runtime.ktx)

    // Cloudinary for cloud image storage
    implementation(libs.cloudinary.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}