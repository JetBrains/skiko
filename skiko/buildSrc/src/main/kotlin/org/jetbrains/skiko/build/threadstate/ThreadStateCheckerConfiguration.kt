package org.jetbrains.skiko.build.threadstate

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.skiko.build.cpp.LinkSkikoTask
import org.jetbrains.skiko.build.utils.OS
import org.jetbrains.skiko.build.utils.SkikoGradleProperties

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
        sourceFiles.from(nativeSourceFiles(target))
        if (this@configureThreadStateChecker != rootProject) {
            val coreTarget = rootProject.extensions.findByType(KotlinMultiplatformExtension::class.java)
                ?.targets?.findByName(target.name) as? KotlinNativeTarget
            if (coreTarget != null) {
                sourceFiles.from(rootProject.nativeSourceFiles(coreTarget))
            }
        }
        moduleDirectory.set(projectDir)
        rootModuleDirectory.set(rootProject.projectDir)
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

private fun Project.nativeSourceFiles(target: KotlinNativeTarget) = target.compilations.flatMap { compilation ->
    compilation.allKotlinSourceSets.flatMap { it.kotlin.srcDirs }
}.distinct().map { dir -> fileTree(dir) { include("**/*.kt") } }
