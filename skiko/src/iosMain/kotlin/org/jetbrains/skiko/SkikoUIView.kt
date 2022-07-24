package org.jetbrains.skiko

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSCoder
import platform.UIKit.UIEvent
import platform.UIKit.UITouch
import platform.UIKit.UIScreen
import platform.UIKit.UIView
import platform.UIKit.setFrame
import platform.UIKit.contentScaleFactor
import platform.UIKit.UIKeyInputProtocol
import platform.UIKit.UIPress
import platform.UIKit.UIPressesEvent

@ExportObjCClass
class SkikoUIView : UIView, UIKeyInputProtocol {
    @OverrideInit
    constructor(frame: CValue<CGRect>) : super(frame)
    @OverrideInit
    constructor(coder: NSCoder) : super(coder)

    private var skiaLayer: SkiaLayer? = null

    constructor(skiaLayer: SkiaLayer, frame: CValue<CGRect> = CGRectMake(0.0, 0.0, 1.0, 1.0)) : super(frame) {
        this.skiaLayer = skiaLayer
    }

    fun detach() = skiaLayer?.detach()

    fun load(): SkikoUIView {
        val (width, height) = UIScreen.mainScreen.bounds.useContents {
            this.size.width to this.size.height
        }
        setFrame(CGRectMake(0.0, 0.0, width, height))
        contentScaleFactor = UIScreen.mainScreen.scale
        skiaLayer?.let { layer ->
            layer.attachTo(this)
            layer.initGestures()
        }

        return this
    }

    fun showScreenKeyboard() = becomeFirstResponder()
    fun hideScreenKeyboard() = resignFirstResponder()
    fun isScreenKeyboardOpen() = isFirstResponder

    private val pressedKeycodes: MutableSet<Long> = mutableSetOf()
    @Deprecated("need be deleted")
    private var inputText: String = ""//todo delete, because it's redundant and may leaks memory
    override fun hasText(): Boolean {
        return inputText.length > 0
    }

    override fun insertText(theText: String) {
        inputText += theText
        skiaLayer?.skikoView?.onInputEvent(toSkikoTypeEvent(theText, null))
    }

    override fun deleteBackward() {
        inputText = inputText.dropLast(1)
        if (!pressedKeycodes.contains(SkikoKey.KEY_BACKSPACE.value)) {
            val downEvent = SkikoKeyboardEvent(
                key = SkikoKey.KEY_BACKSPACE,
                kind = SkikoKeyboardEventKind.DOWN,
                platform = null
            )
            val upEvent = downEvent.copy(
                kind = SkikoKeyboardEventKind.UP
            )
            skiaLayer?.skikoView?.onKeyboardEvent(downEvent)
            skiaLayer?.skikoView?.onKeyboardEvent(upEvent)
        }
    }

    override fun canBecomeFirstResponder() = true

    override fun pressesBegan(presses: Set<*>, withEvent: UIPressesEvent?) {
        if (withEvent != null) {
            for (press in withEvent.allPresses) {
                val uiPress = press as UIPress
                uiPress.key?.let {
                    pressedKeycodes.add(it.keyCode)
                }
                skiaLayer?.skikoView?.onKeyboardEvent(
                    toSkikoKeyboardEvent(press, SkikoKeyboardEventKind.DOWN)
                )
            }
        }
        super.pressesBegan(presses, withEvent)
    }

    override fun pressesEnded(presses: Set<*>, withEvent: UIPressesEvent?) {
        if (withEvent != null) {
            for (press in withEvent.allPresses) {
                val uiPress = press as UIPress
                uiPress.key?.let {
                    pressedKeycodes.remove(it.keyCode)
                }
                skiaLayer?.skikoView?.onKeyboardEvent(
                    toSkikoKeyboardEvent(press, SkikoKeyboardEventKind.UP)
                )
            }
        }
        super.pressesEnded(presses, withEvent)
    }

    override fun touchesBegan(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesBegan(touches, withEvent)
        val events: MutableList<SkikoTouchEvent> = mutableListOf()
        for (touch in touches) {
            val event = touch as UITouch
            val (x, y) = event.locationInView(null).useContents { x to y }
            val timestamp = (event.timestamp * 1_000).toLong()
            events.add(
                SkikoTouchEvent(x, y, SkikoTouchEventKind.STARTED, timestamp, event)
            )
        }
        skiaLayer?.skikoView?.onTouchEvent(events.toTypedArray())
    }

    override fun touchesEnded(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesEnded(touches, withEvent)
        val events: MutableList<SkikoTouchEvent> = mutableListOf()
        for (touch in touches) {
            val event = touch as UITouch
            val (x, y) = event.locationInView(null).useContents { x to y }
            val timestamp = (event.timestamp * 1_000).toLong()
            events.add(
                SkikoTouchEvent(x, y, SkikoTouchEventKind.ENDED, timestamp, event)
            )
        }
        skiaLayer?.skikoView?.onTouchEvent(events.toTypedArray())
    }

    override fun touchesMoved(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesMoved(touches, withEvent)
        val events: MutableList<SkikoTouchEvent> = mutableListOf()
        for (touch in touches) {
            val event = touch as UITouch
            val (x, y) = event.locationInView(null).useContents { x to y }
            val timestamp = (event.timestamp * 1_000).toLong()
            events.add(
                SkikoTouchEvent(x, y, SkikoTouchEventKind.MOVED, timestamp, event)
            )
        }
        skiaLayer?.skikoView?.onTouchEvent(events.toTypedArray())
    }

    override fun touchesCancelled(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesCancelled(touches, withEvent)
        val events: MutableList<SkikoTouchEvent> = mutableListOf()
        for (touch in touches) {
            val event = touch as UITouch
            val (x, y) = event.locationInView(null).useContents { x to y }
            val timestamp = (event.timestamp * 1_000).toLong()
            events.add(
                SkikoTouchEvent(x, y, SkikoTouchEventKind.CANCELLED, timestamp, event)
            )
        }
        skiaLayer?.skikoView?.onTouchEvent(events.toTypedArray())
    }
}
