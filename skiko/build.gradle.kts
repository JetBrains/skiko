@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.crypto.checksum.Checksum
import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import tasks.configuration.*

plugins {
    kotlin("multiplatform")
    org.jetbrains.dokka
    `maven-publish`
    signing
    org.gradle.crypto.checksum
    org.jetbrains.kotlinx.benchmark
}

if (supportAndroid) {
    apply<LibraryPlugin>()
}

apply<WasmImportsGeneratorCompilerPluginSupportPlugin>()
apply<WasmImportsGeneratorForTestCompilerPluginSupportPlugin>()

val skiko = SkikoProperties(rootProject)
val buildType = skiko.buildType
val targetOs = hostOs
val targetArch = skiko.targetArch

val skikoProjectContext = SkikoProjectContext(
    project = project,
    skiko = skiko,
    kotlin = kotlin,
    windowsSdkPathProvider = {
        findWindowsSdkPaths(gradle, targetArch)
    },
    createChecksumsTask = { targetOs: OS, targetArch: Arch, fileToChecksum: Provider<File> ->
        createChecksumsTask(targetOs, targetArch, fileToChecksum)
    },
    additionalRuntimeLibraries = project.registerAdditionalLibraries(targetOs, targetArch, skiko)
)

allprojects {
    group = SkikoArtifacts.groupId
    version = skiko.deployVersion
}

repositories {
    mavenCentral()
    google()
}

kotlin {
    applyHierarchyTemplate(skikoSourceSetHierarchyTemplate)
    skikoProjectContext.declareSkiaTasks()

    if (supportAwt) {
        jvm("awt") {
            compilations.all {
                compileTaskProvider.configure {
                    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
            generateVersion(targetOs, targetArch, skiko)
        }
    }

    if (supportAndroid) {
        androidTarget("android") {
            publishLibraryVariants("release")

            compilations.all {
                compileTaskProvider.configure {
                    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }

            // Keep the previously defined attribute that was used to distinguish JVM and android variant
            attributes {
                attributes.attribute(Attribute.of("ui", String::class.java), "android")
            }
            // TODO: seems incorrect.
            generateVersion(OS.Android, Arch.Arm64, skiko, "release")
        }
    }


    if (supportWeb) {
        skikoProjectContext.declareWasmTasks()

        js {
            moduleName = "skiko-kjs" // override the name to avoid name collision with a different skiko.js file
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
            moduleName = "skiko-kjs-wasm" // override the name to avoid name collision with a different skiko.js file
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
    }

    skikoProjectContext.jvmMainSourceSet?.dependencies {
        implementation(kotlin("stdlib"))
        implementation(libs.coroutines.core.jvm)
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
    // Android configuration, when available
    configure<LibraryExtension> {
        compileSdk = 33
        namespace = "org.jetbrains.skiko"

        defaultConfig.minSdk = 24
        defaultConfig.targetSdk = 24
        defaultConfig.javaCompileOptions

        compileOptions.sourceCompatibility = JavaVersion.VERSION_1_8
        compileOptions.targetCompatibility = JavaVersion.VERSION_1_8

        sourceSets.named("main") {
            java.srcDirs("src/androidMain/java")
            res.srcDirs("src/androidMain/res")
        }
    }

    val os = OS.Android
    val skikoAndroidJar by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-android")
        from(kotlin.androidTarget("android").compilations["release"].output.allOutputs)
    }
    for (arch in arrayOf(Arch.X64, Arch.Arm64)) {
        skikoProjectContext.createSkikoJvmJarTask(os, arch, skikoAndroidJar)
    }
    tasks.matching { name == "publishAndroidReleasePublicationToMavenLocal" }.configureEach {
        // It needs to be compatible with Gradle 8.1
        dependsOn(skikoAndroidJar)
    }
    tasks.matching { name == "generateMetadataFileForAndroidReleasePublication" }.configureEach {
        // It needs to be compatible with Gradle 8.1
        dependsOn(skikoAndroidJar)
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

tasks.withType<JavaCompile> {
    // Workaround to configure Java sources on Android (src/androidMain/java)
    targetCompatibility = "1.8"
    sourceCompatibility = "1.8"
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

tasks.withType<KotlinNativeCompile>().configureEach {
    // https://youtrack.jetbrains.com/issue/KT-56583
    compilerOptions.freeCompilerArgs.add("-XXLanguage:+ImplicitSignedToUnsignedIntegerConversion")
    compilerOptions.freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
}
