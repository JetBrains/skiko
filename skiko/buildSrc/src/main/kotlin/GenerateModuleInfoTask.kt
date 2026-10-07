import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class GenerateModuleInfoTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputDirectory
    abstract val classesDir: DirectoryProperty

    @get:Input
    abstract val moduleName: Property<String>

    @get:Classpath
    abstract val modulePath: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val analysisJar = temporaryDir.resolve("${moduleName.get().replace('.', '-')}.jar")
        val jdkBin = File(jdkHome, "bin")
        val executableSuffix = if (hostOs.isWindows) ".exe" else ""

        outputDir.get().asFile.deleteRecursively()
        execOperations.exec {
            executable(jdkBin.resolve("jar$executableSuffix"))
            args("--create", "--file", analysisJar, "-C", classesDir.get().asFile, ".")
        }.assertNormalExitValue()
        execOperations.exec {
            executable(jdkBin.resolve("jdeps$executableSuffix"))
            args(
                mutableListOf(
                    "--ignore-missing-deps",
                    "--generate-module-info", outputDir.get().asFile,
                    analysisJar,
                ).apply {
                    if (!modulePath.isEmpty) addAll(1, listOf("--module-path", modulePath.asPath))
                }
            )
        }.assertNormalExitValue()
    }
}
