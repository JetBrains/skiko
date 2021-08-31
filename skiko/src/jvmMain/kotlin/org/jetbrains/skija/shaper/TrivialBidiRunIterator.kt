package org.jetbrains.skija.shaper

import org.jetbrains.annotations.ApiStatus

class TrivialBidiRunIterator(text: String, level: Int) : MutableIterator<BidiRun?> {
    @ApiStatus.Internal
    val _length: Int

    @ApiStatus.Internal
    val _level: Int

    @ApiStatus.Internal
    var _atEnd: Boolean
    override fun next(): BidiRun {
        _atEnd = true
        return BidiRun(_length, _level)
    }

    override fun hasNext(): Boolean {
        return !_atEnd
    }

    init {
        _length = text.length
        _level = level
        _atEnd = _length == 0
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}