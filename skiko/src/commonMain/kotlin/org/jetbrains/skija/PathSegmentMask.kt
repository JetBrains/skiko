package org.jetbrains.skija

interface PathSegmentMask {
    companion object {
        const val LINE = 1
        const val QUAD = 2
        const val CONIC = 4
        const val CUBIC = 8
    }
}