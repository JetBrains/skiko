@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.gradle.kotlin.dsl.withType
import tasks.configuration.*
import dsl.SkikoDependencyScope

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library") apply false
    org.jetbrains.dokka
    `maven-publish`
    signing
    org.gradle.crypto.checksum
}

if (supportAndroid) {
    apply(plugin = "com.android.kotlin.multiplatform.library")
}

apply<SideWasmImportsGeneratorPlugin>()

val skiko = SkikoProperties(rootProject)
val targetOs = hostOs
val targetArch = skiko.targetArch
val coreProject = project(":")
val ganeshArtifacts = SkikoArtifacts(
    artifactIdPrefix = "skiko-ganesh",
    displayName = "Skiko Ganesh",
    pomDescription = "Kotlin Skia Ganesh bindings",
)

val ganeshDependencies: SkikoDependencyScope.() -> Unit = {
    dependsOnCore()
    targets {
        all {
            staticSkiaLibs("skia_ganesh_ext")
            compilerFlags("-DSK_SUPPORT_GPU=1", "-DSK_GANESH", "-DSK_GL")
        }
        jvm {
            macos {
                linkFlags("-lobjc")
                frameworks("AppKit", "CoreFoundation", "CoreGraphics", "CoreServices", "CoreText", "Foundation", "IOKit", "Metal", "OpenGL", "QuartzCore")
                compilerFlags("-DSK_METAL")
            }
            windows {
                staticSkiaLibs("d3d12allocator")
                compilerFlags("-DSK_DIRECT3D", "-DSK_ANGLE")
                arm64 {
                    // The Ganesh ARM64 link reports "misaligned ldr/str offset" with ICF enabled.
                    linkFlags("/OPT:NOICF")
                }
            }
            linux {
                dynamicSystemLibs("GL")
                arm64 { dynamicSystemLibs("EGL") }
            }
            android {
                dynamicSystemLibs("GLESv3", "EGL")
            }
        }
        native {
            macos {
                frameworks("Metal", "CoreGraphics", "CoreText", "CoreServices")
                compilerFlags("-DSK_METAL")
            }
            ios {
                frameworks("Metal", "CoreGraphics", "CoreText", "UIKit")
                compilerFlags("-DSK_METAL")
            }
            tvos {
                frameworks("Metal", "CoreGraphics", "CoreText", "UIKit")
                compilerFlags("-DSK_METAL")
            }
            linux {
                dynamicSystemLibs("GL")
                arm64 { dynamicSystemLibs("EGL") }
            }
        }
        wasm {
            linkFlags("-l", "GL", "-s", "MAX_WEBGL_VERSION=2", "-s", "MIN_WEBGL_VERSION=2")
            linkFlags("-s", "SIDE_MODULE=2")
        }
    }
}

val ganeshProjectContext = SkikoProjectContext(
    project = project,
    skiko = skiko,
    kotlin = kotlin,
    kind = SkikoModuleKind.EXTENSION,
    artifacts = ganeshArtifacts,
    windowsSdkPathProvider = {
        findWindowsSdkPaths(gradle, targetArch)
    },
    additionalRuntimeLibraries = emptyList(),
    configureDependencies = ganeshDependencies,
)

repositories {
    mavenCentral {
        url = uri("https://cache-redirector.jetbrains.com/maven-central")
    }
    google()
}

