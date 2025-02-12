import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File

enum class OS(
    val id: String,
    val clangFlags: Array<String>
) {
    Linux("linux", arrayOf()),
    Android("android", arrayOf()),
    Windows("windows", arrayOf()),
    MacOS("macos", arrayOf("-mmacosx-version-min=10.13")),
    Wasm("wasm", arrayOf()),
    IOS("ios", arrayOf()),
    TVOS("tvos", arrayOf())
    ;

    val isWindows
        get() = this == Windows

    val isMacOs
        get() = this == MacOS

    fun idWithSuffix(isUikitSim: Boolean = false): String {
        return id + if (isUikitSim) "Sim" else ""
    }
}

val OS.isCompatibleWithHost: Boolean
    get() = when (this) {
        OS.Linux -> hostOs == OS.Linux
        OS.Windows -> hostOs == OS.Windows
        OS.MacOS, OS.IOS, OS.TVOS -> hostOs == OS.MacOS
        OS.Wasm -> true
        OS.Android -> true
    }

fun compilerForTarget(os: OS, arch: Arch): String =
    when (os) {
        OS.Linux -> when (arch) {
            Arch.X64 -> "g++"
            Arch.Arm64 -> "clang++"
            Arch.Wasm -> "Unexpected combination: $os & $arch"
        }
        OS.Android -> "clang++"
        OS.Windows -> "clang-cl.exe"
        OS.MacOS, OS.IOS, OS.TVOS -> "clang++"
        OS.Wasm -> "emcc"
    }

fun linkerForTarget(os: OS, arch: Arch): String =
    if (os.isWindows) "lld-link.exe" else compilerForTarget(os, arch)

val OS.dynamicLibExt: String
    get() = when (this) {
        OS.Linux, OS.Android -> ".so"
        OS.Windows -> ".dll"
        OS.MacOS, OS.IOS, OS.TVOS -> ".dylib"
        OS.Wasm -> ".wasm"
    }


enum class Arch(val id: String) {
    X64("x64"),
    Arm64("arm64"),
    Wasm("wasm");

    companion object {
        fun byName(name: String) = Arch.values().find { it.id == name }
    }
}

enum class SkiaBuildType(
    val id: String,
    val flags: Array<String>,
    val clangFlags: Array<String>,
    val winCompilerFlags: Array<String>,
    val winLinkerFlags: Array<String>
) {
    DEBUG(
        id = "Debug",
        flags = arrayOf("-DSK_DEBUG"),
        clangFlags = arrayOf("-std=c++17", "-g", "-DSK_TRIVIAL_ABI=[[clang::trivial_abi]]"),
        winCompilerFlags = arrayOf("/Zi", "/std:c++17"),
        winLinkerFlags = arrayOf("/DEBUG"),
    ),
    RELEASE(
        id = "Release",
        flags = arrayOf("-DNDEBUG"),
        clangFlags = arrayOf("-std=c++17", "-O3"),
        winCompilerFlags = arrayOf("/O2", "/std:c++17"),
        winLinkerFlags = arrayOf("/DEBUG"),
    );
    override fun toString() = id
}

val hostOs by lazy {
    val osName = System.getProperty("os.name")
    when {
        osName == "Mac OS X" -> OS.MacOS
        osName == "Linux" -> OS.Linux
        osName.startsWith("Win") -> OS.Windows
        else -> throw Error("Unknown OS $osName")
    }
}

val hostArch by lazy {
    val osArch = System.getProperty("os.arch")
    when (osArch) {
        "x86_64", "amd64" -> Arch.X64
        "aarch64" -> Arch.Arm64
        else -> throw Error("Unknown arch $osArch")
    }
}

fun targetId(os: OS, arch: Arch) = "${os.id}-${arch.id}"

val jdkHome = System.getProperty("java.home") ?: error("'java.home' is null")

class SkikoProperties(private val myProject: Project) {
    val isTeamcityCIBuild: Boolean
        get() = myProject.hasProperty("teamcity")

    val planeDeployVersion: String = myProject.property("deploy.version") as String

