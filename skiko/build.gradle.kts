import de.undercouch.gradle.tasks.download.Download
import org.gradle.crypto.checksum.Checksum
import org.gradle.api.tasks.testing.AbstractTestTask
import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("multiplatform") version "1.5.31"
    `maven-publish`
    signing
    id("org.gradle.crypto.checksum") version "1.1.0"
    id("de.undercouch.download") version "4.1.2"
}

val coroutinesVersion = "1.5.2"

fun targetSuffix(os: OS, arch: Arch): String {
    return "${os.id}_${arch.id}"
}

val skiko = SkikoProperties(rootProject)
val buildType = skiko.buildType
val generatedKotlin = "$buildDir/kotlin/commonMain"

allprojects {
    group = "org.jetbrains.skiko"
    version = skiko.deployVersion
}

repositories {
    mavenCentral()
}

val windowsSdkPaths: WindowsSdkPaths by lazy {
    findWindowsSdkPathsForCurrentOS(gradle)
}

if (supportWasm) {
    val skiaWasmDir = registerOrGetSkiaDirProvider(OS.Wasm, Arch.Wasm)

    val compileWasm by tasks.registering(CompileSkikoCppTask::class) {
        val osArch = OS.Wasm to Arch.Wasm

        dependsOn(skiaWasmDir)

        compiler.set(compilerForTarget(OS.Wasm, Arch.Wasm))
        buildTargetOS.set(osArch.first)
        buildTargetArch.set(osArch.second)
        buildVariant.set(buildType)

        val srcDirs = projectDirs("src/commonMain/cpp/common", "src/jsMain/cpp", "src/nativeJsMain/cpp") +
                if (skiko.includeTestHelpers) projectDirs("src/nativeJsTest/cpp") else emptyList()
        sourceRoots.set(srcDirs)

        includeHeadersNonRecursive(projectDir.resolve("src/nativeJsMain/cpp"))
        includeHeadersNonRecursive(projectDir.resolve("src/commonMain/cpp/common/include"))
        includeHeadersNonRecursive(skiaHeadersDirs(skiaWasmDir.get()))

        flags.set(listOf(
            *skiaPreprocessorFlags(),
            *buildType.clangFlags,
            "-DSKIKO_WASM",
            "-fno-rtti",
            "-fno-exceptions"
        ))
    }

    val linkWasm by tasks.registering(LinkSkikoWasmTask::class) {
        val osArch = OS.Wasm to Arch.Wasm

        dependsOn(compileWasm)
        dependsOn(skiaWasmDir)
        val unpackedSkia = skiaWasmDir.get()

        linker.set(linkerForTarget(OS.Wasm, Arch.Wasm))
        buildTargetOS.set(osArch.first)
        buildTargetArch.set(osArch.second)
        buildVariant.set(buildType)

        libFiles = project.fileTree(unpackedSkia)  { include("**/*.a") }
        objectFiles = project.fileTree(compileWasm.map { it.outDir.get() }) {
            include("**/*.o")
        }

        libOutputFileName.set("skiko.wasm")
        jsOutputFileName.set("skiko.js")

        skikoJsPrefix.set(project.layout.projectDirectory.file("src/jsMain/resources/setup.js"))

        flags.set(listOf(
            "-l", "GL",
            "-s", "USE_WEBGL2=1",
            "-s", "OFFSCREEN_FRAMEBUFFER=1",
            "-s", "ALLOW_MEMORY_GROWTH=1", // TODO: Is there a better way? Should we use `-s INITIAL_MEMORY=X`?
            "--bind",
        ))

        doLast {
            // skiko.js file is directly referenced in karma.config.d/wasm.js
            // so symbols must be replaced right after linking
            val jsFiles = outDir.asFile.get().walk()
                .filter { it.isFile && it.name.endsWith(".js") }

            for (jsFile in jsFiles) {
                val originalContent = jsFile.readText()
                val newContent = originalContent.replace("_org_jetbrains", "org_jetbrains")
                jsFile.writeText(newContent)
            }
        }
    }

    val skikoWasmJar by project.tasks.registering(Jar::class) {
        dependsOn(linkWasm)
        // We produce jar that contains .js of wrapper/bindings and .wasm with Skia + bindings.
        val wasmOutDir = linkWasm.map { it.outDir }

        from(wasmOutDir) {
            include("*.wasm")
            include("*.js")
        }

        archiveBaseName.set("skiko-wasm")
        doLast {
            println("Wasm and JS at: ${archiveFile.get().asFile.absolutePath}")
        }
    }
}

