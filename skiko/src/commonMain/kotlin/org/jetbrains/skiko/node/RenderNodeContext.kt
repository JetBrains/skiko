package org.jetbrains.skiko.node

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier

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