package tasks.configuration

import Arch
import CompileSkikoCppTask
import OS
import SkiaBuildType
import SkikoProjectContext
import SkikoProperties
import WriteCInteropDefFile
import compilerForTarget
import isCompatibleWithHost
import joinToTitleCamelCase
import listOfFrameworks
import mutableListOfLinkerOptions
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool
import projectDirs
import registerOrGetSkiaDirProvider
import registerSkikoTask
import toTitleCase
import java.io.File

fun String.withSuffix(isIosSim: Boolean = false) =
    this + if (isIosSim) "Sim" else ""

fun KotlinTarget.isIosSimArm64() =
    name.contains("iosSimulatorArm64", ignoreCase = true)

fun SkikoProjectContext.compileNativeBridgesTask(
    os: OS, arch: Arch, isArm64Simulator: Boolean
): TaskProvider<CompileSkikoCppTask> = with (this.project) {
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
                    *skiaPreprocessorFlags(OS.IOS, buildType),
                ))
            }
            OS.MacOS -> {
                flags.set(listOf(
                    *buildType.clangFlags,
                    *skiaPreprocessorFlags(OS.MacOS, buildType)
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
                    *skiaPreprocessorFlags(OS.Linux, buildType)
                ))
            }
            OS.Windows -> {
                flags.set(listOf(
                    *buildType.clangFlags,
                    *skiaPreprocessorFlags(OS.Windows, buildType)
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

fun skiaStaticLibraries(skiaDir: String, targetString: String, buildType: SkiaBuildType, os: OS, project: Project): List<String> {
    val skiaBinSubdir = "$skiaDir/out/${buildType.id}-$targetString"
    val libPrefix = if(!os.isWindows) "lib" else ""
    val ignoreLibs = project.property("skiko.link.ignore.libs.${os.id}")?.toString()
        ?.split(',') ?: emptyList()

    return listOf(
        "${libPrefix}skresources.${os.libExtension}",
        "${libPrefix}skparagraph.${os.libExtension}",
        "${libPrefix}skia.${os.libExtension}",
        "${libPrefix}icu.${os.libExtension}",
        "${libPrefix}skottie.${os.libExtension}",
        "${libPrefix}svg.${os.libExtension}",
        "libpng.${os.libExtension}",
        "libwebp_sse41.${os.libExtension}",
        "${libPrefix}sksg.${os.libExtension}",
        "${libPrefix}skunicode.${os.libExtension}",
        "libwebp.${os.libExtension}",
        "${libPrefix}dng_sdk.${os.libExtension}",
        "${libPrefix}piex.${os.libExtension}",
        "${libPrefix}harfbuzz.${os.libExtension}",
        "${libPrefix}expat.${os.libExtension}",
        "${libPrefix}zlib.${os.libExtension}",
        "libjpeg.${os.libExtension}",
        "${libPrefix}skshaper.${os.libExtension}",
    ).filter {
        lib -> ignoreLibs.all { !lib.contains(it) }
    }.map {
        "$skiaBinSubdir/$it"
    }
}

fun SkikoProjectContext.configureNativeTarget(os: OS, arch: Arch, target: KotlinNativeTarget) = with(this.project) {
    if (!os.isCompatibleWithHost) return

    target.generateVersion(os, arch, skiko)
    val isArm64Simulator = target.isIosSimArm64()

    val targetString = "${os.idWithSuffix(isIosSim = isArm64Simulator)}-${arch.id}"

    val unzipper = registerOrGetSkiaDirProvider(os, arch, isArm64Simulator)
    val unpackedSkia = unzipper.get()
    val skiaDir = unpackedSkia.absolutePath

    val bridgesLibrary = "$buildDir/nativeBridges/static/$targetString/skiko-native-bridges-$targetString.${os.libExtension}"
    val allLibraries = skiaStaticLibraries(skiaDir, targetString, buildType, os, project) + bridgesLibrary

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
        OS.Windows -> {
            val windowsLibs = listOf(
                *windowsSdkPaths.libDirs.map { "-L\"${it.absolutePath.replace("\\", "/")}\"" }.toTypedArray()
            )
            configureCinterop("skiko", os, arch, target, targetString, windowsLibs)
            mutableListOfLinkerOptions(windowsLibs)
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
        val libExt = if(os.isWindows) "lib" else "a"
        val staticLib = "$outDir/skiko-native-bridges-$targetString.$libExt"
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
            OS.Windows -> {
                executable = "llvm-ar"
                argumentProviders.add { listOf("-crs", staticLib) }
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

fun KotlinMultiplatformExtension.configureIOSTestsWithMetal(project: Project) {
    val metalTestTargets = listOf("iosX64", "iosSimulatorArm64")
    metalTestTargets.forEach { target: String ->
        if (targets.names.contains(target)) {
            val testBinary = targets.getByName<KotlinNativeTarget>(target).binaries.getTest("DEBUG")
            project.tasks.create(target + "TestWithMetal") {
                dependsOn(testBinary.linkTask)
                doLast {
                    val simulatorIdPropertyKey = "skiko.iosSimulatorUUID"
                    val simulatorId = project.findProperty(simulatorIdPropertyKey)?.toString()
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