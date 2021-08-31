package org.jetbrains.skija.shaper

import org.jetbrains.annotations.ApiStatus

class TrivialScriptRunIterator(text: String, script: String) : MutableIterator<ScriptRun?> {
    @ApiStatus.Internal
    val _length: Int

    @ApiStatus.Internal
    val _script: String

    @ApiStatus.Internal
    var _atEnd: Boolean
    override fun next(): ScriptRun {
        _atEnd = true
        return ScriptRun(_length, _script)
    }

    override fun hasNext(): Boolean {
        return !_atEnd
    }

    init {
        _length = text.length
        _script = script
        _atEnd = _length == 0
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}