package org.jetbrains.skia

/**
 * The logical operations that can be performed when combining two paths.
 */
enum class PathOp {
    /** subtract the op path from the first path  */
    DIFFERENCE,

    /** intersect the two paths  */
    INTERSECT,

    /** union (inclusive-or) the two paths  */
    UNION,

    /** exclusive-or the two paths  */
    XOR,

    /** subtract the first path from the op path  */
    REVERSE_DIFFERENCE;
}