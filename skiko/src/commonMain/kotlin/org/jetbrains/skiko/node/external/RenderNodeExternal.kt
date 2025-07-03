@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skiko.node

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nMake")
internal external fun RenderNode_nMake(context: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetLayerPaint")
internal external fun RenderNode_nGetLayerPaint(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetLayerPaint")
internal external fun RenderNode_nSetLayerPaint(ptr: NativePointer, paint: NativePointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetBounds")
internal external fun RenderNode_nGetBounds(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetBounds")
internal external fun RenderNode_nSetBounds(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetPivot")
internal external fun RenderNode_nGetPivot(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetPivot")
internal external fun RenderNode_nSetPivot(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetAlpha")
internal external fun RenderNode_nGetAlpha(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetAlpha")
internal external fun RenderNode_nSetAlpha(ptr: NativePointer, alpha: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetScaleX")
internal external fun RenderNode_nGetScaleX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetScaleX")
internal external fun RenderNode_nSetScaleX(ptr: NativePointer, scaleX: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetScaleY")
internal external fun RenderNode_nGetScaleY(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetScaleY")
internal external fun RenderNode_nSetScaleY(ptr: NativePointer, scaleY: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetTranslationX")
internal external fun RenderNode_nGetTranslationX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetTranslationX")
internal external fun RenderNode_nSetTranslationX(ptr: NativePointer, translationX: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetTranslationY")
internal external fun RenderNode_nGetTranslationY(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetTranslationY")
internal external fun RenderNode_nSetTranslationY(ptr: NativePointer, translationY: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetShadowElevation")
internal external fun RenderNode_nGetShadowElevation(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetShadowElevation")
internal external fun RenderNode_nSetShadowElevation(ptr: NativePointer, elevation: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetAmbientShadowColor")
internal external fun RenderNode_nGetAmbientShadowColor(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetAmbientShadowColor")
internal external fun RenderNode_nSetAmbientShadowColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetSpotShadowColor")
internal external fun RenderNode_nGetSpotShadowColor(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetSpotShadowColor")
internal external fun RenderNode_nSetSpotShadowColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationX")
internal external fun RenderNode_nGetRotationX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationX")
internal external fun RenderNode_nSetRotationX(ptr: NativePointer, rotationX: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationY")
internal external fun RenderNode_nGetRotationY(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationY")
internal external fun RenderNode_nSetRotationY(ptr: NativePointer, rotationY: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationZ")
internal external fun RenderNode_nGetRotationZ(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationZ")
internal external fun RenderNode_nSetRotationZ(ptr: NativePointer, rotationZ: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetCameraDistance")
internal external fun RenderNode_nGetCameraDistance(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetCameraDistance")
internal external fun RenderNode_nSetCameraDistance(ptr: NativePointer, distance: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipRect")
internal external fun RenderNode_nSetClipRect(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, mode: Int, antiAlias: Boolean)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipRRect")
internal external fun RenderNode_nSetClipRRect(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, radii: InteropPointer, radiiSize: Int, mode: Int, antiAlias: Boolean)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipPath")
internal external fun RenderNode_nSetClipPath(ptr: NativePointer, pathPtr: NativePointer, mode: Int, antiAlias: Boolean)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetClip")
internal external fun RenderNode_nGetClip(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClip")
internal external fun RenderNode_nSetClip(ptr: NativePointer, clip: Boolean)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nBeginRecording")
internal external fun RenderNode_nBeginRecording(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nEndRecording")
internal external fun RenderNode_nEndRecording(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nDrawInto")
internal external fun RenderNode_nDrawInto(ptr: NativePointer, canvas: NativePointer)
