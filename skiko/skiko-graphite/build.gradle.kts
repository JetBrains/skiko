@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import tasks.configuration.*
import dsl.SkikoDependencyScope

plugins {
    kotlin("multiplatform")
    org.jetbrains.dokka
    `maven-publish`
    signing
    org.gradle.crypto.checksum
}

val skiko = SkikoProperties(rootProject)
val targetOs = hostOs
val targetArch = skiko.targetArch
val coreProject = project(":")
val graphiteArtifacts = SkikoArtifacts(
    artifactIdPrefix = "skiko-graphite",
    displayName = "Skiko Graphite",
    pomDescription = "Kotlin Skia Graphite bindings",
)

val graphiteDependencies: SkikoDependencyScope.() -> Unit = {
    dependsOnCore()
    targets {
        jvm {
            macos {
                staticSkiaLibs("skia_graphite_ext")
                linkFlags("-lobjc")
                frameworks("CoreFoundation", "Foundation", "Metal")
            }
            linux {
                staticSkiaLibs("skia_graphite_ext")
                dynamicSystemLibs("vulkan")
                compilerFlags("-DSK_VULKAN", "-DSK_USE_INTERNAL_VULKAN_HEADERS")
            }
            windows {
                staticSkiaLibs("skia_graphite_ext")
                dynamicSystemLibs("vulkan-1", "onecore_apiset", "dxguid")
                compilerFlags("-DSK_VULKAN", "-DSK_USE_INTERNAL_VULKAN_HEADERS")
            }
        }
        native {
            staticSkiaLibs("skia_graphite_ext")
            macos { frameworks("CoreFoundation", "Foundation", "Metal") }
            ios { frameworks("CoreFoundation", "Foundation", "Metal") }
            tvos { frameworks("CoreFoundation", "Foundation", "Metal") }
        }
    }
}

val graphiteProjectContext = SkikoProjectContext(
    project = project,
    skiko = skiko,
    kotlin = kotlin,
    kind = SkikoModuleKind.EXTENSION,
    artifacts = graphiteArtifacts,
    windowsSdkPathProvider = {
        findWindowsSdkPaths(gradle, targetArch)
    },
    additionalRuntimeLibraries = emptyList(),
    configureDependencies = graphiteDependencies,
)

repositories {
    mavenCentral {
        url = uri("https://cache-redirector.jetbrains.com/maven-central")
    }
    google()
}

