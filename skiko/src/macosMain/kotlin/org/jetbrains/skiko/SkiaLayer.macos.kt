package org.jetbrains.skiko

import kotlinx.cinterop.useContents
import org.jetbrains.skiko.context.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.Redrawer
import platform.AppKit.*
import platform.Foundation.NSMakeRect

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

    actual var renderer: SkiaRenderer? = null
    actual var eventProcessor: SkikoEventProcessor? = null

    private var contextHandler = MacOSOpenGLContextHandler(this)

    private var redrawer: Redrawer? = null

    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()

    private fun eventToMouse(event: NSEvent, kind: SkikoMouseEventKind): SkikoMouseEvent {
        val buttons = when (event.buttonNumber.toInt()) {
            0 -> SkikoMouseButtons.LEFT
            1 -> SkikoMouseButtons.RIGHT
            else -> SkikoMouseButtons.NONE
        }
        var (x, y) = event.locationInWindow.useContents {
            this.x to this.y
        }
        // Translate.
        nsView.frame.useContents {
           y = size.height - y
        }
        return SkikoMouseEvent(x.toInt(), y.toInt(), buttons, kind, event)
    }

    private fun eventToKeyboard(event: NSEvent, kind: SkikoKeyboardEventKind): SkikoKeyboardEvent {
         return SkikoKeyboardEvent(event.keyCode.toInt(), kind, event)
    }

    fun initLayer(window: NSWindow) {
        val (width, height) = window.contentLayoutRect.useContents {
            this.size.width to this.size.height
        }
        nsView = object : NSView(NSMakeRect(0.0, 0.0, width, height)) {
            private var trackingArea : NSTrackingArea? = null
            override fun acceptsFirstResponder(): Boolean {
                return true
            }
            override fun updateTrackingAreas() {
                trackingArea?.let { removeTrackingArea(it) }
                trackingArea = NSTrackingArea(rect = bounds, options = NSMouseMoved, owner = null, userInfo = null)
                addTrackingArea(trackingArea!!)
            }
            override fun mouseDown(event: NSEvent) {
                eventProcessor?.onMouseEvent(eventToMouse(event, SkikoMouseEventKind.DOWN))
            }
            override fun mouseUp(event: NSEvent) {
                eventProcessor?.onMouseEvent(eventToMouse(event, SkikoMouseEventKind.UP))
            }
            override fun mouseMoved(event: NSEvent) {
                eventProcessor?.onMouseEvent(eventToMouse(event, SkikoMouseEventKind.MOVE))
            }
            override fun keyDown(event: NSEvent) {
                eventProcessor?.onKeyboardEvent(eventToKeyboard(event, SkikoKeyboardEventKind.DOWN))
            }
            override fun keyUp(event: NSEvent) {
                eventProcessor?.onKeyboardEvent(eventToKeyboard(event, SkikoKeyboardEventKind.UP))
            }
        }
        window.contentView!!.addSubview(nsView)
        redrawer = createNativeRedrawer(this, GraphicsApi.OPENGL, properties)
        redrawer?.redrawImmediately()
    }

    fun disposeLayer() {
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
        renderer?.onRender(canvas, pictureWidth.toInt(), pictureHeight.toInt(), nanoTime)

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
