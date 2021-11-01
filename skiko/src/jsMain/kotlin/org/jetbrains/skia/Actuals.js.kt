package org.jetbrains.skia

actual abstract class OutputStream

actual fun <R> commonSynchronized(lock: Any, block: () -> R) {
    block()
}

actual fun String.intCodePoints(): IntArray = TODO()

actual class Pattern constructor(regex: String) {
    private val _regex = Regex(regex)

    actual fun split(input: CharSequence): Array<String?>? = _regex.split(input).toTypedArray()
    actual fun matcher(input: CharSequence): Matcher = Matcher(_regex, input)
}

actual class Matcher constructor(private val regex: Regex, private val input: CharSequence) {

    private val matches: Boolean by lazy {
        regex.matches(input)
    }

    private val groups: MatchGroupCollection? by lazy { regex.matchEntire(input)?.groups }
    private val namedGroups: MatchNamedGroupCollection? by lazy {
        regex.matchEntire(input)?.groups as? MatchNamedGroupCollection
    }

    actual fun group(name: String): String? = namedGroups?.get(name)?.value
    actual fun group(ix: Int): String? = groups?.get(ix)?.value
    actual fun matches(): Boolean = matches
}

actual fun defaultLanguageTag(): String = TODO()

actual fun compilePattern(regex: String): Pattern = Pattern(regex)

actual typealias ExternalSymbolName = kotlin.js.JsName
