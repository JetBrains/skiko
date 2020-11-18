import org.gradle.api.Project
import java.io.File

enum class OS(val id: String) {
    Linux("linux"),
    Windows("windows"),
    MacOS("macos")
    ;

    val isWindows
        get() = this == Windows
}

enum class Arch(val id: String) {
    X64("x64"),
    Arm64("arm64")
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

val targetOs = hostOs
val targetArch = hostArch

val target = "${targetOs.id}_${targetArch.id}"

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

    val skijaCommitHash: String
        get() = myProject.property("dependencies.skija.git.commit") as String

    val skiaReleaseForCurrentOS: String
        get() = (myProject.property("dependencies.skia.$target") as String)

    val releaseGithubVersion: String
        get() = (myProject.property("release.github.version") as String)

    val releaseGithubCommit: String
        get() = (myProject.property("release.github.commit") as String)

    val visualStudioBuildToolsDir: File?
        get() = System.getenv()["SKIKO_VSBT_PATH"]?.let { File(it) }?.takeIf { it.isDirectory }

    val skijaDir: File?
        get() = System.getenv()["SKIJA_DIR"]?.let { File(it) }?.takeIf { it.isDirectory }

    val skiaDir: File?
        get() = (System.getenv()["SKIA_DIR"] ?: System.getProperty("skia.dir"))?.let { File(it) }
            ?.takeIf { it.isDirectory }

    val composeRepoUrl: String
        get() = System.getenv("COMPOSE_REPO_URL") ?: "https://maven.pkg.jetbrains.space/public/p/compose/dev"

    val composeRepoUserName: String
        get() = System.getenv("COMPOSE_REPO_USERNAME") ?: ""

    val composeRepoKey: String
        get() = System.getenv("COMPOSE_REPO_KEY") ?: ""

    val dependenciesDir: File
        get() = myProject.rootProject.projectDir.resolve("dependencies")
}