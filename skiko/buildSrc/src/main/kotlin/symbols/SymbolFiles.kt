package symbols

import java.nio.file.Path
import kotlin.io.path.readLines
import kotlin.io.path.writeText

internal fun generateDefFile(exportedTxt: Path, output: Path) {
    val symbols = exportedTxt.readSymbolLines()

    output.writeText(buildString {
        appendLine("EXPORTS")
        symbols.forEach { symbol ->
            appendLine("    $symbol")
        }
    })
}

internal fun generateVersionScript(symbolsTxt: Path, output: Path) {
    output.writeText(versionScript(global = symbolsTxt.readSymbolLines(), local = listOf("*")))
}

internal fun versionScript(global: List<String> = emptyList(), local: List<String> = emptyList()): String = buildString {
    appendLine("{")
    if (global.isNotEmpty()) {
        appendLine("  global:")
        global.forEach { symbol ->
            appendLine("    $symbol;")
        }
    }
    if (local.isNotEmpty()) {
        appendLine("  local:")
        local.forEach { symbol ->
            appendLine("    $symbol;")
        }
    }
    appendLine("};")
}

private fun Path.readSymbolLines(): List<String> =
    readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
