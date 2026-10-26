package org.jetbrains.skiko.build.publishing

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.nativeplatform.MachineArchitecture
import org.gradle.nativeplatform.OperatingSystemFamily
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.skiko.build.context.supportAwt
import org.jetbrains.skiko.build.dependencies.additionalRuntimeArchAttribute
import org.jetbrains.skiko.build.dependencies.additionalRuntimeConfigurationPrefix
import org.jetbrains.skiko.build.dependencies.additionalRuntimeNameAttribute
import org.jetbrains.skiko.build.dependencies.additionalRuntimeOsAttribute
import org.jetbrains.skiko.build.utils.Arch
import org.jetbrains.skiko.build.utils.OS
import org.jetbrains.skiko.build.utils.SkikoArtifacts
import org.jetbrains.skiko.build.utils.SkikoProperties
import org.jetbrains.skiko.build.utils.joinToTitleCamelCase
import org.jetbrains.skiko.build.utils.targetId
import org.jetbrains.skiko.build.utils.toTitleCase

data class JvmRuntimePublicationFamily(
    val producerProjectPath: String,
    val artifacts: SkikoArtifacts,
    val targets: List<Pair<OS, Arch>>,
    val runtimeDependencies: List<SkikoArtifacts> = emptyList(),
)

private val desktopRuntimeTargets = listOf(
    OS.MacOS to Arch.X64, OS.MacOS to Arch.Arm64,
    OS.Linux to Arch.X64, OS.Linux to Arch.Arm64,
    OS.Windows to Arch.X64, OS.Windows to Arch.Arm64,
)

private fun additionalRuntimeDetails(configuration: Configuration): Triple<String, OS, Arch> = Triple(
    requireNotNull(configuration.attributes.getAttribute(additionalRuntimeNameAttribute)),
    OS.valueOf(requireNotNull(configuration.attributes.getAttribute(additionalRuntimeOsAttribute))),
    Arch.valueOf(requireNotNull(configuration.attributes.getAttribute(additionalRuntimeArchAttribute))),
)

// Sign tasks write next to their input files, so each publication needs its own archive path.
private fun Project.emptyPublicationJar(publicationName: String, artifactId: String, classifier: String): TaskProvider<Jar> =
    tasks.register<Jar>("${publicationName}Empty${toTitleCase(classifier)}Jar") {
        archiveBaseName.set(artifactId)
        archiveClassifier.set(classifier)
    }

fun Project.declareJvmRuntimePublications(
    skiko: SkikoProperties,
    families: List<JvmRuntimePublicationFamily>,
) {
    plugins.withType(MavenPublishPlugin::class.java) {
        configurePublishingRepositories(skiko)
        configurePublishingTaskGroups()
        families.forEach { family -> configureRuntimePublicationFamily(skiko, family) }
    }
}

private fun Project.configurePublishingRepositories(skiko: SkikoProperties) {
    extensions.getByType<PublishingExtension>().repositories {
        configureEach {
            val repoName = name
            tasks.register("publishTo${repoName}") {
                group = "publishing"
                dependsOn(tasks.named("publishAllPublicationsTo${repoName}Repository"))
            }
        }
        maven {
            name = "BuildRepo"
            url = rootProject.layout.buildDirectory.dir("repo").get().asFile.toURI()
        }
        maven {
            name = "ComposeRepo"
            url = uri(skiko.composeRepoUrl)
            credentials {
                username = skiko.composeRepoUserName
                password = skiko.composeRepoKey
            }
        }
    }
}

private fun Project.configurePublishingTaskGroups() {
    afterEvaluate {
        val publishToTasks = extensions.getByType<PublishingExtension>().repositories
            .map { "publishTo${it.name}" } + "publishToMavenLocal"

        tasks.configureEach {
            if (group == "publishing" && name != "publish" && name !in publishToTasks) {
                group = "other publishing"
            }
        }
    }
}

