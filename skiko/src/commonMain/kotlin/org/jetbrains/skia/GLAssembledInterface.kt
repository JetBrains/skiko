package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt

/** An OpenGL function table assembled with a platform-provided proc-address resolver. */
expect class GLAssembledInterface internal constructor(ptr: NativePointer) : RefCnt {
    companion object {
        /**
         * Creates an OpenGL interface using [fPtr] as a native `GrGLGetProc` callback.
         * [ctxPtr] is passed through to the callback unchanged.
         */
        fun createFromNativePointers(
            ctxPtr: NativePointer,
            fPtr: NativePointer,
        ): GLAssembledInterface
    }
}

/** Creates an OpenGL direct context from an explicitly assembled interface. */
expect fun DirectContext.Companion.makeGLWithInterface(
    assembledInterface: GLAssembledInterface,
): DirectContext
