plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.android.navsafe.args)
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.gabchmel.contextmusicplayer"

        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        versionCode = 15
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("debug")
    }

    buildFeatures {
        buildConfig = true
        // Enables Jetpack Compose for this module
        compose = true
    }

    // Set both the Java and Kotlin compilers to target Java 17.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
            isDebuggable = false
        }
    }

    flavorDimensions += "mode"
    productFlavors {
        create("debugVersion") {
            dimension = "mode"
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        create("full") {
            dimension = "mode"
            applicationIdSuffix = ".release"
            versionNameSuffix = "-full"
        }
    }

    android {
        testOptions.unitTests.isIncludeAndroidResources = true
    }

    namespace = "com.gabchmel.contextmusicplayer"
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })

    // Dependency on the other local modules
    implementation(project(":sensormodule"))
    implementation(project(":common"))
    implementation(project(":predicitonmodule"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Navigation
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.core.splashscreen)

    // Compose
    implementation(libs.compose.ui)
    // Tooling support (Previews, etc.)
    implementation(libs.compose.ui.tooling)
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation(libs.compose.foundation)

    implementation(libs.androidx.material3)
    // Material Design
    implementation(libs.compose.material)
    // Material design icons
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    // Integration with observables
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.runtime.rxjava2)

    // Integration with activities
    implementation(libs.androidx.activity.compose.v181)
    // Integration with ViewModels
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.lifecycle.process)

    // Accompanist
    implementation(libs.accompanist.glide)

    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.common)

    // WorkManager - Kotlin + coroutines
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.kotlinx.datetime)

    // For occurring error of duplicate library?
    implementation(libs.listenablefuture)

    // For iterating through class properties
    implementation(libs.kotlin.reflect)

    // Preferences DataStore
    implementation(libs.androidx.datastore.preferences)

    // UI Tests
    androidTestImplementation(libs.androidx.ui.test.junit4)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)

    testImplementation(libs.core.ktx)
    testImplementation(libs.androidx.junit.ktx)
}