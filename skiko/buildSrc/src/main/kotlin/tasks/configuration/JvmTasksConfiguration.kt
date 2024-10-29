package tasks.configuration

import Arch
import CompileSkikoCppTask
import CompileSkikoObjCTask
import LinkSkikoTask
import OS
import SealAndSignSharedLibraryTask
import SkiaBuildType
import SkikoProjectContext
import compilerForTarget
import dynamicLibExt
import hostArch
import hostOs
import joinToTitleCamelCase
import linkerForTarget
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import org.gradle.util.internal.VersionNumber
import projectDirs
import registerOrGetSkiaDirProvider
import registerSkikoTask
import runPkgConfig
import targetId
import java.io.File

fun SkikoProjectContext.createCompileJvmBindingsTask(
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
    val projectDir = project.projectDir
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
            compiler.set(project.androidClangFor(targetArch))
            osFlags = arrayOf(
                *buildType.clangFlags,
                "-fno-rtti",
                "-fno-exceptions",
                "-fvisibility=hidden",
                "-fPIC"
            )
        }
        OS.Wasm, OS.IOS, OS.TVOS -> error("Should not reach here")
    }

    flags.set(
        listOf(
            *skiaPreprocessorFlags(targetOs, buildType),
            *osFlags
        )
    )
}

fun Provider<String>.orEmpty(): Provider<String> =
    orElse("")

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


fun SkikoProjectContext.createObjcCompileTask(
    os: OS,
    arch: Arch,
    skiaJvmBindingsDir: Provider<File>
) = project.registerSkikoTask<CompileSkikoObjCTask>("objcCompile", os, arch) {
    dependsOn(skiaJvmBindingsDir)

    val srcDirs = projectDirs(
        "src/awtMain/objectiveC/${os.id}"
    )
    sourceRoots.set(srcDirs)
    val jdkHome = File(System.getProperty("java.home") ?: error("'java.home' is null"))

    includeHeadersNonRecursive(jdkHome.resolve("include"))
    includeHeadersNonRecursive(jdkHome.resolve("include/darwin"))
    includeHeadersNonRecursive(skiaHeadersDirs(skiaJvmBindingsDir.get()))
    val projectDir = project.projectDir
    includeHeadersNonRecursive(projectDir.resolve("src/awtMain/cpp/include"))
    includeHeadersNonRecursive(projectDir.resolve("src/commonMain/cpp/common/include"))
    includeHeadersNonRecursive(projectDir.resolve("src/jvmMain/cpp"))

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
            *skiaPreprocessorFlags(os, buildType),
            "-fPIC"
        )
    )
}