kotlin {
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

    if (supportAndroid) {
        targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
            namespace = "org.jetbrains.skiko.ganesh"
            compileSdk = 35
            minSdk = 24
            withJava()
            withHostTest {}

            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }

    if (supportWeb) {
        ganeshProjectContext.declareWasmTasks()

        js {
            outputModuleName.set("skiko-ganesh-kjs")
            browser {
                testTask {
                    useKarma {
                        useChromeHeadless()
                        useConfigDirectory(rootProject.projectDir.resolve("karma.config.d").resolve("js"))
                    }
                }
            }
            binaries.executable()

            val test by compilations.getting
            project.tasks.named<Copy>(test.processResourcesTaskName) {
                dependsOn(test.compileTaskProvider, tasks["compileTestKotlinWasmJs"])
            }

            setupImportsGeneratorPlugin(
                ganeshArtifacts.artifactIdPrefix,
                isSideModule = ganeshProjectContext.kind == SkikoModuleKind.EXTENSION,
            )
        }

        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            outputModuleName.set("skiko-ganesh-kjs-wasm")
            browser {
                testTask {
                    useKarma {
                        useChromeHeadless()
                        useConfigDirectory(rootProject.projectDir.resolve("karma.config.d").resolve("wasm"))
                    }
                }
            }

            val test by compilations.getting
            project.tasks.named<Copy>(test.processResourcesTaskName) {
                dependsOn(test.compileTaskProvider, tasks["compileTestKotlinJs"])
            }

            setupImportsGeneratorPlugin(ganeshArtifacts.artifactIdPrefix, isSideModule = true)
        }
    }

    fun coreNativeSymbolSources(os: OS, arch: Arch, isUikitSim: Boolean) =
        ganeshProjectContext.nativeSymbolSourcesFor(os, arch, isUikitSim).also {
            dependencies.add(it.name, coreProject)
        }

    if (supportNativeMac) {
        ganeshProjectContext.configureNativeTarget(OS.MacOS, Arch.X64, macosX64(), ::coreNativeSymbolSources)
        ganeshProjectContext.configureNativeTarget(OS.MacOS, Arch.Arm64, macosArm64(), ::coreNativeSymbolSources)
    }
    if (supportNativeLinux) {
        ganeshProjectContext.configureNativeTarget(OS.Linux, Arch.X64, linuxX64(), ::coreNativeSymbolSources)
        ganeshProjectContext.configureNativeTarget(OS.Linux, Arch.Arm64, linuxArm64(), ::coreNativeSymbolSources)
    }
    if (supportNativeIosArm64) {
        ganeshProjectContext.configureNativeTarget(OS.IOS, Arch.Arm64, iosArm64(), ::coreNativeSymbolSources)
    }
    if (supportNativeIosSimulatorArm64) {
        ganeshProjectContext.configureNativeTarget(OS.IOS, Arch.Arm64, iosSimulatorArm64(), ::coreNativeSymbolSources)
    }
    if (supportNativeIosX64) {
        ganeshProjectContext.configureNativeTarget(OS.IOS, Arch.X64, iosX64(), ::coreNativeSymbolSources)
    }
    if (supportNativeTvosArm64) {
        ganeshProjectContext.configureNativeTarget(OS.TVOS, Arch.Arm64, tvosArm64(), ::coreNativeSymbolSources)
    }
    if (supportNativeTvosSimulatorArm64) {
        ganeshProjectContext.configureNativeTarget(OS.TVOS, Arch.Arm64, tvosSimulatorArm64(), ::coreNativeSymbolSources)
    }
    if (supportNativeTvosX64) {
        ganeshProjectContext.configureNativeTarget(OS.TVOS, Arch.X64, tvosX64(), ::coreNativeSymbolSources)
    }

    sourceSets.commonMain.dependencies {
        implementation(kotlin("stdlib"))
        compileOnly(coreProject)
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-annotations-common"))
        implementation(coreProject)
        implementation(project(":test-utils"))
    }

    ganeshProjectContext.jvmMainSourceSet?.dependencies {
        implementation(kotlin("stdlib"))
    }
    ganeshProjectContext.jvmTestSourceSet?.dependencies {
        implementation(kotlin("test-junit"))
        implementation(kotlin("test"))
        implementation(project(":test-utils"))
    }
    ganeshProjectContext.webMainSourceSet?.dependencies {
        implementation(libs.kotlinx.browser)
    }
    ganeshProjectContext.awtMainSourceSet?.dependencies {
        implementation(libs.jetbrainsRuntime.api)
    }
    ganeshProjectContext.wasmJsTest?.dependencies {
        implementation(kotlin("test-wasm-js"))
    }

    ganeshProjectContext.webTestSourceSet?.apply {
        val coreWasmTestResources = ganeshProjectContext.wasmTestResourcesFor().also {
            dependencies.add(it.name, coreProject)
        }
        resources.srcDirs(tasks.named("optimizeWasm"), wasmImports, coreWasmTestResources)
    }

    if (supportAndroid && supportAwt) {
        sourceSets.named("androidMain") {
            dependsOn(sourceSets.getByName("jvmMain"))
        }
    }

    if (supportAnyNative) {
        sourceSets.all {
            languageSettings.optIn("kotlin.native.SymbolNameIsInternal")
        }
        configureIOSTestsWithMetal(project)
    }
}

if (supportWeb) {
    ganeshProjectContext.provideWasmSideModules()
    ganeshProjectContext.provideWasmTestResources()
}

if (supportAndroid) {
    val os = OS.Android
    val ganeshAndroidArtifact by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-ganesh-android")
        from(kotlin.targets.getByName("android").compilations.getByName("main").output.allOutputs)
    }
    for (arch in arrayOf(Arch.X64, Arch.Arm64)) {
        val coreJvmLinkedLibrary = ganeshProjectContext.jvmLinkedLibraryFor(os, arch).also {
            dependencies.add(it.name, coreProject)
        }
        ganeshProjectContext.createSkikoJvmJarTask(os, arch, ganeshAndroidArtifact, files(coreJvmLinkedLibrary))
        ganeshProjectContext.provideJvmRequiredSymbols(os, arch)
    }

    tasks.withType<JavaCompile>().configureEach {
        if (name.startsWith("compileAndroid") && name.endsWith("JavaWithJavac")) {
            sourceCompatibility = JavaVersion.VERSION_11.toString()
            targetCompatibility = JavaVersion.VERSION_11.toString()
        }
    }
}

if (supportAwt) {
    val ganeshAwtJarForTests by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-ganesh-awt-test")
        from(kotlin.jvm("awt").compilations["main"].output.allOutputs)
    }
    val coreJvmLinkedLibrary = ganeshProjectContext.jvmLinkedLibraryFor(targetOs, targetArch).also {
        dependencies.add(it.name, coreProject)
    }
    val macosX64CoreLinkedLibrary = if (targetOs == OS.MacOS && targetArch == Arch.Arm64) {
        ganeshProjectContext.jvmLinkedLibraryFor(OS.MacOS, Arch.X64).also {
            dependencies.add(it.name, coreProject)
        }
    } else {
        null
    }
    val coreJvmRuntimeJar = ganeshProjectContext.jvmRuntimeJarFor(targetOs, targetArch).also {
        dependencies.add(it.name, coreProject)
    }

    ganeshProjectContext.setupJvmTestTask(
        ganeshAwtJarForTests,
        targetOs,
        targetArch,
        files(coreJvmLinkedLibrary),
        macosX64CoreLinkedLibrary?.let { files(it) },
        coreJvmRuntimeJar,
    )
    ganeshProjectContext.provideJvmRequiredSymbols(targetOs, targetArch)
    if (targetOs == OS.MacOS && targetArch == Arch.Arm64) {
        ganeshProjectContext.provideJvmRequiredSymbols(OS.MacOS, Arch.X64)
    }
}

ganeshProjectContext.declarePublications()

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

tasks.withType<AbstractTestTask> {
    testLogging {
        events("FAILED", "SKIPPED")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
        showStackTraces = true
    }
}