import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin)
    kotlin("plugin.serialization") version "2.0.0"
}

android {
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        }
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

    namespace = "com.gabchmel.sensorprocessor"
}

dependencies {
    implementation(project(":predicitonmodule"))
    implementation(project(":common"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Kotlin coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Activity detection
    implementation(libs.play.services.location)

//    implementation "nz.ac.waikato.cms.weka:weka-stable:3.6.13"

    implementation(libs.kotlin.csv.jvm)
    implementation(libs.opencsv)
    implementation(libs.kotlin.stdlib.jdk7)

    implementation(libs.jetbrains.kotlin.reflect)

    implementation(libs.dbscan)

    // Preferences DataStore
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(files("libs/cluster-gvm-1.0.jar"))
}
repositories {
    mavenCentral()
}