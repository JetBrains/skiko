import UIKit
import Foundation
import UIKit

public class MyTextField2: UIView {

    var textLayer = CATextLayer()
    var textStorage: String = "storage" {
        didSet {
            textLayer.string = textStorage
        }
    }

    public override init(frame: CGRect) {
        let initialPosition = MyTextPosition(offset: 0)
        _selectedTextRange = MyTextRange(from: initialPosition, to: initialPosition)
        super.init(frame: frame)
        commonSetup()
    }

    required init?(coder aDecoder: NSCoder) {
        let initialPosition = MyTextPosition(offset: 0)
        _selectedTextRange = MyTextRange(from: initialPosition, to: initialPosition)
        super.init(coder: aDecoder)
        commonSetup()
    }

    func commonSetup() {
        textLayer.isWrapped = true
        textLayer.fontSize = 12
        textLayer.alignmentMode = .center
        textLayer.foregroundColor = UIColor.magenta.cgColor
        textLayer.string = textStorage
        layer.addSublayer(textLayer)
    }

    public override var canBecomeFirstResponder: Bool {
        return true
    }

    public override func becomeFirstResponder() -> Bool {
        let success = super.becomeFirstResponder()
        if success {
            let initialPosition = MyTextPosition(offset: textStorage.count)
            _selectedTextRange = MyTextRange(from: initialPosition, to: initialPosition)
        }
        return success
    }

    public override func layoutSubviews() {
        super.layoutSubviews()
        textLayer.frame = bounds
    }

    // MARK: delegates
    public var inputDelegate: UITextInputDelegate?
    public lazy var tokenizer: UITextInputTokenizer = UITextInputStringTokenizer(textInput: self)

    // MARK: - ranges
    // we will update _selectedTextRange & _markedTextRange whenever string is changed

    public var _selectedTextRange: MyTextRange {
        didSet {
            print("\(textStorage), selected range change to \(_selectedTextRange)")
        }
    }
    public var selectedTextRange: UITextRange? {
        get {
            return _selectedTextRange
        }
        set {
            if let range = newValue as? MyTextRange {
                _selectedTextRange = range
            } else {
                fatalError()
            }
        }
    }

    public var _markedTextRange: MyTextRange? {
        didSet {
            if let range = _markedTextRange {
                print("\(textStorage), marked range change to \(range)")
            } else {
                print("\(textStorage), marked range cleared")
            }
        }
    }
    public var markedTextRange: UITextRange? {
        return _markedTextRange
    }

    public var _markedTextStyle: [NSAttributedString.Key : Any]?
}

extension MyTextField2: UITextInput {
    public func selectionRects(for range: UITextRange) -> [UITextSelectionRect] {
        []
    }

    public var markedTextStyle: [NSAttributedString.Key : Any]? {
        set {
            _markedTextStyle = newValue
        }
        get {
            return _markedTextStyle
        }
    }
// MARK: - basic
    public func insertText(_ text: String) {
        // insert operation takes effect on current focus point (marked or selected)
        print("\(textStorage), insertText: \(text)")
        // after insertion, marked range is always cleared, and length of selected range is always zero
        let rangeToReplace = _markedTextRange ?? _selectedTextRange
        let rangeStartIndex = rangeToReplace.startPosition.offset
        textStorage.replaceSubrange(rangeToReplace.fullRange(in: textStorage), with: text)

        _markedTextRange = nil
        let insertedPosition = MyTextPosition(offset: rangeStartIndex + text.count)
        _selectedTextRange = MyTextRange(from: insertedPosition, to: insertedPosition)
    }

