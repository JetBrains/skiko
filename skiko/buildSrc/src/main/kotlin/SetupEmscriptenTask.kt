import java.io.File

/**
 * Verifies that emcc (Emscripten compiler) is available on the system.
 * If emcc is not found from previous installations, the task automatically installs the Emscripten SDK
 * into gradles user home directory using the specified version.
 *
 * The installed emsdk location is exposed via [sdkDir] so that downstream
 * tasks can use the emscripten binaries if needed.
 */
abstract class SetupEmscriptenTask : SetupSdkTask() {
    override val sdkName: String = "Emscripten SDK"

    init {
        sdkDir.convention(
            project.layout.dir(
                sdkVersion.map { version ->
                    project.rootProject.gradle.gradleUserHomeDir.resolve("emsdk/emsdk-$version")
                }
            )
        )
    }

    override fun installedMarker(): File =
        sdkDir.get().asFile.resolve("upstream/emscripten/${if (hostOs.isWindows) "emcc.bat" else "emcc"}")

    override fun installSdk() {
        val version = sdkVersion.get()
        val sdkDir = sdkDir.get().asFile

        downloadAndExtractSdk(
            archiveUrl = "https://github.com/emscripten-core/emsdk/archive/refs/tags/$version.zip",
            archiveType = SdkArchiveType.ZIP,
            sdkDir = sdkDir,
        )

        val emsdkScript = sdkDir.resolve(if (hostOs.isWindows) "emsdk.bat" else "emsdk").absolutePath

        logger.lifecycle("Installing emsdk version $version...")
        exec(emsdkScript, "install", version, workingDir = sdkDir)

        logger.lifecycle("Activating emsdk version $version...")
        exec(emsdkScript, "activate", version, workingDir = sdkDir)
    }
}
