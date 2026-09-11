@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.kotlin.dsl.withType
import org.gradle.kotlin.dsl.named
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

val skiko = SkikoProperties(rootProject)
val skikoRenderingArtifacts = SkikoArtifacts(
    artifactIdPrefix = "skiko-rendering",
    displayName = "Skiko Rendering",
    pomDescription = "Kotlin Skia rendering layer",
)
val buildType = skiko.buildType
val targetOs = hostOs
val targetArch = skiko.targetArch
val coreProject = project(":")

val renderingDependencies: SkikoDependencyScope.() -> Unit = {
    dependsOnCore()
    targets {
        jvm {
            macos {
                linkFlags("-lobjc")
                frameworks(
                    "AppKit",
                    "CoreFoundation",
                    "CoreGraphics",
                    "CoreServices",
                    "CoreText",
                    "Foundation",
                    "IOKit",
                    "Metal",
                    "OpenGL",
                    "QuartzCore",
                )
            }
            linux {
                dynamicSystemLibs("GL", "X11", "fontconfig")
                arm64 { dynamicSystemLibs("EGL") }
            }
        }
    }
}
val skikoRenderingProjectContext = SkikoProjectContext(
    project = project,
    skiko = skiko,
    kotlin = kotlin,
    kind = SkikoModuleKind.EXTENSION,
    artifacts = skikoRenderingArtifacts,
    windowsSdkPathProvider = {
        findWindowsSdkPaths(gradle, targetArch)
    },
    additionalRuntimeLibraries = project.registerAdditionalLibraries(
        targetOs,
        targetArch,
        skiko,
        skikoRenderingArtifacts,
    ),
    configureDependencies = renderingDependencies
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
        freeCompilerArgs.add(
            "-opt-in=org.jetbrains.skiko.InternalSkikoApi"
        )
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
            namespace = "org.jetbrains.skiko.rendering"
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
    tasks.register<Jar>("skikoWasmJar") {
        archiveBaseName.set("skiko-rendering-wasm")
    }

    js {
            outputModuleName.set("skiko-rendering-kjs")
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
                dependsOn(
                    test.compileTaskProvider,
                    tasks["compileTestKotlinWasmJs"],
                )
            }

        }

        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            outputModuleName.set("skiko-rendering-kjs-wasm")
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
                dependsOn(
                    test.compileTaskProvider,
                    tasks["compileTestKotlinJs"],
                )
            }

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
        implementation(libs.coroutines.core)
        /*
        We use compileOnly here because the root project publishes multiple artifacts
        which makes api/implementation(project(":")) fail during publishing.
        This avoids Gradle's multi-publication ambiguity but skiko core is NOT added
        as a transitive dependency of skiko-rendering, and it will NOT appear in the published POM
        consumers MUST explicitly depend on both:
            - implementation("org.jetbrains.skiko:skiko-x")
            - implementation("org.jetbrains.skiko:skiko-rendering-x")
         */
        compileOnly(project(":"))
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-annotations-common"))
        implementation(project(":"))
        implementation(project(":test-utils"))
    }

    skikoRenderingProjectContext.jvmMainSourceSet?.dependencies {
        implementation(kotlin("stdlib"))
        implementation(libs.coroutines.core.jvm)
    }

    skikoRenderingProjectContext.jvmTestSourceSet?.dependencies {
        implementation(libs.coroutines.test)
        implementation(kotlin("test-junit"))
        implementation(kotlin("test"))
    }

    skikoRenderingProjectContext.webMainSourceSet?.dependencies {
        implementation(libs.kotlinx.browser)
    }

    skikoRenderingProjectContext.awtMainSourceSet?.dependencies {
        implementation(libs.jetbrainsRuntime.api)
    }

    skikoRenderingProjectContext.androidMainSourceSet?.dependencies {
        implementation(libs.coroutines.android)
    }

    if (supportAndroid && supportAwt) {
        sourceSets.named("androidMain") {
            dependsOn(sourceSets.getByName("jvmMain"))
        }
    }

    skikoRenderingProjectContext.wasmJsTest?.dependencies {
        implementation(kotlin("test-wasm-js"))
    }
    skikoRenderingProjectContext.webTestSourceSet?.dependencies {
        implementation(libs.coroutines.core)
    }

    skikoRenderingProjectContext.webTestSourceSet?.apply {
        val coreWasmTestResources = skikoRenderingProjectContext.wasmTestResourcesFor().also {
            dependencies.add(it.name, coreProject)
        }
        resources.srcDirs(
            coreWasmTestResources,
        )
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

if (supportAndroid) {
    val os = OS.Android
    val skikoAndroidArtifact by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-rendering-android")
        from(kotlin.targets.getByName("android").compilations.getByName("main").output.allOutputs)
    }
    for (arch in arrayOf(Arch.X64, Arch.Arm64)) {
        val coreJvmLinkedLibrary = skikoRenderingProjectContext.jvmLinkedLibraryFor(os, arch).also {
            dependencies.add(it.name, coreProject)
        }
        skikoRenderingProjectContext.createSkikoJvmJarTask(
            os,
            arch,
            skikoAndroidArtifact,
            files(coreJvmLinkedLibrary)
        )
        skikoRenderingProjectContext.provideJvmRequiredSymbols(os, arch)
    }

    tasks.withType<JavaCompile>().configureEach {
        if (name.startsWith("compileAndroid") && name.endsWith("JavaWithJavac")) {
            sourceCompatibility = JavaVersion.VERSION_11.toString()
            targetCompatibility = JavaVersion.VERSION_11.toString()
        }
    }
}

if (supportAwt) {
    val skikoRenderingAwtJarForTests by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-rendering-awt-test")
        from(kotlin.jvm("awt").compilations["main"].output.allOutputs)
    }
    val coreJvmLinkedLibrary = skikoRenderingProjectContext.jvmLinkedLibraryFor(targetOs, targetArch).also {
        dependencies.add(it.name, coreProject)
    }
    val macosX64CoreLinkedLibrary = if (targetOs == OS.MacOS && targetArch == Arch.Arm64) {
        skikoRenderingProjectContext.jvmLinkedLibraryFor(OS.MacOS, Arch.X64).also {
            dependencies.add(it.name, coreProject)
        }
    } else {
        null
    }
    val coreJvmRuntimeJar = skikoRenderingProjectContext.jvmRuntimeJarFor(targetOs, targetArch).also {
        dependencies.add(it.name, coreProject)
    }
    skikoRenderingProjectContext.setupJvmTestTask(
        skikoRenderingAwtJarForTests,
        targetOs,
        targetArch,
        files(coreJvmLinkedLibrary),
        macosX64CoreLinkedLibrary?.let { files(it) },
        coreJvmRuntimeJar
    )
    skikoRenderingProjectContext.provideJvmRequiredSymbols(targetOs, targetArch)
    if (targetOs == OS.MacOS && targetArch == Arch.Arm64) {
        skikoRenderingProjectContext.provideJvmRequiredSymbols(OS.MacOS, Arch.X64)
    }
}

skikoRenderingProjectContext.declarePublications()

skikoRenderingProjectContext.additionalRuntimeLibraries.forEach {
    it.registerRuntimePublishTaskDependency(listOf("MavenLocal", "ComposeRepoRepository"))
}

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
