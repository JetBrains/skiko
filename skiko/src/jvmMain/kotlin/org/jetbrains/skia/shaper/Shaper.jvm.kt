package org.jetbrains.skia.shaper

import org.jetbrains.skia.FontFeature.Companion.arrayOfFontFeaturesToInterop
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.*

internal actual fun Shaper.doShape(
    textUtf8: ManagedString,
    fontIter: Iterator<FontRun?>,
    bidiIter: Iterator<BidiRun?>,
    scriptIter: Iterator<ScriptRun?>,
    langIter: Iterator<LanguageRun?>,
    opts: ShapingOptions,
    width: Float,
    runHandler: RunHandler
) {
    nativeCall(textUtf8) { textUtf8 ->
        interopScope {
            Shaper_nShape(
                _ptr,
                textUtf8.pointerOrNullPointer,
                fontIter as InteropPointer,
                bidiIter as InteropPointer,
                scriptIter as InteropPointer,
                langIter as InteropPointer,
                optsFeaturesLen = opts.features?.size ?: 0,
                optsFeaturesIntArray = arrayOfFontFeaturesToInterop(opts.features),
                optsBooleanProps = opts._booleanPropsToInt(),
                width = width,
                runHandler = runHandler as InteropPointer
            )
        }
    }
}