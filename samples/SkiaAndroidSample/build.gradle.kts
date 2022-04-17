buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
    }
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

plugins {
    id("com.android.application")  version "7.0.2"
    kotlin("android") version "1.6.20"
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

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 27
        targetSdk = 31
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
//    publishAndroidPublicationToMavenLocal
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    implementation("org.jetbrains.skiko:skiko-android:$version")

    skikoNativeX64("org.jetbrains.skiko:skiko-android-runtime-x64:$version")
    skikoNativeArm64("org.jetbrains.skiko:skiko-android-runtime-arm64:$version")
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile>().configureEach {
    dependsOn(unzipTaskX64)
    dependsOn(unzipTaskArm64)
}