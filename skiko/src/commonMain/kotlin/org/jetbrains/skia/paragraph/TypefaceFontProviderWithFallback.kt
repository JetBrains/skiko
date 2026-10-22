package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
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

// Runnable: new TypefaceFontProviderWithFallback() creates empty maps and an empty typeface vector
@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nMakeAsFallbackProvider")
private external fun _nMakeAsFallbackProvider(): NativePointer

// Native: registerTypeface -> SkTypeface_Mac::onGetFamilyName -> CTFontCopyFamilyName (alias null)
@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nRegisterTypefaceForFallback")
@ForceNativeThreadState
private external fun _nRegisterTypefaceForFallback(
    ptr: NativePointer,
    typefacePtr: NativePointer,
    alias: InteropPointer
): Int