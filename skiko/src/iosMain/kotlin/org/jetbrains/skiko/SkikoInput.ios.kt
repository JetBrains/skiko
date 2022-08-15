package org.jetbrains.skiko

import org.jetbrains.skiko.data.*

actual interface SkikoInput {
    
    /**
     * A Boolean value that indicates whether the text-entry object has any text.
     * https://developer.apple.com/documentation/uikit/uikeyinput/1614457-hastext
     */
    fun hasText(): Boolean

    /**
     * Inserts a character into the displayed text.
     * Add the character text to your class’s backing store at the index corresponding to the cursor and redisplay the text.
     * https://developer.apple.com/documentation/uikit/uikeyinput/1614543-inserttext
     * @param text A string object representing the character typed on the system keyboard.
     */
    fun insertText(text: String)

    /**
     * Deletes a character from the displayed text.
     * Remove the character just before the cursor from your class’s backing store and redisplay the text.
     * https://developer.apple.com/documentation/uikit/uikeyinput/1614572-deletebackward
     */
    fun deleteBackward()

    /**
     * The text position for the end of a document.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614555-endofdocument
     */
    fun endOfDocument(): Long

    /**
     * The range of selected text in a document.
     * If the text range has a length, it indicates the currently selected text.
     * If it has zero length, it indicates the caret (insertion point).
     * If the text-range object is nil, it indicates that there is no current selection.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614541-selectedtextrange
     */
    fun selectedTextRange(): SkikoTextRange?

    /**
     * Returns the text in the specified range.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614527-text
     * @param range A range of text in a document.
     * @return A substring of a document that falls within the specified range.
     */
    fun textInRange(range: SkikoTextRange): String?

    /**
     * Replaces the text in a document that is in the specified range.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614558-replace
     * @param range A range of text in a document.
     * @param text A string to replace the text in range.
     */
    fun replaceRange(range: SkikoTextRange, text: String)

    /**
     * Inserts the provided text and marks it to indicate that it is part of an active input session.
     * Setting marked text either replaces the existing marked text or,
     * if none is present, inserts it in place of the current selection.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614465-setmarkedtext
     * @param markedText The text to be marked.
     * @param selectedRange A range within markedText that indicates the current selection.
     * This range is always relative to markedText.
     */
    fun setMarkedText(markedText: String?, selectedRange: SkikoTextRange)

    /**
     * The range of currently marked text in a document.
     * If there is no marked text, the value of the property is nil.
     * Marked text is provisionally inserted text that requires user confirmation;
     * it occurs in multistage text input.
     * The current selection, which can be a caret or an extended range, always occurs within the marked text.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614489-markedtextrange
     */
    fun markedTextRange(): SkikoTextRange?

    /**
     * Unmarks the currently marked text.
     * After this method is called, the value of markedTextRange is nil.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614512-unmarktext
     */
    fun unmarkText()
    
    //---Working with Geometry and Hit-Testing----------------------------------------------
    /**
     * Returns the first rectangle that encloses a range of text in a document.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614570-firstrect
     * @param range An object that represents a range of text in a document.
     * @return The first rectangle in a range of text.
     * You might use this rectangle to draw a correction rectangle.
     * The “first” in the name refers the rectangle enclosing the first line
     * when the range encompasses multiple lines of text.
     */
    fun firstRectForRange(range: SkikoTextRange): SkikoRect?

    /**
     * Returns a rectangle to draw the caret at a specified insertion point.
     * The system uses this value to calculate the length of the beam—the vertical line representing
     * the pointer—when using a trackpad to interact with a text input area.
     * You must implement this method even if text never becomes editable, and an insertion point caret never appears.
     * @param position An object that identifies a location in a text input area.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614570-firstrect
     * @return A rectangle that defines the area for drawing the caret.
     */
    fun caretRectForPosition(position: Long): SkikoRect?

    /**
     * Returns an array of selection rects corresponding to the range of text.
     * @param range An object representing a range in a document’s text.
     * @return An array of UITextSelectionRect objects that encompass the selection.
     */
    fun selectionRectsForRange(range: SkikoTextRange): List<SkikoRect>

    /**
     * Returns the character or range of characters that is at a specified point in a document.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614574-characterrange
     * @param point A point in the view that is drawing a document’s text.
     * @return An object representing a range that encloses a character (or characters) at point.
     */
    fun characterRangeAtPoint(point: SkikoPoint):SkikoTextRange {
        //todo default impl maybe redundant
        val closestPosition = closestPositionToPoint(point)
        return SkikoTextRange(closestPosition, closestPosition)
    }

    /**
     * Returns the position in a document that is closest to a specified point.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614523-closestposition
     * @param point A point in the view that is drawing a document’s text.
     * @return An object locating a position in a document that is closest to point.
     */
    fun closestPositionToPoint(point: SkikoPoint):Int

    /**
     * Returns the position in a document that is closest to a specified point in a specified range.
     * https://developer.apple.com/documentation/uikit/uitextinput/1614533-closestposition
     * @param point A point in the view that is drawing a document’s text.
     * @param range An object representing a range in a document’s text.
     * @return An object representing the character position in range that is closest to point.
     */
    fun closestPositionToPoint(point: SkikoPoint, range :SkikoTextRange):Int {
        return range.start//todo default impl maybe redundant
    }

}
