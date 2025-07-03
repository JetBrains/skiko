@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nGetFinalizer")
internal external fun Drawable_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nMake")
internal external fun Drawable_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nGetGenerationId")
internal external fun Drawable_nGetGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nDraw")
internal external fun Drawable_nDraw(ptr: NativePointer, canvasPtr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nMakePictureSnapshot")
internal external fun Drawable_nMakePictureSnapshot(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nNotifyDrawingChanged")
internal external fun Drawable_nNotifyDrawingChanged(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nGetBounds")
internal external fun Drawable_nGetBounds(ptr: NativePointer, result: InteropPointer)

// For Native/JS usage only

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nInit")
internal external fun Drawable_nInit(ptr: NativePointer, onGetBounds: InteropPointer, onDraw: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nGetOnDrawCanvas")
internal external fun Drawable_nGetOnDrawCanvas(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Drawable__1nSetBounds")
internal external fun Drawable_nSetBounds(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float)
