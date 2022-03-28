package org.jetbrains.skiko

import kotlinx.cinterop.*
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectNull
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSInteger

@ExportObjCClass
class SkikoViewController : UIViewController {
    @OverrideInit
    constructor() : super(nibName = null, bundle = null)
    @OverrideInit
    constructor(coder: NSCoder) : super(coder)

    constructor(skikoUIView: SkikoUIView) : this() {
        this.skikoUIView = skikoUIView
    }

    private var skikoUIView: SkikoUIView? = null

    override fun loadView() {
        if (skikoUIView == null) {
            super.loadView()
        } else {
            this.view = skikoUIView!!.load()
        }
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        skikoUIView?.showScreenKeyboard()
    } 

    // viewDidUnload() is deprecated and not called.
    override fun viewDidDisappear(animated: Boolean) {
        skikoUIView?.detach()
    }
}

class SkikoTextPosition(val position: NSInteger = 0): UITextPosition() {}
class SkikoTextRange(private val from: SkikoTextPosition, private val to: SkikoTextPosition): UITextRange() {
    override fun isEmpty() = (to.position - from.position) <= 0
    override fun start(): UITextPosition {
        return from
    }
    override fun end(): UITextPosition {
        return to
    }
}

@ExportObjCClass
class SkikoUIView : UIView, UIKeyInputProtocol, UITextInputProtocol {
    // UITextInput: https://developer.apple.com/documentation/uikit/uitextinput?language=objc
    @OverrideInit
    constructor(frame: CValue<CGRect>) : super(frame)
    @OverrideInit
    constructor(coder: NSCoder) : super(coder)

    private var skiaLayer: SkiaLayer? = null