fun SkikoProjectContext.createLinkJvmBindings(
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

    libFiles = project.fileTree(skiaJvmBindingsDir.map { it.resolve(skiaBinSubdir) }) {
        include(if (targetOs.isWindows) "*.lib" else "*.a")
    }

    dependsOn(compileTask)
    objectFiles = project.fileTree(compileTask.map { it.outDir.get() }) {
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
            objectFiles += project.fileTree(objcCompileTask.map { it.outDir.get() }) {
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
                "$skiaBinDir/libskunicode_core.a",
                "$skiaBinDir/libskunicode_icu.a",
                "$skiaBinDir/libskshaper.a",
            )
        }
        OS.Windows -> {
            linker.set(windowsSdkPaths.linker.absolutePath)
            libDirs.set(windowsSdkPaths.libDirs)
            osFlags = mutableListOf<String>().apply {
                addAll(buildType.msvcLinkerFlags)
                addAll(
                    arrayOf(
                        // ignore https://learn.microsoft.com/en-us/cpp/error-messages/tool-errors/linker-tools-warning-lnk4217
                        // because we link OpenGl dynamically, defining functions in our own file in OpenGLLibrary.cc
                        "/ignore:4217"
                    )
                )
                // workaround for VS Build Tools 2022 (17.2+) change
                // https://developercommunity.visualstudio.com/t/-imp-std-init-once-complete-unresolved-external-sy/1684365#T-N10041864
                if (windowsSdkPaths.toolchainVersion >= VersionNumber.parse("14.32")) {
                    addAll(
                        arrayOf(
                            "/ALTERNATENAME:__imp___std_init_once_begin_initialize=__imp_InitOnceBeginInitialize",
                            "/ALTERNATENAME:__imp___std_init_once_complete=__imp_InitOnceComplete"
                        )
                    )
                }
                addAll(
                    arrayOf(
                        "/NOLOGO",
                        "/DLL",
                        "Advapi32.lib",
                        "gdi32.lib",
                        "Dwmapi.lib",
                        "ole32.lib",
                        "Propsys.lib",
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
            linker.set(project.androidClangFor(targetArch))
        }
        OS.Wasm, OS.IOS, OS.TVOS -> {
            throw GradleException("This task shalln't be used with $targetOs")
        }
    }
    flags.set(listOf(*osFlags))
}

private val Arch.darwinSignClientName: String
    get() = when (this) {
        Arch.X64 -> "codesign-client-darwin-amd64"
        Arch.Arm64 -> "codesign-client-darwin-arm64"
        else -> error("Unexpected Arch = $this for codesign-client")
    }

fun SkikoProjectContext.createDownloadCodeSignClientDarwinTask(
    targetOs: OS,
    hostArch: Arch
) = project.registerSkikoTask<de.undercouch.gradle.tasks.download.Download>("downloadCodeSignClient", targetOs, hostArch) {
    val fileUrl = "https://codesign-distribution.labs.jb.gg/${hostArch.darwinSignClientName}"

    src(fileUrl)
    dest(project.layout.buildDirectory)
    overwrite(false)

    // only Teamcity agents have access to download the codesign-client executable file
    enabled = this@createDownloadCodeSignClientDarwinTask.skiko.isTeamcityCIBuild

    doLast {
        val downloadedFile = project.layout.buildDirectory.get().asFile.resolve(hostArch.darwinSignClientName)
         downloadedFile.setExecutable(true)
    }
}

fun SkikoProjectContext.maybeSignOrSealTask(
    targetOs: OS,
    targetArch: Arch,
    linkJvmBindings: Provider<LinkSkikoTask>
) = project.registerSkikoTask<SealAndSignSharedLibraryTask>("maybeSign", targetOs, targetArch) {
    dependsOn(linkJvmBindings)

    if (targetOs.isMacOs) {
        val downloadCodesignClientTask = "downloadCodeSignClient" + joinToTitleCamelCase(targetOs.id, hostArch.id)
        dependsOn(project.tasks.getByName(downloadCodesignClientTask))
    }

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

    if (hostOs == OS.MacOS && this@maybeSignOrSealTask.skiko.isTeamcityCIBuild) {
        codesignClient.set(project.layout.buildDirectory.file(hostArch.darwinSignClientName))
    }
    signHost.set(skiko.signHost)
    signUser.set(skiko.signUser)
    signToken.set(skiko.signToken)
}

fun SkikoProjectContext.skikoJvmRuntimeJarTask(
    targetOs: OS,
    targetArch: Arch,
    awtJar: TaskProvider<Jar>,
    nativeFiles: List<Provider<File>>
) = project.registerSkikoTask<Jar>("skikoJvmRuntimeJar", targetOs, targetArch) {
    dependsOn(awtJar)
    val target = targetId(targetOs, targetArch)
    archiveBaseName.set("skiko")
    archiveClassifier.set(target)
    nativeFiles.forEach { provider -> from(provider) }
}

fun SkikoProjectContext.createSkikoJvmJarTask(os: OS, arch: Arch, commonJar: TaskProvider<Jar>): TaskProvider<Jar> = with(this.project) {
    val skiaBindingsDir = registerOrGetSkiaDirProvider(os, arch)
    val compileBindings = createCompileJvmBindingsTask(os, arch, skiaBindingsDir)
    val objcCompile = if (os == OS.MacOS) createObjcCompileTask(os, arch, skiaBindingsDir) else null
    val linkBindings =
        createLinkJvmBindings(os, arch, skiaBindingsDir, compileBindings, objcCompile)
    if (os.isMacOs) {
        createDownloadCodeSignClientDarwinTask(os, hostArch)
    }
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
        allJvmRuntimeJars[os to altArch] = skikoJvmRuntimeJarTask(os, altArch, commonJar, nativeFiles)
    }
    val skikoJvmRuntimeJar = skikoJvmRuntimeJarTask(os, arch, commonJar, nativeFiles)
    allJvmRuntimeJars[os to arch] = skikoJvmRuntimeJar
    return skikoJvmRuntimeJar
}

fun SkikoProjectContext.skikoRuntimeDirForTestsTask(
    targetOs: OS,
    targetArch: Arch,
    skikoJvmJar: Provider<Jar>,
    skikoJvmRuntimeJar: Provider<Jar>
) = project.registerSkikoTask<Copy>("skikoRuntimeDirForTests", targetOs, targetArch) {
    dependsOn(skikoJvmJar, skikoJvmRuntimeJar)
    from(project.zipTree(skikoJvmJar.flatMap { it.archiveFile }))
    from(project.zipTree(skikoJvmRuntimeJar.flatMap { it.archiveFile }))
    duplicatesStrategy = DuplicatesStrategy.WARN
    destinationDir = project.buildDir.resolve("skiko-runtime-for-tests")
}

fun SkikoProjectContext.skikoJarForTestsTask(
    runtimeDirForTestsTask: Provider<Copy>
) = project.registerSkikoTask<Jar>("skikoJvmJarForTests") {
    dependsOn(runtimeDirForTestsTask)
    from(runtimeDirForTestsTask.map { it.destinationDir })
    archiveFileName.set("skiko-runtime-for-tests.jar")
}

fun SkikoProjectContext.setupJvmTestTask(skikoAwtJarForTests: TaskProvider<Jar>, targetOs: OS, targetArch: Arch) = with(project) {
    val skikoAwtRuntimeJarForTests = createSkikoJvmJarTask(targetOs, targetArch, skikoAwtJarForTests)
    val skikoRuntimeDirForTests = skikoRuntimeDirForTestsTask(targetOs, targetArch, skikoAwtJarForTests, skikoAwtRuntimeJarForTests)
    val skikoJarForTests = skikoJarForTestsTask(skikoRuntimeDirForTests)

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
            systemProperty("skiko.test.debug", buildType == SkiaBuildType.DEBUG)

            // Tests should be deterministic, so disable scaling.
            // On MacOs we need the actual scale, otherwise we will have aliased screenshots because of scaling.
            if (System.getProperty("os.name") != "Mac OS X") {
                systemProperty("sun.java2d.dpiaware", "false")
                systemProperty("sun.java2d.uiScale", "1")
            }
        }

        jvmArgs = listOf("--add-opens", "java.desktop/sun.font=ALL-UNNAMED")
    }
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
fun Project.androidJar(askedVersion: String = ""): Provider<File> =
    androidHomePath().map { androidHomePath ->
        val androidHome = File(androidHomePath)
        val version = if (askedVersion.isEmpty()) {
            val platformsDir = androidHome.resolve("platforms")
            val versions = platformsDir.list().orEmpty()
            versions.maxByOrNull { name -> // possible name: "android-32", "android-33-ext4"
                name.split("-").getOrNull(1)?.toIntOrNull() ?: 0
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