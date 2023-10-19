import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.crypto.checksum.Checksum
import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka") version "1.7.20"
    `maven-publish`
    signing
    id("org.gradle.crypto.checksum") version "1.4.0"
    id("de.undercouch.download") version "5.4.0"
}

val Project.supportWasm: Boolean
    get() = findProperty("skiko.wasm.enabled") == "true" || isInIdea

val Project.supportJs: Boolean
    get() = findProperty("skiko.js.enabled") == "true" || isInIdea

val coroutinesVersion = if (supportWasm) "1.7.2-wasm1" else "1.7.2"
val atomicFuVersion = if (supportWasm) "0.22.0-wasm0" else "0.22.0"

fun targetSuffix(os: OS, arch: Arch): String {
    return "${os.id}_${arch.id}"
}

val skiko = SkikoProperties(rootProject)
val buildType = skiko.buildType
val targetOs = hostOs
val targetArch = skiko.targetArch

allprojects {
    group = SkikoArtifacts.groupId
    version = skiko.deployVersion
}

configurations.all {
    // TODO: remove these HACKS when possible
    val conf = this
    conf.resolutionStrategy.eachDependency {
        if (requested.module.name.contains("kotlin-stdlib")) {
            val kotlinVersion = extra["kotlin.version"] as String
            useVersion(kotlinVersion)
        }

        val isWasm = conf.name.contains("wasm", true)

        if (requested.module.group == "org.jetbrains.kotlinx" &&
            requested.module.name.contains("kotlinx-coroutines", true)
        ) {
            if (!isWasm) useVersion("1.7.2")
        }

        if (requested.module.group == "org.jetbrains.kotlinx" &&
            requested.module.name.contains("atomicfu", true)
        ) {
            if (!isWasm) useVersion("0.21.0")
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    if (supportWasm) {
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
}

val windowsSdkPaths: WindowsSdkPaths by lazy {
    findWindowsSdkPaths(gradle, targetArch)
}

fun KotlinTarget.isIosSimArm64() =
    name.contains("iosSimulatorArm64", ignoreCase = true)

fun String.withSuffix(isIosSim: Boolean = false) =
    this + if (isIosSim) "Sim" else ""

val linkWasm = if (supportJs || supportWasm) {
    val skiaWasmDir = registerOrGetSkiaDirProvider(OS.Wasm, Arch.Wasm)

    val compileWasm by tasks.registering(CompileSkikoCppTask::class) {
        val osArch = OS.Wasm to Arch.Wasm

        dependsOn(skiaWasmDir)

        compiler.set(compilerForTarget(OS.Wasm, Arch.Wasm))
        buildTargetOS.set(osArch.first)
        buildTargetArch.set(osArch.second)
        buildVariant.set(buildType)

        val srcDirs = projectDirs("src/commonMain/cpp/common", "src/jsWasmMain/cpp", "src/nativeJsMain/cpp") +
                if (skiko.includeTestHelpers) projectDirs("src/nativeJsTest/cpp") else emptyList()
        sourceRoots.set(srcDirs)

        includeHeadersNonRecursive(projectDir.resolve("src/nativeJsMain/cpp"))
        includeHeadersNonRecursive(projectDir.resolve("src/jsWasmMain/cpp"))
        includeHeadersNonRecursive(projectDir.resolve("src/commonMain/cpp/common/include"))
        includeHeadersNonRecursive(skiaHeadersDirs(skiaWasmDir.get()))

        flags.set(
            listOf(
                *skiaPreprocessorFlags(OS.Wasm),
                *buildType.clangFlags,
                "-fno-rtti",
                "-fno-exceptions",
            )
        )
    }

    val configureCommon: LinkSkikoWasmTask.(outputES6: Boolean) -> Unit = { outputES6 ->
        val osArch = OS.Wasm to Arch.Wasm

        dependsOn(compileWasm)
        dependsOn(skiaWasmDir)
        val unpackedSkia = skiaWasmDir.get()

        linker.set(linkerForTarget(OS.Wasm, Arch.Wasm))
        buildTargetOS.set(osArch.first)
        buildTargetArch.set(osArch.second)
        buildVariant.set(buildType)
        if (outputES6) buildSuffix.set("es6")

        libFiles = project.fileTree(unpackedSkia) { include("**/*.a") }
        objectFiles = project.fileTree(compileWasm.map { it.outDir.get() }) {
            include("**/*.o")
        }

        val jsFileExtension = if (outputES6) "mjs" else "js"
        val wasmFileName = if (outputES6) {
            "skikomjs.wasm"
        } else {
            "skiko.wasm" // to keep it compatible with older apps
        }
        val jsFileName = if (outputES6) {
            "skikomjs.mjs"
        } else {
            "skiko.js" // to keep it compatible with older apps
        }
        libOutputFileName.set(wasmFileName) // emcc ignores this, it names .wasm file identically to js output
        jsOutputFileName.set(jsFileName) // this determines the name .wasm file too

        skikoJsPrefix.from(
            // the order matters
            project.layout.projectDirectory.file("src/jsWasmMain/resources/skikoCallbacks.js"),
            project.layout.projectDirectory.file("src/jsWasmMain/resources/setup.$jsFileExtension")
        )

        flags.set(buildList {
            addAll(
                listOf(
                    "-l", "GL",
                    "-s", "USE_WEBGL2=1",
                    "-s", "OFFSCREEN_FRAMEBUFFER=1",
                    "-s", "ALLOW_MEMORY_GROWTH=1", // TODO: Is there a better way? Should we use `-s INITIAL_MEMORY=X`?
                    "--bind"
                )
            )
            if (outputES6) {
                addAll(
                    listOf(
                        "-s", "EXPORT_ES6=1",
                        "-s", "MODULARIZE=1",
                        "-s", "EXPORT_NAME=loadSkikoWASM",
                        "-s", "EXPORTED_RUNTIME_METHODS=\"[GL, wasmExports]\"",
                        // "-s", "EXPORT_ALL=1",
                    )
                )
            }
        })

        doLast {
            // skiko.js (and skiko.mjs) files are directly referenced in karma.config.d/*/config.js
            // so symbols must be replaced right after linking
            val jsFiles = outDir.asFile.get().walk()
                .filter { it.isFile && (it.name.endsWith(".js") || it.name.endsWith(".mjs")) }

            for (jsFile in jsFiles) {
                val originalContent = jsFile.readText()
                val newContent = originalContent.replace("_org_jetbrains", "org_jetbrains")
                    .replace("skikomjs.wasm", "skiko.wasm")
                    .replace("if (ENVIRONMENT_IS_NODE) {", "if (false) {") // to make webpack erase this part
                jsFile.writeText(newContent)

                if (outputES6) {
                    // delete this file as its presence can be confusing.
                    // It's identical to skiko.wasm and we use skiko.wasm in `skikoWasmJar`task
                    outDir.file(wasmFileName).get().asFile.delete()

                    outDir.file(jsFileName).get().asFile.renameTo(outDir.asFile.get().resolve("skiko.mjs"))
                }
            }
        }

    }

    val linkWasmWithES6 by tasks.registering(LinkSkikoWasmTask::class) {
        configureCommon(true)
    }

    val linkWasm by tasks.registering(LinkSkikoWasmTask::class) {
        dependsOn(linkWasmWithES6)
        configureCommon(false)
    }


    val skikoWasmJar by project.tasks.registering(Jar::class) {
        dependsOn(linkWasm)
        // We produce jar that contains .js of wrapper/bindings and .wasm with Skia + bindings.
        val wasmOutDir = linkWasm.map { it.outDir }
        val wasmEsOutDir = linkWasmWithES6.map { it.outDir }

        from(wasmOutDir) {
            include("*.wasm")
            include("*.js")
            include("*.mjs")
        }
        from(wasmEsOutDir) {
            include("*.mjs")
        }

        archiveBaseName.set("skiko-wasm")
        doLast {
            println("Wasm and JS at: ${archiveFile.get().asFile.absolutePath}")
        }
    }

    Pair(linkWasm, linkWasmWithES6)
} else Pair(null, null)

fun compileNativeBridgesTask(os: OS, arch: Arch, isArm64Simulator: Boolean): TaskProvider<CompileSkikoCppTask> {
    val skiaNativeDir = registerOrGetSkiaDirProvider(os, arch, isIosSim = isArm64Simulator)

    val actionName = "compileNativeBridges".withSuffix(isIosSim = isArm64Simulator)

    return project.registerSkikoTask<CompileSkikoCppTask>(actionName, os, arch) {
        dependsOn(skiaNativeDir)
        val unpackedSkia = skiaNativeDir.get()

        compiler.set(compilerForTarget(os, arch))
        buildTargetOS.set(os)
        if (isArm64Simulator) {
            buildSuffix.set("sim")
        }
        buildTargetArch.set(arch)
        buildVariant.set(buildType)

        when (os) {
            OS.IOS -> {
                val sdkRoot = "/Applications/Xcode.app/Contents/Developer/Platforms"
                val iphoneOsSdk = "$sdkRoot/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk"
                val iphoneSimSdk = "$sdkRoot/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk"
                val iosArchFlags = when (arch) {
                    Arch.Arm64 -> arrayOf(
                        "-target", if (isArm64Simulator) "arm64-apple-ios-simulator" else "arm64-apple-ios",
                        "-isysroot", if (isArm64Simulator) iphoneSimSdk else iphoneOsSdk,
                        "-miphoneos-version-min=12.0"
                    )
                    Arch.X64 -> arrayOf(
                        "-target", "x86_64-apple-ios-simulator",
                        "-mios-version-min=12.0",
                        "-isysroot", iphoneSimSdk
                    )
                    else -> throw GradleException("Unsupported arch: $arch")
                }
                flags.set(listOf(
                    *iosArchFlags,
                    *buildType.clangFlags,
                    "-stdlib=libc++",
                    *skiaPreprocessorFlags(OS.IOS),
                ))
            }
            OS.MacOS -> {
                flags.set(listOf(
                    *buildType.clangFlags,
                    *skiaPreprocessorFlags(OS.MacOS)
                ))
            }
            OS.Linux -> {
                flags.set(listOf(
                    *buildType.clangFlags,
                    "-fno-rtti",
                    "-fno-exceptions",
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    "-D_GLIBCXX_USE_CXX11_ABI=0",
                    *skiaPreprocessorFlags(OS.Linux)
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

val Project.supportAndroid: Boolean
    get() = findProperty("skiko.android.enabled") == "true" // || isInIdea

kotlin {
    jvm("awt") {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        generateVersion(targetOs, targetArch)
    }

    if (supportAndroid) {
        jvm("android") {
            withJava() // This line needs to add Java sources in src/androidMain/java
            compilations.all {
                kotlinOptions.jvmTarget = "1.8"
            }
            // We need an additional attribute to distinguish between JVM variants.
            attributes {
                attributes.attribute(Attribute.of("ui", String::class.java), "android")
            }
            // TODO: seems incorrect.
            generateVersion(OS.Android, Arch.Arm64)
        }
    }

    if (supportJs) {
        js(IR) {
            moduleName = "skiko-kjs" // override the name to avoid name collision with a different skiko.js file
            browser {
                testTask {
                    dependsOn("linkWasm")
                    useKarma {
                        useChromeHeadless()
                        useConfigDirectory(project.projectDir.resolve("karma.config.d").resolve("js"))
                    }
                }
            }
            binaries.executable()
            generateVersion(OS.Wasm, Arch.Wasm)
        }
    }

    if (supportWasm) {
        wasm {
            moduleName = "skiko-kjs-wasm" // override the name to avoid name collision with a different skiko.js file
            browser {
                testTask {
                    dependsOn("linkWasm")
                    useKarma {
                        this.webpackConfig.experiments.add("topLevelAwait")
                        useChromeHeadlessWasmGc()
//                        useChromeCanaryHeadless()
                        useConfigDirectory(project.projectDir.resolve("karma.config.d").resolve("wasm"))
                    }
                }
            }
            generateVersion(OS.Wasm, Arch.Wasm)

            val test by compilations.getting
            project.tasks.named<Copy>(test.processResourcesTaskName) {
                from(linkWasm.first!!) {
                    include("*.wasm")
                }

                from(linkWasm.second!!) {
                    include("*.mjs")
                }
            }
        }
    }

    if (supportNative) {
        configureNativeTarget(OS.MacOS, Arch.X64, macosX64())
        configureNativeTarget(OS.MacOS, Arch.Arm64, macosArm64())
        configureNativeTarget(OS.Linux, Arch.X64, linuxX64())
        configureNativeTarget(OS.IOS, Arch.Arm64, iosArm64())
        configureNativeTarget(OS.IOS, Arch.X64, iosX64())
        configureNativeTarget(OS.IOS, Arch.Arm64, iosSimulatorArm64())
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutinesVersion")
            }
        }

        val awtMain by getting {
            dependsOn(jvmMain)
        }

        if (supportAndroid) {
            val androidMain by getting {
                dependsOn(jvmMain)
                dependencies {
                    compileOnly(files(androidJar()))
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
                }
            }
        }

        val jvmTest by creating {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
                implementation(kotlin("test-junit"))
                implementation(kotlin("test"))
            }
        }

        val awtTest by getting {
            dependsOn(jvmTest)
        }

        if (supportAndroid) {
            val androidTest by getting {
                dependsOn(jvmTest)
            }
        }

        if (supportJs || supportWasm || supportNative) {
            val nativeJsMain by creating {
                dependsOn(commonMain)
            }

            val nativeJsTest by creating {
                dependsOn(commonTest)
            }

            if (supportJs || supportWasm) {
                val jsWasmMain by creating {
                    dependsOn(nativeJsMain)
                }

                val jsWasmTest by creating {
                    dependsOn(nativeJsTest)
                    dependencies {
                        implementation(kotlin("test"))
                    }
                }

                if (supportJs) {
                    val jsMain by getting {
                        dependsOn(jsWasmMain)
                        dependencies {
                            implementation(kotlin("stdlib-js"))
                            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                            implementation("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
                        }
                    }

                    val jsTest by getting {
                        dependsOn(jsWasmTest)
                    }
                }

                if (supportWasm) {
                    val wasmJsMain by getting {
                        dependsOn(jsWasmMain)
                        dependencies {
//                            implementation(kotlin("stdlib-wasm"))
                            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                            implementation("org.jetbrains.kotlinx:atomicfu:$atomicFuVersion")
                        }
                    }
                    val wasmJsTest by getting {
                        dependsOn(jsWasmTest)
                    }
                }
            }

            if (supportNative) {
                all {
                    // Really ugly, see https://youtrack.jetbrains.com/issue/KT-46649 why it is required,
                    // note that setting it per source set still keeps it unset in commonized source sets.
                    languageSettings.optIn("kotlin.native.SymbolNameIsInternal")
                }
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
                val iosSimulatorArm64Main by getting {
                    dependsOn(iosMain)
                }
                val iosSimulatorArm64Test by getting {
                    dependsOn(iosTest)
                }
            }
        }
    }

    val metalTestTargets = listOf("iosX64", "iosSimulatorArm64")
    metalTestTargets.forEach { target: String ->
        if (kotlin.targets.names.contains(target)) {
            val testBinary = kotlin.targets.getByName<KotlinNativeTarget>(target).binaries.getTest("DEBUG")
            project.tasks.create(target + "TestWithMetal") {
                dependsOn(testBinary.linkTask)
                doLast {
                    val simulatorIdPropertyKey = "skiko.iosSimulatorUUID"
                    val simulatorId = findProperty(simulatorIdPropertyKey)?.toString()
                        ?: error("Property '$simulatorIdPropertyKey' not found. Pass it with -P$simulatorIdPropertyKey=...")

                    project.exec { commandLine("xcrun", "simctl", "boot", simulatorId) }
                    try {
                        project.exec { commandLine("xcrun", "simctl", "spawn", simulatorId, testBinary.outputFile) }
                    } finally {
                        project.exec { commandLine("xcrun", "simctl", "shutdown", simulatorId) }
                    }
                }
            }
        }
    }
}

fun configureCinterop(
    cinteropName: String,
    os: OS,
    arch: Arch,
    target: KotlinNativeTarget,
    targetString: String,
    linkerOpts: List<String>,
) {
    val tasks = target.project.tasks
    val taskNameSuffix = joinToTitleCamelCase(os.idWithSuffix(isIosSim = target.isIosSimArm64()), arch.id)
    val writeCInteropDef = tasks.register("writeCInteropDef$taskNameSuffix", WriteCInteropDefFile::class.java) {
        this.linkerOpts.set(linkerOpts)
        outputFile.set(project.layout.buildDirectory.file("cinterop/$targetString/skiko.def"))
    }
    tasks.withType(CInteropProcess::class.java).configureEach {
        if (konanTarget == target.konanTarget) {
            dependsOn(writeCInteropDef)
        }
    }
    target.compilations.getByName("main") {
        cinterops.create(cinteropName).apply {
            defFileProperty.set(writeCInteropDef.map { it.outputFile.get().asFile })
        }
    }
}

fun configureNativeTarget(os: OS, arch: Arch, target: KotlinNativeTarget) {
    if (!os.isCompatibleWithHost) return

    target.generateVersion(os, arch)
    val isArm64Simulator = target.isIosSimArm64()

    val targetString = "${os.idWithSuffix(isIosSim = isArm64Simulator)}-${arch.id}"

    val unzipper = registerOrGetSkiaDirProvider(os, arch, isArm64Simulator)
    val unpackedSkia = unzipper.get()
    val skiaDir = unpackedSkia.absolutePath

    val bridgesLibrary = "$buildDir/nativeBridges/static/$targetString/skiko-native-bridges-$targetString.a"
    val allLibraries = skiaStaticLibraries(skiaDir, targetString) + bridgesLibrary

    val skiaBinDir = "$skiaDir/out/${buildType.id}-$targetString"
    val linkerFlags = when (os) {
        OS.MacOS -> {
            val macFrameworks = listOfFrameworks("Metal", "CoreGraphics", "CoreText", "CoreServices")
            configureCinterop("skiko", os, arch, target, targetString, macFrameworks)
            mutableListOfLinkerOptions(macFrameworks)
        }
        OS.IOS -> {
            val iosFrameworks = listOfFrameworks("Metal", "CoreGraphics", "CoreText", "UIKit")
            // list of linker options to be included into klib, which are needed for skiko consumers
            // https://github.com/JetBrains/compose-multiplatform/issues/3178
            // Important! Removing or renaming cinterop-uikit publication might cause compile error
            // for projects depending on older Compose/Skiko transitively https://youtrack.jetbrains.com/issue/KT-60399
            configureCinterop("uikit", os, arch, target, targetString, iosFrameworks)
            mutableListOfLinkerOptions(iosFrameworks)
        }
        OS.Linux -> mutableListOfLinkerOptions(
            "-L/usr/lib/x86_64-linux-gnu",
            "-lfontconfig",
            "-lGL",
            // TODO: an ugly hack, Linux linker searches only unresolved symbols.
            "$skiaBinDir/libsksg.a",
            "$skiaBinDir/libskshaper.a",
            "$skiaBinDir/libskunicode.a",
            "$skiaBinDir/libskia.a"
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

    // For some reason since 1.8.0 we need to set freeCompilerArgs for binaries AND for compilations
    target.binaries.all {
        freeCompilerArgs += allLibraries.map { listOf("-include-binary", it) }.flatten() + linkerFlags
    }
    target.compilations.all {
        kotlinOptions {
            freeCompilerArgs += allLibraries.map { listOf("-include-binary", it) }.flatten() + linkerFlags
        }
    }

    val crossCompileTask = compileNativeBridgesTask(os, arch, isArm64Simulator = isArm64Simulator)

    // TODO: move to LinkSkikoTask.
    val actionName = "linkNativeBridges".withSuffix(isIosSim = isArm64Simulator)
    val linkTask = project.registerSkikoTask<Exec>(actionName, os, arch) {
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
        compileTaskProvider.configure {
            dependsOn(linkTask)
        }
    }
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

fun skiaPreprocessorFlags(os: OS): Array<String> {
    val base = listOf(
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
    )

    val perOs = when (os) {
        OS.MacOS -> listOf(
            "-DSK_SHAPER_CORETEXT_AVAILABLE",
            "-DSK_BUILD_FOR_MAC",
            "-DSK_METAL"
        )
        OS.IOS -> listOf(
            "-DSK_BUILD_FOR_IOS",
            "-DSK_SHAPER_CORETEXT_AVAILABLE",
            "-DSK_METAL"
        )
        OS.Windows -> listOf(
            "-DSK_BUILD_FOR_WIN",
            "-D_CRT_SECURE_NO_WARNINGS",
            "-D_HAS_EXCEPTIONS=0",
            "-DWIN32_LEAN_AND_MEAN",
            "-DNOMINMAX",
            "-DSK_GAMMA_APPLY_TO_A8",
            "-DSK_DIRECT3D"
        )
        OS.Linux -> listOf(
            "-DSK_BUILD_FOR_LINUX",
            "-D_GLIBCXX_USE_CXX11_ABI=0"
        )
        OS.Wasm -> listOf(
            "-DSKIKO_WASM"
        )
        OS.Android -> listOf(
            "-DSK_BUILD_FOR_ANDROID"
        )
        else -> TODO("unsupported $os")
    }

    return (base + perOs).toTypedArray()
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

val allJvmRuntimeJars = mutableMapOf<Pair<OS, Arch>, TaskProvider<Jar>>()

val skikoAwtJarForTests by project.tasks.registering(Jar::class) {
    archiveBaseName.set("skiko-awt-test")
    from(kotlin.jvm("awt").compilations["main"].output.allOutputs)
}
val skikoAwtRuntimeJarForTests = createSkikoJvmJarTask(targetOs, targetArch, skikoAwtJarForTests)
val skikoRuntimeDirForTests = skikoRuntimeDirForTestsTask(targetOs, targetArch, skikoAwtJarForTests, skikoAwtRuntimeJarForTests)
val skikoJarForTests = skikoJarForTestsTask(skikoRuntimeDirForTests)

if (supportAndroid) {
    val os = OS.Android
    val skikoAndroidJar by project.tasks.registering(Jar::class) {
        archiveBaseName.set("skiko-android")
        from(kotlin.jvm("android").compilations["main"].output.allOutputs)
    }
    for (arch in arrayOf(Arch.X64, Arch.Arm64)) {
        createSkikoJvmJarTask(os, arch, skikoAndroidJar)
    }
    tasks.getByName("publishAndroidPublicationToMavenLocal") {
        // It needs to be compatible with Gradle 8.1
        dependsOn(skikoAndroidJar)
    }
    tasks.getByName("generateMetadataFileForAndroidPublication") {
        // It needs to be compatible with Gradle 8.1
        dependsOn(skikoAndroidJar)
    }
}

fun createSkikoJvmJarTask(os: OS, arch: Arch, commonJar: TaskProvider<Jar>): TaskProvider<Jar> {
    val skiaBindingsDir = registerOrGetSkiaDirProvider(os, arch)
    val compileBindings = createCompileJvmBindingsTask(os, arch, skiaBindingsDir)
    val objcCompile = if (os == OS.MacOS) createObjcCompileTask(os, arch, skiaBindingsDir) else null
    val linkBindings =
        createLinkJvmBindings(os, arch, skiaBindingsDir, compileBindings, objcCompile)
    val maybeSign = maybeSignOrSealTask(os, arch, linkBindings)
    val nativeLib = maybeSign.map { it.outputFiles.get().single() }
    val createChecksums = createChecksumsTask(os, arch, nativeLib)
    val nativeFiles = mutableListOf(
        nativeLib,
        createChecksums.map { it.outputs.files.singleFile }
    )
    if (os == OS.Windows) {
        val target = targetId(os, arch)
        // Add ICU data files.
        nativeFiles.add(skiaBindingsDir.map { file(it.resolve("out/${buildType.id}-$target/icudtl.dat")) })
    }
    // For ARM macOS add x86 native code for compatibility.
    if (os == OS.MacOS && arch == Arch.Arm64) {
        val altArch = Arch.X64
        val skiaBindingsDir2 = registerOrGetSkiaDirProvider(os, altArch)
        val compileBindings2 = createCompileJvmBindingsTask(os, altArch, skiaBindingsDir2)
        val objcCompile2 = createObjcCompileTask(os, altArch, skiaBindingsDir2)
        val linkBindings2 =
            createLinkJvmBindings(os, altArch, skiaBindingsDir2, compileBindings2, objcCompile2)
        val maybeSign2 = maybeSignOrSealTask(os, altArch, linkBindings2)
        val nativeLib2 = maybeSign2.map { it.outputFiles.get().single() }
        val createChecksums2 = createChecksumsTask(os, altArch, nativeLib2)
        nativeFiles.add(nativeLib2)
        nativeFiles.add(createChecksums2.map { it.outputs.files.singleFile })
    }
    val skikoJvmRuntimeJar = skikoJvmRuntimeJarTask(os, arch, commonJar, nativeFiles)
    allJvmRuntimeJars[os to arch] = skikoJvmRuntimeJar
    return skikoJvmRuntimeJar
}

fun createObjcCompileTask(
    os: OS,
    arch: Arch,
    skiaJvmBindingsDir: Provider<File>
) = registerSkikoTask<CompileSkikoObjCTask>("objcCompile", os, arch) {
    dependsOn(skiaJvmBindingsDir)

    val srcDirs = projectDirs(
        "src/awtMain/objectiveC/${os.id}"
    )
    sourceRoots.set(srcDirs)
    val jdkHome = File(System.getProperty("java.home") ?: error("'java.home' is null"))

    includeHeadersNonRecursive(jdkHome.resolve("include"))
    includeHeadersNonRecursive(jdkHome.resolve("include/darwin"))
    includeHeadersNonRecursive(skiaHeadersDirs(skiaJvmBindingsDir.get()))
    includeHeadersNonRecursive(projectDir.resolve("src/awtMain/cpp/include"))
    includeHeadersNonRecursive(projectDir.resolve("src/commonMain/cpp/common/include"))

    compiler.set("clang")
    buildVariant.set(buildType)
    buildTargetOS.set(os)
    buildTargetArch.set(arch)
    flags.set(
        listOf(
            "-fobjc-arc",
            "-arch", if (arch == Arch.Arm64) "arm64" else "x86_64",
            *os.clangFlags,
            *buildType.clangFlags,
            *skiaPreprocessorFlags(os),
            "-fPIC"
        )
    )
}

fun Project.androidHomePath(): Provider<String> {
    val androidHomeFromSdkRoot: Provider<String> =
        project.providers.environmentVariable("ANDROID_SDK_ROOT")
    val androidHomeFromUserHome: Provider<String> =
        project.providers.systemProperty("user.home")
            .map { userHome ->
                listOf("Library/Android/sdk", ".android/sdk", "Android/sdk")
                    .map { "$userHome/$it" }
                    .firstOrNull { File(it).exists() }
                    ?: error("Define Android SDK via ANDROID_SDK_ROOT")
            }
    return androidHomeFromSdkRoot
        .orElse(androidHomeFromUserHome)
}

fun Project.androidClangFor(targetArch: Arch, version: String = "30"): Provider<String> {
    val androidArch = when (targetArch) {
        Arch.Arm64 -> "aarch64"
        Arch.X64 -> "x86_64"
        else -> throw GradleException("unsupported $targetArch")
    }
    val hostOsArch = when (hostOs) {
        OS.MacOS -> "darwin-x86_64"
        OS.Linux -> "linux-x86_64"
        OS.Windows -> "windows-x86_64"
        else -> throw GradleException("unsupported $hostOs")
    }
    val ndkPath = project.providers
        .environmentVariable("ANDROID_NDK_HOME")
        .orEmpty()
        .map { ndkHomeEnv ->
            ndkHomeEnv.ifEmpty {
                val androidHome = androidHomePath().get()
                val ndkDir1 = file("$androidHome/ndk")
                val candidates1 = if (ndkDir1.exists()) ndkDir1.list() else emptyArray()
                val ndkVersion =
                    arrayOf(*(candidates1.map { "ndk/$it" }.sortedDescending()).toTypedArray(), "ndk-bundle").find {
                        File(androidHome).resolve(it).exists()
                    } ?: throw GradleException("Cannot find NDK, is it installed (Tools/SDK Manager)?")
                "$androidHome/$ndkVersion"
            }
        }
    return ndkPath.map { ndkPath ->
        var clangBinaryName = "$androidArch-linux-android$version-clang++"
        if (hostOs.isWindows) {
            clangBinaryName += ".cmd"
        }
        "$ndkPath/toolchains/llvm/prebuilt/$hostOsArch/bin/$clangBinaryName"
    }
}

fun Provider<String>.orEmpty(): Provider<String> =
    orElse("")

fun Project.androidJar(askedVersion: String = ""): Provider<File> =
    androidHomePath().map { androidHomePath ->
        val androidHome = File(androidHomePath)
        val version = if (askedVersion.isEmpty()) {
            val platformsDir = androidHome.resolve("platforms")
            val versions = platformsDir.list().orEmpty()
            versions.maxByOrNull { name -> // possible name: "android-32", "android-33-ext4"
                name.split("-").getOrNull(1)?.toInt() ?: 0
            } ?: error(
                buildString {
                    appendLine("'$platformsDir' does not contain any directories matching expected 'android-NUMBER' format: ${versions}")
                }
            )
        } else {
            "android-$askedVersion"
        }
        androidHome.resolve("platforms/$version/android.jar").also {
            println("Skiko task androidJar uses android SDK in $it")
        }
    }

fun createCompileJvmBindingsTask(
    targetOs: OS,
    targetArch: Arch,
    skiaJvmBindingsDir: Provider<File>
) = project.registerSkikoTask<CompileSkikoCppTask>("compileJvmBindings", targetOs, targetArch) {
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
    buildSuffix.set("jvm")
    buildVariant.set(buildType)

    val srcDirs = projectDirs(
        "src/commonMain/cpp/common",
        "src/jvmMain/cpp/common",
        "src/awtMain/cpp/common",
        "src/awtMain/cpp/${targetOs.id}",
        "src/jvmTest/cpp"
    )
    sourceRoots.set(srcDirs)
    if (targetOs != OS.Android) includeHeadersNonRecursive(jdkHome.resolve("include"))
    includeHeadersNonRecursive(skiaHeadersDirs(skiaJvmBindingsDir.get()))
    includeHeadersNonRecursive(projectDir.resolve("src/awtMain/cpp/include"))
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
                "-arch", if (targetArch == Arch.Arm64) "arm64" else "x86_64",
                "-fPIC",
                "-stdlib=libc++",
                "-fvisibility=hidden",
                "-fvisibility-inlines-hidden"
            )
        }
        OS.Linux -> {
            includeHeadersNonRecursive(jdkHome.resolve("include/linux"))
            includeHeadersNonRecursive(runPkgConfig("dbus-1"))
            osFlags = arrayOf(
                *buildType.clangFlags,
                "-DGL_GLEXT_PROTOTYPES",
                "-fPIC",
                "-fno-rtti",
                "-fno-exceptions",
                "-fvisibility=hidden",
                "-fvisibility-inlines-hidden"
            )
        }
        OS.Windows -> {
            compiler.set(windowsSdkPaths.compiler.absolutePath)
            includeHeadersNonRecursive(windowsSdkPaths.includeDirs)
            includeHeadersNonRecursive(jdkHome.resolve("include/win32"))
            osFlags = arrayOf(
                "/nologo",
                *buildType.msvcCompilerFlags,
                "/utf-8",
                "/GR-", // no-RTTI.
                "/FS", // Due to an error when building in Teamcity. https://docs.microsoft.com/en-us/cpp/build/reference/fs-force-synchronous-pdb-writes
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
                "-fPIC"
            )
        }
        OS.Wasm, OS.IOS -> error("Should not reach here")
    }

    flags.set(
        listOf(
            *skiaPreprocessorFlags(targetOs),
            *osFlags
        )
    )
}

fun createLinkJvmBindings(
    targetOs: OS,
    targetArch: Arch,
    skiaJvmBindingsDir: Provider<File>,
    compileTask: TaskProvider<CompileSkikoCppTask>,
    objcCompileTask: TaskProvider<CompileSkikoObjCTask>?
) = project.registerSkikoTask<LinkSkikoTask>("linkJvmBindings", targetOs, targetArch) {
    val target = targetId(targetOs, targetArch)
    val skiaBinSubdir = "out/${buildType.id}-$target"
    val skiaBinDir = skiaJvmBindingsDir.get().absolutePath + "/" + skiaBinSubdir
    val osFlags: Array<String>

    libFiles = fileTree(skiaJvmBindingsDir.map { it.resolve(skiaBinSubdir) }) {
        include(if (targetOs.isWindows) "*.lib" else "*.a")
    }

    dependsOn(compileTask)
    objectFiles = fileTree(compileTask.map { it.outDir.get() }) {
        include("**/*.o")
    }
    val libNamePrefix = if (targetOs.isWindows) "skiko" else "libskiko"
    libOutputFileName.set("$libNamePrefix-${targetOs.id}-${targetArch.id}${targetOs.dynamicLibExt}")
    buildTargetOS.set(targetOs)
    buildSuffix.set("jvm")
    buildTargetArch.set(targetArch)
    buildVariant.set(buildType)
    linker.set(linkerForTarget(targetOs, targetArch))

    when (targetOs) {
        OS.MacOS -> {
            dependsOn(objcCompileTask!!)
            objectFiles += fileTree(objcCompileTask.map { it.outDir.get() }) {
                include("**/*.o")
            }
            osFlags = arrayOf(
                *targetOs.clangFlags,
                "-arch", if (targetArch == Arch.Arm64) "arm64" else "x86_64",
                "-shared",
                "-dead_strip",
                "-lobjc",
                "-install_name", "./${libOutputFileName.get()}",
                "-current_version", skiko.planeDeployVersion,
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
                "-lX11",
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
            osFlags = mutableListOf<String>().apply {
                addAll(buildType.msvcLinkerFlags)
                addAll(
                    arrayOf(
                        "/NOLOGO",
                        "/DLL",
                        "Advapi32.lib",
                        "gdi32.lib",
                        "Dwmapi.lib",
                        "opengl32.lib",
                        "shcore.lib",
                        "user32.lib",
                    )
                )
                if (buildType == SkiaBuildType.DEBUG) add("dxgi.lib")
            }.toTypedArray()
        }
        OS.Android -> {
            osFlags = arrayOf(
                "-shared",
                "-static-libstdc++",
                "-lGLESv3",
                "-lEGL",
                "-llog",
                "-landroid",
                // Hack to fix problem with linker not always finding certain declarations.
                "$skiaBinDir/libskia.a",
            )
            linker.set(androidClangFor(targetArch))
        }
        OS.Wasm, OS.IOS -> {
            throw GradleException("This task shalln't be used with $targetOs")
        }
    }
    flags.set(listOf(*osFlags))
}

fun KotlinTarget.generateVersion(
    targetOs: OS,
    targetArch: Arch
) {
    val targetName = this.name
    val isArm64Simulator = isIosSimArm64()
    val generatedDir = project.layout.buildDirectory.dir("generated/$targetName")
    val generateVersionTask = project.registerSkikoTask<DefaultTask>(
        "generateVersion${toTitleCase(platformType.name)}".withSuffix(isIosSim = isArm64Simulator),
        targetOs,
        targetArch
    ) {
        inputs.property("buildType", buildType.id)
        outputs.dir(generatedDir)
        doFirst {
            val outDir = generatedDir.get().asFile
            outDir.deleteRecursively()
            outDir.mkdirs()
            val out = "$outDir/Version.kt"

            val target = "${targetOs.id}-${targetArch.id}"
            val skiaTag = project.property("dependencies.skia.$target") as String
            File(out).writeText(
                """
                package org.jetbrains.skiko
                object Version {
                  val skiko = "${skiko.deployVersion}"
                  val skia = "$skiaTag"
                }
                """.trimIndent()
            )
        }
    }

    val compilation = compilations["main"] ?: error("Could not find 'main' compilation for target '$this'")
    compilation.compileKotlinTaskProvider.configure {
        dependsOn(generateVersionTask)
        (this as KotlinCompileTool).source(generatedDir.get().asFile)
    }
}

fun maybeSignOrSealTask(
    targetOs: OS,
    targetArch: Arch,
    linkJvmBindings: Provider<LinkSkikoTask>
) = project.registerSkikoTask<SealAndSignSharedLibraryTask>("maybeSign", targetOs, targetArch) {
    dependsOn(linkJvmBindings)

    val linkOutputFile = linkJvmBindings.map { task ->
        task.outDir.get().asFile.walk().single { it.name.endsWith(targetOs.dynamicLibExt) }.absoluteFile
    }
    libFile.set(project.layout.file(linkOutputFile))
    val target = targetId(targetOs, targetArch)
    outDir.set(project.layout.buildDirectory.dir("maybe-signed-$target"))

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

fun createChecksumsTask(
    targetOs: OS,
    targetArch: Arch,
    fileToChecksum: Provider<File>
) = project.registerSkikoTask<Checksum>("createChecksums", targetOs, targetArch) {

    files = project.files(fileToChecksum)
    algorithm = Checksum.Algorithm.SHA256
    outputDir = file("$buildDir/checksums-${targetId(targetOs, targetArch)}")
}

fun skikoJvmRuntimeJarTask(
    targetOs: OS,
    targetArch: Arch,
    awtJar: TaskProvider<Jar>,
    nativeFiles: List<Provider<File>>
) = project.registerSkikoTask<Jar>("skikoJvmRuntimeJar", targetOs, targetArch) {
    dependsOn(awtJar)
    val target = targetId(targetOs, targetArch)
    archiveBaseName.set("skiko-$target")
    nativeFiles.forEach { provider -> from(provider) }
}

fun skikoRuntimeDirForTestsTask(
    targetOs: OS,
    targetArch: Arch,
    skikoJvmJar: Provider<Jar>,
    skikoJvmRuntimeJar: Provider<Jar>
) = project.registerSkikoTask<Copy>("skikoRuntimeDirForTests", targetOs, targetArch) {
    dependsOn(skikoJvmJar, skikoJvmRuntimeJar)
    from(zipTree(skikoJvmJar.flatMap { it.archiveFile }))
    from(zipTree(skikoJvmRuntimeJar.flatMap { it.archiveFile }))
    duplicatesStrategy = DuplicatesStrategy.WARN
    destinationDir = project.buildDir.resolve("skiko-runtime-for-tests")
}

fun skikoJarForTestsTask(
    runtimeDirForTestsTask: Provider<Copy>
) = project.registerSkikoTask<Jar>("skikoJvmJarForTests") {
    dependsOn(runtimeDirForTestsTask)
    from(runtimeDirForTestsTask.map { it.destinationDir })
    archiveFileName.set("skiko-runtime-for-tests.jar")
}

tasks.withType<Test>().configureEach {
    dependsOn(skikoRuntimeDirForTests)
    dependsOn(skikoJarForTests)
    options {
        val dir = skikoRuntimeDirForTests.map { it.destinationDir }.get()
        systemProperty("skiko.library.path", dir)
        val jar = skikoJarForTests.get().outputs.files.files.single { it.name.endsWith(".jar") }
        systemProperty("skiko.jar.path", jar.absolutePath)

        systemProperty("skiko.test.screenshots.dir", File(project.projectDir, "src/jvmTest/screenshots").absolutePath)
        systemProperty("skiko.test.font.dir", File(project.projectDir, "src/commonTest/resources/fonts").absolutePath)

        val testingOnCI = System.getProperty("skiko.test.onci", "false").toBoolean()
        val canRunPerformanceTests = testingOnCI
        val canRunUiTests = testingOnCI || System.getProperty("os.name") != "Mac OS X"
        systemProperty(
            "skiko.test.performance.enabled",
            System.getProperty("skiko.test.performance.enabled", canRunPerformanceTests.toString())
        )
        systemProperty("skiko.test.ui.enabled", System.getProperty("skiko.test.ui.enabled", canRunUiTests.toString()))
        systemProperty("skiko.test.ui.renderApi", System.getProperty("skiko.test.ui.renderApi", "all"))

        // Tests should be deterministic, so disable scaling.
        // On MacOs we need the actual scale, otherwise we will have aliased screenshots because of scaling.
        if (System.getProperty("os.name") != "Mac OS X") {
            systemProperty("sun.java2d.dpiaware", "false")
            systemProperty("sun.java2d.uiScale", "1")
        }
    }

    jvmArgs = listOf("--add-opens", "java.desktop/sun.font=ALL-UNNAMED")
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
            groupId = SkikoArtifacts.groupId

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

        allJvmRuntimeJars.forEach { entry ->
            val os = entry.key.first
            val arch = entry.key.second
            create<MavenPublication>("skikoJvmRuntime${toTitleCase(os.id)}${toTitleCase(arch.id)}") {
                pomNameForPublication[name] = "Skiko JVM Runtime for ${os.name} ${arch.name}"
                artifactId = SkikoArtifacts.jvmRuntimeArtifactIdFor(os, arch)
                afterEvaluate {
                    artifact(entry.value.map { it.archiveFile.get() })
                    artifact(emptySourcesJar)
                }
                pom.withXml {
                    asNode().appendNode("dependencies")
                        .appendNode("dependency").apply {
                            appendNode("groupId", SkikoArtifacts.groupId)
                            appendNode("artifactId", SkikoArtifacts.jvmArtifactId)
                            appendNode("version", skiko.deployVersion)
                            appendNode("scope", "compile")
                        }
                }
            }
        }

        if (supportJs || supportWasm) {
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
    configureSignAndPublishDependencies()
}


fun configureSignAndPublishDependencies() {
    if (supportWasm) {
        tasks.forEach { task ->
            val name = task.name
            val publishJs = "publishJsPublicationTo"
            val publishWasm = "publishSkikoWasmRuntimePublicationTo"
            val publishWasmPub = "publishWasmJsPublicationTo"
            val signWasm = "signSkikoWasmRuntimePublication"
            val signJs = "signJsPublication"
            val signWasmPub = "signWasmJsPublication"

            when {
                name.startsWith(publishJs) -> task.dependsOn(signWasm, signWasmPub)
                name.startsWith(publishWasm) -> task.dependsOn(signJs)
                name.startsWith(publishWasmPub) -> task.dependsOn(signJs)
                name.startsWith(signWasmPub) -> task.dependsOn(signWasm)
            }
        }
    }
    if (supportAndroid) {
        tasks.forEach { task ->
            val name = task.name
            val signAndroid = "signAndroidPublication"
            val generateMetadata = "generateMetadataFileForAndroidPublication"
            val publishAndroid = "publishAndroidPublicationTo"
            val publishX64 = "publishSkikoJvmRuntimeAndroidX64PublicationTo"
            val publishArm64 = "publishSkikoJvmRuntimeAndroidArm64PublicationTo"
            val signX64 = "signSkikoJvmRuntimeAndroidX64Publication"
            val signArm64 = "signSkikoJvmRuntimeAndroidArm64Publication"
            val skikoAndroidJar = "skikoAndroidJar"

            when {
                name.startsWith(signAndroid) || name.startsWith(generateMetadata) -> {
                    task.dependsOn(skikoAndroidJar)
                }
                name.startsWith(publishAndroid) -> {
                    task.dependsOn(signX64, signArm64)
                }
                name.startsWith(publishX64) -> {
                    task.dependsOn(signAndroid, signArm64)
                }
                name.startsWith(publishArm64) -> {
                    task.dependsOn(signX64, signAndroid)
                }
            }
        }
    }
}

/**
 * Do not call inside tasks.register or tasks.call callback
 * (tasks' registration during other task's registration is prohibited)
 */
fun registerOrGetSkiaDirProvider(os: OS, arch: Arch, isIosSim: Boolean = false): Provider<File> {
    val taskNameSuffix = joinToTitleCamelCase(buildType.id, os.idWithSuffix(isIosSim = isIosSim), arch.id)
    val skiaRelease = skiko.skiaReleaseFor(os, arch, buildType, isIosSim)
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

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    val nodeExtension = rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>()
    nodeExtension.nodeVersion = "16.0.0"
    // Kotlin/JS has a bug preventing compilation on non-x86 Linux machines,
    // see https://youtrack.jetbrains.com/issue/KT-48631
    // It always downloads and uses x86 version, so on those architectures
    if (hostOs == OS.Linux && hostArch != Arch.X64) {
        nodeExtension.download = false
    }
}

tasks.withType<JavaCompile> {
    // Workaround to configure Java sources on Android (src/androidMain/java)
    targetCompatibility = "1.8"
    sourceCompatibility = "1.8"
}

project.tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xwasm-enable-array-range-checks", "-Xir-dce=true", "-Xskip-prerelease-check",
    )
}

if (supportJs && supportWasm) {
//    project.afterEvaluate {
//        //Disable jsWasmMain intermediate sourceset publication
//        tasks.named("compileJsWasmMainKotlinMetadata") {
//            enabled = false
//        }
//    }
    project.tasks.whenTaskAdded {
        if (name == "compileJsWasmMainKotlinMetadata") {
            enabled = false
        }
    }
}

tasks.getByName("publishSkikoWasmRuntimePublicationToComposeRepoRepository")
    .dependsOn("publishWasmJsPublicationToComposeRepoRepository")


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile>().configureEach {
    // https://youtrack.jetbrains.com/issue/KT-56583
    compilerOptions.freeCompilerArgs.add("-XXLanguage:+ImplicitSignedToUnsignedIntegerConversion")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += "-Xopt-in=kotlinx.cinterop.ExperimentalForeignApi"
    }
}

//tasks.withType(KotlinCompile::class.java).configureEach {
//    kotlinOptions {
//        freeCompilerArgs += "-Xopt-in=kotlinx.cinterop.ExperimentalForeignApi"
//    }
//}