package org.jetbrains.skia.shaper

import java.text.Bidi

class JavaTextBidiRunIterator @JvmOverloads constructor(
    text: String?,
    flags: Int = Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT
) : MutableIterator<BidiRun?> {
    internal val _bidi: Bidi

    internal val _runsCount: Int

    internal var _run: Int
    override fun next(): BidiRun {
        _run++
        return BidiRun(_bidi.getRunLimit(_run), _bidi.getRunLevel(_run))
    }

    override fun hasNext(): Boolean {
        return _run + 1 < _runsCount
    }

    init {
        _bidi = Bidi(text, flags)
        _runsCount = _bidi.runCount
        _run = -1
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}