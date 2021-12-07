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
        nsView = object : NSView(NSMakeRect(0.0, 0.0, width, height)), NSTextInputClientProtocol {
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

            override fun mouseDown(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoMouseButtons.LEFT, SkikoPointerEventKind.DOWN, nsView))
            }
            override fun mouseUp(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoMouseButtons.LEFT, SkikoPointerEventKind.UP, nsView))
            }
            override fun rightMouseDown(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoMouseButtons.RIGHT, SkikoPointerEventKind.DOWN, nsView))
            }
            override fun rightMouseUp(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoMouseButtons.RIGHT, SkikoPointerEventKind.UP, nsView))
            }
            override fun otherMouseDown(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.DOWN, nsView))
            }
            override fun otherMouseUp(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.UP, nsView))
            }
            override fun mouseMoved(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.MOVE, nsView))
            }
            override fun mouseDragged(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoEvent(event, SkikoPointerEventKind.DRAG, nsView))
            }
            override fun scrollWheel(event: NSEvent) {
                skikoView?.onPointerEvent(toSkikoScrollEvent(event))
            }
            override fun keyDown(event: NSEvent) {
                skikoView?.onKeyboardEvent(toSkikoEvent(event, SkikoKeyboardEventKind.DOWN))
                keyEvent = event
                interpretKeyEvents(listOf(event))
            }
            override fun flagsChanged(event: NSEvent) {
                skikoView?.onKeyboardEvent(toSkikoEvent(event))
            }
            override fun keyUp(event: NSEvent) {
                skikoView?.onKeyboardEvent(toSkikoEvent(event, SkikoKeyboardEventKind.UP))
            }
            
            private var keyEvent: NSEvent? = null
            private var markedText: String = ""
            private val kEmptyRange = NSMakeRange(NSNotFound.convert(), 0)
            override fun attributedSubstringForProposedRange(range: CValue<NSRange>, actualRange: NSRangePointer?) = null
            override fun hasMarkedText(): Boolean {
                return markedText.length > 0
            }
            override fun markedRange(): CValue<NSRange> {
                if (markedText.length > 0) {
                    return NSMakeRange(0, (markedText.length - 1).convert())
                }
                return kEmptyRange
            }
            override fun selectedRange() = kEmptyRange
            override fun characterIndexForPoint(point: CValue<NSPoint>) = 0.toULong()
            override fun doCommandBySelector(selector: COpaquePointer?) {}
            override fun firstRectForCharacterRange(range: CValue<NSRange>, actualRange: NSRangePointer?): CValue<NSRect> {
                val (x, y) = window.frame.useContents {
                    origin.x to origin.y
                }
                return NSMakeRect(x, y, 0.0, 0.0)
            }
            override fun insertText(string: Any, replacementRange: CValue<NSRange>) {
                var character = ""
                if (string is NSAttributedString) {
                    character = string.string
                } else {
                    character = string as String
                }
                skikoView?.onInputEvent(toSkikoTypeEvent(character, keyEvent!!))
            }
            override fun setMarkedText(string: Any, selectedRange: CValue<NSRange>, replacementRange: CValue<NSRange>) {
                if (string is NSAttributedString) {
                    markedText = string.string
                } else {
                    markedText = string as String
                }
            }
            override fun unmarkText() {
                markedText = ""
            }
            override fun validAttributesForMarkedText(): List<*> {
                return listOf<Any?>()
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
