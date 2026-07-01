import org.kohsuke.github.*
import org.jetbrains.compose.internal.publishing.*
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import java.io.File

plugins {
    `maven-publish`
}

val skiko = SkikoProperties(project)
val mavenCentral = MavenCentralProperties(project)
val GITHUB_REPO = "JetBrains/skiko"
val skikoArtifacts = SkikoArtifacts()

val skikoArtifactIds: List<String> =
    listOf(
        skikoArtifacts.commonArtifactId,
        skikoArtifacts.jvmArtifactId,
        skikoArtifacts.jvmRuntimeArtifactIdFor(OS.Windows, Arch.X64),
        skikoArtifacts.jvmRuntimeArtifactIdFor(OS.Windows, Arch.Arm64),
        skikoArtifacts.jvmRuntimeArtifactIdFor(OS.Linux, Arch.X64),
        skikoArtifacts.jvmRuntimeArtifactIdFor(OS.Linux, Arch.Arm64),
        skikoArtifacts.jvmRuntimeArtifactIdFor(OS.MacOS, Arch.X64),
        skikoArtifacts.jvmRuntimeArtifactIdFor(OS.MacOS, Arch.Arm64),
        skikoArtifacts.jvmAdditionalRuntimeArtifactIdFor("angle", OS.Windows, Arch.X64),
        skikoArtifacts.jvmAdditionalRuntimeArtifactIdFor("angle", OS.Windows, Arch.Arm64),
        skikoArtifacts.jsWasmArtifactId,
        skikoArtifacts.jsArtifactId,
        skikoArtifacts.wasmArtifactId,
        skikoArtifacts.nativeArtifactIdFor(OS.Linux, Arch.X64),
        skikoArtifacts.nativeArtifactIdFor(OS.Linux, Arch.Arm64),
        skikoArtifacts.nativeArtifactIdFor(OS.MacOS, Arch.Arm64),
        skikoArtifacts.nativeArtifactIdFor(OS.MacOS, Arch.X64),
        skikoArtifacts.nativeArtifactIdFor(OS.IOS, Arch.X64),
        skikoArtifacts.nativeArtifactIdFor(OS.IOS, Arch.Arm64),
        skikoArtifacts.nativeArtifactIdFor(OS.IOS, Arch.Arm64, isUikitSim = true),
        skikoArtifacts.nativeArtifactIdFor(OS.TVOS, Arch.X64),
        skikoArtifacts.nativeArtifactIdFor(OS.TVOS, Arch.Arm64),
        skikoArtifacts.nativeArtifactIdFor(OS.TVOS, Arch.Arm64, isUikitSim = true),

        "${skikoArtifacts.jvmRuntimeArtifactId}-all",
)

val downloadSkikoArtifactsFromComposeDev by tasks.registering(DownloadFromSpaceMavenRepoTask::class) {
    modulesToDownload.set(skikoMavenModules(skiko.deployVersion))
    spaceRepoUrl.set("https://packages.jetbrains.team/maven/p/cmp/dev")
}

val createGithubRelease by tasks.registering {
    dependsOn(downloadSkikoArtifactsFromComposeDev)

    doLast {
        check(skiko.isRelease) { "This task should only be called for releases!" }
        val gh = connectToGitHub()
        val githubVersion = skiko.releaseGithubVersion
        val githubCommit = skiko.releaseGithubCommit
        val repo = gh.getRepository(GITHUB_REPO)
        val release = repo.createRelease("v$githubVersion")
            .name("Version $githubVersion")
            .generateReleaseNotes(true)
            .commitish(githubCommit)
            .create()

        val artifactsToUpload = skikoMavenModules(skiko.deployVersion).get().map { module ->
            val baseName = "${module.artifactId}-${module.version}"
            val pom = module.localDir.resolve("$baseName.pom")
            val regex = "<packaging>([a-zA-Z0-9]+)</packaging>".toRegex()
            val ext = regex.find(pom.readText())?.groupValues?.getOrNull(1) ?: "jar"
            module.localDir.resolve("$baseName.$ext").also {
                check(it.exists()) {
                    "'$it' does not exist"
                }
            }
        }
        for (artifact in artifactsToUpload) {
            logger.info("Uploading '$artifact'")
            release.uploadAsset(artifact, "application/zip")
        }
    }
}

