package org.jetbrains.skia.paragraph

class PositionWithAffinity(val position: Int, affinity: Affinity) {
    val _affinity: Affinity
    val affinity: Affinity
        get() = _affinity

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is PositionWithAffinity) return false
        if (position != other.position) return false
        return this.affinity == other.affinity
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + position
        result = result * PRIME + affinity.hashCode()
        return result
    }

    override fun toString(): String {
        return "PositionWithAffinity(_position=$position, _affinity=$affinity)"
    }

    init {
        _affinity = affinity
    }
}