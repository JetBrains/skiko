package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withResult

class PathSegmentIterator internal constructor(val _path: Path?, ptr: NativePointer) :
    Managed(ptr, PathSegmentIterator_nGetFinalizer()),
    MutableIterator<PathSegment?> {
    companion object {
        fun make(path: Path?, forceClose: Boolean): PathSegmentIterator {
            return try {
                val ptr = _nMake(getPtr(path), forceClose)
                val i = PathSegmentIterator(path, ptr)
                i._nextSegment = ptr.nextSegment()
                i
            } finally {
                reachabilityBarrier(path)
            }
        }

        init {
            staticLoad()
        }
    }

    var _nextSegment: PathSegment? = null
    override fun next(): PathSegment? {
        return try {
            if (_nextSegment?.verb == PathVerb.DONE) throw NoSuchElementException()
            val res = _nextSegment
            _nextSegment = _ptr.nextSegment()
            res
        } finally {
            reachabilityBarrier(this)
        }
    }

    override fun hasNext(): Boolean {
        return _nextSegment?.verb != PathVerb.DONE
    }

    private object _FinalizerHolder {
        val PTR = PathSegmentIterator_nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}

private fun NativePointer.nextSegment() = pathSegmentFromFloatArray(withResult(FloatArray(10)) {
    PathSegmentIterator_nNext(this, it)
})


private fun pathSegmentFromFloatArray(points: FloatArray): PathSegment {
    val context = points.last().toInt()

    val verb = (context) and ((1 shl 3) - 1)
    val isClosedBit = ((context shr 7) and 1)
    val isClosedLineBit = ((context shr 6) and 1)
    val isClosed = isClosedBit != 0

    return when (verb) {
        PathVerb.MOVE.ordinal, PathVerb.CLOSE.ordinal -> {
            PathSegment(verb, points[0], points[1], isClosed)
        }
        PathVerb.LINE.ordinal -> {
            PathSegment(points[0], points[1], points[2], points[3], isClosedLineBit != 0, isClosed)
        }
        PathVerb.QUAD.ordinal -> {
            PathSegment(points[0], points[1], points[2], points[3], points[4], points[5], isClosed)
        }
        PathVerb.CONIC.ordinal -> {
            PathSegment(points[0], points[1], points[2], points[3], points[4], points[5], points[8], isClosed)
        }
        PathVerb.CUBIC.ordinal -> {
            PathSegment(
                points[0],
                points[1],
                points[2],
                points[3],
                points[4],
                points[5],
                points[6],
                points[7],
                isClosed
            )
        }
        PathVerb.DONE.ordinal -> {
            PathSegment()
        }
        else -> throw IllegalStateException()
    }
}

@ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nGetFinalizer")
private external fun PathSegmentIterator_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nNext")
private external fun PathSegmentIterator_nNext(ptr: NativePointer, points: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nMake")
private external fun _nMake(pathPtr: NativePointer, forceClose: Boolean): NativePointer