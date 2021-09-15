import org.gradle.api.Project
import java.io.File

enum class OS(val id: String, val clangFlags: Array<String>) {
    Linux("linux", arrayOf()),
    Windows("windows", arrayOf()),
    MacOS("macos", arrayOf("-mmacosx-version-min=10.13")),
    Wasm("wasm", arrayOf())
    ;

    val isWindows
        get() = this == Windows
}

enum class Arch(val id: String, val clangFlags: Array<String>) {
    X64("x64", arrayOf("-arch", "x86_64")),
    Arm64("arm64", arrayOf("-arch", "arm64")),
    Wasm("wasm", arrayOf("-std=c++17", "--bind", "-DSKIKO_WASM"))
}

enum class SkiaBuildType(
    val id: String,
    val flags: Array<String>,
    val clangFlags: Array<String>,
    val msvcFlags: Array<String>
) {
    DEBUG("Debug", arrayOf("-DSK_DEBUG"), arrayOf("-std=c++14", "-g"), emptyArray()),
    RELEASE("Release", arrayOf("-DNDEBUG"), arrayOf("-std=c++14", "-O3"), arrayOf("/O2"))
    ;
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

fun findTargetOs() = when (System.getProperty("skiko.target.os.name")) {
        "linux" -> OS.Linux
        "macos" -> OS.MacOS
        "windows" -> OS.Windows
        "wasm" -> OS.Wasm
        else -> null
    }

fun findTargetArch() = when (System.getProperty("skiko.target.os.arch")) {
    "x64" -> Arch.X64
    "arm64" -> Arch.Arm64
    "wasm" -> Arch.Wasm
    else -> null
}

val targetOs = findTargetOs() ?: hostOs
val targetArch = findTargetArch() ?: hostArch

val target = targetId(targetOs, targetArch)

fun targetId(os: OS, arch: Arch) =
    "${os.id}-${arch.id}"

val jdkHome = System.getProperty("java.home") ?: error("'java.home' is null")

class SkikoProperties(private val myProject: Project) {
    val isCIBuild: Boolean
        get() = myProject.hasProperty("teamcity")

    val deployVersion: String
        get() {
            val version = myProject.property("deploy.version") as String
            return if (isRelease) version else "$version-SNAPSHOT"
        }

    val isRelease: Boolean
        get() = myProject.findProperty("deploy.release") == "true"

    val buildType: SkiaBuildType
        get() = if (myProject.findProperty("skiko.debug") == "true") SkiaBuildType.DEBUG else SkiaBuildType.RELEASE

    fun skiaReleaseFor(os: OS, arch: Arch): String {
        val target = "${os.id}-${arch.id}"
        val tag = myProject.property("dependencies.skia.$target") as String
        val suffix = if (os == OS.Linux && arch == Arch.X64) "-ubuntu14" else ""
        return "${tag}/Skia-${tag}-${os.id}-${buildType.id}-${arch.id}$suffix"
    }

    val releaseGithubVersion: String
        get() = (myProject.property("release.github.version") as String)

    val releaseGithubCommit: String
        get() = (myProject.property("release.github.commit") as String)

    val visualStudioBuildToolsDir: File?
        get() = System.getenv()["SKIKO_VSBT_PATH"]?.let { File(it) }?.takeIf { it.isDirectory }

    val skiaDir: File?
        get() = (System.getenv()["SKIA_DIR"] ?: System.getProperty("skia.dir"))?.let { File(it) }
            ?.takeIf { it.isDirectory }

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
    // names are also used in samples, e.g. samples/SkijaInjectSample/build.gradle
    val commonArtifactId = "skiko-jvm"
    val jsArtifactId = "skiko-js-runtime"
    val jsWasmArtifactId = "skiko-js-wasm-runtime"
    fun runtimeArtifactIdFor(os: OS, arch: Arch) =
        "skiko-jvm-runtime-${targetId(os, arch)}"
    fun nativeRuntimeArtifactIdFor(os: OS, arch: Arch) =
        "skiko-native-runtime-${targetId(os, arch)}"
}