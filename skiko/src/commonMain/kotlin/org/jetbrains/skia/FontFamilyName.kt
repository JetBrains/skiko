package org.jetbrains.skia

class FontFamilyName(val name: String, val language: String) {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FontFamilyName) return false
        if (this.name != other.name) return false
        return this.language == other.language
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + name.hashCode()
        result = result * PRIME + language.hashCode()
        return result
    }

    override fun toString(): String {
        return "FontFamilyName(_name=$name, _language=$language)"
    }
}