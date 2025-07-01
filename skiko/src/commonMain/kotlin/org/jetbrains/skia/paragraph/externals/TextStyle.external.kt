package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFinalizer")
internal external fun TextStyle_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nMake")
internal external fun TextStyle_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nEquals")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nEquals")
internal external fun TextStyle_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFontStyle")
internal external fun TextStyle_nGetFontStyle(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetFontStyle")
internal external fun TextStyle_nSetFontStyle(ptr: NativePointer, fontStyle: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFontSize")
internal external fun TextStyle_nGetFontSize(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetFontSize")
internal external fun TextStyle_nSetFontSize(ptr: NativePointer, size: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFamilies")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFontFamilies")
internal external fun TextStyle_nGetFontFamilies(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetHeight")
internal external fun TextStyle_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetHeight")
internal external fun TextStyle_nSetHeight(ptr: NativePointer, override: Boolean, height: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetHalfLeading")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetHalfLeading")
internal external fun TextStyle_nGetHalfLeading(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetHalfLeading")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetHalfLeading")
internal external fun TextStyle_nSetHalfLeading(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetTopRatio")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetTopRatio")
internal external fun TextStyle_nGetTopRatio(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetTopRatio")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetTopRatio")
internal external fun TextStyle_nSetTopRatio(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineShift")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineShift")
internal external fun TextStyle_nGetBaselineShift(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineShift")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineShift")
internal external fun TextStyle_nSetBaselineShift(ptr: NativePointer, baselineShift: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAttributeEquals")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nAttributeEquals")
internal external fun TextStyle_nAttributeEquals(ptr: NativePointer, attribute: Int, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetColor")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetColor")
internal external fun TextStyle_nGetColor(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetColor")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetColor")
internal external fun TextStyle_nSetColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetForeground")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetForeground")
internal external fun TextStyle_nGetForeground(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetForeground")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetForeground")
internal external fun TextStyle_nSetForeground(ptr: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBackground")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetBackground")
internal external fun TextStyle_nGetBackground(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBackground")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetBackground")
internal external fun TextStyle_nSetBackground(ptr: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetDecorationStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetDecorationStyle")
internal external fun TextStyle_nGetDecorationStyle(ptr: NativePointer, decorationStyle: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetDecorationStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetDecorationStyle")
internal external fun TextStyle_nSetDecorationStyle(
    ptr: NativePointer,
    underline: Boolean,
    overline: Boolean,
    lineThrough: Boolean,
    gaps: Boolean,
    color: Int,
    style: Int,
    thicknessMultiplier: Float
)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetShadowsCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetShadowsCount")
internal external fun TextStyle_nGetShadowsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetShadows")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetShadows")
internal external fun TextStyle_nGetShadows(ptr: NativePointer, res: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAddShadow")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nAddShadow")
internal external fun TextStyle_nAddShadow(ptr: NativePointer, color: Int, offsetX: Float, offsetY: Float, blurSigma: Double)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nClearShadows")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nClearShadows")
internal external fun TextStyle_nClearShadows(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeatures")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeatures")
internal external fun TextStyle_nGetFontFeatures(ptr: NativePointer, resultIntsArray: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeaturesSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFontFeaturesSize")
internal external fun TextStyle_nGetFontFeaturesSize(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nAddFontFeature")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nAddFontFeature")
internal external fun TextStyle_nAddFontFeature(ptr: NativePointer, name: InteropPointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nClearFontFeatures")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nClearFontFeatures")
internal external fun TextStyle_nClearFontFeatures(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetFontFamilies")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetFontFamilies")
internal external fun TextStyle_nSetFontFamilies(ptr: NativePointer, families: InteropPointer, familiesSize: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetLetterSpacing")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetLetterSpacing")
internal external fun TextStyle_nGetLetterSpacing(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetLetterSpacing")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetLetterSpacing")
internal external fun TextStyle_nSetLetterSpacing(ptr: NativePointer, letterSpacing: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetWordSpacing")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetWordSpacing")
internal external fun TextStyle_nGetWordSpacing(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetWordSpacing")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetWordSpacing")
internal external fun TextStyle_nSetWordSpacing(ptr: NativePointer, wordSpacing: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetTypeface")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetTypeface")
internal external fun TextStyle_nGetTypeface(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetTypeface")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetTypeface")
internal external fun TextStyle_nSetTypeface(ptr: NativePointer, typefacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetLocale")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetLocale")
internal external fun TextStyle_nGetLocale(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetLocale")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetLocale")
internal external fun TextStyle_nSetLocale(ptr: NativePointer, locale: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineMode")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetBaselineMode")
internal external fun TextStyle_nGetBaselineMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineMode")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetBaselineMode")
internal external fun TextStyle_nSetBaselineMode(ptr: NativePointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nGetFontMetrics")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nGetFontMetrics")
internal external fun TextStyle_nGetFontMetrics(ptr: NativePointer, fontMetrics: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nIsPlaceholder")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nIsPlaceholder")
internal external fun TextStyle_nIsPlaceholder(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextStyle__1nSetPlaceholder")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextStyle__1nSetPlaceholder")
internal external fun TextStyle_nSetPlaceholder(ptr: NativePointer)
