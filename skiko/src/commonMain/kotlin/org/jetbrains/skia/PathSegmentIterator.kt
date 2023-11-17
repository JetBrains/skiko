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
                val i = PathSegmentIterator(path, _nMake(getPtr(path), forceClose))
                i._nextSegment = i.nextSegment()
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
            _nextSegment = nextSegment()
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

    private fun nextSegment() = pathSegmentFromIntArray(withResult(IntArray(10)) {
        PathSegmentIterator_nNext(this@PathSegmentIterator._ptr, it)
    })
}

private fun pathSegmentFromIntArray(points: IntArray): PathSegment {
    val context = points.last()

    val verb = (context) and ((1 shl 3) - 1)
    val isClosedBit = ((context shr 7) and 1)
    val isClosedLineBit = ((context shr 6) and 1)
    val isClosed = isClosedBit != 0

    return when (PathVerb.values()[verb]) {
        PathVerb.MOVE, PathVerb.CLOSE -> {
            PathSegment(verb, Float.fromBits(points[0]), Float.fromBits(points[1]), isClosed)
        }
        PathVerb.LINE -> {
            PathSegment(
                Float.fromBits(points[0]),
                Float.fromBits(points[1]),
                Float.fromBits(points[2]),
                Float.fromBits(points[3]),
                isClosedLineBit != 0,
                isClosed
            )
        }
        PathVerb.QUAD -> {
            PathSegment(
                Float.fromBits(points[0]),
                Float.fromBits(points[1]),
                Float.fromBits(points[2]),
                Float.fromBits(points[3]),
                Float.fromBits(points[4]),
                Float.fromBits(points[5]),
                isClosed
            )
        }
        PathVerb.CONIC -> {
            PathSegment(
                Float.fromBits(points[0]),
                Float.fromBits(points[1]),
                Float.fromBits(points[2]),
                Float.fromBits(points[3]),
                Float.fromBits(points[4]),
                Float.fromBits(points[5]),
                Float.fromBits(points[8]),
                isClosed
            )
        }
        PathVerb.CUBIC -> {
            PathSegment(
                Float.fromBits(points[0]),
                Float.fromBits(points[1]),
                Float.fromBits(points[2]),
                Float.fromBits(points[3]),
                Float.fromBits(points[4]),
                Float.fromBits(points[5]),
                Float.fromBits(points[6]),
                Float.fromBits(points[7]),
                isClosed
            )
        }
        PathVerb.DONE -> {
            PathSegment()
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathSegmentIterator__1nGetFinalizer")
private external fun PathSegmentIterator_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nNext")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathSegmentIterator__1nNext")
private external fun PathSegmentIterator_nNext(ptr: NativePointer, points: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_PathSegmentIterator__1nMake")
private external fun _nMake(pathPtr: NativePointer, forceClose: Boolean): NativePointer