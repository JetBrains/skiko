package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nGetFinalizer")
internal external fun StrutStyle_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nMake")
internal external fun StrutStyle_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nEquals")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nEquals")
internal external fun StrutStyle_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nGetHeight")
internal external fun StrutStyle_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nSetHeight")
internal external fun StrutStyle_nSetHeight(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetEnabled")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nSetEnabled")
internal external fun StrutStyle_nSetEnabled(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nGetFontFamilies")
internal external fun StrutStyle_nGetFontFamilies(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nSetFontFamilies")
internal external fun StrutStyle_nSetFontFamilies(ptr: NativePointer, families: InteropPointer, familiesCount: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nGetFontStyle")
internal external fun StrutStyle_nGetFontStyle(ptr: NativePointer, fontStyleData: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nSetFontStyle")
internal external fun _nSetFontStyle(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetFontSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nGetFontSize")
internal external fun StrutStyle_nGetFontSize(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetFontSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nSetFontSize")
internal external fun StrutStyle_nSetFontSize(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetLeading")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nGetLeading")
internal external fun StrutStyle_nGetLeading(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetLeading")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nSetLeading")
internal external fun StrutStyle_nSetLeading(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nIsEnabled")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nIsEnabled")
internal external fun StrutStyle_nIsEnabled(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightForced")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightForced")
internal external fun StrutStyle_nIsHeightForced(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightForced")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightForced")
internal external fun StrutStyle_nSetHeightForced(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightOverridden")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nIsHeightOverridden")
internal external fun StrutStyle_nIsHeightOverridden(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightOverridden")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nSetHeightOverridden")
internal external fun StrutStyle_nSetHeightOverridden(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nIsHalfLeading")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nIsHalfLeading")
internal external fun StrutStyle_nIsHalfLeading(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetHalfLeading")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nSetHalfLeading")
internal external fun StrutStyle_nSetHalfLeading(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nGetTopRatio")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nGetTopRatio")
internal external fun StrutStyle_nGetTopRatio(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_StrutStyle__1nSetTopRatio")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_StrutStyle__1nSetTopRatio")
internal external fun StrutStyle_nSetTopRatio(ptr: NativePointer, value: Float)
