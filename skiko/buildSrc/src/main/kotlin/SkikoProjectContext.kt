import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

class SkikoProjectContext(
    val project: Project,
    val skiko: SkikoProperties,
    val kotlin: KotlinMultiplatformExtension,
    val windowsSdkPathProvider: () -> WindowsSdkPaths,
    val createChecksumsTask: (OS, Arch, Provider<File>) -> TaskProvider<*>
) {

    val buildType = skiko.buildType

    val windowsSdkPaths: WindowsSdkPaths by lazy {
        windowsSdkPathProvider()
    }

    val allJvmRuntimeJars = mutableMapOf<Pair<OS, Arch>, TaskProvider<Jar>>()
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
        if (!skiaDir.isDirectory) {
            throw(GradleException("\"skiko.skiaDir\" property was explicitely set to ${skiaDir} which is not a directory"))
        }
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
    get() = supportAllNative || findProperty("skiko.native.linux.enabled") == "true" || isInIdea

val Project.supportAnyNative: Boolean
    get() = supportAllNative || supportAnyNativeIos || supportNativeMac || supportNativeLinux

val Project.supportWasm: Boolean
    get() = findProperty("skiko.wasm.enabled") == "true" || isInIdea

val Project.supportJs: Boolean
    get() = findProperty("skiko.js.enabled") == "true" || supportWasm || isInIdea
