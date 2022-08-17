package org.jetbrains.skiko

import kotlinx.cinterop.*
import org.jetbrains.skiko.data.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSInteger

const val LOG_OLD = false

@Suppress("CONFLICTING_OVERLOADS")
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

    override fun hasText(): Boolean {
        val result = skiaLayer?.skikoView?.input?.hasText() ?: false
        log()
        return result
//        return inputText.length > 0
    }

    override fun insertText(text: String) {
        skiaLayer?.skikoView?.input?.insertText(text)
        if (LOG_OLD) {
            inputText += text
            val position = IntermediateTextPosition(inputText.length.toLong())
            _selectedTextRange = IntermediateTextRange(position, position)
            skiaLayer?.skikoView?.onInputEvent(toSkikoTypeEvent(text, null))
            log()
        }
    }

    override fun deleteBackward() {//todo delegate to skiaLayer?.skikoView?.input
//        if (!pressedKeycodes.contains(SkikoKey.KEY_BACKSPACE.value)) {
            skiaLayer?.skikoView?.input?.deleteBackward()
//        }
        if (LOG_OLD) {
            _unmarkText()
            inputText = inputText.dropLast(1)
//            if (!pressedKeycodes.contains(SkikoKey.KEY_BACKSPACE.value)) {
//                val downEvent = SkikoKeyboardEvent(
//                    key = SkikoKey.KEY_BACKSPACE,
//                    kind = SkikoKeyboardEventKind.DOWN,
//                    platform = null
//                )
//                val upEvent = downEvent.copy(
//                    kind = SkikoKeyboardEventKind.UP
//                )
//                skiaLayer?.skikoView?.onKeyboardEvent(downEvent)
//                skiaLayer?.skikoView?.onKeyboardEvent(upEvent)
//            }
            log()
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
                    if (uiPress.key?.keyCode != SkikoKey.KEY_BACKSPACE.value) {
                        skiaLayer?.skikoView?.onKeyboardEvent(
                            toSkikoKeyboardEvent(press, SkikoKeyboardEventKind.DOWN)
                        )
                    }
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
                    if (uiPress.key?.keyCode != SkikoKey.KEY_BACKSPACE.value) {
                        skiaLayer?.skikoView?.onKeyboardEvent(
                            toSkikoKeyboardEvent(press, SkikoKeyboardEventKind.UP)
                        )
                    }
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

    private var _selectedTextRange: IntermediateTextRange =
        IntermediateTextRange(IntermediateTextPosition(0), IntermediateTextPosition(0))
    private var _tokenizer: UITextInputStringTokenizer? = null
    private var _inputDelegate: UITextInputDelegateProtocol? = null
    override fun inputDelegate(): UITextInputDelegateProtocol? {
        return _inputDelegate
    }

    override fun setInputDelegate(inputDelegate: UITextInputDelegateProtocol?) {
        _inputDelegate = inputDelegate
    }

    override fun textInRange(range: UITextRange): String? {
        val result = skiaLayer?.skikoView?.input?.textInRange(range.toSkikoTextRange())
        if (LOG_OLD) {
            val from = (range.start() as IntermediateTextPosition).position
            val to = (range.end() as IntermediateTextPosition).position
            val text = getText()
            val oldResult = if (text.isNotEmpty() && from >= 0 && to >= 0 && text.length >= to) {
                val substring = text.substring(from.toInt(), to.toInt())
                substring.replace("\n", "").ifEmpty { null }
            } else {
                null
            }
            log("oldResult: $oldResult")
        }
        return result
    }

    override fun replaceRange(range: UITextRange, withText: String) {
        skiaLayer?.skikoView?.input?.replaceRange(range.toSkikoTextRange(), withText)
        if (LOG_OLD) {
            val start = (range.start() as IntermediateTextPosition).position
            val end = (range.end() as IntermediateTextPosition).position
            if (end > inputText.length) {
                throw Error("TextInput, replaceRange end=$end > inputText.lastIndex=${inputText.length}")
            }
            inputText = inputText.replaceRange(start.toInt(), end.toInt(), withText)
            log()
        }
    }

    override fun setSelectedTextRange(selectedTextRange: UITextRange?) {
        selectedTextRange?.let {
            val start = ((it as IntermediateTextRange).start() as IntermediateTextPosition).position
            val end = (it.end() as IntermediateTextPosition).position
//            println("TODO setSelectedTextRange, start: $start, end: $end")//todo check
            _selectedTextRange = it
        }
    }

    override fun selectedTextRange(): UITextRange? {
        val result = skiaLayer?.skikoView?.input?.selectedTextRange()?.toUITextRange()
        if (LOG_OLD) {
            val from = 0//getText().length //todo temp
            val to = getText().length
            val oldResult = IntermediateTextRange(start = from, end = to)
            log("oldResult: ${oldResult.toSkikoTextRange()}")
        }
        return result
//        return _selectedTextRange //todo
    }

    override fun markedTextRange(): UITextRange? {
        val result = skiaLayer?.skikoView?.input?.markedTextRange()?.toUITextRange()
        log("oldResult: $_markedTextRange")
        return result
    }

    override fun setMarkedTextStyle(markedTextStyle: Map<Any?, *>?) {
//        println("TODO setMarkedTextStyle")//todo
        // do nothing
    }

    override fun markedTextStyle(): Map<Any?, *>? {
//        println("TODO markedTextStyle")//todo
        return null
    }

    override fun setMarkedText(markedText: String?, selectedRange: CValue<NSRange>) {
        val (locationRelative, lengthRelative) = selectedRange.useContents {
            location.toInt() to length.toInt()
        }
        val relativeTextRange = SkikoTextRange(locationRelative, locationRelative + lengthRelative)
        skiaLayer?.skikoView?.input?.setMarkedText(markedText, relativeTextRange)
        if (LOG_OLD) {
            // [markedText] is text about to confirm by user
            // see more: https://developer.apple.com/documentation/uikit/uitextinput?language=objc
            val cursor = inputText.lastIndex
            val location = cursor + 1
            val length = markedText?.length ?: 0

            markedText?.let {
                _markedTextRange = IntermediateTextRange(
                    IntermediateTextPosition(location.toLong()),
                    IntermediateTextPosition(location.toLong() + length.toLong())
                )
                _selectedTextRange = IntermediateTextRange(
                    IntermediateTextPosition(location.toLong()),
                    IntermediateTextPosition(location.toLong() + length.toLong())
                )
                _markedText = markedText
            }
            log()
        }
    }

    override fun unmarkText() {
        skiaLayer?.skikoView?.input?.unmarkText()
        if (LOG_OLD) {
            _unmarkText()
            log()
        }
    }

    private fun _unmarkText() {
        inputText = getText()
        _markedText = ""
        _markedTextRange = null
    }

    override fun beginningOfDocument(): UITextPosition {
        return IntermediateTextPosition(0)
    }

    override fun endOfDocument(): UITextPosition {
        val result = IntermediateTextPosition(skiaLayer?.skikoView?.input?.endOfDocument() ?: 0)
        val oldResult = IntermediateTextPosition(getText().length.toLong())
        log("oldResult: $oldResult")
        return result
    }

    override fun textRangeFromPosition(fromPosition: UITextPosition, toPosition: UITextPosition): UITextRange? {
        val from = (fromPosition as IntermediateTextPosition)
        val to = (toPosition as IntermediateTextPosition)
        return IntermediateTextRange(from, to)
    }

    override fun positionFromPosition(position: UITextPosition, offset: NSInteger): UITextPosition? {
        val p = (position as IntermediateTextPosition).position
        return IntermediateTextPosition(p + offset)
    }

    override fun positionFromPosition(
        position: UITextPosition,
        inDirection: UITextLayoutDirection,
        offset: NSInteger
    ): UITextPosition? {
        TODO("positionFromPosition with inDirection: ${inDirection.directionToStr()}") //todo use inDirection
        return positionFromPosition(position, offset)
    }

    override fun comparePosition(position: UITextPosition, toPosition: UITextPosition): NSComparisonResult {
        val from = position as IntermediateTextPosition
        val to = toPosition as IntermediateTextPosition
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
        val fromPosition = from as IntermediateTextPosition
        val to = toPosition as IntermediateTextPosition
        return to.position - fromPosition.position
    }

    override fun tokenizer(): UITextInputTokenizerProtocol {
        return UITextInputStringTokenizer()
        if (_tokenizer == null) {
            _tokenizer = UITextInputStringTokenizer(textInput = this)
        }
        return _tokenizer as UITextInputStringTokenizer
    }

    override fun positionWithinRange(range: UITextRange, atCharacterOffset: NSInteger): UITextPosition? =
        TODO("positionWithinRange with default super")//super.positionWithinRange(range, atCharacterOffset)

    override fun positionWithinRange(range: UITextRange, farthestInDirection: UITextLayoutDirection): UITextPosition? =
        TODO("positionWithinRange, farthestInDirection: ${farthestInDirection.directionToStr()}")

    override fun characterRangeByExtendingPosition(
        position: UITextPosition,
        inDirection: UITextLayoutDirection
    ): UITextRange? {
        TODO("characterRangeByExtendingPosition, inDirection: ${inDirection.directionToStr()}")
    }

    override fun baseWritingDirectionForPosition(
        position: UITextPosition,
        inDirection: UITextStorageDirection
    ): NSWritingDirection {
        return NSWritingDirectionLeftToRight // TODO support RTL text direction
    }

    override fun setBaseWritingDirection(writingDirection: NSWritingDirection, forRange: UITextRange) {
        TODO("setBaseWritingDirection, writingDirection: ${writingDirection.directionToStr()}")
    }

    //Working with Geometry and Hit-Testing---------------------------------------------------------------------------
    override fun firstRectForRange(range: UITextRange): CValue<CGRect> {
        val result: SkikoRect? = skiaLayer?.skikoView?.input?.firstRectForRange(range.toSkikoTextRange())
        return if (result != null) {
            CGRectMake(result.x, result.y, result.width, result.height)
        } else {
            CGRectNull.readValue()
        }
    }

    override fun caretRectForPosition(position: UITextPosition): CValue<CGRect> {
        val result: SkikoRect? = skiaLayer?.skikoView?.input?.caretRectForPosition(
            position = (position as IntermediateTextPosition).position
        )
        return if (result != null) {
            CGRectMake(result.x, result.y, result.width, result.height)
        } else {
            CGRectNull.readValue()
        }
    }

    override fun selectionRectsForRange(range: UITextRange): List<*> {
        val result: List<SkikoRect>? = skiaLayer?.skikoView?.input?.selectionRectsForRange(range.toSkikoTextRange())
        return result?.map { MySelectionRect(it) } ?: listOf<UITextSelectionRect>()
    }

    override fun closestPositionToPoint(point: CValue<CGPoint>): UITextPosition? {
        return skiaLayer?.skikoView?.input
            ?.closestPositionToPoint(point = point.useContents { this.toSkikoPoint() })
            ?.let { IntermediateTextPosition(it.toLong()) }
    }

    override fun closestPositionToPoint(point: CValue<CGPoint>, withinRange: UITextRange): UITextPosition? {
        return skiaLayer?.skikoView?.input
            ?.closestPositionToPoint(
                point = point.useContents { this.toSkikoPoint() },
                range = withinRange.toSkikoTextRange()
            )?.let { IntermediateTextPosition(it.toLong()) }
    }

    override fun characterRangeAtPoint(point: CValue<CGPoint>): UITextRange? {
        val skikoPoint = point.useContents { this.toSkikoPoint() }
        return skiaLayer?.skikoView?.input
            ?.characterRangeAtPoint(skikoPoint)
            ?.toUITextRange()
    }
    //---------------------------------------------------------------------------------------------------------

    override fun textStylingAtPosition(position: UITextPosition, inDirection: UITextStorageDirection): Map<Any?, *>? {
        return NSDictionary.dictionary()
    }

    override fun characterOffsetOfPosition(position: UITextPosition, withinRange: UITextRange): NSInteger {
        TODO("characterOffsetOfPosition")
    }

    override fun shouldChangeTextInRange(range: UITextRange, replacementText: String): Boolean {
        // Here we should decide to replace text in range or not.
        // By default, this method returns true.
        return true
    }

    override fun textInputView(): UIView {
        return this
    }

    override fun canPerformAction(action: COpaquePointer?, withSender: Any?): Boolean {
        //todo context menu with actions
        return true
    }

    private var _pasteConfiguration: UIPasteConfiguration? = null
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

    private var inputText: String = "qwe"
    private var _markedText: String = ""
    private var _markedTextRange: IntermediateTextRange? = null
    private var counter = 0

    private fun log(vararg messages: Any) {
//        println("/----OLD-begin----------${counter++}\\")
//        println(messages.map { it.toString() })
//        println("inputText: $inputText, _markedText: $_markedText, _markedTextRange: ${_markedTextRange?.toSkikoTextRange()}")
//        println(Exception().stackTraceToString().split("\n")[4].split("        ").last())
//        println("\\-----OLD-end-----------/")
//        println("")
    }
}

private class IntermediateTextPosition(val position: Long = 0) : UITextPosition()

private fun IntermediateTextRange(start: Int, end: Int) =
    IntermediateTextRange(
        _start = IntermediateTextPosition(start.toLong()),
        _end = IntermediateTextPosition(end.toLong())
    )

private class IntermediateTextRange(
    private val _start: IntermediateTextPosition,
    private val _end: IntermediateTextPosition
) : UITextRange() {
    override fun isEmpty() = (_end.position - _start.position) <= 0
    override fun start(): UITextPosition = _start
    override fun end(): UITextPosition = _end
}

class MySelectionRect(val skikoRect: SkikoRect) : UITextSelectionRect() {

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
        return skikoRect.toCGRect()
    }

    override fun writingDirection(): NSWritingDirection {
        return NSWritingDirectionLeftToRight//todo
    }
}

fun CValue<NSRange>.toStr(): String = useContents { "NSRange location: $location, length: $length " }

private fun UITextRange.toSkikoTextRange(): SkikoTextRange =
    SkikoTextRange(
        start = (start() as IntermediateTextPosition).position.toInt(),
        end = (end() as IntermediateTextPosition).position.toInt()
    )

private fun SkikoTextRange.toUITextRange(): UITextRange =
    IntermediateTextRange(start = start, end = end)

fun SkikoRect.toCGRect() = CGRectMake(x, y, width, height)

fun CGPoint.toSkikoPoint(): SkikoPoint = SkikoPoint(x, y)
fun SkikoPoint.toCGPoint() = CGPointMake(x = x, y = y)

//todo When TextField focus lost - unmark text

private fun NSWritingDirection.directionToStr() =
    when (this) {
        UITextLayoutDirectionLeft -> "Left"
        UITextLayoutDirectionRight -> "Right"
        else -> "unknown direction"
    }

