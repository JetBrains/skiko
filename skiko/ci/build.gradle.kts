import org.kohsuke.github.*

val skiko = SkikoProperties(project)
val GITHUB_REPO = "JetBrains/skiko"
fun connectToGitHub() =
    GitHubBuilder()
        .withOAuthToken(System.getenv("SKIKO_GH_RELEASE_TOKEN"))
        .build()


repositories {
    maven(skiko.composeRepoUrl)
}

val skikoArtifacts by configurations.creating
dependencies {
    fun skikoDep(artifact: String) {
        skikoArtifacts("org.jetbrains.skiko:$artifact:${skiko.deployVersion}") {
            isTransitive = false
        }
    }

    skikoDep(SkikoArtifacts.commonArtifactId)
    skikoDep(SkikoArtifacts.metadataArtifactId)
    skikoDep(SkikoArtifacts.runtimeArtifactIdFor(OS.Windows, Arch.X64))
    skikoDep(SkikoArtifacts.runtimeArtifactIdFor(OS.Linux, Arch.X64))
    skikoDep(SkikoArtifacts.runtimeArtifactIdFor(OS.Linux, Arch.Arm64))
    skikoDep(SkikoArtifacts.runtimeArtifactIdFor(OS.MacOS, Arch.X64))
    skikoDep(SkikoArtifacts.runtimeArtifactIdFor(OS.MacOS, Arch.Arm64))
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

        skikoArtifacts
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
