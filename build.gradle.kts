plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.jetbrainsKotlinJvm) apply false
    `maven-publish`
}

// Project-wide configuration for JitPack publishing
subprojects {
    group = "com.github.eslamwael74.speechtotextkit"
    version = "1.0.0"

    // Apply common publishing configuration to all modules
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        apply(plugin = "maven-publish")
    }
}
