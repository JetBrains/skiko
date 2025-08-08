import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.io.File

fun Project.androidClang(targetArch: Arch, version: String = "30"): String {
    val androidArch = when (targetArch) {
        Arch.Arm64 -> "aarch64"
        Arch.X64 -> "x86_64"
        else -> throw GradleException("unsupported $targetArch")
    }
    var clangBinaryName = "$androidArch-linux-android$version-clang++"
    if (hostOs.isWindows) {
        clangBinaryName += ".cmd"
    }
    return "${llvmPath()}/$clangBinaryName"
}

fun Project.androidAr(): String {
    val arBinaryName = if (hostOs.isWindows) "llvm-ar.cmd" else "llvm-ar"
    return "${llvmPath()}/$arBinaryName"
}

private fun Project.llvmPath(): String {
    val hostOsArch = when (hostOs) {
        OS.MacOS -> "darwin-x86_64"
        OS.Linux -> "linux-x86_64"
        OS.Windows -> "windows-x86_64"
        else -> throw GradleException("unsupported $hostOs")
    }
    return "${ndkPath()}/toolchains/llvm/prebuilt/$hostOsArch/bin"
}

private fun Project.ndkPath(): String {
    val ndkHomeEnv = project.providers
        .environmentVariable("ANDROID_NDK_HOME").orElse("").get()
    return ndkHomeEnv.ifEmpty {
        val androidHome = androidHomePath()
        val ndkDir1 = file("$androidHome/ndk")
        val candidates1 = if (ndkDir1.exists()) ndkDir1.list() else emptyArray()
        val ndkVersion =
            arrayOf(
                *(candidates1.map { "ndk/$it" }.sortedDescending()).toTypedArray(),
                "ndk-bundle"
            ).find {
                File(androidHome).resolve(it).exists()
            } ?: throw GradleException("Cannot find NDK, is it installed (Tools/SDK Manager)?")
        "$androidHome/$ndkVersion"
    }
}

private fun Project.androidHomePath(): String {
    val androidHomeFromSdkHome: Provider<String> =
        project.providers.environmentVariable("ANDROID_HOME")

    // ANDROID_SDK_ROOT name is deprecated in favor of ANDROID_HOME
    val deprecatedAndroidHomeFromSdkRoot: Provider<String> =
        project.providers.environmentVariable("ANDROID_SDK_ROOT")

    val androidHomeFromUserHome: Provider<String> =
        project.providers.systemProperty("user.home")
            .map { userHome ->
                listOf("Library/Android/sdk", ".android/sdk", "Android/sdk")
                    .map { "$userHome/$it" }
                    .firstOrNull { File(it).exists() }
                    ?: error("Define Android SDK via ANDROID_SDK_ROOT")
            }
    return androidHomeFromSdkHome
        .orElse(deprecatedAndroidHomeFromSdkRoot)
        .orElse(androidHomeFromUserHome)
        .get()
}
