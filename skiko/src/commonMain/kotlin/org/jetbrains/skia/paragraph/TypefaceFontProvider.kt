package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope

open class TypefaceFontProvider internal constructor(
    ptr: NativePointer
) : FontMgr(ptr) {

    constructor(): this(TypefaceFontProvider_nMake())

    companion object {
        init {
            staticLoad()
        }
    }

    open fun registerTypeface(typeface: Typeface?, alias: String? = null): TypefaceFontProvider {
        return try {
            Stats.onNativeCall()
            interopScope {
                TypefaceFontProvider_nRegisterTypeface(
                    _ptr,
                    getPtr(typeface),
                    toInterop(alias)
                )
            }
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(typeface)
        }
    }

    init {
        Stats.onNativeCall()
    }
}