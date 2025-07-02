package org.jetbrains.skiko.node

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ClipMode
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Path
import org.jetbrains.skia.Point
import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
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