fun compileNativeBridgesTask(os: OS, arch: Arch): TaskProvider<CompileSkikoCppTask> {
    val skiaNativeDir = registerOrGetSkiaDirProvider(os, arch)

    return tasks.register<CompileSkikoCppTask>("${os.id}_${arch.id}_CrossCompile") {
        dependsOn(skiaNativeDir)
        val unpackedSkia = skiaNativeDir.get()

        compiler.set(compilerForTarget(os, arch))
        buildTargetOS.set(os)
        buildTargetArch.set(arch)
        buildVariant.set(buildType)

        when (os)  {
            OS.IOS -> {
                val sdkRoot = "/Applications/Xcode.app/Contents/Developer/Platforms"
                val iosArchFlags = when (arch) {
                    Arch.Arm64 -> arrayOf(
                        "-target", "arm64-apple-ios",
                        "-isysroot", "$sdkRoot/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk"
                    )
                    Arch.X64 -> arrayOf(
                        "-target", "x86_64-apple-ios-simulator",
                        "-isysroot", "$sdkRoot/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk"
                    )
                    else -> throw GradleException("Unsupported arch: $arch")
                }
                flags.set(listOf(
                    *iosArchFlags,
                    *buildType.clangFlags,
                    "-stdlib=libc++",
                    "-DSK_SHAPER_CORETEXT_AVAILABLE",
                    "-DSK_BUILD_FOR_IOS",
                    "-DSK_METAL",
                    *skiaPreprocessorFlags(),
                ))
            }
            OS.MacOS -> {
                flags.set(listOf(
                    *buildType.clangFlags,
                    "-DSK_SHAPER_CORETEXT_AVAILABLE",
                    "-DSK_BUILD_FOR_MAC",
                    "-DSK_METAL",
                    *skiaPreprocessorFlags()
                ))
            }
            OS.Linux -> {
                flags.set(listOf(
                    *buildType.clangFlags,
                    "-fno-rtti",
                    "-fno-exceptions",
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    "-DSK_BUILD_FOR_LINUX",
                    "-D_GLIBCXX_USE_CXX11_ABI=0",
                    *skiaPreprocessorFlags()
                ))
            }
            else -> throw GradleException("$os not yet supported")
        }

        val srcDirs = projectDirs("src/commonMain/cpp/common", "src/nativeNativeJs/cpp", "src/nativeJsMain/cpp") +
                if (skiko.includeTestHelpers) projectDirs("src/nativeJsTest/cpp") else emptyList()
        sourceRoots.set(srcDirs)

        includeHeadersNonRecursive(projectDir.resolve("src/nativeJsMain/cpp"))
        includeHeadersNonRecursive(projectDir.resolve("src/commonMain/cpp/common/include"))
        includeHeadersNonRecursive(skiaHeadersDirs(unpackedSkia))
    }
}

internal val Project.isInIdea: Boolean
    get() {
        return System.getProperty("idea.active")?.toBoolean() == true
    }

val Project.supportNative: Boolean
   get() = findProperty("skiko.native.enabled") == "true" || isInIdea

val Project.supportWasm: Boolean
    get() = findProperty("skiko.wasm.enabled") == "true" || isInIdea

val Project.supportAndroid: Boolean
    get() = findProperty("skiko.android.enabled") == "true" || isInIdea

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

    if (supportWasm) {
        js(IR) {
            browser() {
                testTask {
                    dependsOn("linkWasm")
                    useKarma {
                        useChromeHeadless()
                    }
                }
            }
            binaries.executable()
        }
    }

    if (supportNative) {
        configureNativeTarget(OS.MacOS, Arch.X64, macosX64())
        configureNativeTarget(OS.MacOS, Arch.Arm64, macosArm64())
        configureNativeTarget(OS.Linux, Arch.X64, linuxX64())
        configureNativeTarget(OS.IOS, Arch.Arm64, iosArm64())
        configureNativeTarget(OS.IOS, Arch.X64, iosX64())
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutinesVersion")
                if (supportAndroid) implementation(files(androidJar()))
            }

        }
        val jvmTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
                implementation(kotlin("test-junit"))
                implementation(kotlin("test"))
            }
        }

        if (supportWasm || supportNative) {
            val nativeJsMain by creating {
                dependsOn(commonMain)
            }

            val nativeJsTest by creating {
                dependsOn(commonTest)
            }

            if (supportWasm) {
                val jsMain by getting {
                    dependsOn(nativeJsMain)
                }

                val jsTest by getting {
                    dependsOn(nativeJsTest)
                    dependencies {
                        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                        implementation(kotlin("test-js"))
                    }
                }
            }

            if (supportNative) {
                // See https://kotlinlang.org/docs/mpp-share-on-platforms.html#configure-the-hierarchical-structure-manually
                val nativeMain by creating {
                    dependsOn(nativeJsMain)
                    dependencies {
                        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                    }
                }
                val nativeTest by creating {
                    dependsOn(nativeJsTest)
                }
                val linuxMain by creating {
                    dependsOn(nativeMain)
                }
                val linuxTest by creating {
                    dependsOn(nativeTest)
                }
                val linuxX64Main by getting {
                    dependsOn(linuxMain)
                }
                val linuxX64Test by getting {
                    dependsOn(linuxTest)
                }
                val darwinMain by creating {
                    dependsOn(nativeMain)
                }
                val darwinTest by creating {
                    dependsOn(nativeTest)
                }
                val macosMain by creating {
                    dependsOn(darwinMain)
                }
                val macosTest by creating {
                    dependsOn(darwinTest)
                }
                val iosMain by creating {
                    dependsOn(darwinMain)
                }
                val iosTest by creating {
                    dependsOn(darwinTest)
                }
                val macosX64Main by getting {
                    dependsOn(macosMain)
                }
                val macosX64Test by getting {
                    dependsOn(macosTest)
                }
                val macosArm64Main by getting {
                    dependsOn(macosMain)
                }
                val macosArm64Test by getting {
                    dependsOn(macosTest)
                }
                val iosX64Main by getting {
                    dependsOn(iosMain)
                }
                val iosX64Test by getting {
                    dependsOn(iosTest)
                }
                val iosArm64Main by getting {
                    dependsOn(iosMain)
                }
                val iosArm64Test by getting {
                    dependsOn(iosTest)
                }
            }
        }
    }
}

