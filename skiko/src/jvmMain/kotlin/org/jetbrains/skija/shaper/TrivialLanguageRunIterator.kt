package org.jetbrains.skija.shaper

import org.jetbrains.annotations.ApiStatus

class TrivialLanguageRunIterator(text: String, language: String) : MutableIterator<LanguageRun?> {
    @ApiStatus.Internal
    val _length: Int

    @ApiStatus.Internal
    val _language: String

    @ApiStatus.Internal
    var _atEnd: Boolean
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