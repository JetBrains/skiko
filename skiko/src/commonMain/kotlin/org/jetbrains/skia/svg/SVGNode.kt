@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import kotlin.jvm.JvmStatic

abstract class SVGNode internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_svg_SVGNode__1nGetTag")
        external fun _nGetTag(ptr: NativePointer): Int

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