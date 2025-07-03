@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nGetFinalizer")
internal external fun Shaper_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMake")
internal external fun Shaper_nMake(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakePrimitive")
internal external fun Shaper_nMakePrimitive(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShaperDrivenWrapper")
internal external fun Shaper_nMakeShaperDrivenWrapper(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShapeThenWrap")
internal external fun Shaper_nMakeShapeThenWrap(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShapeDontWrapOrReorder")
internal external fun Shaper_nMakeShapeDontWrapOrReorder(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeCoreText")
internal external fun Shaper_nMakeCoreText(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShapeBlob")
internal external fun Shaper_nShapeBlob(
    ptr: NativePointer,
    text: NativePointer,
    fontPtr: NativePointer,
    optsFeaturesLen: Int,
    optsFeaturesIntArray: InteropPointer,
    optsBooleanProps: Int,
    width: Float,
    offsetX: Float,
    offsetY: Float
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShapeLine")
internal external fun Shaper_nShapeLine(
    ptr: NativePointer,
    text: NativePointer,
    fontPtr: NativePointer,
    optsFeaturesLen: Int,
    optsFeatures: InteropPointer,
    optsBooleanProps: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShape")
internal external fun Shaper_nShape(
    ptr: NativePointer,
    textPtr: NativePointer,
    fontIter: InteropPointer,
    bidiIter: InteropPointer,
    scriptIter: InteropPointer,
    langIter: InteropPointer,
    optsFeaturesLen: Int,
    optsFeaturesIntArray: InteropPointer,
    optsBooleanProps: Int,
    width: Float,
    runHandler: InteropPointer
)

// Native/JS only
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunIterator_1nGetFinalizer")
internal external fun RunIterator_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunIterator_1nCreateRunIterator")
internal external fun RunIterator_nCreateRunIterator(type: Int, textPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunIterator_1nInitRunIterator")
internal external fun RunIterator_nInitRunIterator(
    ptr: NativePointer,
    type: Int,
    onConsume: InteropPointer,
    onEndOfCurrentRun: InteropPointer,
    onAtEnd: InteropPointer,
    onCurrent: InteropPointer
)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nCreate")
internal external fun RunHandler_nCreate(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetFinalizer")
internal external fun RunHandler_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nInit")
internal external fun RunHandler_nInit(
    ptr: NativePointer,
    onBeginLine: InteropPointer,
    onRunInfo: InteropPointer,
    onCommitRunInfo: InteropPointer,
    onRunOffset: InteropPointer,
    onCommitRun: InteropPointer,
    onCommitLine: InteropPointer
)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetGlyphs")
internal external fun RunHandler_nGetGlyphs(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetClusters")
internal external fun RunHandler_nGetClusters(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetPositions")
internal external fun RunHandler_nGetPositions(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nSetOffset")
internal external fun RunHandler_nSetOffset(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetRunInfo")
internal external fun RunHandler_nGetRunInfo(ptr: NativePointer, result: InteropPointer): NativePointer
