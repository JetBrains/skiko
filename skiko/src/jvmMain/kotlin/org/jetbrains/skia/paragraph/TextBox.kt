package org.jetbrains.skia.paragraph

import org.jetbrains.skia.*

class TextBox(val rect: Rect, direction: Direction) {
    val _direction: Direction

    constructor(l: Float, t: Float, r: Float, b: Float, direction: Int) : this(
        Rect.makeLTRB(l, t, r, b),
        Direction.values().get(direction)
    )

    val direction: Direction
        get() = _direction

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is TextBox) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$_rect`: Any = rect
        val `other$_rect`: Any = other.rect
        if (if (`this$_rect` == null) `other$_rect` != null else `this$_rect` != `other$_rect`) return false
        val `this$_direction`: Any = direction
        val `other$_direction`: Any = other.direction
        return if (if (`this$_direction` == null) `other$_direction` != null else `this$_direction` != `other$_direction`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is TextBox
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_rect`: Any = rect
        result = result * PRIME + (`$_rect`?.hashCode() ?: 43)
        val `$_direction`: Any = direction
        result = result * PRIME + (`$_direction`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "TextBox(_rect=" + rect + ", _direction=" + direction + ")"
    }

    init {
        _direction = direction
    }
}