kotlin.run {
    compilerOptions {
        languageVersion.set(skikoKotlinLanguageVersion)
        apiVersion.set(skikoKotlinApiVersion)
        freeCompilerArgs.add("-opt-in=org.jetbrains.skiko.InternalSkikoApi")
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


    fun coreNativeSymbolSources(os: OS, arch: Arch, isUikitSim: Boolean) =
        graphiteProjectContext.nativeSymbolSourcesFor(os, arch, isUikitSim).also {
            dependencies.add(it.name, coreProject)
        }

    if (supportNativeMac) {
        graphiteProjectContext.configureNativeTarget(OS.MacOS, Arch.X64, macosX64(), ::coreNativeSymbolSources)
        graphiteProjectContext.configureNativeTarget(OS.MacOS, Arch.Arm64, macosArm64(), ::coreNativeSymbolSources)
    }
    if (supportNativeIosArm64) {
        graphiteProjectContext.configureNativeTarget(OS.IOS, Arch.Arm64, iosArm64(), ::coreNativeSymbolSources)
    }
    if (supportNativeIosSimulatorArm64) {
        graphiteProjectContext.configureNativeTarget(OS.IOS, Arch.Arm64, iosSimulatorArm64(), ::coreNativeSymbolSources)
    }
    if (supportNativeIosX64) {
        graphiteProjectContext.configureNativeTarget(OS.IOS, Arch.X64, iosX64(), ::coreNativeSymbolSources)
    }
    if (supportNativeTvosArm64) {
        graphiteProjectContext.configureNativeTarget(OS.TVOS, Arch.Arm64, tvosArm64(), ::coreNativeSymbolSources)
    }
    if (supportNativeTvosSimulatorArm64) {
        graphiteProjectContext.configureNativeTarget(OS.TVOS, Arch.Arm64, tvosSimulatorArm64(), ::coreNativeSymbolSources)
    }
    if (supportNativeTvosX64) {
        graphiteProjectContext.configureNativeTarget(OS.TVOS, Arch.X64, tvosX64(), ::coreNativeSymbolSources)
    }

    sourceSets.commonMain.dependencies {
        implementation(kotlin("stdlib"))
        /*
        We use compileOnly here because the root project publishes multiple artifacts
        which makes api/implementation(project(":")) fail during publishing.
        This avoids Gradle's multi-publication ambiguity but skiko core is NOT added
        as a transitive dependency of skiko-graphite, and it will NOT appear in the published POM
        consumers MUST explicitly depend on both:
            - implementation("org.jetbrains.skiko:skiko-x")
            - implementation("org.jetbrains.skiko:skiko-graphite-x")
         */
        compileOnly(coreProject)
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(coreProject)
    }

    if (supportAwt) {
        graphiteProjectContext.jvmMainSourceSet?.dependencies {
            implementation(kotlin("stdlib"))
        }
        graphiteProjectContext.jvmTestSourceSet?.dependencies {
            implementation(kotlin("test-junit"))
            implementation(kotlin("test"))
        }
        graphiteProjectContext.awtMainSourceSet?.dependencies {
            implementation(libs.jetbrainsRuntime.api)
        }
    }


    if (supportAnyNative) {
        sourceSets.all {
            // Really ugly, see https://youtrack.jetbrains.com/issue/KT-46649 why it is required,
            // note that setting it per source set still keeps it unset in commonized source sets.
            languageSettings.optIn("kotlin.native.SymbolNameIsInternal")
        }
        configureIOSTestsWithMetal(project)
    }
}


if (supportAwt) {
    val graphiteAwtJarForTests by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-graphite-awt-test")
        from(kotlin.jvm("awt").compilations["main"].output.allOutputs)
    }
    val coreJvmLinkedLibrary = graphiteProjectContext.jvmLinkedLibraryFor(targetOs, targetArch).also {
        dependencies.add(it.name, coreProject)
    }
    val macosX64CoreLinkedLibrary = if (targetOs == OS.MacOS && targetArch == Arch.Arm64) {
        graphiteProjectContext.jvmLinkedLibraryFor(OS.MacOS, Arch.X64).also {
            dependencies.add(it.name, coreProject)
        }
    } else {
        null
    }
    val coreJvmRuntimeJar = graphiteProjectContext.jvmRuntimeJarFor(targetOs, targetArch).also {
        dependencies.add(it.name, coreProject)
    }

    graphiteProjectContext.setupJvmTestTask(
        graphiteAwtJarForTests,
        targetOs,
        targetArch,
        files(coreJvmLinkedLibrary),
        macosX64CoreLinkedLibrary?.let { files(it) },
        coreJvmRuntimeJar,
    )
    graphiteProjectContext.provideJvmRequiredSymbols(targetOs, targetArch)
    if (targetOs == OS.MacOS && targetArch == Arch.Arm64) {
        graphiteProjectContext.provideJvmRequiredSymbols(OS.MacOS, Arch.X64)
    }
}

graphiteProjectContext.declarePublications()

val mavenCentral = MavenCentralProperties(project)
if (skiko.isTeamcityCIBuild || mavenCentral.signArtifacts) {
    signing {
        sign(publishing.publications)
        useInMemoryPgpKeys(mavenCentral.signArtifactsKey.get(), mavenCentral.signArtifactsPassword.get())
    }
    configureSignAndPublishDependencies()
}

tasks.withType<KotlinNativeCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
}
