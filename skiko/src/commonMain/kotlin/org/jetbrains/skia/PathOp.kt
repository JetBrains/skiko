package org.jetbrains.skia

import kotlin.jvm.JvmInline

/**
 * The logical operations that can be performed when combining two paths.
 */
@JvmInline
value class PathOp internal constructor(val ordinal: Int) {
    companion object {
        /** subtract the op path from the first path  */
        val DIFFERENCE = PathOp(0)
        /** intersect the two paths  */
        val INTERSECT = PathOp(1)
        /** union (inclusive-or) the two paths  */
        val UNION = PathOp(2)
        /** exclusive-or the two paths  */
        val XOR = PathOp(3)
        /** subtract the first path from the op path  */
        val REVERSE_DIFFERENCE = PathOp(4)
    }
}