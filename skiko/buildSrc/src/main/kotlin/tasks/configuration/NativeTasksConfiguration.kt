package tasks.configuration

import Arch
import CompileSkikoCppTask
import OS
import SkiaBuildType
import SkikoProjectContext
import WriteCInteropDefFile
import compilerForTarget
import hostArch
import isCompatibleWithHost
import joinToTitleCamelCase
import listOfFrameworks
import mutableListOfLinkerOptions
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import projectDirs
import registerOrGetSkiaDirProvider
import registerSkikoTask
import java.io.File

fun String.withSuffix(isUikitSim: Boolean = false) =
    this + if (isUikitSim) "Sim" else ""

fun KotlinTarget.isUikitSimulator() =
    name.contains("Simulator", ignoreCase = true) || name == "tvosX64" // x64 tvOS is implicitly a simulator

fun Project.findXcodeSdkRoot(): String {
    val defaultPath = "/Applications/Xcode.app/Contents/Developer/Platforms"
    if (File(defaultPath).exists()) {
        return defaultPath.also {
            println("findXcodeSdkRoot = $it")
        }
    }

    return (project.property("skiko.ci.xcodehome") as? String)?.let {
        val sdkPath = "$it/Platforms"
        println("findXcodeSdkRoot = $sdkPath")
        sdkPath
    } ?: error("gradle property `skiko.ci.xcodehome` is not set")
}

