package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import kotlin.jvm.JvmStatic

class RuntimeEffect internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        @JvmStatic
        fun makeForShader(sksl: String): RuntimeEffect = nativeCallFromStatic {
            interopScope {
                makeFromResultPtr(_nMakeForShader(toInterop(sksl)))
            }
        }

        @JvmStatic
        fun makeForColorFilter(sksl: String): RuntimeEffect = nativeCallFromStatic {
            interopScope {
                makeFromResultPtr(_nMakeForColorFilter(toInterop(sksl)))
            }
        }

        @JvmStatic
        fun makeForBlender(sksl: String): RuntimeEffect = nativeCallFromStatic {
            interopScope {
                makeFromResultPtr(_nMakeForBlender(toInterop(sksl)))
            }
        }

        init {
            staticLoad()
        }
    }

    fun makeShader(
        uniforms: Data?,
        children: Array<Shader?>?,
        localMatrix: Matrix33?
    ): Shader = nativeCall(uniforms, children) { uniforms, children ->
        val childCount = children?.size ?: 0
        val childrenPtrs = NativePointerArray(childCount)
        for (i in 0 until childCount) childrenPtrs[i] = children!![i].pointerOrNullPointer
        interopScope {
            Shader(
                _nMakeShader(
                    _ptr,
                    uniforms.pointerOrNullPointer,
                    toInterop(childrenPtrs),
                    childCount,
                    toInterop(localMatrix?.mat)
                )
            )
        }
    }

    fun makeBlender(uniforms: Data?): Blender = nativeCall(uniforms) { uniforms ->
        interopScope {
            Blender(_nMakeBlender(_ptr, uniforms.pointerOrNullPointer))
        }
    }

}

internal expect fun RuntimeEffect.Companion.makeFromResultPtr(ptr: NativePointer): RuntimeEffect

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeShader")
// Needs ForceNativeThreadState: loops over the caller's child shaders and checks each one, so its time grows with the
// number of children.
// Native call path: SkRuntimeEffect::makeShader -> verify_child_effects
@ForceNativeThreadState
private external fun _nMakeShader(
    runtimeEffectPtr: NativePointer, uniformPtr: NativePointer, childrenPtrs: InteropPointer,
    childCount: Int, localMatrix: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeBlender")
// Needs ForceNativeThreadState: can wait for another thread still initializing the shared empty data object.
// Native call path: SkRuntimeEffect::makeBlender -> SkData::MakeEmpty
@ForceNativeThreadState
private external fun _nMakeBlender(
    runtimeEffectPtr: NativePointer, uniformPtr: NativePointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeForShader")
// Needs ForceNativeThreadState: parses and optimizes shader source code with Skia's compiler, so its time grows with
// the source size.
// Native call path: SkRuntimeEffect::MakeForShader -> MakeFromSource -> SkSL::Compiler
@ForceNativeThreadState
private external fun _nMakeForShader(sksl: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter")
// Needs ForceNativeThreadState: parses and optimizes color-filter source code with Skia's compiler, so its time grows
// with the source size.
// Native call path: SkRuntimeEffect::MakeForColorFilter -> MakeFromSource -> SkSL::Compiler
@ForceNativeThreadState
private external fun _nMakeForColorFilter(sksl: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeForBlender")
// Needs ForceNativeThreadState: parses and optimizes blending source code with Skia's compiler, so its time grows with
// the source size.
// Native call path: SkRuntimeEffect::MakeForBlender -> MakeFromSource -> SkSL::Compiler
@ForceNativeThreadState
private external fun _nMakeForBlender(sksl: InteropPointer): NativePointer

//  The functions below can be used only in JS and native targets

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1Result_nGetPtr")
// No ForceNativeThreadState: hands over the raw effect pointer with no unref or destructor, so it is short and cannot
// block.
// Native call path: sk_sp::release
internal external fun Result_nGetPtr(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1Result_nGetError")
// No ForceNativeThreadState: returns the address of the error text, or null when there is none, so it is short and
// cannot block.
// Native call path: Result::errorText
internal external fun Result_nGetError(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1Result_nDestroy")
// No ForceNativeThreadState: frees the error text, and the effect was already released or is null, so it is short and
// cannot block.
// Native call path: Result::~Result -> SkString::~SkString
internal external fun Result_nDestroy(ptr: NativePointer)
