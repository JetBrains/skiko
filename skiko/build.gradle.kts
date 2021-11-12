import de.undercouch.gradle.tasks.download.Download
import org.gradle.crypto.checksum.Checksum
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

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

buildscript {
    dependencies {
        classpath("org.kohsuke:github-api:1.116")
    }
    repositories {
        mavenCentral()
    }
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

val skiaBinSubdir = "out/${buildType.id}-${targetOs.id}-${targetArch.id}"

internal val Project.isInIdea: Boolean
    get() {
        return System.getProperty("idea.active")?.toBoolean() == true
    }

val listAvailablePlatforms by tasks.registering(ListSkikoPlatformsTask::class)
val generateVersion by tasks.registering {
    configureGenerateVersion()
}

kotlin {
    val requestedPlatforms = skiko.requestedPlatforms
    val skikoSourceSets = SkikoSourceSets(sourceSets, requestedPlatforms)

    skikoSourceSets.configureIfDefined(SkikoPlatform.Common) {
        main.dependencies {
            implementation(kotlin("stdlib-common"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        }
        test.dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }
    }

    skikoSourceSets.configureIfDefined(SkikoPlatform.Jvm) {
        main.dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutinesVersion")
        }
        test.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
            implementation(kotlin("test-junit"))

            implementation(kotlin("test"))
        }
        configureJvmTarget(jvm())
    }

    skikoSourceSets.configureIfDefined(SkikoPlatform.Js) {
        test.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            implementation(kotlin("test-js"))
        }
        configureJsTarget(js(IR))
    }

    skikoSourceSets.configureIfDefined(SkikoPlatform.Native) {
        // See https://kotlinlang.org/docs/mpp-share-on-platforms.html#configure-the-hierarchical-structure-manually
        main.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        }
    }

    skikoSourceSets.configureIfDefined(SkikoPlatform.LinuxX64) {
        configureNativeTarget(OS.Linux, Arch.X64, linuxX64())
    }

    skikoSourceSets.configureIfDefined(SkikoPlatform.MacosX64) {
        configureNativeTarget(OS.MacOS, Arch.X64, macosX64())
    }
    skikoSourceSets.configureIfDefined(SkikoPlatform.MacosArm64) {
        configureNativeTarget(OS.MacOS, Arch.Arm64, macosArm64())
    }

    skikoSourceSets.configureIfDefined(SkikoPlatform.IosX64) {
        configureNativeTarget(OS.IOS, Arch.X64, iosX64())
    }
    skikoSourceSets.configureIfDefined(SkikoPlatform.IosArm64) {
        configureNativeTarget(OS.IOS, Arch.Arm64, iosArm64())
    }
}