fun configureNativeTarget(os: OS, arch: Arch, target: KotlinNativeTarget) {
    if (!os.isCompatibleWithHost) return

    val targetString = "${os.id}-${arch.id}"

    val unzipper = registerOrGetSkiaDirProvider(os, arch)
    val unpackedSkia = unzipper.get()
    val skiaDir = unpackedSkia.absolutePath

    val bridgesLibrary = "$buildDir/nativeBridges/static/$targetString/skiko-native-bridges-$targetString.a"
    val allLibraries = skiaStaticLibraries(skiaDir, targetString) + bridgesLibrary

    target.compilations.all {
        val skiaBinDir = "$skiaDir/out/${buildType.id}-$targetString"
        this.
        kotlinOptions {
            val linkerFlags = when (os) {
                OS.MacOS -> mutableListOf("-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics",
                    "-linker-option", "-framework", "-linker-option", "CoreText",
                    "-linker-option", "-framework", "-linker-option", "CoreServices"
                )
                OS.IOS -> mutableListOf("-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics",
                    "-linker-option", "-framework", "-linker-option", "UIKit",
                    "-linker-option", "-framework", "-linker-option", "CoreText")
                OS.Linux -> mutableListOf(
                    "-linker-option", "-L/usr/lib/x86_64-linux-gnu",
                    "-linker-option", "-lfontconfig",
                    "-linker-option", "-lGL",
                    // TODO: an ugly hack, Linux linker searches only unresolved symbols.
                    "-linker-option", "$skiaBinDir/libskshaper.a",
                    "-linker-option", "$skiaBinDir/libskunicode.a",
                    "-linker-option", "$skiaBinDir/libskia.a"
                )
                else -> mutableListOf()
            }
            if (skiko.includeTestHelpers) {
                linkerFlags.addAll(when (os) {
                    OS.Linux -> listOf(
                        "-linker-option", "-lX11",
                        "-linker-option", "-lGLX",
                    )
                    else -> emptyList()
                })
            }
            freeCompilerArgs = allLibraries.map { listOf("-include-binary", it) }.flatten() + linkerFlags
        }
    }

    val crossCompileTask = compileNativeBridgesTask(os, arch)

    val linkTask = project.tasks.register<Exec>("linkNativeBridges$targetString") {
        dependsOn(crossCompileTask)
        val objectFilesDir = crossCompileTask.map { it.outDir.get() }
        val objectFiles = project.fileTree(objectFilesDir) {
            include("**/*.o")
        }
        inputs.files(objectFiles)
        val outDir = "$buildDir/nativeBridges/static/$targetString"
        val staticLib = "$outDir/skiko-native-bridges-$targetString.a"
        workingDir = File(outDir)
        when (os) {
            OS.Linux -> {
                executable = "ar"
                argumentProviders.add { listOf("-crs", staticLib) }
            }
            OS.MacOS, OS.IOS -> {
                executable = "libtool"
                argumentProviders.add { listOf("-static", "-o", staticLib) }
            }
            else -> error("Unexpected OS for native bridges linking: $os")
        }
        argumentProviders.add { objectFiles.files.map { it.absolutePath } }
        file(outDir).mkdirs()
        outputs.dir(outDir)
    }
    target.compilations.all {
        compileKotlinTask.dependsOn(linkTask)
    }
}

tasks.withType(JavaCompile::class.java).configureEach {
    this.getOptions().compilerArgs.addAll(listOf("-source", "11", "-target", "11"))
}

fun skiaHeadersDirs(skiaDir: File): List<File> =
    listOf(
        skiaDir,
        skiaDir.resolve("include"),
        skiaDir.resolve("include/core"),
        skiaDir.resolve("include/gpu"),
        skiaDir.resolve("include/effects"),
        skiaDir.resolve("include/pathops"),
        skiaDir.resolve("include/utils"),
        skiaDir.resolve("include/codec"),
        skiaDir.resolve("include/svg"),
        skiaDir.resolve("modules/skottie/include"),
        skiaDir.resolve("modules/skparagraph/include"),
        skiaDir.resolve("modules/skshaper/include"),
        skiaDir.resolve("modules/sksg/include"),
        skiaDir.resolve("modules/svg/include"),
        skiaDir.resolve("third_party/externals/harfbuzz/src"),
        skiaDir.resolve("third_party/icu"),
        skiaDir.resolve("third_party/externals/icu/source/common"),
    )

fun includeHeadersFlags(headersDirs: List<File>) =
    headersDirs.map { "-I${it.absolutePath}" }.toTypedArray()

