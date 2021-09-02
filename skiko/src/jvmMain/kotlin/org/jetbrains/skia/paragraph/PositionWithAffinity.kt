package org.jetbrains.skia.paragraph

class PositionWithAffinity(val position: Int, affinity: Affinity) {
    val _affinity: Affinity
    val affinity: Affinity
        get() = _affinity

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is PositionWithAffinity) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (position != other.position) return false
        val `this$_affinity`: Any = affinity
        val `other$_affinity`: Any = other.affinity
        return if (if (`this$_affinity` == null) `other$_affinity` != null else `this$_affinity` != `other$_affinity`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is PositionWithAffinity
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + position
        val `$_affinity`: Any = affinity
        result = result * PRIME + (`$_affinity`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "PositionWithAffinity(_position=" + position + ", _affinity=" + affinity + ")"
    }

    init {
        _affinity = affinity
    }
}