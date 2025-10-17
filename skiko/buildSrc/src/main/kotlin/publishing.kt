import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.nativeplatform.MachineArchitecture
import org.gradle.nativeplatform.OperatingSystemFamily
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType


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
    ctx.configureAwtRuntimeJarPublication()
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

/**
 * There are several artifacts, providing the native runtime, for each OS and architecture:
 * - skiko-awt-runtime-macos-arm64
 * - skiko-awt-runtime-macos-x64
 * - ...
 *
 * Each of those artifacts gets published using its own maven coordinates.
 * In order to support consumers who would like to express a single dependency on Skiko, this 'uber' publication is created,
 * listing each OS and architecture-specific artifact as a dependency to a Gradle variant, distinguised by
 * default Gradle attributes.
 *
 * ```
 * org.jetbrains.skiko:skiko-awt-runtime
 *     - variant jvmRuntimeElements-macos-arm64
 *          - depends on org.jetbrains.skiko:skiko-awt-runtime-macos-arm64
 *
 *     - variant jvmRuntimeElements-macos-x64
 *          - depends on org.jetbrains.skiko:skiko-awt-runtime-macos-x64
 * ...
 * ```
 *
 * This allows Gradle consumers to add a single, universal dependency
 * ```
 * dependencies {
 *      implementation("org.jetbrains.skiko:skiko-awt-runtime:...")
 * }
 * ```
 *
 * which resolves the correct artifact for the current platform.
 */
private fun SkikoPublishingContext.configureAwtRuntimeJarPublication() {
    /*
    Defines all the child targets that this uber publication can point to
     */
    val childAwtRuntimeTargets = listOf(
        OS.MacOS to Arch.X64, OS.MacOS to Arch.Arm64,
        OS.Linux to Arch.X64, OS.Linux to Arch.Arm64,
        OS.Windows to Arch.X64, OS.Windows to Arch.Arm64
    )

    val allJvmRuntimeVariants = childAwtRuntimeTargets.map { (os, arch) ->
        project.configurations.create("awtRuntimeElements-${targetId(os, arch)}").apply {

            /* Setup default attributes */
            attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
            attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
            attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling.EXTERNAL))
            attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
            attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
            attributes.attribute(
                TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                project.objects.named(TargetJvmEnvironment.STANDARD_JVM)
            )

            /*
            Add OS and architecture attributes to the exposed configuration.
             */
            attributes.attribute(
                OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE,
                project.objects.named(
                    when (os) {
                        OS.Linux -> OperatingSystemFamily.LINUX
                        OS.Windows -> OperatingSystemFamily.WINDOWS
                        OS.MacOS -> OperatingSystemFamily.MACOS
                        else -> error("Unsupported OS for jvmRuntimeElements: $os")
                    }
                )
            )

            attributes.attribute(
                MachineArchitecture.ARCHITECTURE_ATTRIBUTE,
                project.objects.named(
                    when (arch) {
                        Arch.X64 -> MachineArchitecture.X86_64
                        Arch.Arm64 -> MachineArchitecture.ARM64
                        else -> error("Unsupported arch for jvmRuntimeElements: $arch")
                    }
                )
            )

            /*
            Add the dependency to the actual JVM runtime artifact.
             */
            dependencies.add(
                project.dependencies.create(
                    SkikoArtifacts.groupId,
                    SkikoArtifacts.jvmRuntimeArtifactIdFor(os, arch),
                    skiko.deployVersion
                )
            )
        }
    }

    /* Create a new software component and add all variants */
    val component = project.serviceOf<SoftwareComponentFactory>().adhoc("jvmRuntimeElements")
    allJvmRuntimeVariants.forEach { variant ->
        component.addVariantsFromConfiguration(variant) {
            mapToMavenScope("runtime")
        }
    }

    /* Create the actual publication for this */
    publications {
        create("jvmRuntimeElements", MavenPublication::class.java) {
            from(component)
            pomNameForPublication[name] = "Skiko JVM Runtime"
            groupId = SkikoArtifacts.groupId
            artifactId = "skiko-awt-runtime"
            version = skiko.deployVersion

            /*
            The entire machinery only works with Gradle attributes;
            therefore, we do not add any dependencies to the maven pom file
             */
            pom {
                withXml {
                    val dependencyNodes = asElement().getElementsByTagName("dependencies")
                    for (i in 0 until dependencyNodes.length) {
                        dependencyNodes.item(i).parentNode.removeChild(dependencyNodes.item(i))
                    }
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
