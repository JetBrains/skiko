package tasks.configuration

import Arch
import OS
import SkiaBuildType
import SkikoProperties
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import registerSkikoTask
import skiaVersion
import supportAndroid
import supportNativeIosArm64
import supportNativeIosSimulatorArm64
import supportNativeIosX64
import supportNativeLinux
import supportNativeMac
import supportNativeTvosArm64
import supportNativeTvosSimulatorArm64
import supportNativeTvosX64
import supportWeb
import toTitleCase
import java.io.File

private fun Project.appleToolchainOutputOrNull(vararg args: String): String? =
    runCatching {
        providers.exec {
            commandLine("xcrun", *args)
        }.standardOutput.asText.get().trim()
    }.getOrNull()?.ifBlank { null }

fun Project.appleToolchainExecutableOrDefault(tool: String, fallback: String): String =
    appleToolchainOutputOrNull("--find", tool) ?: fallback

fun Project.appleMacOsSdkFlags(): List<String> =
    appleToolchainOutputOrNull("--sdk", "macosx", "--show-sdk-path")
        ?.let { listOf("-isysroot", it) }
        ?: emptyList()

fun skiaHeadersDirs(skiaDir: File): List<File> =
    listOf(
        skiaDir,
        skiaDir.resolve("include"),
        skiaDir.resolve("include/core"),
        skiaDir.resolve("include/gpu"),
        skiaDir.resolve("include/effects"),
        skiaDir.resolve("include/pathops"),
        skiaDir.resolve("include/utils"),
        skiaDir.resolve("include/codec"),
        skiaDir.resolve("include/svg"),
        skiaDir.resolve("modules/jsonreader"),
        skiaDir.resolve("modules/skottie/include"),
        skiaDir.resolve("modules/skparagraph/include"),
        skiaDir.resolve("modules/skshaper/include"),
        skiaDir.resolve("modules/skunicode/include"),
        skiaDir.resolve("modules/sksg/include"),
        skiaDir.resolve("modules/svg/include"),
        skiaDir.resolve("third_party/externals/harfbuzz/src"),
        skiaDir.resolve("third_party/icu"),
        skiaDir.resolve("third_party/externals/icu/source/common"),
    )

fun includeHeadersFlags(headersDirs: List<File>) =
    headersDirs.map { "-I${it.absolutePath}" }.toTypedArray()

fun skiaPreprocessorFlags(os: OS, buildType: SkiaBuildType): Array<String> {
    val base = listOf(
        "-DSK_ALLOW_STATIC_GLOBAL_INITIALIZERS=1",
        "-DSK_FORCE_DISTANCE_FIELD_TEXT=0",
        "-DSK_GAMMA_APPLY_TO_A8",
        "-DSK_GAMMA_SRGB",
        "-DSK_SCALAR_TO_FLOAT_EXCLUDED",
        "-DSK_SUPPORT_GPU=1",
        "-DSK_GANESH",
        "-DSK_GL",
        "-DSK_SHAPER_HARFBUZZ_AVAILABLE",
        "-DSK_UNICODE_AVAILABLE",
        "-DSK_SHAPER_UNICODE_AVAILABLE",
        "-DSK_SUPPORT_OPENCL=0",
        "-DSK_UNICODE_AVAILABLE",
        "-DSK_USING_THIRD_PARTY_ICU",
        // For ICU symbols renaming:
        "-DU_DISABLE_RENAMING=0",
        "-DU_DISABLE_VERSION_SUFFIX=1",
        "-DU_HAVE_LIB_SUFFIX=1",
        "-DU_LIB_SUFFIX_C_NAME=_skiko",
        *buildType.flags
    )

    val perOs = when (os) {
        OS.MacOS -> listOf(
            "-DSK_SHAPER_CORETEXT_AVAILABLE",
            "-DSK_BUILD_FOR_MAC",
            "-DSK_METAL"
        )
        OS.IOS -> listOf(
            "-DSK_BUILD_FOR_IOS",
            "-DSK_SHAPER_CORETEXT_AVAILABLE",
            "-DSK_METAL"
        )
        OS.TVOS -> listOf(
            "-DSK_BUILD_FOR_IOS",
            "-DSK_BUILD_FOR_TVOS",
            "-DSK_SHAPER_CORETEXT_AVAILABLE",
            "-DSK_METAL"
        )
        OS.Windows -> listOf(
            "-DSK_BUILD_FOR_WIN",
            "-D_CRT_SECURE_NO_WARNINGS",
            "-D_HAS_EXCEPTIONS=0",
            "-DWIN32_LEAN_AND_MEAN",
            "-DNOMINMAX",
            "-DSK_GAMMA_APPLY_TO_A8",
            "-DSK_DIRECT3D",
            "-DSK_ANGLE"
        )
        OS.Linux -> listOf(
            "-DSK_BUILD_FOR_LINUX",
            "-D_GLIBCXX_USE_CXX11_ABI=0"
        )
        OS.Wasm -> listOf(
            "-DSKIKO_WASM",
            "-sSUPPORT_LONGJMP=wasm"
        )
        OS.Android -> listOf(
            "-DSK_BUILD_FOR_ANDROID"
        )
    }

    return (base + perOs).toTypedArray()
}

