package symbols

private val WHITESPACE = Regex("""\s+""")

/**
 * Parser for `nm` output
 *
 * Each symbol row uses the following layout:
 * ```
 * <name> <type> [<value>]
 * ```
 **/
internal fun parseNmPosix(
    output: String,
): Sequence<Symbol> =
    output
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .filterNot { it.endsWith(':') } // file/archive headers
        .mapNotNull(::parseNmPosixLine)

private fun parseNmPosixLine(line: String): Symbol? {
    val columns = line.split(WHITESPACE, limit = 4)
    if (columns.size < 2) return null

    val name = columns[0]
    val typeText = columns[1]
    if (typeText.length != 1) return null

    val typeLetter = typeText[0]
    val type = classifyNmSymbolType(typeLetter) ?: return null

    return Symbol(
        name = name,
        type = type,
    )
}

private fun classifyNmSymbolType(
    typeLetter: Char,
): SymbolType? =
    when {
        typeLetter == 'U' -> SymbolType.Undefined
        typeLetter.isUpperCase() -> SymbolType.DefinedGlobal
        typeLetter == 'u' -> SymbolType.DefinedGlobal
        else -> null
    }
