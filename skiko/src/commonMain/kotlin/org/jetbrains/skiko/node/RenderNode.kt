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
 *
 * <p>A tree of RenderNodes is owned by a single thread. Recording, changing a property and drawing
 * all mutate the node they are called on, and changing a property of one node may also mutate the
 * nodes drawing it, so none of them may run concurrently with each other. A picture recorded from
 * a node is immutable and can be replayed on any thread.</p>
 */
class RenderNode internal constructor(ptr: NativePointer, managed: Boolean = true) : RefCnt(ptr, managed) {
    private companion object {
        init {
            staticLoad()
        }
    }

    constructor(context: RenderNodeContext) : this(RenderNode_nMake(context._ptr)) {
        Stats.onNativeCall()
    }

    /**
     * Optional paint used when compositing this RenderNode into an offscreen layer.
     *
     * When set, the paint alpha is applied to the composited layer content and therefore
     * overrides [alpha] for content rendering semantics.
     *
     * This does not affect shadow opacity: shadows are still modulated by [alpha].
     */
    var layerPaint: Paint?
        get() = nativeCall {
            val ptr = RenderNode_nGetLayerPaint(_ptr)
            if (ptr == NullPointer) null else Paint(ptr, false)
        }
        set(value) = nativeCall(value) { value ->
            RenderNode_nSetLayerPaint(_ptr, value.pointerOrNullPointer)
        }


    var bounds: Rect
        get() = nativeCall { Rect.fromInteropPointer { RenderNode_nGetBounds(_ptr, it) } }
        set(value) = setNodeBounds(value.left, value.top, value.right, value.bottom)

    /**
     * Sets the bounds for this RenderNode internally and can be used to avoid Rect object creation.
     */
    fun setNodeBounds(left: Float, top: Float, right: Float, bottom: Float) = nativeCall {
        RenderNode_nSetBounds(_ptr, left, top, right, bottom)
    }

    var pivot: Point
        get() = nativeCall { Point.fromInteropPointer { RenderNode_nGetPivot(_ptr, it) } }
        set(value) = setNodePivot(value.x, value.y)

    /**
     * Sets the pivot point for this RenderNode internally and can be used to avoid Point object creation.
     */
    fun setNodePivot(x: Float, y: Float) = nativeCall { RenderNode_nSetPivot(_ptr, x, y) }

    /**
     * Alpha multiplier for this RenderNode.
     *
     * When [layerPaint] is null, this alpha is applied during content replay. When [layerPaint]
     * is set, content alpha is taken from [layerPaint] instead.
     *
     * Shadow opacity is always modulated by this alpha, regardless of [layerPaint].
     */
    var alpha: Float
        get() = nativeCall { RenderNode_nGetAlpha(_ptr) }
        set(value) = nativeCall { RenderNode_nSetAlpha(_ptr, value) }

    var scaleX: Float
        get() = nativeCall { RenderNode_nGetScaleX(_ptr) }
        set(value) = nativeCall { RenderNode_nSetScaleX(_ptr, value) }

    var scaleY: Float
        get() = nativeCall { RenderNode_nGetScaleY(_ptr) }
        set(value) = nativeCall { RenderNode_nSetScaleY(_ptr, value) }

    var translationX: Float
        get() = nativeCall { RenderNode_nGetTranslationX(_ptr) }
        set(value) = nativeCall { RenderNode_nSetTranslationX(_ptr, value) }

    var translationY: Float
        get() = nativeCall { RenderNode_nGetTranslationY(_ptr) }
        set(value) = nativeCall { RenderNode_nSetTranslationY(_ptr, value) }

    var shadowElevation: Float
        get() = nativeCall { RenderNode_nGetShadowElevation(_ptr) }
        set(value) = nativeCall { RenderNode_nSetShadowElevation(_ptr, value) }

    var ambientShadowColor: Int
        get() = nativeCall { RenderNode_nGetAmbientShadowColor(_ptr) }
        set(value) = nativeCall { RenderNode_nSetAmbientShadowColor(_ptr, value) }

    var spotShadowColor: Int
        get() = nativeCall { RenderNode_nGetSpotShadowColor(_ptr) }
        set(value) = nativeCall { RenderNode_nSetSpotShadowColor(_ptr, value) }

    var rotationX: Float
        get() = nativeCall { RenderNode_nGetRotationX(_ptr) }
        set(value) = nativeCall { RenderNode_nSetRotationX(_ptr, value) }

    var rotationY: Float
        get() = nativeCall { RenderNode_nGetRotationY(_ptr) }
        set(value) = nativeCall { RenderNode_nSetRotationY(_ptr, value) }

    var rotationZ: Float
        get() = nativeCall { RenderNode_nGetRotationZ(_ptr) }
        set(value) = nativeCall { RenderNode_nSetRotationZ(_ptr, value) }

    var cameraDistance: Float
        get() = nativeCall { RenderNode_nGetCameraDistance(_ptr) }
        set(value) = nativeCall { RenderNode_nSetCameraDistance(_ptr, value) }

