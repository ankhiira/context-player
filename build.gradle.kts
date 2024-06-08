// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.navsafe.args) apply false
}

allprojects{
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

//buildscript {
//    dependencies {
////        classpath("com.android.tools.build:gradle:8.3.1")
//        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
//    }
//}

//tasks.register<Delete>("clean").configure {
//    delete(rootProject.layout.buildDirectory)
//}