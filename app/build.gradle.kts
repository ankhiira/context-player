plugins {
    id("com.android.application")
    id("kotlin-android")
    id("androidx.navigation.safeargs.kotlin")
}

val kotlinVersion: String by rootProject.extra

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

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Navigation
    val navVersion = "2.7.5"
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation("androidx.navigation:navigation-compose:$navVersion")

    implementation("androidx.core:core-splashscreen:1.1.0-rc01")

    // Compose
    val composeVersion = "1.6.5"
    implementation("androidx.compose.ui:ui:$composeVersion")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:$composeVersion")

    implementation("androidx.compose.material3:material3:1.1.2")
    // Material Design
    implementation("androidx.compose.material:material:$composeVersion")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    // Integration with observables
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")
    implementation("androidx.compose.runtime:runtime-rxjava2:$composeVersion")

    // Integration with activities
    implementation("androidx.activity:activity-compose:1.8.1")
    // Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    val lifecycleVersion = "2.6.2"
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")

    // Accompanist
    implementation("com.google.accompanist:accompanist-glide:0.11.1")

    val media3Version = "1.2.0"
    implementation("androidx.media3:media3-session:$media3Version")
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-common:$media3Version")

    // WorkManager - Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // For occurring error of duplicate library?
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    // For iterating through class properties
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")
}