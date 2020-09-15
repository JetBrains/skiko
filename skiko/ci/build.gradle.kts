import org.kohsuke.github.*

val skiko = SkikoProperties(project)
val GITHUB_REPO = "JetBrains/skiko"
fun connectToGitHub() =
    GitHubBuilder()
        .withOAuthToken(System.getenv("SKIKO_GH_RELEASE_TOKEN"))
        .build()


repositories {
    maven("https://packages.jetbrains.team/maven/p/ui/dev")
}

val skikoArtifacts by configurations.creating
dependencies {
    fun skikoDep(artifact: String) {
        skikoArtifacts("org.jetbrains.skiko:$artifact:${skiko.deployVersion}") {
            isTransitive = false
        }
    }

    skikoDep("skiko-jvm")
    skikoDep("skiko-jvm-runtime-windows")
    skikoDep("skiko-jvm-runtime-linux")
    skikoDep("skiko-jvm-runtime-macos")
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