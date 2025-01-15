package org.jetbrains.skiko.node

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.reachabilityBarrier

/**
 * <p>RenderNode is used to build hardware accelerated rendering hierarchies. Each RenderNode
 * contains both a display list as well as a set of properties that affect the rendering of the
 * display list. RenderNodes are used internally for all Views by default and are not typically
 * used directly.</p>
 *
 * <p>RenderNodes are used to divide up the rendering content of a complex scene into smaller
 * pieces that can then be updated individually more cheaply. Updating part of the scene only needs
 * to update the display list or properties of a small number of RenderNode instead of redrawing
 * everything from scratch. A RenderNode only needs its display list re-recorded when its content
 * alone should be changed. RenderNodes can also be transformed without re-recording the display
 * list through the transform properties.</p>
 */
class RenderNode internal constructor(ptr: NativePointer) : Managed(ptr, FinalizerPointer) {
    private companion object {
        private val FinalizerPointer: NativePointer
        init {
            staticLoad()
            FinalizerPointer = RenderNode_nGetFinalizer()
        }
    }

    constructor(manager: RenderNodeManager) : this(RenderNode_nMake(getPtr(manager))) {
        Stats.onNativeCall()
    }

    var position: Point
        get() = try {
            Stats.onNativeCall()
            Point.fromInteropPointer { RenderNode_nGetPosition(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetPosition(_ptr, value.x, value.y)
        } finally {
            reachabilityBarrier(this)
        }

    var size: Point
        get() = try {
            Stats.onNativeCall()
            Point.fromInteropPointer { RenderNode_nGetSize(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetSize(_ptr, value.x, value.y)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Gets the current transform matrix.
     */
    val matrix: Matrix44
        get() = try {
            Stats.onNativeCall()
            Matrix44.fromInteropPointer { RenderNode_nGetMatrix(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Starts recording a display list for the render node. All
     * operations performed on the returned canvas are recorded and
     * stored in this display list.
     *
     * [endRecording] must be called when the recording is finished in order to apply
     * the updated display list.
     *
     */
    fun beginRecording(): Canvas {
        return try {
            Stats.onNativeCall()
            Canvas(
                ptr = RenderNode_nBeginRecording(_ptr),
                managed = false,
                _owner = this
            )
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Ends the recording for this display list.
     */
    fun endRecording() {
        try {
            Stats.onNativeCall()
            RenderNode_nEndRecording(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun drawInto(canvas: Canvas) {
        try {
            Stats.onNativeCall()
            RenderNode_nDrawInto(_ptr, getPtr(canvas))
        } finally {
            reachabilityBarrier(this)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nMake")
private external fun RenderNode_nMake(manager: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNode_RenderNode_1nGetFinalizer")
private external fun RenderNode_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetPosition")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetPosition")
private external fun RenderNode_nGetPosition(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetPosition")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetPosition")
private external fun RenderNode_nSetPosition(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetSize")
private external fun RenderNode_nGetSize(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetSize")
private external fun RenderNode_nSetSize(ptr: NativePointer, width: Float, height: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetMatrix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetMatrix")
private external fun RenderNode_nGetMatrix(ptr: NativePointer, matrix: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nBeginRecording")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nBeginRecording")
private external fun RenderNode_nBeginRecording(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nEndRecording")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nEndRecording")
private external fun RenderNode_nEndRecording(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nDrawInto")
@ModuleImport("./skiko.mjs", "org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nDrawInto")
private external fun RenderNode_nDrawInto(ptr: NativePointer, canvas: NativePointer)
