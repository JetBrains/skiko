package org.jetbrains.skia.shaper

import org.jetbrains.skia.FourByteTag

class ScriptRun(internal val end: Int, internal val scriptTag: Int) {

    constructor(end: Int, script: String) : this(end, FourByteTag.Companion.fromString(script)) {}

    /**
     * Should be iso15924 codes.
     */
    val script: String
        get() = FourByteTag.toString(scriptTag)

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is ScriptRun) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (end != other.end) return false
        return scriptTag == other.scriptTag
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is ScriptRun
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + end
        result = result * PRIME + scriptTag
        return result
    }

    override fun toString(): String {
        return "ScriptRun(_end=$end, _scriptTag=$scriptTag)"
    }
}