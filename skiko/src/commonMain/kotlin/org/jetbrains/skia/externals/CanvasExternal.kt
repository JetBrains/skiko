@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nGetFinalizer")
internal external fun Canvas_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nMakeFromBitmap")
internal external fun Canvas_nMakeFromBitmap(bitmapPtr: NativePointer, flags: Int, pixelGeometry: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPoint")
internal external fun Canvas_nDrawPoint(ptr: NativePointer, x: Float, y: Float, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPoints")
internal external fun Canvas_nDrawPoints(ptr: NativePointer, mode: Int, coordsCount: Int, coords: InteropPointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawLine")
internal external fun Canvas_nDrawLine(ptr: NativePointer, x0: Float, y0: Float, x1: Float, y1: Float, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawArc")
internal external fun Canvas_nDrawArc(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    startAngle: Float,
    sweepAngle: Float,
    includeCenter: Boolean,
    paintPtr: NativePointer
)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawRect")
internal external fun Canvas_nDrawRect(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawOval")
internal external fun Canvas_nDrawOval(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, paint: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawRRect")
internal external fun Canvas_nDrawRRect(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radii: InteropPointer,
    radiiSize: Int,
    paintPtr: NativePointer
)


@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawDRRect")
internal external fun Canvas_nDrawDRRect(
    ptr: NativePointer,
    ol: Float,
    ot: Float,
    or: Float,
    ob: Float,
    oradii: InteropPointer,
    oradiiSize: Int,
    il: Float,
    it: Float,
    ir: Float,
    ib: Float,
    iradii: InteropPointer,
    iradiiSize: Int,
    paintPtr: NativePointer
)


@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPath")
internal external fun Canvas_nDrawPath(ptr: NativePointer, nativePath: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawImageRect")
internal external fun Canvas_nDrawImageRect(
    ptr: NativePointer,
    nativeImage: NativePointer,
    sl: Float,
    st: Float,
    sr: Float,
    sb: Float,
    dl: Float,
    dt: Float,
    dr: Float,
    db: Float,
    samplingModeVal1: Int,
    samplingModeVal2: Int,
    paintPtr: NativePointer,
    strict: Boolean
)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawImageNine")
internal external fun Canvas_nDrawImageNine(
    ptr: NativePointer,
    nativeImage: NativePointer,
    cl: Int,
    ct: Int,
    cr: Int,
    cb: Int,
    dl: Float,
    dt: Float,
    dr: Float,
    db: Float,
    filterMode: Int,
    paintPtr: NativePointer
)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawRegion")
internal external fun Canvas_nDrawRegion(ptr: NativePointer, nativeRegion: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawString")
internal external fun Canvas_nDrawString(ptr: NativePointer, string: InteropPointer, x: Float, y: Float, font: NativePointer, paint: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawTextBlob")
internal external fun Canvas_nDrawTextBlob(ptr: NativePointer, blob: NativePointer, x: Float, y: Float, paint: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPicture")
internal external fun Canvas_nDrawPicture(ptr: NativePointer, picturePtr: NativePointer, matrix: InteropPointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawVertices")
internal external fun Canvas_nDrawVertices(
    ptr: NativePointer,
    verticesMode: Int,
    vertexCount: Int,
    cubics: InteropPointer,
    colors: InteropPointer,
    texCoords: InteropPointer,
    indexCount: Int,
    indices: InteropPointer,
    blendMode: Int,
    paintPtr: NativePointer
)


@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPatch")
internal external fun Canvas_nDrawPatch(
    ptr: NativePointer,
    cubics: InteropPointer,
    colors: InteropPointer,
    texCoords: InteropPointer,
    blendMode: Int,
    paintPtr: NativePointer
)


@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawDrawable")
internal external fun Canvas_nDrawDrawable(ptr: NativePointer, drawablePrt: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClear")
internal external fun Canvas_nClear(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nDrawPaint")
internal external fun Canvas_nDrawPaint(ptr: NativePointer, paintPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSetMatrix")
internal external fun Canvas_nSetMatrix(ptr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nGetLocalToDevice")
internal external fun Canvas_nGetLocalToDevice(ptr: NativePointer, resultFloats: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nResetMatrix")
internal external fun Canvas_nResetMatrix(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClipRect")
internal external fun Canvas_nClipRect(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    mode: Int,
    antiAlias: Boolean
)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClipRRect")
internal external fun Canvas_nClipRRect(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    radii: InteropPointer,
    size: Int,
    mode: Int,
    antiAlias: Boolean
)


@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClipPath")
internal external fun Canvas_nClipPath(ptr: NativePointer, nativePath: NativePointer, mode: Int, antiAlias: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nClipRegion")
internal external fun Canvas_nClipRegion(ptr: NativePointer, nativeRegion: NativePointer, mode: Int)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nTranslate")
internal external fun Canvas_nTranslate(ptr: NativePointer, dx: Float, dy: Float)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nScale")
internal external fun Canvas_nScale(ptr: NativePointer, sx: Float, sy: Float)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nRotate")
internal external fun Canvas_nRotate(ptr: NativePointer, deg: Float, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSkew")
internal external fun Canvas_nSkew(ptr: NativePointer, sx: Float, sy: Float)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nConcat")
internal external fun Canvas_nConcat(ptr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nConcat44")
internal external fun Canvas_nConcat44(ptr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nReadPixels")
internal external fun Canvas_nReadPixels(ptr: NativePointer, bitmapPtr: NativePointer, srcX: Int, srcY: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nWritePixels")
internal external fun Canvas_nWritePixels(ptr: NativePointer, bitmapPtr: NativePointer, x: Int, y: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSave")
internal external fun Canvas_nSave(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSaveLayer")
internal external fun Canvas_nSaveLayer(ptr: NativePointer, paintPtr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSaveLayerRect")
internal external fun Canvas_nSaveLayerRect(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    paintPtr: NativePointer
): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSaveLayerSaveLayerRec")
internal external fun Canvas_nSaveLayerSaveLayerRec(
    ptr: NativePointer,
    paintPtr: NativePointer,
    backdropImageFilterPtr: NativePointer,
    colorSpacePtr: NativePointer,
    saveLayerFlags: Int
): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nSaveLayerSaveLayerRecRect")
internal external fun Canvas_nSaveLayerSaveLayerRecRect(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    paintPtr: NativePointer,
    backdropImageFilterPtr: NativePointer,
    colorSpacePtr: NativePointer,
    saveLayerFlags: Int
): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nGetSaveCount")
internal external fun Canvas_nGetSaveCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nRestore")
internal external fun Canvas_nRestore(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Canvas__1nRestoreToCount")
internal external fun Canvas_nRestoreToCount(ptr: NativePointer, saveCount: Int)
