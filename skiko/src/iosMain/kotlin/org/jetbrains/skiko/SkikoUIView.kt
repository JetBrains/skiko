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
class SkikoUIView : UIView, UIKeyInputProtocol, UITextInputProtocol, UITextInputTraitsProtocol, UITextPasteConfigurationSupportingProtocol {
    @OverrideInit
    constructor(frame: CValue<CGRect>) : super(frame)

    @OverrideInit
    constructor(coder: NSCoder) : super(coder)

    private var skiaLayer: SkiaLayer? = null

    constructor(skiaLayer: SkiaLayer, frame: CValue<CGRect> = CGRectNull.readValue()) : super(frame) {
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

    fun getText(): String = inputText + _markedText

    @Deprecated("need be deleted")
    private var inputText: String = "qwe"//todo delete, because it's redundant and may leaks memory
    override fun hasText(): Boolean {
        return inputText.length > 0
    }

    override fun insertText(theText: String) {
        println("insertText: $theText")
        inputText += theText
        val position = SkikoTextPosition(inputText.length.toLong())
        _selectedTextRange = SkikoTextRange(position, position)
        skiaLayer?.skikoView?.onInputEvent(toSkikoTypeEvent(theText, null))
    }

    override fun deleteBackward() {
        inputText = inputText.dropLast(1)
        unmarkText()
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
                val uiPress = press as? UIPress
                if (uiPress != null) {
                    uiPress.key?.let {
                        pressedKeycodes.add(it.keyCode)
                    }
                    skiaLayer?.skikoView?.onKeyboardEvent(
                        toSkikoKeyboardEvent(press, SkikoKeyboardEventKind.DOWN)
                    )
                }
            }
        }
        super.pressesBegan(presses, withEvent)
    }

    override fun pressesEnded(presses: Set<*>, withEvent: UIPressesEvent?) {
        if (withEvent != null) {
            for (press in withEvent.allPresses) {
                val uiPress = press as? UIPress
                if (uiPress != null) {
                    uiPress.key?.let {
                        pressedKeycodes.remove(it.keyCode)
                    }
                    skiaLayer?.skikoView?.onKeyboardEvent(
                        toSkikoKeyboardEvent(press, SkikoKeyboardEventKind.UP)
                    )
                }
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

    override fun textInRange(range: UITextRange): String? {
        val from = ((range as SkikoTextRange).start() as SkikoTextPosition).position
        val to = (range.end() as SkikoTextPosition).position
        val text = getText()
        val result = if (text.isNotEmpty() && from >= 0 && to >= 0 && text.length >= to) {
            val substring = text.substring(from.toInt(), to.toInt())
            val result = substring.replace("\n", "")
            result.ifEmpty { null }
        } else {
            null
        }
        return result
    }

    override fun replaceRange(range: UITextRange, withText: String) {
        val start = ((range as SkikoTextRange).start() as SkikoTextPosition).position
        val end = (range.end() as SkikoTextPosition).position
        if (end > inputText.length) {
            throw Error("TextInput, replaceRange end=$end > inputText.lastIndex=${inputText.length}")
        }
        inputText = inputText.replaceRange(start.toInt(), end.toInt(), withText)
    }

    override fun setSelectedTextRange(selectedTextRange: UITextRange?) {
        selectedTextRange?.let {
            val start = ((it as SkikoTextRange).start() as SkikoTextPosition).position
            val end = (it.end() as SkikoTextPosition).position
            println("TODO setSelectedTextRange, start: $start, end: $end")//todo check
            _selectedTextRange = it
        }
    }

    override fun selectedTextRange(): UITextRange? {
        println("TEMP selectedTextRange")
        val from = 0//getText().length //todo temp
        val to = getText().length
        return SkikoTextRange(from = from, to = to)
        return _selectedTextRange
    }

    override fun markedTextRange(): UITextRange? {
        return _markedTextRange
    }

    override fun setMarkedTextStyle(markedTextStyle: Map<Any?, *>?) {
        println("TODO setMarkedTextStyle")//todo
        // do nothing
    }

    override fun markedTextStyle(): Map<Any?, *>? {
        println("TODO markedTextStyle")//todo
        return null
    }

    override fun setMarkedText(markedText: String?, selectedRange: CValue<NSRange>) {
        // [markedText] is text about to confirm by user
        // see more: https://developer.apple.com/documentation/uikit/uitextinput?language=objc
        val (locationRelative, lengthRelative) = selectedRange.useContents {
            location to length
        }
        val cursor = inputText.lastIndex
        val location = cursor + 1
        val length = markedText?.length ?: 0

        markedText?.let {
            _markedTextRange = SkikoTextRange(
                SkikoTextPosition(location.toLong()),
                SkikoTextPosition(location.toLong() + length.toLong())
            )
            _selectedTextRange = SkikoTextRange(
                SkikoTextPosition(location.toLong()),
                SkikoTextPosition(location.toLong() + length.toLong())
            )
            _markedText = markedText
        }
    }

    override fun unmarkText() {
        inputText = getText()
        _markedText = ""
        _markedTextRange = null
    }

    override fun beginningOfDocument(): UITextPosition {
        return SkikoTextPosition(0)
    }

    override fun endOfDocument(): UITextPosition {
        return SkikoTextPosition(getText().length.toLong())
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
        println("TODO positionFromPosition with inDirection") //todo use inDirection
        return positionFromPosition(position, offset)
    }

    override fun comparePosition(position: UITextPosition, toPosition: UITextPosition): NSComparisonResult {
        val from = position as SkikoTextPosition
        val to = toPosition as SkikoTextPosition
        val result = if (from.position < to.position) {
            NSOrderedAscending
        } else if (from.position > to.position) {
            NSOrderedDescending
        } else {
            NSOrderedSame
        }
        return result
    }

    override fun offsetFromPosition(from: UITextPosition, toPosition: UITextPosition): NSInteger {
        val fromPosition = from as SkikoTextPosition
        val to = toPosition as SkikoTextPosition
        return to.position - fromPosition.position
    }

    override fun tokenizer(): UITextInputTokenizerProtocol {
        return UITextInputStringTokenizer()
        if (_tokenizer == null) {
            _tokenizer = UITextInputStringTokenizer(textInput = this)
        }
        return _tokenizer as UITextInputStringTokenizer
    }

    override fun positionWithinRange(range: UITextRange, farthestInDirection: UITextLayoutDirection): UITextPosition? {
        println("TODO positionWithinRange")//todo
        return null
    }

    override fun characterRangeByExtendingPosition(
        position: UITextPosition,
        inDirection: UITextLayoutDirection
    ): UITextRange? {
        println("TODO characterRangeByExtendingPosition")//todo
        return null
    }

    override fun baseWritingDirectionForPosition(
        position: UITextPosition,
        inDirection: UITextStorageDirection
    ): NSWritingDirection {
        println("TODO baseWritingDirectionForPosition")//todo
        return NSWritingDirectionLeftToRight
    }

    override fun setBaseWritingDirection(writingDirection: NSWritingDirection, forRange: UITextRange) {
        println("TODO setBaseWritingDirection")//todo
    }

    override fun firstRectForRange(range: UITextRange): CValue<CGRect> {
        println("TODO firstRectForRange")//todo
        return CGRectNull.readValue()
    }

    override fun caretRectForPosition(position: UITextPosition): CValue<CGRect> {
        println("TODO caretRectForPosition")//todo
        return bounds
    }

    override fun selectionRectsForRange(range: UITextRange): List<*> {
        println("TODO TARGET selectionRectsForRange")//todo
        return listOf<UITextSelectionRect>(MySelectionRect())
    }

    override fun closestPositionToPoint(point: CValue<CGPoint>): UITextPosition? {
        println("TODO closestPositionToPoint")
        return SkikoTextPosition(0)
    }

    override fun closestPositionToPoint(point: CValue<CGPoint>, withinRange: UITextRange): UITextPosition? {
        println("TODO closestPositionToPoint")
        return (withinRange as SkikoTextRange).start()
    }

    override fun characterRangeAtPoint(point: CValue<CGPoint>): UITextRange? {
        println("TODO characterRangeAtPoint")
        val position = closestPositionToPoint(point) as SkikoTextPosition
        return SkikoTextRange(position, position)
    }

    override fun textStylingAtPosition(position: UITextPosition, inDirection: UITextStorageDirection): Map<Any?, *>? {
        println("TODO textStylingAtPosition")
        return NSDictionary.dictionary()
    }

    override fun characterOffsetOfPosition(position: UITextPosition, withinRange: UITextRange): NSInteger {
        println("TODO characterOffsetOfPosition")
        return 0
    }

    override fun shouldChangeTextInRange(range: UITextRange, replacementText: String): Boolean {
        println("TODO shouldChangeTextInRange")
        // Here we should decide to replace text in range or not.
        // By default, this method returns true.
        return true
    }

    override fun textInputView(): UIView {
        return this
    }

    override fun canPerformAction(action: COpaquePointer?, withSender: Any?): Boolean {
        println("TODO canPerformAction")
        return true
    }

    private var _pasteConfiguration:UIPasteConfiguration? = null
    private var _pasteDelegate: UITextPasteDelegateProtocol? = null
    override fun pasteConfiguration(): UIPasteConfiguration? {
        //https://developer.apple.com/documentation/uikit/uitextpasteconfigurationsupporting
        println("TODO PASTE pasteConfiguration")
        return _pasteConfiguration
    }

    override fun setPasteConfiguration(pasteConfiguration: UIPasteConfiguration?) {
        println("TODO PASTE setPasteConfiguration")
        _pasteConfiguration = pasteConfiguration
    }

    override fun pasteDelegate(): UITextPasteDelegateProtocol? {
        println("TODO PASTE pasteDelegate")
        return _pasteDelegate
    }

    override fun setPasteDelegate(pasteDelegate: UITextPasteDelegateProtocol?) {
        println("TODO PASTE setPasteDelegate")
        _pasteDelegate = pasteDelegate
    }

    override fun keyboardType(): UIKeyboardType {
        return UIKeyboardTypeDefault
        return UIKeyboardTypeDecimalPad//todo
    }

    override fun isSecureTextEntry(): Boolean {
        return false//todo change secure to prevent copy text
    }

    override fun autocapitalizationType(): UITextAutocapitalizationType {
        return UITextAutocapitalizationType.UITextAutocapitalizationTypeSentences
//        return UITextAutocapitalizationType.UITextAutocapitalizationTypeAllCharacters
    }

    override fun autocorrectionType(): UITextAutocorrectionType {
        return UITextAutocorrectionType.UITextAutocorrectionTypeYes
    }
}

class SkikoTextPosition(val position: Long = 0) : UITextPosition()

fun SkikoTextRange(from: Int, to: Int) =
    SkikoTextRange(from = SkikoTextPosition(from.toLong()), to = SkikoTextPosition(to.toLong()))

class SkikoTextRange(private val from: SkikoTextPosition, private val to: SkikoTextPosition) : UITextRange() {
    override fun isEmpty() = (to.position - from.position) <= 0
    override fun start(): UITextPosition {
        return from
    }

    override fun end(): UITextPosition {
        return to
    }

    fun toStr(): String = "SkikoTextRange(from: ${from.position}, to: ${to.position})"
}

class MySelectionRect():UITextSelectionRect() {

    /**
     * A Boolean value that indicates whether the rectangle contains the end of the selection.
     */
    override fun containsEnd(): Boolean {
        return false
    }

    /**
     * A Boolean value that indicates whether the rectangle contains the start of the selection.
     */
    override fun containsStart(): Boolean {
        return false
    }

    override fun isVertical(): Boolean {
        return false
    }

    override fun rect(): CValue<CGRect> {
        return CGRectMake(40.0, 150.0, 80.0, 40.0)
    }

    override fun writingDirection(): NSWritingDirection {
        return NSWritingDirectionLeftToRight//todo
    }
}

fun CValue<NSRange>.toStr(): String = useContents { "NSRange location: $location, length: $length" }

//When TextField focus lost - unmark text
