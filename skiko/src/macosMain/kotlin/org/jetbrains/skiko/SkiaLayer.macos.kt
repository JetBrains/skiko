package org.jetbrains.skiko

import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import org.jetbrains.skiko.context.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.Redrawer
import platform.AppKit.*
import platform.Foundation.NSMakeRect
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.addObserver
import platform.darwin.NSObject

actual open class SkiaLayer(
    private val properties: SkiaLayerProperties = makeDefaultSkiaLayerProperties()
) {
    actual var renderApi: GraphicsApi = GraphicsApi.OPENGL
    actual val contentScale: Float
        get() = _contentScale

    actual var fullscreen: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("fullscreen unsupported")
        }

    actual var transparency: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("transparency unsupported")
        }

    lateinit var nsView: NSView
    var _contentScale: Float = 1.0f

    actual var skikoView: SkikoView? = null

    private var contextHandler = MacOSOpenGLContextHandler(this)

    private var redrawer: Redrawer? = null

    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()

    private fun eventToMouse(event: NSEvent, kind: SkikoPointerEventKind): SkikoPointerEvent {
        var buttons = SkikoMouseButtons.NONE
        val mask = event.buttonMask.toInt()
        if ((mask and 1) != 0 || event.buttonNumber == 0L) {
            buttons = buttons or SkikoMouseButtons.LEFT
        }
        if ((mask and 2) != 0 || event.buttonNumber == 1L) {
            buttons = buttons or SkikoMouseButtons.RIGHT
        }
        var (x, y) = event.locationInWindow.useContents {
            this.x to this.y
        }
        // Translate.
        nsView.frame.useContents {
           y = size.height - y
        }
        return SkikoPointerEvent(x, y, buttons, kind, event)
    }

    private fun eventToKeyboard(event: NSEvent, kind: SkikoKeyboardEventKind): SkikoKeyboardEvent {
         return SkikoKeyboardEvent(event.keyCode.toInt(), kind, event)
    }
    actual fun attachTo(container: Any) {
        attachTo(container as NSWindow)
    }
    fun attachTo(window: NSWindow) {
        val (width, height) = window.contentLayoutRect.useContents {
            this.size.width to this.size.height
        }
        nsView = object : NSView(NSMakeRect(0.0, 0.0, width, height)) {
            private var trackingArea : NSTrackingArea? = null
            override fun acceptsFirstResponder(): Boolean {
                return true
            }
            override fun viewWillMoveToWindow(newWindow: NSWindow?) {
                updateTrackingAreas()
            }
            override fun updateTrackingAreas() {
                trackingArea?.let { removeTrackingArea(it) }
                trackingArea = NSTrackingArea(rect = bounds,
                    options = NSMouseMoved or NSTrackingActiveInActiveApp,
                    owner = nsView, userInfo = null)
                nsView.addTrackingArea(trackingArea!!)
            }

            override fun rightMouseDown(event: NSEvent) {
                skikoView?.onPointerEvent(eventToMouse(event, SkikoPointerEventKind.DOWN))
            }
            override fun rightMouseUp(event: NSEvent) {
                skikoView?.onPointerEvent(eventToMouse(event, SkikoPointerEventKind.UP))
            }
            override fun mouseDown(event: NSEvent) {
                skikoView?.onPointerEvent(eventToMouse(event, SkikoPointerEventKind.DOWN))
            }
            override fun mouseUp(event: NSEvent) {
                skikoView?.onPointerEvent(eventToMouse(event, SkikoPointerEventKind.UP))
            }
            override fun mouseMoved(event: NSEvent) {
                skikoView?.onPointerEvent(eventToMouse(event, SkikoPointerEventKind.MOVE))
            }
            override fun keyDown(event: NSEvent) {
                skikoView?.onKeyboardEvent(eventToKeyboard(event, SkikoKeyboardEventKind.DOWN))
            }
            override fun keyUp(event: NSEvent) {
                skikoView?.onKeyboardEvent(eventToKeyboard(event, SkikoKeyboardEventKind.UP))
            }

            @ObjCAction
            open fun onWindowClose(arg: NSObject?) {
                detach()
                val center = NSNotificationCenter.defaultCenter()
                center.removeObserver(nsView)
            }
        }
        val center = NSNotificationCenter.defaultCenter()
        center.addObserver(nsView, NSSelectorFromString("onWindowClose:"),
            NSWindowWillCloseNotification!!, window)
        window.contentView!!.addSubview(nsView)
        redrawer = createNativeRedrawer(this, GraphicsApi.OPENGL, properties)
        redrawer?.redrawImmediately()
    }

    actual fun detach() {
        redrawer?.dispose()
        redrawer = null
        initedCanvas = false
    }

    actual fun needRedraw() {
        redrawer?.needRedraw()
    }

    fun update(nanoTime: Long) {
        val width = nsView.frame.useContents { size.width }
        val height = nsView.frame.useContents { size.height }

        val pictureWidth = (width * contentScale).coerceAtLeast(0.0)
        val pictureHeight = (height * contentScale).coerceAtLeast(0.0)

        val bounds = Rect.makeWH(pictureWidth.toFloat(), pictureHeight.toFloat())
        val canvas = pictureRecorder.beginRecording(bounds)
        skikoView?.onRender(canvas, pictureWidth.toInt(), pictureHeight.toInt(), nanoTime)

        val picture = pictureRecorder.finishRecordingAsPicture()
        this.picture = PictureHolder(picture, pictureWidth.toInt(), pictureHeight.toInt())
    }

    private var initedCanvas = false

    fun draw() {
        contextHandler.apply {
            if (!initedCanvas) {
                if (!initContext()) {
                    error("initContext() failure")
                    return
                }
                initCanvas()
                initedCanvas = true
            }
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
actual typealias SkikoPlatformInputEvent = NSEvent
actual typealias SkikoPlatformKeyboardEvent = NSEvent
actual typealias SkikoPlatformPointerEvent = NSEvent