    val deployVersion: String
        get() {
            val main = if (isRelease) planeDeployVersion else "$planeDeployVersion-SNAPSHOT"
            var metadata = if (buildType == SkiaBuildType.DEBUG) "+debug" else ""
            metadata += if (isWasmBuildWithProfiling) "+profiling" else ""
            return main + metadata
        }

    val isRelease: Boolean
        get() = myProject.findProperty("deploy.release") == "true"

    val buildType: SkiaBuildType
        get() = if (myProject.findProperty("skiko.debug") == "true") SkiaBuildType.DEBUG else SkiaBuildType.RELEASE

    val isWasmBuildWithProfiling: Boolean
        get() = myProject.findProperty("skiko.wasm.withProfiling") == "true"

    val targetArch: Arch
        get() = myProject.findProperty("skiko.arch")?.toString()?.let(Arch::byName) ?: hostArch

    val includeTestHelpers: Boolean
        get() = !isRelease

    val releaseGithubVersion: String
        get() = (myProject.property("release.github.version") as String)

    val releaseGithubCommit: String
        get() = (myProject.property("release.github.commit") as String)

    val visualStudioBuildToolsDir: File?
        get() = System.getenv()["SKIKO_VSBT_PATH"]?.let { File(it) }?.takeIf { it.isDirectory }

    // todo: make compatible with the configuration cache
    val skiaDir: File?
        get() = (System.getenv()["SKIA_DIR"] ?: System.getProperty("skia.dir") ?: myProject.findProperty("skia.dir")
            ?.toString())?.let { skiaDirProp ->
                val file = File(skiaDirProp)
                if (!file.isDirectory) throw (GradleException("\"skiko.skiaDir\" property was explicitly set to ${skiaDirProp} which is not resolved as a directory"))
                file
            }

    val composeRepoUrl: String
        get() = System.getenv("COMPOSE_REPO_URL") ?: "https://maven.pkg.jetbrains.space/public/p/compose/dev"

    val composeRepoUserName: String
        get() = System.getenv("COMPOSE_REPO_USERNAME") ?: ""

    val composeRepoKey: String
        get() = System.getenv("COMPOSE_REPO_KEY") ?: ""

    val signHost: String?
        get() = System.getenv("JB_SIGN_HOST")
            ?: (myProject.findProperty("sign_host") as? String)

    val signUser: String?
        get() = System.getenv("JB_SIGN_USER")
            ?: (myProject.findProperty("sign_host_user") as? String)

    val signToken: String?
        get() = System.getenv("JB_SIGN_TOKEN")
            ?: (myProject.findProperty("sign_host_token") as? String)

    val dependenciesDir: File
        get() = myProject.rootProject.projectDir.resolve("dependencies")
}

object SkikoArtifacts {
    val groupId = "org.jetbrains.skiko"
    // names are also used in samples, e.g. samples/SkijaInjectSample/build.gradle
    val commonArtifactId = "skiko"
    val jvmArtifactId = "skiko-awt"
    // an artifact (klib) for k/js targets
    val jsArtifactId = "skiko-js"
    // an artifact (klib) for k/wasm targets
    val wasmArtifactId = "skiko-wasm-js"
    // an artifact with skiko.wasm and supporting js code - jar
    val jsWasmArtifactId = "skiko-js-wasm-runtime"
    fun jvmRuntimeArtifactIdFor(os: OS, arch: Arch) =
        if (os == OS.Android)
            "skiko-android-runtime-${arch.id}"
        else
            "skiko-awt-runtime-${targetId(os, arch)}"
    // Using custom name like skiko-<Os>-<Arch> (with a dash)
    // does not seem possible (at least without adding a dash to a target's tasks),
    // so we're using the default naming pattern instead.
    // See https://youtrack.jetbrains.com/issue/KT-50001.
    fun nativeArtifactIdFor(os: OS, arch: Arch, isUikitSim: Boolean = false) =
        "skiko-${os.id + if (isUikitSim) "simulator" else ""}${arch.id}"
}
