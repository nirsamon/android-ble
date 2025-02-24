// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
}

tasks.register("assembleAllModulesRelease") {
    group = "build"
    description = "Assembles release APK/AAR for all modules except the app module"

    dependsOn(
        ":common:assembleRelease",
        ":controller:assembleRelease",
        ":blescanner:assembleRelease"
    )
}