val deleteGithubRelease by tasks.registering {
    doLast {
        val gh = connectToGitHub()
        val repo = gh.getRepository(GITHUB_REPO)
        repo.listReleases().firstOrNull { it.tagName == "v${skiko.releaseGithubVersion}" }?.delete()
    }
}

val uploadSkikoArtifactsToMavenCentral by tasks.registering(UploadToSonatypeTask::class) {
    dependsOn(downloadSkikoArtifactsFromComposeDev)

    deployName.set("Skiko ${skiko.deployVersion}")
    modulesToUpload.set(skikoMavenModules(skiko.deployVersion))

    user.set(mavenCentral.user)
    password.set(mavenCentral.password)
    publishAfterUploading.set(mavenCentral.publishAfterUploading)
}

fun Project.skikoMavenModules(version: String): Provider<List<ModuleToUpload>> =
    provider {
        val artifactsDir = layout.buildDirectory.dir("skiko-artifacts").get().asFile

        skikoArtifactIds.map { artifactId ->
            val skikoGroupId = SkikoArtifacts.DEFAULT_GROUP_ID
            ModuleToUpload(
                groupId = skikoGroupId,
                artifactId = artifactId,
                version = version,
                localDir = artifactsDir.resolve("$version/$skikoGroupId/$artifactId")
            )
        }
    }

fun connectToGitHub() =
    GitHubBuilder()
        .withOAuthToken(System.getenv("SKIKO_GH_RELEASE_TOKEN"))
        .build()


configure<PublishingExtension> {
    repositories {
        maven {
            name = "ComposeRepo"
            url = uri(skiko.composeRepoUrl)
            credentials {
                username = skiko.composeRepoUserName
                password = skiko.composeRepoKey
            }
        }
    }
}

// Aggregator task that publishes the awt runtime fat jars of every module that
// calls registerJvmRuntimeAllPublication(). CI only ever needs to invoke this single task.
val publishAllJvmRuntimeAllPublications = tasks.register("publishAllJvmRuntimeAllPublicationsToComposeRepoRepository") {
    group = "publishing"
    description =
        "Publishes the fat (-all) JVM runtime jars of every registered Skiko module to the Compose repository."
}

// Desktop JVM targets bundled into the fat runtime jar.
val jvmRuntimeAllTargets: List<Pair<OS, Arch>> =
    listOf(
        OS.Windows to Arch.X64,
        OS.Windows to Arch.Arm64,
        OS.Linux to Arch.X64,
        OS.Linux to Arch.Arm64,
        OS.MacOS to Arch.X64,
        OS.MacOS to Arch.Arm64,
    )

