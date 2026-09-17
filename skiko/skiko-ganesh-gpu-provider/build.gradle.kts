@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import tasks.configuration.*

plugins {
    kotlin("multiplatform")
    org.jetbrains.dokka
    `maven-publish`
    signing
    org.gradle.crypto.checksum
}

val skiko = SkikoProperties(rootProject)
val providerArtifacts = SkikoArtifacts(
    artifactIdPrefix = "skiko-ganesh-gpu-provider",
    displayName = "Skiko Ganesh GPU Provider",
    pomDescription = "Ganesh implementation of the Skiko GPU provider API",
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

// skiko-ganesh has multiple publications, so this selects the proper publication and avoids the
// ambiguity that prevents using a direct implementation() or api() project dependency.
configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("${project.group}:skiko-ganesh"))
            .using(project(":skiko-ganesh"))
            .because("Use the local Ganesh project while building Skiko")
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
    }
    sourceSets.named("iosMain") {
        dependencies {
            compileOnly(project(":"))
            api(project(":skiko-gpu"))
            implementation("${project.group}:skiko-ganesh:${project.version}")
        }
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
