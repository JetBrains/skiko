package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.impl.*

class TypefaceFontProviderWithFallback private constructor(
    ptr: NativePointer,
) : TypefaceFontProvider(ptr) {

    constructor() : this(_nMakeAsFallbackProvider())

    companion object {
        init {
            Library.staticLoad()
        }
    }

    override fun registerTypeface(typeface: Typeface?, alias: String?): TypefaceFontProviderWithFallback =
        nativeCall(typeface) { typeface ->
            interopScope {
                _nRegisterTypefaceForFallback(
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

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nMakeAsFallbackProvider")
// No ForceNativeThreadState: creates empty maps and an empty typeface list, so it is short and cannot block.
// Native call path: TypefaceFontProviderWithFallback::TypefaceFontProviderWithFallback
private external fun _nMakeAsFallbackProvider(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nRegisterTypefaceForFallback")
// Needs ForceNativeThreadState: adds the typeface to the fallback list, then with no alias asks CoreText for its family
// name.
// Native call path: registerTypeface -> SkTypeface_Mac::onGetFamilyName -> CTFontCopyFamilyName
@ForceNativeThreadState
private external fun _nRegisterTypefaceForFallback(
    ptr: NativePointer,
    typefacePtr: NativePointer,
    alias: InteropPointer
): Int
