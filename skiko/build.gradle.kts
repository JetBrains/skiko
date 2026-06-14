@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.crypto.checksum.Checksum
import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.kotlin.dsl.withType
import tasks.configuration.*
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
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

apply<WasmImportsGeneratorCompilerPluginSupportPlugin>()
apply<WasmImportsGeneratorForTestCompilerPluginSupportPlugin>()

val skiko = SkikoProperties(rootProject)
val buildType = skiko.buildType
val targetOs = hostOs
val targetArch = skiko.targetArch
val skikoArtifacts = SkikoArtifacts()

val coreDependencies: SkikoDependencyScope.() -> Unit = {
    targets {
        all {
            staticSkiaLibs(
                "skia",
                "skia_ganesh_ext",
                "svg",
                "skparagraph",
                "skshaper",
                "skunicode_core",
                "skunicode_icu",
                "icu",
                "harfbuzz",
                "skresources",
                "png",
                "jpeg",
                "webp",
                "webp_sse41",
                "zlib",
                "expat",
            )
        }
        jvm {
            macos {
                staticSkiaLibs("piex", "dng_sdk")
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
                    "QuartzCore",  // for CoreAnimation
                )
            }

            windows {
                    staticSkiaLibs("d3d12allocator")
            }

            linux {
                // Hack to fix problem with linker not always finding certain declarations.
                directStaticSkiaLibs(
                    "skia",
                    "skia_ganesh_ext",
                    "skunicode_core",
                    "skunicode_icu",
                    "skshaper",
                )
                dynamicSystemLibs("GL", "X11", "fontconfig")
                arm64 { dynamicSystemLibs("EGL") }
            }

            android {
                // Hack to fix problem with linker not always finding certain declarations.
                directStaticSkiaLibs("skia", "skia_ganesh_ext")
                dynamicSystemLibs("GLESv3", "EGL")
            }
        }
        native {
            staticSkiaLibs(
                "piex",
                "dng_sdk",
            )

            linux {
                // Hack to fix problem with linker not always finding certain declarations.
                directStaticSkiaLibs(
                    "skshaper",
                    "skunicode_core",
                    "skunicode_icu",
                    "skia",
                    "skia_ganesh_ext"
                )
                dynamicSystemLibs("fontconfig", "GL")
                arm64 { dynamicSystemLibs("EGL") }
            }

            macos {
                frameworks(
                    "Metal",
                    "CoreGraphics",
                    "CoreText",
                    "CoreServices",
                )
            }

            ios {
                frameworks(
                    "Metal",
                    "CoreGraphics",
                    "CoreText",
                    "UIKit",
                )
            }

            tvos {
                frameworks(
                    "Metal",
                    "CoreGraphics",
                    "CoreText",
                    "UIKit",
                )
            }
        }
        wasm {
            staticSkiaLibs(
                "bentleyottmann",
                "freetype2",
                "jpeg12",
                "jpeg16",
                "wuffs",
                "skcms",
                "brotli",
            )
            linkFlags(
                "-l", "GL",
                "-s", "MAX_WEBGL_VERSION=2",
                "-s", "MIN_WEBGL_VERSION=2",
                "-s", "MODULARIZE=1",
                "-s", "EXPORT_NAME=loadSkikoWASM",
                "-s", "EXPORTED_RUNTIME_METHODS=\"[GL, wasmExports]\"",
                "--bind",
            )
        }
    }
}

val skikoProjectContext = SkikoProjectContext(
    project = project,
    skiko = skiko,
    kotlin = kotlin,
    kind = SkikoModuleKind.CORE,
    artifacts = skikoArtifacts,
    windowsSdkPathProvider = {
        findWindowsSdkPaths(gradle, targetArch)
    },
    createChecksumsTask = { targetOs: OS, targetArch: Arch, fileToChecksum: Provider<File> ->
        createChecksumsTask(targetOs, targetArch, fileToChecksum)
    },
    additionalRuntimeLibraries = project.registerAdditionalLibraries(targetOs, targetArch, skiko, skikoArtifacts),
    configureDependencies = coreDependencies
)

allprojects {
    group = SkikoArtifacts.DEFAULT_GROUP_ID
    version = skiko.deployVersion
}

repositories {
    mavenCentral {
        url = uri("https://cache-redirector.jetbrains.com/maven-central")
    }
    google()
}

