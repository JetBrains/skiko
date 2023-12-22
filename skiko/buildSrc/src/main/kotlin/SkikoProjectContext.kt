import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
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
    os: OS, arch: Arch, isIosSim: Boolean = false
): Provider<File> = with(this.project) {
    val taskNameSuffix = joinToTitleCamelCase(buildType.id, os.idWithSuffix(isIosSim = isIosSim), arch.id)
    val skiaRelease = skiko.skiaReleaseFor(os, arch, buildType, isIosSim)
    val downloadSkia = tasks.registerOrGetTask<Download>("downloadSkia$taskNameSuffix") {
        onlyIf { !dest.exists() }
        onlyIfModified(true)
        val skiaUrl = "https://github.com/JetBrains/skia-pack/releases/download/$skiaRelease.zip"
        inputs.property("skia.url", skiaUrl)
        src(skiaUrl)
        dest(skiko.dependenciesDir.resolve("skia/$skiaRelease.zip"))
    }.map { it.dest.absoluteFile }

    return if (skiko.skiaDir != null) {
        tasks.registerOrGetTask<DefaultTask>("skiaDir$taskNameSuffix") {
            // dummy task to simplify usage of the resulting provider (see `else` branch)
            // if a file provider is not created from a task provider,
            // then it cannot be used instead of a task in `dependsOn` clauses of other tasks.
            // e.g. the resulting `skiaDir` could not be used in `dependsOn` of CppCompile configuration
            enabled = false
        }.map { skiko.skiaDir!!.absoluteFile }
    } else {
        tasks.registerOrGetTask<Copy>("unzipSkia$taskNameSuffix") {
            dependsOn(downloadSkia)
            from(downloadSkia.map { zipTree(it) })
            into(skiko.dependenciesDir.resolve("skia/$skiaRelease"))
        }.map { it.destinationDir.absoluteFile }
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