    constructor(
        skiaLayer: SkiaLayer,
        frame: CValue<CGRect> = CGRectNull.readValue()
    ) : super(frame) {
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

    override fun canBecomeFirstResponder() = true
    fun showScreenKeyboard() = becomeFirstResponder()
    fun hideScreenKeyboard() = resignFirstResponder()
    fun isScreenKeyboardOpen() = isFirstResponder

    var keyEvent: UIPress? = null

    override fun pressesBegan(presses: Set<*>, withEvent: UIPressesEvent?) {
        withEvent?.let {
            for (press in it.allPresses) {
                keyEvent = press as UIPress
                skiaLayer?.skikoView?.onKeyboardEvent(
                    toSkikoKeyboardEvent(press, SkikoKeyboardEventKind.DOWN)
                )
            }
        }
        super.pressesBegan(presses, withEvent)
    }

    override fun pressesEnded(presses: Set<*>, withEvent: UIPressesEvent?) {
        withEvent?.let {
            for (press in it.allPresses) {
                keyEvent = press as UIPress
                skiaLayer?.skikoView?.onKeyboardEvent(
                    toSkikoKeyboardEvent(press, SkikoKeyboardEventKind.UP)
                )
            }
        }
        if (keyEvent!!.isBackspaceKey()) { keyEvent = null }
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

    private var _textStorage: String = ""
    private var _markedText: String = ""
    private var _markedTextRange: SkikoTextRange? = null
    private var _selectedTextRange: SkikoTextRange = SkikoTextRange(SkikoTextPosition(0), SkikoTextPosition(0))

    private var _tokenizer: UITextInputStringTokenizer? = null

    private var _inputDelegate: UITextInputDelegateProtocol? = null
    override fun inputDelegate(): UITextInputDelegateProtocol? {
        return _inputDelegate
    }

    override fun setInputDelegate(inputDelegate: UITextInputDelegateProtocol?) {
        _inputDelegate = inputDelegate
    }

    override fun hasText(): Boolean {
        return _textStorage.isNotEmpty()
    }

    override fun insertText(theText: String) {
        _textStorage += theText
        val position = SkikoTextPosition(_textStorage.length.toLong())
        _selectedTextRange = SkikoTextRange(position, position)
        skiaLayer?.skikoView?.onInputEvent(toSkikoTypeEvent(theText, keyEvent))
    }

    override fun deleteBackward() {
        _textStorage = _textStorage.dropLast(1)
        unmarkText()
        // Instead of passing type event with \b character we send keyboard Backspace press/release event
        val backspaceKey = if (keyEvent == null) false else keyEvent!!.isBackspaceKey()
        if (!backspaceKey) {
            skiaLayer?.skikoView?.onKeyboardEvent(
                SkikoKeyboardEvent(
                    key = SkikoKey.KEY_BACKSPACE,
                    kind = SkikoKeyboardEventKind.DOWN,
                    platform = null
                )
            )
            skiaLayer?.skikoView?.onKeyboardEvent(
                SkikoKeyboardEvent(
                    key = SkikoKey.KEY_BACKSPACE,
                    kind = SkikoKeyboardEventKind.UP,
                    platform = null
                )
            )
        }
    }

    override fun textInRange(range: UITextRange): String? {
        val from = ((range as SkikoTextRange).start() as SkikoTextPosition).position
        val to = (range.end() as SkikoTextPosition).position
        if (_textStorage.isNotEmpty() && from >= 0 && to >= 0 && _textStorage.length > to) {
            return _textStorage.substring(from.toInt(), to.toInt())
        }
        return null
    }

    override fun replaceRange(range: UITextRange, withText: String) {
        val start = ((range as SkikoTextRange).start() as SkikoTextPosition).position
        val end = (range.end() as SkikoTextPosition).position
        _textStorage.replaceRange(start.toInt(), end.toInt(), withText)
    }

    override fun setSelectedTextRange(selectedTextRange: UITextRange?) {
        selectedTextRange?.let {
            val start = ((it as SkikoTextRange).start() as SkikoTextPosition).position
            val end = (it.end() as SkikoTextPosition).position
            _selectedTextRange = it
        }
    }

    override fun selectedTextRange(): UITextRange? {
        return _selectedTextRange
    }

    override fun markedTextRange(): UITextRange? {
        return _markedTextRange
    }

    override fun setMarkedTextStyle(markedTextStyle: Map<Any?, *>?) {
        // do nothing
    }

    override fun markedTextStyle(): Map<Any?, *>? {
        return null
    }

    override fun setMarkedText(markedText: String?, selectedRange: CValue<NSRange>) {
        // [markedText] is text about to confirm by user
        // see more: https://developer.apple.com/documentation/uikit/uitextinput?language=objc
        val (location, length) = selectedRange.useContents {
            location to length
        }
        markedText?.let {
            deleteBackward()
            _markedTextRange = SkikoTextRange(
                SkikoTextPosition(location.toLong()),
                SkikoTextPosition(location.toLong() + length.toLong())
            )
            _selectedTextRange = SkikoTextRange(
                SkikoTextPosition(location.toLong()),
                SkikoTextPosition(location.toLong() + length.toLong())
            )
            _markedText = markedText
            insertText(_markedText)
        }
    }

    override fun unmarkText() {
        _markedText = ""
        _markedTextRange = null
    }

    override fun beginningOfDocument(): UITextPosition {
        return SkikoTextPosition(0)
    }

    override fun endOfDocument(): UITextPosition {
        return SkikoTextPosition(_textStorage.length.toLong() - 1)
    }

    override fun textRangeFromPosition(fromPosition: UITextPosition, toPosition: UITextPosition): UITextRange? {
        val from = (fromPosition as SkikoTextPosition)
        val to = (toPosition as SkikoTextPosition)
        return SkikoTextRange(from, to)
    }

    override fun positionFromPosition(position: UITextPosition, offset: NSInteger): UITextPosition? {
        val p = (position as SkikoTextPosition).position
        return SkikoTextPosition(p + offset)
    }

    override fun positionFromPosition(
        position: UITextPosition,
        inDirection: UITextLayoutDirection,
        offset: NSInteger
    ): UITextPosition? {
        return positionFromPosition(position, offset)
    }

    override fun comparePosition(position: UITextPosition, toPosition: UITextPosition): NSComparisonResult {
        val from = position as SkikoTextPosition
        val to = toPosition as SkikoTextPosition
        if (from.position < to.position) {
            return NSOrderedAscending
        }
        if (from.position > to.position) {
            return NSOrderedDescending
        }
        return NSOrderedSame
    }

    override fun offsetFromPosition(from: UITextPosition, toPosition: UITextPosition): NSInteger {
        val fromPosition = from as SkikoTextPosition
        val to = toPosition as SkikoTextPosition
        return to.position - fromPosition.position
    }

    override fun tokenizer(): UITextInputTokenizerProtocol {
        if (_tokenizer == null) {
            _tokenizer = UITextInputStringTokenizer(textInput = this)
        }
        return _tokenizer as UITextInputStringTokenizer
    }

    override fun positionWithinRange(range: UITextRange, farthestInDirection: UITextLayoutDirection): UITextPosition? {
        return null
    }

    override fun characterRangeByExtendingPosition(
        position: UITextPosition,
        inDirection: UITextLayoutDirection
    ): UITextRange? {
        return null
    }

    override fun baseWritingDirectionForPosition(
        position: UITextPosition,
        inDirection: UITextStorageDirection
    ): NSWritingDirection {
        return NSWritingDirectionLeftToRight
    }

    override fun setBaseWritingDirection(writingDirection: NSWritingDirection, forRange: UITextRange) {
        // do nothing
    }

    override fun firstRectForRange(range: UITextRange): CValue<CGRect> {
        return CGRectNull.readValue()
    }

    override fun caretRectForPosition(position: UITextPosition): CValue<CGRect> {
        return bounds
    }

    override fun selectionRectsForRange(range: UITextRange): List<*> {
        return listOf<CGRect>()
    }

    override fun closestPositionToPoint(point: CValue<CGPoint>): UITextPosition? {
        return SkikoTextPosition(0)
    }

    override fun closestPositionToPoint(point: CValue<CGPoint>, withinRange: UITextRange): UITextPosition? {
        return (withinRange as SkikoTextRange).start()
    }

    override fun characterRangeAtPoint(point: CValue<CGPoint>): UITextRange? {
        val position = closestPositionToPoint(point) as SkikoTextPosition
        return SkikoTextRange(position, position)
    }

    override fun textStylingAtPosition(position: UITextPosition, inDirection: UITextStorageDirection): Map<Any?, *>? {
        return NSDictionary.dictionary()
    }

    override fun characterOffsetOfPosition(position: UITextPosition, withinRange: UITextRange): NSInteger {
        return 0
    }

    override fun shouldChangeTextInRange(range: UITextRange, replacementText: String): Boolean {
        // Here we should decide to replace text in range or not.
        // By default, this method returns true.
        return true
    }

    override fun textInputView(): UIView {
        return this
    }
}

private fun UIPress.isBackspaceKey(): Boolean {
    key?.let {
        return it.keyCode == SkikoKey.KEY_BACKSPACE.value
    }
    return false
}