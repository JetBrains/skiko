package symbols

import OS
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File

internal class SymbolExtractor(
    private val execOperations: ExecOperations,
    private val os: OS,
    private val command: List<String>,
) {
    fun extract(files: Collection<File>, type: SymbolType): Set<String> {
        val actualFiles = files.filter { it.isFile }
        if (actualFiles.isEmpty()) return emptySet()
        require(type == SymbolType.DefinedGlobal || type == SymbolType.Undefined) {
            "Symbol extraction does not support ${type.name} symbols"
        }
        return when (os) {
            OS.Linux, OS.Android, OS.MacOS, OS.IOS, OS.TVOS -> {
                val nmCommand = nmCommand(type)
                val output = run(nmCommand.command, nmCommand.args + actualFiles.map { it.absolutePath })
                parseNmPosix(output)
                    .filter { it.type == type }
                    .mapTo(mutableSetOf()) { it.name }
            }

            OS.Windows -> {
                val output = run(
                    command,
                    listOf("/SYMBOLS") + actualFiles.map { it.absolutePath }
                )
                parseDumpbinSymbols(output)
                    .filter { it.type == type }
                    .mapTo(mutableSetOf()) { it.name }
            }

            OS.Wasm -> error("Symbol extraction does not support ${os.name} target")
        }
    }

    private fun nmCommand(type: SymbolType): Command {
        val args = when (type) {
            SymbolType.Undefined -> listOf("-P", "-u")
            SymbolType.DefinedGlobal -> when (os) {
                OS.MacOS, OS.IOS, OS.TVOS -> listOf("-P", "-g", "-U")
                OS.Linux, OS.Android -> listOf("-P", "-g", "--defined-only")
                else -> error("nm symbol extraction does not support ${os.name} target")
            }
        }
        return Command(command, args)
    }

    private fun run(command: List<String>, args: List<String>): String {
        require(command.isNotEmpty()) { "Symbol extractor command must not be empty" }
        val executable = command.first()
        val allArgs = command.drop(1) + args
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execOperations.exec {
            this.executable = executable
            this.args = allArgs
            standardOutput = stdout
            errorOutput = stderr
            isIgnoreExitValue = true
        }
        if (result.exitValue != 0) {
            error(
                """
                Command failed with exit code ${result.exitValue}: $executable ${allArgs.joinToString(" ")}
                stderr:
                $stderr
                """.trimIndent()
            )
        }
        return stdout.toString()
    }

    private data class Command(val command: List<String>, val args: List<String>)
}
