package org.jetbrains.skiko.build.threadstate

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.skiko.build.cpp.LinkSkikoTask
import org.jetbrains.skiko.build.utils.OS
import org.jetbrains.skiko.build.utils.SkikoArtifacts
import org.jetbrains.skiko.build.utils.SkikoGradleProperties
import org.jetbrains.skiko.build.utils.THREAD_STATE_BINDINGS

fun Project.configureThreadStateCheckerTaskGraph() {
    gradle.taskGraph.whenReady {
        val forbidden = allTasks.filter {
            it.name.startsWith("publish", ignoreCase = true) ||
                (it is KotlinNativeLink && it.target.startsWith("macos_") && it.binary !is TestExecutable) ||
                (it is LinkSkikoTask && it.buildTargetOS.get() == OS.MacOS)
        }
        if (forbidden.isNotEmpty()) {
            throw GradleException(
                "${SkikoGradleProperties.THREAD_STATE_CHECKER_ENABLED} is for test runs only. " +
                    "Remove the property before running: ${forbidden.joinToString { it.path }}"
            )
        }
    }
}

fun Project.configureThreadStateChecker(target: KotlinNativeTarget, taskNameSuffix: String) {
    pluginManager.apply(ThreadStateBindingsCompilerPluginSupportPlugin::class.java)
    val binary = target.binaries.getTest("DEBUG")
    binary.linkerOpts("-Wl,-U,___cyg_profile_func_enter", "-Wl,-U,___cyg_profile_func_exit")

    val checkerDir = layout.buildDirectory.dir("thread-state-checker").get().asFile
    val targetDir = checkerDir.resolve(target.name)
    val library = targetDir.resolve("libthreadstatechecker.dylib")
    val bindingsFile = targetDir.resolve("bindings.txt")
    val logFile = targetDir.resolve("log.txt")
    val compile = tasks.register("compileThreadStateChecker$taskNameSuffix", Exec::class.java) {
        val source = rootProject.file("tools/thread-state-checker/interposer.c")
        inputs.file(source)
        outputs.file(library)
        commandLine("xcrun", "clang", "-dynamiclib", "-O2", "-arch", "arm64", "-arch", "x86_64",
            source.absolutePath, "-o", library.absolutePath, "-framework", "CoreFoundation", "-lobjc")
        doFirst { targetDir.mkdirs() }
    }
    val bindings = tasks.register("threadStateCheckerBindings$taskNameSuffix", ThreadStateCheckerBindingsTask::class.java) {
        dependsOn(binary.linkTaskProvider)
        executableFile.set(binary.outputFile)
        bindingsFiles.from(nativeBindingsFiles(target))
        if (this@configureThreadStateChecker != rootProject) {
            val coreTarget = rootProject.extensions.findByType(KotlinMultiplatformExtension::class.java)
                ?.targets?.findByName(target.name) as? KotlinNativeTarget
            if (coreTarget != null) {
                val main = coreTarget.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)
                bindingsFiles.from(rootProject.files(main.threadStateBindingsFile()).builtBy(main.compileTaskProvider))
            }
        }
        outputFile.set(bindingsFile)
    }
    val report = tasks.register("threadStateCheckerReport$taskNameSuffix", ThreadStateCheckerReportTask::class.java) {
        targetName.set(target.name)
        executableFile.set(binary.outputFile)
        this.bindingsFile.set(bindingsFile)
        this.logFile.set(logFile)
        outputFile.set(layout.buildDirectory.file(
            "test-results/${target.name}ThreadStateChecker/TEST-org.jetbrains.skiko.ThreadStateChecker.xml"
        ))
        mustRunAfter("${target.name}Test")
    }
    tasks.named("${target.name}Test", KotlinNativeTest::class.java) {
        dependsOn(compile, bindings)
        environment("DYLD_INSERT_LIBRARIES", listOfNotNull(
            library.absolutePath, System.getenv("DYLD_INSERT_LIBRARIES")?.takeIf { it.isNotBlank() }
        ).joinToString(":"))
        environment("SKIKO_THREAD_STATE_CHECKER_BINDINGS", bindingsFile.absolutePath)
        environment("SKIKO_THREAD_STATE_CHECKER_LOG", logFile.absolutePath)
        outputs.upToDateWhen { false }
        doFirst {
            targetDir.mkdirs()
            logFile.delete()
        }
        finalizedBy(report)
    }
}

private fun Project.nativeBindingsFiles(target: KotlinNativeTarget) = target.compilations.map { compilation ->
    files(compilation.threadStateBindingsFile()).builtBy(compilation.compileTaskProvider)
}

class ThreadStateBindingsCompilerPluginSupportPlugin : KotlinCompilerPluginSupportPlugin {
    override fun getCompilerPluginId() = "org.jetbrains.skiko.thread.state.bindings"

    override fun getPluginArtifact() = SubpluginArtifact(SkikoArtifacts.DEFAULT_GROUP_ID, THREAD_STATE_BINDINGS)

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        (kotlinCompilation.target as? KotlinNativeTarget)?.konanTarget?.name in setOf("macos_arm64", "macos_x64")

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val outputFile = kotlinCompilation.threadStateBindingsFile()
        kotlinCompilation.compileTaskProvider.configure {
            outputs.file(outputFile)
        }
        (kotlinCompilation as KotlinNativeCompilation).configurations.pluginConfiguration.resolutionStrategy.dependencySubstitution {
            substitute(module("${SkikoArtifacts.DEFAULT_GROUP_ID}:$THREAD_STATE_BINDINGS"))
                .using(project(":$THREAD_STATE_BINDINGS"))
        }
        val moduleDirectory = project.projectDir.absolutePath
        return project.provider {
            listOf(
                SubpluginOption("thread-state-bindings-path", outputFile.get().asFile.absolutePath),
                SubpluginOption("thread-state-bindings-module-directory", moduleDirectory)
            )
        }
    }
}

private fun KotlinCompilation<*>.threadStateBindingsFile() = target.project.layout.buildDirectory.file(
    "thread-state-checker/${target.name}/$name-bindings.txt"
)
