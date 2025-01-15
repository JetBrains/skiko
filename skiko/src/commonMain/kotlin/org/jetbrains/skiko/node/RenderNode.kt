package org.jetbrains.skiko.node

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.reachabilityBarrier

class RenderNode internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    private companion object {
        init {
            staticLoad()
        }
    }

    private object _FinalizerHolder {
        val PTR = RenderNode_nGetFinalizer()
    }

    constructor() : this(RenderNode_nMake()) {
        Stats.onNativeCall()
    }

    var matrix: Matrix44
        get() = try {
            Stats.onNativeCall()
            Matrix44.fromInteropPointer { _nGetMatrix(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) {
            Stats.onNativeCall()
            interopScope { _nSetMatrix(_ptr, toInterop(value.mat)) }
        }

    fun beginRecording(): Canvas {
        return try {
            Stats.onNativeCall()
            Canvas(
                ptr = _nBeginRecording(_ptr),
                managed = false,
                _owner = this
            )
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun endRecording() {
        try {
            Stats.onNativeCall()
            _nEndRecording(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

//    setUseCompositingLayer(true, layerPaint)
//    setHasOverlappingRendering(true)
//    setOutline
//    setRenderEffect
//    setClipToBounds
//    setClipToOutline
}


@ExternalSymbolName("org_jetbrains_skia_node_RenderNode__nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_node_RenderNode__nMake")
private external fun RenderNode_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_node_RenderNode__nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_node_RenderNode__nGetFinalizer")
private external fun RenderNode_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_node_RenderNode__nGetMatrix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_node_RenderNode__nGetMatrix")
private external fun _nGetMatrix(ptr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_node_RenderNode__nSetMatrix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_node_RenderNode__nSetMatrix")
private external fun _nSetMatrix(ptr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_node_RenderNode__nBeginRecording")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_node_RenderNode__nBeginRecording")
private external fun _nBeginRecording(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_node_RenderNode__nEndRecording")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_node_RenderNode__nEndRecording")
private external fun _nEndRecording(ptr: NativePointer)
