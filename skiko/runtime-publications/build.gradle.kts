import org.jetbrains.compose.internal.publishing.MavenCentralProperties

plugins {
    `maven-publish`
    signing
    org.gradle.crypto.checksum
}

val skiko = SkikoProperties(rootProject)
val desktopTargets = buildList {
    if (supportAwt) {
        add(hostOs to skiko.targetArch)
        if (hostOs == OS.MacOS && skiko.targetArch == Arch.Arm64) {
            add(OS.MacOS to Arch.X64)
        }
    }
}
val androidTargets = if (supportAndroid) {
    listOf(OS.Android to Arch.X64, OS.Android to Arch.Arm64)
} else {
    emptyList()
}
val coreArtifacts = SkikoArtifacts()

declareJvmRuntimePublications(
    skiko,
    listOf(
        JvmRuntimePublicationFamily(
            producerProjectPath = ":",
            artifacts = coreArtifacts,
            targets = desktopTargets + androidTargets,
        ),
        JvmRuntimePublicationFamily(
            producerProjectPath = ":skiko-graphite",
            artifacts = SkikoArtifacts(
                artifactIdPrefix = "skiko-graphite",
                displayName = "Skiko Graphite",
                pomDescription = "Kotlin Skia Graphite bindings",
            ),
            targets = desktopTargets,
            runtimeDependencies = listOf(coreArtifacts),
        ),
        JvmRuntimePublicationFamily(
            producerProjectPath = ":skiko-skottie",
            artifacts = SkikoArtifacts(
                artifactIdPrefix = "skiko-skottie",
                displayName = "Skiko Skottie",
                pomDescription = "Kotlin Skia Skottie bindings",
            ),
            targets = desktopTargets + androidTargets,
            runtimeDependencies = listOf(coreArtifacts),
        ),
    ),
)

val mavenCentral = MavenCentralProperties(project)
if (skiko.isTeamcityCIBuild || mavenCentral.signArtifacts) {
    signing {
        sign(publishing.publications)
        useInMemoryPgpKeys(mavenCentral.signArtifactsKey.get(), mavenCentral.signArtifactsPassword.get())
    }
}
