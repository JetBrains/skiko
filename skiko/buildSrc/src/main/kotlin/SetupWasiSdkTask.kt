import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File

abstract class SetupWasiSdkTask : SetupSdkTask() {
    override val sdkName: String = "WASI SDK"

    init {
        sdkDir.convention(
            project.layout.dir(
                sdkVersion.map { version ->
                    project.rootProject.gradle.gradleUserHomeDir.resolve("wasi-sdk/${wasiSdkDirName(version)}")
                }
            )
        )
    }

    override fun installedMarker(): File =
        sdkDir.get().asFile.resolve("bin/${wasiSdkExecutableName("clang++")}")

    override fun installSdk() {
        val version = sdkVersion.get()
        val sdkDir = sdkDir.get().asFile
        downloadAndExtractSdk(
            archiveUrl = "https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-$version/${wasiSdkDirName(version)}.tar.gz",
            archiveType = SdkArchiveType.TAR_GZ,
            sdkDir = sdkDir,
        )
    }
}

fun wasiSdkExecutableName(name: String): String =
    if (hostOs.isWindows) "$name.exe" else name

fun wasiSdkDirName(version: String): String =
    "wasi-sdk-${wasiSdkFullVersion(version)}-${wasiSdkHostArch()}-${wasiSdkHostOs()}"

fun Project.resolveSdkDir(path: String): File =
    File(path).let {
        if (it.isAbsolute) it else rootProject.file(path)
    }

private fun wasiSdkFullVersion(version: String): String =
    if (version.contains(".")) version else "$version.0"

private fun wasiSdkHostArch(): String =
    when (hostArch) {
        Arch.X64 -> "x86_64"
        Arch.Arm64 -> "arm64"
        Arch.Wasm -> throw GradleException("WASI SDK setup is not supported on wasm hosts")
    }

private fun wasiSdkHostOs(): String =
    when (hostOs) {
        OS.Linux -> "linux"
        OS.MacOS -> "macos"
        OS.Windows -> "windows"
        OS.Android, OS.IOS, OS.TVOS, OS.Wasm ->
            throw GradleException("WASI SDK setup is not supported on ${hostOs.id} hosts")
    }
