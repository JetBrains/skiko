@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.skiko.build.configuration.configureIOSTestsWithMetal
import org.jetbrains.skiko.build.configuration.configureKotlinCompilerOptions
import org.jetbrains.skiko.build.configuration.configureNativeTarget
import org.jetbrains.skiko.build.configuration.configureSignAndPublishDependencies
import org.jetbrains.skiko.build.configuration.jvmLinkedLibraryFor
import org.jetbrains.skiko.build.configuration.jvmRuntimeJarFor
import org.jetbrains.skiko.build.configuration.nativeSymbolSourcesFor
import org.jetbrains.skiko.build.configuration.provideJvmRequiredSymbols
import org.jetbrains.skiko.build.configuration.setupJvmTestTask
import org.jetbrains.skiko.build.context.SkikoModuleKind
import org.jetbrains.skiko.build.context.SkikoProjectContext
import org.jetbrains.skiko.build.context.awtMainSourceSet
import org.jetbrains.skiko.build.context.jvmMainSourceSet
import org.jetbrains.skiko.build.context.jvmTestSourceSet
import org.jetbrains.skiko.build.context.skikoSourceSetHierarchyTemplate
import org.jetbrains.skiko.build.context.supportAnyNative
import org.jetbrains.skiko.build.context.supportAwt
import org.jetbrains.skiko.build.context.supportNativeIosArm64
import org.jetbrains.skiko.build.context.supportNativeIosSimulatorArm64
import org.jetbrains.skiko.build.context.supportNativeIosX64
import org.jetbrains.skiko.build.context.supportNativeMac
import org.jetbrains.skiko.build.context.supportNativeTvosArm64
import org.jetbrains.skiko.build.context.supportNativeTvosSimulatorArm64
import org.jetbrains.skiko.build.context.supportNativeTvosX64
import org.jetbrains.skiko.build.cpp.findWindowsSdkPaths
import org.jetbrains.skiko.build.dependencies.SkikoDependencyScope
import org.jetbrains.skiko.build.publishing.declarePublications
import org.jetbrains.skiko.build.utils.Arch
import org.jetbrains.skiko.build.utils.OS
import org.jetbrains.skiko.build.utils.SkiaBuildType
import org.jetbrains.skiko.build.utils.SkikoArtifacts
import org.jetbrains.skiko.build.utils.SkikoProperties
import org.jetbrains.skiko.build.utils.hostOs

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
        all {
            compilerFlags("-DSK_SUPPORT_GPU=1", "-DSK_GANESH", "-DSK_GL")
        }
        jvm {
            macos {
                staticSkiaLibs("skia_graphite_ext")
                linkFlags("-lobjc")
                frameworks("CoreFoundation", "Foundation", "Metal")
                compilerFlags("-DSK_METAL")
            }
            linux {
                staticSkiaLibs("skia_graphite_ext")
                if (skiko.buildType == SkiaBuildType.DEBUG) {
                    staticSkiaLibs("spvtools", "spvtools_val")
                }
                dynamicSystemLibs("dl")
                compilerFlags("-DSK_VULKAN", "-DSK_USE_INTERNAL_VULKAN_HEADERS", "-DVK_NO_PROTOTYPES")
            }
            windows {
                staticSkiaLibs("skia_graphite_ext")
                if (skiko.buildType == SkiaBuildType.DEBUG) {
                    staticSkiaLibs("spvtools", "spvtools_val")
                }
                dynamicSystemLibs("onecore_apiset", "dxguid")
                compilerFlags(
                    "-DSK_DIRECT3D",
                    "-DSK_ANGLE",
                    "-DSK_VULKAN",
                    "-DSK_USE_INTERNAL_VULKAN_HEADERS",
                    "-DVK_NO_PROTOTYPES",
                )
            }
        }
        native {
            staticSkiaLibs("skia_graphite_ext")
            macos {
                frameworks("CoreFoundation", "Foundation", "Metal")
                compilerFlags("-DSK_METAL")
            }
            ios {
                frameworks("CoreFoundation", "Foundation", "Metal")
                compilerFlags("-DSK_METAL")
            }
            tvos {
                frameworks("CoreFoundation", "Foundation", "Metal")
                compilerFlags("-DSK_METAL")
            }
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
    applyHierarchyTemplate(skikoSourceSetHierarchyTemplate)

    if (supportAwt) {
        jvm("awt")
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
        api(project(":"))
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

configureKotlinCompilerOptions()

tasks.withType<AbstractTestTask> {
    testLogging {
        events("FAILED", "SKIPPED")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
        showStackTraces = true
    }
}
