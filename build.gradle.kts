// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
//    alias(libs.plugins.androidLibrary) apply false
//    alias(libs.plugins.jetbrainsCompose) apply false
//    alias(libs.plugins.kotlinMultiplatform) apply false
//    alias(libs.plugins.googleServices) apply false
}

//buildscript {
//    val kotlinVersion by extra("1.9.10")

//    dependencies {
////        classpath("com.android.tools.build:gradle:8.3.1")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
//        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
//    }
//}

//tasks.register<Delete>("clean").configure {
//    delete(rootProject.layout.buildDirectory)
//}