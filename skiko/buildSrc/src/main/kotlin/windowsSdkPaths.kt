import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.*
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal
import org.gradle.nativeplatform.toolchain.internal.EmptySystemLibraries
import org.gradle.nativeplatform.toolchain.internal.SystemLibraries
import org.gradle.nativeplatform.toolchain.internal.msvcpp.*
import org.gradle.util.internal.VersionNumber
import java.io.File
import kotlin.math.abs

data class WindowsSdkPaths(
    val compiler: File,
    val linker: File,
    val includeDirs: Collection<File>,
    val libDirs: Collection<File>,
    val toolchainVersion: VersionNumber,
)

private const val ENV_SKIKO_VSBT_PATH = "SKIKO_VSBT_PATH"
private const val ENV_SKIKO_VSBT_VERSION = "SKIKO_VSBT_VERSION"
private const val ENV_SKIKO_WINDOWS_SDK_VERSION = "SKIKO_WINDOWS_SDK_VERSION"

fun findWindowsSdkPaths(gradle: Gradle, arch: Arch): WindowsSdkPaths {
    check(hostOs.isWindows) { "Unexpected host os: $hostOs, expected: ${OS.Windows}" }

    val hostPlatform = host()
    val finder = GradleWindowsComponentFinderWrapper(gradle, hostPlatform, arch)
    val visualCpp = finder.findVisualCpp()
    val windowsSdk = finder.findWindowsSdk()
    val ucrt = finder.findUcrt()
    val winrt = finder.findWinrt()
    val systemLibraries = listOf(visualCpp, windowsSdk, ucrt, winrt)
    return WindowsSdkPaths(
        compiler = visualCpp.compilerExecutable.fixPathFor(arch),
        linker = visualCpp.linkerExecutable.fixPathFor(arch),
        includeDirs = systemLibraries.flatMap { it.includeDirs }.map { it.fixPathFor(arch) },
        libDirs = systemLibraries.flatMap { it.libDirs }.map { it.fixPathFor(arch) },
        toolchainVersion = visualCpp.implementationVersion,
    )
}

// workaround until https://github.com/gradle/gradle/pull/21780 is merged
private fun File.fixPathFor(arch: Arch) = File(absolutePath.replace("x64", arch.id))

private class GradleWindowsComponentFinderWrapper(
    private val gradle: Gradle,
    private val hostPlatform: NativePlatformInternal,
    private val arch: Arch,
) {
    fun findVisualCpp(): VisualCpp {
        val skikoVsbtPath = System.getenv(ENV_SKIKO_VSBT_PATH)

        val vsLocator = gradle.serviceOf<VisualStudioLocator>()
        val vsComponent = if (skikoVsbtPath != null) {
            val vsbtDir = File(skikoVsbtPath)
            check(vsbtDir.isDirectory) {
                "Environment variable '$ENV_SKIKO_VSBT_PATH' points to non-existing directory: '$skikoVsbtPath'\n" +
                        "Please set it to existing Visual Studio Build Tools installation"
            }
            val searchResult = vsLocator.locateComponent(vsbtDir)
            if (!searchResult.isAvailable)
                error("Could not find valid Visual Studio Build Tools installation " +
                        "at the location specified by '$ENV_SKIKO_VSBT_PATH': $skikoVsbtPath"
                )
            else searchResult.component
        } else {
            vsLocator.locateAllComponents().chooseComponentByPreferredVersion(
                componentType = "VS Build Tools",
                preferredVersionEnvVar = ENV_SKIKO_VSBT_VERSION
            )
        }

        return vsComponent.visualCpp.forPlatform(hostPlatform)
            ?: error("Visual Studio location component for host platform '$hostPlatform' is null")
    }

    fun findWindowsSdk(): SystemLibraries {
        val windowsSdkLocator = gradle.serviceOf<WindowsSdkLocator>()
        val windowsSdkComponent = windowsSdkLocator.locateAllComponents()
            .chooseComponentByPreferredVersion(
                componentType = "Windows SDK",
                preferredVersionEnvVar = ENV_SKIKO_WINDOWS_SDK_VERSION
            )
        return windowsSdkComponent.forPlatform(hostPlatform)
            ?: error("Windows SDK component for host platform '$hostPlatform' is null")
    }

    fun findUcrt(): SystemLibraries {
        val ucrtLocator = gradle.serviceOf<UcrtLocator>()
        val ucrtComponent = ucrtLocator.locateAllComponents()
            .chooseComponentByPreferredVersion(
                componentType = "UCRT",
                preferredVersionEnvVar = ENV_SKIKO_WINDOWS_SDK_VERSION
            )
        return ucrtComponent.getCRuntime(hostPlatform)
            ?: error("UCRT component for host platform '$hostPlatform' is null")
    }

    fun findWinrt(): SystemLibraries {
        // Gradle doesn't have a Locator for WinRT, so we take the UCRT one and fix the path
        return object : EmptySystemLibraries() {
            override fun getIncludeDirs(): List<File> {
                return findUcrt().includeDirs.map { File(it.path.replace("ucrt", "winrt")) }
            }
        }
    }

    private fun <T : Any> List<T>.chooseComponentByPreferredVersion(
        componentType: String,
        preferredVersionEnvVar: String,
    ): T = chooseComponentByPreferredVersion(componentType, preferredVersionEnvVar, this)

    private fun <T : Any> chooseComponentByPreferredVersion(
        componentType: String,
        preferredVersionEnvVar: String,
        components: List<T>
    ): T {
        return when (components.size) {
            0 -> error("Could not find any $componentType locations")
            1 -> components.single()
            else -> {
                val versions = components.associateBy { component ->
                    when (component) {
                        is WindowsKitInstall -> component.version
                        is WindowsSdkInstall -> component.version
                        is VisualStudioInstall -> component.version
                        else -> error("Unknown class of $componentType: ${component.javaClass.canonicalName}")
                    }
                }

                val preferredVersion = System.getenv(preferredVersionEnvVar)
                if (preferredVersion != null) {
                    for ((version, component) in versions.entries) {
                        if (preferredVersion == version.toString()) return component
                    }
                }

                val latestVersion = versions.keys.maxOf { it }
                val warningMessage = buildString {
                    appendLine("w: Multiple $componentType versions are found: ${versions.keys.joinToString(", ") { "'$it'"}}")
                    appendLine("Using the latest version '$latestVersion'")
                    appendLine("Use '$preferredVersionEnvVar' environment variable to specify the preferred version")
                }
                gradle.rootProject.logger.warn(warningMessage)
                versions[latestVersion]!!
            }
        }
    }
}