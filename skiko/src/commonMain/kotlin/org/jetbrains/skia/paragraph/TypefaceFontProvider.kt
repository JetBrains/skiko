package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope

class TypefaceFontProvider private constructor(
    ptr: NativePointer,
    private val isFallbackProvider: Boolean
) : FontMgr(ptr) {

    constructor(): this(TypefaceFontProvider_nMake(), false)

    companion object {
        init {
            staticLoad()
        }

        fun createAsFallbackProvider(): TypefaceFontProvider {
            return TypefaceFontProvider(ptr = TypefaceFontProvider_nMakeAsFallbackProvider(), isFallbackProvider = true)
        }
    }

    fun registerTypeface(typeface: Typeface?, alias: String? = null): TypefaceFontProvider {
        return try {
            Stats.onNativeCall()
            interopScope {
                if (!isFallbackProvider) {
                    _nRegisterTypeface(
                        _ptr,
                        getPtr(typeface),
                        toInterop(alias)
                    )
                } else {
                    _nRegisterTypefaceForFallback(
                        _ptr,
                        getPtr(typeface),
                        toInterop(alias)
                    )
                }
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


@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMake")
private external fun TypefaceFontProvider_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMakeAsFallbackProvider")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMakeAsFallbackProvider")
private external fun TypefaceFontProvider_nMakeAsFallbackProvider(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface")
private external fun _nRegisterTypeface(ptr: NativePointer, typefacePtr: NativePointer, alias: InteropPointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypefaceForFallback")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypefaceForFallback")
private external fun _nRegisterTypefaceForFallback(ptr: NativePointer, typefacePtr: NativePointer, alias: InteropPointer): Int
