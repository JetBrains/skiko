import org.jetbrains.compose.internal.publishing.MavenCentralProperties
import org.jetbrains.skiko.build.context.supportAndroid
import org.jetbrains.skiko.build.context.supportAwt
import org.jetbrains.skiko.build.dependencies.additionalRuntimeNameAttribute
import org.jetbrains.skiko.build.publishing.JvmRuntimePublicationFamily
import org.jetbrains.skiko.build.publishing.declareJvmRuntimePublications
import org.jetbrains.skiko.build.utils.Arch
import org.jetbrains.skiko.build.utils.OS
import org.jetbrains.skiko.build.utils.SkikoArtifacts
import org.jetbrains.skiko.build.utils.SkikoProperties
import org.jetbrains.skiko.build.utils.hostOs

plugins {
    `maven-publish`
    signing
    org.gradle.crypto.checksum
}

evaluationDependsOn(":skiko-platform")

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
val platformProject = project(":skiko-platform")
val ganeshArtifacts = SkikoArtifacts(
    artifactIdPrefix = "skiko-ganesh",
    displayName = "Skiko Ganesh",
    pomDescription = "Kotlin Skia Ganesh bindings",
)

declareJvmRuntimePublications(
    skiko,
    listOf(
        JvmRuntimePublicationFamily(
            producerProjectPath = ":",
            artifacts = coreArtifacts,
            targets = desktopTargets + androidTargets,
            additionalRuntimes = rootProject.configurations.filter {
                it.attributes.getAttribute(additionalRuntimeNameAttribute) != null
            },
        ),
        JvmRuntimePublicationFamily(
            producerProjectPath = ":skiko-platform",
            artifacts = SkikoArtifacts(
                artifactIdPrefix = "skiko-platform",
                displayName = "Skiko Platform",
                pomDescription = "Kotlin Skia platform layer",
            ),
            targets = desktopTargets + androidTargets,
            runtimeDependencies = listOf(coreArtifacts, ganeshArtifacts),
            additionalRuntimes = platformProject.configurations.filter {
                it.attributes.getAttribute(additionalRuntimeNameAttribute) != null
            },
        ),
        JvmRuntimePublicationFamily(
            producerProjectPath = ":skiko-ganesh",
            artifacts = ganeshArtifacts,
            targets = desktopTargets + androidTargets,
            runtimeDependencies = listOf(coreArtifacts),
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
