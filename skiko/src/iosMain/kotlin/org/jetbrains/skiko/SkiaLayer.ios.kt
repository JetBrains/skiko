package org.jetbrains.skiko

import kotlinx.cinterop.useContents
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skiko.context.MetalContextHandler
import org.jetbrains.skiko.redrawer.MetalRedrawer
import platform.UIKit.*
import kotlin.system.getTimeNanos
import org.jetbrains.skia.*

@OptIn(InternalSkikoApi::class)
actual open class SkiaLayer {

    fun isShowing(): Boolean {
        return true
    }

    fun showScreenKeyboard() {
        view?.becomeFirstResponder()
    }

    fun hideScreenKeyboard() { view?.resignFirstResponder() }

    fun isScreenKeyboardOpen(): Boolean {
        return if (view == null) false else view!!.isFirstResponder
    }

    actual var renderApi: GraphicsApi
        get() = GraphicsApi.METAL
        set(value) { throw UnsupportedOperationException() }

    actual val contentScale: Float
        get() = view!!.contentScaleFactor.toFloat()

    actual var fullscreen: Boolean
        get() = true
        set(value) { throw UnsupportedOperationException() }

    actual var transparency: Boolean
        get() = false
        set(value) { throw UnsupportedOperationException() }

    actual fun needRedraw() {
        needRedrawCallback()
    }

    actual val component: Any?
        get() = this.view

    val width: Float
       get() = view!!.frame.useContents {
           return@useContents size.width.toFloat()
       }

    val height: Float
        get() = view!!.frame.useContents {
            return@useContents size.height.toFloat()
        }

    @InternalSkikoApi
    var view: UIView? = null
    // We need to keep reference to gesturesDetector as Objective-C will only keep weak reference here.
    internal var gesturesDetector = SkikoGesturesDetector(this)

    var gesturesToListen: Array<SkikoGestureEventKind>? = null
        set(value) {
            field = value
            initGestures()
        }

    @InternalSkikoApi
    fun initGestures() {
        gesturesDetector.setGesturesToListen(gesturesToListen)
    }

    actual fun attachTo(container: Any) {
        TODO("redundant for iOS")
    }

    fun attachTo(view: UIView) {
        this.view = view
        contextHandler = MetalContextHandler(this)
        // TODO: maybe add observer for view.viewDidDisappear() to detach us?
        redrawer = MetalRedrawer(this).apply {
            needRedraw()
        }
    }

    private var isDisposed = false

    actual fun detach() {
        if (!isDisposed) {
            detachCallback()
            isDisposed = true
        }
    }
    actual var skikoView: SkikoView? = null

    internal var redrawer: MetalRedrawer? = null
    private var contextHandler: MetalContextHandler? = null

    @InternalSkikoApi
    var needRedrawCallback: () -> Unit = { redrawer?.needRedraw() }
    @InternalSkikoApi
    var detachCallback: () -> Unit = {
        redrawer?.dispose()
        redrawer = null
        contextHandler?.dispose()
        contextHandler = null
    }

    @InternalSkikoApi
    actual fun draw(canvas: Canvas) {
        check(!isDisposed) { "SkiaLayer is disposed" }
        val (w, h) = view!!.frame.useContents {
            size.width to size.height
        }
        val pictureWidth = (w.toFloat() * contentScale).coerceAtLeast(0.0F)
        val pictureHeight = (h.toFloat() * contentScale).coerceAtLeast(0.0F)

        skikoView?.onRender(canvas, pictureWidth.toInt(), pictureHeight.toInt(), getTimeNanos())
    }

    actual val pixelGeometry: PixelGeometry
        get() = PixelGeometry.UNKNOWN
}

// TODO: do properly
actual typealias SkikoGesturePlatformEvent = UIEvent
actual typealias SkikoPlatformInputEvent = UIPress
actual typealias SkikoPlatformKeyboardEvent = UIPress
actual typealias SkikoPlatformPointerEvent = UIEvent
