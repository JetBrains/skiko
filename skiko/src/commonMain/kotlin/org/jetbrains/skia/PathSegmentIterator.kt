@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

class PathSegmentIterator internal constructor(val _path: Path?, ptr: NativePointer) : Managed(ptr, PathSegmentIterator_nGetFinalizer()),
    MutableIterator<PathSegment?> {
    companion object {
        fun make(path: Path?, forceClose: Boolean): PathSegmentIterator {
            return try {
                val ptr =
                    _nMake(getPtr(path), forceClose)
                val i = PathSegmentIterator(path, ptr)
                i._nextSegment = PathSegmentIterator_nNext(ptr)
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
            _nextSegment = PathSegmentIterator_nNext(_ptr)
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

@ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nGetFinalizer")
private external fun PathSegmentIterator_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nNext")
private external fun PathSegmentIterator_nNext(ptr: NativePointer): PathSegment?

@ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nMake")
private external fun _nMake(pathPtr: NativePointer, forceClose: Boolean): NativePointer