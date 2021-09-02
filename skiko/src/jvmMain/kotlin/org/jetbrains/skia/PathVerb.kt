package org.jetbrains.skia

/**
 * Verb instructs Path how to interpret one or more Point and optional conic weight;
 * manage contour, and terminate Path.
 */
enum class PathVerb {
    /** iter.next returns 1 point  */
    MOVE,

    /** iter.next returns 2 points  */
    LINE,

    /** iter.next returns 3 points  */
    QUAD,

    /** iter.next returns 3 points + iter.conicWeight()  */
    CONIC,

    /** iter.next returns 4 points  */
    CUBIC,

    /** iter.next returns 1 point (contour's moveTo pt)  */
    CLOSE,

    /** iter.next returns 0 points  */
    DONE;
}