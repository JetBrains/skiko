package org.jetbrains.skiko.node

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

class RenderNodeManager internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    private companion object {
        init {
            staticLoad()
        }
    }

    private object _FinalizerHolder {
        val PTR = RenderNodeManager_nGetFinalizer()
    }

    constructor() : this(RenderNodeManager_nMake()) {
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
            _nSetLightingInfo(_ptr, centerX, centerY, centerZ, radius, ambientShadowAlpha, spotShadowAlpha)
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
                ptr = _nCreateRenderNodeCanvas(_ptr, getPtr(canvas)),
                managed = true,
                _owner = this
            )
        } finally {
            reachabilityBarrier(this)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeManager__nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeManager__nMake")
private external fun RenderNodeManager_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeManager__nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeManager__nGetFinalizer")
private external fun RenderNodeManager_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeManager__nSetLightingInfo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeManager__nSetLightingInfo")
private external fun _nSetLightingInfo(ptr: NativePointer, centerX: Float, centerY: Float, centerZ: Float, radius: Float, ambientShadowAlpha: Float, spotShadowAlpha: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeManager__nCreateRenderNodeCanvas")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeManager__nCreateRenderNodeCanvas")
private external fun _nCreateRenderNodeCanvas(ptr: NativePointer, canvas: NativePointer): NativePointer
