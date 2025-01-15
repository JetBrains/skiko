package org.jetbrains.skiko.node

class RenderNodeManager {
    fun setLightingInfo(
        centerX: Float = Float.MIN_VALUE,
        centerY: Float = Float.MIN_VALUE,
        centerZ: Float = Float.MIN_VALUE,
        radius: Float = 0f,
        ambientShadowAlpha: Float = 0f,
        spotShadowAlpha: Float = 0f
    ) {
    }

    fun createRenderNode() = RenderNode()
    fun releaseRenderNode(renderNode: RenderNode) {}
}
