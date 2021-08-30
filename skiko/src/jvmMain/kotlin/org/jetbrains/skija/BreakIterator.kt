package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Managed.CleanerThunk
import org.jetbrains.skija.paragraph.Shadow
import org.jetbrains.skija.paragraph.TextBox
import org.jetbrains.skija.paragraph.Affinity
import org.jetbrains.skija.ManagedString
import org.jetbrains.skija.paragraph.Paragraph
import org.jetbrains.skija.IRange
import org.jetbrains.skija.FontFeature
import org.jetbrains.skija.Typeface
import org.jetbrains.skija.paragraph.HeightMode
import org.jetbrains.skija.paragraph.StrutStyle
import org.jetbrains.skija.paragraph.BaselineMode
import org.jetbrains.skija.paragraph.RectWidthMode
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skija.FontMgr
import org.jetbrains.skija.paragraph.ParagraphCache
import org.jetbrains.skija.paragraph.ParagraphStyle
import org.jetbrains.skija.paragraph.RectHeightMode
import org.jetbrains.skija.paragraph.DecorationStyle
import org.jetbrains.skija.paragraph.ParagraphBuilder
import org.jetbrains.skija.paragraph.PlaceholderStyle
import org.jetbrains.skija.paragraph.TextStyleAttribute
import org.jetbrains.skija.paragraph.DecorationLineStyle
import org.jetbrains.skija.paragraph.PlaceholderAlignment
import org.jetbrains.skija.paragraph.PositionWithAffinity
import org.jetbrains.skija.paragraph.TypefaceFontProvider
import org.jetbrains.skija.shaper.Shaper
import org.jetbrains.skija.TextBlob
import org.jetbrains.skija.shaper.FontRun
import org.jetbrains.skija.FourByteTag
import org.jetbrains.skija.shaper.LanguageRun
import org.jetbrains.skija.shaper.ShapingOptions
import org.jetbrains.skija.shaper.FontMgrRunIterator
import org.jetbrains.skija.shaper.IcuBidiRunIterator
import org.jetbrains.skija.shaper.ManagedRunIterator
import org.jetbrains.skija.shaper.HbIcuScriptRunIterator
import org.jetbrains.skija.shaper.TextBlobBuilderRunHandler
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import org.jetbrains.skija.skottie.Animation
import org.jetbrains.skija.sksg.InvalidationController
import org.jetbrains.skija.skottie.RenderFlag
import org.jetbrains.skija.skottie.AnimationBuilder
import org.jetbrains.skija.skottie.AnimationBuilderFlag
import org.jetbrains.skija.Matrix33
import org.jetbrains.skija.svg.SVGDOM
import org.jetbrains.skija.svg.SVGSVG
import org.jetbrains.skija.svg.SVGTag
import org.jetbrains.skija.svg.SVGNode
import org.jetbrains.skija.WStream
import org.jetbrains.skija.svg.SVGCanvas
import org.jetbrains.skija.svg.SVGLength
import org.jetbrains.skija.svg.SVGLengthType
import org.jetbrains.skija.svg.SVGLengthUnit
import org.jetbrains.skija.svg.SVGLengthContext
import org.jetbrains.skija.svg.SVGPreserveAspectRatio
import org.jetbrains.skija.svg.SVGPreserveAspectRatioAlign
import org.jetbrains.skija.svg.SVGPreserveAspectRatioScale
import org.jetbrains.skija.ColorAlphaType
import org.jetbrains.skija.AnimationDisposalMode
import org.jetbrains.skija.BlendMode
import org.jetbrains.skija.IRect
import org.jetbrains.skija.AnimationFrameInfo
import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.IHasImageInfo
import org.jetbrains.skija.ImageInfo
import org.jetbrains.skija.IPoint
import org.jetbrains.skija.PixelRef
import org.jetbrains.skija.Shader
import org.jetbrains.skija.FilterTileMode
import org.jetbrains.skija.SamplingMode
import org.jetbrains.skija.U16String
import org.jetbrains.skija.SurfaceProps
import org.jetbrains.skija.RRect
import org.jetbrains.skija.ClipMode
import org.jetbrains.skija.FilterMode
import org.jetbrains.skija.Picture
import org.jetbrains.skija.Matrix44
import org.jetbrains.skija.EncodedOrigin
import org.jetbrains.skija.EncodedImageFormat
import org.jetbrains.skija.Color4f
import org.jetbrains.skija.ColorChannel
import org.jetbrains.skija.ColorFilter
import org.jetbrains.skija.ColorMatrix
import org.jetbrains.skija.ColorFilter._LinearToSRGBGammaHolder
import org.jetbrains.skija.ColorFilter._SRGBToLinearGammaHolder
import org.jetbrains.skija.InversionMode
import org.jetbrains.skija.ColorFilter._LumaHolder
import org.jetbrains.skija.ColorInfo
import org.jetbrains.skija.ColorSpace._SRGBHolder
import org.jetbrains.skija.ColorSpace._SRGBLinearHolder
import org.jetbrains.skija.ColorSpace._DisplayP3Holder
import org.jetbrains.skija.ContentChangeMode
import org.jetbrains.skija.CubicResampler
import org.jetbrains.skija.DirectContext
import org.jetbrains.skija.GLBackendState
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.skija.FilterBlurMode
import org.jetbrains.skija.MipmapMode
import org.jetbrains.skija.FilterMipmap
import org.jetbrains.skija.FilterQuality
import org.jetbrains.skija.FontEdging
import org.jetbrains.skija.FontHinting
import org.jetbrains.skija.FontExtents
import org.jetbrains.skija.FontFamilyName
import org.jetbrains.skija.FontMgr._DefaultHolder
import org.jetbrains.skija.FontStyleSet
import org.jetbrains.skija.FontSlant
import org.jetbrains.skija.FontWidth
import org.jetbrains.skija.FontVariation
import org.jetbrains.skija.FontVariationAxis
import org.jetbrains.skija.GradientStyle
import org.jetbrains.skija.MaskFilter
import org.jetbrains.skija.OutputWStream
import org.jetbrains.skija.PaintMode
import org.jetbrains.skija.PaintStrokeCap
import org.jetbrains.skija.PaintStrokeJoin
import org.jetbrains.skija.PathEffect
import org.jetbrains.skija.PaintFilterCanvas
import org.jetbrains.skija.PathSegment
import org.jetbrains.skija.PathOp
import org.jetbrains.skija.PathFillMode
import org.jetbrains.skija.PathVerb
import org.jetbrains.skija.PathEllipseArc
import org.jetbrains.skija.PathDirection
import org.jetbrains.skija.PathSegmentIterator
import org.jetbrains.skija.RSXform
import org.jetbrains.skija.PathMeasure
import org.jetbrains.skija.PictureRecorder
import org.jetbrains.skija.PixelGeometry
import org.jetbrains.skija.Point3
import org.jetbrains.skija.RuntimeEffect
import org.jetbrains.skija.ShadowUtils
import org.jetbrains.skija.SurfaceOrigin
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.TextBlobBuilder
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

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
class BreakIterator @ApiStatus.Internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR), Cloneable {
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
        @JvmOverloads
        fun makeCharacterInstance(locale: String? = null): BreakIterator {
            Stats.onNativeCall()
            return BreakIterator(_nMake(0, locale)) // UBRK_CHARACTER
        }
        /**
         * Returns a new BreakIterator instance for word breaks for the given locale.
         */
        /**
         * Returns a new BreakIterator instance for word breaks for the default locale.
         */
        @JvmOverloads
        fun makeWordInstance(locale: String? = null): BreakIterator {
            Stats.onNativeCall()
            return BreakIterator(_nMake(1, locale)) // UBRK_WORD
        }
        /**
         * Returns a new BreakIterator instance for line breaks for the given locale.
         */
        /**
         * Returns a new BreakIterator instance for line breaks for the default locale.
         */
        @JvmOverloads
        fun makeLineInstance(locale: String? = null): BreakIterator {
            Stats.onNativeCall()
            return BreakIterator(_nMake(2, locale)) // UBRK_LINE
        }
        /**
         * Returns a new BreakIterator instance for sentence breaks for the given locale.
         */
        /**
         * Returns a new BreakIterator instance for sentence breaks for the default locale.
         */
        @JvmOverloads
        fun makeSentenceInstance(locale: String? = null): BreakIterator {
            Stats.onNativeCall()
            return BreakIterator(_nMake(3, locale)) // UBRK_SENTENCE
        }