    fun setClipRect(r: Rect, mode: ClipMode = ClipMode.INTERSECT, antiAlias: Boolean = false) {
        setClipRect(r.left, r.top, r.right, r.bottom, mode, antiAlias)
    }

    fun setClipRect(left: Float, top: Float, right: Float, bottom: Float, mode: ClipMode = ClipMode.INTERSECT, antiAlias: Boolean = false) {
        Stats.onNativeCall()
        RenderNode_nSetClipRect(
            ptr = _ptr,
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            mode = mode.ordinal,
            antiAlias = antiAlias
        )
    }

    fun setClipRRect(r: RRect, mode: ClipMode = ClipMode.INTERSECT, antiAlias: Boolean = false) {
        setClipRRect(r.left, r.top, r.right, r.bottom, r.radii, mode, antiAlias)
    }

    fun setClipRRect(left: Float, top: Float, right: Float, bottom: Float, radii: FloatArray, mode: ClipMode = ClipMode.INTERSECT, antiAlias: Boolean = false) {
        Stats.onNativeCall()
        interopScope {
            RenderNode_nSetClipRRect(
                ptr = _ptr,
                left = left,
                top = top,
                right = right,
                bottom = bottom,
                radii = toInterop(radii),
                radiiSize = radii.size,
                mode = mode.ordinal,
                antiAlias = antiAlias
            )
        }
    }

    fun setClipPath(p: Path?, mode: ClipMode = ClipMode.INTERSECT, antiAlias: Boolean = false) = nativeCall(p) { p ->
        RenderNode_nSetClipPath(
            ptr = _ptr,
            pathPtr = p.pointerOrNullPointer,
            mode = mode.ordinal,
            antiAlias = antiAlias
        )
    }

    var clip: Boolean
        get() = nativeCall { RenderNode_nGetClip(_ptr) }
        set(value) = nativeCall { RenderNode_nSetClip(_ptr, value) }

    fun beginRecording(): Canvas = nativeCall {
        Canvas(
            ptr = RenderNode_nBeginRecording(_ptr),
            managed = false,
            _owner = this
        )
    }

    fun endRecording() = nativeCall { RenderNode_nEndRecording(_ptr) }

