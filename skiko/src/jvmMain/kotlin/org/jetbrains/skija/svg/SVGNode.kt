package org.jetbrains.skija.svg

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

abstract class SVGNode internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        @JvmStatic external fun _nGetTag(ptr: Long): Int

        init {
            staticLoad()
        }
    }

    val tag: SVGTag
        get() = try {
            Stats.onNativeCall()
            SVGTag.values().get(_nGetTag(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }
}