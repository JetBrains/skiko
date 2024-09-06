package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skiko.w3c.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal actual fun <R> commonSynchronized(lock: Any, block: () -> R) {
    block()
}

actual fun String.intCodePoints(): IntArray = IntArray(this.length) { this[it].code }

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

@Suppress("RedundantNullableReturnType")
val LANG: String by lazy {
    val lang: String? = window.navigator.language
    if (lang == null || lang.isEmpty()) { "en-US" } else { lang }
}

actual fun defaultLanguageTag(): String = LANG

actual fun compilePattern(regex: String): Pattern = Pattern(regex)


suspend fun someFoo(): Int {
    return suspendCoroutine {
        val continuation = it

        val callbackPtr = interopScope {
            virtual {
                println(":::someFoo ::: callback body\n")
                continuation.resume(10)
            }
        }
        println(":::someFoo ::: CallbackPtr = $callbackPtr\n")
        skikoInvokeV(1)
    }
}

@ExternalSymbolName("_skiko_invoke_v")
@ModuleImport("./skiko.mjs", "skiko_invoke_v")
external fun skikoInvokeV(callback: InteropPointer)