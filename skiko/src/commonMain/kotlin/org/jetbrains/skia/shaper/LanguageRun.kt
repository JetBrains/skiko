package org.jetbrains.skia.shaper

class LanguageRun(
    internal val end: Int,
    /**
     * Should be BCP-47, c locale names may also work.
     */
    internal val language: String
) {
    /**
     * Should be BCP-47, c locale names may also work.
     */
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is LanguageRun) return false
        if (end != other.end) return false
        return this.language == other.language
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + end
        result = result * PRIME + language.hashCode()
        return result
    }

    override fun toString(): String {
        return "LanguageRun(_end=$end, _language=$language)"
    }
}