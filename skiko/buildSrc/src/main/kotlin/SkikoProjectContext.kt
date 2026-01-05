import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

class SkikoProjectContext(
    val project: Project,
    val skiko: SkikoProperties,
    val kotlin: KotlinMultiplatformExtension,
    val windowsSdkPathProvider: () -> WindowsSdkPaths,
    val createChecksumsTask: (OS, Arch, Provider<File>) -> TaskProvider<*>,
    val additionalRuntimeLibraries: List<AdditionalRuntimeLibrary>,
) {

    val buildType = skiko.buildType

    val windowsSdkPaths: WindowsSdkPaths by lazy {
        windowsSdkPathProvider()
    }

    val allJvmRuntimeJars = mutableMapOf<Pair<OS, Arch>, TaskProvider<Jar>>()
}

fun SkikoProjectContext.declareSkiaTasks() {
    val basicConfigs = listOf("android", "ios", "iosSim", "linux", "macos", "tvos", "tvosSim", "wasm", "windows")

    basicConfigs.forEach { config ->
        (if (config == "wasm") listOf("wasm") else listOf("arm64", "x64")).forEach { arch ->
            val taskNameSuffix = joinToTitleCamelCase(config, arch)
            val target = "$config-$arch"

            val skiaReleaseTag = project.skiaVersion(target)

            val skiaBaseUrl = "https://github.com/JetBrains/skia-pack/releases/download/$skiaReleaseTag"

            val artifactId = "Skia-${skiaReleaseTag}-${config}-$buildType-${arch}"

            val downloadSkiaTask = project.tasks.register<Download>("downloadSkia$buildType$taskNameSuffix") {
                group = "Skia Binaries"

                val skiaUrl = "$skiaBaseUrl/$artifactId.zip"
                description = "downloads $skiaUrl"

                onlyIfModified(true)
                src(skiaUrl)
                dest(skiko.dependenciesDir.resolve(
                    "skia/$skiaReleaseTag/Skia-$skiaReleaseTag-$config-$buildType-${arch}.zip")
                )
            }

            project.tasks.register<Copy>("unzipSkia$buildType$taskNameSuffix") {
                group = "Skia Binaries"

                val outputDir = skiko.dependenciesDir.resolve("skia/$skiaReleaseTag/$artifactId")
                description = "unzips to $outputDir"

                dependsOn(downloadSkiaTask)
                from(project.zipTree(downloadSkiaTask.get().dest))

                into(outputDir)
            }
        }
    }
}


/**
 * Do not call inside tasks.register or tasks.call callback
 * (tasks' registration during other task's registration is prohibited)
 */
fun SkikoProjectContext.registerOrGetSkiaDirProvider(
    os: OS, arch: Arch, isUikitSim: Boolean = false
): Provider<File> {
    val taskNameSuffix = joinToTitleCamelCase(buildType.id, os.idWithSuffix(isUikitSim = isUikitSim), arch.id)

    val skiaDir = skiko.skiaDir
    return if (skiaDir != null) {
        project.tasks.registerOrGetTask<DefaultTask>("skiaDir$taskNameSuffix") {
            // dummy task to simplify usage of the resulting provider (see `else` branch)
            // if a file provider is not created from a task provider,
            // then it cannot be used instead of a task in `dependsOn` clauses of other tasks.
            // e.g. the resulting `skiaDir` could not be used in `dependsOn` of CppCompile configuration
            enabled = false
        }.map {
            skiaDir.absoluteFile
        }
    } else {
        project.tasks.withType<Copy>().named("unzipSkia$taskNameSuffix").map { it.destinationDir.absoluteFile }
    }
}

internal val Project.isInIdea: Boolean
    get() {
        return System.getProperty("idea.active")?.toBoolean() == true
    }

val Project.supportAndroid: Boolean
    get() = findProperty("skiko.android.enabled") == "true" // || isInIdea

val Project.supportAwt: Boolean
    get() = findProperty("skiko.awt.enabled") == "true" || isInIdea

val Project.supportAllNative: Boolean
    get() = findProperty("skiko.native.enabled") == "true" || isInIdea

val Project.supportAllNativeIos: Boolean
    get() = supportAllNative || findProperty("skiko.native.ios.enabled") == "true" || isInIdea

val Project.supportNativeIosArm64: Boolean
    get() = supportAllNativeIos || findProperty("skiko.native.ios.arm64.enabled") == "true" || isInIdea

val Project.supportNativeIosSimulatorArm64: Boolean
    get() = supportAllNativeIos || findProperty("skiko.native.ios.simulatorArm64.enabled") == "true" || isInIdea

val Project.supportNativeIosX64: Boolean
    get() = supportAllNativeIos || findProperty("skiko.native.ios.x64.enabled") == "true" || isInIdea

val Project.supportAnyNativeIos: Boolean
    get() = supportAllNativeIos || supportNativeIosArm64 || supportNativeIosSimulatorArm64 || supportNativeIosX64

val Project.supportAllNativeTvos: Boolean
    get() = supportAllNative || findProperty("skiko.native.tvos.enabled") == "true" || isInIdea

val Project.supportNativeTvosArm64: Boolean
    get() = supportAllNativeTvos || findProperty("skiko.native.tvos.arm64.enabled") == "true" || isInIdea

val Project.supportNativeTvosSimulatorArm64: Boolean
    get() = supportAllNativeTvos || findProperty("skiko.native.tvos.simulatorArm64.enabled") == "true" || isInIdea

val Project.supportNativeTvosX64: Boolean
    get() = supportAllNativeTvos || findProperty("skiko.native.tvos.x64.enabled") == "true" || isInIdea

val Project.supportAnyNativeTvos: Boolean
    get() = supportAllNativeTvos || supportNativeTvosArm64 || supportNativeTvosSimulatorArm64 || supportNativeTvosX64

val Project.supportNativeMac: Boolean
    get() = supportAllNative || findProperty("skiko.native.mac.enabled") == "true" || isInIdea

val Project.supportNativeLinux: Boolean
    get() {
        val enabledProp = findProperty("skiko.native.linux.enabled")?.toString()?.lowercase()
        return supportAllNative || isInIdea || when (enabledProp) {
            "true" -> true
            "false" -> false
            else -> hostOs == OS.Linux
        }
    }

val Project.supportAnyNative: Boolean
    get() = supportAllNative || supportAnyNativeIos || supportNativeMac || supportNativeLinux

val Project.supportWeb: Boolean
    get() = findProperty("skiko.wasm.enabled") == "true" || isInIdea

fun Project.skiaVersion(target: String): String {
    val platformSpecificVersion = "dependencies.skia.$target"

    return if (hasProperty(platformSpecificVersion)) {
        property(platformSpecificVersion) as String
    } else {
        property("dependencies.skia") as String
    }
}
