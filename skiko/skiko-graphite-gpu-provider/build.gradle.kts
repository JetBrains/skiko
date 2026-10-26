@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.skiko.build.configuration.configureSignAndPublishDependencies
import org.jetbrains.skiko.build.context.SkikoModuleKind
import org.jetbrains.skiko.build.context.SkikoProjectContext
import org.jetbrains.skiko.build.context.skikoSourceSetHierarchyTemplate
import org.jetbrains.skiko.build.context.supportNativeIosArm64
import org.jetbrains.skiko.build.context.supportNativeIosSimulatorArm64
import org.jetbrains.skiko.build.context.supportNativeIosX64
import org.jetbrains.skiko.build.cpp.findWindowsSdkPaths
import org.jetbrains.skiko.build.publishing.declarePublications
import org.jetbrains.skiko.build.utils.SkikoArtifacts
import org.jetbrains.skiko.build.utils.SkikoProperties
import org.jetbrains.skiko.build.utils.skikoKotlinApiVersion
import org.jetbrains.skiko.build.utils.skikoKotlinLanguageVersion

plugins {
    kotlin("multiplatform")
    org.jetbrains.dokka
    `maven-publish`
    signing
    org.gradle.crypto.checksum
}

val skiko = SkikoProperties(rootProject)
val providerArtifacts = SkikoArtifacts(
    artifactIdPrefix = "skiko-graphite-gpu-provider",
    displayName = "Skiko Graphite GPU Provider",
    pomDescription = "Graphite implementation of the Skiko GPU provider API",
)
val providerProjectContext = SkikoProjectContext(
    project = project,
    skiko = skiko,
    kotlin = kotlin,
    kind = SkikoModuleKind.EXTENSION,
    artifacts = providerArtifacts,
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
        api(project(":"))
        api(project(":skiko-gpu"))
        implementation(project(":skiko-graphite"))
    }
}

providerProjectContext.declarePublications()

val mavenCentral = MavenCentralProperties(project)
if (skiko.isTeamcityCIBuild || mavenCentral.signArtifacts) {
    signing {
        sign(publishing.publications)
        useInMemoryPgpKeys(mavenCentral.signArtifactsKey.get(), mavenCentral.signArtifactsPassword.get())
    }
    configureSignAndPublishDependencies()
}

tasks.withType<KotlinNativeCompile>().configureEach {
    if (name.startsWith("compileKotlinIos")) {
        compilerOptions.moduleName.set("${project.group}:skiko-gpu-provider")
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
}
