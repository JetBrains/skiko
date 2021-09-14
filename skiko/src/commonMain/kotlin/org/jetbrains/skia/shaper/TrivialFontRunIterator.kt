package org.jetbrains.skia.shaper

import org.jetbrains.skia.*

class TrivialFontRunIterator(text: String, font: Font) : MutableIterator<FontRun?> {
    val _length: Int

    val _font: Font

    internal var _atEnd: Boolean
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