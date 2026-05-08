@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library") apply false
}

if (supportAndroid) {
    apply(plugin = "com.android.kotlin.multiplatform.library")
}

repositories {
    mavenCentral()
    google()
}

kotlin {
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
    }

    applyHierarchyTemplate(skikoSourceSetHierarchyTemplate)

    if (supportAwt) {
        jvm("awt") {
            compilations.all {
                compileTaskProvider.configure {
                    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }

    if (supportAndroid) {
        targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
            namespace = "org.jetbrains.skiko.testutils"
            compileSdk = 35
            minSdk = 24

            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }

    if (supportWeb) {
        js {
            browser()
        }
        wasmJs {
            browser()
        }
    }

    if (supportNativeMac) {
        macosX64()
        macosArm64()
    }
    if (supportNativeLinux) {
        linuxX64()
        linuxArm64()
    }
    if (supportNativeIosArm64) {
        iosArm64()
    }
    if (supportNativeIosSimulatorArm64) {
        iosSimulatorArm64()
    }
    if (supportNativeIosX64) {
        iosX64()
    }
    if (supportNativeTvosArm64) {
        tvosArm64()
    }
    if (supportNativeTvosSimulatorArm64) {
        tvosSimulatorArm64()
    }
    if (supportNativeTvosX64) {
        tvosX64()
    }

    sourceSets.commonMain.dependencies {
        implementation(kotlin("stdlib"))
        api(project(":"))
        implementation(kotlin("test"))
        implementation(kotlin("test-annotations-common"))
        implementation(libs.coroutines.core)
    }

    if (supportAwt) {
        sourceSets.jvmMain.dependencies {
            implementation(kotlin("test-junit"))
        }
    }
}

tasks.withType<KotlinNativeCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
}
