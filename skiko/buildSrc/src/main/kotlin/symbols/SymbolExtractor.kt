package symbols

import OS
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File

internal class SymbolExtractor(
    private val execOperations: ExecOperations,
    private val os: OS,
    private val androidLlvmNm: String? = null,
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
                val output = run(nmCommand.executable, nmCommand.args + actualFiles.map { it.absolutePath })
                parseNmPosix(output)
                    .filter { it.type == type }
                    .mapTo(mutableSetOf()) { it.name }
            }

            OS.Windows -> {
                val output = run("dumpbin", listOf("/SYMBOLS") + actualFiles.map { it.absolutePath })
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
        return when (os) {
            OS.IOS, OS.TVOS -> Command("xcrun", listOf("nm") + args)
            OS.Android -> Command(
                androidLlvmNm ?: error("androidLlvmNm is required for Android symbol extraction"),
                args
            )
            OS.Linux, OS.MacOS -> Command("nm", args)
            else -> error("nm symbol extraction does not support ${os.name} target")
        }
    }

    private fun run(executable: String, args: List<String>): String {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val result = execOperations.exec {
            this.executable = executable
            this.args = args
            standardOutput = stdout
            errorOutput = stderr
            isIgnoreExitValue = true
        }
        if (result.exitValue != 0) {
            error(
                """
                Command failed with exit code ${result.exitValue}: $executable ${args.joinToString(" ")}
                stderr:
                $stderr
                """.trimIndent()
            )
        }
        return stdout.toString()
    }

    private data class Command(val executable: String, val args: List<String>)
}
