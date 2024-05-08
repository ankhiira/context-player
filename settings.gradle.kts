include(":app")
rootProject.name = "ContextMusicPlayer"
include(":predicitonmodule")
include(":sensormodule")
include(":common")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}