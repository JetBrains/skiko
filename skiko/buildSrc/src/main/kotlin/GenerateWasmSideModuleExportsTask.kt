import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.intellij.lang.annotations.Language
import java.io.ByteArrayOutputStream
import javax.inject.Inject

abstract class GenerateWasmSideModuleExportsTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    // Tool path comes from setupEmscripten; it is not source data for this task's output.
    @get:Internal
    abstract val nodeExecutable: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sideModules: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun run() {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execOperations.exec {
            commandLine(
                nodeExecutable.get().asFile.absolutePath,
                // Emscripten's bundled Node 20 needs this to compile side modules that use
                // extended constant expressions. Newer Node versions enable it by default.
                "--experimental-wasm-extended-const",
                "-e",
                EXTRACT_IMPORTS_SCRIPT,
                *sideModules.files.map { it.absolutePath }.toTypedArray()
            )
            standardOutput = stdout
            errorOutput = stderr
            isIgnoreExitValue = true
        }

        val stdoutText = stdout.toString().trim()
        val stderrText = stderr.toString().trim()
        if (stderrText.isNotEmpty()) {
            logger.warn(stderrText)
        }
        if (result.exitValue != 0) {
            throw GradleException(
                "Failed to extract WASM side-module imports (exit code ${result.exitValue}).\n" +
                    "stderr: $stderrText"
            )
        }

        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(stdoutText.lines().joinToString(separator = "\n", postfix = "\n"))
        }
    }

    private companion object {
        @Language("JavaScript")
        val EXTRACT_IMPORTS_SCRIPT = """
            const fs = require("fs");

            // These imports are provided by the side-module loader itself and do not need
            // matching exports from the main wasm module.
            const runtimeImports = new Set([
                "__indirect_function_table",
                "__memory_base",
                "__stack_pointer",
                "__table_base",
                "memory",
            ]);
            const symbols = new Set([
                "__data_end",
                "__global_base",
                "__heap_base",
                "__heap_end",
                "__indirect_function_table",
                "__stack_high",
                "__stack_low",
                "__stack_pointer",
            ]);

            for (const file of process.argv.slice(1)) {
                const module = new WebAssembly.Module(fs.readFileSync(file));
                for (const imp of WebAssembly.Module.imports(module)) {
                    // Side-module env function imports resolve through exported functions
                    // from the main module, except for the runtime imports above.
                    if (imp.module === "env" && imp.kind === "function" && !runtimeImports.has(imp.name)) {
                        symbols.add(imp.name);
                    }
                    // GOT globals need exported symbol addresses so the runtime loader can
                    // populate their WebAssembly.Global entries.
                    if ((imp.module === "GOT.func" || imp.module === "GOT.mem") && imp.kind === "global") {
                        symbols.add(imp.name);
                    }
                }
            }
            console.log(Array.from(symbols).sort().join("\n"));
        """.trimIndent()
    }
}
