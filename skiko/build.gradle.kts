import de.undercouch.gradle.tasks.download.Download
import org.gradle.crypto.checksum.Checksum
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    kotlin("multiplatform") version "1.5.10"
    `cpp-library`
    `maven-publish`
    id("org.gradle.crypto.checksum") version "1.1.0"
    id("de.undercouch.download") version "4.1.1"
}

val coroutinesVersion = "1.5.0"

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

allprojects {
    group = "org.jetbrains.skiko"
    version = skiko.deployVersion
}

repositories {
    mavenCentral()
}

val skiaZip = run {
    val zipName = skiko.skiaReleaseFor(targetOs, targetArch) + ".zip"
    val zipFile = skiko.dependenciesDir.resolve("skia/${zipName.substringAfterLast('/')}")

    tasks.register("downloadSkia", Download::class) {
        onlyIf { skiko.skiaDir == null && !zipFile.exists() }
        inputs.property("skia.release.for.target.os", skiko.skiaReleaseFor(targetOs, targetArch))
        src("https://github.com/JetBrains/skia-pack/releases/download/$zipName")
        dest(zipFile)
        onlyIfModified(true)
    }.map { zipFile }
}

val skiaWasmZip = run {
    val release = skiko.skiaReleaseFor(OS.Wasm, Arch.Wasm)
    val zipName = "$release.zip"
    val zipFile = skiko.dependenciesDir.resolve("skia/${zipName.substringAfterLast('/')}")
    tasks.register("downloadSkiaWasm", Download::class) {
        onlyIf { skiko.skiaDir == null && !zipFile.exists() }
        inputs.property("skia.release.for.wasm", release)
        src("https://github.com/JetBrains/skia-pack/releases/download/$zipName")
        dest(zipFile)
        onlyIfModified(true)
    }.map { zipFile }
}

fun AbstractCopyTask.configureSkiaCopy(targetDir: File) {
    into(targetDir)
}

val skiaDir = run {
    if (skiko.skiaDir != null) {
        tasks.register("skiaDir", DefaultTask::class) {
            // dummy task to simplify usage of the resulting provider (see `else` branch)
            // if a file provider is not created from a task provider,
            // then it cannot be used instead of a task in `dependsOn` clauses of other tasks.
            // e.g. the resulting `skiaDir` could not be used in `dependsOn` of CppCompile configuration
            enabled = false
        }.map { skiko.skiaDir!! }
    } else {
        val targetDir = skiko.dependenciesDir.resolve("skia/skia")
        tasks.register("unzipSkia", Copy::class) {
            from(skiaZip.map { zipTree(it) })
            configureSkiaCopy(targetDir)
        }.map { targetDir }
    }
}

val skiaWasmDir = run {
    if (skiko.skiaDir != null) {
        tasks.register("skiaWasmDir", DefaultTask::class) {
            // dummy task to simplify usage of the resulting provider (see `else` branch)
            // if a file provider is not created from a task provider,
            // then it cannot be used instead of a task in `dependsOn` clauses of other tasks.
            // e.g. the resulting `skiaDir` could not be used in `dependsOn` of CppCompile configuration
            enabled = false
        }.map { skiko.skiaDir!! }
    } else {
        val targetDir = skiko.dependenciesDir.resolve("skia/skia-wasm")
        tasks.register("unzipSkiaWasm", Copy::class) {
            from(skiaWasmZip.map { zipTree(it) })
            configureSkiaCopy(targetDir)
        }.map { targetDir }
    }
}

val skiaBinSubdir = "out/${buildType.id}-${targetOs.id}-${targetArch.id}"