fun Task.configureGenerateVersion() {
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

fun configureNativeTarget(os: OS, arch: Arch, target: KotlinNativeTarget) {
    val targetString = "${os.id}-${arch.id}"

    val unzipper = registerOrGetSkiaDirProvider(os, arch)
    val unpackedSkia = unzipper.get()
    val skiaDir = unpackedSkia.absolutePath

    val bridgesLibrary = buildDir.resolve("nativeBridges/static/$targetString/skiko-native-bridges-$targetString.a")
    val allLibraries = skiaStaticLibraries(skiaDir, targetString) + bridgesLibrary.absolutePath

    target.compilations.all {
        val skiaBinDir = "$skiaDir/out/${buildType.id}-$targetString"
        kotlinOptions {
            val linkerFlags = when (os) {
                OS.MacOS -> listOf("-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics",
                    "-linker-option", "-framework", "-linker-option", "CoreText",
                    "-linker-option", "-framework", "-linker-option", "CoreServices"
                )
                OS.IOS -> listOf("-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics",
                    "-linker-option", "-framework", "-linker-option", "CoreText")
                OS.Linux -> listOf(
                    "-linker-option", "-L/usr/lib/x86_64-linux-gnu",
                    "-linker-option", "-lfontconfig",
                    "-linker-option", "-lGL",
                    // TODO: an ugly hack, Linux linker searches only unresolved symbols.
                    "-linker-option", "$skiaBinDir/libskshaper.a",
                    "-linker-option", "$skiaBinDir/libskunicode.a",
                    "-linker-option", "$skiaBinDir/libskia.a"
                )
                else -> emptyList()
            }
            freeCompilerArgs = allLibraries.map { listOf("-include-binary", it) }.flatten() + linkerFlags
        }
    }
    val skiaNativeDir = registerOrGetSkiaDirProvider(os, arch)
    val nativeBridgesTaskSuffix = "NativeBridges${toTitleCase(os.id)}${toTitleCase(arch.id)}"
    val compileNativeBridges = tasks.register<CompileSkikoCppTask>("compile$nativeBridgesTaskSuffix") {
        configureCompileNativeBridges(os, arch, skiaNativeDir)
    }
    val linkTask = project.tasks.register<Exec>("link$nativeBridgesTaskSuffix") {
        configureLinkNativeBridges(os, bridgesLibrary, compileNativeBridges)
    }
    target.compilations.all {
        compileKotlinTask.dependsOn(linkTask)
    }
    tasks.withType<KotlinNativeCompile>().configureEach {
        dependsOn(generateVersion)
        source(generatedKotlin)
    }
}

fun configureJvmTarget(jvmTarget: KotlinJvmTarget) {
    jvmTarget.compilations.all {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
    tasks.withType(JavaCompile::class.java).configureEach {
        options.compilerArgs.addAll(listOf("-source", "11", "-target", "11"))
    }
    tasks.withType<KotlinCompile>().configureEach { // this one is actually KotlinJvmCompile
        dependsOn(generateVersion)
        source(generatedKotlin)
    }

    val skiaJvmBindingsDir: Provider<File> = registerOrGetSkiaDirProvider(targetOs, targetArch)
    val compileJvmBindings by tasks.registering(CompileSkikoCppTask::class) {
        configureCompileJvmBindings(skiaJvmBindingsDir)
    }
    val compileObjc by tasks.registering(Exec::class) {
        configureCompileObjc(skiaJvmBindingsDir)
    }
    val linkJvmBindings by tasks.registering(LinkSkikoTask::class) {
        configureLinkJvmBindings(skiaJvmBindingsDir, compileJvmBindings, compileObjc)
    }
    val skikoJvmJar by tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-jvm")
        from(kotlin.jvm().compilations["main"].output.allOutputs)
    }
    val maybeSign by project.tasks.registering {
        configureMaybeSign(linkJvmBindings)
    }
    val createChecksums by project.tasks.registering(Checksum::class) {
       configureCreateChecksums(skiaJvmBindingsDir, maybeSign)
    }
    val skikoJvmRuntimeJar by project.tasks.registering(Jar::class) {
       configureJvmRuntimeJar(skiaJvmBindingsDir, skikoJvmJar, createChecksums, maybeSign)
    }
    val skikoRuntimeDirForTests by project.tasks.registering(Copy::class) {
        configureSkikoRuntimeDirForTests(skikoJvmRuntimeJar)
    }
    tasks.withType<Test>().configureEach {
        configureJvmTest(skikoJvmRuntimeJar, skikoRuntimeDirForTests)
    }

    publishing.publications.create<MavenPublication>("skikoJvmRuntime") {
        pom.name.set("Skiko JVM Runtime for ${targetOs.name} ${targetArch.name}")
        artifactId = SkikoArtifacts.jvmRuntimeArtifactIdFor(targetOs, targetArch)
        afterEvaluate {
            artifact(skikoJvmRuntimeJar.map { it.archiveFile.get() })
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
}

fun CompileSkikoCppTask.configureCompileJvmBindings(skiaJvmBindingsDir: Provider<File>) {
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

    includeHeadersNonRecursive(jdkHome.resolve("include"))
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
        OS.Wasm, OS.IOS -> error("Should not reach here")
    }

    flags.set(
        *skiaPreprocessorFlags(),
        *osFlags
    )
}

fun LinkSkikoTask.configureLinkJvmBindings(
    skiaJvmBindingsDir: Provider<File>,
    compileJvmBindings: Provider<CompileSkikoCppTask>,
    compileObjc: Provider<Exec>
) {
    val skiaBinDir = skiaJvmBindingsDir.get().absolutePath + "/" + skiaBinSubdir
    val osFlags: Array<String>

    libFiles = fileTree(skiaJvmBindingsDir.map { it.resolve(skiaBinSubdir)}) {
        include(if (targetOs.isWindows) "*.lib" else "*.a")
    }

    dependsOn(compileJvmBindings)
    objectFiles = fileTree(compileJvmBindings.map { it.outDir.get() }) {
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
            dependsOn(compileObjc)
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
        OS.Wasm, OS.IOS -> {
            throw GradleException("This task shalln't be used with WASM")
        }
    }

    flags.set(*osFlags)
}

fun Exec.configureCompileObjc(skiaJvmBindingsDir: Provider<File>) {
    // Very hacky way to compile Objective-C sources and add the
    // resulting object files into the final library.
    val inputDir = "$projectDir/src/jvmMain/objectiveC/${targetOs.id}"
    val outDir = "$buildDir/objc/$target"
    val names = File(inputDir).listFiles()!!.map { it.name.removeSuffix(".mm") }
    val srcs = names.map { "$inputDir/$it.mm" }.toTypedArray()
    val outs = names.map { "$outDir/$it.o" }.toTypedArray()
    workingDir = File(outDir)
    val skiaDir = skiaJvmBindingsDir.get().absolutePath
    commandLine = listOf(
        "clang",
        *targetOs.clangFlags,
        "-I$jdkHome/include",
        "-I$jdkHome/include/darwin",
        "-I$skiaDir",
        "-I$skiaDir/include",
        "-I$skiaDir/include/gpu",
        "-fobjc-arc",
        "-DSK_METAL",
        "-std=c++17",
        "-c",
        *srcs
    )
    file(outDir).mkdirs()
    inputs.files(srcs)
    outputs.files(outs)
}

fun Task.configureMaybeSign(linkJvmBindings: Provider<LinkSkikoTask>) {
    dependsOn(linkJvmBindings)

    val lib = linkJvmBindings.map { task ->
        task.outDir.get().asFile.walk().single { file -> file.name.endsWith(targetOs.dynamicLibExt) }
    }
    inputs.files(lib)

    val outputDir = project.layout.buildDirectory.dir("maybe-signed")
    val output = outputDir.map { it.asFile.resolve(lib.get().name) }
    outputs.files(output)

    doLast {
        outputDir.get().asFile.apply {
            deleteRecursively()
            mkdirs()
        }

        val libFile = lib.get()
        val outputFile = output.get()
        libFile.copyTo(outputFile, overwrite = true)

        if (targetOs == OS.Linux) {
            // Linux requires additional sealing to run on wider set of platforms.
            val sealer = "$projectDir/tools/sealer-${hostArch.id}"
            sealBinary(sealer, outputFile)
        }
        if (skiko.signHost != null) {
            remoteSignCodesign(outputFile)
        }
    }
}

fun Checksum.configureCreateChecksums(
    skiaJvmBindingsDir: Provider<File>,
    maybeSign: Provider<Task>
) {
    dependsOn(maybeSign)
    files = project.files(maybeSign.map { it.outputs.files }) +
            if (targetOs.isWindows) files(skiaJvmBindingsDir.map { it.resolve("${skiaBinSubdir}/icudtl.dat") }) else files()
    algorithm = Checksum.Algorithm.SHA256
    outputDir = file("$buildDir/checksums")
}

fun Jar.configureJvmRuntimeJar(
    skiaJvmBindingsDir: Provider<File>,
    skikoJvmJar: Provider<Jar>,
    createChecksums: Provider<Checksum>,
    maybeSign: Provider<Task>
) {
    dependsOn(createChecksums)
    archiveBaseName.set("skiko-$target")
    from(skikoJvmJar.map { zipTree(it.archiveFile) })
    from(maybeSign.map { it.outputs.files })
    if (targetOs.isWindows) {
        from(files(skiaJvmBindingsDir.map { it.resolve("${skiaBinSubdir}/icudtl.dat") }))
    }
    from(createChecksums.map { it.outputs.files })
}

fun Copy.configureSkikoRuntimeDirForTests(skikoJvmRuntimeJar: Provider<Jar>) {
    dependsOn(skikoJvmRuntimeJar)
    from(zipTree(skikoJvmRuntimeJar.flatMap { it.archiveFile })) {
        include("*.so")
        include("*.dylib")
        include("*.dll")
        include("icudtl.dat")
    }
    destinationDir = project.buildDir.resolve("skiko-runtime-for-tests")
}

fun Test.configureJvmTest(
    skikoJvmRuntimeJar: Provider<Jar>,
    skikoRuntimeDirForTests: Provider<Copy>
) {
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

fun configureJsTarget(jsTarget: org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl) {
    val skiaWasmDir = registerOrGetSkiaDirProvider(OS.Wasm, Arch.Wasm)
    val compileWasm by project.tasks.registering(CompileSkikoCppTask::class) {
        configureCompileWasm(skiaWasmDir)
    }
    val linkWasm by project.tasks.registering(LinkSkikoWasmTask::class) {
        configureLinkWasm(skiaWasmDir, compileWasm)
    }
    val wasmRuntimeJar by project.tasks.registering(Jar::class) {
        configureWasmRuntimeJar(linkWasm)
    }

    with (jsTarget) {
        browser {
            testTask {
                testLogging.showStandardStreams = true
                dependsOn(linkWasm)
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }

    // Kotlin/JS has a bug preventing compilation on non-x86 Linux machines,
    // see https://youtrack.jetbrains.com/issue/KT-48631
    // It always downloads and uses x86 version, so on those architectures
    if (hostOs == OS.Linux && hostArch != Arch.X64) {
        rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin::class.java) {
            rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().download = false
        }
    }

    val wasmRuntimeSourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
    }

    publishing.publications.create<MavenPublication>("skikoWasmRuntime") {
        pom.name.set("Skiko WASM Runtime")
        artifactId = SkikoArtifacts.jsWasmArtifactId
        artifact(wasmRuntimeJar.get())
        artifact(wasmRuntimeSourcesJar)
    }
}

fun CompileSkikoCppTask.configureCompileWasm(skiaWasmDir: Provider<File>) {
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

    flags.set(
        *skiaPreprocessorFlags(),
        "-DSKIKO_WASM"
    )
}

fun LinkSkikoWasmTask.configureLinkWasm(
    skiaWasmDir: Provider<File>,
    compileWasm: Provider<CompileSkikoCppTask>,
) {
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

    flags.set(
        "-l", "GL",
        "-s", "USE_WEBGL2=1",
        "-s", "OFFSCREEN_FRAMEBUFFER=1",
        "-s", "ALLOW_MEMORY_GROWTH=1", // TODO: Is there a better way? Should we use `-s INITIAL_MEMORY=X`?
        "--bind",
    )

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

fun Jar.configureWasmRuntimeJar(
    linkWasm: Provider<LinkSkikoWasmTask>
) {
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

fun CompileSkikoCppTask.configureCompileNativeBridges(os: OS, arch: Arch, skiaNativeDir: Provider<File>) {
    dependsOn(skiaNativeDir)
    val unpackedSkia = skiaNativeDir.get()

    compiler.set(compilerForTarget(os, arch))
    buildTargetOS.set(os)
    buildTargetArch.set(arch)
    buildVariant.set(buildType)

    when (os)  {
        OS.IOS -> {
            val sdkRoot = "/Applications/Xcode.app/Contents/Developer/Platforms"
            val iosFlags = arrayOf(
                "-std=c++17",
                "-stdlib=libc++",
                "-DSK_SHAPER_CORETEXT_AVAILABLE",
                "-DSK_BUILD_FOR_IOS",
                "-DSK_METAL"
            )
            when (arch) {
                Arch.Arm64 -> flags.set(
                    "-target", "arm64-apple-ios",
                    "-isysroot", "$sdkRoot/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk",
                    *iosFlags,
                    *skiaPreprocessorFlags()
                )
                Arch.X64 -> flags.set(
                    "-target", "x86_64-apple-ios-simulator",
                    "-isysroot", "$sdkRoot/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk",
                    *iosFlags,
                    *skiaPreprocessorFlags()
                )
                else -> throw GradleException("Unsupported arch: $arch")
            }
        }
        OS.MacOS -> flags.set(
            "-std=c++17",
            "-DSK_SHAPER_CORETEXT_AVAILABLE",
            "-DSK_BUILD_FOR_MAC",
            "-DSK_METAL",
            *skiaPreprocessorFlags()
        )
        OS.Linux -> flags.set(
            "-std=c++17",
            "-fno-rtti",
            "-fno-exceptions",
            "-fvisibility=hidden",
            "-fvisibility-inlines-hidden",
            "-DSK_BUILD_FOR_LINUX",
            "-D_GLIBCXX_USE_CXX11_ABI=0",
            *skiaPreprocessorFlags()
        )
        else -> throw GradleException("$os not yet supported")
    }

    val srcDirs = projectDirs("src/commonMain/cpp/common", "src/nativeNativeJs/cpp", "src/nativeJsMain/cpp") +
            if (skiko.includeTestHelpers) projectDirs("src/nativeJsTest/cpp") else emptyList()
    sourceRoots.set(srcDirs)

    includeHeadersNonRecursive(projectDir.resolve("src/nativeJsMain/cpp"))
    includeHeadersNonRecursive(projectDir.resolve("src/commonMain/cpp/common/include"))
    includeHeadersNonRecursive(skiaHeadersDirs(unpackedSkia))
}

fun Exec.configureLinkNativeBridges(
    os: OS,
    outputFile: File,
    compileNativeBridges: Provider<CompileSkikoCppTask>,
) {
    dependsOn(compileNativeBridges)
    val objectFilesDir = compileNativeBridges.map { it.outDir.get() }
    val objectFiles = project.fileTree(objectFilesDir) {
        include("**/*.o")
    }
    inputs.files(objectFiles)
    val outDir = outputFile.parentFile
    workingDir = outDir
    when (os) {
        OS.Linux -> {
            executable = "ar"
            argumentProviders.add { listOf("-crs", outputFile.absolutePath) }
        }
        OS.MacOS, OS.IOS -> {
            executable = "libtool"
            argumentProviders.add { listOf("-static", "-o", outputFile.absolutePath) }
        }
        else -> error("Unexpected OS for native bridges linking: $os")
    }
    argumentProviders.add { objectFiles.files.map { it.absolutePath } }
    file(outDir).mkdirs()
    outputs.dir(outDir)
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

fun localSign(signer: String, lib: File): File {
    println("Local signing $lib as $signer")
    val proc = ProcessBuilder("codesign", "-f", "-s", signer, lib.absolutePath)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    proc.waitFor(5, TimeUnit.MINUTES)
    if (proc.exitValue() != 0) {
        val out = proc.inputStream.bufferedReader().readText()
        val err = proc.errorStream.bufferedReader().readText()
        println(out)
        println(err)
        throw GradleException("Cannot sign $lib: $err")
    }
    return lib
}

// See https://github.com/olonho/sealer.
fun sealBinary(sealer: String, lib: File) {
    println("Sealing $lib by $sealer")
    val proc = ProcessBuilder(sealer, "-f", lib.absolutePath, "-p", "Java_")
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
    proc.waitFor(2, TimeUnit.MINUTES)
    if (proc.exitValue() != 0) {
        throw GradleException("Cannot seal $lib")
    }
    println("Sealed!")
}

fun remoteSignCurl(signHost: String, lib: File, out: File) {
    println("Remote signing $lib on $signHost")
    val user = skiko.signUser ?: error("signUser is null")
    val token = skiko.signToken ?: error("signToken is null")
    val cmd = """
        TOKEN=`curl -fsSL --user $user:$token --url "$signHost/auth" | grep token | cut -d '"' -f4` \
        && curl --no-keepalive --http1.1 --data-binary @${lib.absolutePath} \
        -H "Authorization: Bearer ${'$'}TOKEN" \
        -H "Content-Type:application/x-mac-app-bin" \
        "$signHost/sign?name=${lib.name}" -o "${out.absolutePath}"
    """.trimIndent()
    val proc = ProcessBuilder("bash", "-c", cmd)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    proc.waitFor(5, TimeUnit.MINUTES)
    if (proc.exitValue() != 0) {
        val out = proc.inputStream.bufferedReader().readText()
        val err = proc.errorStream.bufferedReader().readText()
        println(out)
        println(err)
        throw GradleException("Cannot sign $lib: $err")
    } else {
        val outSize = out.length()
        if (outSize < 200 * 1024) {
            val content = out.readText()
            println(content)
            throw GradleException("Output is too short $outSize: ${content.take(200)}...")
        }
    }
}

fun remoteSignCodesign(fileToSign: File) {
    val user = skiko.signUser ?: error("signUser is null")
    val token = skiko.signToken ?: error("signToken is null")
    val cmd = arrayOf(
        projectDir.resolve("tools/codesign-client-darwin-x64").absolutePath,
        fileToSign.absolutePath
    )
    val procBuilder = ProcessBuilder(*cmd).apply {
        directory(fileToSign.parentFile)
        val env = environment()
        env["SERVICE_ACCOUNT_NAME"] = user
        env["SERVICE_ACCOUNT_TOKEN"] = token
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
        redirectError(ProcessBuilder.Redirect.INHERIT)
    }
    logger.info("Starting remote code sign")
    val proc = procBuilder.start()
    proc.waitFor(5, TimeUnit.MINUTES)
    if (proc.exitValue() != 0) {
        throw GradleException("Failed to sign $fileToSign")
    } else {
        val signedDir = fileToSign.parentFile.resolve("signed")
        val signedFile = signedDir.resolve(fileToSign.name)
        check(signedFile.exists()) {
            buildString {
                appendLine("Signed file does not exist: $signedFile")
                appendLine("Other files in $signedDir:")
                signedDir.list()?.let { names ->
                    names.forEach {
                        appendLine("  * $it")
                    }
                }
            }
        }
        val size = signedFile.length()
        if (size < 200 * 1024) {
            val content = signedFile.readText()
            println(content)
            throw GradleException("Output is too short $size: ${content.take(200)}...")
        } else {
            signedFile.copyTo(fileToSign, overwrite = true)
            signedFile.delete()
            logger.info("Successfully signed $fileToSign")
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

        afterEvaluate {
            publications.configureEach {
                if (this is MavenPublication && pom.name.orNull == null) {
                    pom.name.set("Skiko ${if (name == "kotlinMultiplatform") "MPP" else toTitleCase(name)}")
                }
            }
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

fun Task.projectDirs(vararg relativePaths: String): List<Directory> {
    val projectDir = project.layout.projectDirectory
    return relativePaths.map { path -> projectDir.dir(path) }
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
