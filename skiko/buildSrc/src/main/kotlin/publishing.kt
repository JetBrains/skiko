import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar


private val SkikoProjectContext.publishing get() = project.extensions.getByType(PublishingExtension::class.java)

private class SkikoPublishingContext(
    val projectContext: SkikoProjectContext,
) {
    val project = projectContext.project
    val kotlin = projectContext.kotlin
    val skiko = projectContext.skiko

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
    ctx.configureAllJvmRuntimeJarPublications()
    ctx.configureWebPublication()
    ctx.configureAndroidPublication()

    ctx.configurePomNames()
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

private fun SkikoPublishingContext.configurePublicationDefaults() {
    pomNameForPublication["kotlinMultiplatform"] = "Skiko MPP"
    kotlin.targets.forEach {
        pomNameForPublication[it.name] = "Skiko ${toTitleCase(it.name)}"
    }

    publishing {
        publications.configureEach {
            this as MavenPublication
            groupId = SkikoArtifacts.groupId

            // Necessary for publishing to Maven Central
            artifact(emptyJavadocJar)

            pom {
                description.set("Kotlin Skia bindings")
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
        }
    }
}

private fun SkikoPublishingContext.configureAllJvmRuntimeJarPublications() = publications {
    projectContext.allJvmRuntimeJars.forEach { entry ->
        val os = entry.key.first
        val arch = entry.key.second
        create("skikoJvmRuntime${toTitleCase(os.id)}${toTitleCase(arch.id)}", MavenPublication::class.java) {
            pomNameForPublication[name] = "Skiko JVM Runtime for ${os.name} ${arch.name}"
            artifactId = SkikoArtifacts.jvmRuntimeArtifactIdFor(os, arch)
            project.afterEvaluate {
                artifact(entry.value.map { it.archiveFile.get() })
                artifact(emptySourcesJar)
            }
            pom.withXml {
                asNode().appendNode("dependencies")
                    .appendNode("dependency").apply {
                        appendNode("groupId", SkikoArtifacts.groupId)
                        appendNode("artifactId", SkikoArtifacts.jvmArtifactId)
                        appendNode("version", skiko.deployVersion)
                        appendNode("scope", "compile")
                    }
            }
        }
    }
}

private fun SkikoPublishingContext.configureWebPublication() = publications {
    if (!project.supportWeb) return@publications
    create("skikoWasmRuntime", MavenPublication::class.java) {
        pomNameForPublication[name] = "Skiko WASM Runtime"
        artifactId = SkikoArtifacts.jsWasmArtifactId
        artifact(project.tasks.named("skikoWasmJar").get())
        artifact(emptySourcesJar)
    }
}

private fun SkikoPublishingContext.configureAndroidPublication() = publications {
    if (!project.supportAndroid) return@publications
    pomNameForPublication["androidRelease"] = "Skiko Android Runtime"
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
