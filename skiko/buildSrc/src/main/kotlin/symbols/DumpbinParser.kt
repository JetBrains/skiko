package symbols

private val WHITESPACE = Regex("""\s+""")

private val STORAGE_CLASSES = setOf(
    "External",
    "Static",
    "Label",
    "Filename",
    "WeakExternal",
    "EndOfFunction",
    "BeginFunction",
)

/**
 * Helper symbols emitted by Windows COFF toolchains that are not valid .def exports.
 */
private fun isCompilerGeneratedName(name: String): Boolean =
    name.startsWith("__imp_") ||
            name.startsWith(".refptr") ||
            name.startsWith("__real@") ||
            name.startsWith("__xmm@") ||
            name.startsWith("??_C@") ||
            name.startsWith("\"")


/**
 * Parser for `dumpbin /SYMBOLS` output (Windows COFF object files / static libs)
 *
 * Each symbol row uses the following layout:
 * ```
 * 000 00000000 SECT1  notype       External     | SymbolName
 * 001 00000000 UNDEF  notype ()    External     | OtherSymbol
 * ```
 **/
internal fun parseDumpbinSymbols(output: String): Sequence<Symbol> = sequence {
    for (rawLine in output.lineSequence()) {
        val line = rawLine.trim()
        if (line.isEmpty()) continue

        val (header, symbolPart) = line
            .split('|', limit = 2)
            .takeIf { it.size == 2 }
            ?.let { (header, symbol) -> header to symbol }
            ?: continue

        val headerTokens = header.trim().split(WHITESPACE)
        if (headerTokens.size < 4) continue

        val section = headerTokens.getOrNull(2) ?: continue
        val storage = headerTokens.lastOrNull { it in STORAGE_CLASSES } ?: continue

        if (storage != "External") continue

        val name = symbolPart.trim().substringBefore(' ')
        if (name.isEmpty() || isCompilerGeneratedName(name)) continue

        val defined = section != "UNDEF"

        yield(
            Symbol(
                name = name,
                type = if (defined) SymbolType.DefinedGlobal else SymbolType.Undefined,
            )
        )
    }
}
