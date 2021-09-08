@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

class PathSegmentIterator internal constructor(val _path: Path?, ptr: NativePointer) : Managed(ptr, _nGetFinalizer()),
    MutableIterator<PathSegment?> {
    companion object {
        fun make(path: Path?, forceClose: Boolean): PathSegmentIterator {
            return try {
                val ptr =
                    _nMake(getPtr(path), forceClose)
                val i = PathSegmentIterator(path, ptr)
                i._nextSegment = _nNext(ptr)
                i
            } finally {
                reachabilityBarrier(path)
            }
        }

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nMake")
        external fun _nMake(pathPtr: NativePointer, forceClose: Boolean): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nGetFinalizer")
        external fun _nGetFinalizer(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_PathSegmentIterator__1nNext")
        external fun _nNext(ptr: NativePointer): PathSegment?

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
            reachabilityBarrier(this)
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
