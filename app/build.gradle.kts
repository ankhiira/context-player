import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.navsafe.args)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.gabchmel.contextmusicplayer"

        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        versionCode = 17
        versionName = "1.3.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("debug")
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
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
        }
    }

    android {
        testOptions.unitTests.isIncludeAndroidResources = true
    }

    packaging {
        resources {
            excludes += "META-INF/native-image/**"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/NOTICE.md"
        }
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

    implementation(libs.androidx.core.splashscreen)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.compose.ui)
    // Tooling support (Previews, etc.)
    implementation(libs.compose.ui.tooling)
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation(libs.compose.foundation)

    implementation(libs.compose.material3)
    // Material Design
    implementation(libs.compose.material)
    // Material design icons
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)

    // Integration with activities
    implementation(libs.androidx.activity.compose)
    // Integration with ViewModels
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.lifecycle.process)

    implementation(libs.coil.compose)

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

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.testing)

    // Navigation 3
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // UI Tests
    androidTestImplementation(libs.compose.ui.test.junit4)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)

    testImplementation(libs.core.ktx)
    testImplementation(libs.androidx.junit.ktx)
}