package org.jetbrains.skia

import org.jetbrains.skia.impl.Native.Companion.NullPointer
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.interopScope
import kotlin.test.Test

fun getMakeGLAssembledInterfaceFunc(): NativePointer {
    return interopScope {
        _nGetMakeGLAssembledInterfaceFunc()
    }
}

class DirectContextTest {
    @Test
    fun makeFromInterfaceTest() {
        val fPtr = getMakeGLAssembledInterfaceFunc()
        val i = GLAssembledInterface.createFromNativePointers(NullPointer, fPtr)
        DirectContext.makeGLWithInterface(i)
    }
}

private external fun _nGetMakeGLAssembledInterfaceFunc(): NativePointer
