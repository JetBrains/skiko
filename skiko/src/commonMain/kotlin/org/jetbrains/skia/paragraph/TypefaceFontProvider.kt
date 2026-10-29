package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

open class TypefaceFontProvider internal constructor(
    ptr: NativePointer
) : FontMgr(ptr) {

    constructor(): this(TypefaceFontProvider_nMake())

    companion object {
        init {
            staticLoad()
        }
    }

    open fun registerTypeface(typeface: Typeface?, alias: String? = null): TypefaceFontProvider =
        nativeCall(typeface) { typeface ->
            interopScope {
                _nRegisterTypeface(
                    _ptr,
                    typeface.pointerOrNullPointer,
                    toInterop(alias)
                )
            }
            this
        }

    init {
        Stats.onNativeCall()
    }
}

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMake")
// No ForceNativeThreadState: creates an empty family map and holds no typeface yet, so it is short and cannot block.
// Native call path: TypefaceFontProvider::TypefaceFontProvider
private external fun TypefaceFontProvider_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface")
// Needs ForceNativeThreadState: with no alias, it asks CoreText for the typeface's family name.
// Native call path: registerTypeface -> SkTypeface_Mac::onGetFamilyName -> CTFontCopyFamilyName
@ForceNativeThreadState
private external fun _nRegisterTypeface(ptr: NativePointer, typefacePtr: NativePointer, alias: InteropPointer): Int