fun SkikoProjectContext.compileNativeBridgesTask(
    os: OS, arch: Arch, isUikitSim: Boolean
): TaskProvider<CompileSkikoCppTask> = with (this.project) {
    val skiaNativeDir = registerOrGetSkiaDirProvider(os, arch, isUikitSim = isUikitSim)

    val setupMultistrapTask = if (os == OS.Linux && arch == Arch.Arm64 && hostArch != Arch.Arm64) {
        setupMultistrapTask(os, arch, isUikitSim = isUikitSim)
    } else {
        null
    }

    val actionName = "compileNativeBridges".withSuffix(isUikitSim = isUikitSim)

    return project.registerSkikoTask<CompileSkikoCppTask>(actionName, os, arch) {
        dependsOn(skiaNativeDir)
        val unpackedSkia = skiaNativeDir.get()

        setupMultistrapTask?.let { dependsOn(it) }

        compiler.set(compilerForTarget(os, arch, isJvm = false))
        buildTargetOS.set(os)
        if (isUikitSim) {
            buildSuffix.set("sim")
        }
        buildTargetArch.set(arch)
        buildVariant.set(buildType)

        when (os) {
            OS.IOS -> {
                val sdkRoot = findXcodeSdkRoot()
                val iphoneOsSdk = "$sdkRoot/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk"
                val iphoneSimSdk = "$sdkRoot/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk"
                val iosArchFlags = when (arch) {
                    Arch.Arm64 -> arrayOf(
                        "-target", if (isUikitSim) "arm64-apple-ios-simulator" else "arm64-apple-ios",
                        "-isysroot", if (isUikitSim) iphoneSimSdk else iphoneOsSdk,
                        if (isUikitSim) "-mios-simulator-version-min=12.0" else "-mios-version-min=12.0"
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
                    *skiaPreprocessorFlags(OS.IOS, buildType),
                ))
            }
            OS.TVOS -> {
                val sdkRoot = findXcodeSdkRoot()
                val tvOsSdk = "$sdkRoot/AppleTVOS.platform/Developer/SDKs/AppleTVOS.sdk"
                val tvSimSdk = "$sdkRoot/AppleTVSimulator.platform/Developer/SDKs/AppleTVSimulator.sdk"
                val tvosArchFlags = when (arch) {
                    Arch.Arm64 -> arrayOf(
                        "-target", if (isUikitSim) "arm64-apple-tvos-simulator" else "arm64-apple-tvos",
                        if (isUikitSim) "-mappletvsimulator-version-min=12.0" else "-mappletvos-version-min=12.0" ,
                        "-isysroot", if (isUikitSim) tvSimSdk else tvOsSdk,
                    )
                    Arch.X64 -> arrayOf(
                        "-target", "x86_64-apple-tvos-simulator",
                        "-mappletvsimulator-version-min=12.0",
                        "-isysroot", tvSimSdk
                    )
                    else -> throw GradleException("Unsupported arch: $arch")
                }
                flags.set(listOf(
                    *tvosArchFlags,
                    *buildType.clangFlags,
                    "-stdlib=libc++",
                    *skiaPreprocessorFlags(OS.TVOS, buildType),
                ))
            }
            OS.MacOS -> {
                flags.set(listOf(
                    *buildType.clangFlags,
                    *skiaPreprocessorFlags(OS.MacOS, buildType),
                    when(arch) {
                        Arch.Arm64 -> "-arch arm64"
                        Arch.X64 -> "-arch x86_64"
                        else -> error("Unexpected arch: $arch for $os")
                    }
                ))
            }
            OS.Linux -> {
                flags.set(listOfNotNull(
                    *buildType.clangFlags,
                    "-fPIC",
                    "-fno-rtti",
                    "-fno-exceptions",
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    "-D_GLIBCXX_USE_CXX11_ABI=0",
                    *skiaPreprocessorFlags(OS.Linux, buildType)
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


fun configureCinterop(
    cinteropName: String,
    os: OS,
    arch: Arch,
    target: KotlinNativeTarget,
    targetString: String,
    linkerOpts: List<String>,
) {
    val tasks = target.project.tasks
    val taskNameSuffix = joinToTitleCamelCase(os.idWithSuffix(isUikitSim = target.isUikitSimulator()), arch.id)
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
            definitionFile.set(writeCInteropDef.flatMap { it.outputFile })
        }
    }
}

fun skiaStaticLibraries(skiaDir: String, targetString: String, buildType: SkiaBuildType): List<String> {
    val skiaBinSubdir = "$skiaDir/out/${buildType.id}-$targetString"
    return listOf(
        "libskresources.a",
        "libskparagraph.a",
        "libskia.a",
        "libicu.a",
        "libjsonreader.a",
        "libskottie.a",
        "libsvg.a",
        "libpng.a",
        "libwebp_sse41.a",
        "libsksg.a",
        "libskunicode_core.a",
        "libskunicode_icu.a",
        "libwebp.a",
        "libdng_sdk.a",
        "libpiex.a",
        "libharfbuzz.a",
        "libexpat.a",
        "libzlib.a",
        "libjpeg.a",
        "libskshaper.a"
    ).map {
        "$skiaBinSubdir/$it"
    }
}

fun SkikoProjectContext.configureNativeTarget(os: OS, arch: Arch, target: KotlinNativeTarget) = with(this.project) {
    if (!os.isCompatibleWithHost) return

    target.generateVersion(os, arch, skiko)
    val isUikitSim = target.isUikitSimulator()

    val targetString = "${os.idWithSuffix(isUikitSim = isUikitSim)}-${arch.id}"

    val unzipper = registerOrGetSkiaDirProvider(os, arch, isUikitSim)
    val unpackedSkia = unzipper.get()
    val skiaDir = unpackedSkia.absolutePath

    val bridgesLibrary = layout.buildDirectory.file("nativeBridges/static/$targetString/skiko-native-bridges-$targetString.a")
    val bridgesLibraryPath = bridgesLibrary.get().asFile.absolutePath
    val allLibraries = skiaStaticLibraries(skiaDir, targetString, buildType) + bridgesLibraryPath

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
        OS.TVOS -> {
            val tvosFrameworks = listOfFrameworks("Metal", "CoreGraphics", "CoreText", "UIKit")
            configureCinterop("uikit", os, arch, target, targetString, tvosFrameworks)
            mutableListOfLinkerOptions(tvosFrameworks)
        }
        OS.Linux -> {
            val options = mutableListOf(
                "-L/usr/lib/${if (arch == Arch.Arm64) "aarch64" else "x86_64"}-linux-gnu",
                // Libraries required by RGFW/KGFW and Skia
                "-Wl,--allow-shlib-undefined",
                "-lX11",
                "-lXext",
                "-lXrandr",
                "-lXcursor",
                "-lXi",
                "-lXinerama",
                "-lGL",
                "-lstdc++",
                "-lfontconfig",
                "-lfreetype",
                "-lpthread",
                "-lc",
                // Avoid linking system harfbuzz/icu to prevent version mismatches; Skia pack provides required deps
                // "-lharfbuzz",
                // "-licui18n",
                "-ldl",
                // TODO: an ugly hack, Linux linker searches only unresolved symbols.
                "$skiaBinDir/libskparagraph.a",
                "$skiaBinDir/libskottie.a",
                "$skiaBinDir/libjsonreader.a",
                "$skiaBinDir/libsksg.a",
                "$skiaBinDir/libskshaper.a",
                "$skiaBinDir/libskunicode_core.a",
                "$skiaBinDir/libskunicode_icu.a",
                "$skiaBinDir/libharfbuzz.a",
                "$skiaBinDir/libicu.a",
                "$skiaBinDir/libskia.a"
            )
            if (arch == Arch.Arm64 && hostArch != Arch.Arm64) {
                val buildDir = project.layout.buildDirectory.get().asFile
                options.add(0, "-L$buildDir/multistrap-arm64/usr/lib")
                options.add(1, "-L$buildDir/multistrap-arm64/usr/lib/aarch64-linux-gnu")
            }
            mutableListOfLinkerOptions(options)
        }
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
        compileTaskProvider.configure {
            compilerOptions.freeCompilerArgs.addAll(
                allLibraries.flatMap { listOf("-include-binary", it) } + linkerFlags
            )
        }
    }

    val crossCompileTask = compileNativeBridgesTask(os, arch, isUikitSim = isUikitSim)

    // TODO: move to LinkSkikoTask.
    val actionName = "linkNativeBridges".withSuffix(isUikitSim = isUikitSim)
    val linkTask = project.registerSkikoTask<Exec>(actionName, os, arch) {
        dependsOn(crossCompileTask)
        val objectFilesDir = crossCompileTask.map { it.outDir.get() }
        val objectFiles = project.fileTree(objectFilesDir) {
            include("**/*.o")
        }
        inputs.files(objectFiles)
        val outDir = layout.buildDirectory.dir("nativeBridges/static/$targetString").get().asFile
        val staticLib = "skiko-native-bridges-$targetString.a"
        workingDir = outDir
        when (os) {
            OS.Linux -> {
                executable = "ar"
                argumentProviders.add { listOf("-crs", staticLib) }
            }
            OS.MacOS, OS.IOS, OS.TVOS -> {
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

/**
 * This installs dependencies needed for cross compiling Skiko on Linux.
 * https://youtrack.jetbrains.com/issue/KT-36871
 */
fun SkikoProjectContext.setupMultistrapTask(
    os: OS, arch: Arch, isUikitSim: Boolean
): TaskProvider<Exec> = with (this.project) {
    require(os == OS.Linux)
    require(arch == Arch.Arm64)

    val actionName = "setupMultistrap".withSuffix(isUikitSim = isUikitSim)

    return project.registerSkikoTask<Exec>(actionName, os, arch) {
        workingDir(projectDir)
//        commandLine("multistrap", "-f", "multistrap-config-${arch.id}")
        commandLine("who")
    }
}

fun KotlinMultiplatformExtension.configureIOSTestsWithMetal(project: Project) {
    val metalTestTargets = listOf("iosX64", "iosSimulatorArm64")
    metalTestTargets.forEach { target: String ->
        if (targets.names.contains(target)) {
            val testBinary = targets.getByName<KotlinNativeTarget>(target).binaries.getTest("DEBUG")
            project.tasks.register(target + "TestWithMetal") {
                dependsOn(testBinary.linkTaskProvider)
                doLast {
                    val simulatorIdPropertyKey = "skiko.iosSimulatorUUID"
                    val simulatorId = project.findProperty(simulatorIdPropertyKey)?.toString()
                        ?: error("Property '$simulatorIdPropertyKey' not found. Pass it with -P$simulatorIdPropertyKey=...")

                    project.providers.exec { commandLine("xcrun", "simctl", "boot", simulatorId) }
                    try {
                        project.providers.exec { commandLine("xcrun", "simctl", "spawn", simulatorId, testBinary.outputFile) }
                    } finally {
                        project.providers.exec { commandLine("xcrun", "simctl", "shutdown", simulatorId) }
                    }
                }
            }
        }
    }
}
