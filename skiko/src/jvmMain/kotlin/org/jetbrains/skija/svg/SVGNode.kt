package org.jetbrains.skija.svg

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

abstract class SVGNode @ApiStatus.Internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        @ApiStatus.Internal
        external fun _nGetTag(ptr: Long): Int

        init {
            staticLoad()
        }
    }

    val tag: SVGTag
        get() = try {
            Stats.onNativeCall()
            SVGTag._values.get(_nGetTag(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }
}