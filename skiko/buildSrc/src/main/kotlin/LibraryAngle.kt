import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.crypto.checksum.Checksum
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/**
 * ANGLE binaries packaging (Windows)
 */
fun Project.registerAngleBinariesPackaging(skiko: SkikoProperties) {
    val angleTag = property("dependencies.angle") as String
    val baseUrl = "https://github.com/JetBrains/angle-pack/releases/download/$angleTag"

    fun registerAngleTasksFor(arch: Arch) {
        if (hostOs != OS.Windows) return
        val archSuffix = when (arch) {
            Arch.X64 -> "x64"
            Arch.Arm64 -> "arm64"
            else -> return
        }
        val zipName = "Angle-$angleTag-windows-Release-$archSuffix.zip"

        val downloadTask = tasks.register<Download>("downloadAngleWindows${toTitleCase(arch.id)}") {
            group = "Angle Binaries"
            val url = "$baseUrl/$zipName"
            description = "downloads $url"
            onlyIfModified(true)
            src(url)
            dest(skiko.dependenciesDir.resolve("angle/$angleTag/$zipName"))
        }

        val unzipTask = tasks.register<Copy>("unzipAngleWindows${toTitleCase(arch.id)}") {
            group = "Angle Binaries"
            dependsOn(downloadTask)
            val outputDir = skiko.dependenciesDir.resolve("angle/$angleTag/Angle-$angleTag-windows-Release-$archSuffix")
            from(zipTree(downloadTask.get().dest))
            into(outputDir)
        }

        val prepareTask = tasks.register<Copy>("prepareAngleFilesWindows${toTitleCase(arch.id)}") {
            group = "Angle Binaries"
            dependsOn(unzipTask)
            val unzipDir = skiko.dependenciesDir.resolve("angle/$angleTag/Angle-$angleTag-windows-Release-$archSuffix")
            val srcDir = unzipDir.resolve("out/Release-windows-$archSuffix")
            from(srcDir) {
                include("libEGL.dll")
                include("libGLESv2.dll")
                rename("libEGL\\.dll", "skiko-angle-libEGL-windows-$archSuffix.dll")
                rename("libGLESv2\\.dll", "skiko-angle-libGLESv2-windows-$archSuffix.dll")
            }
            into(layout.buildDirectory.dir("angle/windows-$archSuffix").get())
        }

        val angleOutDirProvider = layout.buildDirectory.dir("angle/windows-$archSuffix")
        val eglFileProvider = angleOutDirProvider.map { it.file("skiko-angle-libEGL-windows-$archSuffix.dll").asFile }
        val glesFileProvider = angleOutDirProvider.map { it.file("skiko-angle-libGLESv2-windows-$archSuffix.dll").asFile }

        val checksumEgl = tasks.register<Checksum>("createAngleChecksumsEglWindows${toTitleCase(arch.id)}") {
            files = project.files(eglFileProvider)
            algorithm = Checksum.Algorithm.SHA256
            outputDir = file("$buildDir/checksums-angle-egl-windows-${archSuffix}")
            dependsOn(prepareTask)
        }
        val checksumGles = tasks.register<Checksum>("createAngleChecksumsGlesWindows${toTitleCase(arch.id)}") {
            files = project.files(glesFileProvider)
            algorithm = Checksum.Algorithm.SHA256
            outputDir = file("$buildDir/checksums-angle-gles-windows-${archSuffix}")
            dependsOn(prepareTask)
        }

        tasks.register<Jar>("skikoAngleRuntimeJarWindows${toTitleCase(arch.id)}") {
            group = "Angle Binaries"
            dependsOn(prepareTask, checksumEgl, checksumGles)
            archiveBaseName.set("skiko")
            archiveClassifier.set("windows-${archSuffix}-angle")
            from(eglFileProvider)
            from(glesFileProvider)
            from(checksumEgl.map { it.outputs.files.singleFile })
            from(checksumGles.map { it.outputs.files.singleFile })
        }
    }

    registerAngleTasksFor(Arch.X64)
    registerAngleTasksFor(Arch.Arm64)
}

/**
 * Include ANGLE runtime in test runtime directory on Windows
 */
fun Project.includeAngleRuntimeInTestsOnWindows(skiko: SkikoProperties) {
    if (supportAwt && hostOs == OS.Windows) {
        val archSuffix = when (skiko.targetArch) {
            Arch.X64 -> "X64"
            Arch.Arm64 -> "Arm64"
            else -> null
        }
        if (archSuffix != null) {
            val runtimeDirTaskName = "skikoRuntimeDirForTests${toTitleCase(hostOs.id)}${toTitleCase(skiko.targetArch.id)}"
            val angleJarTaskName = "skikoAngleRuntimeJarWindows$archSuffix"
            tasks.findByName(runtimeDirTaskName)?.let {
                tasks.named<Copy>(runtimeDirTaskName).configure {
                    // Ensure ANGLE jar built
                    dependsOn(tasks.named(angleJarTaskName))
                    val angleJarProvider = tasks.named<Jar>(angleJarTaskName)
                    from(angleJarProvider.flatMap { it.archiveFile }.map { zipTree(it) })
                }
            }
        }
    }
}

/**
 * ANGLE runtime publications for Windows x64 and arm64
 */
fun PublishingExtension.configureAnglePublications(
    project: Project,
    emptySourcesJar: TaskProvider<Jar>,
    pomNameForPublication: MutableMap<String, String>
) {
    publications.configureAnglePublications(project, emptySourcesJar, pomNameForPublication)
}

fun PublicationContainer.configureAnglePublications(
    project: Project,
    emptySourcesJar: TaskProvider<Jar>,
    pomNameForPublication: MutableMap<String, String>
) {
    if (project.supportAwt) {
        val angleWindowsX64Jar = project.tasks.named("skikoAngleRuntimeJarWindowsX64")
        val angleWindowsArm64Jar = project.tasks.named("skikoAngleRuntimeJarWindowsArm64")

        create<MavenPublication>("skikoJvmRuntimeAngleWindowsX64") {
            pomNameForPublication[name] = "Skiko JVM ANGLE Runtime for Windows X64"
            artifactId = "skiko-awt-runtime-angle-windows-x64"
            project.afterEvaluate {
                artifact(angleWindowsX64Jar.get())
                artifact(emptySourcesJar)
            }
        }
        create<MavenPublication>("skikoJvmRuntimeAngleWindowsArm64") {
            pomNameForPublication[name] = "Skiko JVM ANGLE Runtime for Windows Arm64"
            artifactId = "skiko-awt-runtime-angle-windows-arm64"
            project.afterEvaluate {
                artifact(angleWindowsArm64Jar.get())
                artifact(emptySourcesJar)
            }
        }
    }
}

/**
 * Ensure base Windows runtime publish also publishes ANGLE runtime to MavenLocal
 */
fun Project.ensureAnglePublishDependencies() {
    if (supportAwt) {
        tasks.findByName("publishSkikoJvmRuntimeWindowsX64PublicationToMavenLocal")
            ?.dependsOn("publishSkikoJvmRuntimeAngleWindowsX64PublicationToMavenLocal")
        tasks.findByName("publishSkikoJvmRuntimeWindowsArm64PublicationToMavenLocal")
            ?.dependsOn("publishSkikoJvmRuntimeAngleWindowsArm64PublicationToMavenLocal")
    }
}
