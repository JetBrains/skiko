package org.jetbrains.skiko

import kotlinx.cinterop.*
import org.jetbrains.skia.Point
import org.jetbrains.skia.Rect
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSInteger
import kotlin.math.max
import kotlin.math.min

internal expect fun SkikoUIView.skikoInitializeUIView(): Unit
internal expect fun SkikoUIView.skikoShowTextMenu(targetRect: Rect): Unit
internal expect fun SkikoUIView.skikoHideTextMenu(): Unit

/*
 TODO: remove org.jetbrains.skiko.objc.UIViewExtensionProtocol after Kotlin 1.8.20
 https://youtrack.jetbrains.com/issue/KT-40426
*/
@Suppress("CONFLICTING_OVERLOADS")
@ExportObjCClass
class SkikoUIView : UIView, UIKeyInputProtocol, UITextInputProtocol,
    org.jetbrains.skiko.objc.UIViewExtensionProtocol {
    @OverrideInit
    constructor(frame: CValue<CGRect>) : super(frame)

    @OverrideInit
    constructor(coder: NSCoder) : super(coder)

    init {
        skikoInitializeUIView()
    }

    private var skiaLayer: SkiaLayer? = null
    private lateinit var _pointInside: (Point, UIEvent?) -> Boolean
    private var _inputDelegate: UITextInputDelegateProtocol? = null
    private var _currentTextMenuActions: TextActions? = null
    var currentKeyboardType: UIKeyboardType = UIKeyboardTypeDefault
    var currentKeyboardAppearance: UIKeyboardAppearance = UIKeyboardAppearanceDefault
    var currentReturnKeyType: UIReturnKeyType = UIReturnKeyType.UIReturnKeyDefault
    var currentTextContentType: UITextContentType? = null
    var currentIsSecureTextEntry: Boolean = false
    var currentEnablesReturnKeyAutomatically: Boolean = false
    var currentAutocapitalizationType: UITextAutocapitalizationType = UITextAutocapitalizationType.UITextAutocapitalizationTypeSentences
    var currentAutocorrectionType: UITextAutocorrectionType = UITextAutocorrectionType.UITextAutocorrectionTypeYes

    constructor(
        skiaLayer: SkiaLayer,
        frame: CValue<CGRect> = CGRectNull.readValue(),
        pointInside: (Point, UIEvent?) -> Boolean = {_,_-> true }
    ) : super(frame) {
        this.skiaLayer = skiaLayer
        _pointInside = pointInside
    }

    /**
     * Show copy/paste text menu
     * @param targetRect - rectangle of selected text area
     * @param textActions - available (not null) actions in text menu
     */
    fun showTextMenu(targetRect: Rect, textActions: TextActions) {
        _currentTextMenuActions = textActions
        skikoShowTextMenu(targetRect)
    }

    fun hideTextMenu() {
        _currentTextMenuActions = null
        skikoHideTextMenu()
    }

    fun isTextMenuShown():Boolean {
        return _currentTextMenuActions != null
    }

    override fun copy(sender: Any?) {
        _currentTextMenuActions?.copy?.invoke()
    }

    override fun paste(sender: Any?) {
        _currentTextMenuActions?.paste?.invoke()
    }

    override fun cut(sender: Any?) {
        _currentTextMenuActions?.cut?.invoke()
    }

    override fun selectAll(sender: Any?) {
        _currentTextMenuActions?.selectAll?.invoke()
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

    /**
     * A Boolean value that indicates whether the text-entry object has any text.
     * https://developer.apple.com/documentation/uikit/uikeyinput/1614457-hastext
     */
    override fun hasText(): Boolean {
        return skiaLayer?.skikoView?.input?.hasText() ?: false
    }

    /**
     * Inserts a character into the displayed text.
     * Add the character text to your class’s backing store at the index corresponding to the cursor and redisplay the text.
     * https://developer.apple.com/documentation/uikit/uikeyinput/1614543-inserttext
     * @param text A string object representing the character typed on the system keyboard.
     */
    override fun insertText(text: String) {
        skiaLayer?.skikoView?.input?.insertText(text)
    }

    /**
     * Deletes a character from the displayed text.
     * Remove the character just before the cursor from your class’s backing store and redisplay the text.
     * https://developer.apple.com/documentation/uikit/uikeyinput/1614572-deletebackward
     */
    override fun deleteBackward() {
        skiaLayer?.skikoView?.input?.deleteBackward()
    }

    override fun canBecomeFirstResponder() = true
    override fun canBecomeFocused(): Boolean = true

    override fun pressesBegan(presses: Set<*>, withEvent: UIPressesEvent?) {
        handlePress(withEvent, SkikoKeyboardEventKind.DOWN)
        super.pressesBegan(presses, withEvent)
    }

    override fun pressesEnded(presses: Set<*>, withEvent: UIPressesEvent?) {
        handlePress(withEvent, SkikoKeyboardEventKind.UP)
        super.pressesEnded(presses, withEvent)
    }

    private fun handlePress(withEvent: UIPressesEvent?, eventKind: SkikoKeyboardEventKind) {
        if (withEvent != null) {
            for (press in withEvent.allPresses) {
                val uiPress = press as? UIPress
                when {
                    // This property is nil when the press event isn’t from a keyboard; for example, a button press on an Apple TV remote.
                    uiPress?.key != null -> {
                        val key = SkikoKey.valueOf(press.key?.keyCode ?: 0)
                        // println("Press/key $eventKind: $key")
                        skiaLayer?.skikoView?.onKeyboardEvent(
                            toSkikoKeyboardEvent(press, eventKind, key)
                        )
                    }

                    uiPress?.type != null -> {
                        val key = SkikoKey.fromPressType(uiPress.type)
                        // println("Press/type $eventKind: $key")
                        skiaLayer?.skikoView?.onKeyboardEvent(
                            toSkikoKeyboardEvent(press, eventKind, key)
                        )
                    }
                }
            }
        }
    }

    /**
     * https://developer.apple.com/documentation/uikit/uiview/1622533-point
     */
    override fun pointInside(point: CValue<CGPoint>, withEvent: UIEvent?): Boolean {
        val skiaPoint: Point = point.useContents { Point(x.toFloat(), y.toFloat()) }
        return _pointInside(skiaPoint, withEvent)
    }

    /*
     * When there at least one tracked touch, we need notify redrawer about it. It should schedule CADisplayLink which
     * affects frequency of polling UITouch events on high frequency display and forces it to match display refresh rate.
     */
    private var touchesCount = 0
        set(value) {
            field = value

            val needHighFrequencyPolling = value > 0

            skiaLayer?.redrawer?.needsProactiveDisplayLink = needHighFrequencyPolling
        }

    override fun touchesBegan(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesBegan(touches, withEvent)

        touchesCount += touches.size

        sendTouchEventToSkikoView(withEvent!!, SkikoPointerEventKind.DOWN)
    }

    override fun touchesEnded(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesEnded(touches, withEvent)

        touchesCount -= touches.size

        sendTouchEventToSkikoView(withEvent!!, SkikoPointerEventKind.UP)
    }

    override fun touchesMoved(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesMoved(touches, withEvent)
        sendTouchEventToSkikoView(withEvent!!, SkikoPointerEventKind.MOVE)
    }

    override fun touchesCancelled(touches: Set<*>, withEvent: UIEvent?) {
        super.touchesCancelled(touches, withEvent)

        touchesCount -= touches.size

        sendTouchEventToSkikoView(withEvent!!, SkikoPointerEventKind.UP)
    }

    private fun sendTouchEventToSkikoView(event: UIEvent, kind: SkikoPointerEventKind) {
        val pointers = event.touchesForView(this).orEmpty().map {
            val touch = it as UITouch
            val (x, y) = touch.locationInView(null).useContents { x to y }
            SkikoPointer(
                x = x,
                y = y,
                pressed = touch.isPressed,
                device = SkikoPointerDevice.TOUCH,
                id = touch.hashCode().toLong(),
                pressure = touch.force
            )
        }

        skiaLayer?.skikoView?.onPointerEvent(
            SkikoPointerEvent(
                x = pointers.centroidX,
                y = pointers.centroidY,
                kind = kind,
                timestamp = (event.timestamp * 1_000).toLong(),
                pointers = pointers,
                platform = event
            )
        )
         // If invalidation doesn't happen while onPointerEvent is processed, it's too late to schedule any work for this frame.
        skiaLayer?.redrawer?.preventDrawDispatchDuringCurrentFrame()
    }

    private val UITouch.isPressed get() =
        phase != UITouchPhase.UITouchPhaseEnded &&
            phase != UITouchPhase.UITouchPhaseCancelled

    override fun inputDelegate(): UITextInputDelegateProtocol? {
        return _inputDelegate
    }

    override fun setInputDelegate(inputDelegate: UITextInputDelegateProtocol?) {
        _inputDelegate = inputDelegate
    }

    /**
     * Returns the text in the specified range.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614527-text
     * @param range A range of text in a document.
     * @return A substring of a document that falls within the specified range.
     */
    override fun textInRange(range: UITextRange): String? {
        return skiaLayer?.skikoView?.input?.textInRange(range.toIntRange())
    }

    /**
     * Replaces the text in a document that is in the specified range.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614558-replace
     * @param range A range of text in a document.
     * @param withText A string to replace the text in range.
     */
    override fun replaceRange(range: UITextRange, withText: String) {
        skiaLayer?.skikoView?.input?.replaceRange(range.toIntRange(), withText)
    }

    override fun setSelectedTextRange(selectedTextRange: UITextRange?) {
        skiaLayer?.skikoView?.input?.setSelectedTextRange(selectedTextRange?.toIntRange())
    }

    /**
     * The range of selected text in a document.
     * If the text range has a length, it indicates the currently selected text.
     * If it has zero length, it indicates the caret (insertion point).
     * If the text-range object is nil, it indicates that there is no current selection.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614541-selectedtextrange
     */
    override fun selectedTextRange(): UITextRange? {
        return skiaLayer?.skikoView?.input?.getSelectedTextRange()?.toUITextRange()
    }

    /**
     * The range of currently marked text in a document.
     * If there is no marked text, the value of the property is nil.
     * Marked text is provisionally inserted text that requires user confirmation;
     * it occurs in multistage text input.
     * The current selection, which can be a caret or an extended range, always occurs within the marked text.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614489-markedtextrange
     */
    override fun markedTextRange(): UITextRange? {
        return skiaLayer?.skikoView?.input?.markedTextRange()?.toUITextRange()
    }

    override fun setMarkedTextStyle(markedTextStyle: Map<Any?, *>?) {
        // do nothing
    }

    override fun markedTextStyle(): Map<Any?, *>? {
        return null
    }

    /**
     * Inserts the provided text and marks it to indicate that it is part of an active input session.
     * Setting marked text either replaces the existing marked text or,
     * if none is present, inserts it in place of the current selection.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614465-setmarkedtext
     * @param markedText The text to be marked.
     * @param selectedRange A range within markedText that indicates the current selection.
     * This range is always relative to markedText.
     */
    override fun setMarkedText(markedText: String?, selectedRange: CValue<NSRange>) {
        val (locationRelative, lengthRelative) = selectedRange.useContents {
            location.toInt() to length.toInt()
        }
        val relativeTextRange = locationRelative until locationRelative + lengthRelative
        skiaLayer?.skikoView?.input?.setMarkedText(markedText, relativeTextRange)
    }

    /**
     * Unmarks the currently marked text.
     * After this method is called, the value of markedTextRange is nil.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614512-unmarktext
     */
    override fun unmarkText() {
        skiaLayer?.skikoView?.input?.unmarkText()
    }

    override fun beginningOfDocument(): UITextPosition {
        return IntermediateTextPosition(0)
    }

    /**
     * The text position for the end of a document.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614555-endofdocument
     */
    override fun endOfDocument(): UITextPosition {
        return IntermediateTextPosition(skiaLayer?.skikoView?.input?.endOfDocument() ?: 0)
    }

    /**
     * Attention! fromPosition and toPosition may be null
     */
    override fun textRangeFromPosition(fromPosition: UITextPosition, toPosition: UITextPosition): UITextRange? {
        val from = (fromPosition as? IntermediateTextPosition)?.position ?: 0
        val to = (toPosition as? IntermediateTextPosition)?.position ?: 0
        return IntermediateTextRange(
            IntermediateTextPosition(minOf(from, to)),
            IntermediateTextPosition(maxOf(from, to))
        )
    }

    /**
     * Attention! position may be null
     */
    override fun positionFromPosition(position: UITextPosition, offset: NSInteger): UITextPosition? {
        val p = (position as? IntermediateTextPosition)?.position ?: return null
        val endOfDocument = skiaLayer?.skikoView?.input?.endOfDocument()
        return if (endOfDocument != null) {
            IntermediateTextPosition(max(min(p + offset, endOfDocument), 0))
        } else {
            null
        }
    }

    override fun positionFromPosition(
        position: UITextPosition,
        inDirection: UITextLayoutDirection,
        offset: NSInteger
    ): UITextPosition? {
        return when (inDirection) {
            UITextLayoutDirectionLeft, UITextLayoutDirectionUp -> {
                positionFromPosition(position, -offset)
            }

            else -> positionFromPosition(position, offset)
        }
    }

    /**
     * Attention! position and toPosition may be null
     */
    override fun comparePosition(position: UITextPosition, toPosition: UITextPosition): NSComparisonResult {
        val from = (position as? IntermediateTextPosition)?.position ?: 0
        val to = (toPosition as? IntermediateTextPosition)?.position ?: 0
        val result = if (from < to) {
            NSOrderedAscending
        } else if (from > to) {
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
    }

    override fun positionWithinRange(range: UITextRange, atCharacterOffset: NSInteger): UITextPosition? =
        TODO("positionWithinRange range: $range, atCharacterOffset: $atCharacterOffset")

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
        // TODO support RTL text direction
    }

    //Working with Geometry and Hit-Testing. All methods return stubs for now.
    override fun firstRectForRange(range: UITextRange): CValue<CGRect> = CGRectNull.readValue()
    override fun caretRectForPosition(position: UITextPosition): CValue<CGRect> = CGRectNull.readValue()
    override fun selectionRectsForRange(range: UITextRange): List<*> = listOf<UITextSelectionRect>()
    override fun closestPositionToPoint(point: CValue<CGPoint>): UITextPosition? = null
    override fun closestPositionToPoint(point: CValue<CGPoint>, withinRange: UITextRange): UITextPosition? = null
    override fun characterRangeAtPoint(point: CValue<CGPoint>): UITextRange? = null

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

    override fun canPerformAction(action: COpaquePointer?, withSender: Any?): Boolean =
        when (action) {
            NSSelectorFromString(UIResponderStandardEditActionsProtocol::copy.name + ":") ->
                _currentTextMenuActions?.copy != null
            NSSelectorFromString(UIResponderStandardEditActionsProtocol::cut.name + ":") ->
                _currentTextMenuActions?.cut != null
            NSSelectorFromString(UIResponderStandardEditActionsProtocol::paste.name + ":") ->
                _currentTextMenuActions?.paste != null
            NSSelectorFromString(UIResponderStandardEditActionsProtocol::selectAll.name + ":") ->
                _currentTextMenuActions?.selectAll != null
            else -> false
        }

    override fun keyboardType(): UIKeyboardType {
        return currentKeyboardType
    }

    override fun keyboardAppearance(): UIKeyboardAppearance {
        return currentKeyboardAppearance
    }

    override fun returnKeyType(): UIReturnKeyType {
        return currentReturnKeyType
    }

    override fun textContentType(): UITextContentType? {
        return currentTextContentType
    }

    override fun isSecureTextEntry(): Boolean {
        return currentIsSecureTextEntry //todo secure text to prevent copy
    }

    override fun enablesReturnKeyAutomatically(): Boolean {
        return currentEnablesReturnKeyAutomatically
    }

    override fun autocapitalizationType(): UITextAutocapitalizationType {
        return currentAutocapitalizationType
    }

    override fun autocorrectionType(): UITextAutocorrectionType {
        return currentAutocorrectionType
    }

    override fun dictationRecognitionFailed() {
        //todo may be useful
    }

    override fun dictationRecordingDidEnd() {
        //todo may be useful
    }

    /**
     * Call when something changes in text data
     */
    fun textWillChange() {
        _inputDelegate?.textWillChange(this)
    }

    /**
     * Call when something changes in text data
     */
    fun textDidChange() {
        _inputDelegate?.textDidChange(this)
    }

    /**
     * Call when something changes in text data
     */
    fun selectionWillChange() {
        _inputDelegate?.selectionWillChange(this)
    }

    /**
     * Call when something changes in text data
     */
    fun selectionDidChange() {
        _inputDelegate?.selectionDidChange(this)
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

private fun UITextRange.toIntRange(): IntRange {
    val start = (start() as IntermediateTextPosition).position.toInt()
    val end = (end() as IntermediateTextPosition).position.toInt()
    return start until end
}

private fun IntRange.toUITextRange(): UITextRange =
    IntermediateTextRange(start = start, end = endInclusive + 1)

private fun NSWritingDirection.directionToStr() =
    when (this) {
        UITextLayoutDirectionLeft -> "Left"
        UITextLayoutDirectionRight -> "Right"
        UITextLayoutDirectionUp -> "Up"
        UITextLayoutDirectionDown -> "Down"
        else -> "unknown direction"
    }
