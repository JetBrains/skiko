@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skiko.node

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nMake")
internal external fun RenderNodeContext_nMake(measureDrawBounds: Boolean): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nSetLightingInfo")
internal external fun RenderNodeContext_nSetLightingInfo(ptr: NativePointer, centerX: Float, centerY: Float, centerZ: Float, radius: Float, ambientShadowAlpha: Float, spotShadowAlpha: Float)