val Project.supportNative: Boolean
   get() = properties.get("skiko.native.enabled") == "true"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

    js(IR) {
        browser() {
            testTask {
                testLogging.showStandardStreams = true
                dependsOn(project.tasks.named("wasmCompile"))
                useKarma() {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }

    if (supportNative) {
        val targetString = target
        val skiaDir = skiaDir.get().absolutePath
        val nativeTarget = when (targetString) {
            "macos-x64", "macos-arm64" -> macosX64() {
                compilations.all {
                    kotlinOptions {
                        freeCompilerArgs += listOf(
                            "-include-binary",
                            "$skiaDir/$skiaBinSubdir/libskia.a",
                            "-include-binary",
                            "$skiaDir/$skiaBinSubdir/libskshaper.a",
                            "-include-binary",
                            "$skiaDir/$skiaBinSubdir/libskparagraph.a"
                        )

                        freeCompilerArgs += listOf(
                            "-include-binary",
                            "$buildDir/nativeBridges/static/$targetString/skiko-native-bridges-$targetString.a"
                        )
                    }
                }
            }
            else -> null
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
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
                implementation(kotlin("stdlib-jdk8"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutinesVersion")
            }

        }
        val jvmTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
                implementation(kotlin("test-junit"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation(kotlin("test-js"))
            }
        }

        if (supportNative) {
            val macosX64Main by getting {
                dependsOn(commonMain)
            }
        }
    }
}

tasks.withType(JavaCompile::class.java).configureEach {
    this.getOptions().compilerArgs.addAll(listOf("-source", "11", "-target", "11"))
}

val skiaPreprocessorFlags: Array<String> get() {
    val skiaDir = skiaDir.get().absolutePath
    return listOf(
        "-I$skiaDir",
        "-I$skiaDir/include",
        "-I$skiaDir/include/core",
        "-I$skiaDir/include/gpu",
        "-I$skiaDir/include/effects",
        "-I$skiaDir/include/pathops",
        "-I$skiaDir/include/utils",
        "-I$skiaDir/include/codec",
        "-I$skiaDir/include/svg",
        "-I$skiaDir/modules/skottie/include",
        "-I$skiaDir/modules/skparagraph/include",
        "-I$skiaDir/modules/skshaper/include",
        "-I$skiaDir/modules/sksg/include",
        "-I$skiaDir/modules/svg/include",
        "-I$skiaDir/third_party/externals/harfbuzz/src",
        "-I$skiaDir/third_party/icu",
        "-I$skiaDir/third_party/externals/icu/source/common",
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

// See https://docs.gradle.org/current/userguide/cpp_library_plugin.html.
tasks.withType(CppCompile::class.java).configureEach {
    // Prefer 'java.home' system property to simplify overriding from Intellij.
    // When used from command-line, it is effectively equal to JAVA_HOME.
    if (JavaVersion.current() < JavaVersion.VERSION_11) {
        error("JDK 11+ is required, but Gradle JVM is ${JavaVersion.current()}. " +
                "Check JAVA_HOME (CLI) or Gradle settings (Intellij).")
    }
    val jdkHome = System.getProperty("java.home") ?: error("'java.home' is null")
    dependsOn(skiaDir)
    val skiaDir = skiaDir.get().absolutePath
    compilerArgs.addAll(
        listOf("-I$jdkHome/include") + skiaPreprocessorFlags
    )
    val includeDir = "$projectDir/src/jvmMain/cpp/include"
    val commonIncludeDir = "$projectDir/src/commonMain/cpp/headers"

    when (targetOs) {
        OS.MacOS -> {
            compilerArgs.addAll(
                listOf(
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    "-I$jdkHome/include/darwin",
                    "-I$includeDir",
                    "-I$commonIncludeDir",
                    "-DSK_SHAPER_CORETEXT_AVAILABLE",
                    "-DSK_BUILD_FOR_MAC",
                    "-DSK_METAL",
                    *targetArch.clangFlags,
                    *buildType.clangFlags
                )
            )
        }
        OS.Linux -> {
            compilerArgs.addAll(
                listOf(
                    "-fno-rtti",
                    "-fno-exceptions",
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    "-I$jdkHome/include/linux",
                    "-I$includeDir",
                    "-I$commonIncludeDir",
                    "-DSK_BUILD_FOR_LINUX",
                    "-D_GLIBCXX_USE_CXX11_ABI=0",
                    *buildType.clangFlags
                )
            )
        }
        OS.Windows -> {
            compilerArgs.addAll(
                listOf(
                    "-I$jdkHome/include/win32",
                    "-I$includeDir",
                    "-I$commonIncludeDir",
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
                    *buildType.msvcFlags
                    )
            )
        }
        OS.Wasm -> throw GradleException("Should not reach here")
    }
}

// Very hacky way to compile Objective-C sources and add the
// resulting object files into the final library.
project.tasks.register<Exec>("objcCompile") {
    val inputDir = "$projectDir/src/jvmMain/objectiveC/${targetOs.id}"
    val outDir = "$buildDir/objc/$target"
    val names = File(inputDir).listFiles()!!.map { it.name.removeSuffix(".mm") }
    val srcs = names.map { "$inputDir/$it.mm" }.toTypedArray()
    val outs = names.map { "$outDir/$it.o" }.toTypedArray()
    workingDir = File(outDir)
    val skiaDir = skiaDir.get().absolutePath
    commandLine = listOf(
        "clang",
        *targetArch.clangFlags,
        "-mmacosx-version-min=10.13",
        "-I$jdkHome/include",
        "-I$jdkHome/include/darwin",
        "-I$skiaDir",
        "-I$skiaDir/include",
        "-I$skiaDir/include/gpu",
        "-DSK_METAL",
        "-std=c++17",
        "-c",
        *srcs
    )
    file(outDir).mkdirs()
    inputs.files(srcs)
    outputs.files(outs)
}

project.tasks.register<Exec>("wasmCompile") {
    dependsOn(skiaWasmDir)
    val inputDir = "$projectDir/src/jsMain/cpp"
    val outDir = "$buildDir/wasm"
    val names = File(inputDir).listFiles()!!.map { it.name.removeSuffix(".cc") }
    val srcs = names.map { "$inputDir/$it.cc" }.toTypedArray()
    val outJs = "$outDir/skiko.js"
    val outWasm = "$outDir/skiko.wasm"
    val skiaDir = skiaWasmDir.get().absolutePath
    workingDir = File(outDir)
    val libs = fileTree("$skiaDir/out/Release-wasm-wasm").filter { it.name.endsWith(".a") }
    commandLine = listOf(
        "emcc",
        *Arch.Wasm.clangFlags,
        "-I$skiaDir",
        "-I$skiaDir/include",
        "-I$skiaDir/include/gpu",
        "-std=c++17",
        "--bind",
        "-o", outJs,
        *libs.files.map { it.absolutePath }.toTypedArray(),
        *srcs
    )
    file(outDir).mkdirs()
    inputs.files(srcs)
    outputs.files(outJs, outWasm)
}

fun List<String>.findAllFiles(suffix: String): List<String> = this
    .map { File(it) }
    .flatMap { it.walk().toList() }
    .map { it.absolutePath }
    .filter { it.endsWith(suffix) }


// Very hacky way to compile native bridges and add the
// resulting object files into the final native klib.
project.tasks.register<Exec>("nativeBridgesCompile") {
    dependsOn(skiaDir)
    val inputDirs = listOf(
        "$projectDir/src/macosX64Main/cpp/generated",
        "$projectDir/src/macosX64Main/cpp/common",
        "$projectDir/src/commonMain/cpp/common"
    )
    val outDir = "$buildDir/nativeBridges/obj/$target"
    val srcs = inputDirs
        .findAllFiles(".cc")
        .toTypedArray()
    val outs = srcs
        .map { it.substringAfterLast("/") }
        .map { File(it).nameWithoutExtension }
        .map { "$outDir/$it.o" }
        .toTypedArray()

    workingDir = File(outDir)
    commandLine = listOf(
        "clang++",
        *targetArch.clangFlags,
        "-mmacosx-version-min=10.13",
        "-std=c++17",
        "-c",
        "-DPROVIDE_JNI_TYPES",
        "-DSK_SHAPER_CORETEXT_AVAILABLE",
        "-DSK_BUILD_FOR_MAC",
        "-DSK_METAL",
        "-I$projectDir/src/macosX64Main/cpp/headers",
        "-I$projectDir/src/commonMain/cpp/headers",
        *skiaPreprocessorFlags,
        *srcs
    )
    file(outDir).mkdirs()
    inputs.files(srcs)
    outputs.files(outs)
}

project.tasks.register<Exec>("nativeBridgesLink") {
    dependsOn(project.tasks.getByName("nativeBridgesCompile"))
    inputs.files(project.tasks.getByName("nativeBridgesCompile").outputs)

    val outDir = "$buildDir/nativeBridges/static/$target"
    val srcs = inputs.files.files
        .map { it.absolutePath }
        .toTypedArray()
    println("SRCS: $srcs")
    val staticLib = "$outDir/skiko-native-bridges-$target.a"
    workingDir = File(outDir)
    commandLine = listOf(
        "libtool",
        "-static",
        "-o",
        staticLib,
        *srcs
    )
    file(outDir).mkdirs()
    outputs.files(staticLib)
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


fun remoteSign(signHost: String, lib: File, out: File) {
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

tasks.withType(LinkSharedLibrary::class.java).configureEach {
    when (targetOs) {
        OS.MacOS -> {
            dependsOn(project.tasks.named("objcCompile"))
            linkerArgs.addAll(
                listOf(
                    *targetArch.clangFlags,
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
            )
        }
        OS.Linux -> {
            linkerArgs.addAll(
                listOf(
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
                    skiaDir.get().absolutePath + "/$skiaBinSubdir/libsksg.a",
                    skiaDir.get().absolutePath + "/$skiaBinSubdir/libskia.a"
                    )
            )
        }
        OS.Windows -> {
            linkerArgs.addAll(
                listOf(
                    "Advapi32.lib",
                    "gdi32.lib",
                    "Dwmapi.lib",
                    "opengl32.lib",
                    "shcore.lib",
                    "user32.lib"
                )
            )
        }
        OS.Wasm -> {
            throw GradleException("This task shalln't be used with WASM")
        }
    }
}

extensions.configure<CppLibrary> {
    source.from(
        fileTree("$projectDir/src/commonMain/cpp/common"),
        fileTree("$projectDir/src/jvmMain/cpp/common"),
        fileTree("$projectDir/src/jvmMain/cpp/${targetOs.id}")
    )
}

library {
    linkage.addAll(listOf(Linkage.SHARED))
    targetMachines.addAll(listOf(machines.macOS.x86_64, machines.linux.x86_64, machines.windows.x86_64))
    baseName.set("skiko-$target")

    dependencies {
        implementation(
            skiaDir.map {
                fileTree(it.resolve(skiaBinSubdir))
                    .matching { include(if (targetOs.isWindows) "**.lib" else "**.a") }
            }
        )
        implementation(fileTree("$buildDir/objc/$target").matching {
             include("**.o")
        })
    }

    toolChains {
        withType(VisualCpp::class.java) {
            // In some cases Gradle is unable to find VC++ toolchain
            // https://github.com/gradle/gradle-native/issues/617
            skiko.visualStudioBuildToolsDir?.let {
                setInstallDir(it)
            }
        }
    }
}

val skikoJvmJar: Provider<Jar> by tasks.registering(Jar::class) {
    archiveBaseName.set("skiko-jvm")
    from(kotlin.jvm().compilations["main"].output.allOutputs)
}

val skikoNativeLib: File
    get() {
        val linkTask = project.tasks.withType(LinkSharedLibrary::class.java).single { it.name.contains(buildType.id) }
        val lib =
            linkTask.outputs.files.single { it.name.endsWith(".dll") || it.name.endsWith(".dylib") || it.name.endsWith(".so") }
        return lib
    }

val maybeSign by project.tasks.registering {
    val linkTask = project.tasks.withType(LinkSharedLibrary::class.java).single { it.name.contains(buildType.id) }
    dependsOn(linkTask)
    val lib = linkTask.outputs.files.single { it.name.endsWith(".dll") ||  it.name.endsWith(".dylib") ||  it.name.endsWith(".so") }
    inputs.files(lib)
    val output = file(lib.absolutePath + ".maybesigned")
    outputs.files(output)

    doLast {
        if (targetOs == OS.Linux) {
            // Linux requires additional sealing to run on wider set of platforms.
            val sealer = "$projectDir/tools/sealer-${hostArch.id}"
            sealBinary(sealer, lib)
        }
        if (skiko.signHost != null) {
            remoteSign(skiko.signHost!!, lib, output)
        } else {
            lib.copyTo(output, overwrite = true)
        }
    }
}

val createChecksums by project.tasks.registering(org.gradle.crypto.checksum.Checksum::class) {
    dependsOn(maybeSign)
    files = maybeSign.get().outputs.files +
            if (targetOs.isWindows) files(skiaDir.map { it.resolve("${skiaBinSubdir}/icudtl.dat") }) else files()
    algorithm = Checksum.Algorithm.SHA256
    outputDir = file("$buildDir/checksums")
}

val skikoJvmRuntimeJar by project.tasks.registering(Jar::class) {
    dependsOn(createChecksums)
    archiveBaseName.set("skiko-$target")
    from(skikoJvmJar.map { zipTree(it.archiveFile) })
    from(maybeSign.get().outputs.files)
    rename {
        // Not just suffix, as could be in middle of SHA256.
        it.replace(".maybesigned", "")
    }
    if (targetOs.isWindows) {
        from(files(skiaDir.map { it.resolve("${skiaBinSubdir}/icudtl.dat") }))
    }
    from(createChecksums.get().outputs.files)
}

project.tasks.register<Jar>("skikoJsJar") {
    // We produce jar that contains .js of wrapper/bindings and .wasm with Skia + bindings.
    from(project.tasks.named("wasmCompile").get().outputs)
    from(project.tasks.named("jsJar").get().outputs)
    archiveBaseName.set("skiko-wasm")
    doLast {
        println("output at ${outputs.files.files.single()}")
    }
}

project.tasks.register<JavaExec>("run") {
    main = "org.jetbrains.skiko.MainKt"
    classpath = files(skikoJvmRuntimeJar.map { it.archiveFile })
}

// disable unexpected native publications (default C++ publications are failing)
tasks.withType<AbstractPublishToMaven>().configureEach {
    doFirst {
        if (!publication.name.startsWith("skiko")) {
            throw StopExecutionException("Publication '${publication.name}' is disabled")
        }
    }
}

val skikoRuntimeDirForTests by project.tasks.registering(Copy::class) {
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
        systemProperty("skiko.test.window.test.enabled", System.getProperty("skiko.test.window.test.enabled", "false"))

        // Tests should be deterministic, so disable scaling.
        // On MacOs we need the actual scale, otherwise we will have aliased screenshots because of scaling.
        if (System.getProperty("os.name") != "Mac OS X") {
            systemProperty("sun.java2d.dpiaware", "false")
            systemProperty("sun.java2d.uiScale", "1")
        }
    }
}

fun Publication.isSkikoPublication() =
    (this as? MavenPublication)?.name?.startsWith("skiko") == true

fun Task.disable() {
    enabled = false
    group = "Disabled tasks"
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

    // Disable publishing Gradle Metadata, because it seems unnecessary at the moment.
    // Also generating metadata for default C++ publications fails
    tasks.withType(GenerateModuleMetadata::class).configureEach {
        disable()
    }

    // Disable publishing for default publications, because
    // publishing for default C++ publications fails
    tasks.withType<AbstractPublishToMaven>().configureEach {
        if (!publication.isSkikoPublication()) {
            disable()
        }
    }

    tasks.named("clean").configure {
        doLast {
            delete(skiko.dependenciesDir)
            delete(project.file("src/jvmMain/java"))
        }
    }
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
            pom {
                name.set("Skiko")
                description.set("Kotlin Skia bindings")
                url.set("http://www.github.com/JetBrains/skiko")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
        create<MavenPublication>("skikoJvm") {
            artifactId = SkikoArtifacts.commonArtifactId
            afterEvaluate {
                artifact(skikoJvmJar.map { it.archiveFile.get() })
            }
        }
        create<MavenPublication>("skikoJvmRuntime") {
            artifactId = SkikoArtifacts.runtimeArtifactIdFor(targetOs, targetArch)
            afterEvaluate {
                artifact(skikoJvmRuntimeJar.map { it.archiveFile.get() })
            }
        }
        if (supportNative) {
            create<MavenPublication>("skikoNativeRuntime") {
                artifactId = SkikoArtifacts.nativeRuntimeArtifactIdFor(targetOs, targetArch)
                    afterEvaluate {
                        artifact(project.tasks.withType(KotlinNativeCompile::class.java)
                                .single { it.name.startsWith("compileKotlin") } // Exclude compileTestKotlin.
                                .outputs.getFiles().single { it.name.endsWith(".klib") }
                                )
                    }
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    if (name == "compileTestKotlinJvm") {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}
