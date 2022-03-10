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

class UISkikoTextPosition(val position: NSInteger = 0): UITextPosition() {}
class UISkikoTextRange(val from: UISkikoTextPosition, val to: UISkikoTextPosition): UITextRange() {
    override fun isEmpty() = !((to.position - from.position) > 0)
    override fun start(): UITextPosition {
        return from
    }
    override fun end(): UITextPosition {
        return to
    }
}

@ExportObjCClass
class SkikoUIView : UIView, UIKeyInputProtocol, UITextInputProtocol {
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
        if (withEvent != null) {
            for (press in withEvent.allPresses) {
                keyEvent = press as UIPress
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
                keyEvent = press as UIPress
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

    private var _textStorage: String = ""
    private var _markedText: String = ""
    private var _markedTextRange: UISkikoTextRange? = null
    private var _selectedTextRange: UISkikoTextRange = UISkikoTextRange(UISkikoTextPosition(), UISkikoTextPosition())

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
        val position = UISkikoTextPosition(_textStorage.length.toLong())
        _selectedTextRange = UISkikoTextRange(position, position)
        _markedTextRange = null
        skiaLayer?.skikoView?.onInputEvent(toSkikoTypeEvent(theText, keyEvent))
    }

    override fun deleteBackward() {
        println("delete")
        _textStorage = _textStorage.dropLast(1)
        val position = UISkikoTextPosition(_textStorage.length.toLong())
        _selectedTextRange = UISkikoTextRange(position, position)
        _markedTextRange = null
        skiaLayer?.skikoView?.onInputEvent(toSkikoTypeEvent("\b", keyEvent))
    }

    override fun textInRange(range: UITextRange): String? {
        val from = ((range as UISkikoTextRange).start() as UISkikoTextPosition).position
        val to = (range.end() as UISkikoTextPosition).position
        println("textInRange $_textStorage $from $to")
        if (_textStorage.isNotEmpty() && from >= 0 && to >= 0 && _textStorage.length > to) {
            return _textStorage.substring(from.toInt(), to.toInt())
        }
        return null
    }

    override fun replaceRange(range: UITextRange, withText: String) {
        val start = ((range as UISkikoTextRange).start() as UISkikoTextPosition).position
        val end = (range.end() as UISkikoTextPosition).position
        _textStorage.replaceRange(start.toInt(), end.toInt(), withText)
    }

    override fun setSelectedTextRange(selectedTextRange: UITextRange?) {
        selectedTextRange?.let {
            _selectedTextRange = it as UISkikoTextRange
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
        val (location, lenght) = selectedRange.useContents {
            location to length
        }
        println("setMarkedText $markedText $location $lenght")
        markedText?.let {
            _markedText = markedText
            _markedTextRange = UISkikoTextRange(
                UISkikoTextPosition(location.toLong()),
                UISkikoTextPosition(location.toLong() + lenght.toLong())
            )
            insertText(_markedText)
        }
    }

    override fun unmarkText() {
        _markedText = ""
        _markedTextRange = null
    }

    override fun beginningOfDocument(): UITextPosition {
        return UISkikoTextPosition(0)
    }

    override fun endOfDocument(): UITextPosition {
        return UISkikoTextPosition(_textStorage.length.toLong() - 1)
    }

    override fun textRangeFromPosition(fromPosition: UITextPosition, toPosition: UITextPosition): UITextRange? {
        val from = (fromPosition as UISkikoTextPosition)
        val to = (toPosition as UISkikoTextPosition)
        return UISkikoTextRange(from, to)
    }

    override fun positionFromPosition(position: UITextPosition, offset: NSInteger): UITextPosition? {
        val p = (position as UISkikoTextPosition).position
        return UISkikoTextPosition(p + offset)
    }

    override fun positionFromPosition(
        position: UITextPosition,
        inDirection: UITextLayoutDirection,
        offset: NSInteger
    ): UITextPosition? {
        var p = (position as UISkikoTextPosition).position
        when (inDirection) {
            UITextLayoutDirectionUp -> {
                val caretRect = caretRectForPosition(position)
                val target = caretRect.useContents { origin }
                val caretHeight = caretRect.useContents { size.height }
                target.y = target.y - (caretHeight * (offset - 1)) - (caretHeight * 0.5f)
                p = (closestPositionToPoint(target.readValue()) as UISkikoTextPosition).position
            }
            UITextLayoutDirectionDown -> {
                val caretRect = caretRectForPosition(position)
                val target = caretRect.useContents { origin }
                val caretHeight = caretRect.useContents { size.height }
                target.y = target.y + (caretHeight * (offset - 1)) - (caretHeight * 1.5f)
                p = (closestPositionToPoint(target.readValue()) as UISkikoTextPosition).position
            }
            UITextLayoutDirectionLeft -> {
                p -= offset;
            }
            UITextLayoutDirectionRight -> {
                p += offset;
            }
        }

        return UISkikoTextPosition(p)
    }

    override fun comparePosition(position: UITextPosition, toPosition: UITextPosition): NSComparisonResult {
        val from = position as UISkikoTextPosition
        val to = toPosition as UISkikoTextPosition
        if (from.position < to.position) {
            return NSOrderedAscending
        }
        if (from.position > to.position) {
            return NSOrderedDescending
        }
        return NSOrderedSame
    }

    override fun offsetFromPosition(from: UITextPosition, toPosition: UITextPosition): NSInteger {
        val fromPosition = from as UISkikoTextPosition
        val to = toPosition as UISkikoTextPosition
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
        return UISkikoTextPosition(0)
    }

    override fun closestPositionToPoint(point: CValue<CGPoint>, withinRange: UITextRange): UITextPosition? {
        return (withinRange as UISkikoTextRange).start()
    }

    override fun characterRangeAtPoint(point: CValue<CGPoint>): UITextRange? {
        val position = closestPositionToPoint(point) as UISkikoTextPosition
        return UISkikoTextRange(position, position)
    }

    override fun textStylingAtPosition(position: UITextPosition, inDirection: UITextStorageDirection): Map<Any?, *>? {
        return NSDictionary.dictionary()
    }

    override fun characterOffsetOfPosition(position: UITextPosition, withinRange: UITextRange): NSInteger {
        return 0
    }
}
