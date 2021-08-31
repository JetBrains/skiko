package org.jetbrains.skija.shaper

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
    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is LanguageRun) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (end != other.end) return false
        val `this$_language`: Any = language
        val `other$_language`: Any = other.language
        return if (if (`this$_language` == null) `other$_language` != null else `this$_language` != `other$_language`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is LanguageRun
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + end
        val `$_language`: Any = language
        result = result * PRIME + (`$_language`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "LanguageRun(_end=" + end + ", _language=" + language + ")"
    }
}