fun Project.configureSignAndPublishDependencies() {
    val hasAwtTarget = extensions.getByType<KotlinMultiplatformExtension>().targets.findByName("awt") != null

    if (supportWeb) {
        tasks.configureEach {
            val publishJs = "publishJsPublicationTo"
            val publishWasmPub = "publishWasmJsPublicationTo"
            val signJs = "signJsPublication"
            val signWasmPub = "signWasmJsPublication"

            when {
                name.startsWith(publishJs) -> dependsOn(signWasmPub)
                name.startsWith(publishWasmPub) -> dependsOn(signJs)
            }
        }
    }
    if (supportAndroid) {
        tasks.configureEach {
            val signAndroid = "signAndroidPublication"
            val generateMetadata = "generateMetadataFileForAndroidPublication"
            val publishAndroid = "publishAndroidPublicationTo"
            val publishX64 = "publishSkikoJvmRuntimeAndroidX64PublicationTo"
            val publishArm64 = "publishSkikoJvmRuntimeAndroidArm64PublicationTo"
            val signX64 = "signSkikoJvmRuntimeAndroidX64Publication"
            val signArm64 = "signSkikoJvmRuntimeAndroidArm64Publication"
            val skikoAndroidArtifact = "skikoAndroidArtifact"

            when {
                name.startsWith(signAndroid) || name.startsWith(generateMetadata) -> {
                    dependsOn(skikoAndroidArtifact)
                }
                name.startsWith(publishAndroid) -> {
                    dependsOn(signX64, signArm64)
                }
                name.startsWith(publishX64) -> {
                    dependsOn(signAndroid, signArm64)
                }
                name.startsWith(publishArm64) -> {
                    dependsOn(signX64, signAndroid)
                }
            }
        }
    }

    if (supportNativeLinux) {
        val publishLinuxX64 = "publishLinuxX64PublicationTo"
        val publishLinuxArm64 = "publishLinuxArm64PublicationTo"
        val signLinuxArm64Publication = "signLinuxArm64Publication"
        val signLinuxX64Publication = "signLinuxX64Publication"

        tasks.configureEach {
            when {
                name.startsWith(publishLinuxX64) -> {
                    dependsOn(signLinuxArm64Publication)
                    dependsOn(signLinuxX64Publication)
                }

                name.startsWith(publishLinuxArm64) -> {
                    dependsOn(signLinuxArm64Publication)
                    dependsOn(signLinuxX64Publication)
                }
            }
        }
    }

    if (supportNativeMac) {
        val publishMacosArm64 = "publishMacosArm64PublicationTo"
        val publishMacosX64 = "publishMacosX64PublicationTo"
        val signMacosArm64 = "signMacosArm64Publication"
        val signMacosX64 = "signMacosX64Publication"

        tasks.configureEach {
            when {
                name.startsWith(publishMacosArm64) -> {
                    dependsOn(signMacosArm64)
                    dependsOn(signMacosX64)
                }
                name.startsWith(publishMacosX64) -> {
                    dependsOn(signMacosArm64)
                    dependsOn(signMacosX64)
                }
            }
        }
    }

    // iOS family
    if (supportNativeIosArm64 || supportNativeIosSimulatorArm64 || supportNativeIosX64) {
        val publishIosArm64 = "publishIosArm64PublicationTo"
        val publishIosSimArm64 = "publishIosSimulatorArm64PublicationTo"
        val publishIosX64 = "publishIosX64PublicationTo"
        val signIosArm64 = "signIosArm64Publication"
        val signIosSimArm64 = "signIosSimulatorArm64Publication"
        val signIosX64 = "signIosX64Publication"

        tasks.configureEach {
            when {
                name.startsWith(publishIosArm64) -> {
                    if (supportNativeIosArm64) dependsOn(signIosArm64)
                    if (supportNativeIosSimulatorArm64) dependsOn(signIosSimArm64)
                    if (supportNativeIosX64) dependsOn(signIosX64)
                }
                name.startsWith(publishIosSimArm64) -> {
                    if (supportNativeIosArm64) dependsOn(signIosArm64)
                    if (supportNativeIosSimulatorArm64) dependsOn(signIosSimArm64)
                    if (supportNativeIosX64) dependsOn(signIosX64)
                }
                name.startsWith(publishIosX64) -> {
                    if (supportNativeIosArm64) dependsOn(signIosArm64)
                    if (supportNativeIosSimulatorArm64) dependsOn(signIosSimArm64)
                    if (supportNativeIosX64) dependsOn(signIosX64)
                }
            }
        }
    }

    // tvOS family
    if (supportNativeTvosArm64 || supportNativeTvosSimulatorArm64 || supportNativeTvosX64) {
        val publishTvosArm64 = "publishTvosArm64PublicationTo"
        val publishTvosSimArm64 = "publishTvosSimulatorArm64PublicationTo"
        val publishTvosX64 = "publishTvosX64PublicationTo"
        val signTvosArm64 = "signTvosArm64Publication"
        val signTvosSimArm64 = "signTvosSimulatorArm64Publication"
        val signTvosX64 = "signTvosX64Publication"

        tasks.configureEach {
            when {
                name.startsWith(publishTvosArm64) -> {
                    if (supportNativeTvosArm64) dependsOn(signTvosArm64)
                    if (supportNativeTvosSimulatorArm64) dependsOn(signTvosSimArm64)
                    if (supportNativeTvosX64) dependsOn(signTvosX64)
                }
                name.startsWith(publishTvosSimArm64) -> {
                    if (supportNativeTvosArm64) dependsOn(signTvosArm64)
                    if (supportNativeTvosSimulatorArm64) dependsOn(signTvosSimArm64)
                    if (supportNativeTvosX64) dependsOn(signTvosX64)
                }
                name.startsWith(publishTvosX64) -> {
                    if (supportNativeTvosArm64) dependsOn(signTvosArm64)
                    if (supportNativeTvosSimulatorArm64) dependsOn(signTvosSimArm64)
                    if (supportNativeTvosX64) dependsOn(signTvosX64)
                }
            }
        }
    }

    if (hasAwtTarget) {
        val publishJvmRuntimeAngleX64 = "publishSkikoJvmRuntimeAngleWindowsX64PublicationToComposeRepoRepository"
        val publishJvmRuntimeAngleArm64 = "publishSkikoJvmRuntimeAngleWindowsArm64PublicationToComposeRepoRepository"
        val signJvmRuntimeX64 = "signSkikoJvmRuntimeWindowsX64Publication"
        val signJvmRuntimeArm64 = "signSkikoJvmRuntimeWindowsArm64Publication"

        tasks.configureEach {
            when {
                name.startsWith(publishJvmRuntimeAngleX64) -> {
                    dependsOn(signJvmRuntimeX64)
                }
                name.startsWith(publishJvmRuntimeAngleArm64) -> {
                    dependsOn(signJvmRuntimeArm64)
                }
            }
        }
    }

    // Cross-publication pairs due to shared javadoc: KotlinMultiplatform <-> AWT
    tasks.configureEach {
        val publishKmp = "publishKotlinMultiplatformPublicationTo"
        val publishAwt = "publishAwtPublicationTo"
        val publishAwtRuntimeElements = "publishAwtRuntimeElementsPublicationTo"
        val signKmp = "signKotlinMultiplatformPublication"
        val signAwt = "signAwtPublication"
        val signAwtRuntimeElements = "signAwtRuntimeElementsPublication"

        when {
            name.startsWith(publishKmp) -> {
                if (hasAwtTarget) {
                    dependsOn(signAwt)
                    dependsOn(signAwtRuntimeElements)
                }
            }
            name.startsWith(publishAwt) -> {
                dependsOn(signKmp)
                dependsOn(signAwtRuntimeElements)
            }
            name.startsWith(publishAwtRuntimeElements) -> {
                dependsOn(signAwt)
                dependsOn(signKmp)
            }
        }
    }
}

