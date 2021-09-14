package org.jetbrains.skia.shaper

class TrivialLanguageRunIterator(text: String, language: String) : MutableIterator<LanguageRun?> {
    val _length: Int

    val _language: String

    internal var _atEnd: Boolean
    override fun next(): LanguageRun {
        _atEnd = true
        return LanguageRun(_length, _language)
    }

    override fun hasNext(): Boolean {
        return !_atEnd
    }

    init {
        _length = text.length
        _language = language
        _atEnd = _length == 0
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}