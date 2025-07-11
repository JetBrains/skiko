package org.jetbrains.skiko.node

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

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
class RenderNode internal constructor(ptr: NativePointer, managed: Boolean = true) : RefCnt(ptr, managed) {
    private companion object {
        init {
            staticLoad()
        }
    }

    constructor(context: RenderNodeContext) : this(RenderNode_nMake(getPtr(context))) {
        Stats.onNativeCall()
    }

    var layerPaint: Paint?
        get() = try {
            Stats.onNativeCall()
            val ptr = RenderNode_nGetLayerPaint(_ptr)
            if (ptr == NullPointer) null else Paint(ptr, false)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetLayerPaint(_ptr, getPtr(value))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(value)
        }


    var bounds: Rect
        get() = try {
            Stats.onNativeCall()
            Rect.fromInteropPointer { RenderNode_nGetBounds(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetBounds(_ptr, value.left, value.top, value.right, value.bottom)
        } finally {
            reachabilityBarrier(this)
        }

    var pivot: Point
        get() = try {
            Stats.onNativeCall()
            Point.fromInteropPointer { RenderNode_nGetPivot(_ptr, it) }
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetPivot(_ptr, value.x, value.y)
        } finally {
            reachabilityBarrier(this)
        }

    var alpha: Float
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetAlpha(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetAlpha(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var scaleX: Float
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetScaleX(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetScaleX(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var scaleY: Float
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetScaleY(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetScaleY(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var translationX: Float
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetTranslationX(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetTranslationX(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var translationY: Float
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetTranslationY(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetTranslationY(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var shadowElevation: Float
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetShadowElevation(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetShadowElevation(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var ambientShadowColor: Int
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetAmbientShadowColor(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetAmbientShadowColor(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var spotShadowColor: Int
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetSpotShadowColor(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetSpotShadowColor(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var rotationX: Float
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetRotationX(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetRotationX(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var rotationY: Float
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetRotationY(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetRotationY(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var rotationZ: Float
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetRotationZ(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetRotationZ(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    var cameraDistance: Float
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetCameraDistance(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetCameraDistance(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

    fun setClipRect(r: Rect, mode: ClipMode = ClipMode.INTERSECT, antiAlias: Boolean = false) {
        Stats.onNativeCall()
        RenderNode_nSetClipRect(
            ptr = _ptr,
            left = r.left,
            top = r.top,
            right = r.right,
            bottom = r.bottom,
            mode = mode.ordinal,
            antiAlias = antiAlias
        )
    }

    fun setClipRRect(r: RRect, mode: ClipMode = ClipMode.INTERSECT, antiAlias: Boolean = false) {
        Stats.onNativeCall()
        interopScope {
            RenderNode_nSetClipRRect(
                ptr = _ptr,
                left = r.left,
                top = r.top,
                right = r.right,
                bottom = r.bottom,
                radii = toInterop(r.radii),
                radiiSize = r.radii.size,
                mode = mode.ordinal,
                antiAlias = antiAlias
            )
        }
    }

    fun setClipPath(p: Path?, mode: ClipMode = ClipMode.INTERSECT, antiAlias: Boolean = false) {
        try {
            Stats.onNativeCall()
            RenderNode_nSetClipPath(
                ptr = _ptr,
                pathPtr = getPtr(p),
                mode = mode.ordinal,
                antiAlias = antiAlias
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(p)
        }
    }

    var clip: Boolean
        get() = try {
            Stats.onNativeCall()
            RenderNode_nGetClip(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
        set(value) = try {
            Stats.onNativeCall()
            RenderNode_nSetClip(_ptr, value)
        } finally {
            reachabilityBarrier(this)
        }

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
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nMake")
private external fun RenderNode_nMake(context: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetLayerPaint")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetLayerPaint")
private external fun RenderNode_nGetLayerPaint(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetLayerPaint")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetLayerPaint")
private external fun RenderNode_nSetLayerPaint(ptr: NativePointer, paint: NativePointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetBounds")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetBounds")
private external fun RenderNode_nGetBounds(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetBounds")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetBounds")
private external fun RenderNode_nSetBounds(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetPivot")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetPivot")
private external fun RenderNode_nGetPivot(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetPivot")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetPivot")
private external fun RenderNode_nSetPivot(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetAlpha")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetAlpha")
private external fun RenderNode_nGetAlpha(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetAlpha")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetAlpha")
private external fun RenderNode_nSetAlpha(ptr: NativePointer, alpha: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetScaleX")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetScaleX")
private external fun RenderNode_nGetScaleX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetScaleX")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetScaleX")
private external fun RenderNode_nSetScaleX(ptr: NativePointer, scaleX: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetScaleY")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetScaleY")
private external fun RenderNode_nGetScaleY(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetScaleY")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetScaleY")
private external fun RenderNode_nSetScaleY(ptr: NativePointer, scaleY: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetTranslationX")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetTranslationX")
private external fun RenderNode_nGetTranslationX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetTranslationX")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetTranslationX")
private external fun RenderNode_nSetTranslationX(ptr: NativePointer, translationX: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetTranslationY")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetTranslationY")
private external fun RenderNode_nGetTranslationY(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetTranslationY")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetTranslationY")
private external fun RenderNode_nSetTranslationY(ptr: NativePointer, translationY: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetShadowElevation")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetShadowElevation")
private external fun RenderNode_nGetShadowElevation(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetShadowElevation")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetShadowElevation")
private external fun RenderNode_nSetShadowElevation(ptr: NativePointer, elevation: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetAmbientShadowColor")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetAmbientShadowColor")
private external fun RenderNode_nGetAmbientShadowColor(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetAmbientShadowColor")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetAmbientShadowColor")
private external fun RenderNode_nSetAmbientShadowColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetSpotShadowColor")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetSpotShadowColor")
private external fun RenderNode_nGetSpotShadowColor(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetSpotShadowColor")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetSpotShadowColor")
private external fun RenderNode_nSetSpotShadowColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationX")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationX")
private external fun RenderNode_nGetRotationX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationX")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationX")
private external fun RenderNode_nSetRotationX(ptr: NativePointer, rotationX: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationY")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationY")
private external fun RenderNode_nGetRotationY(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationY")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationY")
private external fun RenderNode_nSetRotationY(ptr: NativePointer, rotationY: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationZ")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationZ")
private external fun RenderNode_nGetRotationZ(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationZ")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationZ")
private external fun RenderNode_nSetRotationZ(ptr: NativePointer, rotationZ: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetCameraDistance")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetCameraDistance")
private external fun RenderNode_nGetCameraDistance(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetCameraDistance")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetCameraDistance")
private external fun RenderNode_nSetCameraDistance(ptr: NativePointer, distance: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipRect")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipRect")
private external fun RenderNode_nSetClipRect(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, mode: Int, antiAlias: Boolean)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipRRect")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipRRect")
private external fun RenderNode_nSetClipRRect(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, radii: InteropPointer, radiiSize: Int, mode: Int, antiAlias: Boolean)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipPath")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipPath")
private external fun RenderNode_nSetClipPath(ptr: NativePointer, pathPtr: NativePointer, mode: Int, antiAlias: Boolean)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetClip")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetClip")
private external fun RenderNode_nGetClip(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClip")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClip")
private external fun RenderNode_nSetClip(ptr: NativePointer, clip: Boolean)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nBeginRecording")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nBeginRecording")
private external fun RenderNode_nBeginRecording(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nEndRecording")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nEndRecording")
private external fun RenderNode_nEndRecording(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nDrawInto")
@ModuleImport("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nDrawInto")
private external fun RenderNode_nDrawInto(ptr: NativePointer, canvas: NativePointer)
