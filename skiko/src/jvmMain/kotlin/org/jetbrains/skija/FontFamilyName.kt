package org.jetbrains.skija

class FontFamilyName(val name: String, val language: String) {
    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is FontFamilyName) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$_name`: Any = name
        val `other$_name`: Any = other.name
        if (if (`this$_name` == null) `other$_name` != null else `this$_name` != `other$_name`) return false
        val `this$_language`: Any = language
        val `other$_language`: Any = other.language
        return if (if (`this$_language` == null) `other$_language` != null else `this$_language` != `other$_language`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FontFamilyName
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_name`: Any = name
        result = result * PRIME + (`$_name`?.hashCode() ?: 43)
        val `$_language`: Any = language
        result = result * PRIME + (`$_language`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "FontFamilyName(_name=" + name + ", _language=" + language + ")"
    }
}