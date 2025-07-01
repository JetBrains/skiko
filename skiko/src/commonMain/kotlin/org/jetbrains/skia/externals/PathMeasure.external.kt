package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathMeasure__1nGetFinalizer")
internal external fun PathMeasure_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathMeasure__1nMake")
internal external fun PathMeasure_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nMakePath")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathMeasure__1nMakePath")
internal external fun PathMeasure_nMakePath(pathPtr: NativePointer, forceClosed: Boolean, resScale: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nSetPath")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathMeasure__1nSetPath")
internal external fun PathMeasure_nSetPath(ptr: NativePointer, pathPtr: NativePointer, forceClosed: Boolean)

@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetLength")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathMeasure__1nGetLength")
internal external fun PathMeasure_nGetLength(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetPosition")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathMeasure__1nGetPosition")
internal external fun PathMeasure_nGetPosition(ptr: NativePointer, distance: Float, data: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetTangent")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathMeasure__1nGetTangent")
internal external fun PathMeasure_nGetTangent(ptr: NativePointer, distance: Float, data: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetRSXform")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathMeasure__1nGetRSXform")
internal external fun PathMeasure_nGetRSXform(ptr: NativePointer, distance: Float, data: InteropPointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetMatrix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathMeasure__1nGetMatrix")
internal external fun PathMeasure_nGetMatrix(
    ptr: NativePointer,
    distance: Float,
    getPosition: Boolean,
    getTangent: Boolean,
    data: InteropPointer
): Boolean

@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nGetSegment")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathMeasure__1nGetSegment")
internal external fun PathMeasure_nGetSegment(
    ptr: NativePointer,
    startD: Float,
    endD: Float,
    dstPtr: NativePointer,
    startWithMoveTo: Boolean
): Boolean


@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nIsClosed")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathMeasure__1nIsClosed")
internal external fun PathMeasure_nIsClosed(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_PathMeasure__1nNextContour")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathMeasure__1nNextContour")
internal external fun PathMeasure_nNextContour(ptr: NativePointer): Boolean