fun registerJvmRuntimeAllPublication(artifacts: SkikoArtifacts) {
    val allArtifactId = "${artifacts.jvmRuntimeArtifactId}-all"
    val publicationName = artifacts.artifactIdPrefix
        .split('-')
        .mapIndexed { index, part ->
            if (index == 0) part else part.replaceFirstChar { it.uppercaseChar() }
        }
        .joinToString("") + "JvmRuntimeAll"

    val runtimeModules = provider {
        val artifactsDir = layout.buildDirectory.dir("skiko-artifacts").get().asFile
        jvmRuntimeAllTargets.map { (os, arch) ->
            val artifactId = artifacts.jvmRuntimeArtifactIdFor(os, arch)
            ModuleToUpload(
                groupId = SkikoArtifacts.DEFAULT_GROUP_ID,
                artifactId = artifactId,
                version = skiko.deployVersion,
                localDir = artifactsDir.resolve(
                    "${skiko.deployVersion}/${SkikoArtifacts.DEFAULT_GROUP_ID}/$artifactId"
                )
            )
        }
    }

    val downloadInputs = tasks.register(
        "download${publicationName.replaceFirstChar { it.uppercaseChar() }}Inputs",
        DownloadFromSpaceMavenRepoTask::class.java
    ) {
        modulesToDownload.set(runtimeModules)
        spaceRepoUrl.set(skiko.composeRepoUrl)
    }

    val allJar = tasks.register("${publicationName}Jar", Jar::class.java) {
        dependsOn(downloadInputs)
        isZip64 = true
        archiveBaseName.set(allArtifactId)
        archiveVersion.set(skiko.deployVersion)
        destinationDirectory.set(layout.buildDirectory.dir(allArtifactId))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        runtimeModules.get().forEach { module ->
            // The downloaded jar's file name differs between releases and SNAPSHOTs: cmp/dev stores
            // unique (timestamped) snapshots, so the file is e.g.
            // "<artifactId>-0.0.0-20260630.105840-3.jar" rather than "<artifactId>-0.0.0-SNAPSHOT.jar".
            from(zipTree(provider { resolveDownloadedRuntimeJar(module) })) {
                exclude("**/META-INF/**")
            }
        }
    }

    val emptySourcesJar = tasks.register("${publicationName}SourcesJar", Jar::class.java) {
        archiveBaseName.set(allArtifactId)
        archiveVersion.set(skiko.deployVersion)
        archiveClassifier.set("sources")
        destinationDirectory.set(layout.buildDirectory.dir(allArtifactId))
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>(publicationName) {
                groupId = SkikoArtifacts.DEFAULT_GROUP_ID
                artifactId = allArtifactId
                version = skiko.deployVersion

                artifact(allJar)
                artifact(emptySourcesJar)

                pom {
                    name.set("Composition of all ${artifacts.displayName} JVM Runtimes")
                    description.set(artifacts.pomDescription)
                    configureSkikoPomMetadata()
                }
            }
        }
    }

    val publishTaskName =
        "publish${publicationName.replaceFirstChar { it.uppercaseChar() }}PublicationToComposeRepoRepository"
    val publishTask = tasks.named(publishTaskName)
    publishAllJvmRuntimeAllPublications.configure {
        dependsOn(publishTask)
    }
}

// Resolves the actual main runtime jar downloaded into `module.localDir`. For releases the canonical
// "<artifactId>-<version>.jar" name is used. For unique (timestamped) SNAPSHOTs the real file name is
// read from maven-metadata.xml (e.g. "<artifactId>-0.0.0-20260630.105840-3.jar").
fun resolveDownloadedRuntimeJar(module: ModuleToUpload): File {
    val canonical = module.localDir.resolve("${module.artifactId}-${module.version}.jar")
    if (canonical.exists()) return canonical

    if (module.version.endsWith("-SNAPSHOT")) {
        val resolvedVersion = readResolvedSnapshotJarVersion(module.localDir.resolve("maven-metadata.xml"))
        if (resolvedVersion != null) {
            val timestamped = module.localDir.resolve("${module.artifactId}-$resolvedVersion.jar")
            if (timestamped.exists()) return timestamped
        }
    }

    error(
        "Cannot find a downloaded runtime jar for ${module.artifactId}:${module.version} in " +
            "${module.localDir}. Available files: " +
            (module.localDir.list()?.sorted()?.joinToString() ?: "<none>")
    )
}

// Reads the resolved timestamped version of the main jar artifact
fun readResolvedSnapshotJarVersion(metadataFile: File): String? {
    if (!metadataFile.exists()) return null
    val doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(metadataFile)
    val snapshotVersions = doc.getElementsByTagName("snapshotVersion")
    for (i in 0 until snapshotVersions.length) {
        val element = snapshotVersions.item(i) as org.w3c.dom.Element
        val hasClassifier = element.getElementsByTagName("classifier").length > 0
        val extension = element.getElementsByTagName("extension").item(0)?.textContent
        if (!hasClassifier && extension == "jar") {
            return element.getElementsByTagName("value").item(0)?.textContent
        }
    }
    return null
}

registerJvmRuntimeAllPublication(skikoArtifacts)