kotlin {
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
        freeCompilerArgs.add(
            "-opt-in=org.jetbrains.skiko.InternalSkikoApi"
        )
    }

    applyHierarchyTemplate(skikoSourceSetHierarchyTemplate)
    skikoProjectContext.declareSkiaTasks()

    if (supportAwt) {
        jvm("awt") {
            compilations.all {
                compileTaskProvider.configure {
                    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
                }
            }
            generateVersion(targetOs, targetArch, skiko)
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
        skikoProjectContext.declareWasmTasks()

        js {
            outputModuleName.set("skiko-kjs") // override the name to avoid name collision with a different skiko.js file
            browser {
                testTask {
                    useKarma {
                        useChromeHeadless()
                        useConfigDirectory(project.projectDir.resolve("karma.config.d").resolve("js"))
                    }
                }
            }
            binaries.executable()
            generateVersion(OS.Wasm, Arch.Wasm, skiko)

            val test by compilations.getting

            project.tasks.named<Copy>(test.processResourcesTaskName) {
                dependsOn(test.compileTaskProvider, tasks["compileTestKotlinWasmJs"])
            }

            setupImportsGeneratorPlugin()
        }


        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            outputModuleName.set("skiko-kjs-wasm") // override the name to avoid name collision with a different skiko.js file
            browser {
                testTask {
                    useKarma {
                        useChromeHeadless()
                        useConfigDirectory(project.projectDir.resolve("karma.config.d").resolve("wasm"))
                    }
                }
            }
            generateVersion(OS.Wasm, Arch.Wasm, skiko)

            val test by compilations.getting

            project.tasks.named<Copy>(test.processResourcesTaskName) {
                dependsOn(test.compileTaskProvider, tasks["compileTestKotlinJs"])
            }

            setupImportsGeneratorPlugin()
        }
    }

    if (supportNativeMac) {
        skikoProjectContext.configureNativeTarget(OS.MacOS, Arch.X64, macosX64())
        skikoProjectContext.configureNativeTarget(OS.MacOS, Arch.Arm64, macosArm64())
    }
    if (supportNativeLinux) {
        skikoProjectContext.configureNativeTarget(OS.Linux, Arch.X64, linuxX64())
        skikoProjectContext.configureNativeTarget(OS.Linux, Arch.Arm64, linuxArm64())
    }
    if (supportNativeIosArm64) {
        skikoProjectContext.configureNativeTarget(OS.IOS, Arch.Arm64, iosArm64())
    }
    if (supportNativeIosSimulatorArm64) {
        skikoProjectContext.configureNativeTarget(OS.IOS, Arch.Arm64, iosSimulatorArm64())
    }
    if (supportNativeIosX64) {
        skikoProjectContext.configureNativeTarget(OS.IOS, Arch.X64, iosX64())
    }
    if (supportNativeTvosArm64) {
        skikoProjectContext.configureNativeTarget(OS.TVOS, Arch.Arm64, tvosArm64())
    }
    if (supportNativeTvosSimulatorArm64) {
        skikoProjectContext.configureNativeTarget(OS.TVOS, Arch.Arm64, tvosSimulatorArm64())
    }
    if (supportNativeTvosX64) {
        skikoProjectContext.configureNativeTarget(OS.TVOS, Arch.X64, tvosX64())
    }

    sourceSets.commonMain.dependencies {
        implementation(kotlin("stdlib"))
        implementation(libs.coroutines.core)
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-annotations-common"))
        implementation(project(":test-utils"))
    }

    skikoProjectContext.jvmMainSourceSet?.dependencies {
        implementation(kotlin("stdlib"))
        implementation(libs.coroutines.core.jvm)
    }

    skikoProjectContext.webMainSourceSet?.dependencies {
        implementation(libs.kotlinx.browser)
    }

    skikoProjectContext.awtMainSourceSet?.dependencies {
        implementation(libs.jetbrainsRuntime.api)
    }

    skikoProjectContext.awtTestSourceSet?.dependencies {
        implementation(libs.kotlinx.benchmark.runtime)
    }

    skikoProjectContext.androidMainSourceSet?.dependencies {
        implementation(libs.coroutines.android)
    }

    if (supportAndroid && supportAwt) {
        sourceSets.named("androidMain") {
            dependsOn(sourceSets.getByName("jvmMain"))
        }
    }

    skikoProjectContext.jvmTestSourceSet?.dependencies {
        implementation(libs.coroutines.test)
        implementation(kotlin("test-junit"))
        implementation(kotlin("test"))
    }

    skikoProjectContext.webTestSourceSet?.apply {
        resources.srcDirs(
            tasks.named("linkWasm"), wasmImports
        )
    }

    skikoProjectContext.wasmJsTest?.dependencies {
        implementation(kotlin("test-wasm-js"))
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

/**
 * Setup JVM benchmarks
 */
if (supportAwt) {
    benchmark {
        targets.register("awtTest")
    }

    /* Ensure that the benchmark task has the same classpath as the regular test task */
    tasks.withType<JavaExec>().named { it == "awtTestBenchmark" }.configureEach {
        classpath = project.files({ tasks.withType<Test>().named("awtTest").get().classpath })
    }
}


if (supportAndroid) {
    val os = OS.Android
    kotlin.targets.getByName("android").generateVersion(os, Arch.Arm64, skiko)
    val skikoAndroidArtifact by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-android")
        from(kotlin.targets.getByName("android").compilations.getByName("main").output.allOutputs)
    }
    for (arch in arrayOf(Arch.X64, Arch.Arm64)) {
        skikoProjectContext.createSkikoJvmJarTask(os, arch, skikoAndroidArtifact)
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
) = project.registerSkikoTask<Checksum>("createChecksums", targetOs, targetArch) {

    inputFiles = project.files(fileToChecksum)
    checksumAlgorithm = Checksum.Algorithm.SHA256
    outputDirectory = layout.buildDirectory.dir("checksums-${targetId(targetOs, targetArch)}")
}


if (supportAwt) {
    val skikoAwtJarForTests by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-awt-test")
        from(kotlin.jvm("awt").compilations["main"].output.allOutputs)
    }
    skikoProjectContext.setupJvmTestTask(skikoAwtJarForTests, targetOs, targetArch)
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

    tasks.named("clean").configure {
        doLast {
            delete(skiko.dependenciesDir)
            delete(project.file("src/jvmMain/java"))
        }
    }
}

