package org.jetbrains.skiko.build.dependencies

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.crypto.checksum.Checksum
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.register
import org.jetbrains.skiko.build.utils.Arch
import org.jetbrains.skiko.build.utils.OS
import org.jetbrains.skiko.build.utils.SkikoArtifacts
import org.jetbrains.skiko.build.utils.SkikoProperties
import org.jetbrains.skiko.build.utils.targetId
import org.jetbrains.skiko.build.utils.toTitleCase

interface AdditionalRuntimeLibrary {
    val jarTask: TaskProvider<Jar>
}

val additionalRuntimeNameAttribute: Attribute<String> = Attribute.of("org.jetbrains.skiko.additionalRuntime.name", String::class.java)
val additionalRuntimeOsAttribute: Attribute<String> = Attribute.of("org.jetbrains.skiko.additionalRuntime.os", String::class.java)
val additionalRuntimeArchAttribute: Attribute<String> = Attribute.of("org.jetbrains.skiko.additionalRuntime.arch", String::class.java)
const val additionalRuntimeConfigurationPrefix = "jvmAdditionalRuntimeJarElements"

fun Project.registerAdditionalRuntimeLibrary(
    targetOs: OS,
    targetArch: Arch,
    skikoProperties: SkikoProperties,
    artifacts: SkikoArtifacts,
    name: String,
    archiveUrl: String,
    filesToInclude: List<String>,
): AdditionalRuntimeLibrary {
    val visibleName = "${toTitleCase(name)} Runtime"
    val targetId = targetId(targetOs, targetArch)
    val taskSuffix = "${toTitleCase(name)}${toTitleCase(targetOs.id)}${toTitleCase(targetArch.id)}"

    val archiveFileName = archiveUrl.substringAfterLast('/')
    val archiveDir = skikoProperties.dependenciesDir.resolve(name).resolve(archiveFileName.substringBefore("."))

    val downloadTask = tasks.register<Download>("download$taskSuffix") {
        group = visibleName
        description = "Downloads $archiveUrl"
        onlyIfModified(true)
        src(archiveUrl)
        dest(archiveDir.resolve(archiveFileName))
    }

    val unzipTask = tasks.register<Copy>("unzip$taskSuffix") {
        group = visibleName
        dependsOn(downloadTask)
        from(zipTree(downloadTask.get().dest)) {
            filesToInclude.forEach { include(it) }
            duplicatesStrategy = DuplicatesStrategy.FAIL
            eachFile { path = file.name }
            includeEmptyDirs = false
        }
        into(archiveDir.resolve("extracted"))
    }

    val checksumTask = tasks.register<Checksum>("createChecksums$taskSuffix") {
        group = visibleName
        inputFiles.setFrom(unzipTask)
        checksumAlgorithm.set(Checksum.Algorithm.SHA256)
        outputDirectory.set(layout.buildDirectory.dir("$name/checksums"))
        dependsOn(unzipTask)
    }

    val jarTask = tasks.register<Jar>("skikoRuntimeJar$taskSuffix") {
        group = visibleName
        dependsOn(unzipTask)
        dependsOn(checksumTask)
        archiveBaseName.set("${artifacts.artifactIdPrefix}-$name")
        archiveClassifier.set(targetId)
        from(unzipTask)
        from(checksumTask)
    }

    configurations.create("$additionalRuntimeConfigurationPrefix$taskSuffix") {
        isCanBeConsumed = true
        isCanBeResolved = false
        attributes.attribute(additionalRuntimeNameAttribute, name)
        attributes.attribute(additionalRuntimeOsAttribute, targetOs.name)
        attributes.attribute(additionalRuntimeArchAttribute, targetArch.name)
        outgoing.artifact(jarTask.flatMap { it.archiveFile })
    }

    return object : AdditionalRuntimeLibrary {
        override val jarTask = jarTask
    }
}