fun skiaPreprocessorFlags(): Array<String> {
    return listOf(
        "-DSK_ALLOW_STATIC_GLOBAL_INITIALIZERS=1",
        "-DSK_FORCE_DISTANCE_FIELD_TEXT=0",
        "-DSK_GAMMA_APPLY_TO_A8",
        "-DSK_GAMMA_SRGB",
        "-DSK_SCALAR_TO_FLOAT_EXCLUDED",
        "-DSK_SUPPORT_GPU=1",
        "-DSK_GL",
        "-DSK_SHAPER_HARFBUZZ_AVAILABLE",
        "-DSK_UNICODE_AVAILABLE",
        "-DSK_SUPPORT_OPENCL=0",
        "-DSK_UNICODE_AVAILABLE",
        "-DU_DISABLE_RENAMING",
        "-DSK_USING_THIRD_PARTY_ICU",
        *buildType.flags
    ).toTypedArray()
}

fun skiaStaticLibraries(skiaDir: String, targetString: String): List<String> {
    val skiaBinSubdir = "$skiaDir/out/${buildType.id}-$targetString"
    return listOf(
        "libskresources.a",
        "libparticles.a",
        "libskparagraph.a",
        "libskia.a",
        "libicu.a",
        "libskottie.a",
        "libsvg.a",
        "libpng.a",
        "libfreetype2.a",
        "libwebp_sse41.a",
        "libsksg.a",
        "libskunicode.a",
        "libwebp.a",
        "libdng_sdk.a",
        "libpiex.a",
        "libharfbuzz.a",
        "libexpat.a",
        "libzlib.a",
        "libjpeg.a",
        "libskshaper.a"
    ).map{
        "$skiaBinSubdir/$it"
    }
}

val skiaJvmBindingsDir: Provider<File> = registerOrGetSkiaDirProvider(targetOs, targetArch)

val compileJvmBindings = createCompileJvmBindingsTask(targetOs, targetArch, skiaJvmBindingsDir)
val linkJvmBindings = createLinkJvmBindings(targetOs, targetArch, skiaJvmBindingsDir, compileJvmBindings)
val maybeSign = maybeSignTask(targetOs, targetArch, linkJvmBindings)
val createChecksums =  createChecksumsTask(targetOs, targetArch, maybeSign)
val skikoJvmRuntimeJar = skikoJvmRuntimeJarTask(targetOs, targetArch, maybeSign, createChecksums)
val skikoRuntimeDirForTests = skikoRuntimeDirForTestsTask(targetOs, targetArch, skikoJvmRuntimeJar)

val allJvmRuntimeJars = mutableListOf(skikoJvmRuntimeJar)

if (supportAndroid) {
    val os = OS.Android
    for (arch in arrayOf(Arch.X64, Arch.Arm64)) {
        val skiaAndroidBindingsDir = registerOrGetSkiaDirProvider(os, arch)
        val compileAndroidBindings = createCompileJvmBindingsTask(os, arch, skiaAndroidBindingsDir)
        val linkAndroidBindings =
            createLinkJvmBindings(OS.Android, arch, skiaAndroidBindingsDir, compileAndroidBindings)
        val maybeSignAndroid = maybeSignTask(os, arch, linkAndroidBindings)
        val createChecksumsAndroid =  createChecksumsTask(os, arch, maybeSignAndroid)
        val skikoJvmRuntimeJarAndroid = skikoJvmRuntimeJarTask(os, arch, maybeSignAndroid, createChecksumsAndroid)
        allJvmRuntimeJars += skikoJvmRuntimeJarAndroid
    }
}

fun androidHome() = when (hostOs) {
    OS.MacOS -> File("${System.getProperty("user.home")}/Library/Android/sdk")
    OS.Linux -> File("${System.getProperty("user.home")}/.android")
    else -> throw GradleException("unsupported $hostOs")
}

fun androidClangFor(targetArch: Arch, version: String = "30"): String {
    val androidHome = androidHome()
    val ndkVersion =
        arrayOf("ndk/23.0.7599858", "ndk-bundle").find {
            androidHome.resolve(it).exists()
        }!!
    val androidArch = when (targetArch) {
        Arch.Arm64 -> "aarch64"
        Arch.X64 -> "x86_64"
        else -> throw GradleException("unsupported $targetArch")
    }
    val hostOsArch = when (hostOs) {
        OS.MacOS -> "darwin-x86_64"
        OS.Linux -> "linux-x86_64"
        else -> throw GradleException("unsupported $hostOs")
    }
    val ndkDir = File(androidHome, "/$ndkVersion/toolchains/llvm/prebuilt/$hostOsArch")
    return ndkDir.resolve("bin/$androidArch-linux-android$version-clang++").absolutePath
}

fun androidJar(version: String = "30"): String {
    val androidHome = androidHome()
    return androidHome.resolve("platforms/android-$version/android.jar").absolutePath
}

