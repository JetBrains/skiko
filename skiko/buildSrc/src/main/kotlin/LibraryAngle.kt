import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.crypto.checksum.Checksum
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

fun Project.registerAngleBinariesPackaging(skiko: SkikoProperties) {
    if (supportAwt && hostOs == OS.Windows) {
        val angleTag = property("dependencies.angle") as String
        val baseUrl = "https://github.com/JetBrains/angle-pack/releases/download/$angleTag"

        fun registerAngleTasksFor(targetOs: OS, targetArch: Arch) {
            val taskSuffix = toTitleCase(targetOs.id) + toTitleCase(targetArch.id)
            val fileSuffix = targetOs.id + "-" + targetArch.id

            val zipName = "Angle-$angleTag-${targetOs.id}-Release-${targetArch.id}"

            val downloadTask = tasks.register<Download>("downloadAngle$taskSuffix") {
                group = "Angle Binaries"
                val url = "$baseUrl/$zipName.zip"
                description = "downloads $url"
                onlyIfModified(true)
                src(url)
                dest(skiko.dependenciesDir.resolve("angle/$angleTag/$zipName.zip"))
            }

            val unzipTask = tasks.register<Copy>("unzipAngle$taskSuffix") {
                group = "Angle Binaries"
                dependsOn(downloadTask)
                val outputDir = skiko.dependenciesDir.resolve("angle/$angleTag/$zipName")
                from(zipTree(downloadTask.get().dest))
                into(outputDir)
            }

            val prepareTask = tasks.register<Copy>("prepareAngleFiles$taskSuffix") {
                group = "Angle Binaries"
                dependsOn(unzipTask)
                val unzipDir = skiko.dependenciesDir.resolve("angle/$angleTag/$zipName")
                val srcDir = unzipDir.resolve("out/Release-$fileSuffix")
                from(srcDir) {
                    include("libEGL.dll")
                    include("libGLESv2.dll")
                    rename("libEGL\\.dll", "skiko-angle-libEGL-$fileSuffix.dll")
                    rename("libGLESv2\\.dll", "skiko-angle-libGLESv2-$fileSuffix.dll")
                }
                into(layout.buildDirectory.dir("angle/$fileSuffix").get())
            }

            val angleOutDirProvider = layout.buildDirectory.dir("angle/$fileSuffix")
            val eglFileProvider = angleOutDirProvider.map { it.file("skiko-angle-libEGL-$fileSuffix.dll").asFile }
            val glesFileProvider = angleOutDirProvider.map { it.file("skiko-angle-libGLESv2-$fileSuffix.dll").asFile }

            val checksumEgl = tasks.register<Checksum>("createAngleChecksumsEgl$taskSuffix") {
                files = project.files(eglFileProvider)
                algorithm = Checksum.Algorithm.SHA256
                outputDir = file("$buildDir/checksums-angle-egl-$fileSuffix")
                dependsOn(prepareTask)
            }
            val checksumGles = tasks.register<Checksum>("createAngleChecksumsGles$taskSuffix") {
                files = project.files(glesFileProvider)
                algorithm = Checksum.Algorithm.SHA256
                outputDir = file("$buildDir/checksums-angle-gles-$fileSuffix")
                dependsOn(prepareTask)
            }

            tasks.register<Jar>("skikoAngleRuntimeJar$taskSuffix") {
                group = "Angle Binaries"
                dependsOn(prepareTask, checksumEgl, checksumGles)
                val target = targetId(targetOs, targetArch)
                archiveBaseName.set("skiko-angle")
                archiveClassifier.set(target)
                from(eglFileProvider)
                from(glesFileProvider)
                from(checksumEgl.map { it.outputs.files.singleFile })
                from(checksumGles.map { it.outputs.files.singleFile })
            }

            afterEvaluate {
                tasks.findByName("publishSkikoJvmRuntime${taskSuffix}PublicationToMavenLocal")
                    ?.dependsOn("publishSkikoJvmRuntimeAngle${taskSuffix}PublicationToMavenLocal")
                tasks.findByName("publishSkikoJvmRuntime${taskSuffix}PublicationToComposeRepoRepository")
                    ?.dependsOn("publishSkikoJvmRuntimeAngle${taskSuffix}PublicationToComposeRepoRepository")
            }
        }

        registerAngleTasksFor(OS.Windows, Arch.X64)
        registerAngleTasksFor(OS.Windows, Arch.Arm64)
    }
}

fun PublicationContainer.configureAnglePublications(
    project: Project,
    emptySourcesJar: TaskProvider<Jar>,
    pomNameForPublication: MutableMap<String, String>
) {
    if (project.supportAwt && hostOs == OS.Windows) {
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
