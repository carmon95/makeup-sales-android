// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// build.gradle.kts (raíz del proyecto)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Agrega la clase del plugin google services
        classpath("com.google.gms:google-services:4.4.0")
    }
}