    public func deleteBackward() {
        // deleteBackward operation takes effect on current focus point (marked or selected)
        print("\(textStorage), deleteBackward")

        // after backward deletion, marked range is always cleared, and length of selected range is always zero
        let rangeToDelete = _markedTextRange ?? _selectedTextRange
        var rangeStartPosition = rangeToDelete.startPosition
        var rangeStartIndex = rangeStartPosition.offset
        if rangeToDelete.isEmpty {
            if rangeStartIndex == 0 {
                return
            }
            rangeStartIndex -= 1
            textStorage.remove(at: textStorage.index(textStorage.startIndex, offsetBy: rangeStartIndex))
            rangeStartPosition = MyTextPosition(offset: rangeStartIndex)
        } else {
            textStorage.removeSubrange(rangeToDelete.fullRange(in: textStorage))
        }

        _markedTextRange = nil
        _selectedTextRange = MyTextRange(from: rangeStartPosition, to: rangeStartPosition)
    }

    public var hasText: Bool {
        return !textStorage.isEmpty
    }

    public func setMarkedText(_ string: String?, selectedRange: NSRange) {
        // setMarkedText operation takes effect on current focus point (marked or selected)
        print("\(textStorage), setMarkedText: \(string as Any), selection: \(selectedRange)")

        // after marked text is updated, old selection or markded range is replaced,
        // new marked range is always updated
        // and new selection is always changed to a new range with in

        let rangeToReplace = _markedTextRange ?? _selectedTextRange
        let rangeStartPosition = rangeToReplace.startPosition
        if let newString = string {
            textStorage.replaceSubrange(rangeToReplace.fullRange(in: textStorage), with: newString)
            let rangeStartIndex = rangeStartPosition.offset
            let swiftRange = Range(selectedRange, in: newString)!
            let swiftRangeOffset = newString.distance(from: newString.startIndex, to: swiftRange.lowerBound)
            let swiftRangeLength = newString.distance(from: swiftRange.lowerBound, to: swiftRange.upperBound)

            let selectionStartIndex = rangeStartIndex + swiftRangeOffset
            _markedTextRange = MyTextRange(from: rangeStartPosition, maxOffset: newString.count, in: textStorage)
            _selectedTextRange = MyTextRange(from: MyTextPosition(offset: selectionStartIndex),
                    to: MyTextPosition(offset: selectionStartIndex + swiftRangeLength))
        } else {
            textStorage.removeSubrange(rangeToReplace.fullRange(in: textStorage))
            _markedTextRange = nil
            _selectedTextRange = MyTextRange(from: rangeStartPosition, to: rangeStartPosition)
        }
    }

    public func unmarkText() {
        // unmarkText operation takes effect on current focus point (marked or selected)
        print("\(textStorage), unmarkText")

        // after unmark, marked range is cleared and selection range is at end of previously marked area
        if let previouslyMarkedRange = _markedTextRange {
            let rangeEndPosition = previouslyMarkedRange.endPosition
            _selectedTextRange = MyTextRange(from: rangeEndPosition, to: rangeEndPosition)
            _markedTextRange = nil
        }
    }

// MARK: - replacing text
    public func text(in range: UITextRange) -> String? {
        guard let myRange = range as? MyTextRange else {
            fatalError()
        }

//    print("asking for current range: \(myRange)")
        if range.isEmpty {
            return nil
        } else {
            return String(textStorage[myRange.fullRange(in: textStorage)])
        }
    }

    public func replace(_ range: UITextRange, withText text: String) {
        // replace operation takes effect on designated range
        guard let myRange = range as? MyTextRange else {
            fatalError()
        }

        guard _markedTextRange == nil else {
            fatalError("current logic relies on the assumption that when this method is called, there's no marked area")
        }
        print("\(textStorage), replacing range \(myRange) with text: \(text)")

        // save enough dat before string manipulation
        let insertionIndex = myRange.startPosition.offset

        // after replacement is fulfilled, selected range might change
        // if the replace range overlapses with selected range, selection is cleared
        textStorage.replaceSubrange(myRange.fullRange(in: textStorage), with: text)
        if myRange.endPosition.offset <= _selectedTextRange.startPosition.offset {
            // selected range should change
            let selectionOffset = _selectedTextRange.startPosition.offset - insertionIndex
            let newSelectionOffset = selectionOffset - myRange.length + text.count
            let newSelectionIndex = newSelectionOffset + insertionIndex
            _selectedTextRange = MyTextRange(from: MyTextPosition(offset: newSelectionIndex),
                    to: MyTextPosition(offset: newSelectionIndex + _selectedTextRange.length))
        } else if myRange.startPosition.offset >= _selectedTextRange.endPosition.offset {
            // do nothing
        } else {
            // has intersection
            let insertionEndPosition = MyTextPosition(offset: insertionIndex + text.count)
            _selectedTextRange = MyTextRange(from: insertionEndPosition, to: insertionEndPosition)
        }
    }

// MARK: - Computing Ranges and Positions
    public func textRange(from fromPosition: UITextPosition, to toPosition: UITextPosition) -> UITextRange? {
        guard let from = fromPosition as? MyTextPosition, let to = toPosition as? MyTextPosition else {
            fatalError()
        }
        print("[Geometry] form range [\(from) ..< \(to)]")
        return MyTextRange(from: from, to: to)
    }

