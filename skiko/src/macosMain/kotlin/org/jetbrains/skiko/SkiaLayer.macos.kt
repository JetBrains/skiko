package org.jetbrains.skiko

import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import kotlinx.cinterop.CValue
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.convert
import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.MacOsOpenGLRedrawer
import org.jetbrains.skiko.redrawer.Redrawer
import platform.AppKit.*
import platform.Foundation.NSAttributedString
import platform.Foundation.NSMutableAttributedString
import platform.Foundation.NSMakeRect
import platform.Foundation.NSMakeRange
import platform.Foundation.NSRange
import platform.Foundation.NSRangePointer
import platform.Foundation.NSPoint
import platform.Foundation.NSRect
import platform.Foundation.NSNotFound
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.addObserver
import platform.darwin.NSObject
import platform.CoreGraphics.CGRectMake

/**
 * SkiaLayer implementation for macOS.
 * Supports only [GraphicsApi.METAL]
 */
actual open class SkiaLayer {
    fun isShowing(): Boolean {
        return true
    }

    /**
     * [GraphicsApi.METAL] is the only GraphicsApi supported for macOS.
     * Setter throws an IllegalArgumentException if the value is not [GraphicsApi.METAL].
     */
    actual var renderApi: GraphicsApi = GraphicsApi.METAL
        set(value) {
            if (value != GraphicsApi.METAL) {
                throw IllegalArgumentException("$field is not supported in macOS")
            }
            field = value
        }

    /**
     * The scale factor of [NSWindow]
     * https://developer.apple.com/documentation/appkit/nswindow/1419459-backingscalefactor
     */
    actual val contentScale: Float
        get() = if (this::nsView.isInitialized) nsView.window!!.backingScaleFactor.toFloat() else 1.0f

    /**
     * Fullscreen is not supported
     */
    actual var fullscreen: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("fullscreen unsupported")
        }

    /**
     * Transparency is not supported on macOS native.
     */
    actual var transparency: Boolean
        get() = false
        set(value) {
            if (value) throw IllegalArgumentException("transparency unsupported")
        }

    /**
     * Underlying [NSView]
     */
    lateinit var nsView: NSView

    actual val component: Any?
        get() = this.nsView

    /**
     * Implements rendering logic and events processing.
     */
    actual var renderDelegate: SkikoRenderDelegate? = null

    internal var redrawer: Redrawer? = null

    /**
     * Created/updated by recording in [update].
     * It's used as a source for drawing on canvas in [draw].
     */
    private var picture: PictureHolder? = null
    private val pictureRecorder = PictureRecorder()

    /**
     * @param container - should be an instance of [NSWindow]
     */
    actual fun attachTo(container: Any) {
        attachTo(container as NSWindow)
    }

    /**
     * Initializes the [nsView] then adds it to [window]. Initializes events listeners.
     * Delegates events processing to [skikoView].
     */
    fun attachTo(window: NSWindow) {
        val (width, height) = window.contentLayoutRect.useContents {
            this.size.width to this.size.height
        }
        nsView = object : NSView(NSMakeRect(0.0, 0.0, width, height)) {
            private var trackingArea : NSTrackingArea? = null
            override fun wantsUpdateLayer() = true
            override fun acceptsFirstResponder() = true
            override fun viewWillMoveToWindow(newWindow: NSWindow?) {
                updateTrackingAreas()
            }

            override fun updateTrackingAreas() {
                trackingArea?.let { removeTrackingArea(it) }
                trackingArea = NSTrackingArea(
                    rect = bounds,
                    options = NSTrackingActiveAlways or
                        NSTrackingMouseEnteredAndExited or
                        NSTrackingMouseMoved or
                        NSTrackingActiveInKeyWindow or
                        NSTrackingAssumeInside or
                        NSTrackingInVisibleRect,
                    owner = nsView, userInfo = null)
                nsView.addTrackingArea(trackingArea!!)
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
        window.makeFirstResponder(nsView)
        redrawer = createNativeRedrawer(this, renderApi).apply {
            syncSize()
            needRedraw()
        }
    }

    actual fun detach() {
        redrawer?.dispose()
        redrawer = null
    }

    /**
     * Schedules a frame to an appropriate moment.
     */
    actual fun needRedraw() {
        redrawer?.needRedraw()
    }

    /**
     * Updates the [picture] according to current [nanoTime]
     */
    internal fun update(nanoTime: Long) {
        val width = nsView.frame.useContents { size.width }
        val height = nsView.frame.useContents { size.height }

        val pictureWidth = (width * contentScale).coerceAtLeast(0.0)
        val pictureHeight = (height * contentScale).coerceAtLeast(0.0)

        val bounds = Rect.makeWH(pictureWidth.toFloat(), pictureHeight.toFloat())
        val canvas = pictureRecorder.beginRecording(bounds)
        renderDelegate?.onRender(canvas, pictureWidth.toInt(), pictureHeight.toInt(), nanoTime)

        val picture = pictureRecorder.finishRecordingAsPicture()
        this.picture = PictureHolder(picture, pictureWidth.toInt(), pictureHeight.toInt())
    }

    internal actual fun draw(canvas: Canvas) {
        picture?.also {
            canvas.drawPicture(it.instance)
        }
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}