private fun Project.configureRuntimePublicationFamily(
    skiko: SkikoProperties,
    family: JvmRuntimePublicationFamily,
) {
    val prefix = family.artifacts.artifactIdPrefix
        .split("-")
        .let { parts -> parts.first() + joinToTitleCamelCase(*parts.drop(1).toTypedArray()) }
    val runtimeInputs = family.targets.associateWith { (os, arch) ->
        configurations.create("${prefix}RuntimeInput${joinToTitleCamelCase(os.id, arch.id)}") {
            isCanBeConsumed = false
            isCanBeResolved = true
            dependencies.add(
                project.dependencies.project(
                    family.producerProjectPath,
                    "jvmRuntimeJarElements${joinToTitleCamelCase(os.id, arch.id)}",
                )
            )
        }
    }

    extensions.getByType<PublishingExtension>().publications {
        runtimeInputs.forEach { (target, input) ->
            val (os, arch) = target
            val publicationName = "${prefix}JvmRuntime${joinToTitleCamelCase(os.id, arch.id)}"
            val artifactId = family.artifacts.jvmRuntimeArtifactIdFor(os, arch)
            val sourcesJar = emptyPublicationJar(publicationName, artifactId, "sources")
            val javadocJar = emptyPublicationJar(publicationName, artifactId, "javadoc")
            create<MavenPublication>(publicationName) {
                groupId = SkikoArtifacts.DEFAULT_GROUP_ID
                this.artifactId = artifactId
                version = skiko.deployVersion
                artifact(layout.file(provider { input.singleFile })) {
                    builtBy(input)
                }
                artifact(sourcesJar)
                artifact(javadocJar)
                pom {
                    name.set("${family.artifacts.displayName} JVM Runtime for ${os.name} ${arch.name}")
                    description.set(family.artifacts.pomDescription)
                    configureSkikoPomMetadata()
                    withXml {
                        val dependenciesNode = asNode().appendNode("dependencies")
                        dependenciesNode.appendNode("dependency").apply {
                            appendNode("groupId", SkikoArtifacts.DEFAULT_GROUP_ID)
                            appendNode("artifactId", family.artifacts.jvmArtifactId)
                            appendNode("version", "[${skiko.deployVersion}]")
                            appendNode("scope", "compile")
                        }
                        family.runtimeDependencies.forEach { dependency ->
                            dependenciesNode.appendNode("dependency").apply {
                                appendNode("groupId", SkikoArtifacts.DEFAULT_GROUP_ID)
                                appendNode("artifactId", dependency.jvmRuntimeArtifactIdFor(os, arch))
                                appendNode("version", "[${skiko.deployVersion}]")
                                appendNode("scope", "compile")
                            }
                        }
                    }
                }
            }
        }
    }

    if (supportAwt) {
        configureAwtRuntimeAllJarPublication(skiko, family, prefix, runtimeInputs)
        configureAwtRuntimeJarPublication(skiko, family, prefix)
    }

    // Keep the producer projects' publishing entry points after moving their publications here.
    val publicationNames = runtimeInputs.keys.map { (os, arch) ->
        "SkikoJvmRuntime${joinToTitleCamelCase(os.id, arch.id)}" to
            "${toTitleCase(prefix)}JvmRuntime${joinToTitleCamelCase(os.id, arch.id)}"
    } + if (supportAwt) listOf(
        "AwtRuntimeAll" to "${toTitleCase(prefix)}AwtRuntimeAll",
        "AwtRuntimeElements" to "${toTitleCase(prefix)}AwtRuntimeElements",
    ) else emptyList()
    val producerTasks = project(family.producerProjectPath).tasks
    val runtimeTasks = tasks
    val repositories = extensions.getByType<PublishingExtension>().repositories
    fun registerProducerPublishAlias(oldName: String, newName: String) {
        repositories.forEach { repository ->
            val taskSuffix = "PublicationTo${repository.name}Repository"
            val publish = producerTasks.register("publish${oldName}$taskSuffix") {
                group = "publishing"
                dependsOn(runtimeTasks.named("publish${newName}$taskSuffix"))
            }
            producerTasks.matching { it.name == "publishTo${repository.name}" }
                .configureEach { dependsOn(publish) }
            producerTasks.matching { it.name == "publish" }.configureEach { dependsOn(publish) }
        }
        val publishToMavenLocal = producerTasks.register("publish${oldName}PublicationToMavenLocal") {
            group = "publishing"
            dependsOn(runtimeTasks.named("publish${newName}PublicationToMavenLocal"))
        }
        producerTasks.matching { it.name == "publishToMavenLocal" }
            .configureEach { dependsOn(publishToMavenLocal) }
    }
    publicationNames.forEach { (oldName, newName) -> registerProducerPublishAlias(oldName, newName) }

    project(family.producerProjectPath).configurations
        .matching { it.name.startsWith(additionalRuntimeConfigurationPrefix) }
        .configureEach {
            val runtime = this
            // The producer sets the runtime attributes after the configuration is added, so read them later.
            gradle.projectsEvaluated {
                configureAdditionalRuntimePublication(skiko, family, prefix, runtime)
                val (runtimeName, os, arch) = additionalRuntimeDetails(runtime)
                val suffix = joinToTitleCamelCase(os.id, arch.id)
                val additionalSuffix = "${toTitleCase(runtimeName)}$suffix"
                registerProducerPublishAlias(
                    "SkikoJvmRuntime$additionalSuffix",
                    "${toTitleCase(prefix)}JvmRuntime$additionalSuffix",
                )
                repositories.forEach { repository ->
                    val taskSuffix = "PublicationTo${repository.name}Repository"
                    runtimeTasks.named("publish${toTitleCase(prefix)}JvmRuntime${suffix}$taskSuffix").configure {
                        dependsOn(runtimeTasks.named("publish${toTitleCase(prefix)}JvmRuntime${additionalSuffix}$taskSuffix"))
                    }
                }
                runtimeTasks.named("publish${toTitleCase(prefix)}JvmRuntime${suffix}PublicationToMavenLocal").configure {
                    dependsOn(runtimeTasks.named("publish${toTitleCase(prefix)}JvmRuntime${additionalSuffix}PublicationToMavenLocal"))
                }
            }
        }
}

