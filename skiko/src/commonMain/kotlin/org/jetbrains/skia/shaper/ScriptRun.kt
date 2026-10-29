package org.jetbrains.skia.shaper

import org.jetbrains.skia.FourByteTag

class ScriptRun(internal val end: Int, internal val scriptTag: Int) {

    constructor(end: Int, script: String) : this(end, FourByteTag.fromString(script).tag) {}

    /**
     * Should be iso15924 codes.
     */
    val script: String
        get() = FourByteTag.toString(FourByteTag(scriptTag))

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is ScriptRun) return false
        if (end != other.end) return false
        return scriptTag == other.scriptTag
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