package org.jetbrains.skiko

import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import org.jetbrains.skia.PictureRecorder
import org.jetbrains.skia.Rect
import org.jetbrains.skiko.context.MetalContextHandler
import org.jetbrains.skiko.redrawer.MetalRedrawer
import platform.Foundation.NSSelectorFromString
import platform.UIKit.*
import platform.darwin.NSObject

actual open class SkiaLayer(
    val properties: SkiaLayerProperties = makeDefaultSkiaLayerProperties()
) {
    fun isShowing(): Boolean {
        return true
    }

    actual var renderApi: GraphicsApi
        get() = GraphicsApi.METAL
        set(value) { throw UnsupportedOperationException() }

    actual val contentScale: Float
        get() = 1.0f

    actual var fullscreen: Boolean
        get() = true
        set(value) { throw UnsupportedOperationException() }

    actual var transparency: Boolean
        get() = false
        set(value) { throw UnsupportedOperationException() }

    actual fun needRedraw() {
        redrawer?.needRedraw()
    }

    val width: Float
       get() = view.frame.useContents {
           return@useContents size.width.toFloat()
       }

    val height: Float
        get() = view.frame.useContents {
            return@useContents size.height.toFloat()
        }

    lateinit var view: UIView
    // We need to keep reference to controller as Objective-C will only keep weak reference here.
    lateinit private var controller: NSObject
    actual fun attachTo(container: Any) {
        attachTo(container as UIView)
    }
    fun attachTo(view: UIView) {
        this.view = view

        // See https://developer.apple.com/documentation/uikit/touches_presses_and_gestures/using_responders_and_the_responder_chain_to_handle_events?language=objc
        controller = object : NSObject() {
            @ObjCAction
            fun onTap(sender: UITapGestureRecognizer) {
                val (x, y) = sender.locationInView(view).useContents {
                    x to y
                }
                // TODO: rework events using https://developer.apple.com/documentation/uikit/uiresponder/1621142-touchesbegan?language=objc
                skikoView?.onPointerEvent(
                    SkikoPointerEvent(x, y,
                        SkikoMouseButtons.LEFT, SkikoPointerEventKind.DOWN,
                        null))
                skikoView?.onPointerEvent(
                    SkikoPointerEvent(x, y,
                        SkikoMouseButtons.LEFT, SkikoPointerEventKind.UP,
                null))
            }

        }
        // We have ':' in selector to take care of function argument.
        view.addGestureRecognizer(UITapGestureRecognizer(controller, NSSelectorFromString("onTap:")))
        redrawer = MetalRedrawer(this, properties)
        redrawer?.redrawImmediately()
    }

    actual var skikoView: SkikoView? = null

    internal var redrawer: MetalRedrawer? = null
    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()
    private val contextHandler = MetalContextHandler(this)

    fun update(nanoTime: Long) {
        val pictureWidth = (width * contentScale).coerceAtLeast(0.0F)
        val pictureHeight = (height * contentScale).coerceAtLeast(0.0F)

        val bounds = Rect.makeWH(pictureWidth, pictureHeight)
        val canvas = pictureRecorder.beginRecording(bounds)
        skikoView?.onRender(canvas, pictureWidth.toInt(), pictureHeight.toInt(), nanoTime)
        val picture = pictureRecorder.finishRecordingAsPicture()
        this.picture = PictureHolder(picture, pictureWidth.toInt(), pictureHeight.toInt())
    }

    fun draw() {
        contextHandler.apply {
            if (!initContext()) {
                error("initContext() failure")
            }
            initCanvas()
            clearCanvas()
            val picture = picture
            if (picture != null) {
                drawOnCanvas(picture.instance)
            }
            flush()
        }
    }
}

// TODO: do properly
actual typealias SkikoPlatformInputEvent = UIEvent
actual typealias SkikoPlatformKeyboardEvent = UIEvent
actual typealias SkikoPlatformPointerEvent = UIEvent