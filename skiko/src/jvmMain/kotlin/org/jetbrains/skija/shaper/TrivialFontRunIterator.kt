package org.jetbrains.skija.shaper

import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.*

class TrivialFontRunIterator(text: String, font: Font) : MutableIterator<FontRun?> {
    @ApiStatus.Internal
    val _length: Int

    @ApiStatus.Internal
    val _font: Font

    @ApiStatus.Internal
    var _atEnd: Boolean
    override fun next(): FontRun {
        _atEnd = true
        return FontRun(_length, _font)
    }

    override fun hasNext(): Boolean {
        return !_atEnd
    }

    init {
        _length = text.length
        _font = font
        _atEnd = _length == 0
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}