fun createCompileJvmBindingsTask(targetOs: OS, targetArch: Arch, skiaJvmBindingsDir: Provider<File>) =
    tasks.register<CompileSkikoCppTask>("compileJvmBindings-${targetOs.id}-${targetArch.id}") {
    // Prefer 'java.home' system property to simplify overriding from Intellij.
    // When used from command-line, it is effectively equal to JAVA_HOME.
    if (JavaVersion.current() < JavaVersion.VERSION_11) {
        error("JDK 11+ is required, but Gradle JVM is ${JavaVersion.current()}. " +
                "Check JAVA_HOME (CLI) or Gradle settings (Intellij).")
    }
    val jdkHome = File(System.getProperty("java.home") ?: error("'java.home' is null"))
    dependsOn(skiaJvmBindingsDir)
    buildTargetOS.set(targetOs)
    buildTargetArch.set(targetArch)
    buildVariant.set(buildType)

    val srcDirs = projectDirs(
        "src/commonMain/cpp/common",
        "src/jvmMain/cpp/common",
        "src/jvmMain/cpp/${targetOs.id}",
        "src/jvmTest/cpp"
    )
    sourceRoots.set(srcDirs)
    if (targetOs != OS.Android) includeHeadersNonRecursive(jdkHome.resolve("include"))
    includeHeadersNonRecursive(skiaHeadersDirs(skiaJvmBindingsDir.get()))
    includeHeadersNonRecursive(projectDir.resolve("src/jvmMain/cpp/include"))
    includeHeadersNonRecursive(projectDir.resolve("src/commonMain/cpp/common/include"))

    compiler.set(compilerForTarget(targetOs, targetArch))

    val osFlags: Array<String>
    when (targetOs) {
        OS.MacOS -> {
            includeHeadersNonRecursive(jdkHome.resolve("include/darwin"))
            osFlags = arrayOf(
                *targetOs.clangFlags,
                *buildType.clangFlags,
                "-fPIC",
                "-stdlib=libc++",
                "-fvisibility=hidden",
                "-fvisibility-inlines-hidden",
                "-DSK_SHAPER_CORETEXT_AVAILABLE",
                "-DSK_BUILD_FOR_MAC",
                "-DSK_METAL",
            )
        }
        OS.Linux -> {
            includeHeadersNonRecursive(jdkHome.resolve("include/linux"))
            osFlags = arrayOf(
                *buildType.clangFlags,
                "-fPIC",
                "-fno-rtti",
                "-fno-exceptions",
                "-fvisibility=hidden",
                "-fvisibility-inlines-hidden",
                "-DSK_BUILD_FOR_LINUX",
                "-D_GLIBCXX_USE_CXX11_ABI=0",
            )
        }
        OS.Windows -> {
            compiler.set(windowsSdkPaths.compiler.absolutePath)
            includeHeadersNonRecursive(windowsSdkPaths.includeDirs)
            includeHeadersNonRecursive(jdkHome.resolve("include/win32"))
            osFlags = arrayOf(
                "/nologo",
                *buildType.msvcCompilerFlags,
                "-DSK_BUILD_FOR_WIN",
                "-D_CRT_SECURE_NO_WARNINGS",
                "-D_HAS_EXCEPTIONS=0",
                "-DWIN32_LEAN_AND_MEAN",
                "-DNOMINMAX",
                "-DSK_GAMMA_APPLY_TO_A8",
                "-DSK_DIRECT3D",
                "/utf-8",
                "/GR-", // no-RTTI.
                // LATER. Ange rendering arguments:
                // "-I$skiaDir/third_party/externals/angle2/include",
                // "-I$skiaDir/src/gpu",
                // "-DSK_ANGLE",
            )
        }
        OS.Android -> {
            compiler.set(androidClangFor(targetArch))
            osFlags = arrayOf(
                *buildType.clangFlags,
                "-fno-rtti",
                "-fno-exceptions",
                "-fvisibility=hidden",
                "-fPIC",
                "-DSK_BUILD_FOR_ANDROID"
            )
        }
        OS.Wasm, OS.IOS -> error("Should not reach here")
    }

    flags.set(
        listOf(
            *skiaPreprocessorFlags(),
            *osFlags
        )
    )
}

