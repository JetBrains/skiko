package org.jetbrains.skija.shaper

class TrivialScriptRunIterator(text: String, script: String) : MutableIterator<ScriptRun?> {
    internal val _length: Int

    internal val _script: String

    internal var _atEnd: Boolean
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