        @ApiStatus.Internal
        external fun _nGetFinalizer(): Long
        @ApiStatus.Internal
        external fun _nMake(type: Int, locale: String?): Long
        @ApiStatus.Internal
        external fun _nClone(ptr: Long): Long
        @ApiStatus.Internal
        external fun _nCurrent(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nNext(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nPrevious(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nFirst(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nLast(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nPreceding(ptr: Long, offset: Int): Int
        @ApiStatus.Internal
        external fun _nFollowing(ptr: Long, offset: Int): Int
        @ApiStatus.Internal
        external fun _nIsBoundary(ptr: Long, offset: Int): Boolean
        @ApiStatus.Internal
        external fun _nGetRuleStatus(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nGetRuleStatuses(ptr: Long): IntArray
        @ApiStatus.Internal
        external fun _nSetText(ptr: Long, textPtr: Long)

        init {
            staticLoad()
        }
    }

    @ApiStatus.Internal
    var _text: U16String? = null
    override fun close() {
        super.close()
        if (_text != null) _text!!.close()
    }

    /**
     * Create a copy of this iterator
     */
    public override fun clone(): BreakIterator {
        Stats.onNativeCall()
        return BreakIterator(_nClone(_ptr))
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
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
    fun next(n: Int): Int {
        var n = n
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
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
            _nGetRuleStatuses(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * Set a new text string to be scanned. The current scan position is reset to [].
     */
    fun setText(text: String?) {
        try {
            Stats.onNativeCall()
            _text = U16String(text)
            _nSetText(_ptr, Native.Companion.getPtr(_text))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(_text)
        }
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}