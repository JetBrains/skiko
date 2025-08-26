import SkikoArtifacts.jvmAdditionalRuntimeArtifactIdFor
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.crypto.checksum.Checksum
import org.gradle.kotlin.dsl.register
import org.gradle.api.publish.maven.MavenPublication

interface AdditionalRuntimeLibrary {
    val jarTask: TaskProvider<Jar>

    fun registerMavenPublication(
        container: PublicationContainer,
        emptySourcesJar: TaskProvider<Jar>,
        pomNameForPublication: MutableMap<String, String>
    )

    fun registerRuntimePublishTaskDependency(repos: List<String>)
}

fun Project.registerAdditionalRuntimeLibrary(
    targetOs: OS,
    targetArch: Arch,
    skikoProperties: SkikoProperties,
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
        archiveBaseName.set("skiko-$name")
        archiveClassifier.set(targetId)
        from(unzipTask)
        from(checksumTask)
    }

    return object : AdditionalRuntimeLibrary {
        override val jarTask = jarTask
        
        override fun registerMavenPublication(
            container: PublicationContainer,
            emptySourcesJar: TaskProvider<Jar>,
            pomNameForPublication: MutableMap<String, String>
        ) {
            container.create("skikoJvmRuntime$taskSuffix", MavenPublication::class.java) {
                pomNameForPublication[this.name] = "Skiko $visibleName for ${targetOs.id} ${targetArch.id}"
                artifactId = jvmAdditionalRuntimeArtifactIdFor(name, targetOs, targetArch)
                afterEvaluate {
                    artifact(jarTask.map { it.archiveFile.get() })
                    artifact(emptySourcesJar)
                }
            }
        }

        override fun registerRuntimePublishTaskDependency(repos: List<String>) {
            repos.forEach { repo ->
                val mainTaskSuffix = "${toTitleCase(targetOs.id)}${toTitleCase(targetArch.id)}"
                project.tasks.findByName("publishSkikoJvmRuntime${mainTaskSuffix}PublicationTo${repo}")
                    ?.dependsOn("publishSkikoJvmRuntime${taskSuffix}PublicationTo${repo}")
            }
        }
    }
}
