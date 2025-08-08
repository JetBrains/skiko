import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.crypto.checksum.Checksum
import org.gradle.kotlin.dsl.register

class AngleProjectContext {
    val allJvmRuntimeJars = mutableMapOf<Pair<OS, Arch>, TaskProvider<Jar>>()
}

fun Project.registerAngleBinariesPackaging(skiko: SkikoProperties): AngleProjectContext {
    val context = AngleProjectContext()
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

            val jarTask = tasks.register<Jar>("skikoAngleRuntimeJar$taskSuffix") {
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
            context.allJvmRuntimeJars[targetOs to targetArch] = jarTask
        }

        registerAngleTasksFor(OS.Windows, Arch.X64)
        registerAngleTasksFor(OS.Windows, Arch.Arm64)
    }
    return context
}