    public func position(from position: UITextPosition, offset: Int) -> UITextPosition? {
        guard let from = position as? MyTextPosition else {
            fatalError()
        }
        print("[Geometry] form position \(from) + \(offset)")
        // sometimes the system may want to know off-the-one positions, we should just return boundary
        // if we return nil, a guarded fatal error will trigger somewhere else
        let newOffset = max(min(from.offset + offset, textStorage.count), 0)
        return MyTextPosition(offset: newOffset)
    }

    public func position(from position: UITextPosition, in direction: UITextLayoutDirection, offset: Int) -> UITextPosition? {
        return self.position(from: position, offset: offset)
    }

    public var beginningOfDocument: UITextPosition {
        return MyTextPosition(offset: 0)
    }

    public var endOfDocument: UITextPosition {
        return MyTextPosition(offset: textStorage.count)
    }

    public func compare(_ position: UITextPosition, to other: UITextPosition) -> ComparisonResult {
        guard let from = position as? MyTextPosition, let to = other as? MyTextPosition else {
            fatalError()
        }
        if from.offset < to.offset {
            return .orderedAscending
        }
        if from.offset > to.offset {
            return .orderedDescending
        }
        return .orderedSame
    }

    public func offset(from: UITextPosition, to toPosition: UITextPosition) -> Int {
        guard let from = from as? MyTextPosition, let to = toPosition as? MyTextPosition else {
            fatalError()
        }
//    print("[Geometry] form offset \(to) - \(from)")
        return to.offset - from.offset
    }

// MARK: - Geometry
    public func firstRect(for range: UITextRange) -> CGRect {
        return bounds
    }

    public func caretRect(for position: UITextPosition) -> CGRect {
        return bounds
    }

    public func closestPosition(to point: CGPoint) -> UITextPosition? {
        return MyTextPosition(offset: 0)
    }

    func selectionRects(for range: UITextRange) -> [Any] {
        guard let myRange = range as? MyTextRange else {
            fatalError()
        }
        return [MyTextSelectionRect(rect: bounds, range: myRange, string: textStorage)]
    }

    public func closestPosition(to point: CGPoint, within range: UITextRange) -> UITextPosition? {
        guard let myRange = range as? MyTextRange else {
            fatalError()
        }
        return myRange.startPosition
    }

    public func characterRange(at point: CGPoint) -> UITextRange? {
        return MyTextRange(from: MyTextPosition(offset: 0),
                to: MyTextPosition(offset: textStorage.count))
    }

// MARK: - Layout Direction
    public func position(within range: UITextRange, farthestIn direction: UITextLayoutDirection) -> UITextPosition? {
        return range.end
    }

    public func characterRange(byExtending position: UITextPosition, in direction: UITextLayoutDirection) -> UITextRange? {
        guard let myPosition = position as? MyTextPosition else {
            fatalError()
        }
        return MyTextRange(from: myPosition, to: MyTextPosition(offset: textStorage.count))
    }

    public func baseWritingDirection(for position: UITextPosition, in direction: UITextStorageDirection) -> UITextWritingDirection {
        return .leftToRight
    }

