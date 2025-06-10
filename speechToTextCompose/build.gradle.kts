@file:OptIn(ExperimentalWasmDsl::class)

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.mavenPublishPlugin)
}

android {
    namespace = "io.github.eslamwael74.speechtotextcompose"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        aarMetadata {
            minCompileSdk = libs.versions.android.minSdk.get().toInt()
        }
    }
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
    }

    val xcfName = "speechToTextCompose"

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = xcfName
        }
    }

    jvm("desktop")

    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(libs.kotlinx.coroutines.core)
            api(projects.speechToText)
        }

        androidMain.dependencies {
            implementation(compose.foundation)
        }

        wasmJsMain.dependencies {
            // Add wasm-specific dependencies here if needed
        }
    }
}

mavenPublishing {

    coordinates(
        groupId = "io.github.eslamwael74.speechtotextcompose",
        artifactId = "speechToTextCompose",
        version = "1.0.0"
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("SpeechToTextCompose")
        description.set("A Kotlin Multiplatform library for speech-to-text functionality in Compose applications.")
        inceptionYear.set("2025")
        url.set("https://github.com/eslamwael74/speechtotextkit")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        // Specify developers information
        developers {
            developer {
                id.set("eslamwael74")
                name.set("Eslam Wael")
                email.set("eslam.wael.8.ew@gmail.com")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/eslamwael74/speechtotextkit")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}