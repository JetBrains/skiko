@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

abstract class SVGNode internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        @JvmStatic
        external fun _nGetTag(ptr: Long): Int

        init {
            staticLoad()
        }
    }

    val tag: SVGTag
        get() = try {
            Stats.onNativeCall()
            SVGTag.values().get(_nGetTag(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
}