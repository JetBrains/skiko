package org.jetbrains.skiko.build.publishing

import kotlin.jvm.java
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.skiko.build.context.SkikoProjectContext
import org.jetbrains.skiko.build.context.supportAndroid
import org.jetbrains.skiko.build.context.supportAwt
import org.jetbrains.skiko.build.utils.Arch
import org.jetbrains.skiko.build.utils.OS
import org.jetbrains.skiko.build.utils.SkikoArtifacts
import org.jetbrains.skiko.build.utils.targetId
import org.jetbrains.skiko.build.utils.registerOrGetTask
import org.jetbrains.skiko.build.utils.toTitleCase

private val SkikoProjectContext.publishing get() = project.extensions.getByType(PublishingExtension::class.java)

/**
 * Shared POM metadata for all Skiko publications (license, project URL, SCM, developers).
 */
fun MavenPom.configureSkikoPomMetadata() {
    licenses {
        license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    val repoUrl = "https://www.github.com/JetBrains/skiko"
    url.set(repoUrl)
    scm {
        url.set(repoUrl)
        val repoConnection = "scm:git:$repoUrl.git"
        connection.set(repoConnection)
        developerConnection.set(repoConnection)
    }
    developers {
        developer {
            name.set("Compose Multiplatform Team")
            organization.set("JetBrains")
            organizationUrl.set("https://www.jetbrains.com")
        }
    }
}

private val awtRuntimeTargets = listOf(
    OS.MacOS to Arch.X64, OS.MacOS to Arch.Arm64,
    OS.Linux to Arch.X64, OS.Linux to Arch.Arm64,
    OS.Windows to Arch.X64, OS.Windows to Arch.Arm64
)

private class SkikoPublishingContext(
    val projectContext: SkikoProjectContext,
) {
    val project = projectContext.project
    val kotlin = projectContext.kotlin
    val skiko = projectContext.skiko
    val skikoArtifacts = projectContext.artifacts

    val pomNameForPublication: MutableMap<String, String> = HashMap()
    fun publishing(configure: PublishingExtension.() -> Unit) {
        projectContext.publishing.apply(configure)
    }

    fun publications(configure: PublicationContainer.() -> Unit) {
        projectContext.publishing.publications.apply(configure)
    }

}

fun SkikoProjectContext.declarePublications() {
    val ctx = SkikoPublishingContext(this)
    ctx.configurePublishingRepositories()
    ctx.configurePublicationDefaults()
    if (kotlin.targets.findByName("awt") != null) {
        ctx.configureAwtPublicationConstraints()
    }
    ctx.configureWebPublication()
    ctx.configureAndroidPublication()

    ctx.configurePomNames()
    ctx.configurePublishingTaskGroups()
}

private val SkikoPublishingContext.emptySourcesJar
    get() = project.tasks.registerOrGetTask<Jar>("emptySourcesJar") {
        archiveClassifier.set("sources")
    }

private val SkikoPublishingContext.emptyJavadocJar
    get() = project.tasks.registerOrGetTask<Jar>("emptyJavadocJar") {
        archiveClassifier.set("javadoc")
    }

private fun SkikoPublishingContext.configurePublishingRepositories() {
    publishing {
        repositories {
            configureEach {
                val repoName = name
                project.tasks.register("publishTo${repoName}") {
                    group = "publishing"
                    dependsOn(project.tasks.named("publishAllPublicationsTo${repoName}Repository"))
                }
            }
            maven {
                name = "BuildRepo"
                url = project.rootProject.layout.buildDirectory.dir("repo").get().asFile.toURI()
            }
            maven {
                name = "ComposeRepo"
                url = project.uri(skiko.composeRepoUrl)
                credentials {
                    username = skiko.composeRepoUserName
                    password = skiko.composeRepoKey
                }
            }
        }
    }
}

private fun SkikoPublishingContext.configurePublishingTaskGroups() {
    project.afterEvaluate {
        val publishToTasks = projectContext.publishing.repositories.map { "publishTo${it.name}" } + "publishToMavenLocal"

        tasks.configureEach {
            if (group == "publishing" && name != "publish" && name !in publishToTasks) {
                group = "other publishing"
            }
        }
    }
}

private fun SkikoPublishingContext.configurePublicationDefaults() {
    pomNameForPublication["kotlinMultiplatform"] = "${skikoArtifacts.displayName} KMP"
    kotlin.targets.forEach {
        pomNameForPublication[it.name] = "${skikoArtifacts.displayName} ${toTitleCase(it.name)}"
    }

    publishing {
        publications.configureEach {
            this as MavenPublication
            groupId = SkikoArtifacts.DEFAULT_GROUP_ID

            // Necessary for publishing to Maven Central
            artifact(emptyJavadocJar)

            pom {
                description.set(skikoArtifacts.pomDescription)
                configureSkikoPomMetadata()
            }
        }
    }
}

/**
 * Adds dependency constraints from the skiko-awt (Kotlin) publication to all skiko-awt-runtime-* (JNI) artifacts.
 * This ensures compatibility between the Kotlin and native runtime artifacts.
 * 
 * Constraints are added to the awt target's configurations, which automatically propagates them to both:
 * - Maven POM (via dependencyManagement section)
 * - Gradle Module Metadata (via dependencyConstraints in variants)
 *
 * Note that the consumable 'awtApiElements'/'awtRuntimeElements' configurations do not allow declaring
 * dependency constraints, so the constraints are declared on the 'awtCompilationApi' bucket, which both
 * of them extend.
 */
private fun SkikoPublishingContext.configureAwtPublicationConstraints() {
    // Add constraints to Gradle configurations
    // This will automatically generate both POM dependencyManagement and Gradle Module Metadata dependencyConstraints
    project.configurations.findByName("awtCompilationApi")?.let { config ->
        // Note: "!!" suffix is used to enforce a strict version
        // See https://docs.gradle.org/current/userguide/dependency_versions.html#sec:rich-version-constraints

        // Add constraint for the uber runtime artifact
        config.dependencyConstraints.add(
            project.dependencies.constraints.create(
                "${SkikoArtifacts.DEFAULT_GROUP_ID}:${skikoArtifacts.jvmRuntimeArtifactId}:${skiko.deployVersion}!!"
            )
        )

        // Add constraints for platform-specific runtime artifacts
        awtRuntimeTargets.forEach { (os, arch) ->
            config.dependencyConstraints.add(
                project.dependencies.constraints.create(
                    "${SkikoArtifacts.DEFAULT_GROUP_ID}:${skikoArtifacts.jvmRuntimeArtifactIdFor(os, arch)}:${skiko.deployVersion}!!"
                )
            )
        }
    }
}

private fun SkikoPublishingContext.configureWebPublication() {
    kotlin.targets.withType(KotlinJsIrTarget::class.java).all {
        val skikoWasmRuntimeConfig = project.configurations.create("skikoWasmRuntimeElementsFor${toTitleCase(name)}") {
            isCanBeConsumed = true
            isCanBeResolved = false
            attributes {
                val runtimeConf = this@all.project.configurations.getByName(
                    this@all.compilations.getByName("main").runtimeDependencyConfigurationName
                )
                runtimeConf.attributes.keySet().forEach {
                    attribute(it as Attribute<Any>, runtimeConf.attributes.getAttribute(it) as Any)
                }
                attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, "skiko-runtime"))
            }
            outgoing.artifact(project.tasks.named("skikoWasmJar")) {
                classifier = "skiko-runtime"
            }
        }
        addVariantToKotlinTarget(this, skikoWasmRuntimeConfig)
    }
}

private fun SkikoPublishingContext.configureAndroidPublication() = publications {
    if (!project.supportAndroid) return@publications
    pomNameForPublication["android"] = "${skikoArtifacts.displayName} Android Runtime"
}

private fun SkikoPublishingContext.configurePomNames() = publications {
    val publicationsWithoutPomNames = this.toList().filter { it.name !in pomNameForPublication }
    if (publicationsWithoutPomNames.isNotEmpty()) {
        error("Publications with unknown POM names: ${publicationsWithoutPomNames.joinToString { "'${it.name}'" }}")
    }
    configureEach {
        this as MavenPublication
        pom.name.set(pomNameForPublication[name]!!)
    }
}