private fun Project.configureAdditionalRuntimePublication(
    skiko: SkikoProperties,
    family: JvmRuntimePublicationFamily,
    prefix: String,
    runtime: Configuration,
) {
    val (runtimeName, os, arch) = additionalRuntimeDetails(runtime)
    val suffix = joinToTitleCamelCase(os.id, arch.id)
    val input = configurations.create("${prefix}${toTitleCase(runtimeName)}RuntimeInput$suffix") {
        isCanBeConsumed = false
        isCanBeResolved = true
        dependencies.add(project.dependencies.project(family.producerProjectPath, runtime.name))
    }
    val publicationName = "${prefix}JvmRuntime${toTitleCase(runtimeName)}$suffix"
    val artifactId = family.artifacts.jvmAdditionalRuntimeArtifactIdFor(runtimeName, os, arch)
    val sourcesJar = emptyPublicationJar(publicationName, artifactId, "sources")
    val javadocJar = emptyPublicationJar(publicationName, artifactId, "javadoc")
    extensions.getByType<PublishingExtension>().publications.create<MavenPublication>(publicationName) {
        groupId = SkikoArtifacts.DEFAULT_GROUP_ID
        this.artifactId = artifactId
        version = skiko.deployVersion
        artifact(layout.file(provider { input.singleFile })) { builtBy(input) }
        artifact(sourcesJar)
        artifact(javadocJar)
        pom {
            name.set("${family.artifacts.displayName} ${toTitleCase(runtimeName)} Runtime for ${os.id} ${arch.id}")
            description.set(family.artifacts.pomDescription)
            configureSkikoPomMetadata()
        }
    }
}

