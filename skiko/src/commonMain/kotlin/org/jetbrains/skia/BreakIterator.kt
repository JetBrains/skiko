package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withResult

/**
 *
 * A class that locates boundaries in text.  This class defines a protocol for
 * objects that break up a piece of natural-language text according to a set
 * of criteria.  Instances or subclasses of BreakIterator can be provided, for
 * example, to break a piece of text into words, sentences, or logical characters
 * according to the conventions of some language or group of languages.
 *
 * We provide four built-in types of BreakIterator:
 *
 *  * makeSentenceInstance() returns a BreakIterator that locates boundaries
 * between sentences.  This is useful for triple-click selection, for example.
 *  * makeWordInstance() returns a BreakIterator that locates boundaries between
 * words.  This is useful for double-click selection or "find whole words" searches.
 * This type of BreakIterator makes sure there is a boundary position at the
 * beginning and end of each legal word.  (Numbers count as words, too.)  Whitespace
 * and punctuation are kept separate from real words.
 *  * makeLineInstance() returns a BreakIterator that locates positions where it is
 * legal for a text editor to wrap lines.  This is similar to word breaking, but
 * not the same: punctuation and whitespace are generally kept with words (you don't
 * want a line to start with whitespace, for example), and some special characters
 * can force a position to be considered a line-break position or prevent a position
 * from being a line-break position.
 *  * makeCharacterInstance() returns a BreakIterator that locates boundaries between
 * logical characters.  Because of the structure of the Unicode encoding, a logical
 * character may be stored internally as more than one Unicode code point.  (A with an
 * umlaut may be stored as an a followed by a separate combining umlaut character,
 * for example, but the user still thinks of it as one character.)  This iterator allows
 * various processes (especially text editors) to treat as characters the units of text
 * that a user would think of as characters, rather than the units of text that the
 * computer sees as "characters".
 * The text boundary positions are found according to the rules
 * described in Unicode Standard Annex #29, Text Boundaries, and
 * Unicode Standard Annex #14, Line Breaking Properties.  These
 * are available at http://www.unicode.org/reports/tr14/ and
 * http://www.unicode.org/reports/tr29/.
 *
 *
 * BreakIterator's interface follows an "iterator" model (hence the name), meaning it
 * has a concept of a "current position" and methods like first(), last(), next(),
 * and previous() that update the current position.  All BreakIterators uphold the
 * following invariants:
 *  * The beginning and end of the text are always treated as boundary positions.
 *  * The current position of the iterator is always a boundary position (random-
 * access methods move the iterator to the nearest boundary position before or
 * after the specified position, not _to_ the specified position).
 *  * DONE is used as a flag to indicate when iteration has stopped.  DONE is only
 * returned when the current position is the end of the text and the user calls next(),
 * or when the current position is the beginning of the text and the user calls
 * previous().
 *  * Break positions are numbered by the positions of the characters that follow
 * them.  Thus, under normal circumstances, the position before the first character
 * is 0, the position after the first character is 1, and the position after the
 * last character is 1 plus the length of the string.
 *  * The client can change the position of an iterator, or the text it analyzes,
 * at will, but cannot change the behavior.  If the user wants different behavior, he
 * must instantiate a new iterator.
 *
 * BreakIterator accesses the text it analyzes through a CharacterIterator, which makes
 * it possible to use BreakIterator to analyze text in any text-storage vehicle that
 * provides a CharacterIterator interface.
 *
 * **Note:**  Some types of BreakIterator can take a long time to create, and
 * instances of BreakIterator are not currently cached by the system.  For
 * optimal performance, keep instances of BreakIterator around as long as makes
 * sense.  For example, when word-wrapping a document, don't create and destroy a
 * new BreakIterator for each line.  Create one break iterator for the whole document
 * (or whatever stretch of text you're wrapping) and use it to do the whole job of
 * wrapping the text.
 *
 * <P>
 * **Examples**:</P><P>
 * Creating and using text boundaries
</P> * <blockquote>
 * <pre>
 * public static void main(String args[]) {
 * if (args.length == 1) {
 * String stringToExamine = args[0];
 * //print each word in order
 * BreakIterator boundary = BreakIterator.makeWordInstance();
 * boundary.setText(stringToExamine);
 * printEachForward(boundary, stringToExamine);
 * //print each sentence in reverse order
 * boundary = BreakIterator.makeSentenceInstance(Locale.US);
 * boundary.setText(stringToExamine);
 * printEachBackward(boundary, stringToExamine);
 * printFirst(boundary, stringToExamine);
 * printLast(boundary, stringToExamine);
 * }
 * }
</pre> *
</blockquote> *
 *
 * Print each element in order
 * <blockquote>
 * <pre>
 * public static void printEachForward(BreakIterator boundary, String source) {
 * int start = boundary.first();
 * for (int end = boundary.next();
 * end != BreakIterator.DONE;
 * start = end, end = boundary.next()) {
 * System.out.println(source.substring(start,end));
 * }
 * }
</pre> *
</blockquote> *
 *
 * Print each element in reverse order
 * <blockquote>
 * <pre>
 * public static void printEachBackward(BreakIterator boundary, String source) {
 * int end = boundary.last();
 * for (int start = boundary.previous();
 * start != BreakIterator.DONE;
 * end = start, start = boundary.previous()) {
 * System.out.println(source.substring(start,end));
 * }
 * }
</pre> *
</blockquote> *
 *
 * Print first element
 * <blockquote>
 * <pre>
 * public static void printFirst(BreakIterator boundary, String source) {
 * int start = boundary.first();
 * int end = boundary.next();
 * System.out.println(source.substring(start,end));
 * }
</pre> *
</blockquote> *
 *
 * Print last element
 * <blockquote>
 * <pre>
 * public static void printLast(BreakIterator boundary, String source) {
 * int end = boundary.last();
 * int start = boundary.previous();
 * System.out.println(source.substring(start,end));
 * }
</pre> *
</blockquote> *
 *
 * Print the element at a specified position
 * <blockquote>
 * <pre>
 * public static void printAt(BreakIterator boundary, int pos, String source) {
 * int end = boundary.following(pos);
 * int start = boundary.previous();
 * System.out.println(source.substring(start,end));
 * }
</pre> *
</blockquote> *
 *
 * Find the next word
 * <blockquote>
 * <pre>
 * public static int nextWordStartAfter(int pos, String text) {
 * BreakIterator wb = BreakIterator.makeWordInstance();
 * wb.setText(text);
 * int wordStart = wb.following(pos);
 * for (;;) {
 * int wordLimit = wb.next();
 * if (wordLimit == BreakIterator.DONE) {
 * return BreakIterator.DONE;
 * }
 * int wordStatus = wb.getRuleStatus();
 * if (wordStatus != BreakIterator.WORD_NONE) {
 * return wordStart;
 * }
 * wordStart = wordLimit;
 * }
 * }
</pre> *
 * The iterator returned by [.makeWordInstance] is unique in that
 * the break positions it returns don't represent both the start and end of the
 * thing being iterated over.  That is, a sentence-break iterator returns breaks
 * that each represent the end of one sentence and the beginning of the next.
 * With the word-break iterator, the characters between two boundaries might be a
 * word, or they might be the punctuation or whitespace between two words.  The
 * above code uses [.getRuleStatus] to identify and ignore boundaries associated
 * with punctuation or other non-word characters.
</blockquote> *
 */