fun createLinkJvmBindings(targetOs: OS, targetArch: Arch,
                          skiaJvmBindingsDir: Provider<File>,
                          compileTask: TaskProvider<CompileSkikoCppTask>) =
    tasks.register<LinkSkikoTask>("linkJvmBindings-${targetOs.id}-${targetArch.id}") {
        val target = targetId(targetOs, targetArch)
        val skiaBinSubdir = "out/${buildType.id}-$target"
        val skiaBinDir = skiaJvmBindingsDir.get().absolutePath + "/" + skiaBinSubdir
        val osFlags: Array<String>

        libFiles = fileTree(skiaJvmBindingsDir.map { it.resolve(skiaBinSubdir)}) {
            include(if (targetOs.isWindows) "*.lib" else "*.a")
        }

        dependsOn(compileTask)
        objectFiles = fileTree(compileTask.map { it.outDir.get() }) {
            include("**/*.o")
        }

        val libNamePrefix = if (targetOs.isWindows) "skiko" else "libskiko"
        libOutputFileName.set("$libNamePrefix-${targetOs.id}-${targetArch.id}${targetOs.dynamicLibExt}")
        buildTargetOS.set(targetOs)
        buildTargetArch.set(targetArch)
        buildVariant.set(buildType)
        linker.set(linkerForTarget(targetOs, targetArch))

        when (targetOs) {
            OS.MacOS -> {
                dependsOn("objcCompile")
                objectFiles += fileTree("$buildDir/objc/$target") {
                    include("**/*.o")
                }

                osFlags = arrayOf(
                    *targetOs.clangFlags,
                    "-shared",
                    "-dead_strip",
                    "-lobjc",
                    "-framework", "AppKit",
                    "-framework", "CoreFoundation",
                    "-framework", "CoreGraphics",
                    "-framework", "CoreServices",
                    "-framework", "CoreText",
                    "-framework", "Foundation",
                    "-framework", "IOKit",
                    "-framework", "Metal",
                    "-framework", "OpenGL",
                    "-framework", "QuartzCore" // for CoreAnimation
                )
            }
            OS.Linux -> {
                osFlags = arrayOf(
                    "-shared",
                    "-static-libstdc++",
                    "-static-libgcc",
                    "-lGL",
                    "-lfontconfig",
                    // A fix for https://github.com/JetBrains/compose-jb/issues/413.
                    // Dynamic position independent linking uses PLT thunks relying on jump targets in GOT (Global Offsets Table).
                    // GOT entries marked as (for example) R_X86_64_JUMP_SLOT in the relocation table. So, if there's code loading
                    // platform libstdc++.so, lazy resolve code will resolve GOT entries to platform libstdc++.so on first invocation,
                    // and so further execution will break, as those two libstdc++ are not compatible.
                    // To fix it we enforce resolve of all GOT entries at library load time, and make it read-only afterwards.
                    "-Wl,-z,relro,-z,now",
                    // Hack to fix problem with linker not always finding certain declarations.
                    "$skiaBinDir/libsksg.a",
                    "$skiaBinDir/libskia.a",
                    "$skiaBinDir/libskunicode.a"
                )
            }
            OS.Windows -> {
                linker.set(windowsSdkPaths.linker.absolutePath)
                libDirs.set(windowsSdkPaths.libDirs)
                osFlags = arrayOf(
                    *buildType.msvcLinkerFlags,
                    "/NOLOGO",
                    "/DLL",
                    "Advapi32.lib",
                    "gdi32.lib",
                    "Dwmapi.lib",
                    "opengl32.lib",
                    "shcore.lib",
                    "user32.lib",
                )
            }
            OS.Android -> {
                osFlags = arrayOf(
                    "-shared"
                )
                linker.set(androidClangFor(targetArch))
            }
            OS.Wasm, OS.IOS -> {
                throw GradleException("This task shalln't be used with $targetOs")
            }
        }
        flags.set(listOf(*osFlags))
    }

if (hostOs == OS.MacOS) {
    // Very hacky way to compile Objective-C sources and add the
    // resulting object files into the final library.
    // TODO: rewrite using SkikoCompileTask
    project.tasks.register<Exec>("objcCompile") {
        val target = targetId(targetOs, targetArch)
        val inputDir = "$projectDir/src/jvmMain/objectiveC/${targetOs.id}"
        val outDir = "$buildDir/objc/$target"
        val names = File(inputDir).listFiles()!!.map { it.name.removeSuffix(".mm") }
        val srcs = names.map { "$inputDir/$it.mm" }.toTypedArray()
        val outs = names.map { "$outDir/$it.o" }.toTypedArray()
        workingDir = File(outDir)
        val skiaDir = skiaJvmBindingsDir.get().absolutePath
        dependsOn(skiaJvmBindingsDir)

        commandLine = listOf(
            "clang",
            *targetOs.clangFlags,
            *buildType.clangFlags,
            "-I$jdkHome/include",
            "-I$jdkHome/include/darwin",
            "-I$skiaDir",
            "-I$skiaDir/include",
            "-I$skiaDir/include/gpu",
            "-fobjc-arc",
            "-DSK_METAL",
            "-c",
            *srcs
        )
        file(outDir).mkdirs()
        inputs.files(srcs)
        outputs.files(outs)
    }
}

val generateVersion = generateVersionTask(targetOs, targetArch)

fun generateVersionTask(targetOs: OS, targetArch: Arch) = project.tasks.register("generateVersion") {
    val outDir = generatedKotlin
    file(outDir).mkdirs()
    val out = "$outDir/Version.kt"
    outputs.dir(outDir)
    doFirst {
        val target = "${targetOs.id}-${targetArch.id}"
        val skiaTag = project.property("dependencies.skia.$target") as String
        File(out).writeText("""
        package org.jetbrains.skiko
        object Version {
          val skiko = "${skiko.deployVersion}"
          val skia = "${skiaTag}"
        }
        """.trimIndent())
    }
}

val skikoJvmJar: Provider<Jar> by project.tasks.registering(Jar::class) {
    archiveBaseName.set("skiko-jvm")
    from(kotlin.jvm().compilations["main"].output.allOutputs)
}

