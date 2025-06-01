@file:OptIn(ExperimentalWasmDsl::class)
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

object Project {
    const val ARTIFACT_ID = "speech-to-text-compose"
    const val NAMESPACE = "com.eslamwael74.speechtotextcompose"
}

kotlin {

    androidTarget {
        publishLibraryVariants("release", "debug")
    }

    val xcfName = "speechToTextKitCompose"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
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

        }
    }

}

android {
    namespace = Project.NAMESPACE
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
