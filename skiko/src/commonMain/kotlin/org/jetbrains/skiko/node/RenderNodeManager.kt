package org.jetbrains.skiko.node

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

class RenderNodeManager internal constructor(ptr: NativePointer) : Managed(ptr, FinalizerPointer) {
    private companion object {
        private val FinalizerPointer: NativePointer
        init {
            staticLoad()
            FinalizerPointer = RenderNodeManager_nGetFinalizer()
        }
    }

    constructor(
        measureDrawBounds: Boolean = false,
    ) : this(RenderNodeManager_nMake(measureDrawBounds)) {
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
            RenderNodeManager_nSetLightingInfo(
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

    fun drawIntoCanvas(canvas: Canvas, block: (Canvas) -> Unit) {
        val renderNodeCanvas = createRenderNodeCanvas(canvas)
        try {
            block(renderNodeCanvas)
        } finally {
            renderNodeCanvas.close()
        }
    }

    private fun createRenderNodeCanvas(canvas: Canvas): Canvas {
        return try {
            Stats.onNativeCall()
            Canvas(
                ptr = RenderNodeManager_nCreateRenderNodeCanvas(_ptr, getPtr(canvas)),
                managed = true,
                _owner = this
            )
        } finally {
            reachabilityBarrier(this)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_1nMake")
private external fun RenderNodeManager_nMake(measureDrawBounds: Boolean): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_1nGetFinalizer")
private external fun RenderNodeManager_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_1nSetLightingInfo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_1nSetLightingInfo")
private external fun RenderNodeManager_nSetLightingInfo(ptr: NativePointer, centerX: Float, centerY: Float, centerZ: Float, radius: Float, ambientShadowAlpha: Float, spotShadowAlpha: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeManagerKt_RenderNodeManager_1nCreateRenderNodeCanvas")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeManager_RenderNodeManager_1nCreateRenderNodeCanvas")
private external fun RenderNodeManager_nCreateRenderNodeCanvas(ptr: NativePointer, canvas: NativePointer): NativePointer