private fun Project.configureAwtRuntimeAllJarPublication(
    skiko: SkikoProperties,
    family: JvmRuntimePublicationFamily,
    prefix: String,
    runtimeInputs: Map<Pair<OS, Arch>, Configuration>,
) {
    val inputsDir = rootProject.layout.projectDirectory.dir(
        "dependencies/awtRuntimeAllInputs/${family.artifacts.artifactIdPrefix}"
    )
    val inputPattern = "${family.artifacts.artifactIdPrefix}-${skiko.deployVersion}-*.jar"
    val allJar = tasks.register<Jar>("${prefix}AwtRuntimeAllJar") {
        isZip64 = true
        archiveBaseName.set(family.artifacts.jvmRuntimeAllArtifactId)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        if (skiko.isTeamcityCIBuild) {
            // CI bundle all defined targets read directly from the staging directory.
            from({ fileTree(inputsDir) { include(inputPattern) }.files.sortedBy { it.name }.map(::zipTree) }) {
                exclude("**/META-INF/**")
            }
            doFirst {
                val missing = desktopRuntimeTargets
                    .map { (os, arch) -> "${family.artifacts.artifactIdPrefix}-${skiko.deployVersion}-${targetId(os, arch)}.jar" }
                    .filterNot { inputsDir.file(it).asFile.isFile }
                check(missing.isEmpty()) { "Missing AWT runtime input jars in ${inputsDir.asFile}: ${missing.joinToString()}" }
            }
        } else {
            runtimeInputs.filterKeys { it in desktopRuntimeTargets }.values.forEach { input ->
                dependsOn(input)
                from({ zipTree(input.singleFile) }) { exclude("**/META-INF/**") }
            }
        }
    }

    val publicationName = "${prefix}AwtRuntimeAll"
    val artifactId = family.artifacts.jvmRuntimeAllArtifactId
    val sourcesJar = emptyPublicationJar(publicationName, artifactId, "sources")
    val javadocJar = emptyPublicationJar(publicationName, artifactId, "javadoc")
    extensions.getByType<PublishingExtension>().publications.create<MavenPublication>(publicationName) {
        groupId = SkikoArtifacts.DEFAULT_GROUP_ID
        this.artifactId = artifactId
        version = skiko.deployVersion
        artifact(allJar)
        artifact(sourcesJar)
        artifact(javadocJar)
        pom {
            name.set("Composition of all ${family.artifacts.displayName} AWT Runtimes")
            description.set(family.artifacts.pomDescription)
            configureSkikoPomMetadata()
            withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                dependenciesNode.appendNode("dependency").apply {
                    appendNode("groupId", SkikoArtifacts.DEFAULT_GROUP_ID)
                    appendNode("artifactId", family.artifacts.jvmArtifactId)
                    appendNode("version", "[${skiko.deployVersion}]")
                    appendNode("scope", "compile")
                }
                family.runtimeDependencies.forEach { dependency ->
                    dependenciesNode.appendNode("dependency").apply {
                        appendNode("groupId", SkikoArtifacts.DEFAULT_GROUP_ID)
                        appendNode("artifactId", dependency.jvmRuntimeAllArtifactId)
                        appendNode("version", "[${skiko.deployVersion}]")
                        appendNode("scope", "compile")
                    }
                }
            }
        }
    }

    if (skiko.isTeamcityCIBuild) {
        runtimeInputs.forEach { (target, input) ->
            val (os, arch) = target
            val stage = tasks.register<Copy>("${prefix}Stage${joinToTitleCamelCase(os.id, arch.id)}AwtRuntimeJar") {
                from(input)
                into(inputsDir)
                rename { "${family.artifacts.artifactIdPrefix}-${skiko.deployVersion}-${targetId(os, arch)}.jar" }
            }
            tasks.named(
                "publish${toTitleCase(prefix)}JvmRuntime${joinToTitleCamelCase(os.id, arch.id)}PublicationToComposeRepoRepository"
            ).configure { dependsOn(stage) }
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
 *     - variant awtRuntimeElements-macos-arm64
 *          - depends on org.jetbrains.skiko:skiko-awt-runtime-macos-arm64
 *
 *     - variant awtRuntimeElements-macos-x64
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
private fun Project.configureAwtRuntimeJarPublication(
    skiko: SkikoProperties,
    family: JvmRuntimePublicationFamily,
    prefix: String,
) {
    val variants = desktopRuntimeTargets.map { (os, arch) ->
        configurations.create("${prefix}AwtRuntimeElements-${targetId(os, arch)}") {
            isCanBeConsumed = false
            isCanBeResolved = false

            /* Setup default attributes */
            attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
            attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
            attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
            attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
            attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
            attributes.attribute(
                TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                objects.named(TargetJvmEnvironment.STANDARD_JVM),
            )

            /*
            Add OS and architecture attributes to the exposed configuration.
             */
            attributes.attribute(
                OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE,
                objects.named(
                    when (os) {
                        OS.Linux -> OperatingSystemFamily.LINUX
                        OS.Windows -> OperatingSystemFamily.WINDOWS
                        OS.MacOS -> OperatingSystemFamily.MACOS
                        else -> error("Unsupported desktop runtime OS: $os")
                    }
                ),
            )
            attributes.attribute(
                MachineArchitecture.ARCHITECTURE_ATTRIBUTE,
                objects.named(
                    when (arch) {
                        Arch.X64 -> MachineArchitecture.X86_64
                        Arch.Arm64 -> MachineArchitecture.ARM64
                        else -> error("Unsupported desktop runtime architecture: $arch")
                    }
                ),
            )

            /*
            Add the dependency to the actual JVM runtime artifact.
             */
            dependencies.add(
                project.dependencies.create(
                    SkikoArtifacts.DEFAULT_GROUP_ID,
                    family.artifacts.jvmRuntimeArtifactIdFor(os, arch),
                    skiko.deployVersion,
                )
            )
        }
    }

    /* Create a new software component and add all variants */
    val component = serviceOf<SoftwareComponentFactory>().adhoc("${prefix}AwtRuntimeElements")
    variants.forEach { variant ->
        component.addVariantsFromConfiguration(variant) { mapToMavenScope("runtime") }
    }

    /* Create the actual publication for this */
    val publicationName = "${prefix}AwtRuntimeElements"
    val artifactId = family.artifacts.jvmRuntimeArtifactId
    val javadocJar = emptyPublicationJar(publicationName, artifactId, "javadoc")
    extensions.getByType<PublishingExtension>().publications.create<MavenPublication>(publicationName) {
        from(component)
        groupId = SkikoArtifacts.DEFAULT_GROUP_ID
        this.artifactId = artifactId
        version = skiko.deployVersion
        artifact(javadocJar)
        pom {
            name.set("${family.artifacts.displayName} JVM Runtime")
            description.set(family.artifacts.pomDescription)
            configureSkikoPomMetadata()

            /*
            The entire machinery only works with Gradle attributes;
            therefore, we do not add any dependencies to the maven pom file
             */
            withXml {
                val nodes = asElement().getElementsByTagName("dependencies")
                for (i in nodes.length - 1 downTo 0) {
                    nodes.item(i).parentNode.removeChild(nodes.item(i))
                }
            }
        }
    }
}
