package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import java.lang.ref.Reference
import java.util.NoSuchElementException

class PathSegmentIterator internal constructor(val _path: Path?, ptr: Long) : Managed(ptr, _nGetFinalizer()),
    MutableIterator<PathSegment?> {
    companion object {
        fun make(path: Path?, forceClose: Boolean): PathSegmentIterator {
            return try {
                val ptr =
                    _nMake(Native.Companion.getPtr(path), forceClose)
                val i = PathSegmentIterator(path, ptr)
                i._nextSegment = _nNext(ptr)
                i
            } finally {
                Reference.reachabilityFence(path)
            }
        }

        external fun _nMake(pathPtr: Long, forceClose: Boolean): Long
        external fun _nGetFinalizer(): Long
        external fun _nNext(ptr: Long): PathSegment?

        init {
            staticLoad()
        }
    }

    var _nextSegment: PathSegment? = null
    override fun next(): PathSegment? {
        return try {
            if (_nextSegment?.verb == PathVerb.DONE) throw NoSuchElementException()
            val res = _nextSegment
            _nextSegment = _nNext(_ptr)
            res
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    override fun hasNext(): Boolean {
        return _nextSegment?.verb != PathVerb.DONE
    }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}