class BreakIterator internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        /**
         * DONE is returned by previous() and next() after all valid
         * boundaries have been returned.
         */
        const val DONE = -1

        /**
         * Tag value for "words" that do not fit into any of other categories.
         * Includes spaces and most punctuation.
         */
        const val WORD_NONE = 0

        /**
         * Upper bound for tags for uncategorized words.
         */
        const val WORD_NONE_LIMIT = 100

        /**
         * Tag value for words that appear to be numbers, lower limit.
         */
        const val WORD_NUMBER = 100

        /**
         * Tag value for words that appear to be numbers, upper limit.
         */
        const val WORD_NUMBER_LIMIT = 200

        /**
         * Tag value for words that contain letters, excluding
         * hiragana, katakana or ideographic characters, lower limit.
         */
        const val WORD_LETTER = 200

        /**
         * Tag value for words containing letters, upper limit
         */
        const val WORD_LETTER_LIMIT = 300

        /**
         * Tag value for words containing kana characters, lower limit
         */
        const val WORD_KANA = 300

        /**
         * Tag value for words containing kana characters, upper limit
         */
        const val WORD_KANA_LIMIT = 400

        /**
         * Tag value for words containing ideographic characters, lower limit
         */
        const val WORD_IDEO = 400

        /**
         * Tag value for words containing ideographic characters, upper limit
         */
        const val WORD_IDEO_LIMIT = 500
        /**
         * Returns a new BreakIterator instance for character breaks for the given locale.
         */
        /**
         * Returns a new BreakIterator instance for character breaks for the default locale.
         */
        fun makeCharacterInstance(locale: String? = null): BreakIterator {
            Stats.onNativeCall()
            return BreakIterator(withErrorGuard("Failed to create character iterator") { _nMake(0, toInterop(locale), it)  }) // UBRK_CHARACTER
        }
        /**
         * Returns a new BreakIterator instance for word breaks for the given locale.
         */
        /**
         * Returns a new BreakIterator instance for word breaks for the default locale.
         */
        fun makeWordInstance(locale: String? = null): BreakIterator {
            Stats.onNativeCall()
            return BreakIterator(withErrorGuard("Failed to create word iterator") { _nMake(1, toInterop(locale), it) }) // UBRK_WORD
        }
        /**
         * Returns a new BreakIterator instance for line breaks for the given locale.
         */
        /**
         * Returns a new BreakIterator instance for line breaks for the default locale.
         */
        fun makeLineInstance(locale: String? = null): BreakIterator {
            Stats.onNativeCall()
            return BreakIterator(withErrorGuard("Failed to create line iterator")  { _nMake(2, toInterop(locale), it) }) // UBRK_LINE
        }
        /**
         * Returns a new BreakIterator instance for sentence breaks for the given locale.
         */
        /**
         * Returns a new BreakIterator instance for sentence breaks for the default locale.
         */
        fun makeSentenceInstance(locale: String? = null): BreakIterator {
            Stats.onNativeCall()
            return BreakIterator(withErrorGuard("Failed to create sentence iterator")  {
                _nMake(3, toInterop(locale), it)
            }) // UBRK_SENTENCE
        }

        init {
            staticLoad()
        }
    }

    internal var _text: U16String? = null
    override fun close() {
        super.close()
        _text?.close()
    }

    /**
     * Create a copy of this iterator
     */
    fun clone(): BreakIterator {
        Stats.onNativeCall()
        return BreakIterator(withErrorGuard("Failed to clone") { _nClone(_ptr, it) })
    }

    /**
     * Returns character index of the text boundary that was most recently
     * returned by [], [], [],
     * [], [], [] or
     * []. If any of these methods returns
     * [BreakIterator.DONE] because either first or last text boundary
     * has been reached, it returns the first or last text boundary depending
     * on which one is reached.
     */
    fun current(): Int {
        return try {
            Stats.onNativeCall()
            _nCurrent(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns the boundary following the current boundary. If the current
     * boundary is the last text boundary, it returns [BreakIterator.DONE]
     * and the iterator's current position is unchanged. Otherwise, the
     * iterator's current position is set to the boundary following the current
     * boundary.
     */
    operator fun next(): Int {
        return try {
            Stats.onNativeCall()
            _nNext(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Advances the iterator either forward or backward the specified number of steps.
     * Negative values move backward, and positive values move forward.  This is
     * equivalent to repeatedly calling next() or previous().
     * @param n The number of steps to move.  The sign indicates the direction
     * (negative is backwards, and positive is forwards).
     * @return The character offset of the boundary position n boundaries away from
     * the current one.
     */
    fun next(index: Int): Int {
        var n = index
        var result = 0
        if (n > 0) {
            while (n > 0 && result != DONE) {
                result = next()
                --n
            }
        } else if (n < 0) {
            while (n < 0 && result != DONE) {
                result = previous()
                ++n
            }
        } else {
            result = current()
        }
        return result
    }

    /**
     * Returns the boundary following the current boundary. If the current
     * boundary is the last text boundary, it returns [BreakIterator.DONE]
     * and the iterator's current position is unchanged. Otherwise, the
     * iterator's current position is set to the boundary following the current
     * boundary.
     */
    fun previous(): Int {
        return try {
            Stats.onNativeCall()
            _nPrevious(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns the first boundary. The iterator's current position is set to the first text boundary.
     */
    fun first(): Int {
        return try {
            Stats.onNativeCall()
            _nFirst(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns the last boundary. The iterator's current position is set to the last text boundary.
     */
    fun last(): Int {
        return try {
            Stats.onNativeCall()
            _nLast(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns the last boundary preceding the specified character offset.
     * If the specified offset is equal to the first text boundary, it returns
     * [BreakIterator.DONE] and the iterator's current position is
     * unchanged. Otherwise, the iterator's current position is set to the
     * returned boundary. The value returned is always less than the offset or
     * the value [BreakIterator.DONE].
     */
    fun preceding(offset: Int): Int {
        return try {
            Stats.onNativeCall()
            _nPreceding(_ptr, offset)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns the first boundary following the specified character offset.
     * If the specified offset is equal to the last text boundary, it returns
     * [BreakIterator.DONE] and the iterator's current position is
     * unchanged. Otherwise, the iterator's current position is set to the
     * returned boundary. The value returned is always greater than the offset or
     * the value [BreakIterator.DONE].
     */
    fun following(offset: Int): Int {
        return try {
            Stats.onNativeCall()
            _nFollowing(_ptr, offset)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * Returns true if the specified character offset is a text boundary.
     */
    fun isBoundary(offset: Int): Boolean {
        return try {
            Stats.onNativeCall()
            _nIsBoundary(_ptr, offset)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * For rule-based BreakIterators, return the status tag from the
     * break rule that determined the boundary at the current iteration position.
     *
     *
     * For break iterator types that do not support a rule status,
     * a default value of 0 is returned.
     *
     *
     * @return The status from the break rule that determined the boundary
     * at the current iteration position.
     */
    val ruleStatus: Int
        get() = try {
            Stats.onNativeCall()
            _nGetRuleStatus(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * For RuleBasedBreakIterators, get the status (tag) values from the break rule(s)
     * that determined the the boundary at the current iteration position.
     *
     *
     * For break iterator types that do not support rule status,
     * no values are returned.
     *
     * @return  an array with the status values.
     */
    val ruleStatuses: IntArray
        get() = try {
            Stats.onNativeCall()
            val arrayLen = _nGetRuleStatusesLen(_ptr)
            withResult(IntArray(arrayLen)) { result ->
                _nGetRuleStatuses(_ptr, result, arrayLen)
            }
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Set a new text string to be scanned. The current scan position is reset to [].
     */
    fun setText(text: String?) {
        try {
            Stats.onNativeCall()
            _text?.close()
            _text = U16String(withErrorGuard("Failed to setText") {
                _nSetText(
                    _ptr,
                    toInterop(text?.let { ShortArray(text.length) { text[it].code.toShort() } }),
                    text?.length ?: 0,
                    it
                )
            })
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(_text)
        }
    }

    private object _FinalizerHolder {
        val PTR = BreakIterator_nGetFinalizer()
    }
}

private fun withErrorGuard(message: String, block: InteropScope.(InteropPointer) -> NativePointer): NativePointer {
    val errorCode = IntArray(1)
    return interopScope {
        val handle = toInterop(errorCode)
        val res = block.invoke(this, handle)
        handle.fromInterop(errorCode)
        if (errorCode[0] > 0) {
            throw RuntimeException("$message; operation failed with status ${errorCode}")
        }
        if (res == NullPointer) {
            throw IllegalArgumentException(message)
        }
        res
    }
}

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nGetFinalizer")
private external fun BreakIterator_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nMake")
private external fun _nMake(type: Int, locale: InteropPointer, errorCode: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nClone")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nClone")
private external fun _nClone(ptr: NativePointer, errorCode: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nCurrent")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nCurrent")
private external fun _nCurrent(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nNext")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nNext")
private external fun _nNext(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nPrevious")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nPrevious")
private external fun _nPrevious(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nFirst")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nFirst")
private external fun _nFirst(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nLast")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nLast")
private external fun _nLast(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nPreceding")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nPreceding")
private external fun _nPreceding(ptr: NativePointer, offset: Int): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nFollowing")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nFollowing")
private external fun _nFollowing(ptr: NativePointer, offset: Int): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nIsBoundary")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nIsBoundary")
private external fun _nIsBoundary(ptr: NativePointer, offset: Int): Boolean

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nGetRuleStatus")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nGetRuleStatus")
private external fun _nGetRuleStatus(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nGetRuleStatusesLen")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nGetRuleStatusesLen")
private external fun _nGetRuleStatusesLen(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nGetRuleStatuses")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nGetRuleStatuses")
private external fun _nGetRuleStatuses(ptr: NativePointer, result: InteropPointer, len: Int)

@ExternalSymbolName("org_jetbrains_skia_BreakIterator__1nSetText")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_BreakIterator__1nSetText")
private external fun _nSetText(ptr: NativePointer, textStr: InteropPointer, len: Int, errorCode: InteropPointer): NativePointer