    public func setBaseWritingDirection(_ writingDirection: UITextWritingDirection, for range: UITextRange) {
        // do nothing
    }

// MARK: - Dictation
    public func dictationRecordingDidEnd() {
        print("\(textStorage), dictation recording end")
    }

    public func dictationRecognitionFailed() {
        print("\(textStorage), dictation failed")
    }

    public func insertDictationResult(_ dictationResult: [UIDictationPhrase]) {
        print("\(textStorage), insertDictationResult: \(dictationResult)")
        let text = dictationResult.map{$0.text}.joined()
        insertText(text)
    }

    // if we don't implement these 2 methods, a very special string will append to textStorage
    public var insertDictationResultPlaceholder: Any {
        return "[DICT]"
    }

    public func removeDictationResultPlaceholder(_ placeholder: Any, willInsertResult: Bool) {
        print("removeDictationResultPlaceholder")
    }

// MARK: - Optional

    public func position(within range: UITextRange, atCharacterOffset offset: Int) -> UITextPosition? {
        guard let myRange = range as? MyTextRange else {
            fatalError()
        }
        let endOffset = myRange.startPosition.offset + offset
        if endOffset > myRange.endPosition.offset {
            return nil
        }
        return MyTextPosition(offset: endOffset)
    }

    public func characterOffset(of position: UITextPosition, within range: UITextRange) -> Int {
        guard let myRange = range as? MyTextRange, let position = position as? MyTextPosition else {
            fatalError()
        }
        return position.offset - myRange.startPosition.offset
    }
}

class MyTextPosition: UITextPosition {
    let offset: Int

    init(offset: Int) {
        self.offset = offset
    }
}

extension MyTextPosition {
    override var description: String {
        return "\(offset)"
    }
}

public class MyTextRange: UITextRange {
    let startPosition: MyTextPosition
    let endPosition: MyTextPosition

    // from may be larger than to
    // from and to must each contain a valid indices
    init(from: MyTextPosition, to: MyTextPosition) {
        let start, end: MyTextPosition
        if from.offset < to.offset {
            start = from
            end = to
        } else {
            start = to
            end = from
        }
        self.startPosition = start
        self.endPosition = end
    }

    // maxLength may be negative
    // from must contain a valid index
    init(from: MyTextPosition, maxOffset: Int, in baseString: String) {
        if maxOffset >= 0 {
            self.startPosition = from
            let end = min(baseString.count, from.offset + maxOffset)
            self.endPosition = MyTextPosition(offset: end)
        } else {
            self.endPosition = from
            let begin = max(0, from.offset + maxOffset)
            self.startPosition = MyTextPosition(offset: begin)
        }
    }

    public override var start: UITextPosition {
        return startPosition
    }

    public override var end: UITextPosition {
        return endPosition
    }

    public override var isEmpty: Bool {
        return startPosition.offset >= endPosition.offset
    }

    func fullRange(in baseString: String) -> Range<String.Index> {
        let beginIndex = baseString.index(baseString.startIndex, offsetBy: startPosition.offset)
        let endIndex = baseString.index(beginIndex, offsetBy: endPosition.offset - startPosition.offset)
        return beginIndex..<endIndex
    }

    var length: Int {
        return endPosition.offset - startPosition.offset
    }
}

extension MyTextRange {
    public override var description: String {
        return "[\(startPosition.offset) ..< \(endPosition.offset)]"
    }
}

public class MyTextSelectionRect: UITextSelectionRect {
    let _rect: CGRect
    let _containsStart: Bool
    let _containsEnd: Bool

    public override var writingDirection: UITextWritingDirection {
        return .leftToRight
    }

    public override var isVertical: Bool {
        return false
    }

    public override var rect: CGRect {
        return _rect
    }

    public override var containsStart: Bool {
        return _containsStart
    }

    public override var containsEnd: Bool {
        return _containsEnd
    }

    init(rect: CGRect, range: MyTextRange, string: String) {
        _rect = rect
        _containsStart = range.startPosition.offset == 0
        _containsEnd = range.endPosition.offset == string.count
    }
}
