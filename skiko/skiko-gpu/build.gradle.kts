@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.skiko.build.configuration.configureIOSTestsWithMetal
import org.jetbrains.skiko.build.configuration.configureKotlinCompilerOptions
import org.jetbrains.skiko.build.configuration.configureSignAndPublishDependencies
import org.jetbrains.skiko.build.context.SkikoModuleKind
import org.jetbrains.skiko.build.context.SkikoProjectContext
import org.jetbrains.skiko.build.context.skikoSourceSetHierarchyTemplate
import org.jetbrains.skiko.build.context.supportAnyNativeIos
import org.jetbrains.skiko.build.context.supportNativeIosArm64
import org.jetbrains.skiko.build.context.supportNativeIosSimulatorArm64
import org.jetbrains.skiko.build.context.supportNativeIosX64
import org.jetbrains.skiko.build.cpp.findWindowsSdkPaths
import org.jetbrains.skiko.build.publishing.declarePublications
import org.jetbrains.skiko.build.utils.SkikoArtifacts
import org.jetbrains.skiko.build.utils.SkikoProperties

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
        api(coreProject)
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(coreProject)
    }

    if (supportAnyNativeIos) {
        configureIOSTestsWithMetal(project)
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

configureKotlinCompilerOptions()