fun configureSymbolsFor(os: OS, arch: Arch) {
    val suffix = joinToTitleCamelCase(os.id, arch.id)
    val skiaBindingsDir = skikoProjectContext.registerOrGetSkiaDirProvider(os, arch)
    val coreCompile = tasks.named<CompileSkikoCppTask>("compileJvmBindings$suffix")
    val coreObjcCompile = if (os.isMacOs) tasks.named<CompileSkikoObjCTask>("objcCompile$suffix") else null
    val requiredSymbolFiles = files(
        skikoProjectContext.jvmRequiredSymbolsFor(os, arch).also {
            dependencies.add(it.name, project(":skiko-skottie"))
        }
    )

    skikoProjectContext.configureGenerateSymbolsList(
        os, arch, skiaBindingsDir, coreCompile, coreObjcCompile, requiredSymbolFiles
    )

    tasks.named("linkJvmBindings$suffix") {
        dependsOn("generateSymbolsList$suffix")
    }
}

if (supportAwt) {
    afterEvaluate {
        configureSymbolsFor(targetOs, targetArch)

        if (targetOs == OS.MacOS && targetArch == Arch.Arm64) {
            configureSymbolsFor(OS.MacOS, Arch.X64)
        }
    }
}

if (supportAndroid) {
    afterEvaluate {
        for (arch in arrayOf(Arch.X64, Arch.Arm64)) {
            configureSymbolsFor(OS.Android, arch)
        }
    }
}

skikoProjectContext.declarePublications()

val mavenCentral = MavenCentralProperties(project)
if (skiko.isTeamcityCIBuild || mavenCentral.signArtifacts) {
    signing {
        sign(publishing.publications)
        useInMemoryPgpKeys(mavenCentral.signArtifactsKey.get(), mavenCentral.signArtifactsPassword.get())
    }
    configureSignAndPublishDependencies()
}

tasks.withType<AbstractTestTask> {
    testLogging {
        events("FAILED", "SKIPPED")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
        showStackTraces = true
    }
}

project.tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions.freeCompilerArgs.addAll(listOf(
        "-Xwasm-enable-array-range-checks", "-Xir-dce=true", "-Xskip-prerelease-check",
    ))
}

tasks.findByName("publishSkikoWasmRuntimePublicationToComposeRepoRepository")
    ?.dependsOn("publishWasmJsPublicationToComposeRepoRepository")
tasks.findByName("publishSkikoWasmRuntimePublicationToMavenLocal")
    ?.dependsOn("publishWasmJsPublicationToMavenLocal")

skikoProjectContext.additionalRuntimeLibraries.forEach {
    it.registerRuntimePublishTaskDependency(listOf("MavenLocal", "ComposeRepoRepository"))
}

// Local Skia build tasks
tasks.register<BuildLocalSkiaTask>("prepareLocalSkiaBuild") {
    group = "skia"
    description = "Build Skia binaries locally (without publishing Skiko)"

    skiaVersion.set(provider { skiko.skiaVersionFromEnvOrProperties })
    skiaTarget.set(provider { skiko.skiaTarget })
    buildType.set(skiko.buildType)

    val skiaRepoDir = skiko.skiaRepoDir
    if (skiaRepoDir != null) {
        this.skiaRepoDir.set(skiaRepoDir)
    } else {
        this.skiaRepoDir.set(project.file("skia"))
    }

    skikoTargetFlags.set(provider {
        skiko.skiaTarget.getGradleFlags(skiko.targetArch)
    })
}

tasks.register("printSkiaVersion") {
    group = "skia"
    description = "Print resolved Skia version"
    doLast {
        println(skiko.skiaVersionFromEnvOrProperties)
    }
}

tasks.withType<KotlinNativeCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
}
