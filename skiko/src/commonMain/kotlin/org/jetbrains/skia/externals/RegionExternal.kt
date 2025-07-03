@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nMake")
internal external fun Region_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetFinalizer")
internal external fun Region_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsEmpty")
internal external fun Region_nIsEmpty(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsRect")
internal external fun Region_nIsRect(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetBounds")
internal external fun Region_nGetBounds(ptr: NativePointer, ltrb: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Region__1nSet")
internal external fun Region_nSet(ptr: NativePointer, regoinPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIsComplex")
internal external fun Region_nIsComplex(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nComputeRegionComplexity")
internal external fun Region_nComputeRegionComplexity(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Region__1nGetBoundaryPath")
internal external fun Region_nGetBoundaryPath(ptr: NativePointer, pathPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetEmpty")
internal external fun Region_nSetEmpty(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRect")
internal external fun Region_nSetRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRects")
internal external fun Region_nSetRects(ptr: NativePointer, rects: InteropPointer, count: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetRegion")
internal external fun Region_nSetRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nSetPath")
internal external fun Region_nSetPath(ptr: NativePointer, pathPtr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIntersectsIRect")
internal external fun Region_nIntersectsIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nIntersectsRegion")
internal external fun Region_nIntersectsRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsIPoint")
internal external fun Region_nContainsIPoint(ptr: NativePointer, x: Int, y: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsIRect")
internal external fun Region_nContainsIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nContainsRegion")
internal external fun Region_nContainsRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickContains")
internal external fun Region_nQuickContains(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickRejectIRect")
internal external fun Region_nQuickRejectIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nQuickRejectRegion")
internal external fun Region_nQuickRejectRegion(ptr: NativePointer, regionPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nTranslate")
internal external fun Region_nTranslate(ptr: NativePointer, dx: Int, dy: Int)

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpIRect")
internal external fun Region_nOpIRect(ptr: NativePointer, left: Int, top: Int, right: Int, bottom: Int, op: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegion")
internal external fun Region_nOpRegion(ptr: NativePointer, regionPtr: NativePointer, op: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_Region__1nOpIRectRegion")
internal external fun Region_nOpIRectRegion(
    ptr: NativePointer,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    regionPtr: NativePointer,
    op: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegionIRect")
internal external fun Region_nOpRegionIRect(
    ptr: NativePointer,
    regionPtr: NativePointer,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    op: Int
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Region__1nOpRegionRegion")
internal external fun Region_nOpRegionRegion(
    ptr: NativePointer,
    regionPtrA: NativePointer,
    regionPtrB: NativePointer,
    op: Int
): Boolean
