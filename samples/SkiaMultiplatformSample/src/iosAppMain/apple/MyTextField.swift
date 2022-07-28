import Foundation
import UIKit

class MyTextField: UITextField {
    //UIKeyInput
    override var hasText: Bool {
        super.hasText
    }

    override func insertText(_ text: String) {
        print("MyTextField insertText, text: \(text)")
        super.insertText(text)
    }

    override func deleteBackward() {
        super.deleteBackward()
    }

    //UITextInput

    override func text(in range: UITextRange) -> String? {//todo
//        print("MyTextField func text, range: \(range)")
        return super.text(in: range)
    }

    override func replace(_ range: UITextRange, withText text: String) {
        print("MyTextField func replace, range: \(range), text: \(text)")
        super.replace(range, withText: text)
    }

    override var selectedTextRange: UITextRange? {
        get {
            let result: UITextRange? = super.selectedTextRange
            return result
        }
        set {
            super.selectedTextRange = newValue
        }
    }
    override var markedTextRange: UITextRange? {
        let result: UITextRange? = super.markedTextRange
        if (result != nil) {
            print("MyTextField markedTextRange, result: \(result)")
        }
        return result
    }
    override var markedTextStyle: [NSAttributedString.Key: Any]? {
        get {
            print("MyTextField get markedTextStyle")
            return super.markedTextStyle
        }
        set {
            print("MyTextField set markedTextStyle")
            super.markedTextStyle = newValue
        }
    }

    override func setMarkedText(_ markedText: String?, selectedRange: NSRange) {
        print("MyTextField setMarkedText")
        super.setMarkedText(markedText, selectedRange: selectedRange)
    }

    override func unmarkText() {
        print("MyTextField unmarkText")
        super.unmarkText()
    }

    override var beginningOfDocument: UITextPosition {
        return super.beginningOfDocument
    }
    override var endOfDocument: UITextPosition {
        return super.endOfDocument
    }

    override func textRange(from fromPosition: UITextPosition, to toPosition: UITextPosition) -> UITextRange? {//todo
//        print("MyTextField func textRange(from fromPosition: UITextPosition, to toPosition: UITextPosition) -> UITextRange? {")
        return super.textRange(from: fromPosition, to: toPosition)
    }

    override func position(from position: UITextPosition, offset: Int) -> UITextPosition? {
        print("MyTextField func position(from position: UITextPosition, offset: Int) -> UITextPosition? {")
        return super.position(from: position, offset: offset)
    }

    override func position(from position: UITextPosition, in direction: UITextLayoutDirection, offset: Int) -> UITextPosition? {
        print("MyTextField func position(from position: UITextPosition, in direction: UITextLayoutDirection, offset: Int) -> UITextPosition? {")
        return super.position(from: position, in: direction, offset: offset)
    }

    override func compare(_ position: UITextPosition, to other: UITextPosition) -> ComparisonResult {
        return super.compare(position, to: other)
    }

    override func offset(from: UITextPosition, to toPosition: UITextPosition) -> Int {//todo
        return super.offset(from: from, to: toPosition)
    }

    override var inputDelegate: UITextInputDelegate? {
        get {
            let result: UITextInputDelegate? = super.inputDelegate
            print("MyTextField get inputDelegate, result: \(result)")
            return result
        }
        set {
            print("MyTextField set inputDelegate")
            super.inputDelegate = newValue
        }
    }
    override var tokenizer: UITextInputTokenizer {
        return UITextInputStringTokenizer()
        let result: UITextInputTokenizer = super.tokenizer
        print("MyTextField tokenizer, result: \(result)")
        return result
    }

    override func position(within range: UITextRange, farthestIn direction: UITextLayoutDirection) -> UITextPosition? {//todo
        let result: UITextPosition? = super.position(within: range, farthestIn: direction)
        print("MyTextField func position range: \(range), direction: \(direction), result: \(result)")
        return result
    }

    override func characterRange(byExtending position: UITextPosition, in direction: UITextLayoutDirection) -> UITextRange? {
        print("MyTextField func characterRange(byExtending position: UITextPosition, in direction: UITextLayoutDirection) -> UITextRange? {")
        return super.characterRange(byExtending: position, in: direction)
    }

    override func baseWritingDirection(for position: UITextPosition, in direction: UITextStorageDirection) -> NSWritingDirection {
        print("MyTextField func baseWritingDirection(for position: UITextPosition, in direction: UITextStorageDirection) -> NSWritingDirection {")
        return super.baseWritingDirection(for: position, in: direction)
    }

    override func setBaseWritingDirection(_ writingDirection: NSWritingDirection, for range: UITextRange) {
        print("MyTextField func setBaseWritingDirection(_ writingDirection: NSWritingDirection, for range: UITextRange) {")
        return super.setBaseWritingDirection(writingDirection, for: range)
    }

    override func firstRect(for range: UITextRange) -> CGRect {//todo
        let result: CGRect = super.firstRect(for: range)
        print("MyTextField func firstRect, range: \(range), result: \(result)")
        return result
    }

    override func caretRect(for position: UITextPosition) -> CGRect {
        return CGRect.zero
        print("MyTextField func caretRect(for position: UITextPosition) -> CGRect {")
        return super.caretRect(for: position)
    }