    fun drawInto(canvas: Canvas) = nativeCall(canvas) { canvas ->
        RenderNode_nDrawInto(_ptr, canvas._ptr)
    }
}

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nMake")
// No ForceNativeThreadState: only allocates a few recording objects and a camera, with no typeface or GPU work, so it
// is short and cannot block.
// Native call path: RenderNode::RenderNode
private external fun RenderNode_nMake(context: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetLayerPaint")
// No ForceNativeThreadState: returns the address of a stored paint or null, so it is short and cannot block.
// Native call path: RenderNode::getLayerPaint
private external fun RenderNode_nGetLayerPaint(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetLayerPaint")
// Needs ForceNativeThreadState: replacing paint can release shaders or filters; clearing cached pictures can release
// fonts and images, and time grows with the number of observing nodes.
// Native call path: RenderNode::setLayerPaint -> invalidateSnapshot -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetLayerPaint(ptr: NativePointer, paint: NativePointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetBounds")
// No ForceNativeThreadState: returns stored bounds and copies them to the result array, so it is short and cannot
// block.
// Native call path: RenderNode::getBounds
private external fun RenderNode_nGetBounds(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetBounds")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setBounds -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetBounds(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetPivot")
// No ForceNativeThreadState: returns a stored pivot and copies it to the result array, so it is short and cannot block.
// Native call path: RenderNode::getPivot
private external fun RenderNode_nGetPivot(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetPivot")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setPivot -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetPivot(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetAlpha")
// No ForceNativeThreadState: only returns a stored alpha value, so it is short and cannot block.
// Native call path: RenderNode::getAlpha
private external fun RenderNode_nGetAlpha(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetAlpha")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setAlpha -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetAlpha(ptr: NativePointer, alpha: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetScaleX")
// No ForceNativeThreadState: only returns a stored scale value, so it is short and cannot block.
// Native call path: RenderNode::getScaleX
private external fun RenderNode_nGetScaleX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetScaleX")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setScaleX -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetScaleX(ptr: NativePointer, scaleX: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetScaleY")
// No ForceNativeThreadState: only returns a stored scale value, so it is short and cannot block.
// Native call path: RenderNode::getScaleY
private external fun RenderNode_nGetScaleY(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetScaleY")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setScaleY -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetScaleY(ptr: NativePointer, scaleY: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetTranslationX")
// No ForceNativeThreadState: only returns a stored translation value, so it is short and cannot block.
// Native call path: RenderNode::getTranslationX
private external fun RenderNode_nGetTranslationX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetTranslationX")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setTranslationX -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetTranslationX(ptr: NativePointer, translationX: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetTranslationY")
// No ForceNativeThreadState: only returns a stored translation value, so it is short and cannot block.
// Native call path: RenderNode::getTranslationY
private external fun RenderNode_nGetTranslationY(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetTranslationY")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setTranslationY -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetTranslationY(ptr: NativePointer, translationY: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetShadowElevation")
// No ForceNativeThreadState: only returns a stored shadow elevation, so it is short and cannot block.
// Native call path: RenderNode::getShadowElevation
private external fun RenderNode_nGetShadowElevation(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetShadowElevation")
// Needs ForceNativeThreadState: clears this node's cached picture and those of observing nodes; picture cleanup can
// release fonts and images, and time grows with the number of observers.
// Native call path: RenderNode::setShadowElevation -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetShadowElevation(ptr: NativePointer, elevation: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetAmbientShadowColor")
// No ForceNativeThreadState: only returns a stored shadow color, so it is short and cannot block.
// Native call path: RenderNode::getAmbientShadowColor
private external fun RenderNode_nGetAmbientShadowColor(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetAmbientShadowColor")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setAmbientShadowColor -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetAmbientShadowColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetSpotShadowColor")
// No ForceNativeThreadState: only returns a stored shadow color, so it is short and cannot block.
// Native call path: RenderNode::getSpotShadowColor
private external fun RenderNode_nGetSpotShadowColor(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetSpotShadowColor")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setSpotShadowColor -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetSpotShadowColor(ptr: NativePointer, color: Int)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationX")
// No ForceNativeThreadState: only reads a stored field, so it is short and cannot block.
// Native call path: RenderNode::getRotationX
private external fun RenderNode_nGetRotationX(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationX")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setRotationX -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetRotationX(ptr: NativePointer, rotationX: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationY")
// No ForceNativeThreadState: only reads a stored field, so it is short and cannot block.
// Native call path: RenderNode::getRotationY
private external fun RenderNode_nGetRotationY(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationY")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setRotationY -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetRotationY(ptr: NativePointer, rotationY: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetRotationZ")
// No ForceNativeThreadState: only reads a stored field, so it is short and cannot block.
// Native call path: RenderNode::getRotationZ
private external fun RenderNode_nGetRotationZ(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetRotationZ")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setRotationZ -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetRotationZ(ptr: NativePointer, rotationZ: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetCameraDistance")
// No ForceNativeThreadState: divides a stored value by 72, so it is short and cannot block.
// Native call path: RenderNode::getCameraDistance
private external fun RenderNode_nGetCameraDistance(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetCameraDistance")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setCameraDistance -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetCameraDistance(ptr: NativePointer, distance: Float)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipRect")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setClipRect -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetClipRect(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, mode: Int, antiAlias: Boolean)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipRRect")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setClipRRect -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetClipRRect(ptr: NativePointer, left: Float, top: Float, right: Float, bottom: Float, radii: InteropPointer, radiiSize: Int, mode: Int, antiAlias: Boolean)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClipPath")
// Needs ForceNativeThreadState: clears observing nodes' cached pictures, which can release fonts and images; time grows
// with the number of observers.
// Native call path: RenderNode::setClipPath -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetClipPath(ptr: NativePointer, pathPtr: NativePointer, mode: Int, antiAlias: Boolean)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nGetClip")
// No ForceNativeThreadState: returns a stored clip value, so it is short and cannot block.
// Native call path: RenderNode::getClip
private external fun RenderNode_nGetClip(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nSetClip")
// Needs ForceNativeThreadState: clears this node's cached picture and those of observing nodes; picture cleanup can
// release fonts and images, and time grows with the number of observers.
// Native call path: RenderNode::setClip -> invalidateSnapshot -> reset -> ~SkPicture
@ForceNativeThreadState
private external fun RenderNode_nSetClip(ptr: NativePointer, clip: Boolean)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nBeginRecording")
// Needs ForceNativeThreadState: starting a recording drops references to drawables from an unfinished earlier
// recording, and the last one released can free a typeface, image, or Kotlin callback.
// Native call path: RenderNode::beginRecording -> SkPictureRecorder::beginRecording -> SkRecordCanvas::reset ->
//   forgetRecord
@ForceNativeThreadState
private external fun RenderNode_nBeginRecording(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nEndRecording")
// Needs ForceNativeThreadState: replacing the old cached drawable can release fonts and images; finishing and replaying
// the recording takes time that grows with the number of recorded operations.
// Native call path: RenderNode::endRecording -> updateDependencies
@ForceNativeThreadState
private external fun RenderNode_nEndRecording(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skiko_node_RenderNodeKt_RenderNode_1nDrawInto")
// Needs ForceNativeThreadState: draws onto a canvas that can be GPU-backed, and text can hit the CoreText glyph cache.
// Native call path: RenderNode::drawInto -> SkCanvas::drawDrawable -> RenderNode::onDraw
@ForceNativeThreadState
private external fun RenderNode_nDrawInto(ptr: NativePointer, canvas: NativePointer)
