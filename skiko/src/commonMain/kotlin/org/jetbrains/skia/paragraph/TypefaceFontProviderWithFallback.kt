package org.jetbrains.skia.paragraph

import org.jetbrains.skia.Typeface
import org.jetbrains.skia.impl.Library
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

class TypefaceFontProviderWithFallback private constructor(
    ptr: NativePointer,
) : TypefaceFontProvider(ptr) {

    constructor() : this(TypefaceFontProviderWithFallback_nMakeAsFallbackProvider())

    companion object {
        init {
            Library.staticLoad()
        }
    }

    override fun registerTypeface(typeface: Typeface?, alias: String?): TypefaceFontProviderWithFallback {
        return try {
            Stats.onNativeCall()
            interopScope {
                TypefaceFontProviderWithFallback_nRegisterTypefaceForFallback(
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