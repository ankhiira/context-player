plugins {
    id("com.android.library")
    id("kotlin-android")
    kotlin("plugin.serialization") version "1.9.21"
}

val kotlinVersion: String by rootProject.extra

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
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "com.gabchmel.sensorprocessor"
}

dependencies {
    implementation(project(":predicitonmodule"))
    implementation(project(":common"))

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Activity detection
    implementation("com.google.android.gms:play-services-location:21.0.1")

//    implementation "nz.ac.waikato.cms.weka:weka-stable:3.6.13"

    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.15.2")
    implementation("com.opencsv:opencsv:4.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("com.github.chrfrantz:DBSCAN:v0.1")

    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation(files("libs/cluster-gvm-1.0.jar"))
}
repositories {
    mavenCentral()
}