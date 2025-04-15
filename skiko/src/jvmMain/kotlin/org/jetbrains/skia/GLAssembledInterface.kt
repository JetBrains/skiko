package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skiko.RenderException

class GLAssembledInterface internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        /**
         * Creates an OpenGL interface object.
         *
         * There must be a current OpenGL context set (i.e., by calling `eglMakeCurrent` before this), otherwise
         * this function will fail.
         * For more information refer to skia `GrGLMakeAssembledInterface` function.
         *
         * For example, this `GetGLFuncPtrByName` function could be passed as a [fPtr]:
         *  ```
         *  typedef void(*GLFuncPtr)();
         *  GLFuncPtr GetGLFuncPtrByName(void* ctx, const char* name);
         *  ```
         *
         * @param ctxPtr  native pointer to the custom context, that [fPtr] will be called with.
         * @param fPtr    native pointer to the function that takes [ctxPtr] and the OpenGL function name,
         *                and returns a function pointer of that OpenGL function (see skia `GrGLGetProc`).
         */
        fun createFromNativePointers(ctxPtr: NativePointer, fPtr: NativePointer): GLAssembledInterface {
            if (fPtr == NullPointer) throw RenderException("Function pointer must not be null")
            Stats.onNativeCall()
            val ptr = _nCreateFromNativePointers(ctxPtr, fPtr)
            if (ptr == NullPointer) throw RenderException("Can't assemble OpenGL interface")
            return GLAssembledInterface(ptr)
        }

        init {
            staticLoad()
        }
    }
}

private external fun _nCreateFromNativePointers(ctxPtr: NativePointer, fPtr: NativePointer): NativePointer
