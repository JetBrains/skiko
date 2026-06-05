import org.kohsuke.github.*
import org.jetbrains.compose.internal.publishing.*

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
