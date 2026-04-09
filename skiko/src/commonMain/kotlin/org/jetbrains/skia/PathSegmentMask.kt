package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class PathSegmentMask internal constructor(val value: Int) {
    companion object {
        val LINE = PathSegmentMask(1)
        val QUAD = PathSegmentMask(2)
        val CONIC = PathSegmentMask(4)
        val CUBIC = PathSegmentMask(8)
    }
}