package org.jetbrains.skija

import java.lang.RuntimeException
import java.util.*

class PathSegment @JvmOverloads constructor(
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
        PathVerb.Companion._values.get(
            verbOrdinal
        ), org.jetbrains.skija.Point(x0, y0), null, null, null, 0.0f, false, isClosedContour
    ) {
        assert(verbOrdinal == PathVerb.MOVE.ordinal || verbOrdinal == PathVerb.CLOSE.ordinal) {
            "Expected MOVE or CLOSE, got " + PathVerb.Companion._values.get(
                verbOrdinal
            )
        }
    }

    constructor(x0: Float, y0: Float, x1: Float, y1: Float, isCloseLine: Boolean, isClosedContour: Boolean) : this(
        PathVerb.LINE,
        org.jetbrains.skija.Point(x0, y0),
        org.jetbrains.skija.Point(x1, y1),
        null,
        null,
        0.0f,
        isCloseLine,
        isClosedContour
    ) {
    }

    constructor(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, isClosedContour: Boolean) : this(
        PathVerb.QUAD,
        org.jetbrains.skija.Point(x0, y0),
        org.jetbrains.skija.Point(x1, y1),
        org.jetbrains.skija.Point(x2, y2),
        null,
        0.0f,
        false,
        isClosedContour
    ) {
    }

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
        org.jetbrains.skija.Point(x0, y0),
        org.jetbrains.skija.Point(x1, y1),
        org.jetbrains.skija.Point(x2, y2),
        null,
        conicWeight,
        false,
        isClosedContour
    ) {
    }

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
        org.jetbrains.skija.Point(x0, y0),
        org.jetbrains.skija.Point(x1, y1),
        org.jetbrains.skija.Point(x2, y2),
        org.jetbrains.skija.Point(x3, y3),
        0.0f,
        false,
        isClosedContour
    ) {
    }

    override fun toString(): String {
        return "Segment(" + "verb=" + verb + (if (verb != PathVerb.DONE) ", p0=" + p0 else "") + (if (verb == PathVerb.LINE || verb == PathVerb.QUAD || verb == PathVerb.CONIC || verb == PathVerb.CUBIC) ", p1=" + p1 else "") + (if (verb == PathVerb.QUAD || verb == PathVerb.CONIC || verb == PathVerb.CUBIC) ", p2=" + p2 else "") + (if (verb == PathVerb.CUBIC) ", p3=" + p3 else "") + (if (verb == PathVerb.CONIC) ", conicWeight=" + conicWeight else "") + (if (verb == PathVerb.LINE) ", closeLine=" + isCloseLine else "") + (if (verb != PathVerb.DONE) ", closedContour=" + isClosedContour else "") + ")"
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val segment = o as PathSegment
        return verb == segment.verb && (if (verb != PathVerb.DONE) p0 == segment.p0 else true) && (if (verb == PathVerb.LINE || verb == PathVerb.QUAD || verb == PathVerb.CONIC || verb == PathVerb.CUBIC) p1 == segment.p1 else true) && (if (verb == PathVerb.QUAD || verb == PathVerb.CONIC || verb == PathVerb.CUBIC) p2 == segment.p2 else true) && (if (verb == PathVerb.CUBIC) p3 == segment.p3 else true) && (if (verb == PathVerb.CONIC) java.lang.Float.compare(
            segment.conicWeight,
            conicWeight
        ) == 0 else true) && (if (verb == PathVerb.LINE) isCloseLine == segment.isCloseLine else true) && if (verb != PathVerb.DONE) isClosedContour == segment.isClosedContour else true
    }

    override fun hashCode(): Int {
        return when (verb) {
            PathVerb.DONE -> Objects.hash(verb)
            PathVerb.MOVE -> Objects.hash(verb, p0, isClosedContour)
            PathVerb.LINE -> Objects.hash(verb, p0, p1, isCloseLine, isClosedContour)
            PathVerb.QUAD -> Objects.hash(verb, p0, p1, p2, isClosedContour)
            PathVerb.CONIC -> Objects.hash(
                verb,
                p0,
                p1,
                p2,
                conicWeight,
                isClosedContour
            )
            PathVerb.CUBIC -> Objects.hash(verb, p0, p1, p2, p3, isClosedContour)
            else -> throw RuntimeException("Unreachable")
        }
    }
}