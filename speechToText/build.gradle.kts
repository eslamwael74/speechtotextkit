import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    `maven-publish`
}

android {
    namespace = "com.eslamwael74.speechtotext"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        // Make sure to add this for consuming the library on Android
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

    // For iOS targets, this is also where you should
    // configure native binary output.
    val xcfName = "speechToTextKit"

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

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "speechToText.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    // Source set declarations
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
            }
        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP's default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }

        val desktopMain by getting
        desktopMain.dependencies {

        }
    }
}

// This block is outside the kotlin block
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("speechToTextPublication") { // Unique publication name
                from(components["kotlin"]) // Use the KMP "kotlin" component

                groupId = "com.github.eslamwael74.speechtotextkit"
                artifactId = "speechToText"
                version = "1.0.0"

                pom {
                    name.set("SpeechToText Core Library")
                    description.set("Core Speech to Text API for Kotlin Multiplatform")
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
