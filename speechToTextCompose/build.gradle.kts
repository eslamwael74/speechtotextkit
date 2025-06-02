@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    `maven-publish`
}

android {
    namespace = "com.eslamwael74.speechtotextcompose"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        aarMetadata {
            minCompileSdk = libs.versions.android.minSdk.get().toInt()
        }
    }

    // This block tells AGP which variant the "kotlin" component should use
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

kotlin {
    androidTarget() // Removed publishLibraryVariants, AGP's publishing block handles it

    val xcfName = "speechToTextKitCompose"

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

// This block is outside the kotlin block
afterEvaluate {
    publishing {
        publications {
            // Configure the existing KMP publication
            named<MavenPublication>("kotlinMultiplatform") {
                // Explicitly set GAV for JitPack
                this.groupId = "com.github.eslamwael74.speechtotextkit"
                this.artifactId = "speechToTextCompose" // KMP default is project name, ensure it's what we want
                this.version = "1.0.0"

                pom {
                    name.set("SpeechToText Compose Library")
                    description.set("Jetpack Compose UI components for SpeechToTextKit")
                    url.set("https://github.com/eslamwael74/speechtotextkit")
                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("eslamwael74")
                            name.set("Eslam Wael")
                            url.set("https://github.com/eslamwael74")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/eslamwael74/speechtotextkit.git")
                        developerConnection.set("scm:git:ssh://github.com/eslamwael74/speechtotextkit.git")
                        url.set("https://github.com/eslamwael74/speechtotextkit")
                    }
                }
            }
        }
        repositories {
            // For local testing, you can publish to a local directory
            maven {
                name = "localBuildRepo"
                url = uri(rootProject.buildDir.resolve("repo"))
            }
        }
    }
}
