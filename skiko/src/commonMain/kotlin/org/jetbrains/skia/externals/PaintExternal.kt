@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetFinalizer")
internal external fun Paint_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nMake")
internal external fun Paint_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nMakeClone")
internal external fun Paint_nMakeClone(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nEquals")
internal external fun Paint_nEquals(ptr: NativePointer, otherPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Paint__1nReset")
internal external fun Paint_nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nIsAntiAlias")
internal external fun Paint_nIsAntiAlias(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetAntiAlias")
internal external fun Paint_nSetAntiAlias(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nIsDither")
internal external fun Paint_nIsDither(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetDither")
internal external fun Paint_nSetDither(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetMode")
internal external fun Paint_nGetMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetMode")
internal external fun Paint_nSetMode(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetColor")
internal external fun Paint_nGetColor(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetColor4f")
internal external fun Paint_nGetColor4f(ptr: NativePointer, arr: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetColor")
internal external fun Paint_nSetColor(ptr: NativePointer, argb: Int)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetColor4f")
internal external fun Paint_nSetColor4f(ptr: NativePointer, r: Float, g: Float, b: Float, a: Float, colorSpacePtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetStrokeWidth")
internal external fun Paint_nGetStrokeWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetStrokeWidth")
internal external fun Paint_nSetStrokeWidth(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetStrokeMiter")
internal external fun Paint_nGetStrokeMiter(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetStrokeMiter")
internal external fun Paint_nSetStrokeMiter(ptr: NativePointer, value: Float)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetStrokeCap")
internal external fun Paint_nGetStrokeCap(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetStrokeCap")
internal external fun Paint_nSetStrokeCap(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetStrokeJoin")
internal external fun Paint_nGetStrokeJoin(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetStrokeJoin")
internal external fun Paint_nSetStrokeJoin(ptr: NativePointer, value: Int)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetShader")
internal external fun Paint_nGetShader(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetShader")
internal external fun Paint_nSetShader(ptr: NativePointer, shaderPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetColorFilter")
internal external fun Paint_nGetColorFilter(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetColorFilter")
internal external fun Paint_nSetColorFilter(ptr: NativePointer, colorFilterPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetBlendMode")
internal external fun Paint_nGetBlendMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetBlendMode")
internal external fun Paint_nSetBlendMode(ptr: NativePointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetPathEffect")
internal external fun Paint_nGetPathEffect(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetPathEffect")
internal external fun Paint_nSetPathEffect(ptr: NativePointer, pathEffectPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetMaskFilter")
internal external fun Paint_nGetMaskFilter(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetMaskFilter")
internal external fun Paint_nSetMaskFilter(ptr: NativePointer, filterPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nGetImageFilter")
internal external fun Paint_nGetImageFilter(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Paint__1nSetImageFilter")
internal external fun Paint_nSetImageFilter(ptr: NativePointer, filterPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Paint__1nHasNothingToDraw")
internal external fun Paint_nHasNothingToDraw(ptr: NativePointer): Boolean
