@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetFinalizer")
internal external fun Path_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nMake")
internal external fun Path_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nEquals")
internal external fun Path_nEquals(aPtr: NativePointer, bPtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nReset")
internal external fun Path_nReset(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsVolatile")
internal external fun Path_nIsVolatile(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nSetVolatile")
internal external fun Path_nSetVolatile(ptr: NativePointer, isVolatile: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Path__1nSwap")
internal external fun Path_nSwap(ptr: NativePointer, otherPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetGenerationId")
internal external fun Path_nGetGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nMakeFromSVGString")
internal external fun Path_nMakeFromSVGString(svg: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsInterpolatable")
internal external fun Path_nIsInterpolatable(ptr: NativePointer, comparePtr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nMakeLerp")
internal external fun Path_nMakeLerp(ptr: NativePointer, endingPtr: NativePointer, weight: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetFillMode")
internal external fun Path_nGetFillMode(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nSetFillMode")
internal external fun Path_nSetFillMode(ptr: NativePointer, fillMode: Int)

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsConvex")
internal external fun Path_nIsConvex(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsOval")
internal external fun Path_nIsOval(ptr: NativePointer, rect: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsRRect")
internal external fun Path_nIsRRect(ptr: NativePointer, rrect: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nRewind")
internal external fun Path_nRewind(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsEmpty")
internal external fun Path_nIsEmpty(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsLastContourClosed")
internal external fun Path_nIsLastContourClosed(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsFinite")
internal external fun Path_nIsFinite(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsLineDegenerate")
internal external fun Path_nIsLineDegenerate(x0: Float, y0: Float, x1: Float, y1: Float, exact: Boolean): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsQuadDegenerate")
internal external fun Path_nIsQuadDegenerate(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    exact: Boolean
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Path__1nIsCubicDegenerate")
internal external fun Path_nIsCubicDegenerate(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    x3: Float,
    y3: Float,
    exact: Boolean
): Boolean


@ExternalSymbolName("org_jetbrains_skia_Path__1nMaybeGetAsLine")
internal external fun Path_nMaybeGetAsLine(ptr: NativePointer, rectBuffer: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetPointsCount")
internal external fun Path_nGetPointsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetPoint")
internal external fun Path_nGetPoint(ptr: NativePointer, index: Int, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetPoints")
internal external fun Path_nGetPoints(ptr: NativePointer, points: InteropPointer, max: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nCountVerbs")
internal external fun Path_nCountVerbs(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetVerbs")
internal external fun Path_nGetVerbs(ptr: NativePointer, verbs: InteropPointer, max: Int): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nApproximateBytesUsed")
internal external fun Path_nApproximateBytesUsed(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetBounds")
internal external fun Path_nGetBounds(ptr: NativePointer, rect: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nUpdateBoundsCache")
internal external fun Path_nUpdateBoundsCache(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nComputeTightBounds")
internal external fun Path_nComputeTightBounds(ptr: NativePointer, rect: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nConservativelyContainsRect")
internal external fun Path_nConservativelyContainsRect(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nIncReserve")
internal external fun Path_nIncReserve(ptr: NativePointer, extraPtCount: Int)

@ExternalSymbolName("org_jetbrains_skia_Path__1nMoveTo")
internal external fun Path_nMoveTo(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nRMoveTo")
internal external fun Path_nRMoveTo(ptr: NativePointer, dx: Float, dy: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nLineTo")
internal external fun Path_nLineTo(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nRLineTo")
internal external fun Path_nRLineTo(ptr: NativePointer, dx: Float, dy: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nQuadTo")
internal external fun Path_nQuadTo(ptr: NativePointer, x1: Float, y1: Float, x2: Float, y2: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nRQuadTo")
internal external fun Path_nRQuadTo(ptr: NativePointer, dx1: Float, dy1: Float, dx2: Float, dy2: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nConicTo")
internal external fun Path_nConicTo(ptr: NativePointer, x1: Float, y1: Float, x2: Float, y2: Float, w: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nRConicTo")
internal external fun Path_nRConicTo(ptr: NativePointer, dx1: Float, dy1: Float, dx2: Float, dy2: Float, w: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nCubicTo")
internal external fun Path_nCubicTo(ptr: NativePointer, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nRCubicTo")
internal external fun Path_nRCubicTo(ptr: NativePointer, dx1: Float, dy1: Float, dx2: Float, dy2: Float, dx3: Float, dy3: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nArcTo")
internal external fun Path_nArcTo(
    ptr: NativePointer,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    startAngle: Float,
    sweepAngle: Float,
    forceMoveTo: Boolean
)


@ExternalSymbolName("org_jetbrains_skia_Path__1nTangentArcTo")
internal external fun Path_nTangentArcTo(ptr: NativePointer, x1: Float, y1: Float, x2: Float, y2: Float, radius: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nEllipticalArcTo")
internal external fun Path_nEllipticalArcTo(
    ptr: NativePointer,
    rx: Float,
    ry: Float,
    xAxisRotate: Float,
    size: Int,
    direction: Int,
    x: Float,
    y: Float
)


@ExternalSymbolName("org_jetbrains_skia_Path__1nREllipticalArcTo")
internal external fun Path_nREllipticalArcTo(
    ptr: NativePointer,
    rx: Float,
    ry: Float,
    xAxisRotate: Float,
    size: Int,
    direction: Int,
    dx: Float,
    dy: Float
)


@ExternalSymbolName("org_jetbrains_skia_Path__1nClosePath")
internal external fun Path_nClosePath(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nConvertConicToQuads")
internal external fun Path_nConvertConicToQuads(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    w: Float,
    pow2: Int,
    result: InteropPointer
): Int


@ExternalSymbolName("org_jetbrains_skia_Path__1nIsRect")
internal external fun Path_nIsRect(ptr: NativePointer, rect: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddRect")
internal external fun Path_nAddRect(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float, dir: Int, start: Int)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddOval")
internal external fun Path_nAddOval(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float, dir: Int, start: Int)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddCircle")
internal external fun Path_nAddCircle(ptr: NativePointer, x: Float, y: Float, r: Float, dir: Int)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddArc")
internal external fun Path_nAddArc(ptr: NativePointer, l: Float, t: Float, r: Float, b: Float, startAngle: Float, sweepAngle: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddRRect")
internal external fun Path_nAddRRect(
    ptr: NativePointer,
    l: Float,
    t: Float,
    r: Float,
    b: Float,
    radii: InteropPointer,
    size: Int,
    dir: Int,
    start: Int
)


@ExternalSymbolName("org_jetbrains_skia_Path__1nAddPoly")
internal external fun Path_nAddPoly(ptr: NativePointer, coords: InteropPointer, count: Int, close: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddPath")
internal external fun Path_nAddPath(ptr: NativePointer, srcPtr: NativePointer, extend: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddPathOffset")
internal external fun Path_nAddPathOffset(ptr: NativePointer, srcPtr: NativePointer, dx: Float, dy: Float, extend: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Path__1nAddPathTransform")
internal external fun Path_nAddPathTransform(ptr: NativePointer, srcPtr: NativePointer, matrix: InteropPointer, extend: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Path__1nReverseAddPath")
internal external fun Path_nReverseAddPath(ptr: NativePointer, srcPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nOffset")
internal external fun Path_nOffset(ptr: NativePointer, dx: Float, dy: Float, dst: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nTransform")
internal external fun Path_nTransform(ptr: NativePointer, matrix: InteropPointer, dst: NativePointer, applyPerspectiveClip: Boolean)

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetLastPt")
internal external fun Path_nGetLastPt(ptr: NativePointer, result: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nSetLastPt")
internal external fun Path_nSetLastPt(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_Path__1nGetSegmentMasks")
internal external fun Path_nGetSegmentMasks(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nContains")
internal external fun Path_nContains(ptr: NativePointer, x: Float, y: Float): Boolean

@ExternalSymbolName("org_jetbrains_skia_Path__1nDump")
internal external fun Path_nDump(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nDumpHex")
internal external fun Path_nDumpHex(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_Path__1nSerializeToBytes")
internal external fun Path_nSerializeToBytes(ptr: NativePointer, dst: InteropPointer): Int

@ExternalSymbolName("org_jetbrains_skia_Path__1nMakeCombining")
internal external fun Path_nMakeCombining(onePtr: NativePointer, twoPtr: NativePointer, op: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nMakeFromBytes")
internal external fun Path_nMakeFromBytes(data: InteropPointer, size: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Path__1nIsValid")
internal external fun Path_nIsValid(ptr: NativePointer): Boolean