    override func selectionRects(for range: UITextRange) -> [UITextSelectionRect] {
        print("MyTextField func selectionRects(for range: UITextRange) -> [UITextSelectionRect] {")
        return super.selectionRects(for: range)
    }

    override func closestPosition(to point: CGPoint) -> UITextPosition? {
        return nil
        print("MyTextField func closestPosition(to point: CGPoint) -> UITextPosition? {")
        return super.closestPosition(to: point)
    }

    override func closestPosition(to point: CGPoint, within range: UITextRange) -> UITextPosition? {
        return nil
        print("MyTextField func closestPosition(to point: CGPoint, within range: UITextRange) -> UITextPosition? {")
        return super.closestPosition(to: point, within: range)
    }

    override func characterRange(at point: CGPoint) -> UITextRange? {
        print("MyTextField func characterRange(at point: CGPoint) -> UITextRange? {")
        return super.characterRange(at: point)
    }

    override func shouldChangeText(in range: UITextRange, replacementText text: String) -> Bool {
        print("MyTextField func shouldChangeText(in range: UITextRange, replacementText text: String) -> Bool {")
        return super.shouldChangeText(in: range, replacementText: text)
    }

    override func textStyling(at position: UITextPosition, in direction: UITextStorageDirection) -> [NSAttributedString.Key: Any]? {
        print("MyTextField func textStyling(at position: UITextPosition, in direction: UITextStorageDirection) -> [NSAttributedString.Key: Any]? {")
        return super.textStyling(at: position, in: direction)
    }

    override func position(within range: UITextRange, atCharacterOffset offset: Int) -> UITextPosition? {
        print("MyTextField func position(within range: UITextRange, atCharacterOffset offset: Int) -> UITextPosition? {")
        return super.position(within: range, atCharacterOffset: offset)
    }

    override func characterOffset(of position: UITextPosition, within range: UITextRange) -> Int {
        print("MyTextField func characterOffset(of position: UITextPosition, within range: UITextRange) -> Int {")
        return super.characterOffset(of: position, within: range)
    }

    override var textInputView: UIView {
        self
    }
    override var selectionAffinity: UITextStorageDirection {
        get {
            print("MyTextField get selectionAffinity")
            return super.selectionAffinity
        }
        set {
            print("MyTextField set selectionAffinity")
            super.selectionAffinity = newValue
        }
    }

    override func insertDictationResult(_ dictationResult: [UIDictationPhrase]) {
        print("MyTextField func insertDictationResult(_ dictationResult: [UIDictationPhrase]) {")
        super.insertDictationResult(dictationResult)
    }

    override func dictationRecordingDidEnd() {
        print("MyTextField func dictationRecordingDidEnd() {")
        super.dictationRecordingDidEnd()
    }

    override func dictationRecognitionFailed() {
        print("MyTextField func dictationRecognitionFailed() {")
        super.dictationRecognitionFailed()
    }

    override var insertDictationResultPlaceholder: Any {
        print("MyTextField var insertDictationResultPlaceholder: Any {")
        return super.insertDictationResultPlaceholder
    }

    override func frame(forDictationResultPlaceholder placeholder: Any) -> CGRect {
        print("MyTextField func frame(forDictationResultPlaceholder placeholder: Any) -> CGRect {")
        return super.frame(forDictationResultPlaceholder: placeholder)
    }

    override func removeDictationResultPlaceholder(_ placeholder: Any, willInsertResult: Bool) {
        print("MyTextField func removeDictationResultPlaceholder(_ placeholder: Any, willInsertResult: Bool) {")
        super.removeDictationResultPlaceholder(placeholder, willInsertResult: willInsertResult)
    }

    override func insertText(_ text: String, alternatives: [String], style: UITextAlternativeStyle) {
        print("MyTextField func insertText(_ text: String, alternatives: [String], style: UITextAlternativeStyle) {")
        super.insertText(text, alternatives: alternatives, style: style)
    }

    override func setAttributedMarkedText(_ markedText: NSAttributedString?, selectedRange: NSRange) {
        print("MyTextField func setAttributedMarkedText(_ markedText: NSAttributedString?, selectedRange: NSRange) {")
        super.setAttributedMarkedText(markedText, selectedRange: selectedRange)
    }

    override func insertTextPlaceholder(with size: CGSize) -> UITextPlaceholder {
        print("MyTextField func insertTextPlaceholder(with size: CGSize) -> UITextPlaceholder {")
        return super.insertTextPlaceholder(with: size)
    }

    override func remove(_ textPlaceholder: UITextPlaceholder) {
        print("MyTextField func remove(_ textPlaceholder: UITextPlaceholder) {")
        super.remove(textPlaceholder)
    }

    override func beginFloatingCursor(at point: CGPoint) {
        print("MyTextField func beginFloatingCursor(at point: CGPoint) {")
        super.beginFloatingCursor(at: point)
    }

    override func updateFloatingCursor(at point: CGPoint) {
        print("MyTextField func updateFloatingCursor(at point: CGPoint) {")
        super.updateFloatingCursor(at: point)
    }

    override func endFloatingCursor() {
        print("MyTextField func endFloatingCursor() {")
        super.endFloatingCursor()
    }

}
