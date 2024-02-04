// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
    alias(libs.plugins.googleService) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.daggerHiltAndroid) apply false
    alias(libs.plugins.googleDevToolsKsp) apply false
    alias(libs.plugins.firebasePerformanceMonitoring) apply false
    alias(libs.plugins.secretsGradlePlugin) apply false
}
true // Needed to make the Suppress annotation work for the plugins block