fun maybeSignTask(targetOs: OS, targetArch: Arch, linkJvmBindings: Provider<LinkSkikoTask>) =
    project.tasks.register<SealAndSignSharedLibraryTask>("maybeSign-${targetOs.id}-${targetArch.id}") {
    dependsOn(linkJvmBindings)

    val linkOutputFile = linkJvmBindings.map { task ->
        task.outDir.get().asFile.walk().single { it.name.endsWith(targetOs.dynamicLibExt) }.absoluteFile
    }
    libFile.set(project.layout.file(linkOutputFile))
    outDir.set(project.layout.buildDirectory.dir("maybe-signed"))

    val toolsDir = project.layout.projectDirectory.dir("tools")
    if (targetOs == OS.Linux) {
        // Linux requires additional sealing to run on wider set of platforms.
        // See https://github.com/olonho/sealer.
        when (targetArch) {
            Arch.X64 -> sealer.set(toolsDir.file("sealer-x64"))
            Arch.Arm64 -> sealer.set(toolsDir.file("sealer-arm64"))
            else -> error("Unexpected combination of '$targetArch' and '$targetOs'")
        }
    }

    if (hostOs == OS.MacOS) {
        codesignClient.set(toolsDir.file("codesign-client-darwin-x64"))
    }
    signHost.set(skiko.signHost)
    signUser.set(skiko.signUser)
    signToken.set(skiko.signToken)
}

fun createChecksumsTask(targetOs: OS, targetArch: Arch, maybeSign: Provider<SealAndSignSharedLibraryTask>) = project.tasks.register<Checksum>("createChecksums-${targetOs.id}-${targetArch.id}") {
    val skiaBinSubdir = "out/${buildType.id}-${targetOs.id}-${targetArch.id}"
    dependsOn(maybeSign)
    files = project.files(maybeSign.flatMap { it.outputFiles }) +
            if (targetOs.isWindows) files(skiaJvmBindingsDir.map { it.resolve("${skiaBinSubdir}/icudtl.dat") }) else files()
    algorithm = Checksum.Algorithm.SHA256
    outputDir = file("$buildDir/checksums")
}

fun skikoJvmRuntimeJarTask(targetOs: OS, targetArch: Arch,
                           maybeSign: Provider<SealAndSignSharedLibraryTask>,
                           createChecksums: Provider<Checksum>) =
  project.tasks.register<Jar>("skikoJvmRuntimeJar-${targetOs.id}-${targetArch.id}") {
    val target = targetId(targetOs, targetArch)
    val skiaBinSubdir = "out/${buildType.id}-$target"
    dependsOn(createChecksums)
    archiveBaseName.set("skiko-$target")
    from(skikoJvmJar.map { zipTree(it.archiveFile) })
    from(maybeSign.flatMap { it.outputFiles })
    if (targetOs.isWindows) {
        from(files(skiaJvmBindingsDir.map { it.resolve("${skiaBinSubdir}/icudtl.dat") }))
    }
    from(createChecksums.map { it.outputs.files })
}

fun skikoRuntimeDirForTestsTask(targetOs: OS, targetArch: Arch,
                                skikoJvmRuntimeJar: Provider<Jar>) =
  project.tasks.register<Copy>("skikoRuntimeDirForTests-${targetOs.id}-${targetArch.id}") {
    dependsOn(skikoJvmRuntimeJar)
    from(zipTree(skikoJvmRuntimeJar.flatMap { it.archiveFile })) {
        include("*.so")
        include("*.dylib")
        include("*.dll")
        include("icudtl.dat")
    }
    destinationDir = project.buildDir.resolve("skiko-runtime-for-tests")
}

