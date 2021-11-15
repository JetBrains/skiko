import org.kohsuke.github.*
import org.jetbrains.compose.internal.publishing.*

val skiko = SkikoProperties(project)
val GITHUB_REPO = "JetBrains/skiko"
fun connectToGitHub() =
    GitHubBuilder()
        .withOAuthToken(System.getenv("SKIKO_GH_RELEASE_TOKEN"))
        .build()

repositories {
    maven(skiko.composeRepoUrl)
}

val skikoArtifactIds: List<String> =
    listOf(
        SkikoArtifacts.commonArtifactId,
        SkikoArtifacts.jvmArtifactId,
        SkikoArtifacts.jvmRuntimeArtifactIdFor(OS.Windows, Arch.X64),
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
)

val githubArtifacts by configurations.creating
dependencies {
    for (artifactId in skikoArtifactIds) {
        githubArtifacts("org.jetbrains.skiko:$artifactId:${skiko.deployVersion}") {
            isTransitive = false
        }
    }
}

val createGithubRelease by tasks.registering {
    doLast {
        check(skiko.isRelease) { "This task should only be called for releases!" }
        val gh = connectToGitHub()
        val githubVersion = skiko.releaseGithubVersion
        val githubCommit = skiko.releaseGithubCommit
        val repo = gh.getRepository("JetBrains/skiko")
        val release = repo.createRelease("v$githubVersion")
            .name("Version $githubVersion")
            .commitish(githubCommit)
            .create()

        githubArtifacts
            .filter { it.extension == "jar" }
            .forEach { release.uploadAsset(it, "application/zip") }
    }
}

val deleteGithubRelease by tasks.registering {
    doLast {
        val gh = connectToGitHub()
        val repo = gh.getRepository(GITHUB_REPO)
        repo.listReleases().firstOrNull { it.tagName == "v${skiko.releaseGithubVersion}" }?.delete()
    }
}

fun skikoMavenModules(version: Provider<String>): Provider<List<ModuleToUpload>> =
    version.map { version ->
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

val mavenCentral = MavenCentralProperties(project)
val downloadSkikoArtifactsFromComposeDev by tasks.registering(DownloadFromSpaceMavenRepoTask::class) {
    modulesToDownload.set(skikoMavenModules(mavenCentral.version))
    spaceRepoUrl.set("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val uploadSkikoArtifactsToMavenCentral by tasks.registering(UploadToSonatypeTask::class) {
    dependsOn(downloadSkikoArtifactsFromComposeDev)

    version.set(mavenCentral.version)
    modulesToUpload.set(skikoMavenModules(mavenCentral.version))

    sonatypeServer.set("https://oss.sonatype.org")
    user.set(mavenCentral.user)
    password.set(mavenCentral.password)
    autoCommitOnSuccess.set(mavenCentral.autoCommitOnSuccess)
    stagingProfileName.set("org.jetbrains.skiko")
}
