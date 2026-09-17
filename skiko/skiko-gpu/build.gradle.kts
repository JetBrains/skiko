@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import tasks.configuration.*

plugins {
    kotlin("multiplatform")
    org.jetbrains.dokka
    `maven-publish`
    signing
    org.gradle.crypto.checksum
}

val skiko = SkikoProperties(rootProject)
val coreProject = project(":")
val gpuArtifacts = SkikoArtifacts(
    artifactIdPrefix = "skiko-gpu",
    displayName = "Skiko GPU",
    pomDescription = "Backend-neutral Skiko GPU API",
)
val gpuProjectContext = SkikoProjectContext(
    project = project,
    skiko = skiko,
    kotlin = kotlin,
    kind = SkikoModuleKind.EXTENSION,
    artifacts = gpuArtifacts,
    windowsSdkPathProvider = {
        findWindowsSdkPaths(gradle, skiko.targetArch)
    },
    additionalRuntimeLibraries = emptyList(),
    configureDependencies = {},
)

repositories {
    mavenCentral {
        url = uri("https://cache-redirector.jetbrains.com/maven-central")
    }
}

kotlin {
    compilerOptions {
        languageVersion.set(skikoKotlinLanguageVersion)
        apiVersion.set(skikoKotlinApiVersion)
        freeCompilerArgs.add("-opt-in=org.jetbrains.skiko.InternalSkikoApi")
        freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
    }

    applyHierarchyTemplate(skikoSourceSetHierarchyTemplate)

    if (supportNativeIosArm64) {
        iosArm64()
    }
    if (supportNativeIosSimulatorArm64) {
        iosSimulatorArm64()
    }
    if (supportNativeIosX64) {
        iosX64()
    }

    sourceSets.commonMain.dependencies {
        implementation(kotlin("stdlib"))
        // Core has publications with different coordinates, so project api dependencies cannot be published.
        // Consumers must add core Skiko alongside skiko-gpu.
        compileOnly(coreProject)
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(coreProject)
    }
}

gpuProjectContext.declarePublications()

val mavenCentral = MavenCentralProperties(project)
if (skiko.isTeamcityCIBuild || mavenCentral.signArtifacts) {
    signing {
        sign(publishing.publications)
        useInMemoryPgpKeys(mavenCentral.signArtifactsKey.get(), mavenCentral.signArtifactsPassword.get())
    }
    configureSignAndPublishDependencies()
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
}
