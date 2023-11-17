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

class TypefaceFontProvider : FontMgr(TypefaceFontProvider_nMake()) {
    companion object {
        init {
            staticLoad()
        }
    }

    fun registerTypeface(typeface: Typeface?, alias: String? = null): TypefaceFontProvider {
        return try {
            Stats.onNativeCall()
            interopScope {
                _nRegisterTypeface(
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


@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMake")
private external fun TypefaceFontProvider_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface")
private external fun _nRegisterTypeface(ptr: NativePointer, typefacePtr: NativePointer, alias: InteropPointer)
