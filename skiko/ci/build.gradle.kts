import org.kohsuke.github.*
import org.jetbrains.compose.internal.publishing.*

val skiko = SkikoProperties(project)
val mavenCentral = MavenCentralProperties(project)
val GITHUB_REPO = "JetBrains/skiko"

val skikoArtifactIds: List<String> =
    listOf(
        SkikoArtifacts.commonArtifactId,
        SkikoArtifacts.jvmArtifactId,
        SkikoArtifacts.jvmRuntimeArtifactIdFor(OS.Windows, Arch.X64),
        SkikoArtifacts.jvmRuntimeArtifactIdFor(OS.Windows, Arch.Arm64),
        SkikoArtifacts.jvmRuntimeArtifactIdFor(OS.Linux, Arch.X64),
        SkikoArtifacts.jvmRuntimeArtifactIdFor(OS.Linux, Arch.Arm64),
        SkikoArtifacts.jvmRuntimeArtifactIdFor(OS.MacOS, Arch.X64),
        SkikoArtifacts.jvmRuntimeArtifactIdFor(OS.MacOS, Arch.Arm64),
        SkikoArtifacts.jsWasmArtifactId,
        SkikoArtifacts.nativeArtifactIdFor(OS.Linux, Arch.X64),
        SkikoArtifacts.nativeArtifactIdFor(OS.MacOS, Arch.Arm64),
        SkikoArtifacts.nativeArtifactIdFor(OS.MacOS, Arch.X64),
        SkikoArtifacts.nativeArtifactIdFor(OS.IOS, Arch.X64),
        SkikoArtifacts.nativeArtifactIdFor(OS.IOS, Arch.Arm64),
        SkikoArtifacts.nativeArtifactIdFor(OS.IOS, Arch.Arm64, isIosSim = true),
        SkikoArtifacts.nativeArtifactIdFor(OS.TVOS, Arch.X64),
        SkikoArtifacts.nativeArtifactIdFor(OS.TVOS, Arch.Arm64),
        SkikoArtifacts.nativeArtifactIdFor(OS.TVOS, Arch.Arm64, isIosSim = true),
)

val downloadSkikoArtifactsFromComposeDev by tasks.registering(DownloadFromSpaceMavenRepoTask::class) {
    modulesToDownload.set(skikoMavenModules(skiko.deployVersion))
    spaceRepoUrl.set("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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

    version.set(skiko.deployVersion)
    modulesToUpload.set(skikoMavenModules(skiko.deployVersion))

    sonatypeServer.set("https://oss.sonatype.org")
    user.set(mavenCentral.user)
    password.set(mavenCentral.password)
    autoCommitOnSuccess.set(mavenCentral.autoCommitOnSuccess)
    stagingProfileName.set("org.jetbrains.skiko")
}

fun Project.skikoMavenModules(version: String): Provider<List<ModuleToUpload>> =
    provider {
        val artifactsDir = buildDir.resolve("skiko-artifacts")

        skikoArtifactIds.map { artifactId ->
            val skikoGroupId = "org.jetbrains.skiko"
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
