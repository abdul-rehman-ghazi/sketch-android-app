plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.hotmail.arehmananis.sketchapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hotmail.arehmananis.sketchapp"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"

            buildConfigField("String", "BASE_URL", "\"https://api-dev.sketchapp.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"Development\"")

            resValue("string", "app_name", "Sketch Dev")
        }

        create("qa") {
            dimension = "environment"
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"

            buildConfigField("String", "BASE_URL", "\"https://api-qa.sketchapp.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"QA\"")

            resValue("string", "app_name", "Sketch QA")
        }

        create("stag") {
            dimension = "environment"
            applicationIdSuffix = ".stag"
            versionNameSuffix = "-stag"

            buildConfigField("String", "BASE_URL", "\"https://api-stag.sketchapp.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"Staging\"")

            resValue("string", "app_name", "Sketch Staging")
        }

        create("prod") {
            dimension = "environment"
            // No suffix for production

            buildConfigField("String", "BASE_URL", "\"https://api.sketchapp.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"Production\"")

            resValue("string", "app_name", "Sketch App")
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
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
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Koin - KMP Dependency Injection
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Lifecycle - KMP ViewModel
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Ktor - KMP Networking
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    debugImplementation(libs.ktor.client.logging)

    // Kotlinx Serialization - KMP JSON Parsing
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}