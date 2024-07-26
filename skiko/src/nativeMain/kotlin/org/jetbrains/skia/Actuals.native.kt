package org.jetbrains.skia

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.withResult

internal actual fun <R> commonSynchronized(lock: Any, block: () -> R) {
    block()
}

internal actual fun String.intCodePoints(): IntArray = IntArray(this.length) { this[it].code }

actual class Pattern constructor(regex: String) {
    private val _regex = Regex(regex)

    actual fun split(input: CharSequence): Array<String> = _regex.split(input).toTypedArray()
    actual fun matcher(input: CharSequence): Matcher = Matcher(_regex, input)
}

actual class Matcher constructor(private val regex: Regex, private val input: CharSequence) {

    private val matches: Boolean by lazy {
        regex.matches(input)
    }

    private val groups: MatchGroupCollection? by lazy { regex.matchEntire(input)?.groups }

    actual fun group(ix: Int): String? = groups?.get(ix)?.value
    actual fun matches(): Boolean = matches
}

private val LANG by lazy {
    val localeFromICU = uloc_getDefault()
    var length = 0
    val maxLength = 128
    val langTag = withResult(ByteArray(maxLength)) {
        length = uloc_toLanguageTag(localeFromICU, it, maxLength, false, toInterop(intArrayOf(0)))
    }.decodeToString(0, length)
    langTag.ifEmpty { "en-US" }
}

internal actual fun defaultLanguageTag(): String = LANG

internal actual fun compilePattern(regex: String): Pattern = Pattern(regex)

actual typealias ExternalSymbolName = kotlin.native.SymbolName

//@SymbolName("uloc_getDefault_skiko")
@SymbolName("uloc_getDefault")
private external fun uloc_getDefault(): CPointer<ByteVar>
//@SymbolName("uloc_toLanguageTag_skiko")
@SymbolName("uloc_toLanguageTag")
private external fun uloc_toLanguageTag(localeId: CPointer<ByteVar>, buffer: InteropPointer, size: Int, strict: Boolean, err: InteropPointer): Int