tasks.withType<Test>().configureEach {
    dependsOn(skikoRuntimeDirForTests)
    dependsOn(skikoJvmRuntimeJar)
    options {
        val dir = skikoRuntimeDirForTests.map { it.destinationDir }.get()
        systemProperty("skiko.library.path", dir)
        val jar = skikoJvmRuntimeJar.get().outputs.files.files.single { it.name.endsWith(".jar")}
        systemProperty("skiko.jar.path", jar.absolutePath)

        systemProperty("skiko.test.screenshots.dir", File(project.projectDir, "src/jvmTest/screenshots").absolutePath)
        systemProperty("skiko.test.ui.enabled", System.getProperty("skiko.test.ui.enabled", "false"))
        systemProperty("skiko.test.ui.renderApi", System.getProperty("skiko.test.ui.renderApi", "all"))

        // Tests should be deterministic, so disable scaling.
        // On MacOs we need the actual scale, otherwise we will have aliased screenshots because of scaling.
        if (System.getProperty("os.name") != "Mac OS X") {
            systemProperty("sun.java2d.dpiaware", "false")
            systemProperty("sun.java2d.uiScale", "1")
        }
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

    tasks.named("clean").configure {
        doLast {
            delete(skiko.dependenciesDir)
            delete(project.file("src/jvmMain/java"))
        }
    }
}

val emptySourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        configureEach {
            val repoName = name
            tasks.register("publishTo${repoName}") {
                group = "publishing"
                dependsOn(tasks.named("publishAllPublicationsTo${repoName}Repository"))
            }
        }
        maven {
            name = "BuildRepo"
            url = uri("${rootProject.buildDir}/repo")
        }
        maven {
            name = "ComposeRepo"
            url = uri(skiko.composeRepoUrl)
            credentials {
                username = skiko.composeRepoUserName
                password = skiko.composeRepoKey
            }
        }
    }
    publications {
        val pomNameForPublication = HashMap<String, String>()
        pomNameForPublication["kotlinMultiplatform"] = "Skiko MPP"
        kotlin.targets.forEach {
            pomNameForPublication[it.name] = "Skiko ${toTitleCase(it.name)}"
        }
        configureEach {
            this as MavenPublication
            groupId = "org.jetbrains.skiko"

            // Necessary for publishing to Maven Central
            artifact(emptyJavadocJar)

            pom {
                description.set("Kotlin Skia bindings")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                val repoUrl = "https://www.github.com/JetBrains/skiko"
                url.set(repoUrl)
                scm {
                    url.set(repoUrl)
                    val repoConnection = "scm:git:$repoUrl.git"
                    connection.set(repoConnection)
                    developerConnection.set(repoConnection)
                }
                developers {
                    developer {
                        name.set("Compose Multiplatform Team")
                        organization.set("JetBrains")
                        organizationUrl.set("https://www.jetbrains.com")
                    }
                }
            }
        }

        create<MavenPublication>("skikoJvmRuntime") {
            pomNameForPublication[name] = "Skiko JVM Runtime for ${targetOs.name} ${targetArch.name}"
            artifactId = SkikoArtifacts.jvmRuntimeArtifactIdFor(targetOs, targetArch)
            afterEvaluate {
                allJvmRuntimeJars.forEach { jar ->
                    artifact(jar.map { it.archiveFile.get() })
                }
                var jvmSourcesArtifact: Any? = null
                kotlin.jvm().mavenPublication {
                    jvmSourcesArtifact = artifacts.find { it.classifier == "sources" }
                }
                if (jvmSourcesArtifact == null) {
                    error("Could not find sources jar artifact for JVM target")
                } else {
                    artifact(jvmSourcesArtifact)
                }
            }
        }

        if (supportWasm) {
            create<MavenPublication>("skikoWasmRuntime") {
                pomNameForPublication[name] = "Skiko WASM Runtime"
                artifactId = SkikoArtifacts.jsWasmArtifactId
                artifact(tasks.named("skikoWasmJar").get())
                artifact(emptySourcesJar)
            }
        }

        val publicationsWithoutPomNames = publications.filter { it.name !in pomNameForPublication }
        if (publicationsWithoutPomNames.isNotEmpty()) {
            error("Publications with unknown POM names: ${publicationsWithoutPomNames.joinToString { "'$it'" }}")
        }
        configureEach {
            this as MavenPublication
            pom.name.set(pomNameForPublication[name]!!)
        }
    }
}

val mavenCentral = MavenCentralProperties(project)
if (skiko.isCIBuild || mavenCentral.signArtifacts) {
    signing {
        sign(publishing.publications)
        useInMemoryPgpKeys(mavenCentral.signArtifactsKey.get(), mavenCentral.signArtifactsPassword.get())
    }
}

afterEvaluate {
    tasks.withType<KotlinCompile>().configureEach { // this one is actually KotlinJvmCompile
        dependsOn(generateVersion)
        source(generatedKotlin)

        if (name == "compileTestKotlinJvm") {
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }
    tasks.withType<KotlinNativeCompile>().configureEach {
        dependsOn(generateVersion)
        source(generatedKotlin)
    }
}

// Kotlin/JS has a bug preventing compilation on non-x86 Linux machines,
// see https://youtrack.jetbrains.com/issue/KT-48631
// It always downloads and uses x86 version, so on those architectures
if (hostOs == OS.Linux && hostArch != Arch.X64) {
    rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin::class.java) {
        rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().download = false
    }
}

/**
 * Do not call inside tasks.register or tasks.call callback
 * (tasks' registration during other task's registration is prohibited)
 */
fun registerOrGetSkiaDirProvider(os: OS, arch: Arch): Provider<File> {
    val taskNameSuffix = joinToTitleCamelCase(buildType.id, os.id, arch.id)
    val skiaRelease = skiko.skiaReleaseFor(os, arch, buildType)
    val downloadSkia = tasks.registerOrGetTask<Download>("downloadSkia$taskNameSuffix") {
        onlyIf { !dest.exists() }
        onlyIfModified(true)
        val skiaUrl = "https://github.com/JetBrains/skia-pack/releases/download/$skiaRelease.zip"
        inputs.property("skia.url", skiaUrl)
        src(skiaUrl)
        dest(skiko.dependenciesDir.resolve("skia/$skiaRelease.zip"))
    }.map { it.dest.absoluteFile }

    return if (skiko.skiaDir != null) {
        tasks.registerOrGetTask<DefaultTask>("skiaDir$taskNameSuffix") {
            // dummy task to simplify usage of the resulting provider (see `else` branch)
            // if a file provider is not created from a task provider,
            // then it cannot be used instead of a task in `dependsOn` clauses of other tasks.
            // e.g. the resulting `skiaDir` could not be used in `dependsOn` of CppCompile configuration
            enabled = false
        }.map { skiko.skiaDir!!.absoluteFile }
    } else {
        tasks.registerOrGetTask<Copy>("unzipSkia$taskNameSuffix") {
            dependsOn(downloadSkia)
            from(downloadSkia.map { zipTree(it) })
            into(skiko.dependenciesDir.resolve("skia/$skiaRelease"))
        }.map { it.destinationDir.absoluteFile }
    }
}

tasks.withType<AbstractTestTask> {
    testLogging {
        events("FAILED", "SKIPPED")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
        showStackTraces = true
    }
}
