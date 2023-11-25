plugins {
    id("com.android.library")
    id("kotlin-android")
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
    buildFeatures {
        mlModelBinding = true
    }
    namespace = "com.gabchmel.predicitonmodule"
}

dependencies {
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation(project(":common"))


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // onnx runtime
    implementation("com.microsoft.onnxruntime:onnxruntime-mobile:1.8.0")
//    implementation "com.github.microsoft:onnxruntime:v1.8.0"

    implementation("org.jpmml:pmml-evaluator:1.5.15")
//    implementation "com.github.jpmml.jpmml-android:jpmml-android:master-SNAPSHOT"
//    implementation "org.jpmml:jpmml-android:1.0-SNAPSHOT"

//    implementation "org.jpmml:pmml-model:1.5.15"

    implementation("com.google.code.gson:gson:2.9.0")

    implementation("org.pmml4s:pmml4s_2.11:0.9.11")

    implementation("com.github.haifengl:smile-kotlin:2.6.0")

    // Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

    implementation(files("libs/wekaSTRIPPED.jar"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
}
repositories {
    mavenCentral()
}