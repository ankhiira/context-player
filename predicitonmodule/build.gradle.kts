plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin)
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
    buildFeatures {
        mlModelBinding = true
    }
    namespace = "com.gabchmel.predicitonmodule"
}

dependencies {
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation(project(":common"))


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // onnx runtime
    implementation(libs.onnxruntime.mobile)
//    implementation "com.github.microsoft:onnxruntime:v1.8.0"

    implementation(libs.pmml.evaluator)
//    implementation "com.github.jpmml.jpmml-android:jpmml-android:master-SNAPSHOT"
//    implementation "org.jpmml:jpmml-android:1.0-SNAPSHOT"

//    implementation "org.jpmml:pmml-model:1.5.15"

    implementation(libs.gson)

    implementation(libs.pmml4s)

    implementation(libs.smile.kotlin)

    // Kotlin coroutines
    implementation(libs.kotlinx.coroutines.core)

    implementation(files("libs/wekaSTRIPPED.jar"))
    implementation(libs.kotlin.stdlib.jdk7)
}
repositories {
    mavenCentral()
}