fun KotlinTarget.generateVersion(
    targetOs: OS,
    targetArch: Arch,
    skikoProperties: SkikoProperties,
    compilationName: String = "main"
) {
    val targetName = this.name
    val isUikitSim = isUikitSimulator()
    val generatedDir = project.layout.buildDirectory.dir("generated/$targetName")

    // Read at configuration time: the task action must not touch Project (configuration cache)
    val skikoTag = skikoProperties.deployVersion
    val skiaTag = project.skiaVersion("${targetOs.id}-${targetArch.id}")

    val generateVersionTask = project.registerSkikoTask<DefaultTask>(
        "generateVersion${toTitleCase(platformType.name)}".withSuffix(isUikitSim = isUikitSim),
        targetOs,
        targetArch
    ) {
        inputs.property("skikoVersion", skikoTag)
        inputs.property("skiaVersion", skiaTag)
        outputs.dir(generatedDir)
        doFirst {
            val generatedDirFile = generatedDir.get().asFile
            generatedDirFile.deleteRecursively()

            val outDir = File(generatedDirFile, "org/jetbrains/skiko")
            outDir.mkdirs()
            File("$outDir/Version.kt").writeText(
                """
                package org.jetbrains.skiko
                object Version {
                    val skiko = "$skikoTag"
                    val skia = "$skiaTag"
                }
                """.trimIndent()
            )
        }
    }

    // Needs to be lazily loaded as android compilations are not available right away
    compilations.matching { it.name == compilationName }.configureEach {
        defaultSourceSet.kotlin.srcDir(generateVersionTask)
    }

    // Make IDEA run the generation during Gradle sync, so that the file resolves without building first
    project.tasks.matching { it.name == "prepareKotlinIdeaImport" }.configureEach {
        dependsOn(generateVersionTask)
    }
}
