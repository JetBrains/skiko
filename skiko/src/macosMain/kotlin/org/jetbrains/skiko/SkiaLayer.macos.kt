package org.jetbrains.skiko

import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.MacOsOpenGLRedrawer
import org.jetbrains.skiko.redrawer.Redrawer
import platform.AppKit.*
import platform.Foundation.NSMakeRect
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.addObserver
import platform.darwin.NSObject
import platform.CoreGraphics.CGRectMake

actual open class SkiaLayer() {
    fun isShowing(): Boolean {
        return true
    }

    actual var renderApi: GraphicsApi = GraphicsApi.METAL
    actual val contentScale: Float
        get() = if (this::nsView.isInitialized) nsView.window!!.backingScaleFactor.toFloat() else 1.0f

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

    actual var skikoView: SkikoView? = null

    internal var redrawer: Redrawer? = null

    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()

    actual fun attachTo(container: Any) {
        attachTo(container as NSWindow)
    }
    fun attachTo(window: NSWindow) {
        val (width, height) = window.contentLayoutRect.useContents {
            this.size.width to this.size.height
        }
        nsView = object : NSView(NSMakeRect(0.0, 0.0, width, height)) {
            private var trackingArea : NSTrackingArea? = null
            override fun wantsUpdateLayer(): Boolean {
                return true
            }
            override fun acceptsFirstResponder(): Boolean {
                return true
            }
            override fun viewWillMoveToWindow(newWindow: NSWindow?) {
                updateTrackingAreas()
            }
            override fun updateTrackingAreas() {
                trackingArea?.let { removeTrackingArea(it) }
                trackingArea = NSTrackingArea(rect = bounds,
                    options = NSTrackingActiveAlways or
                        NSTrackingMouseEnteredAndExited or
                        NSTrackingMouseMoved or
                        NSTrackingInVisibleRect,
                    owner = nsView, userInfo = null)
                nsView.addTrackingArea(trackingArea!!)
            }

            override fun rightMouseDown(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.DOWN, nsView))
            }
            override fun rightMouseUp(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.UP, nsView))
            }
            override fun mouseDown(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.DOWN, nsView))
            }
            override fun mouseUp(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.UP, nsView))
            }
            override fun mouseMoved(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.MOVE, nsView))
            }
            override fun mouseDragged(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.DRAG, nsView))
            }
            override fun keyDown(event: NSEvent) {
                skikoView?.onKeyboardEvent(toSkikoEvent(event, SkikoKeyboardEventKind.DOWN))
            }
            override fun keyUp(event: NSEvent) {
                skikoView?.onKeyboardEvent(toSkikoEvent(event, SkikoKeyboardEventKind.UP))
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
        window.delegate = object : NSObject(), NSWindowDelegateProtocol {
            override fun windowDidResize(notification: NSNotification) {
                val (w, h) = window.contentView!!.frame.useContents {
                    size.width to size.height
                }
                nsView.frame = CGRectMake(0.0, 0.0, w, h)
                redrawer?.syncSize()
                redrawer?.redrawImmediately()
            }

            override fun windowDidChangeBackingProperties(notification: NSNotification) {
                redrawer?.syncSize()
                redrawer?.redrawImmediately()
            }
        }
        window.contentView!!.addSubview(nsView)
        redrawer = createNativeRedrawer(this, renderApi).apply {
            syncSize()
            needRedraw()
        }
    }

    actual fun detach() {
        redrawer?.dispose()
        redrawer = null
    }

    actual fun needRedraw() {
        redrawer?.needRedraw()
    }

    internal fun update(nanoTime: Long) {
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

    internal fun draw(canvas: Canvas) {
        picture?.also {
            canvas.drawPicture(it.instance)
        }
    }
}

// TODO: do properly
actual typealias SkikoGesturePlatformEvent = NSEvent
actual typealias SkikoPlatformInputEvent = NSEvent
actual typealias SkikoPlatformKeyboardEvent = NSEvent
actual typealias SkikoPlatformPointerEvent = NSEvent
