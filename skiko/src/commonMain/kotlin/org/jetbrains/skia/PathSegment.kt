package org.jetbrains.skia

class PathSegment constructor(
    val verb: PathVerb = PathVerb.DONE,
    val p0: Point? = null,
    val p1: Point? = null,
    val p2: Point? = null,
    val p3: Point? = null,
    val conicWeight: Float = 0.0f,
    val isCloseLine: Boolean = false,
    val isClosedContour: Boolean = false
) {

    constructor(verbOrdinal: Int, x0: Float, y0: Float, isClosedContour: Boolean) : this(
        PathVerb.values().get(
            verbOrdinal
        ), Point(x0, y0), null, null, null, 0.0f, false, isClosedContour
    ) {
        require(verbOrdinal == PathVerb.MOVE.ordinal || verbOrdinal == PathVerb.CLOSE.ordinal) {
            "Expected MOVE or CLOSE, got " + PathVerb.values()[verbOrdinal]
        }
    }

    constructor(x0: Float, y0: Float, x1: Float, y1: Float, isCloseLine: Boolean, isClosedContour: Boolean) : this(
        PathVerb.LINE,
        Point(x0, y0),
        Point(x1, y1),
        null,
        null,
        0.0f,
        isCloseLine,
        isClosedContour
    )

    constructor(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, isClosedContour: Boolean) : this(
        PathVerb.QUAD,
        Point(x0, y0),
        Point(x1, y1),
        Point(x2, y2),
        null,
        0.0f,
        false,
        isClosedContour
    )

    constructor(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        conicWeight: Float,
        isClosedContour: Boolean
    ) : this(
        PathVerb.CONIC,
        Point(x0, y0),
        Point(x1, y1),
        Point(x2, y2),
        null,
        conicWeight,
        false,
        isClosedContour
    )

    constructor(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float,
        isClosedContour: Boolean
    ) : this(
        PathVerb.CUBIC,
        Point(x0, y0),
        Point(x1, y1),
        Point(x2, y2),
        Point(x3, y3),
        0.0f,
        false,
        isClosedContour
    )

    override fun toString(): String {
        return "Segment(" + "verb=" + verb + (if (verb != PathVerb.DONE) ", p0=$p0" else "") + (if (verb == PathVerb.LINE || verb == PathVerb.QUAD || verb == PathVerb.CONIC || verb == PathVerb.CUBIC) ", p1=" + p1 else "") + (if (verb == PathVerb.QUAD || verb == PathVerb.CONIC || verb == PathVerb.CUBIC) ", p2=" + p2 else "") + (if (verb == PathVerb.CUBIC) ", p3=" + p3 else "") + (if (verb == PathVerb.CONIC) ", conicWeight=" + conicWeight else "") + (if (verb == PathVerb.LINE) ", closeLine=" + isCloseLine else "") + (if (verb != PathVerb.DONE) ", closedContour=" + isClosedContour else "") + ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathSegment) return false
        return verb == other.verb &&
                (if (verb != PathVerb.DONE) p0 == other.p0 else true) &&
                (if (verb == PathVerb.LINE || verb == PathVerb.QUAD || verb == PathVerb.CONIC || verb == PathVerb.CUBIC) p1 == other.p1 else true) &&
                (if (verb == PathVerb.QUAD || verb == PathVerb.CONIC || verb == PathVerb.CUBIC) p2 == other.p2 else true) &&
                (if (verb == PathVerb.CUBIC) p3 == other.p3 else true) &&
                (if (verb == PathVerb.CONIC) other.conicWeight.compareTo(conicWeight) == 0 else true) &&
                (if (verb == PathVerb.LINE) isCloseLine == other.isCloseLine else true) &&
                if (verb != PathVerb.DONE) isClosedContour == other.isClosedContour else true
    }

    override fun hashCode(): Int {
        return when (verb) {
            PathVerb.DONE -> objectHashes(verb)
            PathVerb.MOVE -> objectHashes(verb, p0, isClosedContour)
            PathVerb.LINE -> objectHashes(verb, p0, p1, isCloseLine, isClosedContour)
            PathVerb.QUAD -> objectHashes(verb, p0, p1, p2, isClosedContour)
            PathVerb.CONIC -> objectHashes(
                verb,
                p0,
                p1,
                p2,
                conicWeight,
                isClosedContour
            )
            PathVerb.CUBIC -> objectHashes(verb, p0, p1, p2, p3, isClosedContour)
            else -> throw RuntimeException("Unreachable")
        }
    }
}

internal fun objectHashes(vararg args: Any?): Int {
    return args.contentHashCode()
}