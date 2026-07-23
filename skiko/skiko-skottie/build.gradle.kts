@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.crypto.checksum.Checksum
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
    org.jetbrains.kotlinx.benchmark
}

if (supportAndroid) {
    apply(plugin = "com.android.kotlin.multiplatform.library")
}

apply<SideWasmImportsGeneratorPlugin>()

val skiko = SkikoProperties(rootProject)
val skikoSkottieArtifacts = SkikoArtifacts(
    artifactIdPrefix = "skiko-skottie",
    displayName = "Skiko Skottie",
    pomDescription = "Kotlin Skia Skottie bindings",
)
val buildType = skiko.buildType
val targetOs = hostOs
val targetArch = skiko.targetArch
val coreProject = project(":")

val skottieDependencies: SkikoDependencyScope.() -> Unit = {
    dependsOnCore()
    targets {
        all {
            staticSkiaLibs(
                "skottie",
                "sksg",
                "jsonreader"
            )
        }
    }
}
val skikoSkottieProjectContext = SkikoProjectContext(
    project = project,
    skiko = skiko,
    kotlin = kotlin,
    kind = SkikoModuleKind.EXTENSION,
    artifacts = skikoSkottieArtifacts,
    windowsSdkPathProvider = {
        findWindowsSdkPaths(gradle, targetArch)
    },
    createChecksumsTask = { targetOs: OS, targetArch: Arch, fileToChecksum: Provider<File> ->
        createChecksumsTask(targetOs, targetArch, fileToChecksum)
    },
    additionalRuntimeLibraries = emptyList(),
    configureDependencies = skottieDependencies
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
            namespace = "org.jetbrains.skiko"
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
        skikoSkottieProjectContext.declareWasmTasks()

        js {
            outputModuleName.set("skiko-skottie-kjs")
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

            setupImportsGeneratorPlugin(skikoSkottieArtifacts.artifactIdPrefix, isSideModule = skikoSkottieProjectContext.kind == SkikoModuleKind.EXTENSION)
        }

        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            outputModuleName.set("skiko-skottie-kjs-wasm")
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

            setupImportsGeneratorPlugin(skikoSkottieArtifacts.artifactIdPrefix, isSideModule = true)
        }
    }

    fun coreNativeSymbolSources(os: OS, arch: Arch, isUikitSim: Boolean) =
        skikoSkottieProjectContext.nativeSymbolSourcesFor(os, arch, isUikitSim).also {
            dependencies.add(it.name, coreProject)
        }

    if (supportNativeMac) {
        skikoSkottieProjectContext.configureNativeTarget(OS.MacOS, Arch.X64, macosX64(), ::coreNativeSymbolSources)
        skikoSkottieProjectContext.configureNativeTarget(OS.MacOS, Arch.Arm64, macosArm64(), ::coreNativeSymbolSources)
    }

    if (supportNativeLinux) {
        skikoSkottieProjectContext.configureNativeTarget(OS.Linux, Arch.X64, linuxX64(), ::coreNativeSymbolSources)
        skikoSkottieProjectContext.configureNativeTarget(OS.Linux, Arch.Arm64, linuxArm64(), ::coreNativeSymbolSources)
    }

    if (supportNativeIosArm64) {
        skikoSkottieProjectContext.configureNativeTarget(OS.IOS, Arch.Arm64, iosArm64(), ::coreNativeSymbolSources)
    }

    if (supportNativeIosSimulatorArm64) {
        skikoSkottieProjectContext.configureNativeTarget(OS.IOS, Arch.Arm64, iosSimulatorArm64(), ::coreNativeSymbolSources)
    }

    if (supportNativeIosX64) {
        skikoSkottieProjectContext.configureNativeTarget(OS.IOS, Arch.X64, iosX64(), ::coreNativeSymbolSources)
    }

    if (supportNativeTvosArm64) {
        skikoSkottieProjectContext.configureNativeTarget(OS.TVOS, Arch.Arm64, tvosArm64(), ::coreNativeSymbolSources)
    }

    if (supportNativeTvosSimulatorArm64) {
        skikoSkottieProjectContext.configureNativeTarget(OS.TVOS, Arch.Arm64, tvosSimulatorArm64(), ::coreNativeSymbolSources)
    }

    if (supportNativeTvosX64) {
        skikoSkottieProjectContext.configureNativeTarget(OS.TVOS, Arch.X64, tvosX64(), ::coreNativeSymbolSources)
    }

    sourceSets.commonMain.dependencies {
        implementation(kotlin("stdlib"))
        /*
        We use compileOnly here because the root project publishes multiple artifacts
        which makes api/implementation(project(":")) fail during publishing.
        This avoids Gradle's multi-publication ambiguity but skiko core is NOT added
        as a transitive dependency of skiko-skottie, and it will NOT appear in the published POM
        consumers MUST explicitly depend on both:
            - implementation("org.jetbrains.skiko:skiko-x")
            - implementation("org.jetbrains.skiko:skiko-skottie-x")
         */
        compileOnly(project(":"))
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-annotations-common"))
        implementation(project(":"))
        implementation(project(":test-utils"))
    }

    skikoSkottieProjectContext.jvmMainSourceSet?.dependencies {
        implementation(kotlin("stdlib"))
    }

    skikoSkottieProjectContext.jvmTestSourceSet?.dependencies {
        implementation(libs.coroutines.test)
        implementation(kotlin("test-junit"))
        implementation(kotlin("test"))
    }

    skikoSkottieProjectContext.awtTestSourceSet?.dependencies {
        implementation(libs.kotlinx.benchmark.runtime)
    }
    skikoSkottieProjectContext.webMainSourceSet?.dependencies {
        implementation(libs.kotlinx.browser)
    }

    skikoSkottieProjectContext.awtMainSourceSet?.dependencies {
        implementation(libs.jetbrainsRuntime.api)
    }

    skikoSkottieProjectContext.androidMainSourceSet?.dependencies {
        implementation(libs.coroutines.android)
    }

    if (supportAndroid && supportAwt) {
        sourceSets.named("androidMain") {
            dependsOn(sourceSets.getByName("jvmMain"))
        }
    }

    skikoSkottieProjectContext.wasmJsTest?.dependencies {
        implementation(kotlin("test-wasm-js"))
    }
    skikoSkottieProjectContext.webTestSourceSet?.dependencies {
        implementation(libs.coroutines.core)
    }

    skikoSkottieProjectContext.webTestSourceSet?.apply {
        val coreWasmTestResources = skikoSkottieProjectContext.wasmTestResourcesFor().also {
            dependencies.add(it.name, coreProject)
        }
        resources.srcDirs(
            tasks.named("linkWasm"),
            wasmImports,
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

if (supportWeb) {
    skikoSkottieProjectContext.provideWasmSideModules()
}

if (supportAndroid) {
    val os = OS.Android
    val skikoAndroidArtifact by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-skottie-android")
        from(kotlin.targets.getByName("android").compilations.getByName("main").output.allOutputs)
    }
    for (arch in arrayOf(Arch.X64, Arch.Arm64)) {
        val coreJvmLinkedLibrary = skikoSkottieProjectContext.jvmLinkedLibraryFor(os, arch).also {
            dependencies.add(it.name, coreProject)
        }
        skikoSkottieProjectContext.createSkikoJvmJarTask(
            os,
            arch,
            skikoAndroidArtifact,
            files(coreJvmLinkedLibrary)
        )
        skikoSkottieProjectContext.provideJvmRequiredSymbols(os, arch)
    }

    tasks.withType<JavaCompile>().configureEach {
        if (name.startsWith("compileAndroid") && name.endsWith("JavaWithJavac")) {
            sourceCompatibility = JavaVersion.VERSION_11.toString()
            targetCompatibility = JavaVersion.VERSION_11.toString()
        }
    }
}

// TODO now it can be moved, move it if you change this
// Can't be moved to buildSrc because of Checksum dependency
fun createChecksumsTask(
    targetOs: OS,
    targetArch: Arch,
    fileToChecksum: Provider<File>
) = project.registerSkikoTask<org.gradle.crypto.checksum.Checksum>("createChecksums", targetOs, targetArch) {
    inputFiles = project.files(fileToChecksum)
    checksumAlgorithm = org.gradle.crypto.checksum.Checksum.Algorithm.SHA256
    outputDirectory = layout.buildDirectory.dir("checksums-${targetId(targetOs, targetArch)}")
}

if (supportAwt) {
    val skikoSkottieAwtJarForTests by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-skottie-awt-test")
        from(kotlin.jvm("awt").compilations["main"].output.allOutputs)
    }
    val coreJvmLinkedLibrary = skikoSkottieProjectContext.jvmLinkedLibraryFor(targetOs, targetArch).also {
        dependencies.add(it.name, coreProject)
    }
    val macosX64CoreLinkedLibrary = if (targetOs == OS.MacOS && targetArch == Arch.Arm64) {
        skikoSkottieProjectContext.jvmLinkedLibraryFor(OS.MacOS, Arch.X64).also {
            dependencies.add(it.name, coreProject)
        }
    } else {
        null
    }
    val coreJvmRuntimeJar = skikoSkottieProjectContext.jvmRuntimeJarFor(targetOs, targetArch).also {
        dependencies.add(it.name, coreProject)
    }

    skikoSkottieProjectContext.setupJvmTestTask(
        skikoSkottieAwtJarForTests,
        targetOs,
        targetArch,
        files(coreJvmLinkedLibrary),
        macosX64CoreLinkedLibrary?.let { files(it) },
        coreJvmRuntimeJar
    )
    skikoSkottieProjectContext.provideJvmRequiredSymbols(targetOs, targetArch)
    if (targetOs == OS.MacOS && targetArch == Arch.Arm64) {
        skikoSkottieProjectContext.provideJvmRequiredSymbols(OS.MacOS, Arch.X64)
    }
}

afterEvaluate {
    tasks.configureEach {
        if (group == "publishing") {
            // There are many intermediate tasks in 'publishing' group.
            // There are a lot of them and they have verbose names.
            // To decrease noise in './gradlew tasks' output and Intellij Gradle tool window,
            // group verbose tasks in a separate group 'other publishing'.
            val allRepositories = publishing.repositories.map { it.name } + "MavenLocal"
            val publishToTasks = allRepositories.map { "publishTo$it" }
            if (name != "publish" && name !in publishToTasks) {
                group = "other publishing"
            }
        }
    }
}

skikoSkottieProjectContext.declarePublications()

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
