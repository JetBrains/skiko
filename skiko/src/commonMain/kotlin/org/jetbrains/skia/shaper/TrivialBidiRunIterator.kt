package org.jetbrains.skia.shaper

class TrivialBidiRunIterator(text: String, level: Int) : MutableIterator<BidiRun?> {
    val _length: Int

    val _level: Int

    internal var _atEnd: Boolean
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