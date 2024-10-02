// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}

buildscript {
    val objectboxVersion by extra("4.0.2")
    repositories {
        mavenCentral()

        // Note: 2.9.0 and older are available on jcenter()
    }
    dependencies {
        // Android Gradle Plugin 3.3.0 or later supported.
        classpath("io.objectbox:objectbox-gradle-plugin:$objectboxVersion")
    }
}