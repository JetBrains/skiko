package org.jetbrains.skiko.node

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class RenderNodeContext internal constructor(ptr: NativePointer, managed: Boolean = true) : RefCnt(ptr, managed) {
    private companion object {
        init {
            staticLoad()
        }
    }

    constructor(
        measureDrawBounds: Boolean = false,
    ) : this(RenderNodeContext_nMake(measureDrawBounds)) {
        Stats.onNativeCall()
    }

    fun setLightingInfo(
        centerX: Float = Float.MIN_VALUE,
        centerY: Float = Float.MIN_VALUE,
        centerZ: Float = Float.MIN_VALUE,
        radius: Float = 0f,
        ambientShadowAlpha: Float = 0f,
        spotShadowAlpha: Float = 0f
    ) {
        try {
            Stats.onNativeCall()
            RenderNodeContext_nSetLightingInfo(
                ptr = _ptr,
                centerX = centerX,
                centerY = centerY,
                centerZ = centerZ,
                radius = radius,
                ambientShadowAlpha = ambientShadowAlpha,
                spotShadowAlpha = spotShadowAlpha
            )
        } finally {
            reachabilityBarrier(this)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nMake")
private external fun RenderNodeContext_nMake(measureDrawBounds: Boolean): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nSetLightingInfo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nSetLightingInfo")
private external fun RenderNodeContext_nSetLightingInfo(ptr: NativePointer, centerX: Float, centerY: Float, centerZ: Float, radius: Float, ambientShadowAlpha: Float, spotShadowAlpha: Float)
