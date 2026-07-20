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

    /**
     * @param measureDrawBounds whether the nodes created with this context measure the area their
     * content actually covers as they record it. Without it a node whose content is not confined to
     * its own bounds -- one that does not clip, or that casts a shadow -- reports a conservative
     * area instead, and whatever records that node widens its cull rect to match. It costs a
     * bounding hierarchy built over every recording.
     * @param snapshotCache whether the nodes created with this context record their content into an
     * immutable picture once and replay it until the content changes, instead of re-recording it for
     * every frame. It trades memory for recording time, so it pays off for trees that are drawn far
     * more often than they change, and costs for trees that are re-recorded every frame. A node that
     * casts a shadow, and every node drawing it, replays its content directly either way, because
     * the shadow it draws depends on where it is replayed.
     *
     * Snapshotting is a property of the whole tree rather than of a single node: a snapshot inlines
     * the drawing of the nodes below it, so those nodes have to report their changes upwards for it
     * to be dropped in time.
     */
    constructor(
        measureDrawBounds: Boolean = false,
        snapshotCache: Boolean = false,
    ) : this(RenderNodeContext_nMake(measureDrawBounds, snapshotCache)) {
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
private external fun RenderNodeContext_nMake(measureDrawBounds: Boolean, snapshotCache: Boolean): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeContextKt_RenderNodeContext_1nSetLightingInfo")
private external fun RenderNodeContext_nSetLightingInfo(ptr: NativePointer, centerX: Float, centerY: Float, centerZ: Float, radius: Float, ambientShadowAlpha: Float, spotShadowAlpha: Float)
