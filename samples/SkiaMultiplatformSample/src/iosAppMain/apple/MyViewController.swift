
import Foundation
import UIKit

class MyViewController:UIViewController {
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }

    override func loadView() {
        let textField = MyTextField(frame: CGRect(x: 10, y: 10, width: 200, height: 50))
        textField.text = "Hello TextField"
        textField.contentVerticalAlignment = .top
        self.view = textField

    }
}

class MyTextField:UITextField {
    //UIKeyInput
    override var hasText: Bool {
        super.hasText
    }

    override func insertText(_ text: String) {
        super.insertText(text)
    }

    override func deleteBackward() {
        super.deleteBackward()
    }

    //UITextInput
    override func text(in range: UITextRange) -> String? {
        super.text(in: range)
    }

    override func replace(_ range: UITextRange, withText text: String) {
        super.replace(range, withText: text)
    }

    override var selectedTextRange: UITextRange? {
        get {
            super.selectedTextRange
        }
        set {
            super.selectedTextRange = newValue
        }
    }
    override var markedTextRange: UITextRange? {
        super.markedTextRange
    }
    override var markedTextStyle: [NSAttributedString.Key: Any]? {
        get {
            super.markedTextStyle
        }
        set {
            super.markedTextStyle = newValue
        }
    }

    override func setMarkedText(_ markedText: String?, selectedRange: NSRange) {
        super.setMarkedText(markedText, selectedRange: selectedRange)
    }

    override func unmarkText() {
        super.unmarkText()
    }

    override var beginningOfDocument: UITextPosition {
        super.beginningOfDocument
    }
    override var endOfDocument: UITextPosition {
        super.endOfDocument
    }

    override func textRange(from fromPosition: UITextPosition, to toPosition: UITextPosition) -> UITextRange? {
        super.textRange(from: fromPosition, to: toPosition)
    }

    override func position(from position: UITextPosition, offset: Int) -> UITextPosition? {
        super.position(from: position, offset: offset)
    }

    override func position(from position: UITextPosition, in direction: UITextLayoutDirection, offset: Int) -> UITextPosition? {
        super.position(from: position, in: direction, offset: offset)
    }

    override func compare(_ position: UITextPosition, to other: UITextPosition) -> ComparisonResult {
        super.compare(position, to: other)
    }

    override func offset(from: UITextPosition, to toPosition: UITextPosition) -> Int {
        super.offset(from: from, to: toPosition)
    }

    override var inputDelegate: UITextInputDelegate? {
        get {
            super.inputDelegate
        }
        set {
            super.inputDelegate = newValue
        }
    }
    override var tokenizer: UITextInputTokenizer {
        super.tokenizer
    }

    override func position(within range: UITextRange, farthestIn direction: UITextLayoutDirection) -> UITextPosition? {
        super.position(within: range, farthestIn: direction)
    }

    override func characterRange(byExtending position: UITextPosition, in direction: UITextLayoutDirection) -> UITextRange? {
        super.characterRange(byExtending: position, in: direction)
    }

    override func baseWritingDirection(for position: UITextPosition, in direction: UITextStorageDirection) -> NSWritingDirection {
        super.baseWritingDirection(for: position, in: direction)
    }

    override func setBaseWritingDirection(_ writingDirection: NSWritingDirection, for range: UITextRange) {
        super.setBaseWritingDirection(writingDirection, for: range)
    }

    override func firstRect(for range: UITextRange) -> CGRect {
        super.firstRect(for: range)
    }

    override func caretRect(for position: UITextPosition) -> CGRect {
        super.caretRect(for: position)
    }

    override func selectionRects(for range: UITextRange) -> [UITextSelectionRect] {
        super.selectionRects(for: range)
    }

    override func closestPosition(to point: CGPoint) -> UITextPosition? {
        super.closestPosition(to: point)
    }

    override func closestPosition(to point: CGPoint, within range: UITextRange) -> UITextPosition? {
        super.closestPosition(to: point, within: range)
    }

    override func characterRange(at point: CGPoint) -> UITextRange? {
        super.characterRange(at: point)
    }

    override func shouldChangeText(in range: UITextRange, replacementText text: String) -> Bool {
        super.shouldChangeText(in: range, replacementText: text)
    }

    override func textStyling(at position: UITextPosition, in direction: UITextStorageDirection) -> [NSAttributedString.Key: Any]? {
        super.textStyling(at: position, in: direction)
    }

    override func position(within range: UITextRange, atCharacterOffset offset: Int) -> UITextPosition? {
        super.position(within: range, atCharacterOffset: offset)
    }

    override func characterOffset(of position: UITextPosition, within range: UITextRange) -> Int {
        super.characterOffset(of: position, within: range)
    }

    override var textInputView: UIView {
        super.textInputView
    }
    override var selectionAffinity: UITextStorageDirection {
        get {
            super.selectionAffinity
        }
        set {
            super.selectionAffinity = newValue
        }
    }

    override func insertDictationResult(_ dictationResult: [UIDictationPhrase]) {
        super.insertDictationResult(dictationResult)
    }

    override func dictationRecordingDidEnd() {
        super.dictationRecordingDidEnd()
    }

    override func dictationRecognitionFailed() {
        super.dictationRecognitionFailed()
    }

    override var insertDictationResultPlaceholder: Any {
        super.insertDictationResultPlaceholder
    }

    override func frame(forDictationResultPlaceholder placeholder: Any) -> CGRect {
        super.frame(forDictationResultPlaceholder: placeholder)
    }

    override func removeDictationResultPlaceholder(_ placeholder: Any, willInsertResult: Bool) {
        super.removeDictationResultPlaceholder(placeholder, willInsertResult: willInsertResult)
    }

    override func insertText(_ text: String, alternatives: [String], style: UITextAlternativeStyle) {
        super.insertText(text, alternatives: alternatives, style: style)
    }

    override func setAttributedMarkedText(_ markedText: NSAttributedString?, selectedRange: NSRange) {
        super.setAttributedMarkedText(markedText, selectedRange: selectedRange)
    }

    override func insertTextPlaceholder(with size: CGSize) -> UITextPlaceholder {
        super.insertTextPlaceholder(with: size)
    }

    override func remove(_ textPlaceholder: UITextPlaceholder) {
        super.remove(textPlaceholder)
    }

    override func beginFloatingCursor(at point: CGPoint) {
        super.beginFloatingCursor(at: point)
    }

    override func updateFloatingCursor(at point: CGPoint) {
        super.updateFloatingCursor(at: point)
    }

    override func endFloatingCursor() {
        super.endFloatingCursor()
    }


}
