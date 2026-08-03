@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyTemplate
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import tasks.configuration.robovmIosTargetNames

// jvmMain (the AWT-free JNI implementation of the Skia API) is also consumed by the
// RoboVM targets, so it exists even when the AWT flavor is disabled.
val SkikoProjectContext.jvmMainSourceSet get() = if (project.supportAwt || project.supportAnyRoboVMIos) kotlin.sourceSets.getByName("jvmMain") else null

val SkikoProjectContext.jvmTestSourceSet get() = if (project.supportAwt) kotlin.sourceSets.getByName("jvmTest") else null

val SkikoProjectContext.awtMainSourceSet get() = if (project.supportAwt) kotlin.sourceSets.getByName("awtMain") else null

val SkikoProjectContext.awtTestSourceSet get() = if (project.supportAwt) kotlin.sourceSets.getByName("awtTest") else null

val SkikoProjectContext.androidMainSourceSet get() = if (project.supportAndroid) kotlin.sourceSets.getByName("androidMain") else null

val SkikoProjectContext.webMainSourceSet get() = if (project.supportWeb) kotlin.sourceSets.getByName("webMain") else null

val SkikoProjectContext.webTestSourceSet get() = if (project.supportWeb) kotlin.sourceSets.getByName("webTest") else null

val SkikoProjectContext.wasmJsTest get() = if (project.supportWeb) kotlin.sourceSets.getByName("wasmJsTest") else null

val SkikoProjectContext.robovmMainSourceSet get() = if (project.supportAnyRoboVMIos) kotlin.sourceSets.getByName("robovmMain") else null

val SkikoProjectContext.robovmTestSourceSet get() = if (project.supportAnyRoboVMIos) kotlin.sourceSets.getByName("robovmTest") else null

val skikoSourceSetHierarchyTemplate = KotlinHierarchyTemplate {
    common {
        group("jvm") {
            withAndroidTarget()
            // All Kotlin/JVM targets except the RoboVM as it has has no AWT/JAWT/Canvas, so it must not see the shared jvm sources.
            withCompilations { compilation ->
                compilation.target.platformType == KotlinPlatformType.jvm &&
                        compilation.target.name !in robovmIosTargetNames
            }
        }

        // RoboVM (JVM on iOS): deliberately outside both the "jvm" group (no AWT)
        // and the "native" group (no Kotlin/Native runtime). Rendering follows the
        // native iOS approach (Metal) via RoboVM CocoaTouch classes.
        group("robovm") {
            withCompilations { it.target.name in robovmIosTargetNames }
        }

        group("web") {
            withJs()
            withWasmJs()
        }

        group("native") {
            group("linux") {
                withLinux()
            }

            group("darwin") {
                group("uikit") {
                    group("tvos")
                    group("ios") {
                        withIos()
                    }
                }

                group("macos") {
                    withMacos()
                }

                group("tvos") {
                    withTvos()
                }
            }
        }

        group("nativeJs") {
            group("web")
            group("native")
        }
    }
}
