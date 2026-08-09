package org.jetbrains.skia

import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skiko.RenderException

/** An OpenGL function table assembled with a platform-provided proc resolver. */
actual class GLAssembledInterface actual internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    actual companion object {
        @OptIn(ExperimentalForeignApi::class)
        actual fun createFromNativePointers(ctxPtr: NativePointer, fPtr: NativePointer): GLAssembledInterface {
            if (fPtr == NullPointer) throw RenderException("Function pointer must not be null")
            Stats.onNativeCall()
            val ptr = _nCreateFromNativePointers(ctxPtr, fPtr)
            if (ptr == NullPointer) throw RenderException("Can't assemble OpenGL interface")
            return GLAssembledInterface(ptr)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_GLAssembledInterface__1nCreateFromNativePointers")
private external fun _nCreateFromNativePointers(ctxPtr: NativePointer, fPtr: NativePointer): NativePointer

/** Creates an OpenGL context using an explicitly assembled GL interface. */
actual fun DirectContext.Companion.makeGLWithInterface(assembledInterface: GLAssembledInterface): DirectContext {
    if (assembledInterface._ptr == NullPointer) throw RenderException("Interface pointer must not be null")
    Stats.onNativeCall()
    val ptr = _nMakeGLWithInterface(assembledInterface._ptr)
    if (ptr == NullPointer) throw RenderException("Can't create OpenGL DirectContext with provided interface")
    return DirectContext(ptr)
}

@ExternalSymbolName("org_jetbrains_skia_DirectContext__1nMakeGLWithInterface")
private external fun _nMakeGLWithInterface(interfacePtr: NativePointer): NativePointer
