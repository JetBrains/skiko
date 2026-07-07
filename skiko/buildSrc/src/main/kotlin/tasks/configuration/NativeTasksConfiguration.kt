package tasks.configuration

import Arch
import CompileSkikoCppTask
import GenerateWaylandProtocolsTask
import PatchSkiaSymbolsTask
import runPkgConfigVariable
import OS
import SkikoProjectContext
import WriteCInteropDefFile
import compilerForTarget
import dsl.TargetEnv
import hostArch
import isCompatibleWithHost
import joinToTitleCamelCase
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
import symbols.HideSkiaSymbolsTask
import java.io.File
import kotlin.collections.plus

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
        val sdkPath = it + "/Platforms"
        println("findXcodeSdkRoot = $sdkPath")
        sdkPath
    } ?: error("gradle property `skiko.ci.xcodehome` is not set")
}

fun SkikoProjectContext.compileNativeBridgesTask(
    os: OS, arch: Arch, isUikitSim: Boolean,
    waylandProtocols: TaskProvider<GenerateWaylandProtocolsTask>? = null,
): TaskProvider<CompileSkikoCppTask> = with (this.project) {
    val skiaNativeDir = registerOrGetSkiaDirProvider(os, arch, isUikitSim = isUikitSim)

    val actionName = "compileNativeBridges".withSuffix(isUikitSim = isUikitSim)

    return project.registerSkikoTask<CompileSkikoCppTask>(actionName, os, arch) {
        dependsOn(skiaNativeDir)
        val unpackedSkia = skiaNativeDir.get()

        compiler.set(compilerForTarget(os, arch))
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
                compiler.set(project.appleToolchainExecutableOrDefault("clang++", compiler.get()))
                flags.set(listOf(
                    *project.appleMacOsSdkFlags().toTypedArray(),
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
                val archFlags = if (arch == Arch.Arm64) arrayOf(
                    // Always inline atomics for ARM64 to prevent linking incompatibility issues after updating GCC to 10
                    "-mno-outline-atomics",
                ) else arrayOf()
                val linuxFlags = mutableListOf(
                    *buildType.clangFlags,
                    "-fPIC",
                    "-fno-rtti",
                    "-fno-exceptions",
                    "-fvisibility=hidden",
                    "-fvisibility-inlines-hidden",
                    *archFlags,
                    *skiaPreprocessorFlags(OS.Linux, buildType)
                )
                // Add sysroot for ARM64 cross-compilation
                if (arch == Arch.Arm64 && hostArch != Arch.Arm64) {
                    val armToolchainSysroot = file("/opt/arm-gnu-toolchain/aarch64-none-linux-gnu/libc")
                    if (armToolchainSysroot.exists()) {
                        linuxFlags.add(0, "--sysroot=${armToolchainSysroot.absolutePath}")
                    } else {
                        // Distro cross toolchain (e.g. Ubuntu g++-aarch64-linux-gnu): libc comes
                        // from the toolchain's own sysroot; arch-neutral X11/GL/fontconfig headers
                        // are taken from the host after the sysroot search paths.
                        linuxFlags.add("-idirafter /usr/include")
                    }
                }
                flags.set(linuxFlags)
            }
            else -> throw GradleException("$os not yet supported")
        }

        val srcDirs = projectDirs("src/commonMain/cpp/common", "src/nativeNativeJs/cpp", "src/nativeJsMain/cpp") +
                if (skiko.includeTestHelpers) projectDirs("src/nativeJsTest/cpp") else emptyList()
        sourceRoots.set(srcDirs)
        if (waylandProtocols != null) {
            dependsOn(waylandProtocols)
            sourceRoots.add(waylandProtocols.flatMap { it.outDir })
            // wayland-util.h comes from the host even when a cross-compilation sysroot is active
            flags.add("-idirafter /usr/include")
        }

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
        outputFile.set(project.layout.buildDirectory.file("cinterop/$targetString/$cinteropName.def"))
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

/**
 * The Wayland protocols the Linux native backend binds beyond the core `wayland.xml`
 * (which ships pre-generated in libwayland-client itself): `xdg-shell` for toplevel
 * windowing, `viewporter` and `fractional-scale-v1` for HiDPI scale.
 */
private val waylandProtocolXmlPaths = listOf(
    "stable/xdg-shell/xdg-shell.xml",
    "stable/viewporter/viewporter.xml",
    "staging/fractional-scale/fractional-scale-v1.xml",
)

fun SkikoProjectContext.registerGenerateWaylandProtocolsTask(
    os: OS, arch: Arch, targetString: String
): TaskProvider<GenerateWaylandProtocolsTask> = with(this.project) {
    registerSkikoTask<GenerateWaylandProtocolsTask>("generateWaylandProtocols", os, arch) {
        val protocolsRoot = runPkgConfigVariable("wayland-protocols", "pkgdatadir")
        protocolXmlFiles.from(waylandProtocolXmlPaths.map { "$protocolsRoot/$it" })
        outDir.set(layout.buildDirectory.dir("generated/wayland/$targetString"))
    }
}

/**
 * Registers the `waylandegl` cinterop: libwayland-client + wayland-egl + EGL plus the
 * client headers codegen'd by [GenerateWaylandProtocolsTask]. The def file is generated
 * per target because the generated-header include path points into the build directory.
 */
fun configureWaylandEglCinterop(
    arch: Arch,
    target: KotlinNativeTarget,
    targetString: String,
    generateProtocols: TaskProvider<GenerateWaylandProtocolsTask>,
) {
    val project = target.project
    val gnuArch = if (arch == Arch.Arm64) "aarch64" else "x86_64"
    val writeCInteropDef = project.tasks.register(
        "writeWaylandEglCInteropDef${joinToTitleCamelCase(OS.Linux.id, arch.id)}",
        WriteCInteropDefFile::class.java
    ) {
        headers.set(
            listOf("wayland-client.h", "wayland-egl.h", "EGL/egl.h", "EGL/eglext.h") +
                    waylandProtocolXmlPaths.map {
                        "${File(it).nameWithoutExtension}-client-protocol.h"
                    }
        )
        compilerOpts.set(generateProtocols.flatMap { it.outDir }.map {
            listOf(
                "-D_GNU_SOURCE",
                "-I${it.asFile.absolutePath}",
                // Host headers after the Konan sysroot, same reasoning as x11gl.def
                "-idirafter", "/usr/include",
                "-idirafter", "/usr/include/$gnuArch-linux-gnu",
            )
        })
        linkerOpts.set(
            listOf("-L/usr/lib/$gnuArch-linux-gnu", "-lwayland-client", "-lwayland-egl", "-lEGL")
        )
        outputFile.set(project.layout.buildDirectory.file("cinterop/$targetString/waylandegl.def"))
    }
    project.tasks.withType(CInteropProcess::class.java).configureEach {
        if (konanTarget == target.konanTarget) {
            dependsOn(writeCInteropDef)
            dependsOn(generateProtocols)
        }
    }
    target.compilations.getByName("main") {
        cinterops.create("waylandegl").apply {
            definitionFile.set(writeCInteropDef.flatMap { it.outputFile })
        }
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

    val bridgesLibrary = layout.buildDirectory.file("nativeBridges/static/$targetString/$nativeBridgesLibPrefix-$targetString.a")
    val bridgesLibraryPath = bridgesLibrary.get().asFile.absolutePath

    // For iOS/tvOS we patch every library so that public Skia symbols are
    // renamed, preventing conflicts when multiple Skia copies are present in
    // the same app binary. Many C++ Itanium-mangled symbols are rewritten by
    // inserting the `skiko` namespace into the mangled name; C symbols and
    // unsupported shapes fall back to a "_skiko" suffix.
    val requiresSymbolPatching = os == OS.IOS || os == OS.TVOS
    val patchedLibsDir = layout.buildDirectory.dir("nativeBridges/patched/$targetString").get().asFile

    val skiaBinDir = "$skiaDir/out/${buildType.id}-$targetString"
    val resolvedBinaryInputs = resolveBinaryInputs(os, arch, TargetEnv.NATIVE, skiaBinDir)
    val nativeArchives = resolvedBinaryInputs.staticArchivePaths.distinct()
    val allLibraries = if (requiresSymbolPatching) {
        nativeArchives.map { lib ->
            "${patchedLibsDir.absolutePath}/${File(lib).name}"
        } + "${patchedLibsDir.absolutePath}/$nativeBridgesLibPrefix-$targetString.a"
    } else {
        nativeArchives + bridgesLibraryPath
    }

    val hiddenSymbolsFile = layout.buildDirectory.file(
        "nativeHiddenSymbols/$targetString/${if (os.isLinux) "symbols.map" else "symbols.txt"}"
    )
    val hiddenSymbolSources = if (requiresSymbolPatching) {
        val patchedBaseLibraries = nativeArchives.map { lib -> "${patchedLibsDir.absolutePath}/${File(lib).name}" }
        val patchedBridgeLibrary = "${patchedLibsDir.absolutePath}/${nativeBridgesLibPrefix}-$targetString.a"
        patchedBaseLibraries + patchedBridgeLibrary
    } else {
        nativeArchives
    }
    val hideSkiaSymbols = project.registerSkikoTask<HideSkiaSymbolsTask>(
        "hideSkiaSymbols".withSuffix(isUikitSim = isUikitSim),
        os,
        arch
    ) {
        targetOs.set(os)
        symbolExtractorCommand.set(if (os == OS.IOS || os == OS.TVOS) listOf("xcrun", "nm") else listOf("nm"))
        symbolSourceLibraries.from(hiddenSymbolSources.map { File(it) })
        outputFile.set(hiddenSymbolsFile)
    }

    // Feeds both the waylandegl cinterop (client headers) and the native bridges
    // compile (protocol marshalling code).
    val waylandProtocolsTask =
        if (os == OS.Linux) registerGenerateWaylandProtocolsTask(os, arch, targetString) else null

    val linkerFlags = when (os) {
        OS.MacOS -> {
            configureCinterop(cinteropName, os, arch, target, targetString, resolvedBinaryInputs.frameworks)
            mutableListOfLinkerOptions(resolvedBinaryInputs.frameworks + resolvedBinaryInputs.linkFlags + listOf(
                "-unexported_symbols_list",
                hiddenSymbolsFile.get().asFile.absolutePath
            ))
        }
        OS.IOS -> {
            // list of linker options to be included into klib, which are needed for skiko consumers
            // https://github.com/JetBrains/compose-multiplatform/issues/3178
            // Important! Removing or renaming cinterop-uikit publication might cause compile error
            // for projects depending on older Compose/Skiko transitively https://youtrack.jetbrains.com/issue/KT-60399
            configureCinterop("uikit", os, arch, target, targetString, resolvedBinaryInputs.frameworks)
            mutableListOfLinkerOptions(resolvedBinaryInputs.frameworks + resolvedBinaryInputs.linkFlags + listOf(
                "-unexported_symbols_list",
                hiddenSymbolsFile.get().asFile.absolutePath
            ))
        }
        OS.TVOS -> {
            configureCinterop("uikit", os, arch, target, targetString, resolvedBinaryInputs.frameworks)
            mutableListOfLinkerOptions(resolvedBinaryInputs.frameworks + resolvedBinaryInputs.linkFlags + listOf(
                "-unexported_symbols_list",
                hiddenSymbolsFile.get().asFile.absolutePath
            ))
        }
        OS.Linux -> {
            target.compilations.getByName("main") {
                cinterops.create("x11gl").apply {
                    definitionFile.set(project.file("src/nativeInterop/cinterop/x11gl.def"))
                }
            }
            configureWaylandEglCinterop(arch, target, targetString, waylandProtocolsTask!!)
            val options = mutableListOf(
                "-L/usr/lib64",
                "-L/usr/lib/${if (arch == Arch.Arm64) "aarch64" else "x86_64"}-linux-gnu",
            )
            options.addAll(resolvedBinaryInputs.directStaticArchivePaths)
            options.addAll(resolvedBinaryInputs.dynamicLibNames.map { "-l$it" })
            options.addAll(resolvedBinaryInputs.linkFlags)
            // When cross-compiling for ARM64 from x64, use the ARM toolchain sysroot
            if (arch == Arch.Arm64 && hostArch != Arch.Arm64) {
                // ARM GNU toolchain sysroot paths
                options.add(0, "-L/opt/arm-gnu-toolchain/aarch64-none-linux-gnu/libc/lib64")
                options.add(1, "-L/opt/arm-gnu-toolchain/aarch64-none-linux-gnu/libc/usr/lib64")
            }
            options.add("--version-script=${hiddenSymbolsFile.get().asFile.absolutePath}")
            mutableListOfLinkerOptions(options)
        }
        else -> mutableListOf()
    }
    if (skiko.includeTestHelpers) {
        linkerFlags.addAll(when (os) {
            OS.Linux -> listOf(
                "-linker-option", "-lX11",
                "-linker-option", "-lGLX",
                "-linker-option", "-lwayland-client",
                "-linker-option", "-lwayland-egl",
                "-linker-option", "-lEGL",
            )
            else -> emptyList()
        })
    }

    // For some reason since 1.8.0 we need to set freeCompilerArgs for binaries AND for compilations
    target.binaries.all {
        freeCompilerArgs += allLibraries.map { listOf("-include-binary", it) }.flatten() + linkerFlags
    }


    target.compilations.all {
        compilerOptions.configure {
            freeCompilerArgs.addAll(
                allLibraries.flatMap { listOf("-include-binary", it) } + linkerFlags
            )
        }
    }

    val crossCompileTask =
        compileNativeBridgesTask(os, arch, isUikitSim = isUikitSim, waylandProtocols = waylandProtocolsTask)

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
        val staticLib = "$nativeBridgesLibPrefix-$targetString.a"
        workingDir = outDir
        when (os) {
            OS.Linux -> {
                executable = if (arch == Arch.Arm64 && hostArch != Arch.Arm64) "aarch64-linux-gnu-ar" else "ar"
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

    // For iOS/tvOS: patch all Skia + skiko-bridge symbols after linking.
    val compilationDependency = if (requiresSymbolPatching) {
        val patchActionName = "patchSkikoSymbols".withSuffix(isUikitSim = isUikitSim)
        project.registerSkikoTask<PatchSkiaSymbolsTask>(patchActionName, os, arch) {
            dependsOn(unzipper)
            dependsOn(linkTask)
            skiaLibs.set(nativeArchives.map { File(it) })
            symbolSourceLibs.set(emptyList())
            skikoBridge.set(File(bridgesLibraryPath))
            outputDir.set(patchedLibsDir)
        }
    } else {
        linkTask
    }

    hideSkiaSymbols.configure {
        dependsOn(unzipper)
        dependsOn(compilationDependency)
    }

    target.compilations.all {
        compileTaskProvider.configure {
            dependsOn(hideSkiaSymbols)
        }
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
