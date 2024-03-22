package org.jetbrains.skiko

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import org.jetbrains.skia.*
import org.jetbrains.skiko.redrawer.Redrawer
import platform.AppKit.*
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.darwin.NSObject

/**
 * SkiaLayer implementation for macOS.
 * Supports only [GraphicsApi.METAL]
 */
@OptIn(BetaInteropApi::class)
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
        private set

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

    private val nsViewObserver = object : NSObject() {
        @ObjCAction
        fun frameDidChange(notification: NSNotification) {
            redrawer?.syncSize()
            redrawer?.redrawImmediately()
        }

        @ObjCAction
        fun windowDidChangeBackingProperties(notification: NSNotification) {
            redrawer?.syncSize()
            redrawer?.redrawImmediately()
        }

        fun addObserver() {
            val center = NSNotificationCenter.defaultCenter()
            center.addObserver(
                    observer = this,
                    selector = NSSelectorFromString("frameDidChange:"),
                    name = NSViewFrameDidChangeNotification,
                    `object` = nsView,
                )
            center.addObserver(
                observer = this,
                selector = NSSelectorFromString("windowDidChangeBackingProperties:"),
                name = NSWindowDidChangeBackingPropertiesNotification,
                `object` = nsView.window,
            )
        }

        fun removeObserver() {
            val center = NSNotificationCenter.defaultCenter()
            center.removeObserver(this)
        }
    }

    /**
     * @param container - should be an instance of [NSView]
     */
    actual fun attachTo(container: Any) {
        check(!this::nsView.isInitialized) { "Already attached to another NSView" }
        check(container is NSView) { "container should be an instance of NSView" }
        nsView = container
        nsView.postsFrameChangedNotifications = true
        nsViewObserver.addObserver()
        redrawer = createNativeRedrawer(this, renderApi).apply {
            syncSize()
            needRedraw()
        }
    }

    actual fun detach() {
        nsViewObserver.removeObserver()
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
