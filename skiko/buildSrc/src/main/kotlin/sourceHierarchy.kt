@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyTemplate

val SkikoProjectContext.jvmMainSourceSet get() = if (project.supportAwt) kotlin.sourceSets.findByName("jvmMain") else null

val SkikoProjectContext.jvmTestSourceSet get() = if (project.supportAwt) kotlin.sourceSets.findByName("jvmTest") else null

val SkikoProjectContext.awtMainSourceSet get() = if (project.supportAwt) kotlin.sourceSets.findByName("awtMain") else null

val SkikoProjectContext.awtTestSourceSet get() = if (project.supportAwt) kotlin.sourceSets.findByName("awtTest") else null

val SkikoProjectContext.androidMainSourceSet get() = if (project.supportAndroid) kotlin.sourceSets.findByName("androidMain") else null

val SkikoProjectContext.webMainSourceSet get() = if (project.supportWeb || project.supportNativeWasm) kotlin.sourceSets.findByName("webMain") else null

val SkikoProjectContext.webTestSourceSet get() = if (project.supportWeb || project.supportNativeWasm) kotlin.sourceSets.findByName("webTest") else null

val SkikoProjectContext.wasmJsTestSourceSet get() = if (project.supportWeb || project.supportNativeWasm) kotlin.sourceSets.findByName("wasmJsTest") else null

val skikoSourceSetHierarchyTemplate = KotlinHierarchyTemplate {
    common {
        group("jvm") {
            withAndroidTarget()
            withJvm()
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
            withWasmWasi()
        }
    }
}
