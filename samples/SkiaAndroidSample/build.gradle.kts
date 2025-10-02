buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.9.0")
    }
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

plugins {
    id("com.android.application") version "8.9.0"
    kotlin("android") version "1.9.21"
}

val skikoNativeX64 by configurations.creating
val skikoNativeArm64 by configurations.creating

val jniDir = "${projectDir.absolutePath}/src/main/jniLibs"

// TODO: filter .so files only.
val unzipTaskX64 = tasks.register("unzipNativeX64", Copy::class) {
    destinationDir = file("$jniDir/x86_64")
    from(skikoNativeX64.map { zipTree(it) })
}

val unzipTaskArm64 = tasks.register("unzipNativeArm64", Copy::class) {
    destinationDir = file("$jniDir/arm64-v8a")
    from(skikoNativeArm64.map { zipTree(it) })
}

kotlin {
    jvmToolchain(11)
}

android {
    compileSdk = 35
    namespace = "org.jetbrains.skiko.sample"
    defaultConfig {
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        applicationId = "org.jetbrains.skiko.sample"

        ndk {
            abiFilters += listOf("x86_64", "arm64-v8a")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

var version = if (project.hasProperty("skiko.version")) {
    project.properties["skiko.version"] as String
} else {
    "0.0.0-SNAPSHOT"
}

// ./gradlew -Pskiko.android.enabled=true \
//    publishSkikoJvmRuntimeAndroidX64PublicationToMavenLocal \
//    publishSkikoJvmRuntimeAndroidArm64PublicationToMavenLocal \
//    publishAndroidReleasePublicationToMavenLocal
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    implementation("org.jetbrains.skiko:skiko-android:$version")

    skikoNativeX64("org.jetbrains.skiko:skiko-android-runtime-x64:$version")
    skikoNativeArm64("org.jetbrains.skiko:skiko-android-runtime-arm64:$version")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    dependsOn(unzipTaskX64)
    dependsOn(unzipTaskArm64)
}

// SKIKO-934: we need to unpack these libraries before these are collected from android
// TODO the tasks we're actually targetting are mergeDebugJniLibFolders and mergeReleaseJniLibFolders,
//  this adds unncessary dependencies
tasks.withType<com.android.build.gradle.tasks.MergeSourceSetFolders>()
    .configureEach {
        dependsOn(unzipTaskX64)
        dependsOn(unzipTaskArm64)
    }

tasks.withType<Copy> {
    // This line needs to properly merge MANIFEST files from jars into dex
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
