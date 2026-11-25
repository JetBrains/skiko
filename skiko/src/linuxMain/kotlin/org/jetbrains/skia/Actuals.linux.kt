package org.jetbrains.skia

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.withResult

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

@SymbolName("uloc_getDefault_skiko")
private external fun uloc_getDefault(): CPointer<ByteVar>
@SymbolName("uloc_toLanguageTag_skiko")
private external fun uloc_toLanguageTag(localeId: CPointer<ByteVar>, buffer: InteropPointer, size: Int, strict: Boolean, err: InteropPointer): Int
