package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

class TypefaceFontProviderWithFallback private constructor(
    ptr: NativePointer,
) : TypefaceFontProvider(ptr) {

    constructor() : this(_nMakeAsFallbackProvider())

    companion object {
        init {
            Library.staticLoad()
        }
    }

    override fun registerTypeface(typeface: Typeface?, alias: String?): TypefaceFontProviderWithFallback {
        return try {
            Stats.onNativeCall()
            interopScope {
                _nRegisterTypefaceForFallback(
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

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nMakeAsFallbackProvider")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nMakeAsFallbackProvider")
private external fun _nMakeAsFallbackProvider(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nRegisterTypefaceForFallback")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nRegisterTypefaceForFallback")
private external fun _nRegisterTypefaceForFallback(
    ptr: NativePointer,
    typefacePtr: NativePointer,
    alias: InteropPointer
): Int